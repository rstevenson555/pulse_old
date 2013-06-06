package com.bos.applets.arch;

import EDU.oswego.cs.dl.util.concurrent.ConcurrentHashMap;
import EDU.oswego.cs.dl.util.concurrent.FutureResult;
import com.bos.applets.arch.jcookies.CookiesJar;
import com.bos.art.logParser.broadcast.beans.*;
import com.bos.art.logParser.broadcast.beans.delegate.*;
import com.bos.art.logParser.records.SystemTask;
import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.codec.net.URLCodec;
import org.jgroups.*;
//import org.jgroups.blocks.PullPushAdapter;
import org.jgroups.protocols.*;

import org.jgroups.protocols.pbcast.GMS;
import org.jgroups.protocols.pbcast.NAKACK2;
import org.jgroups.protocols.pbcast.STABLE;
import org.jgroups.protocols.pbcast.FLUSH;
import org.jgroups.stack.IpAddress;
import org.jgroups.stack.ProtocolStack;

public class AppletMessageListener extends ReceiverAdapter implements ChannelListener, TransferClient {

    private ConcurrentHashMap serverMap = new ConcurrentHashMap();
    //private PullPushAdapter adapter;
    private JChannel channel = null;
    private List<Address> allViewMembers = null;
    private static Address serverAddress = null;
    private CookiesJar cookiesJar = null;
    private UserBean userBean = null;
    private String mhostApp = null;
    private String localIp = null;
    private static Timer timer = new Timer();
    //private ConnectionDetector detector = null;
    static private AppletMessageListener instance = new AppletMessageListener();
    private long lastReceive = System.currentTimeMillis();
    private List externalAccessRecordDelegates = new ArrayList();
    private List accessRecordDelegates = new ArrayList();
    private List queryBeanDelegates = new ArrayList();
    private List chatBeanDelegates = new ArrayList();
    private List memoryStatDelegates = new ArrayList();
    private List errorStatDelegates = new ArrayList();
    private List sessionDataDelegates = new ArrayList();
    private List userDelegates = new ArrayList();
    private List channelListeners = new ArrayList();
    private List browserDelegates = new ArrayList();

    private AppletMessageListener() {
    }

    public static AppletMessageListener getInstance() {
        if (instance == null) {
            instance = new AppletMessageListener();
        }
        return instance;
    }

