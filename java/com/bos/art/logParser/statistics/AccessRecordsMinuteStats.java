/*
 * Created on Oct 22, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package com.bos.art.logParser.statistics;

import com.bos.art.logParser.broadcast.beans.AccessRecordsMinuteBean;
import com.bos.art.logParser.broadcast.network.CommunicationChannel;
import com.bos.art.logParser.db.AccessRecordPersistanceStrategy;
import com.bos.art.logParser.db.ConnectionPoolT;
import com.bos.art.logParser.db.ForeignKeyStore;
import com.bos.art.logParser.db.PersistanceStrategy;
import com.bos.art.logParser.records.ILiveLogParserRecord;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * @author I0360D3
 *
 * To change the template for this generated type comment go to Window>Preferences>Java>Code Generation>Code and Comments
 */
public class AccessRecordsMinuteStats extends StatisticsUnit {

    private static final Logger logger =
            (Logger) Logger.getLogger(AccessRecordsMinuteStats.class.getName());
    private static AccessRecordsMinuteStats instance;
    private static final DateTimeFormatter fdf  = DateTimeFormat.forPattern("yyyy-MM/dd HH:mm:ss");
    private static final DateTimeFormatter fdfKey  = DateTimeFormat.forPattern("yyyyMMddHHmm");
    private ConcurrentHashMap<String, TimeSpanEventContainer> minutes;
    private int calls;
    private int eventsProcessed;
    private int timeSlices;
    private java.util.Date lastDataWriteTime;
    private static final int SECONDS_DELAY = 5;
    transient private PersistanceStrategy pStrat;
    private static DateTimeFormatter sdf2 = DateTimeFormat.forPattern("yyyyMMddHHmmss");

    public AccessRecordsMinuteStats() {
        minutes = new ConcurrentHashMap<String, TimeSpanEventContainer>();
        lastDataWriteTime = new java.util.Date();
        pStrat = AccessRecordPersistanceStrategy.getInstance();
    }

    public static AccessRecordsMinuteStats getInstance() {
        if (instance == null) {
            instance = new AccessRecordsMinuteStats();
        }
        return instance;
    }

    public void setInstance(StatisticsUnit su) {
        if (su instanceof AccessRecordsMinuteStats) {
            if (instance != null) {
                instance.runnable = false;
            }
            instance = (AccessRecordsMinuteStats) su;
        }
    }

    /*
     * (non-Javadoc) @see
     * com.bos.art.logParser.statistics.StatisticsUnit#processRecord(com.bos.art.logParser.records.LiveLogParserRecord)
     */
    public void processRecord(ILiveLogParserRecord record) {
        if (record.isAccessRecord()) {
            ++calls;
            TimeSpanEventContainer container =
                    getTimeSpanEventContainer(record);
            container.tally(
                    record.getLoadTime(),
                    record.isFirstTimeUser(),
                    record.isErrorPage());
            ++eventsProcessed;
        }
        if (calls > 0 && calls % 500000 == 0) {
            ++calls;
            int totalCount = 0;
            for (String nextKey : minutes.keySet()) {
                TimeSpanEventContainer tsec =
                        (TimeSpanEventContainer) minutes.get(nextKey);

                totalCount += tsec.getSize();
            }
            logger.warn("AccessRecordsMinuteStats  : " + totalCount);
        }
        return;
    }

    private TimeSpanEventContainer getTimeSpanEventContainer(ILiveLogParserRecord record) {
        String key = fdfKey.print(record.getEventTime().getTime().getTime());
        TimeSpanEventContainer container =
                (TimeSpanEventContainer) minutes.get(key);
        if (container == null) {
            ++timeSlices;
            container =
                    new TimeSpanEventContainer(
                        record.getServerName(),
                        record.getAppName(),
                        record.getContext(),
                        record.getRemoteHost(),
                        record.getEventTime(),
                        record.getInstance());
            minutes.put(key, container);
        }
        return container;
    }

