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
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.codec.net.URLCodec;
import org.jgroups.*;
import org.jgroups.protocols.*;
import org.jgroups.protocols.pbcast.GMS;
import org.jgroups.protocols.pbcast.NAKACK2;
import org.jgroups.protocols.pbcast.STABLE;
import org.jgroups.stack.IpAddress;
import org.jgroups.stack.ProtocolStack;

public class AppletMessageListener extends ReceiverAdapter implements ChannelListener, TransferClient {

    private Map serverMap = new ConcurrentHashMap();
    private JChannel channel = null;
    private List<Address> allViewMembers = null;
    private static Address serverAddress = null;
    private CookiesJar cookiesJar = null;
    private UserBean userBean = null;
    private String mhostApp = null;
    private String localIp = null;
    private static Timer timer = new Timer();
    static private AppletMessageListener instance = new AppletMessageListener();
    private List externalAccessRecordDelegates = new ArrayList();
    private List<AccessRecordsDelegate> accessRecordDelegates = new ArrayList<AccessRecordsDelegate>();
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

            allViewMembers = new CopyOnWriteArrayList();

            if (channel != null) {
                return;
            } else {
            }

            // need to replace this with a bundle
            Properties properties = new Properties();
            properties.put("log4j.rootLogger", "WARN, STDOUT");
            properties.put("log4j.appender.STDOUT", "org.apache.log4j.ConsoleAppender");
            properties.put("log4j.appender.STDOUT.layout", "org.apache.log4j.PatternLayout");
            properties.put("log4j.appender.STDOUT.layout.ConversionPattern", "%m%n");

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
            tcp.setBindAddress(localinet);                         
            tcp.setBindPort(bind_port);
            tcp.setLoopback(false);
            tcp.setEnableBundling(true);
            tcp.setDiscardIncompatiblePackets(true);
            tcp.setMaxBundleSize(128000);
            //tcp.setReaperInterval(300000);
            tcp.use_send_queues = true;
            tcp.sock_conn_timeout = 300;
                                 
            System.out.println("localip: " + localIp);

            // soltuions
            int server_port = 7800;
            //int server_port = 8810;
            IpAddress artServerAddress = new IpAddress(mhost, server_port);
            InetSocketAddress serverAddr = new InetSocketAddress(mhost, server_port);
            InetSocketAddress localAddr = new InetSocketAddress(localIp,bind_port);

            List<InetSocketAddress> slist = new ArrayList<InetSocketAddress>();
            slist.add(serverAddr);

            TCPGOSSIP gossip = new TCPGOSSIP();
            gossip.setInitialHosts(slist);
            gossip.setNumInitialMembers(2);
            gossip.setTimeout(8000);
            
            System.out.println("initial hosts: " + artServerAddress);
            System.out.println("initial hosts: " + mhost);

            NAKACK2 nakack2 = new NAKACK2();
            nakack2.setDiscardDeliveredMsgs(true);
            nakack2.setUseMcastXmit(false);

            GMS gms = new GMS();
            gms.setJoinTimeout(8000);
            gms.setViewAckCollectionTimeout(2000);
            gms.setMergeTimeout(8000);
            gms.setMaxJoinAttempts(2);
            gms.setViewBundling(true);

            //FD fd = new FD();
            //fd.setTimeout(5000);
            FD_ALL fd = new FD_ALL();
            fd.setTimeout(40000);
            fd.setInterval(8000);

            VERIFY_SUSPECT vsuspect = new VERIFY_SUSPECT();
//            vsuspect.bind_addr = localinet;
            
            //vsuspect.setValue("timeout", 4500);
            
            //vsuspect.use_icmp = true;

            MERGE2 merge2 = new MERGE2();
            merge2.setMaxInterval(30000);
            merge2.setMinInterval(10000);

            UNICAST unicast = new UNICAST();
            FRAG2 frag2 = new FRAG2();
            frag2.setFragSize(60000);
            
            stack.addProtocol(tcp).
                    addProtocol(gossip).
                    addProtocol(merge2).
                    addProtocol(fd).
                    addProtocol(vsuspect).
                    //addProtocol(new VERIFY_SUSPECT()).
                    //addProtocol(new BARRIER()).
                    addProtocol(nakack2).
                    addProtocol(unicast).
                    addProtocol(new STABLE()).
                    addProtocol(gms).
//                    addProtocol(new UFC()).
                    addProtocol(new MFC()).
                    addProtocol(frag2);
                    //addProtocol(flush);

            try {
                stack.init();                         // 5
            } catch (Exception ex) {
                Logger.getLogger(AppletMessageListener.class.getName()).log(Level.SEVERE, null, ex);
            }


