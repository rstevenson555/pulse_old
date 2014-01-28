package com.bos.art.logServer.Queues;

import com.bos.art.logServer.Queues.MBeanInterface.DisruptorMBean;
import com.bos.art.logServer.main.Collector;
import com.bos.art.logServer.utils.TimeIntervalConstants;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.log4j.Logger;

public class MessageUnloader  extends java.lang.Thread implements DisruptorMBean {
    private static final int ENGINE_OUTPUT_BUFFER_SIZE = 1024 * 8;

    private BlockingQueue queue = null;
    private static Logger logger = Logger.getLogger(MessageUnloader.class.getName());
    private ObjectOutputStream outputStream = null;
    private Connector connector;
    private InetAddress address = null;
    private int port = 0;
    final private long reconnectionDelay = TimeIntervalConstants.THIRTY_SECONDS_MILLIS;
    private long writeCount = 0;
    private boolean exitOnFinish = false;
    private long writeTime = System.currentTimeMillis();
    private static MessageUnloader instance = new MessageUnloader();
    private int failCount = 0;
    //private int MESSAGE_QUEUE_SIZE = 300000;
    private int MESSAGE_QUEUE_SIZE = 5000;
    private static int SOCKET_BUFFER = 262144;
    private BasicThreadFactory tFactory = new BasicThreadFactory.Builder()
                .namingPattern("MessageUnloader-%d")
                .build();

    private final ExecutorService executor = Executors.newSingleThreadExecutor(tFactory);

    private Disruptor<ObjectEvent> disruptor = new Disruptor<ObjectEvent>(ObjectEvent.FACTORY, 4 * 1024, executor,
            ProducerType.SINGLE, new SleepingWaitStrategy());

    private static class ObjectEvent {
        private Object record;

        public static final EventFactory<ObjectEvent> FACTORY = new EventFactory<ObjectEvent>() {
            public ObjectEvent newInstance() {
                return new ObjectEvent();
            }
        };
    };

    private class ObjectEventHandler implements EventHandler<ObjectEvent> {
        public int failureCount = 0;
        public int messagesSeen = 0;

        public ObjectEventHandler() {
        }

        public void onEvent(ObjectEvent pevent, long sequence, boolean endOfBatch) throws Exception {
            Object event = pevent.record;

            try {
                event = pevent.record;

                if (outputStream != null) {
                    //System.out.println(event);
                    //outputStream.writeObject(event);
                    writeData(outputStream, event);
                    // System.out.println("writing object"+writeCount);
                    if (++writeCount % 1000 == 0) {
                        outputStream.reset();
                    }
                    if (writeCount % 10000 == 0) {
                        logger.info("sent 10000 objects to the ArtEngine in " + (System.currentTimeMillis() - writeTime) / 10
                                + " millis; leaving [" + queue.size() + "] in the queue");

                        writeTime = System.currentTimeMillis();
                    }
                    //System.out.println("writing ojbect to server");
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
//            } catch (InterruptedException ie) {
//                try {
//                    outputStream.reset();
//                } catch (IOException io) {
//                }
//                logger.error("InterruptedException in MessageUnloader", ie);
//                fireConnector();
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

    static public MessageUnloader getInstance() {
        return instance;
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            cleanUp();
            //queue.clear();
            queue = null;
        } finally {
            super.finalize();
        }
    }

    public int size() {
        return queue.size();
    }

    private MessageUnloader() {
//        queue = new ArrayBlockingQueue(MESSAGE_QUEUE_SIZE); //across all jvm's because this is static connection
        queue = new ArrayBlockingQueue(1); //across all jvm's because this is static connection

        disruptor.handleExceptionsWith(new FatalExceptionHandler());

        ObjectEventHandler handler = new ObjectEventHandler();
        disruptor.handleEventsWith(handler);
        disruptor.start();

        // connect to the output

        this.address = getAddressByName(Collector.ART_ENGINE_MACHINE);
        this.port = Collector.ART_ENGINE_PORT;

        connect(address, port);
        start();
    }

    @Override
    public void start() {
        // go
        super.start();
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
            Socket socket;

            while (!interrupted) {
                try {
                    sleep(reconnectionDelay);
                    logger.info("Attempting connection to ArtEngine at: " + address.getHostName());
                    socket = new Socket(address, port);
                    logger.warn("Socket Buffer Size: " + socket.getSendBufferSize());
                    socket.setSendBufferSize(SOCKET_BUFFER);

                    logger.warn("Socket Buffer Size after change: " + socket.getSendBufferSize());

                    synchronized (this) {

                        outputStream = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream(),64*1024));
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
            Socket socket = new Socket(address, port);

            logger.warn("Socket Buffer Size: " + socket.getSendBufferSize());
            socket.setSendBufferSize(262144);
            logger.warn("Socket Buffer Size after change: " + socket.getSendBufferSize());

            outputStream = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream(),128*1024));
            logger.info("Success connecting to ArtEngine at:" + address + " port: " + port);
        } catch (IOException io) {
            logger.error("Error connecting to : " + address + " " + io);
            fireConnector(); // fire the connector thread
        }
    }

