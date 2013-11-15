package com.bos.art.logParser.broadcast.network;



import com.bos.art.logParser.broadcast.beans.*;
import com.bos.art.logParser.broadcast.history.QueryBroadcast;
import com.bos.art.logParser.broadcast.history.QueryBroadcastFactory;
import com.bos.art.logParser.db.ConnectionPoolT;
import com.bos.art.logParser.server.Engine;
import com.bos.art.logParser.statistics.StatisticsModule;
import com.sun.rowset.WebRowSetImpl;
import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import javax.sql.rowset.WebRowSet;
import org.apache.log4j.Logger;
import org.jgroups.*;
import org.jgroups.protocols.*;
import org.jgroups.protocols.pbcast.GMS;
import org.jgroups.protocols.pbcast.STABLE;
import org.jgroups.stack.IpAddress;
import org.jgroups.stack.ProtocolStack;
import org.jgroups.ReceiverAdapter;
import org.jgroups.protocols.pbcast.FLUSH;
import org.jgroups.protocols.pbcast.NAKACK2;
import org.jgroups.util.SocketFactory;

/**
 * @author I0360D3
 *
 */
public class CommunicationChannel extends ReceiverAdapter implements ChannelListener, TransferClient  {

    private static final Logger logger = (Logger) Logger.getLogger(CommunicationChannel.class.getName());
    private CopyOnWriteArrayList<TransferBean> browserBuffer = new CopyOnWriteArrayList<TransferBean>();
    private CopyOnWriteArrayList<TransferBean> memoryBuffer = new CopyOnWriteArrayList<TransferBean>();
    
    private static final String PROTOCOL =
            "TCP(start_port=" + Engine.JAVA_GROUPS_ROUTER_SERVER_PORT + ";bind_addr=" + Engine.JAVA_GROUPS_ROUTER_SERVER + ";loopback=false):" +
            "TCPPING(initial_hosts=" + Engine.JAVA_GROUPS_ROUTER_SERVER + "[" + Engine.JAVA_GROUPS_ROUTER_SERVER_PORT + "]):" + 
            "MERGE2():" +
            "FD_SOCK():"+
            "FD():"+
            "VERIFY_SUSPECT:"+
            "pbcast.NAKACK(use_mcast_xmit=false;):" + 
            "UNICAST():" +
            "pbcast.STABLE():" +
            "pbcast.GMS()";
        
    private static CommunicationChannel instance = null;
    private List<Address> allViewMembers = null;

    private JChannel channel = null;
    private static Timer timer = null;


