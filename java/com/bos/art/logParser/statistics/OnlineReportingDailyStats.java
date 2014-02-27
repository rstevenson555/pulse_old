/*
 * Created on Oct 22, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package com.bos.art.logParser.statistics;

import com.bos.art.logParser.db.AccessRecordPersistanceStrategy;
import com.bos.art.logParser.db.ConnectionPoolT;
import com.bos.art.logParser.db.PersistanceStrategy;
import com.bos.art.logParser.records.AccumulatorEventTiming;
import com.bos.art.logParser.records.ILiveLogParserRecord;
import com.bos.helper.MutableSingletonInstanceHelper;
import com.bos.helper.SingletonInstanceHelper;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author I0360D3
 *         <p/>
 *         To change the template for this generated type comment go to
 *         Window>Preferences>Java>Code Generation>Code and Comments
 */
public class OnlineReportingDailyStats extends StatisticsUnit {

    private static final Logger logger = (Logger) Logger.getLogger(OnlineReportingDailyStats.class.getName());
    private static final int MINUTE_DELAY = 15;
    private static final int MINUTES_DATA_DELAY = 75;
    private static final int MINUTES_MOD_DELAY = 20;
    private static final int WRITE_DELAY_SECONDS = 60;
    private static MutableSingletonInstanceHelper instance = new MutableSingletonInstanceHelper<OnlineReportingDailyStats>(OnlineReportingDailyStats.class) {
        @Override
        public java.lang.Object createInstance() {
            return new OnlineReportingDailyStats();
        }
    };
    private static TreeSet OnlineReportingClassificationSet = new TreeSet();

    static {
        OnlineReportingClassificationSet.add(new Integer(110));
        OnlineReportingClassificationSet.add(new Integer(111));
        OnlineReportingClassificationSet.add(new Integer(112));
        OnlineReportingClassificationSet.add(new Integer(113));
    }

    private Hashtable eventContainers;
    private int calls;
    private int eventsProcessed;
    private java.util.Date lastDataWriteTime;
    transient private PersistanceStrategy pStrat;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
    private SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMdd hh:mm:ss");


    public OnlineReportingDailyStats() {
        eventContainers = new Hashtable();
        lastDataWriteTime = new java.util.Date();
        pStrat = AccessRecordPersistanceStrategy.getInstance();
    }

    public static OnlineReportingDailyStats getInstance() {
        return (OnlineReportingDailyStats) instance.getInstance();
    }

    public void setInstance(StatisticsUnit su) {
        if (su instanceof OnlineReportingDailyStats) {
            if (instance.getInstance()!=null) {
                ((OnlineReportingDailyStats)instance.getInstance()).setRunnable(false);
            }
            instance.setInstance(su);
        }
    }


    /* (non-Javadoc)
     * @see com.bos.art.logParser.statistics.StatisticsUnit#processRecord(com.bos.art.logParser.records.LiveLogParserRecord)
     */
    public void processRecord(ILiveLogParserRecord record) {
        if (record.isAccumulatorEvent()) {
            //logger.warn("OnlineReportingDailyStats: isAccumulatorEvent() Found....");
            AccumulatorEventTiming aet = (AccumulatorEventTiming) record;
            Integer classify = new Integer(aet.getClassification());
            if (OnlineReportingClassificationSet.contains(classify)) {
                //logger.warn("OnlineReportingDailyStats: OnlineReportingEvent() Found....");
                processRecord(classify, aet.getType(), aet.getValue(), aet.getEventTime());
            }
        }
        return;
    }


    /*
     *
     */
    public void processRecord(Integer classification, String type, String value, Calendar eventTime) {
        SimpleEventContainer sec = getEventContainer(classification, type, eventTime);
        try {
            int tallyAmount = Integer.parseInt(value);
            sec.tally(tallyAmount);
            logger.warn("Tally Hit: " + sec.getTotalLoads());
        } catch (Exception e) {
            logger.error("Error tallying value in OnlineReportingDailyStats: ", e);
            sec.tally(1);
        }
    }

