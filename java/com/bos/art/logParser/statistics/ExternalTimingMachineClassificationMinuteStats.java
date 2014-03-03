/*
 * Created on Oct 22, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package com.bos.art.logParser.statistics;

import com.bos.art.logParser.broadcast.beans.ExternalAccessRecordsMinuteBean;
import com.bos.art.logParser.broadcast.network.CommunicationChannel;
import com.bos.art.logParser.db.AccessRecordPersistanceStrategy;
import com.bos.art.logParser.db.ConnectionPoolT;
import com.bos.art.logParser.db.ForeignKeyStore;
import com.bos.art.logParser.db.PersistanceStrategy;
import com.bos.art.logParser.records.ExternalEventTiming;
import com.bos.art.logParser.records.ILiveLogParserRecord;
import com.bos.helper.MutableSingletonInstanceHelper;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.bos.art.logServer.utils.StringConstants.*;
import static com.bos.art.logServer.utils.TimeIntervalConstants.FIVE_SECOND_DELAY;

/**
 * @author I0360D3
 *         <p/>
 *         To change the template for this generated type comment go to Window>Preferences>Java>Code Generation>Code and Comments
 */
public class ExternalTimingMachineClassificationMinuteStats extends StatisticsUnit {

    private static final Logger logger =
            (Logger) Logger.getLogger(ExternalTimingMachineClassificationMinuteStats.class.getName());
    private static final DateTimeFormatter fdf = DateTimeFormat.forPattern("yyyy-MM/dd HH:mm:ss");
    private static final DateTimeFormatter fdfKey = DateTimeFormat.forPattern("yyyyMMddHHmm");
    private static final String SQL_INSERT_STATEMENT =
            "insert into ExternalMinuteStatistics ("
                    + "Machine_id,             Time,  "
                    + "TotalLoads,             AverageLoadTime,  "
                    + "NinetiethPercentile,    TwentyFifthPercentile,"
                    + "FiftiethPercentile,     SeventyFifthPercentile, "
                    + "MaxLoadTime,            MinLoadTime, "
                    + "DistinctUsers,          ErrorPages, "
                    + "ThirtySecondLoads,      TwentySecondLoads, "
                    + "FifteenSecondLoads,     TenSecondLoads, "
                    + "FiveSecondLoads, Classification_ID) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    private static final String SQL_UPDATE_STATEMENT =
            "update ExternalMinuteStatistics set "
                    + "TotalLoads = ?,             AverageLoadTime = ?,  "
                    + "NinetiethPercentile = ?,    TwentyFifthPercentile = ?,"
                    + "FiftiethPercentile = ?,     SeventyFifthPercentile = ?, "
                    + "MaxLoadTime = ?,            MinLoadTime = ?, "
                    + "DistinctUsers = ?,          ErrorPages = ?, "
                    + "ThirtySecondLoads = ?,      TwentySecondLoads = ?, "
                    + "FifteenSecondLoads = ?,     TenSecondLoads = ?, "
                    + "FiveSecondLoads = ?,        State = ? where "
                    + "Machine_id = ? and Time = ? and Classification_ID= ?";
    private static MutableSingletonInstanceHelper instance = new MutableSingletonInstanceHelper<ExternalTimingMachineClassificationMinuteStats>(ExternalTimingMachineClassificationMinuteStats.class) {
        @Override
        public java.lang.Object createInstance() {
            return new ExternalTimingMachineClassificationMinuteStats();
        }
    };
    private static DateTimeFormatter sdf2 = DateTimeFormat.forPattern("yyyyMMddHHmmss");
    private ConcurrentHashMap<String, TimeSpanEventContainer> minutes;
    private int calls;
    private int eventsProcessed;
    private int timeSlices;
    private java.util.Date lastDataWriteTime;
    transient private PersistanceStrategy pStrat;
    private java.util.Date lastPersistDate;

    public ExternalTimingMachineClassificationMinuteStats() {
        minutes = new ConcurrentHashMap<String, TimeSpanEventContainer>();
        lastDataWriteTime = new java.util.Date();
        pStrat = AccessRecordPersistanceStrategy.getInstance();
    }

    public static ExternalTimingMachineClassificationMinuteStats getInstance() {
        return (ExternalTimingMachineClassificationMinuteStats) instance.getInstance();
    }

    public void setInstance(StatisticsUnit su) {
        if (su instanceof ExternalTimingMachineClassificationMinuteStats) {
            if (instance.getInstance() != null) {
                ((ExternalTimingMachineClassificationMinuteStats) instance.getInstance()).setRunnable(false);
            }
            instance.setInstance(su);
        }
    }

    /*
     * (non-Javadoc) @see
     * com.bos.art.logParser.statistics.StatisticsUnit#processRecord(com.bos.art.logParser.records.LiveLogParserRecord)
     */
    public void processRecord(ILiveLogParserRecord record) {
        if (record.isExternalAccessEvent()) {

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
            logger.warn("ExternalTimingMachineClassificationMinuteStats :  " + totalCount);
        }
        return;
    }