    static {
        try {
            timer = new Timer();
            instance = new CommunicationChannel();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private CommunicationChannel() {
        try {
            // this Latch acquire will prevent other Threads from 
            // doing their web-service requests until the latch.release
            // executes               
                    
            channel = new JChannel(false);                 // 1
            channel.setReceiver(this);
            ProtocolStack stack=new ProtocolStack(); // 2
            channel.setProtocolStack(stack);       // 3

            TCP tcp = new TCP();
            tcp.setBindToAllInterfaces(false);
            tcp.setBindPort(Engine.JAVA_GROUPS_ROUTER_SERVER_PORT);
            tcp.setLoopback(false);
            tcp.setEnableBundling(true);
            tcp.setDiscardIncompatiblePackets(true);
            tcp.setMaxBundleSize(64000);
            tcp.use_send_queues = true;
            tcp.sock_conn_timeout = 300;
            
            //tcp.setReaperInterval(300000);
            
            InetSocketAddress serveraddr = new InetSocketAddress(Engine.JAVA_GROUPS_ROUTER_SERVER,Engine.JAVA_GROUPS_ROUTER_SERVER_PORT);

            ArrayList<InetSocketAddress> serverList = new ArrayList<InetSocketAddress>();
            serverList.add(serveraddr);
            
            TCPGOSSIP gossip = new TCPGOSSIP();
            gossip.setInitialHosts(serverList);
            gossip.setNumInitialMembers(2);
            gossip.setTimeout(8000);
            
            NAKACK2 nakack2 = new NAKACK2();

            nakack2.setDiscardDeliveredMsgs(true);                        
            nakack2.setUseMcastXmit(false);  
                       
            GMS gms = new GMS();
            gms.setJoinTimeout(8000);
            gms.setViewAckCollectionTimeout(3000);
            gms.setMergeTimeout(8000);
            gms.setMaxJoinAttempts(2);
            gms.setViewBundling(true);
            //gms.setMaxBundlingTime(5000);
            
            FD_SOCK fdsock = new FD_SOCK();
            
//            FD fd = new FD();
//            fd.setTimeout(5000);
            //fd.setMaxTries(3);
            FD_ALL fd = new FD_ALL();
            fd.setTimeout(40000);
            fd.setInterval(8000);
            
            VERIFY_SUSPECT vsuspect = new VERIFY_SUSPECT();
            vsuspect.setValue("timeout", 4500);
            //vsuspect.use_icmp = true;
            
            MERGE2 merge2 = new MERGE2();
            merge2.setMaxInterval(30000);
            //merge2.setMinInterval(20000);
            merge2.setMinInterval(10000);

            UNICAST unicast = new UNICAST();

            stack.addProtocol(tcp).
                    addProtocol(gossip).
                    addProtocol(merge2).
                    addProtocol(fdsock).
                    addProtocol(fd).
                    addProtocol(vsuspect).
                    addProtocol(nakack2).
                    addProtocol(unicast).
                    addProtocol(new STABLE()).
                    addProtocol(gms);
//                    addProtocol(new UFC()).
//                    addProtocol(new MFC());
                    //addProtocol(new FLUSH());
            
            try {
                stack.init();                         // 5
            } catch (Exception ex) {
                //Logger.getLogger(AppletMessageListener.class.getName()).log(Level.SEVERE, null, ex);
                System.err.println(ex);
            }
            
            allViewMembers = new CopyOnWriteArrayList<Address>();
            
            channel.connect("ART-DATA");            
            channel.addChannelListener(this);
             
            // we will restart the connection every morning at 5:00 am
            // turn off the scheduler for now
            // ******************************************************************
//            java.util.Calendar morning = java.util.Calendar.getInstance();
//
//            morning.set(Calendar.HOUR_OF_DAY, 5);
//            morning.set(Calendar.MINUTE, 0);
//            morning.set(Calendar.SECOND, 0);
//            morning.add(Calendar.DAY_OF_MONTH, 1);
//
//            Thread.sleep(5000);

            // turn this off for now
            // timer.scheduleAtFixedRate( reconnectTask, morning.getTime(), 86400*1000 );
        } catch (InterruptedException e) {// ignore
        } catch (Exception ce) {
            logger.error("Channel Exception ", ce);
        }
    }

    synchronized public void stop() {
        if (channel != null) {
            //adapter.stop();
            channel.close();
        }
        channel = null;
    }

    synchronized public void restart() {
        try {
            channel = new JChannel(PROTOCOL);
            allViewMembers = new CopyOnWriteArrayList<Address>();

            channel.setReceiver(this);
            channel.connect("ART-DATA");
            channel.addChannelListener(this);
                         
        } catch (Exception ce) {
            logger.error("Channel Exception ", ce);
        }
    }

    public void channelConnected(Channel channel) {
        logger.warn("We've are connected: " + channel);
    }

    public void channelDisconnected(Channel channel) {
        logger.warn("We've been disconnected: " + channel);
    }

    public void channelClosed(Channel channel) {
        logger.warn("We've been closed!");
    }
        
    public void channelShunned() {
        logger.warn("We've been shunned!");
    }
        
    public void channelReconnected(Address addr) {
        logger.warn("Channel Reconnected on: " + addr);
    }

    @Override
    public void suspect(Address suspected_mbr) {
        logger.warn("Cluster suspect called for: " + suspected_mbr);
    }

    @Override
    public void viewAccepted(View new_view) {
        List<Address> new_mbrs = new_view.getMembers();

        if (new_mbrs != null) {
            sendViewChangedNotifications(new_mbrs);
        }
    }
        
    private void sendViewChangedNotifications(List<Address> new_mbrs) {

        List<Address> joined, left;

        logger.warn(" Backfill Beans Broadcast Detected.... ");
            
        if (allViewMembers == null || new_mbrs == null || new_mbrs.isEmpty()) {
            return;
        }
            
        joined = new ArrayList<Address>();
        for(Address nmbr:new_mbrs) {
            if (!allViewMembers.contains(nmbr)) {
                joined.add(nmbr);
            }
        }
            
        left = new ArrayList<Address>();
        for(Address lmbr:allViewMembers) {
            if (!new_mbrs.contains(lmbr)) {                
                left.add(lmbr);
            }
        }
        
        // now see if the one that left is the same as the one joining then, remove from the joined list        
        for(Iterator<Address> jiter = joined.iterator();jiter.hasNext();) {
            // if the member that joined is in the left list, then 
            // remove it from both lists
            Address jmbr = jiter.next();
            if (left.contains(jmbr)) { 
                logger.warn("Cluster member left and joined (ignoring it; and leaving in cluster)"+ jmbr);
                jiter.remove();
                left.remove(jmbr);
            }
        }
        
        if (left.size() > 0) { 
            logger.warn("Members Left Cluster ");
        }
        
        for(Address lmbr:left) {
            logger.warn("Cluster member left: " + lmbr);
            allViewMembers.remove(lmbr);
        }
                        
        if (joined.size() > 0) {
            logger.warn(" Backfill Beans Broadcast Detected.... new Members Identified. ");
        }

        for(Address joinedMbr:joined) {            
            logger.warn("Cluster member joined: " + joinedMbr);
            allViewMembers.add(joinedMbr);

            // send queued up data
            sendQueuedBeans((Address)joinedMbr);            
        }
    }

    private void sendQueuedStatsBeans(Address joinedMbr)
    {
        Collection<TransferBean> beans = StatisticsModule.getInstance().getAllBeans();

        try {
            // let's try to send all the beans in one shot
            BeanBag bag = new BeanBag();

            bag.setBeans(beans);
            TransferBean tbean = bag;

            broadcast(tbean, (Address) joinedMbr);

        } catch (Exception cnce) {
            logger.warn("ChannelNotConnected while broadcasting joined cluster");
        }
    }

    private void sendQueuedBeans(Address joinedMbr)
    {
        sendQueuedStatsBeans(joinedMbr);
    
        sendQueuedBrowserAndOSBeans(joinedMbr);

        sendQueuedMemoryBeans(joinedMbr);
    }

    private void sendQueuedMemoryBeans(Address joinedMbr)
    {
        try {
            BeanBag bag = new BeanBag();

            bag.setBeans(memoryBuffer);
            TransferBean tbean = bag;

            broadcast((TransferBean)tbean, (Address) joinedMbr);
        } catch (Exception cnce) {
            logger.warn("ChannelNotConnected while broadcasting joined cluster");        
        }
    }

    private void sendQueuedBrowserAndOSBeans(Address joinedMbr)
    {
        try {
            BeanBag bag = new BeanBag();

            bag.setBeans(browserBuffer);
            TransferBean tbean = bag;

            broadcast((TransferBean)tbean, (Address) joinedMbr);
        } catch (Exception cnce) {
            logger.warn("ChannelNotConnected while broadcasting joined cluster");
        }
    }
   
    /**
     * send out data to broadcast
     * @param tbean the databean being transfered
     * @param client the client to send the data to or null to send to all clients
     **/
    public void broadcast(TransferBean tbean, Address client) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            if ( channel != null && (!channel.isOpen() && !channel.isConnected()) ) {
                logger.error("Channel not yet open or connected, not broadcasting..");
                return;
            } else if ( channel == null) {
                logger.error("Channel not yet created, not broadcasting..");
                return;
            }
            // only continue if channel is not null & connected

            ObjectOutputStream oos = new ObjectOutputStream(baos);

            oos.writeObject(tbean);
        } catch (IOException e) {
            // this should not happen since we are writing to memory
            logger.error("IOException", e);
        }
        Message message = new Message(client, channel.getAddress(), baos.toByteArray());
        try {
            channel.send(message);
        }
        catch(Exception cnce)
        {
            logger.error("Failed to send Message; we are not connected!");
        }
    }


