/*
 * Tester.java
 *
 * Created on April 26, 2001, 1:25 PM
 */

package logParser;
import java.sql.*;
import java.util.*;
import java.io.*;

 
/**
 *
 * @author  i0360d3
 * @version 
 */ 
public class Tester extends java.lang.Object {

    /** Creates new Tester */ 
    public Tester() {
    } 

    /**
    * @param args the command line arguments
    */
    public static void main (String args[]) {

        
        java.util.Date date = new java.util.Date(System.currentTimeMillis());
        GregorianCalendar localCal = new GregorianCalendar();
        localCal.setTime(date);
        localCal.add(GregorianCalendar.DAY_OF_MONTH,-1);
        date=localCal.getTime();
        System.out.println(LPConstants.yyyyMMddFormat.format(date));
        

        
        


///////////////////////////////////////////////////////////////////////////////
//  Daily Load Times Report
///////////////////////////////////////////////////////////////////////////////
        Stack sPageQuery = new Stack(); 
        Hashtable htPageData = new Hashtable();
        htPageData.put(new Integer(1),"NAS1");
        if(args != null && args.length==1)
            htPageData.put(new Integer(2),args[0]);
        else
            htPageData.put(new Integer(2),LPConstants.yyyyMMddFormat.format(date));
        sPageQuery.add(new QueryObject(LPConstants.ORACLE_CreateDailyLoadTimesM,htPageData));
        QueryMacro qmPageQuery = new QueryMacro(sPageQuery);
        System.out.println("after qmPageQuery2");
        Stack sPageQueryObject = new Stack();
        sPageQueryObject.add(qmPageQuery.getDataObject()); 
        PageCentricReport pcr = new PageCentricReport(sPageQueryObject,"PageData");
//      The Page Centric Prport pcr is ready to be added to the Stack.
////////////////////////////////////////////////////////////////////////////////
        
        
        System.out.println("after sPageQueryObject2");
        
        
///////////////////////////////////////////////////////////////////////////////
//    Hourly Report, using the Assembly Design Pattern (Much easier)
///////////////////////////////////////////////////////////////////////////////
        MachineCentricReport mcr = MachineCentricReport.createReport(
                                    LPConstants.ORACLE_SessionsDataM,
                                    true,false,false,
                                    LPConstants.yyyyMMddFormat.format(date),
                                    "HourlyNewWay",
                                    "HourlySessions");
//      The Machine Centric Prport pcr is ready to be added to the Stack.
////////////////////////////////////////////////////////////////////////////////

        
        
///////////////////////////////////////////////////////////////////////////////
//    Quarter Hourly Report, using the Assembly Design Pattern (Much easier)
///////////////////////////////////////////////////////////////////////////////
        MachineCentricReport mcr2 = MachineCentricReport.createReport(
                                    LPConstants.ORACLE_SessionsDataM,
                                    true,false,false,
                                    LPConstants.yyyyMMddFormat.format(date), 
                                    "QuarterHourlyNewWay",
                                    "QuarterHourlySession");
//      The Machine Centric Prport pcr is ready to be added to the Stack.
////////////////////////////////////////////////////////////////////////////////

        
        Stack ss = new Stack();
 
//        for(int i=16;i<20;++i){
        
        
        
///////////////////////////////////////////////////////////////////////////////
//  All pages Load Time Report  -  Page centric reports don't have the Assmbly design pattern yet.
///////////////////////////////////////////////////////////////////////////////
            Stack sPageQuery2 = new Stack(); 
            Hashtable htPageData2 = new Hashtable();
            if(args != null && args.length==1){
                htPageData2.put(new Integer(1),args[0]);
                htPageData2.put(new Integer(2),args[0]);
            }else{
                System.out.println("date format: " + LPConstants.SimpleFileNameFormat.format(date));
                htPageData2.put(new Integer(1),LPConstants.SimpleFileNameFormat.format(date));
                htPageData2.put(new Integer(2),LPConstants.SimpleFileNameFormat.format(date));
                
                
            }
            sPageQuery2.add(new QueryObject(LPConstants.ORACLE_NasPageReport,htPageData2));
            QueryMacro qmPageQuery2 = new QueryMacro(sPageQuery2);
            Stack sPageQueryObject2 = new Stack();
            sPageQueryObject2.add(qmPageQuery2.getDataObject()); 
            PageCentricReport pcr2 = new PageCentricReport(sPageQueryObject2,"AllPagesData");
            
            
            System.out.println("Query: "+ LPConstants.ORACLE_NasPageReport);
            System.out.println("Parameters: "+ (String)htPageData2.get(new Integer(1)));
            System.out.println("Parameters: "+ (String)htPageData2.get(new Integer(2)));
//      The Page Centric Prport pcr2 is ready to be added to the Stack.
////////////////////////////////////////////////////////////////////////////////

            
///////////////////////////////////////////////////////////////////////////////
//  All pages Load Time Report  -  Ordered Report.
///////////////////////////////////////////////////////////////////////////////
            Stack sPageQuery4 = new Stack(); 
            Hashtable htPageData4 = new Hashtable();
            if(args != null && args.length==1){
                htPageData4.put(new Integer(1),args[0]);
                htPageData4.put(new Integer(2),args[0]);
            }else{
                System.out.println("date format: " + LPConstants.SimpleFileNameFormat.format(date));
                htPageData4.put(new Integer(1),LPConstants.SimpleFileNameFormat.format(date));
                htPageData4.put(new Integer(2),LPConstants.SimpleFileNameFormat.format(date));
                
                
            }
            sPageQuery4.add(new QueryObject(LPConstants.ORACLE_NasPageReport,htPageData4));
            QueryMacro qmPageQuery4 = new QueryMacro(sPageQuery4);
            Stack sPageQueryObject4 = new Stack();
            sPageQueryObject4.add(qmPageQuery4.getDataObject()); 
            StringBuffer sbTitleBlock = new StringBuffer();

            sbTitleBlock.append("This report represents: Max Load Time, Min Load Time, Average Load Time, and Total Loads <BR>").
                append(" For each page, shown by machine.<BR>").
                append("The report is sorted by the Sum of the product of Average Load time and total loads for all machines.<BR>").
                append("<BR>").
                append("Ad-Hoc Report Tool (ART Report 001)<BR>").
                append("<BR>").
                append("All times are in Seconds.<BR>").
                append("<BR>").
                append("<B>"+LPConstants.HTMLHeaderFormat.format(date)+"</B>");

           OrderedReport or4 = new OrderedReport(sPageQueryObject4,"AllPagesDataOrdered",sbTitleBlock.toString());
            
            
            System.out.println("Query: "+ LPConstants.ORACLE_NasPageReport);
            System.out.println("Parameters: "+ (String)htPageData4.get(new Integer(1)));
            System.out.println("Parameters: "+ (String)htPageData4.get(new Integer(2)));
//      The Page Centric Prport pcr2 is ready to be added to the Stack.
////////////////////////////////////////////////////////////////////////////////
             
            
///////////////////////////////////////////////////////////////////////////////
//  30 second load time report   -  Page centric reports don't have the Assmbly design pattern yet.
///////////////////////////////////////////////////////////////////////////////
            Stack sPageQuery3 = new Stack(); 
            Hashtable htPageData3 = new Hashtable();
            if(args != null && args.length==1){
                System.out.println("Date: " +args[0]);
                htPageData3.put(new Integer(1),args[0]);
            }else{
                System.out.println("date format: " + LPConstants.SimpleFileNameFormat.format(date));
                htPageData3.put(new Integer(1),LPConstants.SimpleFileNameFormat.format(date));
                
                
            }
            sPageQuery3.add(new QueryObject(LPConstants.ORACLE_30SecondLoads,htPageData3));
            QueryMacro qmPageQuery3 = new QueryMacro(sPageQuery3);
            Stack sPageQueryObject3 = new Stack();
            sPageQueryObject3.add(qmPageQuery3.getDataObject()); 
            sbTitleBlock = new StringBuffer();
            sbTitleBlock.append("This report represents all pages which took longer than 30 seconds to load. <BR>").
                            append("The report is sorted by machine, page, user <BR>").
                            append("<BR>").
                            append("Ad-Hoc Report Tool (ART Report 002)<BR>").
                            append("<BR>").
                            append("All times are in seconds.<BR>").
                            append("<BR>").
                            append("<B>"+LPConstants.HTMLHeaderFormat.format(date)+"</B>");
            OrderedReport pcr3 = new OrderedReport(sPageQueryObject3,"30SecondLoad", sbTitleBlock.toString());
//      The Page Centric Prport pcr2 is ready to be added to the Stack.
////////////////////////////////////////////////////////////////////////////////


///////////////////////////////////////////////////////////////////////////////
//  30 second load time report   -  Page centric reports don't have the Assmbly design pattern yet.
///////////////////////////////////////////////////////////////////////////////
            Stack sPageQuery5 = new Stack(); 
            Hashtable htPageData5 = new Hashtable();
            if(args != null && args.length==1){
                System.out.println("Date: " +args[0]);
                htPageData5.put(new Integer(1),args[0]);
            }else{
                System.out.println("date format: " + LPConstants.SimpleFileNameFormat.format(date));
                htPageData5.put(new Integer(1),LPConstants.SimpleFileNameFormat.format(date));
                
                
            }
            sPageQuery5.add(new QueryObject(LPConstants.ORACLE_HourlyReport,htPageData5));
            QueryMacro qmPageQuery5 = new QueryMacro(sPageQuery5);
            Stack sPageQueryObject5 = new Stack();
            sPageQueryObject5.add(qmPageQuery5.getDataObject()); 
            sbTitleBlock = new StringBuffer();
            sbTitleBlock.append("This Hourly Usage report represents the number of users, and total pages served, by hour <BR>").
                            append("The report is sorted by machine, hour <BR>").
                            append("<BR>").
                            append("Ad-Hoc Report Tool (ART Report 003)<BR>").
                            append("<BR>").
                            append("Values represent number of distinct users, and total pages served up.<BR>").
                            append("<BR>").
                            append("<B>"+LPConstants.HTMLHeaderFormat.format(date)+"</B>");
            OrderedReport or5 = new OrderedReport(sPageQueryObject5,"HourlyUsage", sbTitleBlock.toString());
//      The Ordered Report or5 is ready to be added to the Stack.
////////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////
//  Machine Utilization Report.
///////////////////////////////////////////////////////////////////////////////
            Stack sPageQuery6 = new Stack(); 
            Hashtable htPageData6 = new Hashtable();
            if(args != null && args.length==1){
                System.out.println("Date: " +args[0]);
                htPageData6.put(new Integer(1),args[0]);
            }else{
                System.out.println("date format: " + LPConstants.SimpleFileNameFormat.format(date));
                htPageData6.put(new Integer(1),LPConstants.SimpleFileNameFormat.format(date));
                
                
            }
            sPageQuery6.add(new QueryObject(LPConstants.ORACLE_MachineUtilization,htPageData6));
            QueryMacro qmPageQuery6 = new QueryMacro(sPageQuery6);
            Stack sPageQueryObject6 = new Stack();
            sPageQueryObject6.add(qmPageQuery6.getDataObject()); 
            sbTitleBlock = new StringBuffer();
            sbTitleBlock.append("This report represents the Machine Utilization <BR>").
                            append("The report includes the Total Pages Loaded, Average Time Per Page, and Total CPU time.  <BR>").
                            append("<BR>").
                            append("Ad-Hoc Report Tool (ART Report 004)<BR>").
                            append("<BR>").
                            append("All time values are in MilliSeconds .<BR>").
                            append("<BR>").
                            append("<B>"+LPConstants.HTMLHeaderFormat.format(date)+"</B>");
            OrderedReport or6 = new OrderedReport(sPageQueryObject6,"MachineUtilization", sbTitleBlock.toString());
//      The Ordered Report or5 is ready to be added to the Stack.
////////////////////////////////////////////////////////////////////////////////
               
             
            //Add all of the various reports to the Stack.
            ss.add(mcr);
            ss.add(mcr2);
            ss.add(pcr);  
            ss.add(pcr2);   
            ss.add(pcr3); 
            ss.add(or4); 
            ss.add(or5);
            ss.add(or6);

            
            
            
            
            
            
            ReportDirector rd = new ReportDirector();
//         rd.BuildCSVReports(ss.elements());
           rd.BuildHTMLReports(ss.elements());
     
         localCal.add(GregorianCalendar.DAY_OF_MONTH,1);
        date=localCal.getTime();

         
      
    }   
    
     
    
