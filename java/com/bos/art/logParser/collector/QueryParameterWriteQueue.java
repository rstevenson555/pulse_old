/*
 * Created on Oct 21, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package com.bos.art.logParser.collector;


import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.bos.art.logParser.db.ConnectionPoolT;
import com.bos.art.logParser.records.QueryParameters;
import java.util.concurrent.LinkedBlockingQueue;


/**
 * @author I0360D3
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class QueryParameterWriteQueue extends Thread implements Serializable {
    private static final Logger logger = (Logger) Logger.getLogger(QueryParameterWriteQueue.class.getName());
    private static QueryParameterWriteQueue instance;
    private LinkedBlockingQueue dequeue; // UnboundedFifoBuffer dequeue;
    private int objectsRemoved;
    private int objectsWritten;
    private long totalWriteTime;

    private static final int BATCH_INSERT_SIZE = 500;
    private final static int MAXBATCHINSERTSIZE = 5000;
    private final static int INCREMENT_AMOUNT = 10;
    private final static int MINBATCHINSERTSIZE = 10;
    private static int lastBatchInsertSize = MINBATCHINSERTSIZE;
    private static int currentBatchInsertSize = MINBATCHINSERTSIZE;
    private static double timePerInsert = 5000.0;

    protected static boolean unloadDB = true;
    //private static final int MAX_DB_QUEUE_SIZE = 200000;
    // QUEUE SIZE
    private static final int MAX_DB_QUEUE_SIZE = 5000;
    
    private QueryParameterWriteQueue() {
        dequeue = new LinkedBlockingQueue(MAX_DB_QUEUE_SIZE);
    }

    public static QueryParameterWriteQueue getInstance() {
        if (instance == null) {
            instance = new QueryParameterWriteQueue();
        }
        return instance;
    }

    public void addLast(Object o) {

        boolean success = dequeue.offer(o);
        if (!success) {
            logger.error("QueryParameterWriteQueue failed adding to the QueryParameterWriteQueue: ");
        }

    }

    public Object removeFirst() {
        try {
            // if empty wait until someone puts something in
            
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
        StringBuffer sb = new StringBuffer();

        sb.append("Database Write Queue size:");
        /*if (dequeue instanceof BoundedLinkedQueue) {
            sb.append(((BoundedLinkedQueue) dequeue).size());
        } else if (dequeue instanceof BoundedBuffer) { */
            sb.append(((LinkedBlockingQueue) dequeue).size());
        //}
        sb.append("\t\t this thread: ");
        sb.append(Thread.currentThread().getName());
        sb.append("\n\tObjects Popped              :  " + objectsRemoved);
        sb.append("\n\tObjects Written             :  " + objectsWritten);
        if (objectsWritten > 1000) {
            sb.append("\n\tWrite Time millis per 1000  :  " + totalWriteTime / (objectsWritten / 1000));
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
        while (unloadDB) {
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
                if (o instanceof QueryParameters.DBQueryParamRecord) {
                    long sTime = System.currentTimeMillis();	
                    QueryParameters.DBQueryParamRecord  dbqp = (QueryParameters.DBQueryParamRecord) o;

                    try {
                        PreparedStatement pstmt = (PreparedStatement) threadLocalPstmt.get();

                        pstmt.setInt(1, dbqp.getRecordPK().intValue());
                        pstmt.setInt(2, dbqp.getQueryParameterID().intValue());
                        blockInsert(pstmt);
                        ++objectsWritten;
                        // requestType, requestToken, userServiceTime
                    } catch (SQLException se) {
                        logger.error("Exception", se);
                        resetThreadLocalPstmt();
                        // return false;
                    }
                    finally {
                        // Removed because of Thread Local.
                        long sTime2 = System.currentTimeMillis();	

                        totalWriteTime += (sTime2 - sTime);
                    }
            
                } else {
                    logger.error("removeFirst gave " + o.getClass().getName());
                }
            } catch (RuntimeException t) {
                logger.error("Throwable in QueryParameterWiteQueue Thread! " + Thread.currentThread().getName() + ":", t);
            }
        }
    }

    private static ThreadLocal threadLocalCon = new ThreadLocal() {
        @Override
        protected synchronized Object initialValue() {
            try {
                return ConnectionPoolT.getConnection();
            } catch (SQLException se) {
                logger.error("SQL Exception ", se);
            }
            return null;
        }
    };
   
    private static ThreadLocal threadLocalPstmt = new ThreadLocal() {
        @Override
        protected synchronized Object initialValue() {
            try {
                return ((Connection) threadLocalCon.get()).prepareStatement(
                        "insert into QueryParamRecords (RecordPK, QueryParameter_ID) values (?,?) ");
            } catch (SQLException se) {
                logger.error("SQL Exception ", se);
            }
            return null;
        }
    };
    private static ThreadLocal threadLocalInserts = new ThreadLocal() {
        @Override
        protected synchronized Object initialValue() {
            return new Integer(0);
        }
    };

    public void resetThreadLocalPstmt() {
        logger.info("Resetting the Pstmt!");
        PreparedStatement ps = (PreparedStatement) threadLocalPstmt.get();
        Connection con = (Connection) threadLocalCon.get();

        try {
            try {
                if (ps != null) {
                    ps.close();
                    ps = null;
                }
                if (con != null) {
                    con.close();
                    con = null;
                }
            } catch (SQLException se) {
                logger.error("Exception resetting the ThreadLocal PreparedStatement", se);
            }
            con = ConnectionPoolT.getConnection();
            ps = con.prepareStatement("insert into QueryParamRecords (RecordPK, QueryParameter_ID) values (?,?) ");
            threadLocalCon.set(con);
            threadLocalPstmt.set(ps);
        } catch (Exception e) {
            logger.error("Exception ", e);
        }
    }

    public void blockInsert(PreparedStatement pstmt) {
        try {
            pstmt.addBatch();
            Integer count = (Integer) threadLocalInserts.get();
            int icount = count.intValue() + 1;

            threadLocalInserts.set(new Integer(icount));
            if (icount % currentBatchInsertSize == 0) {
                long startTime = System.currentTimeMillis();

                pstmt.executeBatch();
                long elapsed = System.currentTimeMillis() - startTime;
                double currentTimePerInsert = (double) elapsed / (double) currentBatchInsertSize;

                if (((currentTimePerInsert <= timePerInsert) && (currentBatchInsertSize < MAXBATCHINSERTSIZE - INCREMENT_AMOUNT))) {
                    currentBatchInsertSize += INCREMENT_AMOUNT;
                    timePerInsert = currentTimePerInsert;
                    // logger.warn("QueryParameterWriteQueueu currentBatchInsertSize set to-> : " + currentBatchInsertSize);
                } else if ((currentTimePerInsert * .65) > timePerInsert
                        && (currentBatchInsertSize > MINBATCHINSERTSIZE + INCREMENT_AMOUNT)) {
                    currentBatchInsertSize -= INCREMENT_AMOUNT;
                    timePerInsert = currentTimePerInsert;
                    // logger.warn("QueryParameterWriteQueueu currentBatchInsertSize set to-> : " + currentBatchInsertSize);
                }
                if (icount % 100000 == 0) {
                    logger.warn("QueryParameterWriteQueueu currentBatchInsertSize is-> : " + currentBatchInsertSize);
                }
                
            }
        } catch (SQLException se) {
            logger.error("Exception", se);
            resetThreadLocalPstmt();
        }
    }
}