    public Map<String, TimeSpanEventContainer> getData() {
        return minutes;
    }

    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("\n\n\nAccessRecordsMinuteStats");
        sb.append(calls).append(":").append(eventsProcessed).append(":").append(timeSlices).append("\n");
        sb.append(minutes.toString());
        return sb.toString();
    }

    /**
     * This persist Data will do the following: if the data is fresh, it will insert, if the data is stale but clean it will
     * move on, or if the data is stale and dirty, it will update.
     *
     * @see com.bos.art.logParser.statistics.StatisticsUnit#persistData()
     */
    public void persistData() {
        Calendar gc = new GregorianCalendar();
        gc.setTime(lastDataWriteTime);
        gc.add(Calendar.SECOND, SECONDS_DELAY);
        Date nextWriteDate = gc.getTime();
        gc.setTime(new java.util.Date());
        gc.add(Calendar.HOUR, -2);
        Date broadcastCutOff = gc.getTime();
        
        if ( logger.isDebugEnabled()) {
            logger.debug(
                    "persistCalled for Minute Stats time:nextWriteDate: -- "
                    + System.currentTimeMillis()
                    + ":"
                    + nextWriteDate.getTime()
                    + " diff:"
                    + (System.currentTimeMillis() - nextWriteDate.getTime()));
        }

        if (new java.util.Date().after(nextWriteDate)) {
            if ( logger.isDebugEnabled()) {
                logger.debug(
                        "persistCalled for Minute Stats time:nextWriteDate: -- "
                        + System.currentTimeMillis()
                        + ":"
                        + nextWriteDate.getTime()
                        + " diff:"
                        + (System.currentTimeMillis() - nextWriteDate.getTime()));
            }
            lastDataWriteTime = new java.util.Date();
            for (String nextKey : minutes.keySet()) {
                TimeSpanEventContainer tsec =
                        (TimeSpanEventContainer) minutes.get(nextKey);
                if (persistData(tsec, nextKey, broadcastCutOff)) {
                    minutes.remove(nextKey);
                }
            }
        }
    }
    private static final String SQL_INSERT_STATEMENT =
            "insert into MinuteStatistics ("
            + "Machine_id,             Time,  "
            + "TotalLoads,             AverageLoadTime,  "
            + "NinetiethPercentile,    TwentyFifthPercentile,"
            + "FiftiethPercentile,     SeventyFifthPercentile, "
            + "MaxLoadTime,            MinLoadTime, "
            + "DistinctUsers,          ErrorPages, "
            + "ThirtySecondLoads,      TwentySecondLoads, "
            + "FifteenSecondLoads,     TenSecondLoads, "
            + "FiveSecondLoads) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    private static final String SQL_UPDATE_STATEMENT =
            "update MinuteStatistics set "
            + "TotalLoads = ?,             AverageLoadTime = ?,  "
            + "NinetiethPercentile = ?,    TwentyFifthPercentile = ?,"
            + "FiftiethPercentile = ?,     SeventyFifthPercentile = ?, "
            + "MaxLoadTime = ?,            MinLoadTime = ?, "
            + "DistinctUsers = ?,          ErrorPages = ?, "
            + "ThirtySecondLoads = ?,      TwentySecondLoads = ?, "
            + "FifteenSecondLoads = ?,     TenSecondLoads = ?, "
            + "FiveSecondLoads = ?,        State = ? where "
            + "Machine_id = ? and Time = ? ";

    private String getString(TimeSpanEventContainer tsec, String nextKey) {
        StringBuilder sb = new StringBuilder();
        int machineID =
                ForeignKeyStore.getInstance().getForeignKey(
                tsec.getAccessRecordsForeignKeys(),
                nextKey.substring(12),
                ForeignKeyStore.FK_MACHINES_MACHINE_ID,
                pStrat);
        sb.append("\nKey: ").append(nextKey);
        sb.append("\n sql:" + SQL_INSERT_STATEMENT);
        sb.append(",\n").append(machineID);
        sb.append(",\n").append(nextKey.substring(0, 12)).append("00");
        sb.append(",\n").append(tsec.getTotalLoads());
        sb.append(",\n").append(tsec.getAverageLoadTime());
        sb.append(",\n").append(tsec.get90Percentile());
        StringBuilder append = sb.append(",\n").append(tsec.get25Percentile());
        sb.append(",\n").append(tsec.get50Percentile());
        sb.append(",\n").append(tsec.get75Percentile());
        StringBuilder append1 = sb.append(",\n").append(tsec.getMaxLoadTime());
        sb.append(",\n").append(tsec.getMinLoadTime());
        sb.append(",\n").append(tsec.getDistinctUsers());
        sb.append(",\n").append(tsec.getErrorPages());
        sb.append(",\n").append(tsec.getThirtySecondLoads());
        sb.append(",\n").append(tsec.getTwentySecondLoads());
        sb.append(",\n").append(tsec.getFifteenSecondLoads());
        sb.append(",\n").append(tsec.getTenSecondLoads());
        sb.append(",\n").append(tsec.getFiveSecondLoads());
        return sb.toString();
    }

    /**
     * This persist Data will do the following: if the data is fresh, it will insert, if the data is stale but clean it will
     * move on, or if the data is stale and dirty, it will update.
     */
    private boolean persistData(
            TimeSpanEventContainer tsec,
            String nextKey,
            Date broadcastCutOffTime) {
        //logger.info("persistData Called for :" + fdfKey.format(tsec.getTime()));
        boolean shouldRemove = false;

        if (tsec.getTimesPersisted() == 0) {
            if (logger.isDebugEnabled()) {
                logger.debug(
                        "FirstTime Persist for getTime()--lastModTime()"
                        + fdf.print(tsec.getTime().getTime().getTime())
                        + "--"
                        + fdf.print(tsec.getLastModDate().getTime()));
            }
            insertData(tsec, nextKey);
            if (logger.isDebugEnabled()) {
                logger.debug(
                        "persistData Broadcast Called for ...[Initial Write]:"
                        + fdfKey.print(tsec.getTime().getTimeInMillis()));
            }

            broadcast(tsec, nextKey);
        } else if (shouldCloseRecord(tsec)) {
            logger.info(
                    "Closing Data for getTime()--lastModTime()"
                    + fdf.print(tsec.getTime().getTime().getTime())
                    + "--"
                    + fdf.print(tsec.getLastModDate().getTime()));
            updateAndCloseData(tsec, nextKey);
            shouldRemove = true;
            logger.info(
                    "persistData Broadcast Called for ...[Close Time Period]:"
                    + fdfKey.print(tsec.getTime().getTimeInMillis()));
            broadcast(tsec, nextKey);
        } else if (tsec.isDatabaseDirty()) {
            if (tsec.getTime().getTime().after(broadcastCutOffTime)) {
                if ( logger.isDebugEnabled()) {
                    logger.debug(
                            "Re-persist for getTime()--lastModTime()"
                            + fdf.print(tsec.getTime().getTime().getTime())
                            + "--"
                            + fdf.print(tsec.getLastModDate().getTime()));
                }
                updateData(tsec, nextKey, "O");
                if ( logger.isDebugEnabled()) {
                    logger.debug(
                            "persistData Broadcast Called for ...[Update Time Period]:"
                            + fdfKey.print(tsec.getTime().getTimeInMillis()));
                }
                broadcast(tsec, nextKey);
            } else {
                logger.warn(
                        "Late Data for AccessRecordsMinute Stats nextKey:Actual Time : "
                        + nextKey
                        + ":"
                        + fdf.print(tsec.getTime().getTime().getTime()));
            }
        }
        return shouldRemove;
    }

    private void broadcast(TimeSpanEventContainer tsec, String nextKey) {
        AccessRecordsMinuteBean bean =
                new AccessRecordsMinuteBean(tsec, nextKey);
        try {
            CommunicationChannel.getInstance().broadcast(bean, null);
        } catch (Exception e) {
            logger.error("Error broadcasting data", e);
        }
    }

