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

        /*
        Hashtable sdht1 = new Hashtable();
        Hashtable sdht2 = new Hashtable();
        Hashtable sdhtb1 = new Hashtable();
        Hashtable sdhtb2 = new Hashtable();
        sdht1.put(new Integer(1), "NAS1");
        sdht1.put(new Integer(2), "HourlySessions");
        sdht1.put(new Integer(3), "20010430");
        sdht2.put(new Integer(1), "NAS3");
        sdht2.put(new Integer(2), "HourlySessions");
        sdht2.put(new Integer(3), "20010430");
        sdhtb1.put(new Integer(1), "NAS1");
        sdhtb1.put(new Integer(2), "QuarterHourlySession");
        sdhtb1.put(new Integer(3), "20010430");
        sdhtb2.put(new Integer(1), "NAS3");
        sdhtb2.put(new Integer(2), "QuarterHourlySession");
        sdhtb2.put(new Integer(3), "20010430");

        
        Stack sQueryObj = new Stack();
        Stack sQueryObj2 = new Stack();
        Stack sQueryObjb = new Stack();
        Stack sQueryObjb2 = new Stack();
        Stack sPageQuery = new Stack(); 
        Hashtable htPageData = new Hashtable();
        htPageData.put(new Integer(1),"NAS1");
        htPageData.put(new Integer(2),"20010430");
        
        sPageQuery.add(new QueryObject(LPConstants.ORACLE_CreateDailyLoadTimesM,htPageData));

        sQueryObj.add(new QueryObject(LPConstants.ORACLE_SessionsDataM,sdht1));
        sQueryObj2.add(new QueryObject(LPConstants.ORACLE_SessionsDataM,sdht2));
        sQueryObjb.add(new QueryObject(LPConstants.ORACLE_SessionsDataM,sdhtb1));
        sQueryObjb2.add(new QueryObject(LPConstants.ORACLE_SessionsDataM,sdhtb2));
        
        QueryMacro qmPageQuery = new QueryMacro(sPageQuery);
        QueryMacro qm = new QueryMacro(sQueryObj);
        QueryMacro qm2 = new QueryMacro(sQueryObjb);
        
        Stack sDataobject2 = new Stack();
        Stack sDataobject = new Stack();
        Stack sPageQueryObject = new Stack();
        sPageQueryObject.add(qmPageQuery.getDataObject()); 
 
        sDataobject.add(qm.getDataObject());
        sDataobject2.add(qm2.getDataObject());
        
        qm = new QueryMacro(sQueryObj2); 
        qm2 = new QueryMacro(sQueryObjb2);

        sDataobject.add(qm.getDataObject());
        sDataobject2.add(qm2.getDataObject());
        */
        
        
        Stack sPageQuery = new Stack(); 

        Stack sPageQuery2 = new Stack(); 

        
        //Making a new page centric report
        Hashtable htPageData2 = new Hashtable();
        //htPageData2.put(new Integer(1),"0713");
        //htPageData2.put(new Integer(2),"0713");
        
        
        Hashtable htPageData = new Hashtable();
        htPageData.put(new Integer(1),"NAS1");
        htPageData.put(new Integer(2),"20010713");

        
        sPageQuery.add(new QueryObject(LPConstants.ORACLE_CreateDailyLoadTimesM,htPageData));
        sPageQuery2.add(new QueryObject(LPConstants.ORACLE_NasPageReport,htPageData2));
        QueryMacro qmPageQuery = new QueryMacro(sPageQuery);
        QueryMacro qmPageQuery2 = new QueryMacro(sPageQuery2);
        System.out.println("after qmPageQuery2");
        Stack sPageQueryObject = new Stack();
        Stack sPageQueryObject2 = new Stack();
        sPageQueryObject.add(qmPageQuery.getDataObject()); 
        sPageQueryObject2.add(qmPageQuery2.getDataObject()); 
  
        System.out.println("after sPageQueryObject2");
        
        
        
        MachineCentricReport mcr = MachineCentricReport.createReport(
                                    LPConstants.ORACLE_SessionsDataM,
                                    true,false,false,
                                    "20010713",
                                    "HourlyNewWay",
                                    "HourlySessions");
        MachineCentricReport mcr2 = MachineCentricReport.createReport(
                                    LPConstants.ORACLE_SessionsDataM,
                                    true,false,false,
                                    "20010713", 
                                    "QuarterHourlyNewWay",
                                    "QuarterHourlySession");
        System.out.println("after mach2"); 
      PageCentricReport pcr = new PageCentricReport(sPageQueryObject,"PageData");
      PageCentricReport pcr2 = new PageCentricReport(sPageQueryObject2,"AllPagesData");
//        MachineCentricReport mcr = new MachineCentricReport(sDataobject,"teststtt");
        Stack ss = new Stack();

        ss.add(mcr);
       ss.add(mcr2);
        ss.add(pcr);  
        ss.add(pcr2);
         ReportDirector rd = new ReportDirector();
         rd.BuildCSVReports(ss.elements());
     
      
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
