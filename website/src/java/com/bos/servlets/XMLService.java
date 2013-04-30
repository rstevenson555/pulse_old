/* $Id$
 *
 * $Author$
 * $Date$
 * $Revision$
 *
 * This class is responsible for generating XML Documents via an http request
 *
 * Copyright (c) 2003 Boise Office Solutions Inc.
 * 800 West Bryn Mawr, Itasca, IL 60148, U. S. A.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Boise Office Solutions, Inc. ("Confidential Information").
 */

package com.bos.servlets;

import EDU.oswego.cs.dl.util.concurrent.BoundedBuffer;
import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;
import EDU.oswego.cs.dl.util.concurrent.ThreadFactory;
import com.bcop.arch.builder.XMLUtils;
import com.bcop.arch.exception.InitializationException;
import com.bcop.arch.logger.Logger;
import com.bcop.arch.utility.Util;
import com.bos.cache.factory.impl.NonExpiringFactory;
import com.bos.cache.impl.MRUCache;
import com.sun.org.apache.xerces.internal.util.DefaultErrorHandler;
import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
import com.sun.org.apache.xml.internal.serializer.OutputPropertiesFactory;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.StringTokenizer;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.*;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

public class XMLService extends HttpServlet {

    protected static String _version = "1.0";
    private static final int STYLESHEET_CACHE_SIZE = 137;
    private static final Logger logger = (Logger)Logger.getLogger(XMLService.class.getName());
    private static final long TIME_IN_CACHE = ((120 * 60) * 1000); // 2 hours in millis

    private static int xsltTransformerPoolMaxSize = 30;
    private static int xsltTransformerKeepAliveTime = 1000 * 60 * 5;
    private static int xsltTransformerPoolMinSize = 4;

    private final String XSLUserAgentsSupported = "Java, MSIE 6.0";
    private ArrayList ListofSupportedAgents = new ArrayList();
    private MRUCache stylesheetCache = null;
    private static SAXTransformerFactory transformerFactory = null;
    private static PooledExecutor transformerPool = null;

    static {
        //System.setProperty("javax.xml.transform.TransformerFactory","org.apache.xalan.xsltc.trax.TransformerFactoryImpl");
        //System.setProperty("javax.xml.parsers.DocumentBuilderFactory","org.apache.xerces.jaxp.DocumentBuilderFactoryImpl");
        //System.setProperty("javax.xml.parsers.SAXParserFactory","org.apache.xerces.jaxp.SAXParserFactoryImpl");

        XMLUtils.initSAXFactory("com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl", true, false);

        logger.info("Using the: ["+ System.getProperty("javax.xml.transform.TransformerFactory") +"] TransformerFactory");
        logger.info("Using the: ["+ System.getProperty("javax.xml.parsers.DocumentBuilderFactory") +"] DocumentBuilderFactory");
        logger.info("Using the: ["+ System.getProperty("javax.xml.parsers.SAXParserFactory") +"] SAXParserFactory");
        transformerFactory = getTransformerFactory();

    }

    /**
     * get's the transformer and try's to convert it to a SAX TransformerFactory
     **/
    private static SAXTransformerFactory getTransformerFactory() {
        SAXTransformerFactory factory = (SAXTransformerFactory) TransformerFactory.newInstance();
        return factory;
    }

