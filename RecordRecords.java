/*
 * RecordRecords.java
 *
 * Created on March 22, 2001, 10:18 AM
 */

package logParser;
import edu.colorado.io.EasyReader;
//import edu.colorado.io.BufferedEasyReader;
import java.util.*;
import java.text.*;
import java.io.*;
import java.sql.*;

/**
 *
 * @author  i0360d3
 * @version 
 */
public class RecordRecords extends java.lang.Object {
    public static final boolean debug1 = false;
    public static final boolean debug2 = true;
    public static final boolean type4Driver = false;

    /** Creates new RecordRecords */
    public RecordRecords() {
    }

    /**
    * @param args the command line arguments
    */
    public static void main (String args[])  {
        String driverName = null;
        String connectionURL = null;
        
        if(LPConstants.Driver.equalsIgnoreCase("MySQL_Type4")){
	    connectionURL = "jdbc:mysql://localhost.localdomain:3306/NasAccess";
	    driverName = "org.gjt.mm.mysql.Driver";
        }else if(LPConstants.Driver.equalsIgnoreCase("MySQL_ODBC")){
            driverName = "sun.jdbc.odbc.JdbcOdbcDriver";
            connectionURL = "jdbc:odbc:NasAccess";
        }else if(LPConstants.Driver.equalsIgnoreCase("Oracle_Linux")){
            connectionURL = "jdbc:oracle:thin:Boise/boise@localhost.localdomain:1521:Dimok";
        }
           
        
        
            
        Connection con=null;
        String nextLine=null;
        jspErrorObject jeoObj = null;
    	try{
            if(!LPConstants.Driver.equalsIgnoreCase("Oracle_Linux")){
                Class.forName(driverName).newInstance();
            }else{
                DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
                
            }

	    //		Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
	}catch (Exception e){
            e.printStackTrace();
	}

        EasyReader erin = new EasyReader(System.in);
        String sfile = erin.stringQuery("Enter the file Name -->");
        int totalRecords = 0;
        
        long startTime = System.currentTimeMillis();
        long endTime = System.currentTimeMillis();
        long startFKTime = System.currentTimeMillis();
        long FKTime = 0;
        long startUpdateTime = System.currentTimeMillis();
        long UpdateTime = 0;
        long startReadTime = System.currentTimeMillis();
        long ReadTime =0;
        long startJEOTime = System.currentTimeMillis();
        long JEOTime = 0;
        long startdummyTime = System.currentTimeMillis();
        long dummyTime = 0;
        String stemp;
        if(LPConstants.MachineNameMethod.equalsIgnoreCase("LOCAL")){
            EasyReader ler = new EasyReader("Machine.txt");
            String mn = ler.stringInputLine();
            LPConstants.MachineName = mn;
            try{
                ler.close();
            }catch (IOException ioe){
                ioe.printStackTrace();
            }
        }
        
        EasyReader er = new EasyReader(sfile);
        String[] ForeignKeys;
        PrintWriter pw = null;
        try{
            pw = getPrintWriter();
        }catch(IOException ioe){
            ioe.printStackTrace();
        }
        try{
        if(LPConstants.Driver.equalsIgnoreCase("MySQL_Type4")){
            con = DriverManager.getConnection(connectionURL,"root","");
        }else if(LPConstants.Driver.equalsIgnoreCase("MySQL_ODBC")){
            con = DriverManager.getConnection("jdbc:odbc:NasAccess");
        }else if(LPConstants.Driver.equalsIgnoreCase("Oracle_Linux")){
            con = DriverManager.getConnection(connectionURL);
            con.setAutoCommit(true);
        }
            
            while(!er.isEOF()){
                //System.out.println("Entering ForeignKeys");
                 startReadTime = System.currentTimeMillis();
                 nextLine = er.stringInputLine();
   //              System.out.println("Next Line: " + nextLine);
                 ReadTime += System.currentTimeMillis() - startReadTime;
                //System.out.println("Retruning From Foreign Keys");



                 startFKTime = System.currentTimeMillis();
                try{
                   //System.out.print("~");
                    startJEOTime = System.currentTimeMillis();
     //               System.out.println("getting the jeoObj");
                    jeoObj = new jspErrorObject(nextLine);
                    //System.out.println("jeoObj.isValid(): " + jeoObj.isValid());
                    if(!jeoObj.isValid()){
                        //A poorly formed log entry has been encountered.
                        //record to the log file and exit.
       //                 System.out.println("I should be performing a continue");
                        pw.println(nextLine);
                        continue;
                    }
                    JEOTime += System.currentTimeMillis() - startJEOTime; 
         //           System.out.println("getting the foriegn Keys");
                    ForeignKeys = ConnectionT.getForiegnKeys2(new jspErrorObject(nextLine),con);
           //         System.out.println("Returning from the foreign Keys");

                }catch (SQLException se){
                    System.out.println("Some SQL ERROR Getting Foreign Keys: " + nextLine);
                    throw new Exception();
                }
                 FKTime += System.currentTimeMillis() - startFKTime;

                    startdummyTime = System.currentTimeMillis();
                    ConnectionT.dummyCall();

                    dummyTime += System.currentTimeMillis() - startdummyTime; 




                 startUpdateTime = System.currentTimeMillis();
                try{
                    //System.out.print(".");
                    //stemp = erin.stringQuery("Add another Recod ->");
                    if(ConnectionT.addRecord(ForeignKeys,con))
                       ;// System.out.println("Adding the Record");//                    System.out.println("Record Added");
                    else
                        System.out.println("Error Adding Record");
                    if(++totalRecords%10000 == 0){
                        endTime = System.currentTimeMillis();
                        System.out.println("" + totalRecords + " Total Records added in " + 
                                    ( endTime -startTime)/1000 + " seconds " + 
                                    (endTime-startTime)/(totalRecords/100) +" millis per 100 Records");
                        System.out.println("                   File Read Time Per 100 Records (millis): "
                                           + ReadTime / (totalRecords/100));
                        System.out.println("          Foreign Key Lookup Time Per 100 Records (millis): "
                                           + FKTime / (totalRecords/100));
                        System.out.println("               Record Update Time Per 100 Records (millis): "
                                           + UpdateTime / (totalRecords/100));
                        System.out.println("                JEO Record Create Per 100 Records (millis): "
                                           + JEOTime / (totalRecords/100));
                        System.out.println("                       Dummy Call tot All Records (millis): "
                                           + dummyTime  );
                        
                        //startTime=endTime
                        if(totalRecords%50000 == 0)
                             System.gc();
                    }
                }catch (RecordRecordsException rre){
                    System.out.println("Error Writing Record: " + nextLine);
                }catch (SQLException se){
                    System.out.println("Some SQL ERROR ADDING A RECORD: " + nextLine);
                }
                UpdateTime += System.currentTimeMillis() - startUpdateTime;
               
            }
        }catch (SQLException sqle){
            sqle.printStackTrace();
        }catch (NullPointerException ne){
            ne.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
        }
        try{
            pw.flush();
            pw.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    
    static PrintWriter getPrintWriter() throws IOException {
        java.util.Date d = new java.util.Date(System.currentTimeMillis());
        String fname = "jeoError"+LPConstants.FileNameFormat.format(d) + ".log";
        FileOutputStream fsoError = new FileOutputStream(fname);
        OutputStreamWriter oswError = new OutputStreamWriter(fsoError);
        PrintWriter pwError = new PrintWriter(oswError);
        return pwError;
    }
    static PrintWriter getPrintWriter(String s) throws IOException {
        java.util.Date d = new java.util.Date(System.currentTimeMillis());
        String fname = s+LPConstants.FileNameFormat.format(d) + ".log";
        FileOutputStream fsoError = new FileOutputStream(fname);
        OutputStreamWriter oswError = new OutputStreamWriter(fsoError);
        PrintWriter pwError = new PrintWriter(oswError);
        return pwError;
    }
        static PrintWriter getPrintWriter(String pre,String post) throws IOException {
        java.util.Date d = new java.util.Date(System.currentTimeMillis());
        String fname = pre+LPConstants.SimpleFileNameFormat.format(d) + post;
        FileOutputStream fsoError = new FileOutputStream(fname);
        OutputStreamWriter oswError = new OutputStreamWriter(fsoError);
        PrintWriter pwError = new PrintWriter(oswError);
        return pwError;
    }


}

