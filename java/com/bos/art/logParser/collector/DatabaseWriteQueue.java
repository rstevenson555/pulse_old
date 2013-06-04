/*
 * Created on Oct 21, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package com.bos.art.logParser.collector;


import com.bos.art.logParser.records.ILiveLogParserRecord;
import java.io.Serializable;
import java.util.concurrent.LinkedBlockingQueue;
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
    private LinkedBlockingQueue dequeue; // UnboundedFifoBuffer dequeue;
    private int objectsRemoved;
    private int objectsWritten;
    private long totalWriteTime;
    protected static boolean unloadDB = true;
    //private static final int MAX_DB_QUEUE_SIZE = 300000;
    private static final int MAX_DB_QUEUE_SIZE = 25000;
    private static long fullCount = 0;
    private static long writeCount = 0;
    // guards for boundaries
    
    private DatabaseWriteQueue() {
        dequeue = new LinkedBlockingQueue(MAX_DB_QUEUE_SIZE);
    }

    public static DatabaseWriteQueue getInstance() {
        return instance;
    }


    public void addLast(Object o) {

            boolean success = dequeue.offer(o);
            if (!success && (fullCount++ % 100) == 0) {
                logger.error("DatabaseWriteQueue is full, throwing out messages");
            }
    }

    public Object removeFirst() {
        try {

            Object o = dequeue.take();

            return o;
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
        /*if (dequeue instanceof BoundedLinkedQueue) {
            sb.append(((BoundedLinkedQueue) dequeue).size());
        } else if (dequeue instanceof BoundedBuffer) { */
            sb.append(((LinkedBlockingQueue) dequeue).size());
        //}
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
        while (unloadDB) {
            if (logger.isInfoEnabled()) {
                if (objectsRemoved % 100000 == 0) {
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
                if (o instanceof ILiveLogParserRecord) {
                    long writeStartTime = System.currentTimeMillis();

                    if ( writeCount++ % 1000 == 0) {
                        //logger.warn("DatabaseWriteQueue writeToDatabase called");
                    }
                    ((ILiveLogParserRecord) o).writeToDatabase();
                    totalWriteTime += (System.currentTimeMillis() - writeStartTime);
                    ++objectsWritten;
                } else {
                    logger.error("removeFirst gave " + o.getClass().getName());
                }
            } catch (Throwable t) {
                logger.error("Throwable in DatabaseWriteQueue Thread! " + Thread.currentThread().getName() + ":", t);
            }
        }
        }catch(Throwable t) {
            logger.error("DatabaseWriteQueue errored, should never happen!!!",t);
        }
    }
}

