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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Stack;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspFactory;
import javax.servlet.jsp.PageContext;
import javax.xml.transform.Result;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TemplatesHandler;
import javax.xml.transform.sax.TransformerHandler;

import javax.xml.transform.Transformer;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.dom.DOMSource;
import org.xml.sax.EntityResolver;
import org.apache.xalan.xsltc.trax.DOM2SAX;
import org.apache.xml.serialize.Method;
import org.xml.sax.*;
import javax.xml.transform.OutputKeys;
import org.apache.xalan.templates.OutputProperties;
import org.apache.xml.serializer.OutputPropertiesFactory;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serializer.Serializer;
import org.apache.xml.serialize.SerializerFactory;
import org.apache.xml.utils.DefaultErrorHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import com.bcop.arch.exception.InitializationException;
import com.bcop.arch.logger.Logger;
import com.bcop.arch.utility.Util;
import com.bos.cache.Cache;
import com.bos.cache.CacheFactory;

public class XMLService extends HttpServlet {

    protected static String _version = "1.0";
    private static final Logger logger = (Logger)Logger.getLogger(XMLService.class.getName());
    private static final long TIME_IN_CACHE = ((120 * 60) * 1000); // 2 hours in millis
    private DOMStreamHelper helper = null;
    private static int STREAMERPORT = 5556; // start at 5556 and increment by 1 until we find a good one

    private final String XSLUserAgentsSupported = "MSIE 6.0,Java";
    private ArrayList ListofSupportedAgents = new ArrayList();
    private Cache stylesheetCache = null;
    private static javax.xml.parsers.SAXParserFactory       saxFactory;
    private static Stack                  saxParsers = new Stack();
    private static final String saxParserFactoryProperty =
        "javax.xml.parsers.SAXParserFactory";
    private static Object lock = new Object();  // use for locking our stack

    static {
        System.setProperty("javax.xml.transform.TransformerFactory","org.apache.xalan.processor.TransformerFactoryImpl");
        System.setProperty("javax.xml.parsers.DocumentBuilderFactory","org.apache.xerces.jaxp.DocumentBuilderFactoryImpl");
        //System.setProperty("javax.xml.parsers.SAXParserFactory","org.apache.xerces.jaxp.SAXParserFactoryImpl");

        initSAXFactory(null, true, false);

        logger.info("Using the: ["+ System.getProperty("javax.xml.transform.TransformerFactory") +"] TransformerFactory");
        logger.info("Using the: ["+ System.getProperty("javax.xml.parsers.DocumentBuilderFactory") +"] DocumentBuilderFactory");
        logger.info("Using the: ["+ System.getProperty("javax.xml.parsers.SAXParserFactory") +"] SAXParserFactory");
    }

    /**
     * doPost method required from the HttpServlet class.
     * @param request        the HttpServletRequest associated with XML Request
     * @param response       the HttpSerlvetResponse associated with XML Request
     */
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

