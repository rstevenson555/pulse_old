/*
 * Created on Oct 22, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package com.bos.art.logParser.statistics;

import com.bos.art.logParser.db.AccessRecordPersistanceStrategy;
import com.bos.art.logParser.db.ConnectionPoolT;
import com.bos.art.logParser.db.ForeignKeyStore;
import com.bos.art.logParser.records.AccumulatorEventTiming;
import com.bos.art.logParser.records.ILiveLogParserRecord;
import com.bos.helper.MutableSingletonInstanceHelper;
import org.apache.log4j.Logger;
import org.joda.time.DateMidnight;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author I0360D3
 *         <p/>
 *         To change the template for this generated type comment go to
 *         Window>Preferences>Java>Code Generation>Code and Comments
 */
public class AccumulatorDailyStats extends StatisticsUnit {

    public static final String INTEGER_CLASS_NAME = "Integer";
    private static final Logger logger = (Logger) Logger.getLogger(AccumulatorDailyStats.class.getName());
    private static final int MINUTE_DELAY = 2;
    private static final String DOUBLE_CLASS_NAME = "Double";
    private static final String BIG_DECIMAL_CLASS_NAME = "BigDecimal";
    private static final String FLOAT_CLASS_NAME = "Float";
    private static final String LONG_CLASS_NAME = "Long";
    private static final String NUMBER_CLASS_NAME = "Number";
    private static final String SQL_SELECT_RECORD = "SELECT * from AccumulatorStats where Time = ? and AccumulatorStat_ID=? and Context_ID=?";
    private static final String SQL_INSERT_STATEMENT = "insert into  AccumulatorStats ( " +
            " Time, " +
            " AccumulatorStat_ID, " +
            " Value, Count, Context_ID ) " +
            " values(?,?,?,?,?)";
    private static final String SQL_UPDATE_STATEMENT = "update  AccumulatorStats set " +
            " Value = ?, Count = ?  " +
            " where Time = ? and AccumulatorStat_ID = ? and Context_ID=?";
    static DateTimeFormatter fdf = DateTimeFormat.forPattern("yyyy/MM/dd");
    static DateTimeFormatter fdfMysql = DateTimeFormat.forPattern("yyyy-MM-dd 00:00:00");
    private static MutableSingletonInstanceHelper instance = new MutableSingletonInstanceHelper<AccumulatorDailyStats>(AccumulatorDailyStats.class) {
        @Override
        public java.lang.Object createInstance() {
            return new AccumulatorDailyStats();
        }
    };
    private static DateTimeFormatter sdf = DateTimeFormat.forPattern("yyyyMMdd");
    private static DateTimeFormatter sdfzone = DateTimeFormat.forPattern("yyyyMMdd HH:mm:ss zzz");
    private static DateTimeFormatter mysql2 = DateTimeFormat.forPattern("yyyy-MM-dd 00:00:00");
    private ConcurrentHashMap<String, AccumulatorEventContainer> days;
    private int calls;
    private int eventsProcessed;
    private int timeSlices;
    private java.util.Date lastDataWriteTime;

    public AccumulatorDailyStats() {
        days = new ConcurrentHashMap<String, AccumulatorEventContainer>();
        lastDataWriteTime = new java.util.Date();
    }

    public static AccumulatorDailyStats getInstance() {
        return (AccumulatorDailyStats) instance.getInstance();
    }

    public void setInstance(StatisticsUnit su) {
        if (su instanceof AccumulatorDailyStats) {
            if (instance.getInstance()!=null) {
                ((AccumulatorDailyStats) instance.getInstance()).setRunnable(false);
            }
            instance.setInstance(su);
        }
    }

    /* (non-Javadoc)
     * @see com.bos.art.logParser.statistics.StatisticsUnit#processRecord(com.bos.art.logParser.records.LiveLogParserRecord)
     */
    public void processRecord(ILiveLogParserRecord rec) {
        if (rec.isAccumulatorEvent()) {

            ++calls;
            AccumulatorEventTiming event = (AccumulatorEventTiming) rec;
            if (event.getClassification() > 99999) {
                return;
            }
            AccumulatorEventContainer container = getAccumulatorEventContainer(event);
            String value = event.getValue();
            //
            //  If we have a dollar amount, we must multipy by 100 and convert it to Cents.
            //
            int ivalue = 0;
            if ((event.getType().indexOf(DOUBLE_CLASS_NAME) > -1)
                    || (event.getType().indexOf(BIG_DECIMAL_CLASS_NAME) > -1)
                    || (event.getType().indexOf(FLOAT_CLASS_NAME) > -1)
                    || (event.getType().indexOf(LONG_CLASS_NAME) > -1)
                    || (event.getType().indexOf(NUMBER_CLASS_NAME) > -1)) {
                ivalue = (int) ((double) 100 * (double) Double.parseDouble(value));
            } else if (event.getType().indexOf(INTEGER_CLASS_NAME) > -1) {
                ivalue = Integer.parseInt(value);
            } else if (event.getType().indexOf("String") > -1) {
                try {
                    ivalue = (int) ((double) 100 * (double) Double.parseDouble(value));
                } catch (NumberFormatException e) {
                    logger.debug("Exception setting string to double....for: " + value, e);
                    ivalue = 0;
                }
            }
            container.tally(ivalue, "sum");
            ++eventsProcessed;
        }
        if (calls > 0 && calls % 500000 == 0) {
            ++calls;
            int totalCount = 0;
            //synchronized (days) {
            //Enumeration keys = days.keys();
            //while (keys.hasMoreElements()) {
            //	String nextKey = (String) keys.nextElement();
            for (String nextKey : days.keySet()) {
                AccumulatorEventContainer tsec =
                        (AccumulatorEventContainer) days.get(nextKey);
                //totalCount+=tsec.getSize();
            }
            //}
            logger.warn("AccumulatorDailyStats : " + totalCount);
        }
        return;
    }

