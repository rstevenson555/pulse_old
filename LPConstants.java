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

    //**********************************************************************************
    //Various date formatters used throughout the project.
    //**********************************************************************************
    public static final SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss aa");
    public static final SimpleDateFormat logFileFormat = new SimpleDateFormat("MM/dd/yyyy, hh:mm:ss aa");
    public static final SimpleDateFormat MySQLTimeStampFormat = new SimpleDateFormat("yyyyMMddHHmmss");
    public static final SimpleDateFormat TimeStampFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.000000000");
    public static final SimpleDateFormat TimeStampFormat2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.0");
    public static final SimpleDateFormat FileNameFormat = new SimpleDateFormat("ddMMyyyyHHmmss");
    public static final SimpleDateFormat LogFileNameFormat = new SimpleDateFormat("MMddHHmmyyyyss");
    public static final SimpleDateFormat SimpleFileNameFormat = new SimpleDateFormat("MMdd");
    public static final SimpleDateFormat YearFormat = new SimpleDateFormat("yyyy");
    public static final SimpleDateFormat MonthFormat = new SimpleDateFormat("MM");
    public static final SimpleDateFormat QuarterHourlyDBFormat = new SimpleDateFormat("yyyyMMddHHmm");
    public static final SimpleDateFormat yyyyMMddFormat = new SimpleDateFormat("yyyyMMdd");
    public static final SimpleDateFormat yyyyMMddHHmmssFormat = new SimpleDateFormat("yyyyMMddHHmmss");
    public static final SimpleDateFormat QuarterHourlyQueryFormat = new SimpleDateFormat("yyyyMMddHH mm");
    
    
    
    
    //**********************************************************************************
    //ORACLE ----  ORACLE ----  ORACLE ----  ORACLE ----  ORACLE ----  ORACLE ----  
    //Standard Report Queries.
    //ORACLE ----  ORACLE ----  ORACLE ----  ORACLE ----  ORACLE ----  ORACLE ----  
    //**********************************************************************************
    
    
    public static final String ORACLE_GetQueryPK = "SELECT q.Query_ID as qid from QUERIES q WHERE "+
                   " q.QueryName=? ";
    
    public static final String ORACLE_getHRPK = "SELECT HR_ID from HistoricalRecords Where "+
                  "Machine_ID=? AND TO_CHAR(Time,'YYYYMMDDHH24')=? AND Query_ID=?";
    
    public static final String ORACLE_getQHRPK = "SELECT HR_ID from HistoricalRecords Where "+
                  "Machine_ID=? AND TO_CHAR(Time,'YYYYMMDDHH24mi')=? AND Query_ID=?";

    public static final String ORACLE_CreateDailyLoadTimesResultSet = "SELECT a.Page_ID as pageid, "+
                   "a.Machine_ID as machineid, "+
                   "Max(a.loadTime) as maxlt, Min(a.loadTime) as minlt, "+ 
                   "AVG(a.loadTime) as alt, count(a.loadTime) as totalhits,  "+
                   "TO_CHAR(Time,'yyyyMMdd') as timePeriod "+
                   "from  AccessRecords a "+
                   "WHERE TO_CHAR(Time,'yyyyMMdd')=? "+
                   "GROUP BY a.Page_ID, a.Machine_ID, TO_CHAR(Time,'yyyyMMdd') ORDER BY maxlt ";

    public static final String ORACLE_getDLTPK = "SELECT DailyLoadTimes_ID as dltid from DailyLoadTimes " +
                  "Where Machine_ID=? AND PAGE_ID=? AND TO_CHAR(Day,'yyyymmdd')=? ";
    
    public static final String ORACLE_InsertDailyLoadTimeRecords ="INSERT INTO DailyLoadTimes "+
                   "(DailyLoadTimes_ID, Day, Page_ID, Machine_ID, AverageLoadTime, MaxLoadTime, MinLoadTime, TotalLoads) " +
                   "VALUES (DAILYLOADTIMESSEQUENCE.NEXTVAL,?,?,?,?,?,?,?) ";

    public static final String ORACLE_UpdateDailyLoadTimeRecords = "UPDATE DailyLoadTimes SET "+
                   "AverageLoadTime=?, MaxLoadTime=?, MinLoadTime=?, TotalLoads=? WHERE DailyLoadTimes_ID=? ";
    
    public static final String ORACLE_InsertHourlyHistoricalRecords = "INSERT INTO HistoricalRecords "+
                   "(HR_ID, Machine_ID, Time, Query_ID, Distinct_Hits, Total_Hits) "+
                   "VALUES (HISTORICALRECORDSSEQUENCE.NEXTVAL,?,?,?,?,?)";

    public static final String ORACLE_UpdateHourlyHistoricalRecords = "UPDATE HistoricalRecords SET "+
                   "Distinct_Hits=?, Total_Hits=? "+
                   "WHERE HR_ID=?";
                  
    public static final String ORACLE_CreateHourlyHistoricalResultSet = "SELECT "+
                  "a.Machine_ID as Machine, TO_CHAR(Time,'YYYYMMDDHH24') as timePeriod, "+
                  " COUNT(DISTINCT a.SESSION_ID) as distSessions, "+
                  "COUNT(a.SESSION_ID) as totalSessions FROM AccessRecords a "+
                  "WHERE TO_CHAR(Time,'YYYYMMDD')=?  "+
                  "GROUP BY TO_CHAR(Time,'YYYYMMDDHH24'), a.Machine_ID "+
                  "ORDER BY TO_CHAR(Time,'YYYYMMDDHH24') ";
    
    public static final String ORACLE_CreateQuarterHourlyResultSet  = "SELECT TO_CHAR(Time,'yyyyMMDDhh24') || TO_CHAR(TO_NUMBER(TO_CHAR(Time,'mi'),'09') - MOD(TO_NUMBER(TO_CHAR(Time,'mi'),'09'),15), '09') as timePeriod, "+
  	      "count(distinct Session_ID) as distSessions, "+
  	      "count(Session_ID) as totalSessions, "+
  	      "Machine_ID as Machine "+
  	      "from accessrecords ar  "+
  	      "where  "+
  	      "TO_CHAR(Time,'yyyyMMdd')='20010430'  "+
                     "group by TO_CHAR(Time,'yyyyMMDDhh24')|| TO_CHAR(TO_NUMBER(TO_CHAR(Time,'mi'),'09') -  MOD(TO_NUMBER(TO_CHAR(Time,'mi'),'09'),15), '09'), "+
                     "Machine_ID "+
               "order by timePeriod ASC ";
               

    
    public static final String ORACLE_addQueries = "INSERT INTO Queries (Query_ID, Query, OpUser_ID, QueryName) "+
                    " VALUES (QUERIESSEQUENCE.NEXTVAL,?,?,?)";
    
    
    
    
    ////////////////////////////////////////////////////////////////////////////
    //Queries from the standard report Tables  
    ////////////////////////////////////////////////////////////////////////////
    
    public static final String ORACLE_SessionsDataM = "SELECT hr.Time, hr.Distinct_Hits, hr.Total_Hits "+
                   "From HistoricalRecords hr, Queries q, Machines m "+
                   "Where m.Machine_ID=hr.Machine_ID AND "+
                   "m.MachineName=? AND "+
                   "hr.query_ID=q.query_ID AND "+
                   "q.queryName=? AND "+
                   "TO_CHAR(Time,'yyyymmdd')=? ";
    
    public static final String ORACLE_CreateDailyLoadTimesM = "SELECT p.PageName as PageName, "+
                   "dlt.AverageLoadTime as AVELT, "+
                   "dlt.maxloadtime as maxlt, dlt.minloadtime as minlt, "+ 
                   "dlt.totalloads as TotalHits  "+
                   "from pages p, dailyloadtimes dlt, machines m "+
                   "Where m.MachineName=? AND dlt.Machine_ID=m.Machine_ID AND "+
                   " TO_CHAR(DAY,'yyyyMMdd')=? AND p.Page_ID=dlt.Page_ID order by maxlt";
    
    /*
 
                INSERT INTO Queries (Query_ID, Query, OpUser_ID, QueryName) 
                         VALUES (QUERIESSEQUENCE.NEXTVAL,'TEST',1,'Test')
     
     */
    //**********************************************************************************
    //MySQL ----  MySQL ----  MySQL ----  MySQL ----  MySQL ----  MySQL ----  MySQL ----    
    //Standard Report Queries.
    //MySQL ----  MySQL ----  MySQL ----  MySQL ----  MySQL ----  MySQL ----  MySQL ----  
    //**********************************************************************************

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
                  "DATE_FORMAT(Time,\"%Y%m%d\")=? and  "+
                  "ar.Machine_ID=ms.Machine_ID and  "+
                  "ms.MachineName=? and "+
                  "qt.QueryName='QuarterHourlySession' "+
                  "GROUP BY timePeriod order by timePeriod ASC";

    public static final String MySQL_CreatePageLoadTimeResultSet = "SELECT p.PageName, "+
                    "COUNT( ar.Page_ID) as cnt, FORMAT(AVG(ar.loadTime),0) as AVE_LT, "+
                    "SUM(ar.loadTime) as TOT_LT from AccessRecords ar, Pages p "+
                    "Where DATE_FORMAT(Time,\"%d\")='29' AND p.Page_ID=ar.Page_ID  "+
                    "GROUP BY ar.Page_ID ORDER BY TOT_LT DESC";
    
    public static final String MySQL_InsertHistoricalRecords = "INSERT INTO HistoricalRecords " +
                    "(Time,Query_ID,Distinct_Hits,Total_Hits,Machine_ID) Values (?,?,?,?,?)";
    
    
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
    
    public static final String MySQL_CreateDailyLoadTimesResultSet = "SELECT a.Page_ID as pageid, "+
                               "a.Machine_ID as machineid, "+
                               "Max(a.loadTime) as maxlt, Min(a.loadTime) as minlt, "+ 
                               "AVG(a.loadTime) as alt, count(a.loadTime) as totalhits,  "+
                               "DATE_FORMAT(Time,\"%Y%m%d\") as timePeriod "+
                               "from  AccessRecords a "+
                               "WHERE DATE_FORMAT(Time,\"%Y%m%d\")=? "+
                               "GROUP BY a.Page_ID, a.Machine_ID ORDER BY maxlt ";
    
    public static final String MySQL_InsertDailyLoadTimeRecords ="INSERT INTO DailyLoadTimes "+
           "(Day, Page_ID, Machine_ID, AverageLoadTime, MaxLoadTime, MinLoadTime, TotalLoads) " +
           "VALUES (?,?,?,?,?,?,?) ";

    
     
    
    
    public static final String MachineNameMethod = "LOCAL";// or "SYSTEM" or "LOGFILE"
    public static String MachineName = "NA";
    
    //  Be sure to set the correct driver, make a new driver if the database changes.
    public static String Driver = "Oracle_Boise"; // Or Oracle_Boise or MySQL_Type4 or MySQL_ODBC or Oracle_Linuxor Oracle_Sun_oci or Oracle_Sun_Type4
    public static String Database = "Oracle"; // Or MySQL
    /** Creates new LPConstants */
    public LPConstants() {
    }

}