    // this operation is overloaded,
    // so that we can save a queue of the last 30 of them, and
    // send them out on a client-connect
    public void broadcast(BrowserBean bbean,Address client) throws Exception
    {
        // add to the queue
        if ( browserBuffer.size() == 50) {
            browserBuffer.remove(0);
        }
        browserBuffer.add(bbean);

        broadcast((TransferBean)bbean,client);
    }

     // this operation is overloaded,
    // so that we can save a queue of the last 30 of them, and
    // send them out on a client-connect
    public void broadcast(MemoryStatBean mbean,Address client) throws Exception
    {
        // add to the queue
        if ( memoryBuffer.size() == 3600) {
            memoryBuffer.remove(0);
        }
        memoryBuffer.add(mbean);

        broadcast((TransferBean)mbean,client);
    }

    /**
     * inner class to run db jobs asynchronously
     **/
    class DBRunner extends Thread {
        org.jgroups.Message msg;
        QueryBean bean;

        public DBRunner(org.jgroups.Message msg,QueryBean bean)
        {
            this.msg = msg;
            this.bean = bean;
        }

        public void run()
        {
            Connection conn = null;
            Statement ps = null;
            WebRowSet wrs;
            try {
                wrs = new WebRowSetImpl();
                logger.warn("Executing query " + bean.getQuery());
                conn = ConnectionPoolT.getConnection();
                ps = conn.createStatement();
                ResultSet resultSet = ps.executeQuery(bean.getQuery());
    
                wrs.populate(resultSet);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                Writer writer = new OutputStreamWriter(baos);
    
                wrs.writeXml(writer);
                bean.setResponse(baos.toByteArray());
            } catch (SQLException sqe) {
                logger.error("Bad Query: " + bean.getQuery(), sqe);
            }
    
            try {
                org.jgroups.Message reply = msg.makeReply();
    
                ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(baos2);
    
                // write the bean back
                oos.writeObject(bean);
    
                reply.setBuffer(baos2.toByteArray());
    
                channel.send(reply);
            } catch (IOException io) {
                logger.error(getClass().getName() + " IOError writing result: ", io);
            } catch (Exception cnc) {
                logger.error(getClass().getName() + " ChannelClosed: ", cnc);
            }
            finally {
                try {
                    if (ps != null) {
                        ps.close();
                    }
                    if (conn != null) {
                        conn.close();
                    }
                } catch (Exception e) {// ignore
                }
            } 
        }
    }

