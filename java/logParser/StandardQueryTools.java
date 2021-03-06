/*
 * StandardQueryTools.java
 *
 * Created on May 3, 2001, 3:14 PM
 */

package logParser;
import java.util.*;
import java.text.*;
import java.io.*;
import java.sql.*;

/**
 *
 * @author  Bryce L. Alcock
 * @version 
 */
public class StandardQueryTools extends java.lang.Object {

    /** Creates new StandardQueryTools */
    public StandardQueryTools() {
    }

    /**
    * @param args the command line arguments
    */ 
    public static void main (String args[]) {
        System.out.println("Starting StandardQueryTools");
        if(args != null && args.length>0){
            for(int i =0;i<args.length;++i){
                try{
                    System.out.println("Location 1");
                    ConnectionPoolT cpt = new ConnectionPoolT();
                    System.out.println("Location 2");
                    Connection con = cpt.getConnection();
                    con.setAutoCommit(true);
                    System.out.println("Location 3");
                    StandardQueryTools sqt = new StandardQueryTools();
                    sqt.UpdateStandardQueriesToDB(con);
                    sqt.UpdateQuarterHourlyHistoricalRecords(args[i],con);
                    System.out.println("Location 4");
                    sqt.UpdateDailyLoadTimes(args[i], con);
                    sqt.UpdateHourlyHistoricalRecords(args[i],con);
                    System.out.println("Location 5");
                }catch (SQLException se){
                    se.printStackTrace();
                }catch (ClassNotFoundException cnfe){
                    cnfe.printStackTrace();
                }
            }
        }else{
           // Do it for yesterday.
            try{
                java.util.Date date = new java.util.Date(System.currentTimeMillis());
                GregorianCalendar localCal = new GregorianCalendar();
                localCal.setTime(date);
                localCal.add(GregorianCalendar.DAY_OF_MONTH,-1);
                date=localCal.getTime();
//                SimpleDateFormat sdf = new SimpleDateFormat();
                
                System.out.println(LPConstants.yyyyMMddFormat.format(date));
                ConnectionPoolT cpt = new ConnectionPoolT();
                Connection con = cpt.getConnection();
                con.setAutoCommit(true);
                StandardQueryTools sqt = new StandardQueryTools();
                sqt.UpdateStandardQueriesToDB(con);
                sqt.UpdateQuarterHourlyHistoricalRecords(LPConstants.yyyyMMddFormat.format(date),con);
                sqt.UpdateDailyLoadTimes(LPConstants.yyyyMMddFormat.format(date), con);
                sqt.UpdateHourlyHistoricalRecords(LPConstants.yyyyMMddFormat.format(date),con);
                sqt.UpdateDailyLoadTimesSummaryData(LPConstants.yyyyMMddFormat.format(date),con);
            }catch (SQLException se){
                se.printStackTrace();
            }catch (ClassNotFoundException cnfe){
                cnfe.printStackTrace();
            }
            
            
        }
        
    } 
     
    
    
    void UpdateStandardQueriesToDB(Connection con)throws SQLException{
        String[] SQNames = new String[2];
        String[] SQueries = new String[2];
        SQNames[0] = "QuarterHourlySession";
        SQueries[0] = LPConstants.MySQL_CreateQuarterHourlyResultSet;
        SQNames[1] = "HourlySessions";
        SQueries[1] = LPConstants.ORACLE_CreateHourlyHistoricalResultSet;
        
        String[] SQIDs = new String[SQNames.length];
        for(int i=0;i<SQNames.length;++i){
            SQIDs[i] = getQueryPK(SQNames[i],con);
        }
        for(int i=0;i<SQNames.length;++i){
            if(SQIDs[i] != null){
                PreparedStatement psmt = con.prepareStatement(LPConstants.updateQueries);
                psmt.setString(1,SQueries[i]);
                psmt.setInt(2,1);
                psmt.setInt(3,Integer.parseInt(SQIDs[i]));
                int rc = psmt.executeUpdate();
                psmt.close();
            }else{
                PreparedStatement psmt = con.prepareStatement(LPConstants.ORACLE_addQueries);
                psmt.setString(1,SQueries[i]);
                psmt.setInt(2,1);
                psmt.setString(3,SQNames[i]);
            System.out.println(" updatestandard... Location 15d");

            psmt.executeUpdate();
//          int rc = psmt.executeUpdate();
            psmt.close();
            }                
        }
    }
    
