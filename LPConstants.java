/*
 * LPConstants.java
 *
 * Created on April 25, 2001, 9:07 AM
 */

package logParser;
import java.text.*;
/**
 *
 * @author  i0360d3
 * @version 
 */
public class LPConstants extends java.lang.Object {
    public static final SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss aa");
    public static final SimpleDateFormat logFileFormat = new SimpleDateFormat("MM/dd/yyyy, hh:mm:ss aa");
    public static final SimpleDateFormat MySQLTimeStampFormat = new SimpleDateFormat("yyyyMMddHHmmss");
    public static final SimpleDateFormat TimeStampFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.000000000");
    public static final SimpleDateFormat FileNameFormat = new SimpleDateFormat("ddMMyyyyHHmmss");
    
    
    public static final String MySQL_CreateHourlyDataPerDay = "Create Temporary table StagingHourly as "+ 
                               "select DISTINCT Session_ID, DATE_FORMAT(Time, \"%H\") aa "+
                               "from accessrecords where DATE_FORMAT(Time,\"%d\")=?";
    
    public static final String MySQL_GetHourlyDataResultSet = "select aa, count(*) "+
                               "from StagingHourly group by aa";
    
    
    public static final String MySQL_DeleteFromHourlyStaging = "delete from StagingHourly";
    public static final String MySQL_DeleteFromQuarterStaging = "delete from StagingQuarterHourly";
    public static final String MySQL_DeleteFromMinuteStaging = "delete from StagingMinute";
    
   /* public static final String  =" SELECT " +
                        "CONCAT(DATE_FORMAT(Time,\"%H\"),TRUNCATE(DATE_FORMAT(Time,\"%ii\")/15,0)*15) as a,"+
                        "count(distinct Session_ID) as b,"+
                        "TRUNCATE(DATE_FORMAT(Time,\"%i\")/15,0)*15 + DATE_FORMAT(TIME,\"%H\")*60 "+
                        "as MinSinceMidnight,  TRUNCATE(DATE_FORMAT(Time,\"%i\")/15,0)*15 as yyz "+
                        "from accessrecords "+
                        "where DATE_FORMAT(Time,\"%d\")='23' "+
                        "GROUP BY MinSinceMidnight";
    
    */
    public static final String MySQL_CreateQuarterHourlyResultSet ="SELECT CONCAT(DATE_FORMAT(Time,\"%H\"),DATE_FORMAT(DATE_ADD(Time,INTERVAL "+
                               "(15-TRUNCATE(DATE_FORMAT(Time,\"%i\")%15,0)) MINUTE),\"%i\")) "+
	                       "as timePeriod, "+ 
	                       "count(distinct Session_ID) as distSessions,  "+
	                       "TRUNCATE(DATE_FORMAT(Time,\"%i\")/15,0)*15 + DATE_FORMAT(TIME,\"%H\")*60 "+
	                       "as MinSinceMidnight,   "+
                               "FORMAT(TRUNCATE(DATE_FORMAT(Time,\"%i\")/15,0)*15,0) as yyz "+
	                       "from accessrecords  "+
	                       "where DATE_FORMAT(Time,\"%d\")='23'  "+
                               "GROUP BY MinSinceMidnight";
    
    
    
    public static final String MachineNameMethod = "LOCAL";// or "SYSTEM" or "LOGFILE"
    public static String MachineName = "NA";
    /** Creates new LPConstants */
    public LPConstants() {
    }

}