    private SimpleEventContainer getEventContainer(Integer classification, String type, Calendar eventTime) {
        Date d = new Date();
        try {
            d = sdf2.parse(sdf.format(eventTime.getTime()) + " 00:00:00");
        } catch (Exception e) {
            logger.error("Error formating and parsing a date", e);
        }
        java.sql.Date sqlDate = new java.sql.Date(d.getTime());
        String key = classification.toString().trim() + type.trim() + d.getTime();


        synchronized (eventContainers) {
            Object o = eventContainers.get(key);

            if (o != null && o instanceof SimpleEventContainer) {
                //   First Check Memory for a record.
                SimpleEventContainer sec = (SimpleEventContainer) o;
                return sec;
            }
            //   We will only get here if we did not find this object in memory, lets look in the database.
            Connection con = null;
            PreparedStatement pstmt = null;
            ResultSet rs = null;
            //   Second Check The Database for a record.
            try {
                con = getConnection();
                pstmt = con.prepareStatement("Select * from daily_online_report_summary where day=? and classification_id=? and username = ?");
                pstmt.setDate(1, sqlDate);
                pstmt.setInt(2, classification.intValue());
                pstmt.setString(3, type.trim());
                rs = pstmt.executeQuery();
                if (rs.next()) {
                    int recordCount = rs.getInt("reports");
                    SimpleEventContainer sec = new SimpleEventContainer(d, classification.intValue(), type, recordCount);
                    eventContainers.put(key, sec);
                    return sec;
                }
            } catch (SQLException se) {
                logger.error("Exception trying to retrive daily_online_report_summary from database", se);
            } finally {
                try {
                    if (rs != null) {
                        rs.close();
                    }
                    if (pstmt != null) {
                        pstmt.close();
                    }
                    if (con != null) {
                        con.close();
                    }
                } catch (SQLException se) {
                    logger.error("Exception trying to close the ResultSet, PreparedStatement, or Connection for daily_online_report_summary, ", se);
                }
            }
            //  If we find ourselves here, then we need to create a new record, and mark it as new so as to insert into the database.
            //   Finally create a new record. 
            SimpleEventContainer sec = new SimpleEventContainer(d, classification.intValue(), type);
            logger.warn("Tally Hit: Create new SimpleEventContainer...");
            sec.setIsNew(true);
            eventContainers.put(key, sec);
            return sec;
        }
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("\n\n\nOnlineReportingDailyStats");

        return sb.toString();
    }


    /* (non-Javadoc)
     * @see com.bos.art.logParser.statistics.StatisticsUnit#persistData()
     */
    public void persistData() {
        long nextWriteTime = lastDataWriteTime.getTime() + WRITE_DELAY_SECONDS * 1000L;
        if (System.currentTimeMillis() > nextWriteTime) {
            Iterator iter = eventContainers.keySet().iterator();
            ArrayList removals = new ArrayList();
            while (iter.hasNext()) {
                String key = (String) iter.next();
                if (key != null) {
                    SimpleEventContainer sec = (SimpleEventContainer) eventContainers.get(key);
                    if (sec.isNew() || sec.isDatabaseDirty()) {
                        if (persistData(sec)) {
                            //  If persistData returns true, then we should remove the record from the database.
                            removals.add(key);
                        }
                    }
                }
            }
            iter = removals.iterator();
            synchronized (eventContainers) {
                while (iter.hasNext()) {
                    eventContainers.remove(iter.next());
                }
            }
            lastDataWriteTime = new java.util.Date();
        }
    }

    private String getString(TimeSpanEventContainer tsec, String nextKey) {
        StringBuffer sb = new StringBuffer();
        return sb.toString();
    }

    //private static final DateTimeFormatter fdf  = DateTimeFormat.forPattern("yyyy/MM/dd HH:mm:ss");


