/*
 * Created on Oct 22, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package com.bos.art.logParser.statistics;

import com.bcop.arch.utility.FastDateFormat;
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
import java.util.concurrent.ConcurrentHashMap;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * @author I0360D3
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class AccessRecordsHourlyStats extends StatisticsUnit {

	private static final Logger logger = (Logger)Logger.getLogger(AccessRecordsHourlyStats.class.getName());
	private static AccessRecordsHourlyStats instance;
    private static DateTimeFormatter sdf = DateTimeFormat.forPattern("yyyyMMddHH"); 
	private static final DateTimeFormatter sdf2 = DateTimeFormat.forPattern("yyyyMMddHHmmss");

	private ConcurrentHashMap<String,TimeSpanEventContainer> hours;
	private int calls;
	private int eventsProcessed;
	private int timeSlices;
	private java.util.Date lastDataWriteTime;
	private static final int MINUTE_DELAY = 15;
	private static final int MINUTES_DATA_DELAY = 75;
	private static final int MINUTES_MOD_DELAY = 20;
	transient private PersistanceStrategy pStrat;

	
	public AccessRecordsHourlyStats(){
		hours = new ConcurrentHashMap<String,TimeSpanEventContainer>();
		lastDataWriteTime = new java.util.Date();
		pStrat = AccessRecordPersistanceStrategy.getInstance();
	}
	public static AccessRecordsHourlyStats getInstance(){
		if(instance == null){
			instance = new AccessRecordsHourlyStats();
		}
		return instance;
	}
	
	public void setInstance(StatisticsUnit su){
		if(su instanceof AccessRecordsHourlyStats){
            if (instance!=null) {
                instance.runnable=false;
            }
			instance = (AccessRecordsHourlyStats)su;
		}
	}

	
	/* (non-Javadoc)
	 * @see com.bos.art.logParser.statistics.StatisticsUnit#processRecord(com.bos.art.logParser.records.LiveLogParserRecord)
	 */
	public void processRecord(ILiveLogParserRecord record) {
		if ( record.isAccessRecord() ) {
		    ++calls;
			TimeSpanEventContainer container = getTimeSpanEventContainer(record);
			container.tally(record.getLoadTime(),record.isFirstTimeUser(), record.isErrorPage());
			++eventsProcessed;
		}
		if(calls > 0 && calls%500000 == 0){
            ++calls;
            int totalCount = 0;
            for(String nextKey : hours.keySet()) {
                TimeSpanEventContainer tsec =
                    (TimeSpanEventContainer) hours.get(nextKey);
                totalCount += tsec.getSize();
            }
            logger.warn("AccessRecordsHourlyStats : " + totalCount);
		}
		return;
	}
	
	synchronized private TimeSpanEventContainer getTimeSpanEventContainer(ILiveLogParserRecord record){
		String key = sdf.print(record.getEventTime().getTime().getTime()) + record.getServerName() + record.getInstance();
		TimeSpanEventContainer container = (TimeSpanEventContainer)hours.get(key);
		if(container == null){
			++timeSlices;
			container = new TimeSpanEventContainer(record.getServerName(),record.getAppName(),record.getContext(),record.getRemoteHost(),record.getEventTime(),record.getInstance());
			container.setModDelayMinutes(MINUTES_MOD_DELAY);
			container.setDataDelayMinutes(MINUTES_DATA_DELAY);
			hours.put(key,container);
		}
		return container;
	}
	
    @Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("\n\n\nAccessRecordsHourlyStats");

		sb.append(calls).append(":").append(eventsProcessed).append(":").append(timeSlices).append("\n");
		sb.append(hours.toString());
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
            for(String nextKey: hours.keySet()) {
                TimeSpanEventContainer tsec =
                    (TimeSpanEventContainer) hours.get(nextKey);
                if (persistData(tsec, nextKey)) {
                    hours.remove(nextKey);
                }
            }
		}
	}
	
	private static final String SQL_INSERT_STATEMENT =
		"insert into HourlyStatistics ("
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
		"update HourlyStatistics set "
			+ "TotalLoads = ?,             AverageLoadTime = ?,  "
			+ "NinetiethPercentile = ?,    TwentyFifthPercentile = ?,"
			+ "FiftiethPercentile = ?,     SeventyFifthPercentile = ?, "
			+ "MaxLoadTime = ?,            MinLoadTime = ?, "
			+ "DistinctUsers = ?,          ErrorPages = ?, "
			+ "ThirtySecondLoads = ?,      TwentySecondLoads = ?, "
			+ "FifteenSecondLoads = ?,     TenSecondLoads = ?, "
			+ "FiveSecondLoads = ?,        State = ? where "
			+ "Machine_id = ? and Time = ? ";

	private String getString(TimeSpanEventContainer tsec, String nextKey){
		StringBuilder sb = new StringBuilder();
		sb.append("\nKey: ").append(nextKey);
		sb.append("\n sql:" + SQL_INSERT_STATEMENT );
		sb.append(",\n").append(nextKey.substring(10));
		sb.append(",\n").append(nextKey.substring(0,9)).append("0000");
		sb.append(",\n").append(tsec.getTotalLoads());
        StringBuilder append = sb.append(",\n").append(tsec.getAverageLoadTime());
		sb.append(",\n").append(tsec.get90Percentile());
		sb.append(",\n").append(tsec.get25Percentile());
		sb.append(",\n").append(tsec.get50Percentile());
		sb.append(",\n").append(tsec.get75Percentile());
		sb.append(",\n").append(tsec.getMaxLoadTime());
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
	
	private static final DateTimeFormatter fdf  = DateTimeFormat.forPattern("yyyy/MM/dd HH:mm:ss");	
	
	
	private boolean persistData(TimeSpanEventContainer tsec, String nextKey){
		boolean shouldRemove = false;
		if (tsec.getTimesPersisted() == 0) {
			logger.info(
				"FirstTime Persist for getTime()--lastModTime()"
					+ fdf.print(tsec.getTime().getTime().getTime())
					+ "--"
					+ fdf.print(tsec.getLastModDate().getTime()));
			insertData(tsec, nextKey);
		} else if (shouldCloseRecord(tsec)) {
			logger.info(
				"Closing Data for getTime()--lastModTime()"
					+ fdf.print(tsec.getTime().getTime().getTime())
					+ "--"
					+ fdf.print(tsec.getLastModDate().getTime()));
			updateAndCloseData(tsec, nextKey);
			shouldRemove = true;
		} else if (tsec.isDatabaseDirty()) {
			logger.info(
				"Re-persist for getTime()--lastModTime()"
					+ fdf.print(tsec.getTime().getTime().getTime())
					+ "--"
					+ fdf.print(tsec.getLastModDate().getTime()));
			updateData(tsec, nextKey, "O");
		}
		return shouldRemove;
										
	}
	
	
	private boolean shouldCloseRecord(TimeSpanEventContainer tsec) {
		java.util.Date currentDate = new java.util.Date();
		if (currentDate.after(tsec.getCloseTimeForData().getTime())
			&& currentDate.after(tsec.getCloseTimeForMod().getTime())) {
			return true;
		}
		return false;
	}

	private static final int DATE_LENGTH = 10;
	private void insertData(TimeSpanEventContainer tsec, String nextKey) {
		Connection con = null;
		try {

			int machineID =
				ForeignKeyStore.getInstance().getForeignKey(
					tsec.getAccessRecordsForeignKeys(),
					nextKey.substring(DATE_LENGTH),
					ForeignKeyStore.FK_MACHINES_MACHINE_ID,
					pStrat);
			con = getConnection();
			PreparedStatement pstmt =
				con.prepareStatement(SQL_INSERT_STATEMENT);
			pstmt.setInt(1, machineID);
            Date d = null;
            try {
                DateTime dt = sdf2.parseDateTime(nextKey.substring(0, DATE_LENGTH) + "0000");
                d = dt.toDate();

            }
            catch(IllegalArgumentException pe)
            {
                d = new Date();
            }
            pstmt.setTimestamp(2, new java.sql.Timestamp( d.getTime() )); 
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
					logger.error("Error closing Connection..",t);
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
            Date d = null;
            try {
                DateTime dt = sdf2.parseDateTime(nextKey.substring(0, DATE_LENGTH) + "0000");
                d = dt.toDate();

            }
            catch(IllegalArgumentException pe)
            {
                d = new Date();
            }
			pstmt.setTimestamp(18, new java.sql.Timestamp( d.getTime()));
			//pstmt.setString(18, nextKey.substring(0, DATE_LENGTH) + "0000");
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
					logger.error("Error Closing Connection",t);
				}
			}
		}

	}
	private void updateAndCloseData(
		TimeSpanEventContainer tsec,
		String nextKey) {
		updateData(tsec, nextKey, "C");
	}
	
	
	Connection getConnection() throws SQLException{
		return ConnectionPoolT.getConnection();
	}
	public void flush(){
	}


}

