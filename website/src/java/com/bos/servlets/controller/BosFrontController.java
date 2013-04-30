/* $Id$
 *
 * $Author$
 * $Date$
 * $Revision$
 *
 * This class is responsible for brokering user events to the appropriate event
 * handler classes using the struts framework.
 *
 * Copyright (c) 2003 Boise Office Solutions Inc.
 * 800 West Bryn Mawr, Itasca, IL 60148, U. S. A.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Boise Office Solutions, Inc. ("Confidential Information").
 */

package com.bos.servlets.controller;

import com.bcop.arch.logger.Logger;
import com.bcop.arch.utility.*;
import com.bos.cache.factory.impl.NonExpiringFactory;
import com.bos.cache.impl.MRUCache;
import com.bos.config.struts.BoiseActionMapping;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.digester.Digester;
import org.apache.struts.Globals;
import org.apache.struts.action.ActionServlet;
import org.apache.struts.config.ModuleConfig;
import org.xml.sax.InputSource;

public class BosFrontController extends ActionServlet implements ControllerInterface, FileChangedListener {
    protected String _version = "1.0";
    // used to cache the struts config file.
    private static MRUCache aliasCache = null;
    private static WatchDog strutsConfigWatcher = null;
    private static java.util.ArrayList aliasMapping = null;
    protected static final String ERROR = "javax.servlet.jsp.jspException";

    private static final Logger logger = (Logger)Logger.getLogger(BosFrontController.class.getName());

    static {
        //CacheFactory cf = CacheFactory.newInstance();
        //aliasCache = cf.createMRUCache();
        // a nice prime store 337 of the virtual names
        //aliasCache.setCacheSize(337);
        aliasCache = new MRUCache(337, new NonExpiringFactory());
    }

    /** this gets fired on when a config file that was being watched gets updated */
    public void fileDidChange(String filename) {
        logger.info("Got Notified of a file modification in " + filename);
        cleanUp();
        try {
            init();
        }
        catch (ServletException se) {
            logger.error(getClass().getName() + ": Unexpected error in fileDidChange", se);
        }
    }

    /**
     * Receives standard HTTP requests from the public service method and dispatches
     * them to the doXXX methods defined in this class.
     * @param request - the HttpServletRequest object that contains the request the client made of the servlet
     * @param response - the HttpServletResponse object that contains the response the servlet returns to the client
     */
    public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        BoiseActionMapping mapping = getBoiseActionMapping(request);

        logger.info("service():request.getRequestURL(): "+request.getRequestURL().toString());

        //Basic security checking, ActionHandlers will not be executed unless the customer has logged in.
        //The block is skipped when the user performs a login request.
        //If a null pointer exception is caught here error.jsp will still display a session timeout message
        //when appropriate.
        /*
        if (config.getLoginRequest() == null || config.getLoginRequest().equals("false")) {
            try {
                HttpSession session = request.getSession(false);
                opa.validate();
            } catch (Exception e) {
                request.setAttribute(ERROR, e);
                RequestDispatcher dispatcher = request.getRequestDispatcher("/error.jsp");
                dispatcher.forward(request, response);
                return;
            }
        }
        */

        request.setAttribute("xsltdoc", mapping.getStyleSheet());
        logger.info(Util.getHttpHeaderData(request));
        logger.info(Util.getHttpParamData(request));
            
