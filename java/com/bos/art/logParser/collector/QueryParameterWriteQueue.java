/*
 * Created on Oct 21, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package com.bos.art.logParser.collector;


import com.bos.art.logParser.db.ConnectionPoolT;
import com.bos.art.logParser.records.QueryParameters;
import com.bos.art.logServer.utils.TPSCalculator;
import com.bos.helper.SingletonInstanceHelper;
import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.Util;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import javax.management.*;
import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * @author I0360D3
 *         <p/>
 *         To change the template for this generated type comment go to
 *         Window>Preferences>Java>Code Generation>Code and Comments
 */
public class QueryParameterWriteQueue implements QueryParameterWriteQueueMBean, Serializable {
    private static final Logger logger = (Logger) Logger.getLogger(QueryParameterWriteQueue.class.getName());
    private final static int MAXBATCHINSERTSIZE = 5000;
    private final static int INCREMENT_AMOUNT = 100;
    private final static int MINBATCHINSERTSIZE = 3000;
    private static int currentBatchInsertSize = MINBATCHINSERTSIZE;
    private static final int MAX_DB_QUEUE_SIZE = 3000;
    protected static boolean unloadDB = true;
    static AtomicInteger qpInstance = new AtomicInteger(0);
    private static SingletonInstanceHelper instance = new SingletonInstanceHelper<QueryParameterWriteQueue>(QueryParameterWriteQueue.class) {
        @Override
        public java.lang.Object createInstance() {
            return new QueryParameterWriteQueue();
        }
    };
    private static double timePerInsert = 5000.0;
    private static DateTime now = null;
    private static DateTime oneMinute = new DateTime().plusMinutes(1);
    private static long recordsPerMinute = 0;
    private static long fullCount = 0;
    private static BasicThreadFactory tFactory = new BasicThreadFactory.Builder()
            .namingPattern("QueryParameterWriteQueue-%d")
            .build();
    private final ExecutorService executor = Executors.newSingleThreadExecutor(tFactory);
    private static TPSCalculator tpsCalculator = new TPSCalculator();
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
    private Disruptor<DBQueryParamRecordEvent> disruptor = new Disruptor<DBQueryParamRecordEvent>(DBQueryParamRecordEvent.FACTORY, 512, executor,
            ProducerType.SINGLE, new BlockingWaitStrategy());
    private static long batch = 0;

    private static DateTime batchOneMinute = new DateTime().plusMinutes(1);
    private static DateTime batchNow = new DateTime();
    private int objectsRemoved;
    private int objectsWritten;
    private long totalWriteTime;

    private QueryParameterWriteQueue() {

        disruptor.handleExceptionsWith(new FatalExceptionHandler());

        DBQueryParamRecordEventHandler handler = new DBQueryParamRecordEventHandler();
        disruptor.handleEventsWith(handler);
        disruptor.start();

        registerWithMBeanServer();
    }

    public static QueryParameterWriteQueue getInstance() {
        return (QueryParameterWriteQueue) instance.getInstance();
    }