    // messages going to the ArtEngine

    public void addMessage(final Object o) {
        //if (!queue.offer(o)) {
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

    public long getBufferSize() {
        return disruptor.getBufferSize();
    }

    public long getCursor() {
        return disruptor.getCursor();
    }

    public void exitOnFinish() {
        exitOnFinish = true;

        
    }

    @Override
    public void run() {
        Object event;

        while (!exitOnFinish) {
            // now write it to their respective files and empty the stack

            // this loop could take a long time and it wouldnt slow the app down
            // because the userEventStack is released, the the userEventStack
            // can continue to be filled up

            try {
                event = removeLast();
                if (event == null) {
                    //yield();
                    continue;
                }
                if (outputStream != null) {
                    //System.out.println(event);
                    //outputStream.writeObject(event);
                    writeData(outputStream, event);
                    // System.out.println("writing object"+writeCount);
                    if (++writeCount % 1000 == 0) {
                        outputStream.reset();
                    }
                    if (writeCount % 10000 == 0) {
                        logger.info("sent 10000 objects to the ArtEngine in " + (System.currentTimeMillis() - writeTime) / 10
                                + " millis; leaving [" + queue.size() + "] in the queue");

                        writeTime = System.currentTimeMillis();
                    }
                    //System.out.println("writing ojbect to server");
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
            } catch (InterruptedException ie) {
                try {
                    outputStream.reset();
                } catch (IOException io) {
                }
                logger.error("InterruptedException in MessageUnloader", ie);
                fireConnector();
            } catch (Throwable t) {
                try {
                    outputStream.reset();
                } catch (IOException io) {
                }
                logger.error("Throwable in MessageUnloader", t);
                fireConnector();
            }

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

    private Object removeLast() throws InterruptedException {
        //return queue.poll(5L, TimeUnit.SECONDS);
        return queue.take();
    }

    private void writeData(ObjectOutputStream outputStream, Object event) throws IOException {
        outputStream.writeObject(event);
    }

    /**
     * drain the queue out
     */
    private void drainQueue(ObjectOutputStream outputStream) throws IOException {
        Object event;

        while (queue.size() > 0) {
            try {
                event = removeLast();
                if (event == null) {
                    //yield();
                    continue;
                }
                if (outputStream != null) {
                    writeData(outputStream, event);
                    if (++writeCount % 1000 == 0) {
                        outputStream.reset();
                    }
                    if (writeCount % 10000 == 0) {
                        logger.info("sent 10000 objects to the ArtEngine in " + (System.currentTimeMillis() - writeTime) / 10
                                + " millis; leaving [" + queue.size() + "] in the queue");


                        writeTime = System.currentTimeMillis();
                    }
                    //System.out.println("writing ojbect to server");
                } else {
                    logger.error("outputstream is null");
                }

                event = null;
            } catch (InterruptedException ie) {
                // thread interrupted
            } finally {
                try {
                    outputStream.reset();
                } catch (IOException io) {
                }
            }
        }
    }
}

