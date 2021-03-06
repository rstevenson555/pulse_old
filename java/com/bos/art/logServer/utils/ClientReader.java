package com.bos.art.logServer.utils;

import com.bos.art.logParser.records.*;
import com.bos.art.logServer.Queues.MessageUnloader;
import org.apache.commons.digester.Digester;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.management.*;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.lang.management.ManagementFactory;
import java.net.Socket;
import java.util.Calendar;
import java.util.Stack;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * every client-connection has a clientreader
 *
 * So then there's a logServerUnloader for every instance
 *
 */
public class ClientReader implements Runnable, ClientReaderMBean {

    private static final int COLLECTOR_INPUT_BUFFER = 1024 * 32;
    private static final int PARSER_BUFFER_SIZE = 8192;
    private Socket inputSocket = null;
    private SystemTask task = null;
    private static Logger logger = Logger.getLogger(ClientReader.class.getName());
    private static MessageUnloader commandUnloader = new MessageUnloader();
    private static int NUM_MESSAGE_HANDLERS = 2;
    private static MessageUnloaderHandler messageUnloaderHandlers[] = new MessageUnloaderHandler[NUM_MESSAGE_HANDLERS];
    private static BasicThreadFactory tFactory = new BasicThreadFactory.Builder()
            .namingPattern("MessageUnloaderHandler-%d")
            .build();
    // this controls how wide to scale the message handlers (very critical)
    private static ExecutorService executorService = Executors.newFixedThreadPool(NUM_MESSAGE_HANDLERS,tFactory);
    private static Stack saxParsers = new Stack();
    private static SAXParserFactory saxFactory;
    private InputStream inputStream = null;
    private Digester digester = null;
    private SAXParser parser = null;
    private static final Object lock = new Object(); // use for locking our stack
    private static boolean tryReset = true;
    int mode = NO_MODE_SET;
    private String command = null;
    static final int NO_MODE_SET = 0;
    static final int SOCKET_MODE = 1;
    static final int FILE_MODE = 2;
    static final int COMMAND_MODE = 3;
    private ClientCache clientCache = new ClientCache();
    private boolean debugging = false;
// for debugging purposes
    private static int filecounter = 0;
    private boolean encode_input = false;
    private static int SOCKET_BUFFER = 262144;
    private TPSCalculator tpsCalculator = new TPSCalculator();
    private static TPSCalculator outTPSCalculator = new TPSCalculator();
    static private int uniqueClientCounter = 1;
    private static Object initSyncLock = new Object();

    static {
        // Initialize SAX Parser factory defaults
        initSAXFactory(null, false, false);
    }

    class ResetParser implements Runnable {

        public void run() {
            logger.info("About to close input socket; a new one will re-open");
            try {
                if (inputSocket!=null)
                    inputSocket.close();
            } catch (Exception e) {
            } finally {
            }

            // reschedule the job for the next day
            init();
        }
    }

    public class ClientCache {