    /**
     * doPost method required from the HttpServlet class.
     * @param request        the HttpServletRequest associated with XML Request
     * @param response       the HttpSerlvetResponse associated with XML Request
     */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        //redirect call to doGet().  This needs to be here to handle POST for form actions
        //(i.e. updateItemQuantities.web).
        doGet(request, response);
    }

    /**
     * doGet method required from the HttpServlet class. This method will accept the baseXMLService request and send back the
     * appropriate XML or HTML data. The following steps will be performed as part of the invokation of this method:
     * 1) get the model and stylesheet parameters from the httpRequest
     * 2) construct the ModelXML object via dynamic class loading 
     * 3) initialize the ModelXML object
     * 4) invoke the processHttpResponse method
     * @param request        the HttpServletRequest associated with XML Request
     * @param response       the HttpSerlvetResponse associated with XML Request
     * @throws ServletException
     * @throws IOException
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {

        Document doc = (Document)request.getAttribute("xmldoc");
        String xsltFile = (String)request.getAttribute("xsltdoc");

        String model = request.getParameter("model");
        String stylesheet = xsltFile;

        try {
            if (logger.isDebugEnabled()) {
                logger.debug(Util.getHttpHeaderData(request));
                logger.debug(Util.getHttpParamData(request));
            }


            if (logger.isDebugEnabled()) {
                StringBuffer strBuffer = new StringBuffer();
                strBuffer.append("request.getMethod(): ");
                strBuffer.append(request.getMethod());
                strBuffer.append("\n");
                strBuffer.append("request.getPathInfo(): ").append(request.getPathInfo()).append("\n");
                strBuffer.append("request.getPathTranslated(): ").append(request.getPathTranslated()).append("\n");
                strBuffer.append("request.getRequestURI(): ").append(request.getRequestURI()).append("\n");
                strBuffer.append("request.getRequestedSessionId(): ").append(request.getRequestedSessionId()).append("\n");
                strBuffer.append("request.getServletPath(): ").append(request.getServletPath()).append("\n");
                logger.info(strBuffer);
            }

            //String wholeURL = Util.getRequestURL(request).toString();

            if (logger.isDebugEnabled()) {
                StringBuffer strBuffer = new StringBuffer();
                strBuffer.append("###################################\n");
                strBuffer.append("# stylesheet: ").append(stylesheet).append(" #\n");
                strBuffer.append("###################################");
                logger.debug(strBuffer);
            }

            processHttpResponse(request, response, stylesheet, doc);

        } catch (Throwable t) {
            logger.error("XMLService Error: ", t);
            //request.setAttribute(ERROR, t);
            RequestDispatcher dispatcher = request.getRequestDispatcher("/order.displayErrorMessage.web");
            dispatcher.forward(request, response);
        }
    }

    private static int counter = 0;
    /** Default thread factory. Creates worker threads. */
    private static final ThreadFactory FACTORY = new ThreadFactory() {
        public Thread newThread(Runnable command) {
            Thread t = new Thread(command, "XMLSerializer-"+counter++);

            return t;
        }
    };

    /** init method required from the HttpServlet class. */
    @Override
    public void init() throws ServletException {
        super.init();

        transformerPool = new PooledExecutor(new BoundedBuffer(10), xsltTransformerPoolMaxSize);
        transformerPool.setKeepAliveTime(xsltTransformerKeepAliveTime);
        transformerPool.setMinimumPoolSize(xsltTransformerPoolMinSize);
        transformerPool.discardWhenBlocked();
        transformerPool.setThreadFactory(FACTORY); 

        stylesheetCache = new MRUCache(STYLESHEET_CACHE_SIZE, new NonExpiringFactory());
        StringTokenizer TokenizerOfSupportedAgents = new StringTokenizer(XSLUserAgentsSupported, ",");

        while (TokenizerOfSupportedAgents.hasMoreElements()) {
            ListofSupportedAgents.add(TokenizerOfSupportedAgents.nextElement());
        }

        logger.info("INIT of XMLService Servlet");
        //logger.info("XALAN version: " + org.apache.xalan.Version.getVersion());
        //logger.info("XERCES version: " + org.apache.xerces.impl.Version.getVersion());
    }

    /**
     * method used to identify whether or not the user-agent (users browser) is able to handle XML/XSL transformation.
     * @param user-agent               The user agent string of the browser making the request.
     * @return boolean
     */
    protected boolean isXSLSupportedByUserAgent(String userAgent) {

        boolean isAgentSupported = false;

        for (int i = 0; i < ListofSupportedAgents.size(); i++) {
            if (userAgent.lastIndexOf((String)ListofSupportedAgents.get(i)) != -1) {
                isAgentSupported = true;
                break;
            }
        }
        return isAgentSupported;
    }


    /**
     * method used to handle the processesing of the httpServletResponse for the requested data model/style.
     * This method will be responsible for ensuring that the appropriate data (XML or HTML) is sent back
     * the the client browser.
     * @param request               the HttpServletRequest associated with XML Request
     * @param response              the HttpServletResponse associated with XML Request
     * @param model                 the data model that is being requested
     * @throws Exception
     * @return JspBeanContainer
     */
    protected void processHttpResponse(HttpServletRequest request,
    HttpServletResponse response,
    String stylesheet,
    Document model)
    throws Exception {

        //new
        if (logger.isDebugEnabled()) {
            StringBuffer strBuffer = new StringBuffer();
            strBuffer.append("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&\n");
            strBuffer.append("& ").append(request.getHeader("user-agent")).append(" &\n");
            strBuffer.append("& request.getContextPath(): " + request.getContextPath() + "&\n");
            strBuffer.append("& stylesheet ").append(stylesheet).append(" &\n");
            strBuffer.append("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&\n");
            strBuffer.append("AGENTS in props: ").append(XSLUserAgentsSupported).append("\n");
            strBuffer.append("isXSLSupportedByUserAgent(request.getHeader(user-agent)): " +
                isXSLSupportedByUserAgent(request.getHeader("user-agent")) + "\n");
            logger.info(strBuffer);
        }

        Templates t = getTemplates(stylesheet);
        final String agent = request.getHeader("user-agent");

        OutputStream bos = null;

        if ( response.getBufferSize()==0) {
            response.setBufferSize(8192);
            bos = response.getOutputStream();
            //logger.debug("Response Buffer Size was null");
        } else {
            bos = response.getOutputStream();
        }

        if (isXSLSupportedByUserAgent(agent)) {
            if (logger.isDebugEnabled()) {
                logger.debug("text/xml");
            }
   
            String contentType = "text/xml";
  
            response.setContentType(contentType);
 
            Element element = model.getDocumentElement();
            ProcessingInstruction pi = model.createProcessingInstruction("xml-stylesheet",
                "type=\"text/xsl\" href=\"" + request.getContextPath() + stylesheet + "\"");
            element.getParentNode().insertBefore(pi, element);

            sendHttpOutputBuffer(request, model, stylesheet, contentType, bos, t);

        }
        else {
            if (logger.isDebugEnabled()) {
                logger.debug("text/html");
            }

            String contentType = "text/html";

            response.setContentType(contentType);
            //new code
            sendHttpOutputBuffer(request, model, stylesheet, contentType, bos, t);

        }
    }


    /**
     * gets the templates object either from the cache or from the file-system
     * @param isString the name of the sylesheet
     * @return Templates the template object either from cache or newly created
     */
    public Templates getTemplates(String isString) throws IOException {

        StylesheetCacheContainer c = null;

        // lookup the stylesheet in the cache
        logger.debug("looking up: " + isString);
        c = (StylesheetCacheContainer)stylesheetCache.get(isString);
        if (c == null) {
            logger.debug("loading stylesheet, not in cache: " + isString);
            // reload actually adds the stylesheet to the cache as well as loads the stylesheet
            // from the filesystem
            return reload(isString);
        }
        else {
            // is it found in the cache
            // if the object has been accessed for > TIME_IN_CACHE time period
            // then we want to flush it from the cache and reload it
            // so that we can get interday stylesheet changes loaded
            long stale = System.currentTimeMillis() - (TIME_IN_CACHE); // ?? minutes
            if (stale > c.getAge()) {
                logger.debug("stale, re-loading stylesheet");
                return reload(isString);
            }
            else {
                // the object has been found in the cache
                // and the TIME_IN_CACHE is still below the flush value
                // so just return the template object here
                logger.debug("found stylesheet in cache");
                return c.getTemplates();
            }
        }
    }


    /**
     * reloads or loads a stylesheet from the file system if it's required or couldn't find it in the cache
     * @param isString the name of the stylesheet
     * @return Templates the new template object
     */
    private Templates reload(String isString) throws IOException {
        String stylesheetName = isString;
        InputStream is = null;

        try {

            URL fileURL = getServletContext().getResource(isString);

            java.net.URLConnection urlConn = fileURL.openConnection();

            is = urlConn.getInputStream();

            String systemID = fileURL.toExternalForm();
            XMLReader reader = XMLReaderFactory.createXMLReader();

            SAXSource source = new SAXSource( reader, new InputSource(is) );
            source.setSystemId( systemID );

            Templates templates = transformerFactory.newTemplates(source);

            source = null;

            stylesheetCache.put(new String(stylesheetName), new StylesheetCacheContainer(templates, urlConn));

            return templates;
        } catch(SAXException se) {
            logger.error("SAXException " +se);
        } catch (javax.xml.transform.TransformerConfigurationException tce) {
            logger.error("Transformer config error:", tce); 
        } 

        return null; // should not happen
    }

    /**
     * a private inner-class that is used for retaining a cache of most recently used stylesheets
     * it contains a accesscount so that after x many loads we will flush and reload it from the file-system
     * so that it will pick up any inter-day stylesheet changes if we need that ability
     */
    /**
     * a private inner-class that is used for retaining a cache of most recently used stylesheets
     * it contains a accesscount so that after x many loads we will flush and reload it from the file-system
     * so that it will pick up any inter-day stylesheet changes if we need that ability
     */
    private class StylesheetCacheContainer {
        private Templates templates;
        private long age;
        private long timestamp;
        private java.net.URLConnection urlConn;

        /**
         * contruct a stylesheetCacheContainer
         * @param t1 the value
         */
        StylesheetCacheContainer(Templates t1, java.net.URLConnection conn) {
            templates = t1;
            age = System.currentTimeMillis();
            urlConn = conn;
            timestamp = urlConn.getLastModified();
        }

        boolean isStale() {
            logger.info("last modified: " + urlConn.getLastModified());
            return timestamp != urlConn.getLastModified();
        }

        /**
         * gets the value object, which is a templates object
         * @return Templates - a trax templates object
         */
        Templates getTemplates() {
            return templates;
        }

        /** @return the number of times this object has been loaded from the cache */
        long getAge() {
            return age;
        }
    }



    /**
     *  creates the output to send to the response object
     *  @param stylesheet - the name of the stylesheet to use in the transform on the server-side
     *  @param outputType - the type of output xml, html etc
     *  @param bw - the output writer to send output to
     */
     private void sendHttpOutputBuffer(HttpServletRequest request, Document model, String stylesheet, String outputType,
            OutputStream bw, Templates templates) throws TransformerException, InitializationException, IOException {

        long start = 0;
        
        if (logger.isDebugEnabled())
        	start = System.currentTimeMillis();

        // Create a ContentHandler that can liston to SAX events
        // and transform the output to DOM nodes.
        Transformer transformer = null;

        if (outputType != null && outputType.equals("text/html")) {

            transformer = templates.newTransformer();
                
            transformer.setParameter("global.sessionID", ";jsessionid=" + request.getSession(false).getId());
            transformer.setOutputProperty(OutputKeys.METHOD, "html");
            transformer.setOutputProperty(OutputKeys.INDENT, "no");
            transformer.setOutputProperty(OutputPropertiesFactory.S_KEY_INDENT_AMOUNT, "4");
        } else {
            transformer = transformerFactory.newTransformer();

            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputPropertiesFactory.S_KEY_INDENT_AMOUNT, "4");
        }

        try {

            // create a communication pipe to the worker object running in another thread
            PipedInputStream pis = new PipedInputStream( );

            // create the worker and pass the Document object that needs to be serialized
            Worker worker = new Worker( model );

            // connect the 2 pipes together
            worker.connectToInput( pis );

            // do it!
            transformerPool.execute(worker);

            javax.xml.parsers.SAXParser parser = XMLUtils.getSAXParser();
            XMLReader reader = parser.getXMLReader();
            reader.setFeature("http://xml.org/sax/features/validation",false);

            SAXSource source = new SAXSource( reader, new InputSource( pis ));

            // now do the transform on the input stream (pipe from the worker thread)
            transformer.transform(source, new StreamResult(bw) );

            transformer = null;
            source = null;

            XMLUtils.releaseSAXParser( parser );

            if ( logger.isDebugEnabled())
                logger.debug("transform elapsed: " + (System.currentTimeMillis()-start));

        } catch(SAXException se) {
            logger.error("SAXException " +se);
        } catch(InterruptedException ie) {
            logger.error("XMLTransformationService interruptedexception");
        } catch (java.lang.RuntimeException rexception) {
            //if (rexception.getMessage().indexOf("java.net.SocketException") != -1) {
                //logger.error("XMLTransformationService SocketException, most likely client has broken the pipe, by moving on");
            //} else {
                logger.error("XMLTransformationService RuntimeException" + rexception,rexception);
            //}
        } 
    } 

