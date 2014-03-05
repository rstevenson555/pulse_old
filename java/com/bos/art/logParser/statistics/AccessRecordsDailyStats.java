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
import com.bos.art.logParser.records.ILiveLogParserRecord;
import com.bos.art.logParser.records.UserRequestTiming;
import com.bos.helper.MutableSingletonInstanceHelper;
import org.apache.log4j.Logger;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author I0360D3
 *         <p/>
 *         To change the template for this generated type comment go to
 *         Window>Preferences>Java>Code Generation>Code and Comments
 */
public class AccessRecordsDailyStats extends StatisticsUnit {

    private static final Logger logger = (Logger) Logger.getLogger(AccessRecordsDailyStats.class.getName());
    private static final int MINUTE_DELAY = 2;
    private static final String SQL_SELECT_RECORD = "SELECT * from DailySummary where DAY = ?";
    private static final String SQL_INSERT_STATEMENT = "insert into  DailySummary ( " +
            " Day, " +
            " ThirtySecondLoads, " +
            " TwentySecondLoads, " +
            " FifteenSecondLoads, " +
            " TenSecondLoads, " +
            " FiveSecondLoads, " +
            " TotalLoads, " +
            " AverageLoadTime, " +
            " NinetiethPercentile, " +
            " TwentyFifthPercentile, " +
            " FiftiethPercentile, " +
            " SeventyFifthPercentile, " +
            " MaxLoadTime, " +
            " MaxLoadTime_Page_ID, " +
            " MaxLoadTime_User_ID, " +
            " DistinctUsers, " +
            " ErrorPages ) " +
            " values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    private static final String SQL_UPDATE_STATEMENT = "update  DailySummary set " +
            " ThirtySecondLoads = ?, " +
            " TwentySecondLoads = ?, " +
            " FifteenSecondLoads = ?, " +
            " TenSecondLoads = ?, " +
            " FiveSecondLoads = ?, " +
            " TotalLoads = ?, " +
            " AverageLoadTime = ?, " +
            " NinetiethPercentile = ?, " +
            " TwentyFifthPercentile = ?, " +
            " FiftiethPercentile = ?, " +
            " SeventyFifthPercentile = ?, " +
            " MaxLoadTime = ?, " +
            " MaxLoadTime_Page_ID = ?, " +
            " MaxLoadTime_User_ID = ?, " +
            " DistinctUsers = ?, " +
            " ErrorPages = ?, " +
            " State = ? " +
            " where Day = ?";
    static DateTimeFormatter fdf = DateTimeFormat.forPattern("yyyy/MM/dd");
    private static MutableSingletonInstanceHelper instance = new MutableSingletonInstanceHelper<AccessRecordsDailyStats>(AccessRecordsDailyStats.class) {
        @Override
        public java.lang.Object createInstance() {
            return new AccessRecordsDailyStats();
        }
    };
    private static DateTimeFormatter sdf = DateTimeFormat.forPattern("yyyyMMdd");
    private static int counter = 0;
    private ConcurrentHashMap<String, TimeSpanEventContainer> days;
    private int calls;
    private int eventsProcessed;
    volatile private int timeSlices;
    private DateTime lastDataWriteTime;

    public AccessRecordsDailyStats() {
        days = new ConcurrentHashMap<String, TimeSpanEventContainer>();
        lastDataWriteTime = new DateTime();
    }

    public static AccessRecordsDailyStats getInstance() {
        return (AccessRecordsDailyStats) instance.getInstance();
    }

    public void setInstance(StatisticsUnit su) {

        if (su instanceof AccessRecordsDailyStats) {
            if (instance.getInstance() != null) {
                ((AccessRecordsDailyStats) instance.getInstance()).setRunnable(false);
            }
            instance.setInstance(su);
        }
    }

    /* (non-Javadoc)
     * @see com.bos.art.logParser.statistics.StatisticsUnit#processRecord(com.bos.art.logParser.records.LiveLogParserRecord)
     */
    public void processRecord(ILiveLogParserRecord rec) {
        int recordCount = 0;
        if (rec.isAccessRecord()) {
            ++calls;
            UserRequestTiming record = (UserRequestTiming) rec;
            TimeSpanEventContainer container = getTimeSpanEventContainer(record);
            if (record.getUserKey() == null || record.getPage() == null) {
                logger.warn("Bad Record Class Name : " + record.getClass().getName());
                logger.warn("User: Null or Page: Null " + record.toString());

            } else {
                int user_id = ForeignKeyStore.getInstance().getForeignKey(record.obtainForeignKeys(), record.getUserKey(), ForeignKeyStore.FK_USERS_USER_ID, AccessRecordPersistanceStrategy.getInstance());
                int page_id = ForeignKeyStore.getInstance().getForeignKey(record.obtainForeignKeys(), record.getPage(), ForeignKeyStore.FK_PAGES_PAGE_ID, AccessRecordPersistanceStrategy.getInstance());
                container.tally(record.getLoadTime(), record.isFirstTimeUser(), record.isErrorPage(), page_id, user_id);
                ++eventsProcessed;
            }
            recordCount = container.getSize();
        }
        if (calls > 0 && calls % 500000 == 0) {
            ++calls;
            int totalCount = 0;
            for (String nextKey : days.keySet()) {
                TimeSpanEventContainer tsec = days.get(nextKey);
                totalCount += tsec.getSize();
            }

            logger.warn("AccessRecordsDailyStats : " + totalCount);
        }
        return;
    }