        public int objectsWritten = 0;
        public long writeTime = System.currentTimeMillis();
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            digester = null;
        } finally {
            super.finalize();
        }
    }

    /**
     * Initialize the SAX parser factory.
     *
     * @param factoryClassName The (optional) class name of the desired SAXParserFactory implementation. Will be assigned to
     * the system property <b>javax.xml.parsers.SAXParserFactory</b> unless this property is already set. If
     * <code>null</code>, leaves current setting alone.
     * @param namespaceAware true if we want a namespace-aware parser
     * @param validating true if we want a validating parser
     *
     */
    public static void initSAXFactory(String factoryClassName,
            boolean namespaceAware,
            boolean validating) {
//        System.setProperty("javax.xml.parsers.SAXParserFactory","org.apache.xerces.jaxp.SAXParserFactoryImpl");
        saxFactory = javax.xml.parsers.SAXParserFactory.newInstance();
        saxFactory.setNamespaceAware(namespaceAware);
        saxFactory.setValidating(validating);

        // Discard existing parsers
        saxParsers.clear();
    }

    /**
     * Get a SAX parser instance from the JAXP factory.
     *
     * @return a SAXParser instance.
     */
    public static SAXParser getSAXParser() {
        synchronized (lock) {
            if (!saxParsers.empty()) {
                return (SAXParser) saxParsers.pop();
            }

            try {
                SAXParser parser = saxFactory.newSAXParser();
                //parser.getParser().setEntityResolver(new DefaultEntityResolver());
                XMLReader reader = parser.getXMLReader();

                //reader.setEntityResolver(new DefaultEntityResolver());
                reader.setFeature("http://xml.org/sax/features/namespace-prefixes", false);
                //reader.setProperty("http://apache.org/xml/properties/input-buffer-size", new Integer(PARSER_BUFFER_SIZE));

                return parser;
            } catch (javax.xml.parsers.ParserConfigurationException e) {
                logger.error("XMLUtils.parserConfigurationException00", e);
                return null;
            } catch (SAXException se) {
                logger.error("XMLUtils.SAXException00", se);
                return null;
            }
        }
    }

    /**
     * Return a SAX parser for reuse.
     *
     * @param parser A SAX parser that is available for reuse
     */
    public static void releaseSAXParser(SAXParser parser) {
        if (!tryReset) {
            return;
        }

        // Free up possible ref. held by past contenthandler.
        try {
            XMLReader xmlReader = parser.getXMLReader();

            if (null != xmlReader) {
                synchronized (lock) {
                    saxParsers.push(parser);
                }
            } else {
                tryReset = false;
            }
        } catch (org.xml.sax.SAXException e) {
            tryReset = false;
        }
    }

    public ClientReader(Socket sock,boolean encode_input) {
        init();
        inputSocket = sock;
        inputStream = null;
        logger.info("Created a new Client Reader on Port: " + inputSocket.getPort());
        mode = SOCKET_MODE;
        this.encode_input = encode_input;
    }

    public ClientReader(String command) {
        init();
        inputSocket = null;

        mode = COMMAND_MODE;
        this.command = command;
    }

    /**
     * Called from the command - line
     */
    public ClientReader(InputStream input) {
        init();
        inputStream = input;
        inputSocket = null;
        mode = FILE_MODE;
    }

    public ClientReader() {
        init();
    }

    public static void main(String[] args) {
        ClientReader reader = new ClientReader();

        reader.run();
    }

    public void init() {
        Calendar c = Calendar.getInstance();
        // now + 1 day, 00:01:00
        c.roll(Calendar.DAY_OF_YEAR, 1);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 1);
        c.set(Calendar.SECOND, 0);

        initHandlers();