    private TimeSpanEventContainer getTimeSpanEventContainer(ILiveLogParserRecord record) {
        ExternalEventTiming eet = (ExternalEventTiming) record;
        String key = fdfKey.print(record.getEventTime().getTimeInMillis())
                + START_INSTANCE + eet.getInstance()
                + START_SERVER + eet.getServerName()
                + START_CLASSIFICATION + eet.getClassification();

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

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("\n\n\nExternalTimingMachineClassificationMinuteStats");
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

        DateTime nextWriteDate = new DateTime(lastDataWriteTime);
        nextWriteDate = nextWriteDate.plusSeconds(FIVE_SECOND_DELAY);

        DateTime broadcastCutOff = new DateTime();
        broadcastCutOff = broadcastCutOff.minusHours(2);

        if (logger.isDebugEnabled()) {
            logger.debug(
                    "persistCalled for External Minute Stats time:nextWriteDate: -- "
                            + System.currentTimeMillis()
                            + ":"
                            + nextWriteDate.getMillis()
                            + " diff:"
                            + (System.currentTimeMillis() - nextWriteDate.getMillis()));
        }

        DateTime now = new DateTime();
        if (now.isAfter(nextWriteDate)) {
            if (logger.isDebugEnabled()) {
                logger.debug(
                        "persistCalled for External Minute Stats time:nextWriteDate: -- "
                                + System.currentTimeMillis()
                                + ":"
                                + nextWriteDate.getMillis()
                                + " diff:"
                                + (System.currentTimeMillis() - nextWriteDate.getMillis()));
            }
            lastDataWriteTime = now.toDate();
            for (String nextKey : minutes.keySet()) {
                TimeSpanEventContainer tsec = minutes.get(nextKey);
                if (persistData(tsec, nextKey, broadcastCutOff)) {
                    minutes.remove(nextKey);
                }
            }
        }
    }

    private String getString(TimeSpanEventContainer tsec, String nextKey) {
        StringBuilder sb = new StringBuilder();
        int machineID =
                ForeignKeyStore.getInstance().getForeignKey(
                        tsec.getAccessRecordsForeignKeys(),
                        nextKey.substring(12),
                        ForeignKeyStore.FK_MACHINES_MACHINE_ID,
                        pStrat);
        sb.append("\nKey: " + nextKey);
        sb.append("\n sql:" + SQL_INSERT_STATEMENT);
        sb.append(",\n" + machineID);
        sb.append(",\n" + nextKey.substring(0, 12) + "00");
        sb.append(",\n" + tsec.getTotalLoads());
        sb.append(",\n" + tsec.getAverageLoadTime());
        sb.append(",\n" + tsec.get90Percentile());
        sb.append(",\n" + tsec.get25Percentile());
        sb.append(",\n" + tsec.get50Percentile());
        sb.append(",\n" + tsec.get75Percentile());
        sb.append(",\n" + tsec.getMaxLoadTime());
        sb.append(",\n" + tsec.getMinLoadTime());
        sb.append(",\n" + tsec.getDistinctUsers());
        sb.append(",\n" + tsec.getErrorPages());
        sb.append(",\n" + tsec.getThirtySecondLoads());
        sb.append(",\n" + tsec.getTwentySecondLoads());
        sb.append(",\n" + tsec.getFifteenSecondLoads());
        sb.append(",\n" + tsec.getTenSecondLoads());
        sb.append(",\n" + tsec.getFiveSecondLoads());
        return sb.toString();
    }

    /**
     * This persist Data will do the following: if the data is fresh, it will insert, if the data is stale but clean it will
     * move on, or if the data is stale and dirty, it will update.
     */
    private boolean persistData(
            TimeSpanEventContainer tsec,
            String nextKey,
            DateTime broadcastCutOffTime) {
        boolean shouldRemove = false;

        if (tsec.getTimesPersisted() == 0) {
            logger.info(
                    "External Timing FirstTime Persist for getTime()--lastModTime()"
                            + fdf.print(tsec.getTime().getTimeInMillis())
                            + "--"
                            + fdf.print(tsec.getLastModDate().getTime()));
            insertData(tsec, nextKey);
            logger.info(
                    "External Timing persistData Broadcast Called for ...[Initial Write]:"
                            + fdfKey.print(tsec.getTime().getTimeInMillis()));

            broadcast(tsec, nextKey);
        } else if (shouldCloseRecord(tsec)) {
            logger.info(
                    "External Timing Closing Data for getTime()--lastModTime()"
                            + fdf.print(tsec.getTime().getTimeInMillis())
                            + "--"
                            + fdf.print(tsec.getLastModDate().getTime()));
            updateAndCloseData(tsec, nextKey);
            shouldRemove = true;
            logger.info(
                    "External Timing persistData Broadcast Called for ...[Close Time Period]:"
                            + fdfKey.print(tsec.getTime().getTimeInMillis()));
            broadcast(tsec, nextKey);
        } else if (tsec.isDatabaseDirty()) {
            if (tsec.getTime().getTime().after(broadcastCutOffTime.toDate())) {
                logger.debug(
                        "External Timing Re-persist for getTime()--lastModTime()"
                                + fdf.print(tsec.getTime().getTimeInMillis())
                                + "--"
                                + fdf.print(tsec.getLastModDate().getTime()));
                updateData(tsec, nextKey, "O");
                logger.debug(
                        "External Timing persistData Broadcast Called for ...[Update Time Period]:"
                                + fdfKey.print(tsec.getTime().getTimeInMillis()));
                broadcast(tsec, nextKey);
            } else {
                logger.warn(
                        "External Timing Late Data for Minute Stats nextKey:Actuall Time : "
                                + nextKey
                                + ":"
                                + fdf.print(tsec.getTime().getTimeInMillis()));
            }
        }
        return shouldRemove;
    }