    /*synchronized*/
    private AccumulatorEventContainer getAccumulatorEventContainer(AccumulatorEventTiming record) {
        //String dateKey = sdf.format(record.getEventTime().getTime());
        String dateKey = sdf.print(record.getEventTime().getTimeInMillis());
        String context = record.getContext();
        String key = dateKey + record.getClassification() + context;
        AccumulatorEventContainer lcontainer = (AccumulatorEventContainer) days.get(key);

        if (lcontainer == null) {

            Calendar ltime = GregorianCalendar.getInstance();
            java.util.Date date = null;

            try {
                date = new DateMidnight(record.getEventTime().getTimeInMillis()).toDateTime().toDate();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }


            if (date != null) {
                ltime.setTime(date);
            } else {
                ltime.setTime(new java.util.Date());
                logger.error("Accumulator Event Daily Stats -- Setting unparseable date to current time");
            }

            ++timeSlices;
            // Try to get the container from the database.
            int contextid = ForeignKeyStore.getInstance().getForeignKey(null, context, ForeignKeyStore.FK_CONTEXTS_CONTEXT_ID, AccessRecordPersistanceStrategy.getInstance());

            lcontainer = getFromDatabase(stripTime(record.getEventTime().getTime()), key, ltime, record.getClassification(), contextid);

            if (lcontainer == null) {
                lcontainer = new AccumulatorEventContainer(record.getClassification(), ltime, contextid);
            }

            days.put(key, lcontainer);
        }
        return lcontainer;
    }

