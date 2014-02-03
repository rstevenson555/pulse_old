package com.bos.art.logServer.Queues;

import com.bos.art.logServer.main.Collector;
import com.bos.art.logServer.utils.TPSCalculator;
import com.bos.art.logServer.utils.TimeIntervalConstants;
import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.Util;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.log4j.Logger;

import javax.management.*;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MessageUnloader implements MessageUnloaderMBean {
    private static final int ENGINE_OUTPUT_BUFFER_SIZE = 1024 * 8;

    private static Logger logger = Logger.getLogger(MessageUnloader.class.getName());
    private ObjectOutputStream outputStream = null;
    private Connector connector;
    private InetAddress address = null;
    private int port = 0;
    final private long reconnectionDelay = TimeIntervalConstants.THIRTY_SECONDS_MILLIS;
    private boolean exitOnFinish = false;
    private long writeTime = System.currentTimeMillis();
    private Socket socket;
    private int failCount = 0;
    private int MESSAGE_QUEUE_SIZE = 2*1024;
    private static int SOCKET_BUFFER = 262144;
    private BasicThreadFactory tFactory = new BasicThreadFactory.Builder()
            .namingPattern("MessageUnloader-%d")
            .build();
    private static TPSCalculator tpsCalculator = new TPSCalculator();
    private final ExecutorService executor = Executors.newSingleThreadExecutor(tFactory);

    private Disruptor<ObjectEvent> disruptor = new Disruptor<ObjectEvent>(ObjectEvent.FACTORY, MESSAGE_QUEUE_SIZE, executor,
            ProducerType.SINGLE, new SleepingWaitStrategy());

    private static class ObjectEvent {
        private Object record;

        public static final EventFactory<ObjectEvent> FACTORY = new EventFactory<ObjectEvent>() {
            public ObjectEvent newInstance() {
                return new ObjectEvent();
            }
        };
    }

    /**
     * return number of elements in the ringbuffer
     * @return
     */
    public long size() {
        return (disruptor.getRingBuffer().getBufferSize() - disruptor.getRingBuffer().remainingCapacity());
    }

    private class ObjectEventHandler implements EventHandler<ObjectEvent> {

        public ObjectEventHandler() {
        }

        public void onEvent(ObjectEvent pevent, long sequence, boolean endOfBatch) throws Exception {
            Object event = pevent.record;

            try {
                event = pevent.record;

                if (outputStream != null) {
                    writeData(outputStream, event);

                    long writeCount = tpsCalculator.incrementTransaction();

                    if (writeCount % 1000 == 0) {
                        outputStream.reset();
                    }

                    if (writeCount % 10000 == 0) {

                        logger.info("sent 10000 objects to the ArtEngine in " + (System.currentTimeMillis() - writeTime) / 10
                                + " millis; leaving [" + size() + "] in the queue");

                        writeTime = System.currentTimeMillis();
                    }
                } else {
                    logger.error("outputstream is null");
                }

                event = null;
            } catch (IOException io) {
                try {
                    outputStream.reset();
                } catch (IOException i) {
                }
                logger.error("IOException in MessageUnloader", io);
                fireConnector();
            } catch (Throwable t) {
                try {
                    outputStream.reset();
                } catch (IOException io) {
                }
                logger.error("Throwable in MessageUnloader", t);
                fireConnector();
            }

            if (exitOnFinish) {
                try {
                    drainQueue(outputStream);
                    try {
                        outputStream.reset();
                    } catch (IOException io) {
                    }
                    outputStream.flush();
                    outputStream.close();
                } catch (IOException io) {
                    try {
                        outputStream.reset();
                    } catch (IOException i) {
                    }
                    logger.error("IO Error draining queue " + io);
                }
                logger.info("exiting");
                System.exit(0);
            }

        }
    }

    /**
     * create the disruptor and connect to the end point
     */
    public MessageUnloader() {

        disruptor.handleExceptionsWith(new FatalExceptionHandler());

        ObjectEventHandler handler = new ObjectEventHandler();
        disruptor.handleEventsWith(handler);
        disruptor.start();

        // connect to the output

        this.address = getAddressByName(Collector.ART_ENGINE_MACHINE);
        this.port = Collector.ART_ENGINE_PORT;

        connect(address, port);
    }

    private void registerWithMBeanServer() {
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        ObjectName name = null;
        try {
            name = new ObjectName("com.omx.collector:type=MessageUnloaderMBean,name=MessageUnloaderInstance-"+socket.getLocalPort());
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

//    static public MessageUnloader getInstance() {
//        return instance;
//    }

    @Override
    protected void finalize() throws Throwable {
        try {
            cleanUp();
        } finally {
            super.finalize();
        }
    }

    /**
     * The Connector will reconnect when the server becomes available again. It does this by attempting to open a new
     * connection every
     * <code>reconnectionDelay</code> milliseconds.
     * <p/>
     * <p>It stops trying whenever a connection is established. It will restart to try reconnect to the server when
     * previpously open connection is droppped.
     *
     * @author Ceki G&uuml;lc&uuml;
     * @since 0.8.4
     */
    class Connector extends Thread {

        boolean interrupted = false;
        private static final int SOCKET_BUFFER = 262144;

        @Override
        public void run() {

            while (!interrupted) {
                try {
                    sleep(reconnectionDelay);
                    logger.info("Attempting connection to ArtEngine at: " + address.getHostName());
                    socket = new Socket(address, port);
                    logger.warn("Socket Buffer Size: " + socket.getSendBufferSize());
                    socket.setSendBufferSize(SOCKET_BUFFER);

                    logger.warn("Socket Buffer Size after change: " + socket.getSendBufferSize());

                    synchronized (this) {

                        outputStream = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream(), 64 * 1024));
                        logger.info("Success connecting to ArtEngine at:" + address + " port: " + port);
                        connector = null;
                        break;
                    }

                } catch (InterruptedException e) {
                    //logger.debug( "Connector interrupted. Leaving loop." );
                    return;

                } catch (java.net.ConnectException e) {
                    //logger.debug( "Remote host " + address.getHostName()
                    //             + " refused connection." );
                    logger.error("connect exception: " + e);

                } catch (IOException e) {
                    //logger.debug( "Could not connect to " + address.getHostName() +
                    //              ". Exception is " + e );
                    logger.error("IOException " + e);
                }
            }

        }
    }


    void fireConnector() {
        if (connector == null) {
            logger.info("Starting a new connector thread.");
            connector = new Connector();
            connector.setDaemon(true);
            //connector.setPriority( Thread.MIN_PRIORITY );
            connector.start();
        }
    }

    static InetAddress getAddressByName(String host) {
        try {
            return InetAddress.getByName(host);

        } catch (Exception e) {
            logger.error("Could not find address of [" + host + "].");
            return null;
        }
    }

    /**
     * Drop the connection to the remote host and release the underlying connector thread if it has been created
     */
    public void cleanUp() {
        if (outputStream != null) {
            try {
                outputStream.close();

            } catch (IOException e) {
                logger.error("Could not close outputStream.");
            }

            outputStream = null;
        }

        if (connector != null) {
            //logger.debug("Interrupting the connector.");
            connector.interrupted = true;
            connector = null;  // allow gc
        }
    }

    void connect(InetAddress address, int port) {
        if (this.address == null) {
            return;
        }

        // First, close the previous connection if any.
        cleanUp();

        try {
            logger.info("trying to connect to ArtEngine at: " + address + " port: " + port);
            socket = new Socket(address, port);

            logger.warn("Socket Buffer Size: " + socket.getSendBufferSize());
            socket.setSendBufferSize(262144);
            logger.warn("Socket Buffer Size after change: " + socket.getSendBufferSize());

            outputStream = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream(), 128 * 1024));
            logger.info("Success connecting to ArtEngine at:" + address + " port: " + port);

            registerWithMBeanServer();

        } catch (IOException io) {
            logger.error("Error connecting to : " + address + " " + io);
            fireConnector(); // fire the connector thread
        }
    }

    // messages going to the ArtEngine

    public void addMessage(final Object o) {
        boolean success = disruptor.getRingBuffer().tryPublishEvent(new EventTranslator<ObjectEvent>() {
            public void translateTo(ObjectEvent event, long sequence) {
                event.record = o;
            }

        });
        if (!success) {
            failCount++;
            if (++failCount % 100 == 0) {
                logger.error("Failed to offer in queue to ArtEngine");
            }
        } else {
            failCount = 0;
        }
    }

    // JMX Interface exposed
    public long getBufferSize() {
        return disruptor.getBufferSize();
    }

    /**
     * get the remaining capacity of the ringbuffer
     * @return
     */
    public long getRemainingCapacity() {
        return disruptor.getRingBuffer().remainingCapacity();
    }

    /**
     * change the buffer size to nearest power of two
     * @param sz
     */
    public void setBufferSize(long sz) {
        disruptor.shutdown();

        int psize = Util.ceilingNextPowerOfTwo((int) sz);

        disruptor = new Disruptor<ObjectEvent>(ObjectEvent.FACTORY, psize, executor,
                ProducerType.SINGLE, new SleepingWaitStrategy());

        disruptor.handleExceptionsWith(new FatalExceptionHandler());

        ObjectEventHandler handler = new ObjectEventHandler();
        disruptor.handleEventsWith(handler);
        disruptor.start();
    }

    /**
     * return messages per second calculation
     * @return
     */
    public long getMessagesPerSecond() {
        return tpsCalculator.getMessagesPerSecond();
    }

    public long getWriteCount() {
        return tpsCalculator.getTransactionCount();
    }

    public void exitOnFinish() {
        exitOnFinish = true;
    }


    private void writeData(ObjectOutputStream outputStream, Object event) throws IOException {
        outputStream.writeObject(event);
    }

    /**
     * drain the queue out
     */
    private void drainQueue(ObjectOutputStream outputStream) throws IOException {

    }
}