    private boolean persistData(SimpleEventContainer sec) {
        boolean shouldRemove = false;
        if (sec.isNew()) {
            logger.info(this.getClass().getName() + "First Time Persist: " + sec.getShortDesc());
            insertData(sec);
            sec.setIsNew(false);
        } else if (shouldCloseRecord(sec)) {
            logger.info(this.getClass().getName() + "Closing Record for: " + sec.getShortDesc());
            updateAndCloseData(sec);
            shouldRemove = true;
        } else if (sec.isDatabaseDirty()) {
            logger.info(this.getClass().getName() + "Dabase Dirty Record for: " + sec.getShortDesc());
            updateData(sec);
        }
        return shouldRemove;

    }

    private void insertData(SimpleEventContainer sec) {
        Connection con = null;
        PreparedStatement pstmt = null;
        try {
            java.sql.Date sqlDate = new java.sql.Date(sec.getTime().getTimeInMillis());
            con = getConnection();
            pstmt = con.prepareStatement("insert into daily_online_report_summary (day, classification_id, username, reports, state) values (?,?,?,?,?)");
            pstmt.setDate(1, sqlDate);
            pstmt.setInt(2, sec.getClassificationID());
            pstmt.setString(3, sec.getUserName());
            pstmt.setInt(4, sec.getTotalLoads());
            pstmt.setString(5, "O");
            pstmt.execute();
        } catch (SQLException se) {
            logger.error("Error inserting new record into daily_online_report_summary", se);
        } finally {
            try {
                if (pstmt != null) {
                    pstmt.close();
                }
                if (con != null) {
                    con.close();
                }
            } catch (SQLException se) {
                logger.error("Error closing connection/pstmt after insert of new daily_online_report_summary", se);
            }
        }
    }

    private void updateData(SimpleEventContainer sec) {
        Connection con = null;
        PreparedStatement pstmt = null;
        try {
            java.sql.Date sqlDate = new java.sql.Date(sec.getTime().getTimeInMillis());
            con = getConnection();
            pstmt = con.prepareStatement("update daily_online_report_summary set reports = ? where day = ? and classification_id=? and username = ?");
            pstmt.setInt(1, sec.getTotalLoads());
            pstmt.setDate(2, sqlDate);
            pstmt.setInt(3, sec.getClassificationID());
            pstmt.setString(4, sec.getUserName());
            pstmt.execute();
        } catch (SQLException se) {
            logger.error("Error updating  existing  record into daily_online_report_summary", se);
        } finally {
            try {
                if (pstmt != null) {
                    pstmt.close();
                }
                if (con != null) {
                    con.close();
                }
            } catch (SQLException se) {
                logger.error("Error closing connection/pstmt after update of existing daily_online_report_summary", se);
            }
        }
    }

    private void updateAndCloseData(SimpleEventContainer sec) {
        Connection con = null;
        PreparedStatement pstmt = null;
        try {
            java.sql.Date sqlDate = new java.sql.Date(sec.getTime().getTimeInMillis());
            con = getConnection();
            pstmt = con.prepareStatement("update daily_online_report_summary set reports = ?, state='C' where day = ? and classification_id=? and username = ?");
            pstmt.setInt(1, sec.getTotalLoads());
            pstmt.setDate(2, sqlDate);
            pstmt.setInt(3, sec.getClassificationID());
            pstmt.setString(4, sec.getUserName());
            pstmt.execute();
        } catch (SQLException se) {
            logger.error("Error updating  existing  record into daily_online_report_summary", se);
        } finally {
            try {
                if (pstmt != null) {
                    pstmt.close();
                }
                if (con != null) {
                    con.close();
                }
            } catch (SQLException se) {
                logger.error("Error closing connection/pstmt after update of existing daily_online_report_summary", se);
            }
        }
    }


    private boolean shouldCloseRecord(SimpleEventContainer sec) {
        java.util.Date currentDate = new java.util.Date();
        if (currentDate.after(sec.getCloseTimeForData().getTime())
                && currentDate.after(sec.getCloseTimeForMod().getTime())) {
            return true;
        }
        return false;
    }

    Connection getConnection() throws SQLException {
        return ConnectionPoolT.getConnection();
    }

    public void flush() {
    }

}