            channel.addChannelListener(instance);
            channel.connect("ART-DATA");
                        
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }



    /*
     * shuts down the channel
     *
     */
    synchronized public void stop() {
        if (channel != null && channel.isConnected()) {
            System.out.println("about to stop adapter");
            System.out.println("about to shutdown channel");
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
        }
    }


    public void viewAccepted(View new_view) {
        List<Address> mbrs = new_view.getMembers();

        sendViewChangedNotifications(mbrs);
    }

  private void sendViewChangedNotifications(List<Address> new_members) {

        List<Address> joined_members, left_members;
        List<Address> old_members = allViewMembers;

        for (Address mbr : new_members) {
            System.out.println("view: " + mbr + " name: " + mbr.getClass());
        }
        if (old_members == null || new_members == null || new_members.isEmpty()) {
            return;
        }

        joined_members = new ArrayList<Address>();
        for(Address mbr:new_members) {
            if (!old_members.contains(mbr)) {
                joined_members.add(mbr);
            }
        }

        if (joined_members.size() > 0) {
            for (Iterator iter = userDelegates.iterator(); iter.hasNext();) {
                UserDelegate app = (UserDelegate) iter.next();
                boolean dontNotify = false;
                for (int i = 0, tot = joined_members.size(); i < tot; i++) {
                    org.jgroups.util.UUID imbr = (org.jgroups.util.UUID) joined_members.get(i);
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
            for (int i = 0, tot = joined_members.size(); i < tot; i++) {
                imbr = (org.jgroups.util.UUID) joined_members.get(i);
                if (imbr.toString().indexOf("art-app") != -1) {
                    // skip putting art member in the cluster list
                    serverAddress = imbr;
                    continue;
                }
            }
        }

        left_members = new ArrayList<Address>();
        for(Address mbr:old_members) {
            if (!new_members.contains(mbr)) {
                left_members.add(mbr);
            }
        }

        if (left_members.size() > 0) {
            org.jgroups.util.UUID imbr = null;
            for (int i = 0, tot = left_members.size(); i < tot; i++) {
                imbr = (org.jgroups.util.UUID) left_members.get(i);
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

        for(Address mbr:left_members) {
            old_members.remove(mbr);
        }

        for(Address mbr: joined_members) {
            old_members.add(mbr);
        }

        // announce self to joined_members members
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
     * This is the main entry point for data coming from the server
     *
     */
    @Override
    public void receive(Message msg) {
        if (msg ==null) {
            // invalid object, just discard
            return;
        }
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(msg.getBuffer());
            ObjectInputStream ois = new ObjectInputStream(bais);
            TransferBean data = (TransferBean) ois.readObject();

            // register us and dispatch the message.
            
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

    public void process(Message msg, BrowserBean bean) {
        for (Iterator iter = browserDelegates.iterator(); iter.hasNext();) {
            BrowserDelegate app = (BrowserDelegate) iter.next();
            app.didReceiveBrowserBean(msg, bean);
        }
    }

    public void process(Message msg, BeanBag bag) {
        for (TransferBean o : bag.getBeans()) {
            o.registerClient(this);
            o.processBean(msg);
        }

        for (Iterator aiter = accessRecordDelegates.iterator(); aiter.hasNext();) {        
            AppDelegate app = (AppDelegate) aiter.next();
            app.didCompleteBagProcessing(msg);
        }
    }

    public void process(Message msg, UserBean bean) {
        System.out.println("process UserBean  " + bean);
        System.out.println("process userDelegates.size: " + userDelegates.size());        
        for (Iterator iter = userDelegates.iterator(); iter.hasNext();) {
            UserDelegate app = (UserDelegate) iter.next();
            app.didReceiveUserBean(msg, bean);
        }
    }

    public void process(Message msg, ChatBean bean) {
        for (Iterator iter = chatBeanDelegates.iterator(); iter.hasNext();) {
            ChatDelegate delegate = (ChatDelegate) iter.next();
            delegate.didReceiveChatBean(msg, bean);
        }
    }

    public void process(Message msg, HistoryBean bean) {
        System.out.println("got a HistoryBean: ");
    }

    public void process(Message msg, QueryBean bean) {
        //System.out.println("got a queryBean: " + (new String(bean.getResponse())));
        FutureResult result = (FutureResult) serverMap.remove(bean.getSerial());
        result.set(bean);
    }

    public void process(Message msg, AccessRecordsMinuteBean obj) {
        for(AccessRecordsDelegate delegate:accessRecordDelegates) {
            delegate.didReceiveAccessRecordsBean(msg, obj);
        }
    }

    public void process(Message msg, MemoryStatBean bean) {
        for (Iterator iter = memoryStatDelegates.iterator(); iter.hasNext();) {
            MemoryStatDelegate delegate = (MemoryStatDelegate) iter.next();
            delegate.didReceiveMemoryStatBean(msg, bean);
        }
    }
    
    public void process(Message msg, ErrorStatBean bean) {
        for (Iterator iter = errorStatDelegates.iterator(); iter.hasNext();) {
            ErrorStatDelegate delegate = (ErrorStatDelegate) iter.next();
            delegate.didReceiveErrorStatBean(msg, bean);
        }
    }

    public void process(Message msg, ExternalAccessRecordsMinuteBean bean) {
        for (Iterator iter = externalAccessRecordDelegates.iterator(); iter.hasNext();) {
            ExternalAccessRecordsDelegate delegate = (ExternalAccessRecordsDelegate) iter.next();
            delegate.didReceiveExternalAccessRecordsBean(msg, bean);
        }
    }

    public void process(Message msg, SessionDataBean bean) {
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
        }
    }

    public void channelConnected(Channel channel) {
        // do nothing
        System.err.println("Channel Connected");

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
    }

    public void channelReconnected(Address addr) {
        // do nothing
        System.err.println("Channel ReConnected");
    }
}