    static void oldmain(){
      // Test 1  
      String sql = "SELECT * from Pages";
      Stack sss = new Stack();
      sss.add(new QueryObject(sql,new Hashtable()));
      QueryMacro qm = new QueryMacro(sss);
      ReportObject ro =qm.getReportObject();
     try{
        PrintWriter pw = RecordRecords.getPrintWriter("Test");
        ro.writeToFile(pw);
     }catch (IOException ioe){
         ioe.printStackTrace();
     }
     
     
     //Test 2
     sss = new Stack();
   /*  Hashtable ht1 = new Hashtable();
     ht1.put(new Integer(1),new Integer(22));
     sss.add(new QueryObject(LPConstants.MySQL_GetHourlyDataResultSet,new Hashtable()));
     sss.add(new QueryObject(LPConstants.MySQL_CreateHourlyDataPerDay,ht1));
     qm = new QueryMacro(sss);
     ro = qm.getReportObject();
     try{
         PrintWriter pw = RecordRecords.getPrintWriter("HourlyReport");
         ro.writeToFile(pw);
     }catch (IOException ioe){
         ioe.printStackTrace();
     }
     */
     sss.add(new QueryObject(LPConstants.MySQL_CreateQuarterHourlyResultSet,new Hashtable()));
     qm = new QueryMacro(sss);
     ro = qm.getReportObject();
     try{ 
         PrintWriter pw = RecordRecords.getPrintWriter("QHR");
         ro.writeToFile(pw); 
     }catch (IOException ioe){
         ioe.printStackTrace();
     }
        
        
     
        
        
    }

    
    