    private void insertData(TimeSpanEventContainer tsec, String nextKey) {
        Connection con = null;
        try {

            int startInstance = nextKey.indexOf(START_INSTANCE) + START_INSTANCE.length();
            int endInstance = nextKey.indexOf(START_SERVER);
            int startMachine = nextKey.indexOf(START_SERVER) + START_SERVER.length();
            int endMachine = nextKey.indexOf(START_CLASSIFICATION);
            int startClassification = endMachine + START_CLASSIFICATION.length();

            int machineID =
                    ForeignKeyStore.getInstance().getForeignKey(
                            tsec.getAccessRecordsForeignKeys(),
                            nextKey.substring(startMachine, endMachine),
                            ForeignKeyStore.FK_MACHINES_MACHINE_ID,
                            pStrat);

            int instanceID =
                    ForeignKeyStore.getInstance().getForeignKey(
                            tsec.getAccessRecordsForeignKeys(),
                            nextKey.substring(startInstance, endInstance),
                            ForeignKeyStore.FK_INSTANCES_INSTANCE_ID,
                            pStrat);
            int classificationID = 0;
            try {
                classificationID = Integer.parseInt(nextKey.substring(startClassification));
            } catch (NumberFormatException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            con = getConnection();
            PreparedStatement pstmt =
                    con.prepareStatement(SQL_INSERT_STATEMENT);
            pstmt.setInt(1, machineID);

            Date d = null;
            try {
                //d = sdf2.parse(nextKey.substring(0, 12) + "00");
                DateTime dt = sdf2.parseDateTime(nextKey.substring(0, 12) + "00");
                d = dt.toDate();
            } catch (IllegalArgumentException pe) {
                d = new Date();
            }


            //pstmt.setString(2, nextKey.substring(0, 12) + "00");
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
            pstmt.setInt(18, classificationID);
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
                    logger.error("insertData, Error Closing Connection ... ", t);
                }
            }
        }
    }

    private void updateData(
            TimeSpanEventContainer tsec,
            String nextKey,
            String state) {
//FIXME update query with instance ID
        Connection con = null;
        try {

            int startInstance = nextKey.indexOf(START_INSTANCE) + START_INSTANCE.length();
            int endInstance = nextKey.indexOf(START_SERVER);
            int startMachine = nextKey.indexOf(START_SERVER) + START_SERVER.length();
            int endMachine = nextKey.indexOf(START_CLASSIFICATION);
            int startClassification = endMachine + START_CLASSIFICATION.length();

            int machineID =
                    ForeignKeyStore.getInstance().getForeignKey(
                            tsec.getAccessRecordsForeignKeys(),
                            nextKey.substring(startMachine, endMachine),
                            ForeignKeyStore.FK_MACHINES_MACHINE_ID,
                            pStrat);

            int instanceID =
                    ForeignKeyStore.getInstance().getForeignKey(
                            tsec.getAccessRecordsForeignKeys(),
                            nextKey.substring(startInstance, endInstance),
                            ForeignKeyStore.FK_INSTANCES_INSTANCE_ID,
                            pStrat);

            int classificationID = 0;
            try {
                classificationID = Integer.parseInt(nextKey.substring(startClassification));
            } catch (NumberFormatException e) {
                logger.error("updateData parse exception", e);
            }
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

            Date d = null;
            try {
                //d = sdf2.parse(nextKey.substring(0, 12) + "00");
                DateTime dt = sdf2.parseDateTime(nextKey.substring(0, 12) + "00");
                d = dt.toDate();

            } catch (IllegalArgumentException pe) {
                logger.error("ExternalTimingMachingClassificationMinutStats bad date format: ", pe);
                d = new Date();
            }

            pstmt.setTimestamp(18, new Timestamp(d.getTime()));

            pstmt.setInt(19, classificationID);
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

    private void broadcast(TimeSpanEventContainer tsec, String nextKey) {
        ExternalAccessRecordsMinuteBean bean = new ExternalAccessRecordsMinuteBean(tsec, nextKey);
        try {
            CommunicationChannel.getInstance().broadcast(bean, null);
        } catch (Exception e) {
            logger.error("ExternalTimingMachineClassificationMinuteStats: Error broadcasting data", e);
        }

    }
}

