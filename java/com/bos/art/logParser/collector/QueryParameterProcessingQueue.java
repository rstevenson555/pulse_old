/*
 * Created on Oct 21, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package com.bos.art.logParser.collector;


import java.io.Serializable;
import org.apache.log4j.Logger;
import com.bos.art.logParser.records.QueryParameters;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


/**
 * @author I0360D3
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class QueryParameterProcessingQueue extends Thread implements Serializable {
    private static final Logger logger = (Logger) Logger.getLogger(QueryParameterProcessingQueue.class.getName());
    private static QueryParameterProcessingQueue instance;
    private BlockingQueue dequeue; // UnboundedFifoBuffer dequeue;
    private int objectsRemoved;
    private int objectsProcessed;
    private volatile long totalSysTime;
    protected static boolean unloadDB = true;
    private static final int MAX_DB_QUEUE_SIZE = 10000;
    //private static final int MAX_DB_QUEUE_SIZE = 50000;
    private static long fullCount = 0;
    
    private QueryParameterProcessingQueue() {
        dequeue = new ArrayBlockingQueue(MAX_DB_QUEUE_SIZE);
    }

    public static QueryParameterProcessingQueue getInstance() {
        if (instance == null) {
            instance = new QueryParameterProcessingQueue();
        }
        return instance;
    }

    public void addLast(Object o) {
            
            boolean success = dequeue.offer(o);
            if (!success && (fullCount++ % 100) == 0) {
                logger.error("Failed adding to the QueryParameterProcessingQueue Queue: ");
            }
            
    }

    public Object removeFirst() {
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
                Object o = this.removeFirst();

                ++objectsRemoved;
                if (o == null) {
                    logger.error("removeFirst Returned Null!");
                    continue;
                }
                if (o instanceof QueryParameters) {
                    long sTime = System.currentTimeMillis();
                    QueryParameters qp = (QueryParameters) o;

                    qp.processQueryParameters();
                    long sTime2 = System.currentTimeMillis();

                    ++objectsProcessed;
                    totalSysTime += (sTime2 - sTime);
                } else {
                    logger.error("removeFirst gave " + o.getClass().getName());
                }
            } catch (Throwable t) {
                logger.error("Throwable in QueryParameterProcessingQueue Thread! " + Thread.currentThread().getName() + ":", t);
            }
        }
    }
}

