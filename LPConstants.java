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
    public static final SimpleDateFormat LogFileNameFormat = new SimpleDateFormat("MMddHHmmyyyyss");
    public static final SimpleDateFormat SimpleFileNameFormat = new SimpleDateFormat("MMdd");
    public static final SimpleDateFormat YearFormat = new SimpleDateFormat("yyyy");
    public static final SimpleDateFormat MonthFormat = new SimpleDateFormat("MM");
    
    
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
    
    public static final String updateQueries = "UPDATE Queries SET Query=?, OpUser_ID=? "+
                        " WHERE Query_ID=? ";
    public static final String addQueries = "INSERT INTO Queries (Query, OpUser_ID, QueryName) "+
                        " VALUES (?,?,?)";
    public static final String MySQL_CreateQuarterHourlyResultSet ="SELECT "+  
                          "DATE_FORMAT(DATE_SUB(Time,INTERVAL "+
                               "(TRUNCATE(DATE_FORMAT(Time,\"%i\")%15,0)) "+
                               "MINUTE),\"%Y%m%d%H%i\") "+
                               "as timePeriod, "+
                          "qt.Query_ID as qid, "+
                          "count(distinct Session_ID) as distSessions, "+
                          "count(Session_ID) as totalSessions, "+
                          "ms.Machine_ID as Machine "+
                          "from accessrecords ar,  "+
                               "Machines ms, "+
                               "Queries qt "+
                          "where  "+
                          "DATE_FORMAT(Time,\"%d\")=? and  "+
                          "ar.Machine_ID=ms.Machine_ID and  "+
                          "ms.MachineName=? and "+
                          "qt.QueryName='QuarterHourlySession' "+
                          "GROUP BY timePeriod order by timePeriod ASC";

    public static final String MySQL_CreatePageLoadTimeResultSet = "SELECT p.PageName, "+
                        "COUNT( ar.Page_ID) as cnt, FORMAT(AVG(ar.loadTime),0) as AVE_LT, "+
                        "SUM(ar.loadTime) as TOT_LT from AccessRecords ar, Pages p "+
                        "Where DATE_FORMAT(Time,\"%d\")='29' AND p.Page_ID=ar.Page_ID  "+
                        "GROUP BY ar.Page_ID ORDER BY TOT_LT DESC";
    
    
    public static final String MySQL_CreateQuarterHourlyMachineResultSet ="SELECT CONCAT(DATE_FORMAT(Time,\"%H\"),DATE_FORMAT(DATE_ADD(Time,INTERVAL "+
                               "(15-TRUNCATE(DATE_FORMAT(Time,\"%i\")%15,0)) MINUTE),\"%i\")) "+
	                       "as timePeriod, "+ 
	                       "count(distinct Session_ID) as distSessions,  "+
	                       "TRUNCATE(DATE_FORMAT(Time,\"%i\")/15,0)*15 + DATE_FORMAT(TIME,\"%H\")*60 "+
	                       "as MinSinceMidnight,   "+
                               "FORMAT(TRUNCATE(DATE_FORMAT(Time,\"%i\")/15,0)*15,0) as yyz "+
	                       "from AccessRecords ar, Machines ms  "+
	                       "where DATE_FORMAT(Time,\"%d\")='30' and ar.Machine_ID=ms.Machine_ID and ms.MachineName=? "+ 
                               "GROUP BY MinSinceMidnight ORDER BY timePeriod";
    
    public static final String MachineNameMethod = "LOCAL";// or "SYSTEM" or "LOGFILE"
    public static String MachineName = "NA";
    
    //  Be sure to set the correct driver, make a new driver if the database changes.
    public static String Driver = "MySQL_ODBC"; // Or Oracle_Boise or MySQL_Type4 or MySQL_ODBC or Oracle_Linuxor Oracle_Sun_oci or Oracle_Sun_Type4
    public static String Database = "MySQL"; // Or MySQL
    /** Creates new LPConstants */
    public LPConstants() {
    }

}