/**
     * this inner class is a object that runs in the thread pool
     * which is responsible for serializing out the dom over a pipe
     * to the main processing thread
     **/
    public class Worker implements Runnable
    {
        private PipedOutputStream pos = null;
        private Object data = null;
        public Worker(Object o)
        {
            data = o;
        }
        public void connectToInput(PipedInputStream pis)
        {
            try {
                pos = new PipedOutputStream();
                pis.connect( pos );
            }
            catch(IOException io)
            {
                logger.error("ERror connecting pipes");
            }
        }
        public void run()
        {
            try {

                OutputFormat format = new OutputFormat((Document)data);

                XMLSerializer serializer = new XMLSerializer(pos,format);

                // stream the DOM over the pipe(socket), a client can be reading the streamed output
                // as it is in - progress
                serializer.asDOMSerializer().serialize((Document)data);                
                pos.flush();
                pos.close();

                serializer = null;
                pos = null;
                format = null;
            }
            catch(IOException io)
            {
                logger.error("IOException writing to pipe "+io);
            }
            catch(Exception e) {
                logger.error("Exception writing to pipe " + e);
            }
            finally {
                try {
                    if ( pos!=null)
                        pos.close();
                }
                catch(IOException io)
                { 
                    // shouldn't happen
                }
            }
        }
    }

    /**
     * our own error handler for sax errors or validation errors
     **/
    public class MyDefaultErrorHandler extends DefaultErrorHandler {
        public void warning(SAXParseException exception)
        throws SAXException {
            logger.debug("MyDefaultErrorHandler.warning: " + exception.getMessage());
        }

        public void error(SAXParseException exception)
        throws SAXException {
            logger.debug("MyDefaultErrorHandler.error: " + exception.getMessage());
        }
    }
}

