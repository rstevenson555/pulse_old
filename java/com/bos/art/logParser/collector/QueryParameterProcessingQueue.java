/*
 * Created on Oct 21, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package com.bos.art.logParser.collector;


import java.io.Serializable;

import com.bos.art.logParser.records.ILiveLogParserRecord;
import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.log4j.Logger;
import com.bos.art.logParser.records.QueryParameters;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * @author I0360D3
 *         <p/>
 *         To change the template for this generated type comment go to
 *         Window>Preferences>Java>Code Generation>Code and Comments
 */
public class QueryParameterProcessingQueue extends Thread implements Serializable {
    private static final Logger logger = (Logger) Logger.getLogger(QueryParameterProcessingQueue.class.getName());
    private static QueryParameterProcessingQueue instance;
    private BlockingQueue<QueryParameters> dequeue; // UnboundedFifoBuffer dequeue;
    private int objectsRemoved;
    private int objectsProcessed;
    private volatile long totalSysTime;
    protected static boolean unloadDB = true;
    private static final int MAX_DB_QUEUE_SIZE = 3000;
    //private static final int MAX_DB_QUEUE_SIZE = 50000;
    private static long fullCount = 0;
    private BasicThreadFactory tFactory = new BasicThreadFactory.Builder()
                .namingPattern("QueryParameterProcessingQueue-%d")
                .build();
    private final ExecutorService executor = Executors.newSingleThreadExecutor(tFactory);

    private Disruptor<QueryParametersEvent> disruptor = new Disruptor<QueryParametersEvent>(QueryParametersEvent.FACTORY, 2 * 1024, executor,
            ProducerType.SINGLE, new SleepingWaitStrategy());


    private static class QueryParametersEvent {
        private QueryParameters record;

        public static final EventFactory<QueryParametersEvent> FACTORY = new EventFactory<QueryParameterProcessingQueue.QueryParametersEvent>() {
            public QueryParametersEvent newInstance() {
                return new QueryParametersEvent();
            }
        };
    }

    ;

    private class QueryParametersEventHandler implements EventHandler<QueryParametersEvent> {
        public int failureCount = 0;
        public int messagesSeen = 0;

        public QueryParametersEventHandler() {
        }

        public void onEvent(QueryParametersEvent event, long sequence, boolean endOfBatch) throws Exception {
            QueryParameters qp = event.record;

            ++objectsRemoved;
//                if (qp == null) {
//                    logger.error("removeFirst Returned Null!");
//                    continue;
//                }

            long sTime = System.currentTimeMillis();

            String str = qp.processQueryParameters();
            qp.writeQueryParameter(str);

            long sTime2 = System.currentTimeMillis();

            ++objectsProcessed;
            totalSysTime += (sTime2 - sTime);
        }
    }

    private QueryParameterProcessingQueue() {
//        dequeue = new ArrayBlockingQueue<QueryParameters>(MAX_DB_QUEUE_SIZE);
        dequeue = new ArrayBlockingQueue<QueryParameters>(1);

        disruptor.handleExceptionsWith(new FatalExceptionHandler());

        QueryParametersEventHandler handler = new QueryParametersEventHandler();
        disruptor.handleEventsWith(handler);
        disruptor.start();
    }

    public static QueryParameterProcessingQueue getInstance() {
        if (instance == null) {
            instance = new QueryParameterProcessingQueue();
        }
        return instance;
    }

    public void addLast(Object o) {
        addLast((QueryParameters) o);
    }

    public void addLast(final QueryParameters o) {
        //boolean success = dequeue.offer(o);
        boolean success = disruptor.getRingBuffer().tryPublishEvent(new EventTranslator<QueryParametersEvent>() {
            public void translateTo(QueryParametersEvent event, long sequence) {
                event.record = o;
            }

        });
        if (!success && (fullCount++ % 100) == 0) {
            logger.error("Failed adding to the QueryParameterProcessingQueue Queue: ");
        }
    }

    public Object removeFirst() {
        return removeFirst0();
    }

    public QueryParameters removeFirst0() {
        try {
            return dequeue.take();
        } catch (InterruptedException e) {
            logger.error("Interrupted Exception taking from the Database Write Queue: ", e);
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
        sb.append("\n\tObjects Processed           :  ").append(objectsProcessed);
        if (objectsProcessed > 1000) {
            sb.append("\n\tProc Time millis per 1000  :  ").append(totalSysTime / (objectsProcessed / 1000));
        } else {
            sb.append("\n\tProc Time millis per 1000  :  0");
        }
        return sb.toString();
    }

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */

    @Override
    public void run() {
        while (unloadDB && !Thread.currentThread().isInterrupted()) {
            if (logger.isInfoEnabled()) {
                if (objectsRemoved % 10000 == 0) {
                    logger.info(toString());
                }
            }
            try {
                QueryParameters qp = removeFirst0();

                ++objectsRemoved;
                if (qp == null) {
                    logger.error("removeFirst Returned Null!");
                    continue;
                }
                long sTime = System.currentTimeMillis();

                String str = qp.processQueryParameters();
                qp.writeQueryParameter(str);

                long sTime2 = System.currentTimeMillis();

                ++objectsProcessed;
                totalSysTime += (sTime2 - sTime);
            } catch (Throwable t) {
                logger.error("Throwable in QueryParameterProcessingQueue Thread! " + Thread.currentThread().getName() + ":", t);
            }
        }
    }
}