            try {

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

            }
            catch (TransformerException te) {
                logger.error("transformerexception ", te);
            }
        }
        catch (Exception ie) {
            // the following is used to call our jsp error pages
            logger.error("baseXMLService Error: ", ie);

            JspFactory _jspxFactory = null;
            PageContext pageContext = null;

            _jspxFactory = JspFactory.getDefaultFactory();
            pageContext = _jspxFactory.getPageContext(this, request, response, "/order.displayErrorMessage.web", true, 20480, true);
            pageContext.handlePageException(ie);
        }
    }

    /** init method required from the HttpServlet class. */
    public void init() throws ServletException {
        super.init();

        CacheFactory cf = CacheFactory.newInstance();
        stylesheetCache = cf.createMRUCache();
        // a nice prime store 137 of the most frequent stylesheets
        stylesheetCache.setCacheSize(137);
        StringTokenizer TokenizerOfSupportedAgents = new StringTokenizer(XSLUserAgentsSupported, ",");

        while (TokenizerOfSupportedAgents.hasMoreElements()) {
            ListofSupportedAgents.add(TokenizerOfSupportedAgents.nextElement());
        }

        helper = new DOMStreamHelper();
        helper.start();

        logger.info("INIT of XMLService Servlet");
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

        if (isXSLSupportedByUserAgent(request.getHeader("user-agent"))) {
            if (logger.isDebugEnabled()) {
                logger.debug("text/xml");
            }

            String contentType = "text/xml";

            response.setContentType(contentType);

            BufferedOutputStream bos = new BufferedOutputStream(response.getOutputStream());
            logger.debug("HIT before create PI -> " + "type=\"text/xsl\" href=\"" + request.getContextPath() + stylesheet + "\"");
            Element element = model.getDocumentElement();
            ProcessingInstruction pi = model.createProcessingInstruction("xml-stylesheet",
                "type=\"text/xsl\" href=\"" + request.getContextPath() + stylesheet + "\"");
            element.getParentNode().insertBefore(pi, element);
            //element.appendChild(model.createProcessingInstruction("xml-stylesheet","type=\"text/xsl\"
            // href=\""+request.getContextPath()+stylesheet+"\""));

            logger.debug("HIT after create PI");
            this.sendHttpOutputBuffer(request, model, stylesheet, contentType, bos, t);
            bos.flush();
            bos.close();

            //end new code
            //out.println(text);
        }
        else {
            if (logger.isDebugEnabled()) {
                logger.debug("text/html");
            }

            String contentType = "text/html";

            response.setContentType(contentType);
            //new code
            BufferedOutputStream bos = new BufferedOutputStream(response.getOutputStream());

            this.sendHttpOutputBuffer(request, model, stylesheet, contentType, bos, t);

            bos.flush();
            bos.close();

            //end new code
            //out.println(text);
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
        isString = "/WEB-INF" + isString;
        logger.info("@@@@@@@@@@@@@@@"+isString+"@@@@@@@@@@@@@@@");
        BufferedInputStream is = null;

        try {

            /*
             *
             * We need to use the current stylesheet to set the systemID.  This will allow Xalan to
             * find other stylesheets that are included/imported in stylesheet.
             *
             * Note: Xalan has a bug that will not allow include/imported stylesheets to change contexts.
             * All include/import statements in our stylesheets should load stylesheet (href) from the
             * location of the stylesheet doing the importing (example, href="../Addl-Info.xsl"
             * NOT href="/boiseop/stylesheets/Addl-Info.xsl").
             *
             * Xalan Related Bug #10626
             * http://nagoya.apache.org/bugzilla/show_bug.cgi?id=10626
             *
             */

            //URL fileURL = cl.getResource(isString);
            URL fileURL = getServletContext().getResource(isString);
            is = new BufferedInputStream(fileURL.openStream());

            String systemID = fileURL.toExternalForm();

            InputSource stylesource = new InputSource(is);
            stylesource.setSystemId(systemID);
            SAXTransformerFactory saxTFactory = (SAXTransformerFactory)TransformerFactory.newInstance();

            TemplatesHandler templatesHandler = saxTFactory.newTemplatesHandler();
            templatesHandler.setSystemId(systemID);
            XMLReader reader = XMLReaderFactory.createXMLReader();
            reader.setContentHandler(templatesHandler);

            // this parse does the work of loading and parsing the stylesheet
            if (logger.isDebugEnabled()) {
                StringBuffer strBuffer = new StringBuffer();
                strBuffer.append("Stylesheet to be reloaded: ").append(isString).append("\n");
                strBuffer.append("stylesource.getSystemID: ").append(stylesource.getSystemId()).append("\n");
                strBuffer.append("templatesHandler.getSystemID: ").append(templatesHandler.getSystemId()).append("\n");
                logger.debug(strBuffer);
            }
            reader.parse(stylesource);

            // then get the templates object out of the handler
            Templates t = templatesHandler.getTemplates();

            is.close();

            stylesheetCache.put(new String(stylesheetName), new StylesheetCacheContainer(t));
            stylesource = null;
            return t;
        }
        catch (javax.xml.transform.TransformerConfigurationException tce) {
            logger.error("Transformer config error:", tce);
        }

        catch (org.xml.sax.SAXException se) {
            logger.error("Sax Error Message: " + se.getMessage());
            logger.error("Sax Exception", se);
        }
        catch (IOException io) {
            logger.error("IO Error", io);
        }

        return null; // should not happen
    }

    /**
     * a private inner-class that is used for retaining a cache of most recently used stylesheets
     * it contains a accesscount so that after x many loads we will flush and reload it from the file-system
     * so that it will pick up any inter-day stylesheet changes if we need that ability
     */
    private class StylesheetCacheContainer {
        private Templates templates;
        private long age;

        /**
         * contruct a stylesheetCacheContainer
         * @param t1 the value
         */
        StylesheetCacheContainer(Templates t1) {
            templates = t1;
            age = System.currentTimeMillis();
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

            TransformerFactory tfactory = TransformerFactory.newInstance();
            Serializer serializer = null;

            // Create a reader and set it's ContentHandler to be the
            // transformer.
            XMLReader reader = null;

            // Use JAXP1.1 ( if possible )
            javax.xml.parsers.SAXParser parser = null;
            try {
                parser = getSAXParser();
                reader = parser.getXMLReader();

                if (reader == null) {
                    reader = XMLReaderFactory.createXMLReader();
                }

            }
            catch (org.xml.sax.SAXException se) {
                //throw new org.xml.sax.SAXException( se );
                logger.error("sax error1" + se);
            }
            /*catch (javax.xml.parsers.ParserConfigurationException ex) {
                //throw new org.xml.sax.SAXException( ex );
                logger.error("sax error2" + ex);
            } */
            catch (javax.xml.parsers.FactoryConfigurationError ex1) {
                //throw new org.xml.sax.SAXException( ex1.toString() );
                logger.error("sax error3" + ex1);
            }
            catch (NoSuchMethodError ex2) {
                logger.error("sax error4" + ex2);
            }

            // Make sure the transformer factory we obtained supports both
            // DOM and SAX.
            if (tfactory.getFeature(SAXSource.FEATURE) && tfactory.getFeature(DOMSource.FEATURE)) {
                // We can now safely cast to a SAXTransformerFactory.
                SAXTransformerFactory sfactory = (SAXTransformerFactory)tfactory;

                // Create a ContentHandler that can liston to SAX events
                // and transform the output to DOM nodes.
                TransformerHandler handler = null;
                if (outputType != null && outputType.equals("text/html")) {

                    handler = sfactory.newTransformerHandler(templates);
                    serializer = org.apache.xml.serializer.SerializerFactory.getSerializer(
                        org.apache.xml.serializer.OutputPropertiesFactory.getDefaultMethodProperties("html"));
                    handler.getTransformer().setParameter("global.sessionID", ";jsessionid=" + request.getSession(false).getId());
                }
                else {
                    handler = sfactory.newTransformerHandler();
                    serializer = org.apache.xml.serializer.SerializerFactory.getSerializer(
                        org.apache.xml.serializer.OutputPropertiesFactory.getDefaultMethodProperties("xml"));
                }

                Properties props = templates.getOutputProperties();

                props.setProperty(OutputPropertiesFactory.S_KEY_INDENT_AMOUNT, "4");
                props.put("indent", "yes");

                serializer.setOutputFormat(props);
                // force the encoding this way
                ((org.apache.xml.serializer.SerializerBase) serializer).setEncoding(props.getProperty(OutputKeys.ENCODING));

                serializer.setOutputStream(bw);

                Result result = new SAXResult(serializer.asContentHandler());
                handler.setResult(result);

                try {

                    if (logger.isDebugEnabled()) {
                        reader.setFeature("http://xml.org/sax/features/validation", true);
                        reader.setFeature("http://apache.org/xml/features/validation/schema", true);
                        reader.setFeature("http://apache.org/xml/features/validation/dynamic", true);
                        reader.setFeature("http://apache.org/xml/features/continue-after-fatal-error", true);
                        reader.setDTDHandler(handler);
                    }

                    reader.setErrorHandler(new MyDefaultErrorHandler());
                    //reader.setFeature("http://xml.org/sax/features/namespace-prefixes", false);
                    //reader.setProperty("http://xml.org/sax/properties/lexical-handler", handler);

                    reader.setContentHandler(handler);

                    // Send the SAX events from the parser to the transformer,
                    // and thus to the DOM tree.
                    //
                    // Try to get SAX InputSource from DOM Source.
                    if (logger.isDebugEnabled()) {
                        logger.debug("=============================================================");
                        logger.debug("XML Document that was Validated:");
                        logger.debug("=============================================================");
                        logger.debug(org.apache.axis.utils.XMLUtils.DocumentToString(model));
                        logger.debug("=============================================================");
                    }

                    long start = 0;
                    if ( logger.isDebugEnabled()) {
                        start = System.currentTimeMillis();
                    }

                    InputSource input = dom2InputSource(model);

                    if (logger.isDebugEnabled()) {
                        logger.debug("=============================================================");
                        logger.debug("Errors from Schema Validation:");
                        logger.debug("=============================================================");
                    }
                    reader.parse(input);
                    releaseSAXParser( parser );

                    if ( logger.isDebugEnabled()) {
                        logger.debug("elapsed transform: " + (System.currentTimeMillis()-start));
                    }

                    if (logger.isDebugEnabled()) {
                        logger.debug("=============================================================");
                    }
                    input = null;

                }
                catch (org.xml.sax.SAXException se) {
                    logger.error("sendHttpOutputBuffer sax error" + se);
                }
            }
    }

    /** Initialize the SAX parser factory.
     *
     * @param factoryClassName The (optional) class name of the desired
     *                         SAXParserFactory implementation. Will be
     *                         assigned to the system property
     *                         <b>javax.xml.parsers.SAXParserFactory</b>
     *                         unless this property is already set.
     *                         If <code>null</code>, leaves current setting
     *                         alone.
     * @param namespaceAware true if we want a namespace-aware parser
     * @param validating true if we want a validating parser
     *
     */
    public static void initSAXFactory(String factoryClassName,
                                      boolean namespaceAware,
                                      boolean validating)
    {
        if (factoryClassName != null) {
            try {
                saxFactory = (javax.xml.parsers.SAXParserFactory)Class.forName(factoryClassName).
                    newInstance();
                /*
                 * Set the system property only if it is not already set to
                 * avoid corrupting environments in which Axis is embedded.
                 */
                if (System.getProperty(saxParserFactoryProperty) == null) {
                    System.setProperty(saxParserFactoryProperty,
                                       factoryClassName);
                }
            } catch (Exception e) {
                logger.error("exception00", e);
                saxFactory = null;
            }
       } else {
            saxFactory = javax.xml.parsers.SAXParserFactory.newInstance();
        }
        saxFactory.setNamespaceAware(namespaceAware);
        saxFactory.setValidating(validating);

        // Discard existing parsers
        saxParsers.clear();
    }

    private static boolean tryReset= true;

    /** Get a SAX parser instance from the JAXP factory.
     *
     * @return a SAXParser instance.
     */
    public static javax.xml.parsers.SAXParser getSAXParser() {
        synchronized( lock ) {
            if(!saxParsers.empty()) {
                return (javax.xml.parsers.SAXParser )saxParsers.pop();
            }
    
            try {
                javax.xml.parsers.SAXParser parser = saxFactory.newSAXParser();
                //parser.getParser().setEntityResolver(new DefaultEntityResolver());
                XMLReader reader = parser.getXMLReader(); 
                //reader.setEntityResolver(new DefaultEntityResolver());
                //reader.setFeature("http://xml.org/sax/features/namespace-prefixes", false);
                return parser;
            } catch (javax.xml.parsers.ParserConfigurationException e) {
                logger.error("parserConfigurationException00", e);
                return null;
            } catch (SAXException se) {
                logger.error("SAXException00", se);
                return null;
            }
        }
    }


    /** Return a SAX parser for reuse.
     * @param parser A SAX parser that is available for reuse
     */
    public static void releaseSAXParser(javax.xml.parsers.SAXParser parser) {
        if(!tryReset) return;

        //Free up possible ref. held by past contenthandler.
        try{
            XMLReader xmlReader= parser.getXMLReader();
            if(null != xmlReader){
//                xmlReader.setContentHandler(doNothingContentHandler);
//                xmlReader.setDTDHandler(doNothingContentHandler);
//                xmlReader.setEntityResolver(doNothingContentHandler);
//                xmlReader.setErrorHandler(doNothingContentHandler);
                synchronized ( lock ) {
                    saxParsers.push(parser);
                }
            }
            else {
                tryReset= false;
            }
        } catch (org.xml.sax.SAXException e) {
            tryReset= false;
        }
    }


    /**
     * this is not optimal, but we dump a dom to a chararraywriter, then use that to create a InputSource, will try
     * to find a better way!!!!
     * @param doc - the dom to convert to a InputSource
     */
    /*public static InputSource dom2InputSource(Document doc) throws IOException {

        InputSource is = null;

        Serializer serializer =
        org.apache.xml.serializer.SerializerFactory.getSerializer(
                    org.apache.xml.serializer.OutputPropertiesFactory.getDefaultMethodProperties("xml"));

        java.io.CharArrayWriter caw = new java.io.CharArrayWriter();
        serializer.setWriter(caw);

        serializer.asDOMSerializer().serialize(doc);
        is = new InputSource(new java.io.CharArrayReader(caw.toCharArray()));
        return is;
    } */


    /**
     *
     * This ugly mess of code is because current there is no provision to generate SAX events for
     * a in memory DOM object, so we have to somehow get the DOM into a format in which SAX can use it
     * So what I've done is start another thread which serializes the DOM out to the SOCKET's outputstream
     * then the main thread can parse from the sockets' inputstream, this accomplishes 2 things
     * first it improves performance (better than doing the 2 things sequentially) and it improves 
     * memory usage (by using streams, you only read/write chunks of data at a time
     *
     * DOM Level3 is supposed to address this issue, but is not yet fully implemented in XERCES
     * so this code can go away at that point
     **/

    /**
     * this will support 20 simultaneuous transforms on this jvm
     **/
    private com.bcop.arch.utility.DefaultThreadPool pool = new com.bcop.arch.utility.DefaultThreadPool(20);
    private java.util.Hashtable domMap = new java.util.Hashtable();

    /**
     * convert a DOM to a InputSource, so that sax can parse it
     **/
    public InputSource dom2InputSource(Document doc) throws IOException {

        InputSource is = null;

        java.net.Socket s= new java.net.Socket("127.0.0.1", STREAMERPORT );
        String key = (new java.rmi.server.UID()).toString();
        // put the dom in the hashtable, with this unique key (per jvm)
        domMap.put( key, doc);

        // write the key to the socket, so the other end of the socket
        // can lookup the key, and retrieve the DOM to serialize
        s.getOutputStream().write(new String(key +"\n").getBytes());
        s.getOutputStream().flush();

        is = new InputSource( new BufferedInputStream( s.getInputStream()) );
        return is;
    }


    /**
     * This class is the Socket listener on port 5556, when this class accepts connections
     * it delegates work to the DOMStreamer and then waits again for the next connection
     **/
    public class DOMStreamHelper extends Thread {
        public void run()
        {
            java.net.ServerSocket socket = null;
            while( true ) {
                try {
                    // listen for connections on 5556
                    socket = new java.net.ServerSocket(STREAMERPORT);
                    break;
                }
                catch(IOException io)
                {
                    logger.warn("DOMStreamHelper.run io error starting server socket: " + io + "\nWill try the next port num " + STREAMERPORT+1);
                    STREAMERPORT++; // increment and try the next port num
                }
            }

            while(true ) {
                try {
                    java.net.Socket s = socket.accept();

                    DOMStreamer streamer = new DOMStreamer(s);
                    // use the thread-pool to avoid the overhead of 
                    // starting/stopping the Threads
                    pool.invokeLater(streamer);
                }
                catch(IOException io)
                {
                    logger.error("DOMStreamHelper.run, io error accepting connections: " + io);
                    break;
                }
            }
        }
    }

    /**
     * This class is responsible for streaming the DOM over the socket
     * so a client can be reading it at the same time and we avoid the memory
     * overhead associated with streaming a DOM to memory and then parsing it
     **/
    public class DOMStreamer implements Runnable
    {
        private java.net.Socket socket = null;
        public DOMStreamer(java.net.Socket s)
        {
            socket = s;
        }
        /**
         * this is the heart of the beast, it does the grunt work of streaming the dom out to the 
         * socket
         **/
        public void run()
        {
            try {
                Serializer serializer =
                    org.apache.xml.serializer.SerializerFactory.getSerializer(
                org.apache.xml.serializer.OutputPropertiesFactory.getDefaultMethodProperties("xml"));
                java.io.BufferedOutputStream bos = new BufferedOutputStream( socket.getOutputStream() );
                serializer.setOutputStream( bos );
                java.io.BufferedReader br
                    = new java.io.BufferedReader(new java.io.InputStreamReader(socket.getInputStream()));

                // read the key sent over the wire, then look it up to get a DOM
                String key = br.readLine();
                Document doc = (Document)domMap.remove( key );
                // stream the DOM over the pipe(socket), a client can be reading the streamed output
                // as it is in - progress
                serializer.asDOMSerializer().serialize(doc);
                bos.flush();
                bos.close();
                socket.close();
            }
            catch(IOException io)
            {
                logger.error("DOMStreamer.run io error streaming xml");
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