    /*
     * private static class ReconnectTask extends TimerTask { public void run() { System.err.println("Dropping Communication
     * Channel"); instance.stop(); System.err.println("Restarting Communication Channel"); instance.restart();
     * System.err.println("Restarting Complete!"); } }
     */
    synchronized public void start(String host, String localIp, CookiesJar cookies) {
        try {
            System.out.println("AppletMessageListener.start(String,String,CookiesJar) called...");
            System.out.println("AppletMessageListener.instance: " + instance);
            final String mhost = host;
            cookiesJar = cookies;
            mhostApp = host;
            this.localIp = localIp;

            allViewMembers = new ArrayList();

            if (channel != null) {
                // it's already been created so exit
                //System.out.println("adapter : " + adapter);
//                try {
//                    System.out.println("starting connectiondetector");
//                    if (detector != null) {
//                        detector.interrupt();
//                    }
//                    detector = new ConnectionDetector();
//                    detector.start();
//                    System.out.println("about to reconnect");
//                    channel.setReceiver(this);
//                    channel.connect("ART-DATA");
////                    if (adapter != null) {
////                        adapter.start();
////                    } else {
////                        adapter = new PullPushAdapter(channel, (org.jgroups.MessageListener) instance, (org.jgroups.MembershipListener) instance);
////                    }
//                } catch (Exception e) {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//                //} catch (ChannelException e) {
//                    // TODO Auto-generated catch block
//                 //   e.printStackTrace();
//                }
                return;
            } else {
//                detector = new ConnectionDetector();
//                detector.start();
            }

            // need to replace this with a bundle
            Properties properties = new Properties();
            properties.put("log4j.rootLogger", "WARN, STDOUT");
            properties.put("log4j.appender.STDOUT", "org.apache.log4j.ConsoleAppender");
            properties.put("log4j.appender.STDOUT.layout", "org.apache.log4j.PatternLayout");
            properties.put("log4j.appender.STDOUT.layout.ConversionPattern", "%m%n");
            // PropertyConfigurator.configure(properties); 


            /**
             * tcp 
             *
             */
            channel = new JChannel(false);                 // 1
            ProtocolStack stack = new ProtocolStack(); // 2
            channel.setReceiver(this);
            channel.setProtocolStack(stack);       // 3

                                    
            InetAddress localinet = InetAddress.getByName(localIp);
            System.out.println("localip name: " + InetAddress.getByName(localIp));
            
            
            String ipWithoutDots= localIp.replaceAll("\\.","");
            //int bind_port = 8899;
            int bind_port = 9899; 
            do 
                bind_port = (int)((System.currentTimeMillis()+Integer.parseInt(ipWithoutDots)) % 60017);
            while(bind_port <1024);
            

            TCP tcp = new TCP();
            tcp.setBindToAllInterfaces(false);
            tcp.setEnableBundling(true);
            tcp.setBindAddress(localinet);                         
            tcp.setBindPort(bind_port);
            tcp.setLoopback(false);
            tcp.setEnableBundling(true);
            tcp.setDiscardIncompatiblePackets(true);
            tcp.setMaxBundleSize(128000);
            tcp.setReaperInterval(300000);


            System.out.println("localip: " + localIp);

            // soltuions
            int server_port = 7800;
            //int server_port = 8810;
            IpAddress artServerAddress = new IpAddress(mhost, server_port);
            InetSocketAddress serveraddr = new InetSocketAddress(mhost, server_port);
            List<IpAddress> initialhosts = new ArrayList<IpAddress>();
            
            //InetSocketAddress addr = new InetSocketAddress(null, bind_port)
            //IpAddress laddress = new IpAddress(localIp, bind_port);
            //IpAddress localAddress = new IpAddress(localinet, bind_port);
            IpAddress localAddress = new IpAddress(localinet,bind_port) {
 
                @Override
                public String toString() {
                    //return super.toString();
                    return "myname";
                }                                
                
            };

            initialhosts.add(artServerAddress);
            initialhosts.add(localAddress);
            
            TCPPING tcpping = new TCPPING();
            tcpping.setInitialHosts(initialhosts);
            tcpping.setPortRange(4);
            tcpping.setTimeout(4000);
            //tcpping.setNumPingRequests(1);
            tcpping.setNumInitialMembers(1);

            TCPGOSSIP gossip = new TCPGOSSIP();
            ArrayList<InetSocketAddress> slist = new ArrayList<InetSocketAddress>();
            slist.add(serveraddr);
            gossip.setInitialHosts(slist);
            
            System.out.println("initial hosts: " + artServerAddress);
            System.out.println("initial hosts: " + mhost);

            NAKACK2 nakack2 = new NAKACK2();
            nakack2.setDiscardDeliveredMsgs(true);
            nakack2.setUseMcastXmit(false);

            GMS gms = new GMS();
            gms.setJoinTimeout(5000);
            gms.setViewAckCollectionTimeout(3000);
            gms.setMergeTimeout(3000);  
            gms.setMaxJoinAttempts(1);
            gms.setViewBundling(true);
            gms.setMaxBundlingTime(1500);
            
            
FD_SOCK fdsock = new FD_SOCK();
            fdsock.bind_addr = localinet;
            FD fd = new FD();
            fd.setTimeout(5000);
            
            VERIFY_SUSPECT vsuspect = new VERIFY_SUSPECT();
            vsuspect.bind_addr = localinet;
            //vsuspect.setValue("timeout", 2500);

            stack.addProtocol(tcp).
                    addProtocol(gossip).
                    addProtocol(new MERGE2()).
                    addProtocol(fdsock).
                    addProtocol(fd).
                    addProtocol(vsuspect).
                    //addProtocol(new VERIFY_SUSPECT()).
                    //addProtocol(new BARRIER()).
                    addProtocol(nakack2).
                    addProtocol(new UNICAST()).                    
                    addProtocol(new STABLE()).
                    addProtocol(gms);
                    //addProtocol(new FLUSH());
//                    addProtocol(new UFC()).
//                    addProtocol(new MFC()).
//                    addProtocol(new FRAG2());
                    
                    
                    //addProtocol(new VIEW_SYNC());
           
                    

            try {
                stack.init();                         // 5
            } catch (Exception ex) {
                Logger.getLogger(AppletMessageListener.class.getName()).log(Level.SEVERE, null, ex);
            }


            channel.addChannelListener(instance);
            channel.connect("ART-DATA");
                        
            // connect to the channel in a background thread
//            Thread t = new Thread(new Runnable() {
//
//                public void run() {
//                    try {
//                        channel.addChannelListener(instance);
//                        channel.connect("ART-DATA");
//                        //adapter = new PullPushAdapter(channel, (org.jgroups.MessageListener) instance, (org.jgroups.MembershipListener) instance);
//                        //channel.setOpt(Channel.AUTO_RECONNECT, Boolean.TRUE);
//                        //channel.setOpt(Channel.LOCAL, Boolean.FALSE);
//
//
//                    } catch (Exception e) {
//                        // TODO Auto-generated catch block
//                        e.printStackTrace();
//                    //} catch (ChannelException e) {
//                        // TODO Auto-generated catch block
//                     //   e.printStackTrace();
//                    }
//                }
//            });
//
//            t.setDaemon(true);
//            t.start();

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    /**
     * inner class to try to determine if we are connected to the server if we are not , we drop the connection and try to
     * re-connect. sometimes, if members come up at the same time, they fail to connect
     *
     */
//    class ConnectionDetector extends Thread {
//
//        ConnectionDetector() {
//            //setPriority(Thread.NORM_PRIORITY-1);
//        }
//
//        public void run() {
//            try {
//                while (!interrupted()) {
//                    boolean connectionToOther = false;
//
//                    if ( channel == null) {
//                         Thread.sleep(60000);
//                         continue;
//                    }
//                    org.jgroups.util.UUID localAddress = (org.jgroups.util.UUID) channel.getAddress();
//
//                    if (channel != null) {
//                        if (channel.getView() != null) {
//                            if (channel.getView().getMembers() != null) {
//                                for (Iterator iter = channel.getView().getMembers().iterator(); iter!=null && iter.hasNext();) {
//                                    org.jgroups.util.UUID mbr = (org.jgroups.util.UUID) iter.next();
//                                    //System.out.println("member: " + mbr);
//                                    if (localAddress.toString().equals(mbr.toString())) {
//                                        //if ( localAddress.getPort() == mbr.getPort() ) {
//                                        // don't count it if it's us
//                                        continue;
//                                        //} 
//                                    } else {
//                                        connectionToOther = true;
//                                    }
//                                }
//                            }
//                        }
//                    }
//
//                    //if (!connectionToOther && (System.currentTimeMillis() - lastReceive) > 300000) {
//                    if (!connectionToOther && (System.currentTimeMillis() - lastReceive) > 30000) {
//                        System.err.println("Trying to reconnect to the local-cluster");
//                        //reconnect();
//                        lastReceive = System.currentTimeMillis();
//
//                        System.err.println("Restarting Communication Channel");
//                        //instance.restart();
//                        restart();
//                        System.err.println("Restarting Complete!");
//
//                    }
//
//                    // 60 seconds
//                    Thread.sleep(60000);
//
//                }
//            } catch (InterruptedException ie) {
//                // won't happen
//            }
//        }
//    }

    /*
     * shuts down the channel
     *
     */
    synchronized public void stop() {
        if (channel != null && channel.isConnected()) {
//            if (detector != null) {
//                detector.interrupt();
//                detector = null;
//            }
            System.out.println("about to stop adapter");
//            try {
//                adapter.stop();
//            } catch (Exception e) {
//            }
            System.out.println("about to shutdown channel");
            //channel.shutdown();
            try {
                synchronized (channel) {
                    channel.disconnect();
                }
            } catch (Exception e) {
            }
            System.out.println("about to close channel");
            try {
                channel.close();
            } catch (Exception e) {
            }
            channel = null;
            instance = null;
        }
    }

    synchronized public void restart() {
        try {
            channel.disconnect();
            channel.connect("ART-DATA");

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        //} catch (ChannelException e) {
            // TODO Auto-generated catch block
         //   e.printStackTrace();
        }
    }

    /**
     * send out chat messages
     *
     * @param msg , the chat msg bean
     *
     */
    public void sendChatMessage(ChatBean msg) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            ObjectOutputStream oos = new ObjectOutputStream(baos);

            ChatBean cbean = new ChatBean();
            cbean.setMessage(userBean.getName() + "> " + msg.getMessage());

            oos.writeObject(cbean);
        } catch (IOException e) {
            // this should not happen since we are writing to memory
            System.err.println("IOException" + e);
        }

        try {

            Message message = new Message(null, channel.getAddress(),
                    baos.toByteArray());

            channel.send(message);
        } catch (Exception cnce) {
            System.out.println("ChannelNotConnected");
        //} catch (ChannelClosedException cce) {
         //   System.out.println("ChannelClosed");
        }
    }

