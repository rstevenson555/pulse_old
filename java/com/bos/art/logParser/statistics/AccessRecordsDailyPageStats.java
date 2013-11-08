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
import com.bos.art.logParser.db.PersistanceStrategy;
import com.bos.art.logParser.records.AccessRecordsForeignKeys;
import com.bos.art.logParser.records.ILiveLogParserRecord;
import com.bos.art.logParser.records.UserRequestTiming;
import static com.bos.art.logServer.utils.StringConstants.*;
import static com.bos.art.logServer.utils.TimeIntervalConstants.TEN_MINUTE_DELAY;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
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
public class AccessRecordsDailyPageStats extends StatisticsUnit {
    private static final int PRINTSTATS_MODVALUE = 500000;

    private static final DateTimeFormatter fdf  = DateTimeFormat.forPattern("yyyy-MM/dd HH:mm:ss");
    private static final int DATE_LENGTH = 8;
    private static final Logger logger =
            (Logger) Logger.getLogger(AccessRecordsDailyPageStats.class.getName());
    private static AccessRecordsDailyPageStats instance = new AccessRecordsDailyPageStats();
    private static DateTimeFormatter sdf = DateTimeFormat.forPattern("yyyyMMdd"); 
    private static DateTimeFormatter sdf2 = DateTimeFormat.forPattern("yyyyMMddHHmmss"); 

    private Map<String, TimeSpanEventContainer> hours;
    private int calls;
    private int eventsProcessed;
    private int timeSlices;
    private java.util.Date lastDataWriteTime;
    private static int counter = 0;

    public AccessRecordsDailyPageStats() {
        hours = new ConcurrentHashMap<String, TimeSpanEventContainer>();
        lastDataWriteTime = new java.util.Date();
    }

    public static AccessRecordsDailyPageStats getInstance() {
        return instance;
    }