    String getQueryPK(String name,Connection con) throws SQLException{
        PreparedStatement psmt = con.prepareStatement("SELECT Query_ID from Queries WHERE QueryName=?");
        psmt.setString(1,name.trim());
        ResultSet rs = psmt.executeQuery();
        String sQuery_ID = null;
        while(rs.next()){
            sQuery_ID = rs.getString("Query_ID");
        }
            rs.close();
            psmt.close();
        return sQuery_ID;
    }
    
   /**
     *This method runs the queries to update the QuarterHourly Standard Queries
     *@param yyyymmdd is the day for which you which to generate the Quarter Hourly Queries.
     *@param con This is the database connection.
     *@return Boolean if it was successful
     *<dt><b>Notes</b><dd>  This method will look through each of the unique key sets 
     *to confirm that the data is not in the database.  If the set of data is in 
     *the database, then an update will be performed instead of an insert.
     */
    boolean UpdateQuarterHourlyHistoricalRecords(String yyyymmdd,  Connection con)throws SQLException {
        // ht   -    Used to create the QuarterHourlyResultSet.
        // ht2  -    Used to proform the insert of the update.
        // ht3  -    Used to get the QueryID.
        // ht4  -    To get the HR_ID if it exists.
        
        Hashtable ht = new Hashtable();
        ht.put(new Integer(1),yyyymmdd);
        Hashtable ht3 = new Hashtable();
        ht3.put(new Integer(1), "QuarterHourlySession");
        
        ResultSet rs2 = QueryService.executeRSQuery(LPConstants.ORACLE_GetQueryPK, ht3 , con);
        int qid=1;
        while(rs2.next()){
            System.out.println("QID " + rs2.getInt("qid"));
            qid = rs2.getInt("qid");
        }
        // }else{
        //   System.out.println("Did not find HourlySessions");
        // throw new Exception();
        //}
        rs2.close();
        System.out.println("Before CreateQuarterHourlyResultSet");
        ResultSet rs = QueryService.executeRSQuery(LPConstants.ORACLE_CreateQuarterHourlyResultSet, new Hashtable()/*ht*/, con);
        System.out.println("After CreateQuarterHourlyResultSet");
        while(rs.next()){ 
            //  First we must get a Primary key if it exists.
            //  Then if the PK exists we perform an update.
            //  Else we perform an insert.
            //  Unique Keys for the HourlySession data are:
            //  Machine_ID
            //  Query_ID
            //  Time:   -- formateed as yyyymmddHH24
            
            Hashtable ht4 = new Hashtable();
            ht4.put(new Integer(1), new Integer(rs.getInt("Machine")));
            
            String tp=null;  
            try{ 
                tp =LPConstants.QuarterHourlyDBFormat.format(
                         LPConstants.QuarterHourlyQueryFormat.parse(
                                  rs.getString("timePeriod")
                                  )
                          );
            }catch (ParseException pe){
                pe.printStackTrace();
            }
            ht4.put(new Integer(2),tp);
            ht4.put(new Integer(3), new Integer(qid));
            System.out.println("Machine" + rs.getInt("Machine") + " tp " + tp + " qid " +qid);
            PreparedStatement pstmt = QueryService.getPreparedStatementQuery(LPConstants.ORACLE_getQHRPK,ht4,con);
            ResultSet HistoricalRecordsPKRS = pstmt.executeQuery();

            if(!HistoricalRecordsPKRS.next()){
                // This means we did not find the Unique key, and are therefore inserting
                HistoricalRecordsPKRS.close();
                pstmt.close();
                Hashtable ht2 = new Hashtable();
                ht2.put(new Integer(1),new Integer(rs.getInt("Machine")));
                
                System.out.println(tp+"00");

                
                try{
                //    java.sql.Timestamp dd= new java.sql.Timestamp((LPConstants.yyyyMMddHHmmssFormat.parse(rs.getString("timePeriod")+"0000")).getTime());
                //    System.out.println(dd.toString());

                    ht2.put(new Integer(2),new java.sql.Timestamp((LPConstants.yyyyMMddHHmmssFormat.parse(tp+"00")).getTime()));
                }catch (ParseException pe){
                    pe.printStackTrace(); 
                    System.out.println("Error in StandardQueryTools near line 254");
                }
                 
                ht2.put(new Integer(3),new Integer(qid));
                ht2.put(new Integer(4),new Integer(rs.getInt("distSessions")));
                ht2.put(new Integer(5),new Integer(rs.getInt("totalSessions")));

                if(QueryService.executeNRSQuery(LPConstants.ORACLE_InsertHourlyHistoricalRecords,ht2,con))
                    ;
                else
                    System.out.println("Failure adding record to Historical table from Hourly");
            }else{
                // This means we found the Unique Key, and are therefore Updating.
                String hrpk = HistoricalRecordsPKRS.getString("HR_ID");
                HistoricalRecordsPKRS.close();
                pstmt.close();
                Hashtable ht2 = new Hashtable();
                ht2.put(new Integer(1),new Integer(rs.getInt("distSessions")));
                ht2.put(new Integer(2),new Integer(rs.getInt("totalSessions")));
                ht2.put(new Integer(3), new Integer(hrpk));

                if(QueryService.executeNRSQuery(LPConstants.ORACLE_UpdateHourlyHistoricalRecords,ht2,con))
                    ;
                else
                    System.out.println("Failure adding record to Historical table from Hourly");
            }
        }
        return true;  
    }
    
    