//	private SimpleDateFormat sdfForClose =
//		new SimpleDateFormat("yyyyMMddHHmmss");
//	private Calendar gcForClose = GregorianCalendar.getInstance();
    private void insertData(TimeSpanEventContainer tsec, String nextKey) {
        Connection con = null;
        try {

            int machineID =
                    ForeignKeyStore.getInstance().getForeignKey(
                    tsec.getAccessRecordsForeignKeys(),
                    nextKey.substring(12),
                    ForeignKeyStore.FK_MACHINES_MACHINE_ID,
                    pStrat);
            con = getConnection();
            PreparedStatement pstmt =
                    con.prepareStatement(SQL_INSERT_STATEMENT);
            pstmt.setInt(1, machineID);

            //pstmt.setString(2, nextKey.substring(0, 12) + "00");
            //
            Date d = null;
            try {
                //d = sdf2.parse(nextKey.substring(0, 12) + "00");
                DateTime dt = sdf2.parseDateTime(nextKey.substring(0, 12) + "00");
                d = dt.toDate();

            } catch (IllegalArgumentException pe) {
                d = new Date();
            }
            pstmt.setTimestamp(2, new java.sql.Timestamp(d.getTime()));

            pstmt.setInt(3, tsec.getTotalLoads());
            pstmt.setInt(4, tsec.getAverageLoadTime());
            pstmt.setInt(5, tsec.get90Percentile());
            pstmt.setInt(6, tsec.get25Percentile());
            pstmt.setInt(7, tsec.get50Percentile());
            pstmt.setInt(8, tsec.get75Percentile());
            pstmt.setInt(9, tsec.getMaxLoadTime());
            pstmt.setInt(10, tsec.getMinLoadTime());
            pstmt.setInt(11, tsec.getDistinctUsers());
            pstmt.setInt(12, tsec.getErrorPages());
            pstmt.setInt(13, tsec.getThirtySecondLoads());
            pstmt.setInt(14, tsec.getTwentySecondLoads());
            pstmt.setInt(15, tsec.getFifteenSecondLoads());
            pstmt.setInt(16, tsec.getTenSecondLoads());
            pstmt.setInt(17, tsec.getFiveSecondLoads());
            pstmt.execute();
            pstmt.close();

            tsec.setDatabaseDirty(false);
            tsec.setTimesPersisted(tsec.getTimesPersisted() + 1);

        } catch (SQLException se) {
            String message = se.getMessage();
            if (message != null && message.indexOf("Duplicate entry") < 0) {
                try {
                    se.printStackTrace();
                    con.rollback();

                } catch (SQLException sse) {
                    sse.printStackTrace();
                }
            }

        } finally {
            if (con != null) {
                try {
//					con.commit();
                    con.close();
                } catch (Throwable t) {
                    //TODO Logger
                    logger.error("Error Closing Connection ... ", t);
                }
            }
        }
    }

    private void updateData(
            TimeSpanEventContainer tsec,
            String nextKey,
            String state) {

        Connection con = null;
        try {

            int machineID =
                    ForeignKeyStore.getInstance().getForeignKey(
                    tsec.getAccessRecordsForeignKeys(),
                    nextKey.substring(12),
                    ForeignKeyStore.FK_MACHINES_MACHINE_ID,
                    pStrat);
            con = getConnection();
            PreparedStatement pstmt =
                    con.prepareStatement(SQL_UPDATE_STATEMENT);
            pstmt.setInt(1, tsec.getTotalLoads());
            pstmt.setInt(2, tsec.getAverageLoadTime());
            pstmt.setInt(3, tsec.get90Percentile());
            pstmt.setInt(4, tsec.get25Percentile());
            pstmt.setInt(5, tsec.get50Percentile());
            pstmt.setInt(6, tsec.get75Percentile());
            pstmt.setInt(7, tsec.getMaxLoadTime());
            pstmt.setInt(8, tsec.getMinLoadTime());
            pstmt.setInt(9, tsec.getDistinctUsers());
            pstmt.setInt(10, tsec.getErrorPages());
            pstmt.setInt(11, tsec.getThirtySecondLoads());
            pstmt.setInt(12, tsec.getTwentySecondLoads());
            pstmt.setInt(13, tsec.getFifteenSecondLoads());
            pstmt.setInt(14, tsec.getTenSecondLoads());
            pstmt.setInt(15, tsec.getFiveSecondLoads());
            pstmt.setString(16, state);
            pstmt.setInt(17, machineID);

            //pstmt.setString(18, nextKey.substring(0, 12) + "00");
            Date d = null;
            try {
                //d = sdf2.parse(nextKey.substring(0, 12) + "00");
                DateTime dt = sdf2.parseDateTime(nextKey.substring(0, 12) + "00");
                d = dt.toDate();

            } catch (IllegalArgumentException pe) {
                d = new Date();
            }

            pstmt.setTimestamp(18, new java.sql.Timestamp(d.getTime()));

            pstmt.execute();
            pstmt.close();

            tsec.setDatabaseDirty(false);
            tsec.setTimesPersisted(tsec.getTimesPersisted() + 1);

        } catch (SQLException se) {
            // TODO Logger
            se.printStackTrace();
            try {
                con.rollback();
            } catch (SQLException sse) {
                sse.printStackTrace();
            }

        } finally {
            if (con != null) {
                try {
//					con.commit();
                    con.close();
                } catch (Throwable t) {
                    //TODO Logger
                    logger.error("Error Closing Connection ...", t);
                }
            }
        }

    }

    private void updateAndCloseData(
            TimeSpanEventContainer tsec,
            String nextKey) {
        updateData(tsec, nextKey, "C");
    }

    Connection getConnection() throws SQLException {
        return ConnectionPoolT.getConnection();
    }

    public void flush() {
        persistData();
    }
}