//        registerWithMBeanServer();
    }


    public void initHandlers() {
        logger.info("ClientReader.createHandlers");
        synchronized (initSyncLock) {
            for(int i = 0;i<messageUnloaderHandlers.length;i++) {
                if ( messageUnloaderHandlers[i]==null)
                    messageUnloaderHandlers[i] = new MessageUnloaderHandler();
            }
        }
    }

    private static AtomicInteger handlerCount = new AtomicInteger(0);

    private MessageUnloaderHandler setNextHandler(UserRequestEventDesc timing) {

        int current = handlerCount.get();
        if ( current >= messageUnloaderHandlers.length) {
            current = 0;
            handlerCount.set(current);
        }
        messageUnloaderHandlers[current].setTimingRecord(timing);

        int nextHandlerCount = handlerCount.incrementAndGet();
        if ( nextHandlerCount >= messageUnloaderHandlers.length) {
            nextHandlerCount = 0;
            handlerCount.set(nextHandlerCount);
        }

        return messageUnloaderHandlers[current];
    }

    private void registerWithMBeanServer() {
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        ObjectName name = null;
        try {
            int port = (inputSocket!=null) ?  inputSocket.getPort() : uniqueClientCounter++;
            name = new ObjectName("com.omx.collector:type=ClientReaderMBean,name=ClientReader-"+port);
            mbs.registerMBean(this, name);
        } catch (MalformedObjectNameException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (InstanceAlreadyExistsException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (MBeanRegistrationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (NotCompliantMBeanException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    private static DateTimeFormatter timeformat  = DateTimeFormat.forPattern("HH:mm:ss a");

    private static DateTime now = null;
    private static DateTime pagesMinute = new DateTime().plusMinutes(1);
    private static DateTime arMinute = new DateTime().plusMinutes(1);
    private static long pagesPerMinute = 0,arPerMinute = 0;
    
    /**
     * because of the structure of our xml, timing msg and external timing msg are members of UserRequestEventDesc, then
     * after each element is completed we turn those attribute members in top-level object and copy the child data into the
     * higher level objects
     */
    public void setNextEvent(UserRequestEventDesc event) {
        now = new DateTime();
        if (event.retrieveArtAccumulator() == null) {
            if (event.retrieveExternalTiming() == null) {
                if (event.retrieveTimingEvent() == null) {
                    if (event.retrieveConfigMessage() == null) {
                        if (event.retrievePageRecordEvent() == null) {
                            if (event.retrieveExceptionRecordEvent() == null) {
                                // send event out as a plain UserRequestEventDesc
                                // return event;
                                System.out.println("should not happen!!!!");
                                add(event);
                            } else {
                                ExceptionRecordEvent ere = event.retrieveExceptionRecordEvent();

                                ere.copyFrom(event);
                                add(ere);
                            }
                        } else {
                            PageRecordEvent pre = event.retrievePageRecordEvent();
                            pre.copyFrom(event);
                            // time format;  02:38:33 PM
                            // eventtime is a gregorianCalendar
                            //System.out.println("eventtime: " +pre.getEventTime());
                            //System.out.println("time: " + pre.getTime());

                            add(pre);
                        }
                    } else {
                        ConfigMessage config = event.retrieveConfigMessage();

                        config.copyFrom(event);
                        add(config);
                    }
                } else {
                    UserRequestTiming timing = event.retrieveTimingEvent();
                    timing.copyFrom(event);

                    // this is to ensure that we only process end type messages
                    if (!timing.getBegin() ) {
                        add(timing);
                    }
                }
            } else {
                ExternalEventTiming exttiming = event.retrieveExternalTiming();
                UserRequestTiming timing = (UserRequestTiming) event.retrieveTimingEvent();

                exttiming.copyFrom(timing);
                exttiming.copyFrom(event);

                // this is to ensure that we only process end type messages
                if (!exttiming.getBegin()) {
                    add(exttiming);
                }
            }
        } else {
            AccumulatorEventTiming accumulator = event.retrieveArtAccumulator();
            UserRequestTiming timing = (UserRequestTiming) event.retrieveTimingEvent();

            accumulator.copyFrom(timing);
            accumulator.copyFrom(event);
            
            // this is to ensure that we only process end type messages
            if (!accumulator.getBegin()) {
                add(accumulator);
            }
        }
    }

    public void run() {
        InputStream insource = null;
//        Thread.currentThread().setPriority(Thread.MIN_PRIORITY);

        try {
            parser = getSAXParser();
            digester = new Digester(parser);
            digester.push(this);
            digester.setValidating(false);

            // SYSTEM Task Node
            digester.addObjectCreate("SYSTEM/TASK", SystemTask.class);
            digester.addCallMethod("SYSTEM/TASK", "setTask", 0, new Class[]{String.class});
            digester.addSetNext("SYSTEM/TASK", "saveTask");
            // digester.addObjectCreate("FILESTARTXML", Messages.class);

            // EVENT NODE
            digester.addObjectCreate("FILESTARTXML/EVENT", UserRequestEventDesc.class);
            digester.addSetNext("FILESTARTXML/EVENT", "setNextEvent");

            digester.addSetProperties("FILESTARTXML/EVENT", new String[]{
                        "type", "priority", "id", "appname", "branchname", "instance", "servername", "classname", "context", "date", "time"},
                    new String[]{
                        "type", "priority", "eventId", "appName", "branchName", "instance", "serverName", "className", "context",
                        "date", "time"});

            // start of external event xml pattern matching
            digester.addObjectCreate("FILESTARTXML/EVENT/ExternalEvent", ExternalEventTiming.class);
            digester.addCallMethod("FILESTARTXML/EVENT/ExternalEvent/Payload", "setPayLoad", 0, new Class[]{String.class});
            digester.addCallMethod("FILESTARTXML/EVENT/ExternalEvent/Classification", "setClassification", 0,
                    new Class[]{int.class});
            digester.addSetNext("FILESTARTXML/EVENT/ExternalEvent", "setPayLoadReference");

            // start of artaccumulator pattern matching
            digester.addObjectCreate("FILESTARTXML/EVENT/AccumulatorEvent", AccumulatorEventTiming.class);
            digester.addCallMethod("FILESTARTXML/EVENT/AccumulatorEvent/Classification", "setClassification", 0,
                    new Class[]{int.class});
            digester.addCallMethod("FILESTARTXML/EVENT/AccumulatorEvent/Type", "setType", 0, new Class[]{String.class});
            digester.addCallMethod("FILESTARTXML/EVENT/AccumulatorEvent/Value", "setValue", 0, new Class[]{String.class});
            digester.addSetNext("FILESTARTXML/EVENT/AccumulatorEvent", "setAccumulatorReference");

            // start of HtmlPage pattern matching
            digester.addObjectCreate("FILESTARTXML/EVENT/HtmlPage", PageRecordEvent.class);
            digester.addSetProperties("FILESTARTXML/EVENT/HtmlPage", new String[]{"name"}, new String[]{"pageName"});

            digester.addCallMethod("FILESTARTXML/EVENT/HtmlPage/DATE", "setDate", 0, new Class[]{String.class});
            digester.addCallMethod("FILESTARTXML/EVENT/HtmlPage/TIME", "setTime", 0, new Class[]{String.class});
            digester.addCallMethod("FILESTARTXML/EVENT/HtmlPage/SessionId", "setSessionId", 0, new Class[]{String.class});
            digester.addCallMethod("FILESTARTXML/EVENT/HtmlPage/requestToken", "setRequestToken", 0, new Class[]{int.class});
            digester.addCallMethod("FILESTARTXML/EVENT/HtmlPage/requestTokenCount", "setRequestTokenCount", 0,
                    new Class[]{int.class});
            digester.addCallMethod("FILESTARTXML/EVENT/HtmlPage/startBase64EncodedSection", "setEncodedPage", 0,
                    new Class[]{String.class});
            digester.addSetNext("FILESTARTXML/EVENT/HtmlPage", "setHtmlPageRecordReference");

            // start of ExceptionEvent pattern matching
            digester.addObjectCreate("FILESTARTXML/EVENT/ExceptionEvent", ExceptionRecordEvent.class);
            digester.addSetProperties("FILESTARTXML/EVENT/ExceptionEvent", new String[]{"message"}, new String[]{"message"});
            digester.addCallMethod("FILESTARTXML/EVENT/ExceptionEvent/DATE", "setDate", 0, new Class[]{String.class});
            digester.addCallMethod("FILESTARTXML/EVENT/ExceptionEvent/TIME", "setTime", 0, new Class[]{String.class});
            digester.addCallMethod("FILESTARTXML/EVENT/ExceptionEvent/SessionId", "setSessionId", 0, new Class[]{String.class});
            digester.addCallMethod("FILESTARTXML/EVENT/ExceptionEvent/requestToken", "setRequestToken", 0, new Class[]{int.class});
            digester.addCallMethod("FILESTARTXML/EVENT/ExceptionEvent/requestTokenCount", "setRequestTokenCount", 0,
                    new Class[]{int.class});
            digester.addCallMethod("FILESTARTXML/EVENT/ExceptionEvent/exception/startBase64EncodedSection", "setEncodedException", 0,
                    new Class[]{String.class});
            digester.addCallMethod("FILESTARTXML/EVENT/ExceptionEvent/jspbeancontainer/startBase64EncodedSection",
                    "setEncodedBeanContainer", 0, new Class[]{String.class});
            digester.addSetNext("FILESTARTXML/EVENT/ExceptionEvent", "setExceptionRecordEvent");

            // this code handles a config message
            digester.addObjectCreate("FILESTARTXML/EVENT/FILENAME", ConfigMessage.class);
            digester.addSetNext("FILESTARTXML/EVENT/FILENAME", "setConfigMessage");
            digester.addCallMethod("FILESTARTXML/EVENT/FILENAME", "setFileName", 0, new Class[]{String.class});
            digester.addCallMethod("FILESTARTXML/EVENT/DATE", "setDate", 0, new Class[]{String.class});
            digester.addCallMethod("FILESTARTXML/EVENT/TIME", "setTime", 0, new Class[]{String.class});
            digester.addCallMethod("FILESTARTXML/EVENT/MAXSIZE", "setMaxSize", 0, new Class[]{int.class});
            digester.addCallMethod("FILESTARTXML/EVENT/MAXAGE", "setMaxAge", 0, new Class[]{int.class});

            // PAGE Node
            digester.addObjectCreate("FILESTARTXML/EVENT/PAGE", UserRequestTiming.class);
            digester.addSetNext("FILESTARTXML/EVENT/PAGE", "setTiming");

            digester.addSetProperties("FILESTARTXML/EVENT/PAGE", new String[]{
                        "begin", "name"}, new String[]{"begin", "page"});

            digester.addCallMethod("FILESTARTXML/EVENT/PAGE/QueryParams", "setQueryParams", 0, new Class[]{String.class});
            digester.addCallMethod("FILESTARTXML/EVENT/PAGE/DATE", "setDate", 0, new Class[]{String.class});
            digester.addCallMethod("FILESTARTXML/EVENT/PAGE/TIME", "setTime", 0, new Class[]{String.class});
            digester.addCallMethod("FILESTARTXML/EVENT/PAGE/ELAPSED", "setLoadTime", 0, new Class[]{int.class});

            // REQUESTTOKENDATA
            digester.addCallMethod("FILESTARTXML/EVENT/PAGE/TIMINGTOKENDATA/TIMINGTOKEN", "setRequestToken", 0,
                    new Class[]{int.class});
            digester.addCallMethod("FILESTARTXML/EVENT/PAGE/TIMINGTOKENDATA/CREATIONTIMEMILLIS", "setTokenCreationTime", 0,
                    new Class[]{long.class});
            digester.addCallMethod("FILESTARTXML/EVENT/PAGE/TIMINGTOKENDATA/CURRENTTIMEMILLIS", "setRequestEndTime", 0,
                    new Class[]{long.class});
            digester.addCallMethod("FILESTARTXML/EVENT/PAGE/TIMINGTOKENDATA/REQUESTTYPE", "setRequestType", 0,
                    new Class[]{int.class});

            // USERINFO NODE
            digester.addSetProperties("FILESTARTXML/EVENT/PAGE/USERINFO", new String[]{"sessionid"}, new String[]{"sessionId"});
            digester.addCallMethod("FILESTARTXML/EVENT/PAGE/USERINFO/IP", "setIpAddress", 0, new Class[]{String.class});
            digester.addCallMethod("FILESTARTXML/EVENT/PAGE/USERINFO/USERKEY", "setUserKey", 0, new Class[]{String.class});
            digester.addCallMethod("FILESTARTXML/EVENT/PAGE/USERINFO/BROWSER", "setBrowser", 0, new Class[]{String.class});

            // Parse our test input.
            InputSource inputSource = null;

            /*
             * String tmpEvent = new String(" <FILESTARTXML>"); tmpEvent += " <EVENT type=\"config\" classname=\"null\"
             * id=\"jsptiming\" appname=\"orderpoint\" branchname=\"CC_12345_BRANCH\" servername=\"000I0360B6/10.7.229.127\">
             * <FILENAME>testfile </FILENAME> <DATE>10/11/2003 </DATE> <TIME>01:03:00 PM </TIME> <MAXSIZE>20000 </MAXSIZE>
             * <MAXAGE>1 </MAXAGE> </EVENT>\n"; tmpEvent += " <EVENT type=\"log\" id=\"jsptiming\" appname=\"orderpoint\"
             * branchname=\"CC_12345_BRANCH\" servername=\"000I0360B6/10.7.229.127\"> <PAGE
             * name=\"/NASApp/Reliable/admin.loginCustomerContainer.web\" begin=\"false\"> <DATE>10/22/2003 </DATE> <TIME>
             * 02:25:44 PM </TIME> <ELAPSED>271 </ELAPSED> <USERINFO sessionid=\"42995B50A42CED342CB4C7383B37BA62\">
             * <IP>127.0.0.1 </IP> <USERKEY>029188WWebb405 </USERKEY> <BROWSER>Mozilla/5.0 (Windows; U; Windows NT 5.0;
             * en-US; rv:1.5) Gecko/20031007 </BROWSER> </USERINFO> </PAGE> </EVENT>\n"; tmpEvent += " <EVENT type=\"log\"
             * id=\"jsptiming\" appname=\"orderpoint\" branchname=\"CC_12345_BRANCH\" servername=\"000I0360B6/10.7.229.127\">
             * <PAGE name=\"/shop/admin.loginCustomerContainer.web\" begin=\"false\"> <DATE>10/22/2003 </DATE> <TIME>
             * 06:25:44 PM </TIME> <ELAPSED>271 </ELAPSED> <USERINFO sessionid=\"42995B50A42CED342CB4C7383B37BA62\">
             * <IP>127.0.0.1 </IP> <USERKEY>029188WWebb405 </USERKEY> <BROWSER>Mozilla/5.0 (Windows; U; Windows NT 5.0;
             * en-US; rv:1.5) Gecko/20031007 </BROWSER> </USERINFO> <QueryParams> <![CDATA[test=value]]> </QueryParams>
             * </PAGE>"; tmpEvent += " <Payload name=\"name\"> <![CDATA[ <greeting>Hello, world! </greeting>]]> </Payload>";
             * tmpEvent += " </EVENT>"; tmpEvent += "\n </FILESTARTXML>"; inputSource = new InputSource(new
             * StringReader(tmpEvent)); System.out.println("tmpEvent: " + tmpEvent);
             */

            if (mode == SOCKET_MODE) {

                PushbackInputStream pinput = null;
                logger.warn("Socket Receive Buffer Size: " + inputSocket.getReceiveBufferSize());
                inputSocket.setReceiveBufferSize(SOCKET_BUFFER);
                logger.warn("Socket Receive Buffer Size changed to : " + inputSocket.getReceiveBufferSize());

                registerWithMBeanServer();
                
                if (!encode_input) {
                    pinput = new PushbackInputStream(
                            new PatchFilterInputStream(
                          new BufferedInputStream(inputSocket.getInputStream(),64*1024)),
                    50);

                } else {
                    pinput = new PushbackInputStream(
                            //new PatchFilterInputStream(
                                new Base64EncodedInputStream(
                                    new Base64EncodedInputStream(
                                        //new XMLEncodedInputStream(
                                            new BufferedInputStream(inputSocket.getInputStream()),
                                            //"<Payload>", "</Payload>"),
                                    "<startBase64EncodedSection>", "</startBase64EncodedSection>"), /* error */
                            "<ExceptionEvent message=\"", "\">"), /* exception */
                        //),
                    50);
                }

                String dfilename = System.getProperty("user.dir") + File.separator + Thread.currentThread().getName() + "-" + filecounter++;
                if (debugging) {
                    System.out.println("Debug filename: " + dfilename);
                }

                pinput.unread(new String("<?xml version=\"1.0\"?>\n<FILESTARTXML>").getBytes());

                DebugInputStream ptmp = null;
                if (debugging) {
                    ptmp = new DebugInputStream(pinput, dfilename);
                }
                
                // we need to push this on the input stream, because this always
                // has to be at the top of an
                // xml document, we should of just had the clients handle this,
                // but for some reason
                // we didn't
                // But anyways this is kind of nice!
                logger.info("reading out local input, from client connections");

                if (debugging) {
                    insource = ptmp;
                } else {
                    insource = (InputStream) pinput;
                }
            }
            if (mode == FILE_MODE) {
                insource =
                        new com.bos.art.logServer.utils.Base64EncodedInputStream(new BufferedInputStream(inputStream),
                        "<startBase64EncodedSection>", "</startBase64EncodedSection>");

                // DebugInputStream dis = new DebugInputStream(insource,System.getProperty("user.dir")+File.separator+Thread.currentThread().getName()+"-"+filecounter++);

            } else if (mode == COMMAND_MODE) {
                commandUnloader.exitOnFinish();
                
                command = "<?xml version=\"1.0\"?>\n" + command;
                // System.out.println(command);
                insource = new BufferedInputStream(new ByteArrayInputStream(command.getBytes()));
            }

            inputSource = new InputSource(insource);

            // specify an 8k input buffer
            digester.parse(inputSource);

            tpsCalculator.incrementTransaction();
            outTPSCalculator.incrementTransaction();
            commandUnloader.addMessage((Object) task);

        } catch (java.io.IOException e) {
            String message = e.getMessage();

            if (message != null && message.indexOf("Connection reset") > -1) {
                System.err.println(
                        "The Connection Was Reset (Most likely by peer).  Ending thread: " + Thread.currentThread().getName());
                return;
            }
            logger.error("IO Error parsing stream: " + e.getMessage() + "\non Thread: " + Thread.currentThread().getName(),e);
        } catch (org.xml.sax.SAXParseException spe) {
            int c = 0;

            try {
                if ((c = insource.read()) == -1) {
                    // eof reached
                    logger.error("Reached end of input!");
                    return;
                }
            } catch (IOException io) {
                logger.error("The input stream is closed!");
                // the stream is closed!!!
                return;
            }

            logger.error("SAXParseException parsing input: " + spe + "\non Thread: " + Thread.currentThread().getName(),spe);

        } catch (org.xml.sax.SAXException se) {
            int c = 0;

            try {
                if ((c = insource.read()) == -1) {
                    // eof reached
                    logger.error("Reached end of input!");
                    return;
                }
            } catch (IOException io) {
                logger.error("The input stream is closed!");
                // the stream is closed!!!
                return;
            }

            logger.error("SAX Error parsing input: " + se + "\non Thread: " + Thread.currentThread().getName(),se);

        } catch (Exception ei) {
            logger.error("Unknown Error reading input: " + ei + "\non Thread: " + Thread.currentThread().getName(),ei);

        } finally {
            digester.clear();
            parser.reset();
            releaseSAXParser(parser);

            if (mode == FILE_MODE || mode == COMMAND_MODE) {
                commandUnloader.exitOnFinish();
                // test_unloader.exitOnFinish();

            }
            try {
                if (inputSocket!=null)
                    inputSocket.close();
            }catch(java.io.IOException io) {
                logger.error("Error closing client socket",io);
            }
        }
    }

    /**
     * send a command to put us in command mode, during command mode don't accept other messages
     *
     * @param task
     */
    public void saveTask(SystemTask task) {
        this.task = task;
    }

    private class MessageUnloaderHandler implements Runnable {

        UserRequestEventDesc timing;
        MessageUnloader unloader = new MessageUnloader();

        public void setTimingRecord(Object t) { timing = (UserRequestEventDesc)t;}

        public void run() {
            unloader.addMessage(timing);
        }
    }

    private void add(UserRequestEventDesc timing) {

        if (timing.getPriority() == -1) {
            System.out.println("priority is -1");
        }

        tpsCalculator.incrementTransaction();
        outTPSCalculator.incrementTransaction();

        MessageUnloaderHandler messageUnloaderHandler = setNextHandler(timing);
        executorService.execute(messageUnloaderHandler);

        //commandUnloader.addMessage((Object) timing);

        clientCache.objectsWritten++;
        if (clientCache.objectsWritten % 10000 == 0) {

//            logger.info(
//                    "time per 1000 puts to the ArtEngine out-queue: " + (System.currentTimeMillis() - clientCache.writeTime) / 10
//                    + " queue size is [" + commandUnloader.size() + "]");
            logger.info("messages per second to the ArtEngine out-queue: " + outTPSCalculator.getMessagesPerSecond());

            clientCache.writeTime = System.currentTimeMillis();
        }
    }


    /**
     * return messages per second calculation
     * @return
     */
    public long getMessagesPerSecond() {
        return tpsCalculator.getMessagesPerSecond();
    }

}
