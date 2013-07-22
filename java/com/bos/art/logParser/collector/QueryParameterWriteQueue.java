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
import org.joda.time.DateTime;


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

    private final static int MAXBATCHINSERTSIZE = 2000;
    private final static int INCREMENT_AMOUNT = 10;
    private final static int MINBATCHINSERTSIZE = 1200;
    private static int currentBatchInsertSize = MINBATCHINSERTSIZE;
    private static double timePerInsert = 5000.0;

    protected static boolean unloadDB = true;
    private static final int MAX_DB_QUEUE_SIZE = 200000;
    // QUEUE SIZE
    //private static final int MAX_DB_QUEUE_SIZE = 5000;
    //private static final int MAX_DB_QUEUE_SIZE = 50000;
    
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

    private static DateTime now = null;
    private static DateTime oneMinute = new DateTime().plusMinutes(1);
    private static long recordsPerMinute = 0;

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        while (unloadDB) {
            
            now = new DateTime();
            recordsPerMinute++;

            if ( now.isAfter(oneMinute)) {
                logger.warn("QueryParameterWriteQueue records per minute: " + (recordsPerMinute));
                oneMinute = now.plusMinutes(1);
                recordsPerMinute = 0;
            }
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

    private static long batch = 0;
    private static DateTime batchOneMinute = new DateTime().plusMinutes(1);
    private static DateTime batchNow = new DateTime();

    public void blockInsert(PreparedStatement pstmt) {
        try {
            pstmt.addBatch();
            Integer count = (Integer) threadLocalInserts.get();
            int icount = count.intValue() + 1;

            batchNow = new DateTime();
            
            threadLocalInserts.set(new Integer(icount));
            if (icount % currentBatchInsertSize == 0) {
                long startTime = System.currentTimeMillis();

                pstmt.executeBatch();
                batch++;

                if(batchNow.isAfter(batchOneMinute)) {
                    logger.warn("QueryParameterWriteQueue " + " batch per minute: " + (batch) + " records per minute: " + (currentBatchInsertSize*batch));

                    batchOneMinute = batchNow.plusMinutes(1);
                    batch = 0;
                }
                
                long elapsed = System.currentTimeMillis() - startTime;
                double currentTimePerInsert = (double) elapsed / (double) currentBatchInsertSize;

                if (((currentTimePerInsert <= timePerInsert) && (currentBatchInsertSize < MAXBATCHINSERTSIZE - INCREMENT_AMOUNT))) {
                    currentBatchInsertSize += INCREMENT_AMOUNT;
                    timePerInsert = currentTimePerInsert;
                    logger.warn("QueryParameterWriteQueue currentBatchInsertSize set to-> : " + currentBatchInsertSize + " time per insert: " + timePerInsert + " elapsed: " + elapsed);
                } else if ((currentTimePerInsert * .65) > timePerInsert
                        && (currentBatchInsertSize > MINBATCHINSERTSIZE + INCREMENT_AMOUNT)) {
                    currentBatchInsertSize -= INCREMENT_AMOUNT;
                    timePerInsert = currentTimePerInsert;
                    logger.warn("QueryParameterWriteQueue currentBatchInsertSize set to-> : " + currentBatchInsertSize+ " time per insert: " + timePerInsert+ " elapsed: " + elapsed);
                }
                

                if (icount % 100000 == 0) {
                    logger.warn("QueryParameterWriteQueue currentBatchInsertSize is-> : " + currentBatchInsertSize);
                }
                
            }
        } catch (SQLException se) {
            logger.error("Exception", se);
            resetThreadLocalPstmt();
        }
    }
}

