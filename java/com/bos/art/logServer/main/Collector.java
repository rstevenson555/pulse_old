package com.bos.art.logServer.main;

import com.bos.art.logServer.Queues.MessageUnloader;
import com.bos.art.logServer.utils.ClientReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.yaml.snakeyaml.Yaml;

import javax.management.*;

public class Collector {

    public static int ART_COLLECTOR_PORT=0;
    public static int ART_ENGINE_PORT=0;
    public static String ART_ENGINE_MACHINE="";
    public static int ART_PAGE_VIEW_PORT=0;
    private static Logger logger = Logger.getLogger(Collector.class.getName());    
    private static final String STRESSENV_HOSTNAME_MATCH = "stress-";
    private static final String PRODENV_HOSTNAME_MATCH = "prod-";
    private static final String PRODCONFIG = "prodconfig";
    private static final String STRESSCONFIG = "stressconfig";
    private static final String LOCALCONFIG = "localconfig";
    private static int SOCKET_BUFFER = 262144;  // 256kb

    public static void init() {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        InputStream in = null;
        
        if (System.getProperty("CONFIG_OVERRIDE")!=null) {
            try {
                in = new FileInputStream(System.getProperty("CONFIG_OVERRIDE"));
            } catch (FileNotFoundException ex) {
                logger.error("Collector.init",ex);
            }
        } else {
            in = loader.getResourceAsStream("solutions_collector_config.yml");
        }
        InputStream logging = null;
        if (System.getProperty("LOG4J_OVERRIDE")!=null) {
            try {
                logging = new FileInputStream(System.getProperty("LOG4J_OVERRIDE"));
            } catch (FileNotFoundException ex) {
                logger.error("Collector.init",ex);
            }
        } else {
            logging = loader.getResourceAsStream("solutions_log4j.properties");
        }
        Properties properties = new Properties();
        try {
            properties.load(logging);
            PropertyConfigurator.configure(properties);

        } catch (IOException ex) {            
            logger.error("Error loading properties " ,ex);
        }
            
        Yaml yconfig = new Yaml();
        Map allConfig = (Map) yconfig.load(in);
        
        Map config;
        String hostname = "";
        try {
            hostname = java.net.InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException ex) {
            logger.error("Collector.loadConfig", ex);
        }

        if (hostname.indexOf(STRESSENV_HOSTNAME_MATCH) != -1) {
            config = (Map) allConfig.get(STRESSCONFIG);
            logger.info("Collector Loading the stressconfig");
        } else if (hostname.indexOf(PRODENV_HOSTNAME_MATCH) != -1) {
            logger.info("Collector Loading the prodconfig");
            config = (Map) allConfig.get(PRODCONFIG);
        } else {
            // set the default configuration to use based on hostname
            config = (Map) allConfig.get(LOCALCONFIG);
            logger.info("Collector Loading the localconfig");
        }

        //logger.info("Collector configuration:\n");
        //logger.info(config);
        
        Map engine = (Map)config.get("engine");
        Integer port = (Integer)engine.get("port");
        String host = (String)engine.get("machine");
        
        ART_ENGINE_PORT = port;
        ART_ENGINE_MACHINE = host;
        
        Map collector_settings = (Map)config.get("collector");
        port = (Integer)collector_settings.get("port");
        ART_COLLECTOR_PORT = port;
        ART_PAGE_VIEW_PORT = 0;
        
        logger.info("Setting Port:" + ART_ENGINE_PORT + ", and machine to:" + ART_ENGINE_MACHINE);
        logger.info("Setting the Collector to : " + ART_COLLECTOR_PORT);

    }
        
    static public void run(String[] args) {
        logger.info("running from: " + System.getProperty("user.dir"));
        
        boolean encode_input = false;
        BasicThreadFactory tFactory = new BasicThreadFactory.Builder()
                    .namingPattern("Collector-%d")
                    .build();

        ExecutorService pool = Executors.newCachedThreadPool(tFactory);

        if ( args[1].equals("-encode_input")) {
            encode_input = true;
        }
        if (args[0].equals("-server")) {
            MessageUnloader unloader = MessageUnloader.getInstance();
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            ObjectName name = null;
            try {
                name = new ObjectName("com.omx.MessageUnloader.jmx:type=MessageUnloaderMBean");
                mbs.registerMBean(unloader, name);
            } catch (MalformedObjectNameException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (InstanceAlreadyExistsException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (MBeanRegistrationException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (NotCompliantMBeanException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

            java.net.ServerSocket server = null;
            System.out.println("Running in server mode, listening on " + ART_COLLECTOR_PORT);
            while (true) {
                try {
                    server = new ServerSocket();
                    SocketAddress localSocketAddress = new InetSocketAddress(ART_COLLECTOR_PORT);
                    server.setReceiveBufferSize(SOCKET_BUFFER);
                    server.bind(localSocketAddress);

                    break;
                } catch (java.net.SocketException se) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ie) {
                        // just ignore
                    }
                    continue;
                } catch (java.io.IOException io) {
                    logger.error("io exception: " + io.getMessage());
                }
            }
            try {
                while (true) {
                    pool.execute(new ClientReader(server.accept(),encode_input));
                }
            } catch (java.io.IOException iex) { // time expired
                // try to re-connect to the server
                System.out.println("time expired");
            } catch (java.lang.Exception e) {
                System.out.println(e.getMessage());
            }
        } else if (args[0].equals("-localfile")) {
            System.out.println("Running in local-file mode\nusing file: " + args[1]);
            try {
                InputStream input = new FileInputStream(args[1]);

                pool.execute(new ClientReader(input));
            } catch (java.io.IOException iex) { // time expired
                // try to re-connect to the server
                System.out.println("IOError " + iex);
            } catch (java.lang.Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        } else if (args[0].equals("-command")) {
            System.out.println("Running in command mode\ncommand is: " + args[1]);
            try {
                pool.execute(new ClientReader("<SYSTEM><TASK>" + args[1] + "</TASK></SYSTEM>"));
            } catch (java.lang.Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("Usage:\n");
            System.out.println("-command {command to run}");
            System.out.println("-server");
            System.out.println("-localfile {filename to parse}");
            System.exit(1);
        }
    }

    static public void main(String[] args) {
        Collector.init();
        Collector.run(args);
    }

    public static void init(String args[]) {
        main(args);
    }

    public static void start() {
    }

    public static void stop() {
    }

    public static void destroy() {
    }
}