    /**
     * register the mbean with JMX
     */
    private void registerWithMBeanServer() {
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        ObjectName name = null;
        try {
            name = new ObjectName("com.omx.engine:type=QueryParameterWriteQueueMBean,name=QueryParameterWriteQueue-" + (qpInstance.incrementAndGet()));
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

    public void addLast(Object o) {
        addLast((QueryParameters.DBQueryParamRecord) o);
    }

    public void addLast(final QueryParameters.DBQueryParamRecord o) {
        boolean success = disruptor.getRingBuffer().tryPublishEvent(new EventTranslator<DBQueryParamRecordEvent>() {
            public void translateTo(DBQueryParamRecordEvent event, long sequence) {
                event.record = o;
            }

        });
        if (!success && (fullCount++ % 100) == 0) {
            logger.error("QueryParameterWriteQueue failed adding to the QueryParameterWriteQueue: ");
        }

    }

    public long size() {
        return (disruptor.getRingBuffer().getBufferSize() - disruptor.getRingBuffer().remainingCapacity());
    }

    // JMX Interface exposed
    public long getBufferSize() {
        return disruptor.getBufferSize();
    }

    /**
     * change the buffer size to nearest power of two
     *
     * @param sz
     */
    public void setBufferSize(long sz) {
        disruptor.shutdown();

        int psize = Util.ceilingNextPowerOfTwo((int) sz);

        disruptor = new Disruptor<DBQueryParamRecordEvent>(DBQueryParamRecordEvent.FACTORY, 4 * 1024, executor,
                ProducerType.SINGLE, new BlockingWaitStrategy());

        disruptor.handleExceptionsWith(new FatalExceptionHandler());

        DBQueryParamRecordEventHandler handler = new DBQueryParamRecordEventHandler();
        disruptor.handleEventsWith(handler);
        disruptor.start();
    }

    /**
     * get the remaining capacity of the ringbuffer
     *
     * @return
     */
    public long getRemainingCapacity() {
        return disruptor.getRingBuffer().remainingCapacity();
    }

    /**
     * return messages per second calculation
     *
     * @return
     */
    public long getMessagesPerSecond() {
        return tpsCalculator.getMessagesPerSecond();
    }

    public long getWriteCount() {
        return tpsCalculator.getTransactionCount();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("Database Write Queue size:");
        sb.append(size());
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

            batchNow = new DateTime();

            threadLocalInserts.set(new Integer(icount));
            if (icount % currentBatchInsertSize == 0) {
                long startTime = System.currentTimeMillis();

                pstmt.executeBatch();
                batch++;

                if (batchNow.isAfter(batchOneMinute)) {
                    logger.warn("QueryParameterWriteQueue " + " batch per minute: " + (batch) + " records per minute: " + (currentBatchInsertSize * batch));

                    batchOneMinute = batchNow.plusMinutes(1);
                    batch = 0;
                }

                long elapsed = System.currentTimeMillis() - startTime;
                double currentTimePerInsert = (double) elapsed / (double) currentBatchInsertSize;

                if (((currentTimePerInsert <= timePerInsert) && (currentBatchInsertSize < MAXBATCHINSERTSIZE - INCREMENT_AMOUNT))) {
                    currentBatchInsertSize += INCREMENT_AMOUNT;
                    timePerInsert = currentTimePerInsert;
                    logger.warn("QueryParameterWriteQueue currentBatchInsertSize set to-> : " + currentBatchInsertSize +
                            " time per insert: " + timePerInsert + " elapsed: " + elapsed);
                } else if ((currentTimePerInsert * .65) > timePerInsert
                        && (currentBatchInsertSize > MINBATCHINSERTSIZE + INCREMENT_AMOUNT)) {
                    currentBatchInsertSize -= INCREMENT_AMOUNT;
                    timePerInsert = currentTimePerInsert;
                    logger.warn("QueryParameterWriteQueue currentBatchInsertSize set to-> : " + currentBatchInsertSize + " time per insert: " + timePerInsert + " elapsed: " + elapsed);
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

    private static class DBQueryParamRecordEvent {
        public static final EventFactory<DBQueryParamRecordEvent> FACTORY = new EventFactory<DBQueryParamRecordEvent>() {
            public DBQueryParamRecordEvent newInstance() {
                return new DBQueryParamRecordEvent();
            }
        };
        private QueryParameters.DBQueryParamRecord record;
    }

    private class DBQueryParamRecordEventHandler implements EventHandler<DBQueryParamRecordEvent> {

        public DBQueryParamRecordEventHandler() {
        }

        public void onEvent(DBQueryParamRecordEvent event, long sequence, boolean endOfBatch) throws Exception {

            now = new DateTime();
            recordsPerMinute++;

            if (now.isAfter(oneMinute)) {
                logger.warn("QueryParameterWriteQueue records per minute: " + (recordsPerMinute));
                oneMinute = now.plusMinutes(1);
                recordsPerMinute = 0;
            }
            if (logger.isInfoEnabled()) {
                if (objectsRemoved % 10000 == 0) {
                    logger.info(toString());
                }
            }

            QueryParameters.DBQueryParamRecord dbqp = event.record;
            ++objectsRemoved;
            tpsCalculator.incrementTransaction();
            long sTime = System.currentTimeMillis();

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
            } finally {
                // Removed because of Thread Local.
                long sTime2 = System.currentTimeMillis();

                totalWriteTime += (sTime2 - sTime);
            }

        }
    }


}