    private TimeSpanEventContainer getTimeSpanEventContainer(ILiveLogParserRecord record) {
        String key = sdf.print(record.getEventTime().getTimeInMillis());
        TimeSpanEventContainer container =  days.get(key);
        if (container == null) {

            DateTime ltime = null;
            DateTime date = null;

            try {
                date = new DateMidnight(record.getEventTime().getTimeInMillis()).toDateTime();

            } catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                date = new DateTime();
                e.printStackTrace();
            }

            if (date != null) {
                ltime = new DateTime(date);
            } else {
                ltime = new DateTime();
                logger.error("Access Records Daily Stats -- Setting unparseable date to current time");
            }

            ++timeSlices;
            // Try to get the container from the database.
            container = getFromDatabase(stripTime(record.getEventTime().getTime()), null, ltime);

            if (container == null) {
                container = new TimeSpanEventContainer(record.getServerName(), record.getAppName(), record.getContext(), record.getRemoteHost(), ltime.toGregorianCalendar(), record.getInstance());
            }

            days.put(key, container);
        }
        return container;
    }

    private TimeSpanEventContainer getFromDatabase(Date dateKey, String k, DateTime dateTime) {
        Connection con = null;
        TimeSpanEventContainer container = null;
        try {
            con = getConnection();
            PreparedStatement pstmt = con.prepareStatement(SQL_SELECT_RECORD);
            pstmt.setDate(1, new java.sql.Date(dateKey.getTime()));

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                //logger.error("AccessRecordsDailyStats after query");
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
                int pmaxLoadTimePageID = rs.getInt("MaxLoadTime_Page_ID");
                int pmaxLoadTimeUserID = rs.getInt("MaxLoadTime_User_ID");
                int preload90Percentile = rs.getInt("NinetiethPercentile");
                int preload25Percentile = rs.getInt("TwentyFifthPercentile");
                int preload50Percentile = rs.getInt("FiftiethPercentile");
                int preload75Percentile = rs.getInt("SeventyFifthPercentile");
                int pminLoadTime = 0;
                int ptotalUsers = pdistinctUsers;
                long ptotalLoadTime = 1l * paverageLoadTime * ptotalLoads;


                container = new TimeSpanEventContainer("Summary", "Summary", "Summary", "Summary", dateTime.toGregorianCalendar(), "Summary",
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
                        pmaxLoadTimeUserID,
                        pmaxLoadTimePageID,
                        preload90Percentile,
                        preload75Percentile,
                        preload50Percentile,
                        preload25Percentile
                );
            }
            rs.close();
            pstmt.close();
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
                    con.close();
                } catch (Throwable t) {
                    //TODO Logger
                    logger.error("Error Closing Connection ... ", t);
                }
            }
        }

        return container;

    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("\n\n\nAccessRecordsDailyStats");
        sb.append(calls).append(":").append(eventsProcessed).append(":").append(timeSlices).append("\n");
        sb.append(days.toString());
        return sb.toString();
    }

    /* (non-Javadoc)
     * @see com.bos.art.logParser.statistics.StatisticsUnit#persistData()
     */
    public void persistData() {
        DateTime nextWriteDate = new DateTime(lastDataWriteTime);
        nextWriteDate = nextWriteDate.plusMinutes(MINUTE_DELAY);

        if (new DateTime().isAfter(nextWriteDate)) {
            lastDataWriteTime = new DateTime();
            for (String nextKey : days.keySet()) {
                TimeSpanEventContainer tsec = days.get(nextKey);
                if (persistData(tsec, nextKey)) {
                    days.remove(nextKey);
                }
            }
        }
    }

    private boolean persistData(TimeSpanEventContainer dec, String nextKey) {
        boolean shouldRemove = false;
        if (dec.getTimesPersisted() == 0) {
            logger.info(
                    "FirstTime Persist for getTime()--lastModTime()"
                            + fdf.print(dec.getTime().getTimeInMillis())
                            + "--"
                            + fdf.print(dec.getLastModDate().getTime()));
            insertData(dec, nextKey);
            dec.setTimesPersisted(dec.getTimesPersisted() + 1);
        } else if (shouldCloseRecord(dec)) {
            logger.info(
                    "Closing Data for getTime()--lastModTime()"
                            + fdf.print(dec.getTime().getTimeInMillis())
                            + "--"
                            + fdf.print(dec.getLastModDate().getTime()));
            updateAndCloseData(dec, nextKey);
            dec.setTimesPersisted(dec.getTimesPersisted() + 1);
            shouldRemove = true;
        } else if (dec.isDatabaseDirty()) {
            logger.info(
                    "Re-persist for getTime()--lastModTime()"
                            + fdf.print(dec.getTime().getTimeInMillis())
                            + "--"
                            + fdf.print(dec.getLastModDate().getTime()));
            updateData(dec, nextKey, "O");
            dec.setTimesPersisted(dec.getTimesPersisted() + 1);
        }
        return shouldRemove;

    }

    private void updateAndCloseData(TimeSpanEventContainer dec, String nextKey) {
        updateData(dec, nextKey, "C");
    }

    private void insertData(TimeSpanEventContainer tsec, String nextKey) {
        Connection con = null;
        try {
            con = getConnection();
            PreparedStatement pstmt = con.prepareStatement(SQL_INSERT_STATEMENT);

            DateTime d = null;
            try {
                //d = sdf.parse( nextKey );
                DateTime dt = sdf.parseDateTime(nextKey);
                d = dt;

            } catch (IllegalArgumentException pe) {
                d = new DateTime();
            }

            //pstmt.setString(1, nextKey                        );
            pstmt.setDate(1, new java.sql.Date(d.getMillis()));
            pstmt.setInt(2, tsec.getThirtySecondLoads());
            pstmt.setInt(3, tsec.getTwentySecondLoads());
            pstmt.setInt(4, tsec.getFifteenSecondLoads());
            pstmt.setInt(5, tsec.getTenSecondLoads());
            pstmt.setInt(6, tsec.getFiveSecondLoads());
            pstmt.setInt(7, tsec.getTotalLoads());
            pstmt.setInt(8, tsec.getAverageLoadTime());
            pstmt.setInt(9, tsec.get90Percentile());
            pstmt.setInt(10, tsec.get25Percentile());
            pstmt.setInt(11, tsec.get50Percentile());
            pstmt.setInt(12, tsec.get75Percentile());
            pstmt.setInt(13, tsec.getMaxLoadTime());
            pstmt.setInt(14, tsec.getMaxLoadTimeUserID());
            pstmt.setInt(15, tsec.getMaxLoadTimePageID());
            pstmt.setInt(16, tsec.getDistinctUsers());
            pstmt.setInt(17, tsec.getErrorPages());

            pstmt.execute();
            pstmt.close();
        } catch (SQLException se) {
            // TODO Logger
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
                    con.close();
                } catch (Throwable t) {
                    //TODO Logger
                    logger.error("Error Closing Connection ... ", t);
                }
            }
        }
    }


    private void updateData(TimeSpanEventContainer tsec, String nextKey, String state) {
        Connection con = null;
        try {
            con = getConnection();
            PreparedStatement pstmt = con.prepareStatement(SQL_UPDATE_STATEMENT);
            pstmt.setInt(1, tsec.getThirtySecondLoads());
            pstmt.setInt(2, tsec.getTwentySecondLoads());
            pstmt.setInt(3, tsec.getFifteenSecondLoads());
            pstmt.setInt(4, tsec.getTenSecondLoads());
            pstmt.setInt(5, tsec.getFiveSecondLoads());
            pstmt.setInt(6, tsec.getTotalLoads());
            pstmt.setInt(7, tsec.getAverageLoadTime());
            pstmt.setInt(8, tsec.get90Percentile());
            pstmt.setInt(9, tsec.get25Percentile());
            pstmt.setInt(10, tsec.get50Percentile());
            pstmt.setInt(11, tsec.get75Percentile());
            pstmt.setInt(12, tsec.getMaxLoadTime());
            pstmt.setInt(13, tsec.getMaxLoadTimeUserID());
            pstmt.setInt(14, tsec.getMaxLoadTimePageID());
            pstmt.setInt(15, tsec.getDistinctUsers());
            pstmt.setInt(16, tsec.getErrorPages());
            pstmt.setString(17, state);
            Date d = null;
            try {
                DateTime dt = sdf.parseDateTime(nextKey);
                d = dt.toDate();

            } catch (IllegalArgumentException pe) {
                logger.warn("bad date defaulting the value:");
                d = new Date();
            }
            pstmt.setDate(18, new java.sql.Date(d.getTime()));

            pstmt.execute();
            pstmt.close();
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
                    con.close();
                } catch (Throwable t) {
                    //TODO Logger
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

