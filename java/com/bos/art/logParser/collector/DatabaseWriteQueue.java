/*
 * Created on Oct 21, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package com.bos.art.logParser.collector;


import com.bos.art.logParser.records.ILiveLogParserRecord;
import java.io.Serializable;
import java.util.concurrent.*;

import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;
import org.apache.log4j.Logger;

/**
 * @author I0360D3
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class DatabaseWriteQueue extends Thread implements Serializable {
    private static final Logger logger = (Logger) Logger.getLogger(DatabaseWriteQueue.class.getName());
    private static DatabaseWriteQueue instance = new DatabaseWriteQueue();
    private BlockingQueue<ILiveLogParserRecord> dequeue;
    private int objectsRemoved;
    private int objectsWritten;                 
    private long totalWriteTime;
    protected static boolean unloadDB = true;
    private static final int MAX_DB_QUEUE_SIZE = 4500;
    private static long fullCount = 0;
    private static long writeCount = 0;
    private final ExecutorService executor = Executors.newSingleThreadExecutor(DaemonThreadFactory.INSTANCE);

    private Disruptor<ILiveLogParserRecordEvent> disruptor = new Disruptor<ILiveLogParserRecordEvent>(ILiveLogParserRecordEvent.FACTORY, 5000, executor,
                ProducerType.SINGLE, new BusySpinWaitStrategy());

    // guards for boundaries

    private static class ILiveLogParserRecordEvent
    {
        private ILiveLogParserRecord record;

        public static final EventFactory<ILiveLogParserRecordEvent> FACTORY = new EventFactory<DatabaseWriteQueue.ILiveLogParserRecordEvent>()
        {
            public ILiveLogParserRecordEvent newInstance()
            {
                return new ILiveLogParserRecordEvent();
            }
        };
    };

    private class LiveLogParserRecordEventHandler implements EventHandler<ILiveLogParserRecordEvent>
    {
        public int failureCount = 0;
        public int messagesSeen = 0;

        public LiveLogParserRecordEventHandler()
        {
        }

        public void onEvent(ILiveLogParserRecordEvent event, long sequence, boolean endOfBatch) throws Exception
        {
//            if (event.sequence != sequence ||
//                    event.a != sequence + 13 ||
//                    event.b != sequence - 7 ||
//                    !("wibble-" + sequence).equals(event.s))
//            {
//                failureCount++;
//            }

            ILiveLogParserRecord ilpr = event.record;
            ++objectsRemoved;

            long writeStartTime = System.currentTimeMillis();

            if (writeCount++ % 1000 == 0) {
                //logger.warn("DatabaseWriteQueue writeToDatabase called");
            }
            logger.warn("Persisting...");
            ilpr.writeToDatabase();
            totalWriteTime += (System.currentTimeMillis() - writeStartTime);
            ++objectsWritten;
            messagesSeen++;
        }
    }


    private DatabaseWriteQueue() {
        dequeue = new ArrayBlockingQueue<ILiveLogParserRecord>(MAX_DB_QUEUE_SIZE);

        RingBuffer<ILiveLogParserRecordEvent> ringBuffer = disruptor.getRingBuffer();
        disruptor.handleExceptionsWith(new FatalExceptionHandler());
        LiveLogParserRecordEventHandler handler = new LiveLogParserRecordEventHandler();
        disruptor.handleEventsWith(handler);
        disruptor.start();
    }

    public static DatabaseWriteQueue getInstance() {
        return instance;
    }


    public void addLast(Object o) {
        addLast((ILiveLogParserRecord)o);
    }

    public void addLast(final ILiveLogParserRecord o) {
        //boolean success = dequeue.offer(o);
        disruptor.publishEvent(new EventTranslator<ILiveLogParserRecordEvent>() {
            public void translateTo(ILiveLogParserRecordEvent event, long sequence)
            {
                event.record = o;
            }

        });
//        if (!success && (fullCount++ % 100) == 0) {
//            logger.error("DatabaseWriteQueue is full, throwing out messages");
//        }
    }


    public Object removeFirst() {
        return removeFirst0();
    }

    public ILiveLogParserRecord removeFirst0() {
        try {
            return dequeue.take();
        } catch (InterruptedException e) {
            logger.error("Interrupted Exception taking from the Database Write Queue: ", e);
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("Database Write Queue size:");
        sb.append(((BlockingQueue) dequeue).size());
        sb.append("\t\t this thread: ");
        sb.append(Thread.currentThread().getName());
        sb.append("\n\tObjects Popped              :  ").append(objectsRemoved);
        sb.append("\n\tObjects Written             :  ").append(objectsWritten);
        if (objectsWritten > 1000) {
            sb.append("\n\tWrite Time millis per 1000  :  ").append(totalWriteTime / (objectsWritten / 1000));
        } else {
            sb.append("\n\tWrite Time millis per 1000  :  0");
        }
        return sb.toString();
    }

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */

    @Override
    public void run() {
        try {
            while (unloadDB && !Thread.currentThread().isInterrupted()) {
                if (logger.isInfoEnabled()) {
                    if (objectsRemoved % 100000 == 0) {
                        logger.info(toString());
                    }
                }
                try {
                    ILiveLogParserRecord ilpr = removeFirst0();

                    ++objectsRemoved;
                    if (ilpr == null) {
                        logger.error("removeFirst Returned Null!");
                        continue;
                    }
                    long writeStartTime = System.currentTimeMillis();

                    if (writeCount++ % 1000 == 0) {
                        //logger.warn("DatabaseWriteQueue writeToDatabase called");
                    }
                    ilpr.writeToDatabase();
                    totalWriteTime += (System.currentTimeMillis() - writeStartTime);
                    ++objectsWritten;
                } catch (Throwable t) {
                    logger.error("Throwable in DatabaseWriteQueue Thread! " + Thread.currentThread().getName() + ":", t);
                }
            }
        } catch (Throwable t) {
            logger.error("DatabaseWriteQueue errored, should never happen!!!", t);
        }
    }
}