        super.service(request, response);
    }

    public BosFrontController() {
        super();
    }

    /** called before a struts-config is re-read */
    public void cleanUp() {
        internal = null;
        processor = null;
        configDigester = null;
        servletName = null;
        getServletContext().removeAttribute(Globals.LOCALE_KEY);
        getServletContext().removeAttribute(Globals.MODULE_KEY);
        getServletContext().removeAttribute(Globals.MAPPING_KEY);
        getServletContext().removeAttribute(Globals.REQUEST_PROCESSOR_KEY);
        getServletContext().removeAttribute(Globals.DATA_SOURCE_KEY);
        getServletContext().removeAttribute(Globals.TRANSACTION_TOKEN_KEY);
        getServletContext().removeAttribute(Globals.SERVLET_KEY);
        //        getServletContext().removeAttribute(Globals.FORWARDS_KEY);
        //        getServletContext().removeAttribute(Globals.MAPPINGS_KEY);
        getServletContext().removeAttribute(Globals.ACTION_SERVLET_KEY);
        //        getServletContext().removeAttribute(Globals.FORM_BEANS_KEY);
        super.destroyModules();
    }

    /**
     * init method required from the HttpServlet class. This method will be called by the
     * application server when the servlet is first loaded in the servlet container. The
     * following steps will be performed as part of the invokation of this method: 1) initialize struts framework.
     * 2) initialize all the URL and File Watchers.
     * @return void
     */
    public void init() throws ServletException {
        super.init();
        // set ourselves in the util class, this is BAD, BAD
        // should be removed because Util should not be calling a
        // method in this controller, bad packaging!!!!
        Util.setController(this);

        try {
            logger.info("INIT of BosFrontController Servlet");

            loadStrutsConfigDocument(getServletContext(),getServletConfig());

            String masterConfigPath = getServletConfig().getInitParameter("config");

            URL url = installStrutsConfigWatcher( masterConfigPath );

            //ExternalEventMonitor externalEventMonitor = ExternalEventMonitor.getInstance();
            
            // GlobalPropertiesExternalEventListener listens for events and notifies
            // GlobalProperties. See GlobalProperties for more info
            //externalEventMonitor.addExternalEventListener(new GlobalProperties.GlobalPropertiesExternalEventListener());
            //externalEventMonitor.setPriority(Thread.MIN_PRIORITY);
            //externalEventMonitor.start();

        } catch (Throwable ex) {
            logger.error("init error", ex);
        }
    }

    /**
     * installs the file monitor for the struts config master .xml file
     * @param masterConfigPath the filename of the struts-config-master file
     * @return the url being monitored
     **/
    private URL installStrutsConfigWatcher( String masterConfigPath ) throws java.net.MalformedURLException
    {
        if ( strutsConfigWatcher!=null) {
            return null;
        }

        logger.info("Monitoring config file: " + masterConfigPath + " for changes");
        URL url = getServletContext().getResource(masterConfigPath);
        int filepos = 0;
        String externalForm = url.toExternalForm();
        if ((filepos = externalForm.indexOf("file:")) == -1) {
            // use the url watcher
            if (strutsConfigWatcher == null) {
                logger.info("    Using a url-based watcher for: " + url.toExternalForm());
                strutsConfigWatcher = new URLWatchDog(url);
                strutsConfigWatcher.setPriority(Thread.MIN_PRIORITY);
                strutsConfigWatcher.start();
            }
        } else {
            // it's a file based url so use a filewatcher
            if (strutsConfigWatcher == null) {
                logger.info("   Using a file-based watcher for: " + externalForm.substring(filepos + 5).toString());
                strutsConfigWatcher = new FileWatchDog(new File(externalForm.substring(filepos + 5)).toString());
                strutsConfigWatcher.setPriority(Thread.MIN_PRIORITY);
                strutsConfigWatcher.start();
            }
        }
        if (strutsConfigWatcher != null)
            strutsConfigWatcher.setDelegate(this);
        else {
            logger.warn(getClass().getName() + ": strutsConfigWatcher is NULL, it should not be...");
            // we can still continue going we just won't get notifications
        }
        return url;
    }

    /**
     * Retrieves a java bean representing the configuration found in struts-config.xml for the requested action handler.
     * @param request               the HttpServletRequest associated with Servlet Request
     * @return BoiseActionMapping
     */
    protected BoiseActionMapping getBoiseActionMapping(HttpServletRequest request) {
        String reqURI = request.getRequestURI().substring(request.getRequestURI().lastIndexOf("/"));
        int pos = 0;
        if ((pos = reqURI.lastIndexOf(".")) == -1) {
            logger.error("getBoiseActionMapping() Could not parse reqURI, a '.' is required but was not found.  reqURI: " +
                reqURI);
            return null;
        }
        pos = reqURI.lastIndexOf(".");
        String reqActionMap = reqURI.substring(0, pos);
        ModuleConfig ac = getModuleConfig(request);
        if (logger.isDebugEnabled()) {
            logger.debug("getBoiseActionMapping() reqURI: " + reqURI + " reqActionMap " + reqActionMap);
        }
        return (BoiseActionMapping)ac.findActionConfig(reqActionMap);
    }

    /**
     * @deprecated this should be removed once we are using websphere 5.0
     * it's provided so that util.java can call this class(util should not be calling this class that is BAD, BAD
     */
    public String getWebResourceAliasFromUtil(String physicalResourceName) {
        return getWebResourceAlias(physicalResourceName);
    }

    /**
     * method used to load the Struts config file in order to evaluate jsp aliases at runtime
     * this method is public so as any application code can perform a lookup.
     */
    public static String getWebResourceAlias(String physicalResourceName) {
        logger.info("physicalResourceName: "+physicalResourceName);
        String virtualName = null;
        if ( (virtualName = (String)aliasCache.get( physicalResourceName )) != null) {
            return virtualName;
        } else {

            Action action = null;
            for(int i = 0,tot = aliasMapping.size();i<tot;i++) {
                action = (Action)aliasMapping.get(i);
                if ( action.getForward().getPath().equals( physicalResourceName )) {
                    virtualName  = action.getPath();
                    // this name has the leading slash and no .web at the end
                    break;
                }
            }

            // if we find the physicalResourceName in the list then strip off the leading / and
            // append .web
            if ( virtualName != null) {

                StringBuffer nv = new StringBuffer();
                if ( virtualName.charAt(0)=='/') {
                    nv.append(virtualName.substring(1));
                } else {
                    nv.append(virtualName);
                }

                if ( virtualName.indexOf(".web")==-1) {
                    // not found
                    nv.append(".web");
                }


                String finalString = nv.toString();
                // put the modified alias name, ie with no leading slash and with .web appended
                // into the cache
                aliasCache.put( physicalResourceName, finalString );

                return finalString;
            } else {
                return "";
            }
        }
    }


       /**
     * inner class to hold the contents of the struts-config-master file
     * it maps to the struts action block of the struts-config file format
     **/
    static public class Action {
        private String path;
        private String type;
        private String scope;
        private Forward forward;

        public void setForward(Forward fwd) { forward = fwd; }
        public Forward getForward() { return this.forward; }

        public void setPath(String path) { this.path = path.substring(1); this.path+=".web"; }
        public String getPath() { return this.path; }
        public void setType(String type) { this.type = type; }
        public String getType() { return this.type; }
        public void setScope(String scope) { this.scope = scope; }
        public String getScope() { return this.scope; }

        public String toString() { StringBuffer buff = new StringBuffer();
            buff.append("path: ").append( path ).append("\n");
            buff.append("type: " ).append( type ).append("\n");
            buff.append("scope: " ).append( scope ).append("\n");
            return buff.toString();
        }
    }

    /**
     * inner class to hold the contents of the struts-config-master file
     * it maps to the struts forward line of the struts-config file format
     **/
    static public class Forward {
        private String name;
        private String path;
        private String redirect;

        public void setName(String name) { this.name = name; }
        public String getName() { return this.name; }
        public void setPath(String path) { this.path = path; }
        public String getPath() { return this.path; }
        public void setRedirect(String redirect) { this.redirect = redirect; }
        public String getRedirect() { return this.redirect; }

        public String toString() { StringBuffer buff = new StringBuffer();
            buff.append("name: ").append( name ).append("\n");
            buff.append("path: ").append( path ).append("\n");
            buff.append("redirect: " ).append( redirect ).append("\n");
            return buff.toString();
        }
    }

    /**
     * method used to initialize the loading of the struts config file
     */
    private static void loadStrutsConfigDocument(ServletContext sc, ServletConfig scg) {

        try {

            String configPath = scg.getInitParameter("config");
            if (logger.isDebugEnabled())
                logger.debug("loadStrutsConfigDocument config file: "+configPath );
            URL url = sc.getResource(configPath);

            InputSource is = new InputSource();
            is.setByteStream(url.openStream());
            is.setSystemId(url.toExternalForm());

            // using the commons-digester to map this xml to java - objects
            Digester digester = new Digester();
            digester.addObjectCreate("struts-config/action-mappings", java.util.ArrayList.class);

            digester.addObjectCreate("struts-config/action-mappings/action", Action.class);
            digester.addSetProperties("struts-config/action-mappings/action");

            digester.addSetNext("struts-config/action-mappings/action/forward","add");

            digester.addObjectCreate("struts-config/action-mappings/action/forward", Forward.class);
            digester.addSetProperties("struts-config/action-mappings/action/forward");

            digester.addSetNext("struts-config/action-mappings/action/forward","setForward");

            aliasMapping = (ArrayList)digester.parse(is);

            // if we are debugging dump contents
            if ( logger.isDebugEnabled() ) {
                for(int i = 0,tot = aliasMapping.size();i<tot;i++) {
                    Action action = (Action)aliasMapping.get(i);
                    logger.debug( "alias: " + action.getPath() + " physical name: " + action.getForward().getPath());
                }
            }
         }
         catch(java.io.IOException e)
         {
             logger.error("loadStrutsConfigDocument parsing error" + e.getMessage() );
         }
         catch(org.xml.sax.SAXException se)
         {
             logger.error("loadStrutsConfigDocument sax error" + se.getMessage() );
         }
         catch(Exception ei)
         {
             logger.error(" loadStrutsConfigDocument error" + ei.getMessage(),ei );
         }
     }

}