    private AccumulatorEventContainer getFromDatabase(Date dateKey, String key, Calendar ltime, int accumulatorID, int contextID) {
        Connection con = null;
        AccumulatorEventContainer container = null;
        try {
            con = getConnection();
            PreparedStatement pstmt = con.prepareStatement(SQL_SELECT_RECORD);
            //pstmt.setString(1, key                        );
            pstmt.setTimestamp(1, new java.sql.Timestamp(dateKey.getTime()));

            pstmt.setInt(2, accumulatorID);
            pstmt.setInt(3, contextID);
            ResultSet rs = pstmt.executeQuery();
            boolean returnval = rs.next();
            if (!returnval) {
                logger.error("AccumulatorDailyStats record not found; date, accumulatorid,contextid : " + sdfzone.print(dateKey.getTime()) + " " + accumulatorID + " " + contextID);
            }
            if (returnval) {
                int value = rs.getInt("Value");
                int count = rs.getInt("Count");
                container = new AccumulatorEventContainer(accumulatorID, ltime, value, count, contextID);
                container.setTimesPersisted(1);
            }
            rs.close();
            pstmt.close();
        } catch (SQLException se) {
            se.printStackTrace();
            try {
                con.rollback();
            } catch (SQLException sse) {
                sse.printStackTrace();
            }

        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (Throwable t) {
                    logger.error("Error Closing Connection ... ", t);
                }
            }
        }
        return container;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n\n\nAccumulatorStats : ");
        sb.append(calls).append(":").append(eventsProcessed).append(":").append(timeSlices).append("\n");
        sb.append(days.toString());
        return sb.toString();
    }

    /* (non-Javadoc)
     * @see com.bos.art.logParser.statistics.StatisticsUnit#persistData()
     */
    public void persistData() {
        Calendar gc = new GregorianCalendar();
        gc.setTime(lastDataWriteTime);
        gc.add(Calendar.MINUTE, MINUTE_DELAY);
        Date nextWriteDate = gc.getTime();

        if (new java.util.Date().after(nextWriteDate)) {
            lastDataWriteTime = new java.util.Date();
            for (String nextKey : days.keySet()) {
                AccumulatorEventContainer aec = (AccumulatorEventContainer) days.get(nextKey);
                if (persistData(aec, nextKey)) {
                    days.remove(nextKey);
                }
            }
        }
    }

    private boolean persistData(AccumulatorEventContainer aec, String nextKey) {
        boolean shouldRemove = false;
        if (aec.getTimesPersisted() == 0) {
            logger.info(
                    "FirstTime Persist for getTime()--lastModTime()"
                            + fdf.print(aec.getTime().getTimeInMillis())
                            + "--"
                            + fdf.print(aec.getLastModDate().getTime()));
            insertData(aec, nextKey);
            aec.setTimesPersisted(aec.getTimesPersisted() + 1);
        } else if (shouldCloseRecord(aec)) {
            logger.info(
                    "Closing Data for getTime()--lastModTime()"
                            + fdf.print(aec.getTime().getTimeInMillis())
                            + "--"
                            + fdf.print(aec.getLastModDate().getTime()));
            updateAndCloseData(aec, nextKey);
            aec.setTimesPersisted(aec.getTimesPersisted() + 1);
            shouldRemove = true;
        } else if (aec.isDatabaseDirty()) {
            logger.info(
                    "Re-persist for getTime()--lastModTime()"
                            + fdf.print(aec.getTime().getTimeInMillis())
                            + "--"
                            + fdf.print(aec.getLastModDate().getTime()));
            updateData(aec, nextKey, "O");
            aec.setTimesPersisted(aec.getTimesPersisted() + 1);
        }
        return shouldRemove;

    }

    private void updateAndCloseData(AccumulatorEventContainer dec, String nextKey) {
        updateData(dec, nextKey, "C");
    }

    private void insertData(AccumulatorEventContainer tsec, String nextKey) {
        Connection con = null;
        int accumulatorStatID = 0;
        Date d = null;
        try {
            try {
                d = new DateMidnight(tsec.getTime()).toDate();

            } catch (IllegalArgumentException pe) {
                logger.error("AccumlatorDailStats error: ", pe);
                d = new Date();
            }


            accumulatorStatID = tsec.getAccumulatorId();
            int value = tsec.getAccumulationStat();

            con = getConnection();
            PreparedStatement pstmt = con.prepareStatement(SQL_INSERT_STATEMENT);
            //pstmt.setString(1, time                        );
            //pstmt.setTimestamp(1, new java.sql.Timestamp( tsec.getTime().getTime().getTime()));
            pstmt.setTimestamp(1, new java.sql.Timestamp(d.getTime()));
            pstmt.setInt(2, accumulatorStatID);
            pstmt.setInt(3, value);
            pstmt.setInt(4, tsec.getAccumulationCount());
            pstmt.setInt(5, tsec.getContextID());

            pstmt.execute();
            pstmt.close();
        } catch (SQLException se) {
            String message = se.getMessage();
            if (message != null && !(message.indexOf("duplicate key violates unique constraint") >= 0)) {
                try {
                    con.rollback();
                } catch (SQLException sse) {
                    sse.printStackTrace();
                }
            } else {
                logger.error("AccumlatorDailyStats error inserting: time, accumulatorStatID " + mysql2.print(d.getTime()) + " " + accumulatorStatID, se);
            }

        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (Throwable t) {
                    logger.error("Error Closing Connection ... ", t);
                }
            }
        }
    }


    private void updateData(AccumulatorEventContainer tsec, String nextKey, String state) {

        Connection con = null;
        try {
            con = getConnection();
            //String time = fdfMysql.format(tsec.getTime());
            int accumulatorStatID = tsec.getAccumulatorId();
            int value = tsec.getAccumulationStat();
            PreparedStatement pstmt = con.prepareStatement(SQL_UPDATE_STATEMENT);
            pstmt.setInt(1, value);
            pstmt.setInt(2, tsec.getAccumulationCount());
            //pstmt.setString(3   ,   time                );
            //
            Date d = null;
            try {

                d = new DateMidnight(tsec.getTime()).toDate();

            } catch (IllegalArgumentException pe) {
                d = new Date();
            }
            //pstmt.setTimestamp(3, new java.sql.Timestamp( tsec.getTime().getTime().getTime() ));
            pstmt.setTimestamp(3, new java.sql.Timestamp(d.getTime()));
            pstmt.setInt(4, accumulatorStatID);
            pstmt.setInt(5, tsec.getContextID());

            pstmt.execute();
            pstmt.close();
        } catch (SQLException se) {
            logger.warn("Accumulator Event Container...SQLException", se);
            try {
                con.rollback();
            } catch (SQLException sse) {
                sse.printStackTrace();
            }

        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (Throwable t) {
                    logger.error("Error Closing Conneciton ", t);
                }
            }
        }
    }


    Connection getConnection() throws SQLException {
        return ConnectionPoolT.getConnection();
    }

    public void flush() {
    }

}