    public void setInstance(StatisticsUnit su) {
        if (su instanceof AccessRecordsDailyPageStats) {
            if (instance != null) {
                instance.runnable = false;
            }
            instance = (AccessRecordsDailyPageStats) su;
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
        if (calls > 0 && calls % PRINTSTATS_MODVALUE == 0) {
            ++calls;
            int totalCount = 0;
            for (String nextKey : hours.keySet()) {
                TimeSpanEventContainer tsec =
                        (TimeSpanEventContainer) hours.get(nextKey);
                totalCount += tsec.getSize();
            }
            logger.warn("AccessRecordsDailyPageStats : " + totalCount);
        }
        return;
    }

    /*synchronized */private TimeSpanEventContainer getTimeSpanEventContainer(ILiveLogParserRecord record) {
        UserRequestTiming urt = (UserRequestTiming) record;
        String key =
                sdf.print(record.getEventTime().getTime().getTime())
                + CONTEXT
                + record.getContext()
                + PAGE
                + urt.getPage()
                + MACHINE_TYPE
                + ForeignKeyStore.getInstance().getMachineType(urt.getServerName()) 
                + START_INSTANCE
                + "ALL";
                //+ urt.getInstance();
        
        TimeSpanEventContainer container =
                (TimeSpanEventContainer) hours.get(key);
        if (container == null) {

            ++timeSlices;

            container = getFromDatabase(stripTime(record.getEventTime().getTime()), key, record.getEventTime());

            if (container == null) {
                container =
                        new TimeSpanEventContainer(
                            record.getServerName(),
                            record.getAppName(),
                            record.getContext(),
                            record.getRemoteHost(),
                            record.getEventTime(),
                                "ALL");
                            //record.getInstance());
            }
            hours.put(key, container);
        }
        return container;
    }
    private static final String SQL_SELECT_RECORD = "SELECT * from DailyPageLoadTimes where Day = ? and Page_ID = ? and Context_ID=? and machineType=? and instance_id=?";

    private TimeSpanEventContainer getFromDatabase(Date dateKey, String key, Calendar ltime) {
        Connection con = null;
        TimeSpanEventContainer container = null;
        int pageID = getPageIDFromKey(key, ltime.getTime());
        int contextID = getContextIDFromKey(key, ltime.getTime());
        String machineType = getMachineTypeFromKey(key, ltime.getTime());
        //int instanceID = getInstanceIDFromKey(key, ltime.getTime());
        int instanceID = 0;
        try {

            con = getConnection();
            PreparedStatement pstmt = con.prepareStatement(SQL_SELECT_RECORD);
            pstmt.setDate(1, new java.sql.Date(dateKey.getTime()));
            pstmt.setInt(2, pageID);
            pstmt.setInt(3, contextID);
            pstmt.setString(4, machineType);
            pstmt.setInt(5,instanceID);

            ResultSet rs = pstmt.executeQuery();
            boolean found = rs.next();
            if (found) {

                int ptotalLoads = rs.getInt("TotalLoads");
                int paverageLoadTime = rs.getInt("AverageLoadTime");
                int pmaxLoadTime = rs.getInt("MaxLoadTime");

                int pdistinctUsers = rs.getInt("DistinctUsers");
                int perrorPages = rs.getInt("ErrorPages");
                int pthirtySecondLoads = rs.getInt("ThirtySecondLoads");
                int ptwentySecondLoads = rs.getInt("TwentySecondLoads");
                int pfifteenSecondLoads = rs.getInt("FifteenSecondLoads");
                int ptenSecondLoads = rs.getInt("TenSecondLoads");
                int pfiveSecondLoads = rs.getInt("FiveSecondLoads");
                int preload90Percentile = rs.getInt("NinetiethPercentile");
                int preload25Percentile = rs.getInt("TwentyFifthPercentile");
                int preload50Percentile = rs.getInt("FiftiethPercentile");
                int preload75Percentile = rs.getInt("SeventyFifthPercentile");
                int pminLoadTime = 0;
                int ptotalUsers = pdistinctUsers;
                long ptotalLoadTime = 1l * paverageLoadTime * ptotalLoads;

                container = new TimeSpanEventContainer("Summary", "Summary", "Summary", "Summary", ltime,"Summary",
                        ptotalLoads,
                        paverageLoadTime,
                        ptotalLoadTime,
                        pmaxLoadTime,
                        pminLoadTime,
                        pdistinctUsers,
                        ptotalUsers,
                        perrorPages,
                        pthirtySecondLoads,
                        ptwentySecondLoads,
                        pfifteenSecondLoads,
                        ptenSecondLoads,
                        pfiveSecondLoads,
                        0,
                        0,
                        preload90Percentile,
                        preload75Percentile,
                        preload50Percentile,
                        preload25Percentile);
            }
            rs.close();
            pstmt.close();
        } catch (SQLException se) {
            logger.error("AccessRecordsDailyStats",se);
            try {
                con.rollback();
            } catch (SQLException sse) {
                logger.error("AccessRecordsDailyStats",sse);
            }

        } finally {
            if (con != null) {
                try {
//					con.commit();
                    con.close();
                } catch (Throwable t) {
                    logger.error("Exception Closing Connection .. ", t);
                }
            }
        }
        return container;

    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append("\nAccessRecordsDailyPageStats : ");
        sb.append(calls).append(":").append(eventsProcessed).append(":").append(timeSlices).append("\n");
        for (String nextKey : hours.keySet()) {
            sb.append("\nnextKey   : ").append(nextKey);
            TimeSpanEventContainer tsec =
                    (TimeSpanEventContainer) hours.get(nextKey);
            sb.append(tsec.getTotalLoads());
        }


        return sb.toString();
    }

    /*
     * (non-Javadoc) @see com.bos.art.logParser.statistics.StatisticsUnit#persistData()
     */
    public void persistData() {

        DateTime gc = new DateTime(lastDataWriteTime);
        gc.plusMinutes(TEN_MINUTE_DELAY);
        Date nextWriteDate = gc.toDate();        

        if (new java.util.Date().after(nextWriteDate)) {
            lastDataWriteTime = new java.util.Date();
            for (String nextKey : hours.keySet()) {
                TimeSpanEventContainer tsec =
                        (TimeSpanEventContainer) hours.get(nextKey);
                if (persistData(tsec, nextKey)) {
                    hours.remove(nextKey);
                }
            }
        }
    }

    private boolean persistData(TimeSpanEventContainer tsec, String nextKey) {
        boolean shouldRemove = false;
        if (tsec.getTimesPersisted() == 0) {
            //logger.warn("AccessRecords DailyPageStats persistDataM2 times persisted ==0 : " +nextKey);
            logger.info(
                    "FirstTime Persist for getTime()--lastModTime()"
                    + fdf.print(tsec.getTime().getTime().getTime())
                    + "--"
                    + fdf.print(tsec.getLastModDate().getTime()));
            insertData(tsec, nextKey);
            //TODO: Remove and reload from DataBase.
            tsec.setTimesPersisted(tsec.getTimesPersisted() + 1);
        } else if (shouldCloseRecord(tsec)) {
            //logger.warn("AccessRecords DailyPageStats persistDataM2 shouldCloseRecord: " +nextKey);
            logger.info(
                    "Closing Data for getTime()--lastModTime()"
                    + fdf.print(tsec.getTime().getTime().getTime())
                    + "--"
                    + fdf.print(tsec.getLastModDate().getTime()));
            updateAndCloseData(tsec, nextKey);
            shouldRemove = true;
        } else if (tsec.isDatabaseDirty()) {
            //logger.warn("AccessRecords DailyPageStats persistDataM2 isDatabaseDirty: " +nextKey);
            logger.info(
                    "Re-persist for getTime()--lastModTime()"
                    + fdf.print(tsec.getTime().getTime().getTime())
                    + "--"
                    + fdf.print(tsec.getLastModDate().getTime()));
            updateData(tsec, nextKey, "O");
        }
        //logger.warn("AccessRecords DailyPageStats persistDataM2 should Remove: " +nextKey +" should remove "+ shouldRemove);
        return shouldRemove;
    }
    private static final String SQL_INSERT_STATEMENT =
            "insert into DailyPageLoadTimes ( "
            + "Day,                    Page_ID, "
            + "Context_ID,             machineType,         "
            + "TotalLoads,             AverageLoadTime,  "
            + "NinetiethPercentile,    TwentyFifthPercentile,"
            + "FiftiethPercentile,     SeventyFifthPercentile, "
            + "MaxLoadTime,            MinLoadTime, "
            + "DistinctUsers,          ErrorPages, "
            + "ThirtySecondLoads,      TwentySecondLoads, "
            + "FifteenSecondLoads,     TenSecondLoads, "
            + "FiveSecondLoads,instance_id) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    private static final String SQL_UPDATE_STATEMENT =
            "update DailyPageLoadTimes set "
            + "TotalLoads = ?,             AverageLoadTime = ?,  "
            + "NinetiethPercentile = ?,    TwentyFifthPercentile = ?,"
            + "FiftiethPercentile = ?,     SeventyFifthPercentile = ?, "
            + "MaxLoadTime = ?,            MinLoadTime = ?, "
            + "DistinctUsers = ?,          ErrorPages = ?, "
            + "ThirtySecondLoads = ?,      TwentySecondLoads = ?, "
            + "FifteenSecondLoads = ?,     TenSecondLoads = ?, "
            + "FiveSecondLoads = ?,        State = ? where "
            + "Day = ? and Page_ID = ? and Context_ID = ? and machineType = ? and instance_id = ?";

    private int getContextIDFromKey(String nextKey, Date ldate) {
        int contextStart = nextKey.indexOf(CONTEXT) + CONTEXT.length();
        int contextEnd = nextKey.indexOf(PAGE);

        String contextName = nextKey.substring(contextStart, contextEnd);
        PersistanceStrategy pStrat = AccessRecordPersistanceStrategy.getInstance();
        int contextID =
                ForeignKeyStore.getInstance().getForeignKey(
                new AccessRecordsForeignKeys(ldate),
                contextName,
                ForeignKeyStore.FK_CONTEXTS_CONTEXT_ID,
                pStrat);
        return contextID;
    }

    private String getMachineTypeFromKey(String nextKey, Date ldate) {
        try {
            int machineTypeStart = nextKey.indexOf(MACHINE_TYPE) + MACHINE_TYPE.length();
            int machineTypeEnd = nextKey.indexOf(START_INSTANCE);
            return nextKey.substring(machineTypeStart,machineTypeEnd);
        } catch (RuntimeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return "NE";
        }
    }
    
    private int getInstanceIDFromKey(String nextKey, Date ldate) {
        int contextStart = nextKey.indexOf(START_INSTANCE) + START_INSTANCE.length();
        String instanceName = nextKey.substring(contextStart);
        
        PersistanceStrategy pStrat = AccessRecordPersistanceStrategy.getInstance();
        int instanceID =
                ForeignKeyStore.getInstance().getForeignKey(
                new AccessRecordsForeignKeys(ldate),
                instanceName,
                ForeignKeyStore.FK_INSTANCES_INSTANCE_ID,
                pStrat);
        return instanceID;
    }


    private int getPageIDFromKey(String nextKey, Date ldate) {
        int contextEnd = nextKey.indexOf(PAGE);
        int pageStart = contextEnd + PAGE.length();
        int pageEnd = nextKey.indexOf(MACHINE_TYPE);

        String pageName = nextKey.substring(pageStart, pageEnd);
        PersistanceStrategy pStrat = AccessRecordPersistanceStrategy.getInstance();
        int pageID =
                ForeignKeyStore.getInstance().getForeignKey(
                new AccessRecordsForeignKeys(ldate),
                pageName,
                ForeignKeyStore.FK_PAGES_PAGE_ID,
                pStrat);
        return pageID;
    }

    private void insertData(TimeSpanEventContainer tsec, String nextKey) {
        Connection con = null;
        int contextStart = nextKey.indexOf(CONTEXT) + CONTEXT.length();
        int contextEnd = nextKey.indexOf(PAGE);
        int pageStart = contextEnd + PAGE.length();
        int pageEnd = nextKey.indexOf(MACHINE_TYPE);
        int machineTypeStart = nextKey.indexOf(MACHINE_TYPE) + MACHINE_TYPE.length();
        int machineTypeEnd = nextKey.indexOf(START_INSTANCE);
        int instanceStart = nextKey.indexOf(START_INSTANCE) + START_INSTANCE.length();
       // int instanceEnd = nextKey.indexOf(START_INSTANCE);

        String contextName = nextKey.substring(contextStart, contextEnd);
        String pageName = nextKey.substring(pageStart, pageEnd);
        String machineType = nextKey.substring(machineTypeStart,machineTypeEnd);
        String instanceName = nextKey.substring(instanceStart);
        
        PersistanceStrategy pStrat = AccessRecordPersistanceStrategy.getInstance();
        int pageID = 0;
        int contextID = 0;
        int instanceID = 0;
        Date d = null;
        try {

            contextID =
                    ForeignKeyStore.getInstance().getForeignKey(
                    tsec.getAccessRecordsForeignKeys(),
                    contextName,
                    ForeignKeyStore.FK_CONTEXTS_CONTEXT_ID,
                    pStrat);
            
            instanceID =
//                    ForeignKeyStore.getInstance().getForeignKey(
//                    tsec.getAccessRecordsForeignKeys(),
//                    instanceName,
//                    ForeignKeyStore.FK_INSTANCES_INSTANCE_ID,
//                    pStrat);
            instanceID = 0;

            pageID =
                    ForeignKeyStore.getInstance().getForeignKey(
                    tsec.getAccessRecordsForeignKeys(),
                    pageName,
                    ForeignKeyStore.FK_PAGES_PAGE_ID,
                    pStrat);

            con = getConnection();
            PreparedStatement pstmt =
                    con.prepareStatement(SQL_INSERT_STATEMENT);

            try {
                DateTime dt = sdf2.parseDateTime(nextKey.substring(0, DATE_LENGTH) + "000000");
                d = dt.toDate();

            } catch (IllegalArgumentException pe) {
                logger.error("AccessRecordsDailyPageStats ParseException : ", pe);
                d = new Date();
            }

            pstmt.setDate(1, new java.sql.Date(d.getTime()));

            //pstmt.setString(1, nextKey.substring(0, DATE_LENGTH) + "000000");

            pstmt.setInt(2, pageID);
            pstmt.setInt(3, contextID);
            pstmt.setString(4, machineType);

            pstmt.setInt(5, tsec.getTotalLoads());
            pstmt.setInt(6, tsec.getAverageLoadTime());
            pstmt.setInt(7, tsec.get90Percentile());
            pstmt.setInt(8, tsec.get25Percentile());
            pstmt.setInt(9, tsec.get50Percentile());
            pstmt.setInt(10, tsec.get75Percentile());
            pstmt.setInt(11, tsec.getMaxLoadTime());
            pstmt.setInt(12, tsec.getMinLoadTime());
            pstmt.setInt(13, tsec.getDistinctUsers());
            pstmt.setInt(14, tsec.getErrorPages());
            pstmt.setInt(15, tsec.getThirtySecondLoads());
            pstmt.setInt(16, tsec.getTwentySecondLoads());
            pstmt.setInt(17, tsec.getFifteenSecondLoads());
            pstmt.setInt(18, tsec.getTenSecondLoads());
            pstmt.setInt(19, tsec.getFiveSecondLoads());
            pstmt.setInt(20, instanceID);
            pstmt.execute();
            pstmt.close();

            tsec.setDatabaseDirty(false);
            tsec.setTimesPersisted(tsec.getTimesPersisted() + 1);

        } catch (SQLException se) {
            String message = se.getMessage();
            if (message != null && !(message.indexOf("duplicate key") >= 0)) {
                try {
                    logger.warn("AccessRecordsDailyPageStts SQLException: time, page, context , machine " + sdf2.print(d.getTime()) + " " + pageID + " " + contextID
                            + " " + machineType, se);
                    con.rollback();
                } catch (SQLException sse) {
                    logger.warn("Entry Already exists, RollBack Error .", sse);
                }
            } else {
                logger.warn("AccessRecordsDailyPageStats Duplicate Key:" + pageID + ":" + contextID + ":" + sdf2.print(d.getTime()) + " message: " + message);
            }

        } finally {
            if (con != null) {
                try {
//					con.commit();
                    con.close();
                } catch (Throwable t) {
                    //TODO Logger
                    logger.error("Exception Closing Connection ", t);
                }
            }
        }
    }

    private void updateData(
            TimeSpanEventContainer tsec,
            String nextKey,
            String state) {
        Connection con = null;
        int contextStart = nextKey.indexOf(CONTEXT) + CONTEXT.length();
        int contextEnd = nextKey.indexOf(PAGE);
        int pageStart = contextEnd + PAGE.length();
        int pageEnd = nextKey.indexOf(MACHINE_TYPE);
        int machineTypeStart = nextKey.indexOf(MACHINE_TYPE) + MACHINE_TYPE.length();
        int machineTypeEnd = nextKey.indexOf(START_INSTANCE);
        int instanceStart = nextKey.indexOf(START_INSTANCE) + START_INSTANCE.length();
        
        String contextName = nextKey.substring(contextStart, contextEnd);
        String pageName = nextKey.substring(pageStart, pageEnd);
        String machineType = nextKey.substring(machineTypeStart,machineTypeEnd);
        String instanceName = nextKey.substring(instanceStart);

        PersistanceStrategy pStrat = AccessRecordPersistanceStrategy.getInstance();
        try {

            int contextID =
                    ForeignKeyStore.getInstance().getForeignKey(
                    tsec.getAccessRecordsForeignKeys(),
                    contextName,
                    ForeignKeyStore.FK_CONTEXTS_CONTEXT_ID,
                    pStrat);
            int pageID =
                    ForeignKeyStore.getInstance().getForeignKey(
                    tsec.getAccessRecordsForeignKeys(),
                    pageName,
                    ForeignKeyStore.FK_PAGES_PAGE_ID,
                    pStrat);
//            int instanceID =
//                    ForeignKeyStore.getInstance().getForeignKey(
//                    tsec.getAccessRecordsForeignKeys(),
//                    instanceName,
//                    ForeignKeyStore.FK_INSTANCES_INSTANCE_ID,
//                    pStrat);
            int instanceID = 0;

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
            Date d = null;
            try {
                DateTime dt = sdf2.parseDateTime(nextKey.substring(0, DATE_LENGTH) + "000000");
                d = dt.toDate();

            } catch (IllegalArgumentException pe) {
                logger.error("AccessRecordsDailyPageStats ParseException : ", pe);
                d = new Date();
            }
            pstmt.setDate(17, new java.sql.Date(d.getTime()));
            //pstmt.setString(17, nextKey.substring(0, DATE_LENGTH) + "000000");
            pstmt.setInt(18, pageID);
            pstmt.setInt(19, contextID);
            pstmt.setString(20, machineType);
            pstmt.setInt(21, instanceID);
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
                    logger.error("Exception Closing Connection", t);
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
    }
}