    /**
     * called by the channel listener when messages come in
     **/
    @Override
    public void receive(org.jgroups.Message msg) {
        // we don't send null's
        // this app does process UserBeans and ChatBeans, they get re-broadcasted 
        // to all clients
        if (msg == null) {
            // invalid msg, just discard
            return;
        }
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(msg.getBuffer());
            ObjectInputStream ois = new ObjectInputStream(bais);
            TransferBean data = (TransferBean) ois.readObject();

            data.registerClient(this);
            data.processBean(msg);
            
        } catch (ClassNotFoundException cnfe) {
            System.out.println("ClassNotFound in receive " + cnfe);
        } catch (java.io.IOException io) {
            System.out.println("IOException in receive " + io);
        }
    }

    public void process(org.jgroups.Message msg,ChatBean bean)
    {
        try {
            broadcast(bean, null);
        } catch (Exception cnce) {
            logger.warn("ChannelNotConnected while broadcasting joined cluster");
        //} catch (ChannelClosedException cce) {
          //  logger.warn("ChannelClosedException while broadcasting joined cluster");
        }
    }

    public void process(org.jgroups.Message msg,BeanBag bag)
    {
       ; // do nothing
    }

    public void process(org.jgroups.Message msg,QueryBean bean)
    {
        DBRunner dbrun = new DBRunner(msg,bean);
        dbrun.start();
    }



    /**
     * process a BeanBag, a container for a collection of accessminutebeans
     **/
    public void process(org.jgroups.Message msg, BrowserBean bean) {
        ; // do nothing
    }
    public void process(org.jgroups.Message msg, HistoryBean bean) {

        String chart =  bean.getChartName();
    
        java.util.Date time = bean.getDate();
    
        String direction =  bean.getDirection();
    
        int points =  bean.getDataPoints();
    
        String precision =  bean.getDataPrecision();

        processHistoryRequest(chart,time,direction,points,precision,msg.makeReply());
    }

    /**
     * process a UserBean from the receiver
     **/
    public void process(org.jgroups.Message msg,UserBean bean)
    {
        try {
            //System.out.println("Process  userbean");
            broadcast(bean, null);
        } catch (Exception cnce) {
            logger.warn("ChannelNotConnected while broadcasting userbean");
        //} catch (ChannelClosedException cce) {
         //   logger.warn("ChannelClosedException while broadcasting userbean");
        }
    }

    /* (non-Javadoc)
     * @see com.bos.art.logParser.broadcast.beans.TransferClient#process(com.bos.art.logParser.broadcast.beans.MemoryStatBean)
     */
    public void process(org.jgroups.Message msg, MemoryStatBean bean) {// TODO Auto-generated method stub
    }
    
    public void process(org.jgroups.Message msg, ErrorStatBean bean) {// TODO Auto-generated method stub
    }


    public void process(org.jgroups.Message msg, AccessRecordsMinuteBean obj) {
        ; // do nothing
    }

    public void process(org.jgroups.Message msg, ExternalAccessRecordsMinuteBean obj) {
        ; // do nothing
    }

    public void process(org.jgroups.Message msg, SessionDataBean obj) {
        ; // do nothing
    }

    public static CommunicationChannel getInstance() {
        return instance;
    }

    /**
     * @return
     */
    public Channel getChannel() {
        return channel;
    }

    public void processHistoryRequest(String chart,
            java.util.Date time,
            String direction,
            int points,
            String precision,
            org.jgroups.Message msg){
        logger.warn("processHistoryRequest : " 
                + chart + " time " 
                + time.getTime() + "direction "
                + direction +"points " 
                + points +"precision " + precision);
        QueryBroadcast qb = QueryBroadcastFactory.getQueryBroadcast(
            chart,
            time,
            direction,
            points,
            precision,
            msg
            );
        qb.processRequest();

    }

}