            //
     //
     //  Not so old main
     //  but old non the less
     //
     //
    static void notSoOldMain(){
        String sql = "SELECT * from Pages";
      Stack sss = new Stack();
      Stack ss = new Stack();
      Stack ts = new Stack();      
      Stack ts2 = new Stack();      
      Stack ts3 = new Stack();      
      Stack ts4 = new Stack();      
      sss.add(new QueryObject(sql,new Hashtable()));
      QueryMacro qm = new QueryMacro(sss);
      
      ReportObject ro =qm.getReportObject("Simp");
      ss.add(ro);
      
      
      Hashtable nas1ht = new Hashtable();
      Hashtable nas3ht = new Hashtable();
      Hashtable nas4ht = new Hashtable();
      nas1ht.put(new Integer(1),new String("NAS1"));
      nas3ht.put(new Integer(1),new String("NAS3"));
      nas4ht.put(new Integer(1),new String("NAS4"));
      Stack nas1s = new Stack();
      Stack nas3s = new Stack();
      Stack nas4s = new Stack();
      nas1s.add(new QueryObject(LPConstants.MySQL_CreateQuarterHourlyMachineResultSet,nas1ht));
      nas3s.add(new QueryObject(LPConstants.MySQL_CreateQuarterHourlyMachineResultSet,nas3ht));
      nas4s.add(new QueryObject(LPConstants.MySQL_CreateQuarterHourlyMachineResultSet,nas4ht));
      Stack StandardHourlyReportStack = new Stack();
      StandardHourlyReportStack.addElement((new QueryMacro(nas1s)).getDataObject());
      StandardHourlyReportStack.addElement((new QueryMacro(nas3s)).getDataObject());
      StandardHourlyReportStack.addElement((new QueryMacro(nas4s)).getDataObject());
      MachineCentricReport hourlyMachineReport = new MachineCentricReport(StandardHourlyReportStack,"QHMR");
      
      sss.add(new QueryObject(LPConstants.MySQL_CreateQuarterHourlyResultSet,new Hashtable()));
      ts.add(new QueryObject(LPConstants.MySQL_CreateQuarterHourlyResultSet,new Hashtable()));
      ts2.add(new QueryObject(LPConstants.MySQL_CreateQuarterHourlyResultSet,new Hashtable()));
      ts3.add(new QueryObject(LPConstants.MySQL_CreatePageLoadTimeResultSet,new Hashtable()));
      ts4.add(new QueryObject(LPConstants.MySQL_CreatePageLoadTimeResultSet,new Hashtable()));
      
      qm = new QueryMacro(sss);
      QueryMacro qm2 = new QueryMacro(ts);
      QueryMacro qm3 = new QueryMacro(ts2);
      QueryMacro qm4 = new QueryMacro(ts3);
      QueryMacro qm5 = new QueryMacro(ts4);
      ro = qm.getReportObject("Comp");
      ss.add(ro);
      Stack mcrStack = new Stack();
      Stack pcrStack = new Stack();
      pcrStack.addElement(qm4.getDataObject());
      pcrStack.addElement(qm5.getDataObject());
      
      mcrStack.addElement(qm2.getDataObject());
      mcrStack.addElement(qm3.getDataObject());
      PageCentricReport pcr = new PageCentricReport(pcrStack,"workpcr");
      MachineCentricReport mcr = new MachineCentricReport(mcrStack,"work"); 
      ss.add(mcr);
      ss.add(pcr);
      ss.add(hourlyMachineReport);
      ReportDirector rd = new ReportDirector();
      rd.BuildCSVReports(ss.elements());
      
    }
    static void display(ResultSet rs) throws SQLException {
        System.out.println("Displaying the result set");
        ResultSetMetaData rsmd = rs.getMetaData();
        int colcount = rsmd.getColumnCount();
        String[] colnames = new String[colcount+1];
        int[] colTypes = new int[colcount+1];
         
        System.out.println("Starting to use the metadata");
        for(int i = 1; i<=colcount;++i){
            colnames[i] = rsmd.getColumnName(i);
            colTypes[i] = rsmd.getColumnType(i);
            
            
        }
        for(int i = 1; i<=colcount;++i){
            System.out.print(" " + colnames[i]);
        }
          
        System.out.println();
        while(rs.next()){
            for(int i =1; i<=colcount; ++i){
                if(colTypes[i] > java.sql.Types.TINYINT && colTypes[i] < java.sql.Types.INTEGER){
                    System.out.print("  "+rs.getInt(i));
                }else if(colTypes[i] == java.sql.Types.VARCHAR){
                    System.out.print("  "+rs.getString(i));
                }else if(colTypes[i] == java.sql.Types.DECIMAL){
                    System.out.print("  "+rs.getDouble(i));
                }else if(colTypes[i] == java.sql.Types.DATE){
                    System.out.print("  "+rs.getDate(i));
                }else if(colTypes[i] == java.sql.Types.TIMESTAMP){
                    System.out.print("  "+rs.getTimestamp(i));
                }else {
                    System.out.print(" Unknow sql type" + colTypes[i]);
                }
                System.out.println(); 
            }
        }
        System.out.println("java.sql.Types.ARRAY :" + java.sql.Types.ARRAY );
        System.out.println("java.sql.Types.INTEGER :" + java.sql.Types.INTEGER );
        System.out.println("java.sql.Types.NUMERIC :" + java.sql.Types.NUMERIC );
        System.out.println("java.sql.Types.DATE :" + java.sql.Types.DATE );
        System.out.println("java.sql.Types.DOUBLE :" + java.sql.Types.DOUBLE );
        System.out.println("java.sql.Types.TIMESTAMP :" + java.sql.Types.TIMESTAMP );
        System.out.println("java.sql.Types.VARCHAR :" + java.sql.Types.VARCHAR );
        System.out.println("java.sql.Types.NULL :" + java.sql.Types.NULL );
        System.out.println("java.sql.Types.TINYINT :" + java.sql.Types.TINYINT );
        System.out.println("java.sql.Types.BIGINT :" + java.sql.Types.BIGINT );
        
        
    }
}