    /**
     *This method updates the daily load times for each page on the give day,
     *it then puts this data into the DailyLoadTimes Table.
     *@param yyyymmdd this is the day for which you want to update the records.
     *@return true if all went well and the records got updated.
     */
    boolean UpdateDailyLoadTimes(String yyyymmdd, Connection con)throws SQLException {
        Hashtable ht = new Hashtable();
        ht.put(new Integer(1),yyyymmdd);
        ResultSet rs = QueryService.executeRSQuery(LPConstants.ORACLE_CreateDailyLoadTimesResultSet, ht, con);
        
        while(rs.next()){
            //  First we must get a Primary key if it exists.
            //  Then if the PK exists we perform an update.
            //  Else we perform an insert.
            //  Unique Keys for the HourlySession data are:
            //  Machine_ID
            //  Query_ID
            //  Time:   -- formateed as yyyymmddHH24
            
            Hashtable ht4 = new Hashtable();
            ht4.put(new Integer(1), new Integer(rs.getInt("machineid")));
            ht4.put(new Integer(2), new Integer(rs.getInt("pageid")));
            ht4.put(new Integer(3), rs.getString("timePeriod"));
            PreparedStatement pstmt = QueryService.getPreparedStatementQuery(LPConstants.ORACLE_getDLTPK,ht4,con);
            ResultSet dltPKRS = pstmt.executeQuery();

            
            if(!dltPKRS.next()){
                // This means we did not find the Unique key, and are therefore inserting
                dltPKRS.close();
                pstmt.close();
                Hashtable ht2 = new Hashtable();
                try{
                    ht2.put(new Integer(1),new java.sql.Date((LPConstants.yyyyMMddFormat.parse(rs.getString("timePeriod"))).getTime()));
                } catch (ParseException pe){
                    pe.printStackTrace();
                    System.out.println("Error in StandardQueryTools near line 94");
                }
                ht2.put(new Integer(2),new Integer(rs.getInt("pageid")));
                ht2.put(new Integer(3),new Integer(rs.getInt("machineid")));
                ht2.put(new Integer(4),new Integer(rs.getInt("alt")));
                ht2.put(new Integer(5),new Integer(rs.getInt("maxlt")));
                ht2.put(new Integer(6),new Integer(rs.getInt("minlt")));
                ht2.put(new Integer(7),new Integer(rs.getInt("totalhits"))); 
System.out.println("just before QueryService.executeNRSQuery(LPConstants.ORACLE_InsertDailyLoadTimeRecords)");
                if(QueryService.executeNRSQuery(LPConstants.ORACLE_InsertDailyLoadTimeRecords,ht2,con))
                    ;
                else
                    System.out.println("Failure adding record to Historical table from QuarterHourly");
System.out.println("just after QueryService.executeNRSQuery(LPConstants.ORACLE_InsertDailyLoadTimeRecords)");

            
            }else{

                // This means we found the Unique Key, and are therefore Updating.
                
                String dltpk = dltPKRS.getString("dltid");
                dltPKRS.close();
                pstmt.close();
                Hashtable ht2 = new Hashtable();
                ht2.put(new Integer(1),new Integer(rs.getInt("alt")));
                ht2.put(new Integer(2),new Integer(rs.getInt("maxlt")));
                ht2.put(new Integer(3),new Integer(rs.getInt("minlt")));
                ht2.put(new Integer(4),new Integer(rs.getInt("totalhits"))); 
                ht2.put(new Integer(5), new Integer(dltpk));

                if(QueryService.executeNRSQuery(LPConstants.ORACLE_UpdateDailyLoadTimeRecords,ht2,con))
                    ;
                else
                    System.out.println("Failure adding record to Historical table from Hourly");
            }
        }
        return true;   
    }
    

    
   /**
     *This method updates the hourlyHistoricalRecords.  The HistoricalRecords table 
     *contains Unique Key values of QUERY_ID, Time, and Machine.  This Method is 
     *responsible for Updating all the records on the day yyyymmdd, for the Query_ID
     *associated with "HourlySessions" in the Queries table.
     *@param yyyymmdd is the day for wich the records will be populated.
     *@param con is the connection to the database.
     *@return true if all went well.
     *false if something went aRye.
     */
    boolean UpdateHourlyHistoricalRecords(String yyyymmdd, Connection con)throws SQLException {
        Hashtable ht = new Hashtable();
        ht.put(new Integer(1),yyyymmdd);
        Hashtable ht3 = new Hashtable();
        ht3.put(new Integer(1), "HourlySessions");
        
        ResultSet rs2 = QueryService.executeRSQuery(LPConstants.ORACLE_GetQueryPK, ht3 , con);
        int qid=1;
        while(rs2.next()){
            System.out.println("QID " + rs2.getInt("qid"));
            qid = rs2.getInt("qid");
        }
       // }else{
         //   System.out.println("Did not find HourlySessions");
           // throw new Exception();
        //}
        rs2.close();
        
        ResultSet rs = QueryService.executeRSQuery(LPConstants.ORACLE_CreateHourlyHistoricalResultSet, ht, con);
        while(rs.next()){
            //  First we must get a Primary key if it exists.
            //  Then if the PK exists we perform an update.
            //  Else we perform an insert.
            //  Unique Keys for the HourlySession data are:
            //  Machine_ID
            //  Query_ID
            //  Time:   -- formateed as yyyymmddHH24
            
            Hashtable ht4 = new Hashtable();
            ht4.put(new Integer(1), new Integer(rs.getInt("Machine")));
                ht4.put(new Integer(2),rs.getString("timePeriod"));
           ht4.put(new Integer(3), new Integer(qid));
            PreparedStatement pstmt = QueryService.getPreparedStatementQuery(LPConstants.ORACLE_getHRPK,ht4,con);
            ResultSet HistoricalRecordsPKRS = pstmt.executeQuery() ;

            
            if(!HistoricalRecordsPKRS.next()){
                // This means we did not find the Unique key, and are therefore inserting
                HistoricalRecordsPKRS.close();
                pstmt.close();
                Hashtable ht2 = new Hashtable();
                ht2.put(new Integer(1),new Integer(rs.getInt("Machine")));
                System.out.println(rs.getString("timePeriod")+"0000");
                try{
                    java.sql.Timestamp dd= new java.sql.Timestamp((LPConstants.yyyyMMddHHmmssFormat.parse(rs.getString("timePeriod")+"0000")).getTime());
                    System.out.println(dd.toString());

                    ht2.put(new Integer(2),new java.sql.Timestamp((LPConstants.yyyyMMddHHmmssFormat.parse(rs.getString("timePeriod")+"0000")).getTime()));
                }catch (ParseException pe){
                    pe.printStackTrace(); 
                    System.out.println("Error in StandardQueryTools near line 254");
                }
                ht2.put(new Integer(3),new Integer(qid));
                ht2.put(new Integer(4),new Integer(rs.getInt("distSessions")));
                ht2.put(new Integer(5),new Integer(rs.getInt("totalSessions")));
                
System.out.println("ORACLE_InsertHourlyHistoricalRecords");
                if(QueryService.executeNRSQuery(LPConstants.ORACLE_InsertHourlyHistoricalRecords,ht2,con))
                    ;
                else
                    System.out.println("Failure adding record to Historical table from Hourly");
System.out.println("after ORACLE_InsertHourlyHistoricalRecords");
            }else{
                // This means we found the Unique Key, and are therefore Updating.
                String hrpk = HistoricalRecordsPKRS.getString("HR_ID");
                HistoricalRecordsPKRS.close();
                pstmt.close();
                Hashtable ht2 = new Hashtable();
                ht2.put(new Integer(1),new Integer(rs.getInt("distSessions")));
                ht2.put(new Integer(2),new Integer(rs.getInt("totalSessions")));
                ht2.put(new Integer(3), new Integer(hrpk));

System.out.println("ORACLE_UpdateHourlyHistoricalRecords");
                if(QueryService.executeNRSQuery(LPConstants.ORACLE_UpdateHourlyHistoricalRecords,ht2,con))
                    ;
                else
                    System.out.println("Failure adding record to Historical table from Hourly");
System.out.println("after ORACLE_UpdateInsertHourlyHistoricalRecords");
            }
        }
        return true;  
    }  

private static final String ORACLE_GET_DAILY_LOAD_TIME_SUMMARY_RS = " select page_id, sum(averageloadtime*totalloads)/sum(totalloads) as AVELT, " +
                      "max(maxloadtime) as MAXLT, " +
                      "min(minloadtime) as MINLT, " +
                      "sum(totalloads) as TOTAL " +
                      "from dailyloadtimes dlt, machines m where m.machine_id=dlt.machine_id and m.machine_type='P' " +
                      "and to_char(day,'yyyymmdd')=? " +
                      "group by page_id";

private static final String ORACLE_INSERT_DAILY_LOAD_TIME_SUMMARY = "insert into dailyloadtimes (dailyloadtimes_id,page_id,machine_id " +
                      ",averageloadtime,maxloadtime,minloadtime,totalloads,day) " +
                      "values(DAILYLOADTIMES_ID_SEQ.NEXTVAL,?,(select machine_id from machines where shortname='Summary') " +
                      ",?,?,?,?, to_date(?,'yyyymmdd') ) ";


    private void UpdateDailyLoadTimesSummaryData(String sdate, Connection con){
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try{
            pstmt = con.prepareStatement(ORACLE_GET_DAILY_LOAD_TIME_SUMMARY_RS);
            pstmt.setString(1,sdate);
            rs = pstmt.executeQuery();
            PreparedStatement pstmt2 = null;
            while(rs.next()){
                try{
                    pstmt2 = con.prepareStatement(ORACLE_INSERT_DAILY_LOAD_TIME_SUMMARY);
                    pstmt2.setInt(1,rs.getInt("page_id"));
                    pstmt2.setInt(2,rs.getInt("AVELT"));
                    pstmt2.setInt(3,rs.getInt("MAXLT"));
                    pstmt2.setInt(4,rs.getInt("MINLT"));
                    pstmt2.setInt(5,rs.getInt("TOTAL"));
                    pstmt2.setString(6,sdate);
                    pstmt2.executeUpdate();
                }catch(SQLException se){
                    continue;
                }finally{
                    pstmt2.close();
                }
            }
        }catch(SQLException se){
            return;
        }finally{
            try{
                rs.close();
                pstmt.close();
            }catch(Exception e){
            }
        }

    }
}