    public void sendQueryBean(QueryBean query) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            ObjectOutputStream oos = new ObjectOutputStream(baos);

            oos.writeObject(query);
        } catch (IOException e) {
            // this should not happen since we are writing to memory
            System.err.println("IOException" + e);
        }

        try {

            Message message = new Message(null, channel.getAddress(),
                    baos.toByteArray());

            channel.send(message);
        } catch (Exception cnce) {
            System.out.println("ChannelNotConnected");
        //} catch (ChannelClosedException cce) {
         //   System.out.println("ChannelClosed");
        }
    }

    public void sendHistoryBean(HistoryBean query) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            ObjectOutputStream oos = new ObjectOutputStream(baos);

            oos.writeObject(query);
        } catch (IOException e) {
            // this should not happen since we are writing to memory
            System.err.println("IOException" + e);
        }

        try {

            Message message = new Message(null, channel.getAddress(),
                    baos.toByteArray());

            channel.send(message);
        } catch (Exception cnce) {
            System.out.println("ChannelNotConnected");
        //} catch (ChannelClosedException cce) {
        //    System.out.println("ChannelClosed");
        }
    }

    public void sendCommandToServer(String command) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            ObjectOutputStream oos = new ObjectOutputStream(baos);

            SystemTask task = new SystemTask();
            task.setTask(command);
            oos.writeObject(task);
        } catch (IOException e) {
            // this should not happen since we are writing to memory
            System.err.println("IOException" + e);
        }

        try {

            Message message = new Message(serverAddress, channel.getAddress(),
                    baos.toByteArray());

            channel.send(message);
        } catch (Exception cnce) {
            System.out.println("ChannelNotConnected");
        //} catch (ChannelClosedException cce) {
         //   System.out.println("ChannelClosed");
        }
    }

//    public byte[] getState() {
//        return null;
//    }
//
//    public void setState(byte[] state) {
//    }
//
//    public void block() {
//    }
//
//    public void suspect(Address suspected_mbr) {
//        //System.out.println("suspect called for: " + suspected_mbr);
//    }


    public void viewAccepted(View new_view) {
        //System.out.println("public channel members: " + new_view);
        List<Address> mbrs = new_view.getMembers();

        sendViewChangedNotifications(mbrs);
    }

    private void sendViewChangedNotifications(List<Address> new_mbrs) {

        List<Address> joined, left;
        Address mbr;
        List<Address> old_mbrs = allViewMembers;

        for (Object o : new_mbrs) {
            System.out.println("view: " + o + " name: " + o.getClass());
        }
        if (old_mbrs == null || new_mbrs == null || new_mbrs.isEmpty()) {
            return;
        }

        joined = new ArrayList<Address>();
        for (int i = 0; i < new_mbrs.size(); i++) {
            mbr = new_mbrs.get(i);
            if (!old_mbrs.contains(mbr)) {
                joined.add(mbr);
            }
        }

        if (joined.size() > 0) {
            for (Iterator iter = userDelegates.iterator(); iter.hasNext();) {
                UserDelegate app = (UserDelegate) iter.next();
                boolean dontNotify = false;
                for (int i = 0, tot = joined.size(); i < tot; i++) {
                    org.jgroups.util.UUID imbr = (org.jgroups.util.UUID) joined.get(i);
                    if (imbr.toString().indexOf("art-app") != -1) {
                        dontNotify = true;
                    } else {
                        dontNotify = false;
                    }

                }

                if (!dontNotify) {
                    app.membersJoined();
                }
            }
            //System.out.println("localhost: " + ((IpAddress)channel.getLocalAddress()).getIpAddress().getHostName());
            org.jgroups.util.UUID imbr = null;
            for (int i = 0, tot = joined.size(); i < tot; i++) {
                imbr = (org.jgroups.util.UUID) joined.get(i);
                if (imbr.toString().indexOf("art-app") != -1) {
                    // skip putting art member in the cluster list
                    serverAddress = imbr;
                    continue;
                }
            }
        }

        left = new ArrayList<Address>();
        for (int i = 0; i < old_mbrs.size(); i++) {
            mbr = old_mbrs.get(i);
            if (!new_mbrs.contains(mbr)) {
                left.add(mbr);
            }
        }

        if (left.size() > 0) {
            org.jgroups.util.UUID imbr = null;
            for (int i = 0, tot = left.size(); i < tot; i++) {
                imbr = (org.jgroups.util.UUID) left.get(i);
                if (imbr.toString().indexOf("art-app") != -1) {
                    // skip putting art member in the cluster list
                    serverAddress = imbr;
                    continue;
                }

                System.out.println("userDelegates size: " + userDelegates.size());
                for (Iterator iter = userDelegates.iterator(); iter.hasNext();) {
                    UserDelegate app = (UserDelegate) iter.next();
                    //app.memberLeft( imbr.getIpAddress().getHostName(), imbr.getPort(), ((IpAddress)channel.getLocalAddress()).getIpAddress().getHostName() );
                    String address = imbr.toString();
                }
            }
        }

        for (int i = 0; i < left.size(); i++) {
            old_mbrs.remove(left.get(i));
        }

        for (int i = 0; i < joined.size(); i++) {
            old_mbrs.add(joined.get(i));
        }

        // announce self to joined members
        //System.out.println("ammounceSelf");
        //announceSelf(null);
    }

    public void setExternalAccessRecordsDelegate(ExternalAccessRecordsDelegate delegate) {
        // if this delegate already exists remove it
        externalAccessRecordDelegates.remove(delegate);
        // re-add it
        externalAccessRecordDelegates.add(delegate);
    }

    public void setAccessRecordsDelegate(AccessRecordsDelegate delegate) {
        // if this delegate already exists remove it
        accessRecordDelegates.remove(delegate);
        // re-add it
        accessRecordDelegates.add(delegate);
    }

    public void setQueryDelegate(QueryDelegate delegate) {
        queryBeanDelegates.remove(delegate);
        queryBeanDelegates.add(delegate);
    }

    public void setBrowserDelegate(BrowserDelegate delegate) {
        browserDelegates.remove(delegate);
        browserDelegates.add(delegate);
    }

    public void setChatDelegate(ChatDelegate delegate) {
        chatBeanDelegates.remove(delegate);
        chatBeanDelegates.add(delegate);
    }

    public void setMemoryStatDelegate(MemoryStatDelegate delegate) {
        memoryStatDelegates.remove(delegate);
        memoryStatDelegates.add(delegate);
    }
    
     public void setErrorStatDelegate(ErrorStatDelegate delegate) {
        errorStatDelegates.remove(delegate);
        errorStatDelegates.add(delegate);
    }

    public void setSessionDataDelegate(SessionDataDelegate delegate) {
        sessionDataDelegates.remove(delegate);
        sessionDataDelegates.add(delegate);
    }

    public void setUserDelegate(UserDelegate delegate) {
        userDelegates.remove(delegate);
        userDelegates.add(delegate);
    }

    public void setChannelListener(org.jgroups.ChannelListener listener) {
        channelListeners.remove(listener);
        channelListeners.add(listener);
    }

    /**
     * called by the channel listener when messages come in
     *
     */
    public void receive(org.jgroups.Message msg) {
        // we don't send null's
       // System.out.println("receive:" + msg);
        if (msg ==null) {
            // invalid object, just discard
            return;
        }
        lastReceive = System.currentTimeMillis();
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(msg.getBuffer());
            ObjectInputStream ois = new ObjectInputStream(bais);
            TransferBean data = (TransferBean) ois.readObject();

            data.registerClient(this);
            data.processBean(msg);
        } catch (ClassNotFoundException cnfe) {
            System.out.println("ClassNotFound in receive " + cnfe);
        } catch (java.io.EOFException eof) {
            //System.out.println("EOF in receive " + eof);
        } catch (java.io.IOException io) {
            System.out.println("IOException in receive " + io);
        }
    }

    final public void registerQuery(String key, final FutureResult serverObj) {
        serverMap.put(key, serverObj);
    }

    public void process(org.jgroups.Message msg, BrowserBean bean) {
        for (Iterator iter = browserDelegates.iterator(); iter.hasNext();) {
            BrowserDelegate app = (BrowserDelegate) iter.next();
            app.didReceiveBrowserBean(msg, bean);
        }
    }

    public void process(org.jgroups.Message msg, BeanBag bag) {
        for (TransferBean o : bag.getBeans()) {
            o.registerClient(this);
            o.processBean(msg);
        }

        for (Iterator aiter = accessRecordDelegates.iterator(); aiter.hasNext();) {
            AppDelegate app = (AppDelegate) aiter.next();
            app.didCompleteBagProcessing(msg);
        }
    }

    public void process(org.jgroups.Message msg, UserBean bean) {
System.out.println("process UserBean  " + bean);
        System.out.println("process userDelegates.size: " + userDelegates.size());        
        for (Iterator iter = userDelegates.iterator(); iter.hasNext();) {
            UserDelegate app = (UserDelegate) iter.next();
            app.didReceiveUserBean(msg, bean);
        }
    }

    public void process(org.jgroups.Message msg, ChatBean bean) {
        for (Iterator iter = chatBeanDelegates.iterator(); iter.hasNext();) {
            ChatDelegate delegate = (ChatDelegate) iter.next();
            delegate.didReceiveChatBean(msg, bean);
        }
    }

    public void process(org.jgroups.Message msg, HistoryBean bean) {
        System.out.println("got a HistoryBean: ");
    }

    public void process(org.jgroups.Message msg, QueryBean bean) {
        //System.out.println("got a queryBean: " + (new String(bean.getResponse())));
        FutureResult result = (FutureResult) serverMap.remove(bean.getSerial());
        result.set(bean);
    }

    public void process(org.jgroups.Message msg, AccessRecordsMinuteBean obj) {
        for (Iterator iter = accessRecordDelegates.iterator(); iter.hasNext();) {
            AccessRecordsDelegate delegate = (AccessRecordsDelegate) iter.next();
            delegate.didReceiveAccessRecordsBean(msg, obj);
        }
    }

    public void process(org.jgroups.Message msg, MemoryStatBean bean) {
        for (Iterator iter = memoryStatDelegates.iterator(); iter.hasNext();) {
            MemoryStatDelegate delegate = (MemoryStatDelegate) iter.next();
            delegate.didReceiveMemoryStatBean(msg, bean);
        }
    }
    
    public void process(org.jgroups.Message msg, ErrorStatBean bean) {
        for (Iterator iter = errorStatDelegates.iterator(); iter.hasNext();) {
            ErrorStatDelegate delegate = (ErrorStatDelegate) iter.next();
            delegate.didReceiveErrorStatBean(msg, bean);
        }
    }

    public void process(org.jgroups.Message msg, ExternalAccessRecordsMinuteBean bean) {
        for (Iterator iter = externalAccessRecordDelegates.iterator(); iter.hasNext();) {
            ExternalAccessRecordsDelegate delegate = (ExternalAccessRecordsDelegate) iter.next();
            delegate.didReceiveExternalAccessRecordsBean(msg, bean);
        }
    }

    public void process(org.jgroups.Message msg, SessionDataBean bean) {
        for (Iterator iter = sessionDataDelegates.iterator(); iter.hasNext();) {
            SessionDataDelegate delegate = (SessionDataDelegate) iter.next();
            delegate.didReceiveSessionDataBean(msg, bean);
        }
    }

    /**
     * send my name and ipaddress to other member
     *
     */
    private void announceSelf(IpAddress other) {
        // announce to all who I am
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            ObjectOutputStream oos = new ObjectOutputStream(baos);

            if (userBean == null) {
                String[] names = cookiesJar.getAllNames();
                Properties props = cookiesJar.read("yourform");
                userBean = new UserBean();

                String myAddress = channel.getAddress().toString();
                if (myAddress.charAt(0) == '/') {
                    myAddress = myAddress.substring(1);
                }

                int pos = 0;
                if ((pos = myAddress.indexOf("/")) != -1) {
                    myAddress = myAddress.substring(0, pos);
                }

                userBean.setIpAddress(myAddress);

                URLCodec codec = new URLCodec();
                String username = "no name";
                try {
                    username = codec.decode((String) props.get("Login"));
                } catch (Exception e) {
                    System.out.println("Error reading login");
                }

                if (username == null) {
                    String key = (new java.rmi.server.UID()).toString();
                    username = "unknown" + key;
                }
                userBean.setName(username);
            }
            oos.writeObject(userBean);
        } catch (IOException e) {
            // this should not happen since we are writing to memory
            System.err.println("IOException" + e);
        }

        try {

            Message message = new Message(other, channel.getAddress(),
                    baos.toByteArray());

            channel.send(message);
        } catch (Exception cnce) {
            System.out.println("ChannelNotConnected");
       // } catch (ChannelClosedException cce) {
         //   System.out.println("ChannelClosed");
        }
    }

    public void channelConnected(Channel channel) {
        // do nothing
        System.err.println("Channel Connected");

//        if (detector != null) {
//            detector.interrupt();
//        }
//
//        detector = new ConnectionDetector();
//        detector.start();

        for (Iterator iter = channelListeners.iterator(); iter.hasNext();) {
            org.jgroups.ChannelListener listener = (org.jgroups.ChannelListener) iter.next();
            listener.channelConnected(channel);
        }
    }

    public void channelDisconnected(Channel channel) {
        // do nothing
        System.err.println("Channel Disconnected");
        for (Iterator iter = channelListeners.iterator(); iter.hasNext();) {
            org.jgroups.ChannelListener listener = (org.jgroups.ChannelListener) iter.next();
            listener.channelDisconnected(channel);

        }
    }

    public void channelClosed(Channel channel) {
        // do nothing
        System.err.println("Channel Closed");
        for (Iterator iter = channelListeners.iterator(); iter.hasNext();) {
            org.jgroups.ChannelListener listener = (org.jgroups.ChannelListener) iter.next();
            listener.channelClosed(channel);

        }
    }

    public void channelShunned() {
        // do nothing
        System.err.println("Channel Shunned");
//        for (Iterator iter = channelListeners.iterator(); iter.hasNext();) {
//            org.jgroups.ChannelListener listener = (org.jgroups.ChannelListener) iter.next();
//            listener.channelShunned();
//
//        }
    }

    public void channelReconnected(Address addr) {
        // do nothing
        System.err.println("Channel ReConnected");
//        if (detector != null) {
//            detector.interrupt();
//        }
//
//        detector = new ConnectionDetector();
//        detector.start();

//        for (Iterator iter = channelListeners.iterator(); iter.hasNext();) {
//            org.jgroups.ChannelListener listener = (org.jgroups.ChannelListener) iter.next();
//            listener.channelReconnected(addr);
//
//        }
    }
}

