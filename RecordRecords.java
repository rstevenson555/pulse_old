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


import logParser.Tools.*;


/**
 *
 * @author  i0360d3
 * @version 
 */
public class RecordRecords extends java.lang.Object {
    public static final boolean debug1 = false;
    public static final boolean debug2 = true;
    public static final boolean type4Driver = true;
    private static String driverName = null;
    private static String connectionURL = null;

    private static PrintWriter pw = null;
    private static PrintWriter LoadLog = null;
    public static timingClass tc =new timingClass();

    
    static{
        try{
            LoadLog = getPrintWriter("LoadLog","log");
            pw = getPrintWriter();
        }catch(IOException ioe){
            ioe.printStackTrace();
        }
        
        
        if(LPConstants.Driver.equalsIgnoreCase("MySQL_Type4")){
	    connectionURL = "jdbc:mysql://localhost.localdomain:3306/NasAccess";
	    driverName = "org.gjt.mm.mysql.Driver";
        }else if(LPConstants.Driver.equalsIgnoreCase("MySQL_ODBC")){
            driverName = "sun.jdbc.odbc.JdbcOdbcDriver";
            connectionURL = "jdbc:odbc:NasAccess";
        }else if(LPConstants.Driver.equalsIgnoreCase("Oracle_Linux")){
            connectionURL = "jdbc:oracle:thin:Boise/boise@localhost.localdomain:1521:Dimok";
        }else if(LPConstants.Driver.equalsIgnoreCase("Oracle_Boise")){
            driverName="oracle.jdbc.driver.OracleDriver";
            //connectionURL="jdbc:oracle:thin:I97_USER/horton@10.7.209.73:5792:ioe";
            connectionURL="jdbc:oracle:thin:art_user/stream1@10.7.209.73:5792:artp";
        }
    }
    
    /** Creates new RecordRecords */
    public RecordRecords() {
    }

    
    
    public static Connection getConnection() throws SQLException{
        Connection con = null;
        DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
        con = DriverManager.getConnection(connectionURL);
        con.setAutoCommit(true);
        return con;

    }
    
    /**
    * @param args the command line arguments
    */
    public static void main (String args[]) throws SQLException  {
        Connection con=null;
        String nextLine=null;
        jspErrorObject jeoObj = null;
        //System.out.println("Location 1 in main");
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
        //System.out.println("Location 2 in main");

        EasyReader erin = new EasyReader(System.in);
        //String sfile = erin.stringQuery("Enter the file Name -->");
        int totalRecords = 0;
        //        System.out.println("Location 3 in main");

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
        
        EasyReader er;// = new EasyReader(sfile);
        File f = null;
        try{
            if(LPConstants.Driver.equalsIgnoreCase("MySQL_Type4")){
                con = DriverManager.getConnection(connectionURL,"root","");
            }else if(LPConstants.Driver.equalsIgnoreCase("MySQL_ODBC")){
                con = DriverManager.getConnection("jdbc:odbc:NasAccess");
            }else if(LPConstants.Driver.equalsIgnoreCase("Oracle_Linux")){
                con = DriverManager.getConnection(connectionURL);
                con.setAutoCommit(true);
            }else if(LPConstants.Driver.equalsIgnoreCase("Oracle_Boise")){
                con = DriverManager.getConnection(connectionURL);
                con.setAutoCommit(true);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        //System.out.println("Location 4 in main");

        if(args != null && args.length==1 && args[0].equalsIgnoreCase("XmlFile")){
            File[] filearray = FileTool.getOrderpointXMLFiles(".");
            //LinkedList UpTimesLL = FileTool.getUpTimes(filearray);
          //          System.out.println("Location 5 in main");

            for (int jj = 0; jj< filearray.length; ++jj){
            //    System.out.println("Location 6 in main");
                //int linesInDatabase = ConnectionT.getLinesInDatabase(filearray[jj].toString(),con);
              //  System.out.println("Location 7 in main");
                //er = new EasyReader(filearray[jj].toString());
               // System.out.println("Location 8 in main");
                //egressToCurrentLocation(er,linesInDatabase);
               //Record should return the total number of lines contained in the file.
                //  if the file already was processed then it should just return 0.
                int linesInserted = Record(filearray[jj],con);
                try{ 
                    //er.close();
                    //ConnectionT.PopulateUptimes((java.util.Date[])UpTimesLL.removeFirst(),filearray[jj].toString(),linesInserted+linesInDatabase,con);
    //            }catch (IOException ioe){
    //                ioe.printStackTrace();
    //            }catch (SQLException se){
    //                se.printStackTrace();
    //            }catch (RecordRecordsException rre){
    //                rre.printStackTrace();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }else if(args != null && args.length==1 && args[0].equalsIgnoreCase("FlatFile")){
            File[] filearray = FileTool.getFiles(".");
            LinkedList UpTimesLL = FileTool.getUpTimes(filearray);
                 //   System.out.println("Location 5 in main");

            for (int jj = 0; jj< filearray.length; ++jj){
                //System.out.println("Location 6 in main");
                int linesInDatabase = ConnectionT.getLinesInDatabase(filearray[jj].toString(),con);
                //System.out.println("Location 7 in main");
                er = new EasyReader(filearray[jj].toString());
                //System.out.println("Location 8 in main");
                egressToCurrentLocation(er,linesInDatabase);
               //Record should return the total number of lines contained in the file.
                //  if the file already was processed then it should just return 0.
                int linesInserted = Record(er,con);
                try{ 
                    er.close();
                    ConnectionT.PopulateUptimes((java.util.Date[])UpTimesLL.removeFirst(),filearray[jj].toString(),linesInserted+linesInDatabase,con);
                }catch (IOException ioe){
                    ioe.printStackTrace();
                }catch (SQLException se){
                    se.printStackTrace();
                }catch (RecordRecordsException rre){
                    rre.printStackTrace();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
                    
            FileTool.addToArchive(filearray,new File("."));
            FileTool.deleteFiles(filearray);
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
        static PrintWriter getPrintWriter(String dir,String pre,String post) throws IOException {
        java.util.Date d = new java.util.Date(System.currentTimeMillis());
        String fname = dir+pre+LPConstants.SimpleFileNameFormat.format(d) + post;
        FileOutputStream fsoError = new FileOutputStream(fname);
        OutputStreamWriter oswError = new OutputStreamWriter(fsoError);
        PrintWriter pwError = new PrintWriter(oswError);
        return pwError;
    }

  
        private static void egressToCurrentLocation(EasyReader er,int linesInDatabase){
            for(int i =0;i<linesInDatabase;++i){
                if(!er.isEOF()){
                    er.stringInputLine();
                   System.out.println("I am egressing");
                    
                }
            }
        }

    
    
    static int Record(EasyReader er, Connection con){
        String nextLine=null;
        jspErrorObject jeoObj = null;
        
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
        
        
        
        
        String[] ForeignKeys;
        PrintWriter pw = null;
        PrintWriter LoadLog = null;
        try{
            LoadLog = getPrintWriter("LoadLog","log");
            pw = getPrintWriter();
        }catch(IOException ioe){
            ioe.printStackTrace();
        }
        try{
            
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
                    //System.out.println("getting the foriegn Keys");
                    ForeignKeys = ConnectionT.getForiegnKeys2(new jspErrorObject(nextLine),con);
                    //System.out.println("Returning from the foreign Keys");

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
                     ;//  System.out.println("Adding the Record");//                    System.out.println("Record Added");
                    else
                        System.out.println("Error Adding Record");
                    if(++totalRecords%10000 == 0){
                        endTime = System.currentTimeMillis();
                        LoadLog.println("" + totalRecords + " Total Records added in " + 
                                    ( endTime -startTime)/1000 + " seconds " + 
                                    (endTime-startTime)/(totalRecords/100) +" millis per 100 Records");
                        LoadLog.println("                   File Read Time Per 100 Records (millis): "
                                           + ReadTime / (totalRecords/100));
                        LoadLog.println("          Foreign Key Lookup Time Per 100 Records (millis): "
                                           + FKTime / (totalRecords/100));
                        LoadLog.println("               Time in the database for FK Lookup (millis): "
                                           +  ConnectionT.fkTimer/(totalRecords/100));
                        LoadLog.println("               Record Update Time Per 100 Records (millis): "
                                           + UpdateTime / (totalRecords/100));
                        LoadLog.println("                JEO Record Create Per 100 Records (millis): "
                                           + JEOTime / (totalRecords/100));
                        LoadLog.println("                       Dummy Call tot All Records (millis): "
                                           + dummyTime  );
                        LoadLog.flush();
                        //startTime=endTime
                        if(totalRecords%50000 == 0){
                         //Time to clse a connection, and start a new one.
                            con.close();
                            try{
                                if(LPConstants.Driver.equalsIgnoreCase("MySQL_Type4")){
                                    con = DriverManager.getConnection(connectionURL,"root","");
                                }else if(LPConstants.Driver.equalsIgnoreCase("MySQL_ODBC")){
                                    con = DriverManager.getConnection("jdbc:odbc:NasAccess");
                                }else if(LPConstants.Driver.equalsIgnoreCase("Oracle_Linux")){
                                    con = DriverManager.getConnection(connectionURL);
                                    con.setAutoCommit(true);
                                }else if(LPConstants.Driver.equalsIgnoreCase("Oracle_Boise")){
                                    con = DriverManager.getConnection(connectionURL);
                                    con.setAutoCommit(true);
                                }
                            }catch (Exception e){
                                e.printStackTrace();
                            }

                             System.gc();
                        }
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
        return totalRecords;
        
    }

    /*
     *This is being commented out because of some reworking, once the reworked 
     code is validated, it can be removed.
    static int Record(File f, Connection con){
        System.out.println("Processing File: " + f.toString());
        String nextLine=null;
        jspErrorObject jeoObj = null;
        boolean hasForeignKeys=true;
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
        
        
        
        
        String[] ForeignKeys = null;
        
        
        xmlOracleParser.parseXML(f);
        try{
            
            while(xmlOracleParser.jeoObjects.size()>0){
                 startReadTime = System.currentTimeMillis();
                 ReadTime += System.currentTimeMillis() - startReadTime;

                 startFKTime = System.currentTimeMillis();
                try{
                    startJEOTime = System.currentTimeMillis();
                    jeoObj = (jspErrorObject)xmlOracleParser.jeoObjects.pop();
                    if(xmlOracleParser.jeoObjects.size()%1000==0)
                    System.out.println("Removing the top record" + xmlOracleParser.jeoObjects.size());
                    if(!jeoObj.isValid()){
                        //A poorly formed log entry has been encountered.
                        //record to the log file and exit.
                        continue;
                    }
                    JEOTime += System.currentTimeMillis() - startJEOTime; 
                    try{
                        ForeignKeys = ConnectionT.getForiegnKeys2(jeoObj,con);
                    }catch (NullPointerException ne){
                        hasForeignKeys=false;
                        pw.println(jeoObj.getOracleCSVRecord());
                    }

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
                    if(hasForeignKeys){
                        if(ConnectionT.addRecord(ForeignKeys,con))
                         ;// Do Nothing
                        else
                            System.out.println("Error Adding Record");
                    }else{
                        hasForeignKeys=true;
                    }
                    if(++totalRecords%10000 == 0  && totalRecords > 0){
                        endTime = System.currentTimeMillis();
                        LoadLog.println("" + totalRecords + " Total Records added in " + 
                                    ( endTime -startTime)/1000 + " seconds " + 
                                    (endTime-startTime)/(totalRecords/100) +" millis per 100 Records");
                        LoadLog.println("                   File Read Time Per 100 Records (millis): "
                                           + ReadTime / (totalRecords/100));
                        LoadLog.println("          Foreign Key Lookup Time Per 100 Records (millis): "
                                           + FKTime / (totalRecords/100));
                        LoadLog.println("               Time in the database for FK Lookup (millis): "
                                           +  ConnectionT.fkTimer/(totalRecords/100));
                        LoadLog.println("               Record Update Time Per 100 Records (millis): "
                                           + UpdateTime / (totalRecords/100));
                        LoadLog.println("                JEO Record Create Per 100 Records (millis): "
                                           + JEOTime / (totalRecords/100));
                        LoadLog.println("                       Dummy Call tot All Records (millis): "
                                           + dummyTime  );
                        LoadLog.flush();
                        if(totalRecords%50000 == 0){
                         //Time to clse a connection, and start a new one.
                            con.close();
                            try{
                                if(LPConstants.Driver.equalsIgnoreCase("MySQL_Type4")){
                                    con = DriverManager.getConnection(connectionURL,"root","");
                                }else if(LPConstants.Driver.equalsIgnoreCase("MySQL_ODBC")){
                                    con = DriverManager.getConnection("jdbc:odbc:NasAccess");
                                }else if(LPConstants.Driver.equalsIgnoreCase("Oracle_Linux")){
                                    con = DriverManager.getConnection(connectionURL);
                                    con.setAutoCommit(true);
                                }else if(LPConstants.Driver.equalsIgnoreCase("Oracle_Boise")){
                                    con = DriverManager.getConnection(connectionURL);
                                    con.setAutoCommit(true);
                                }
                            }catch (Exception e){
                                e.printStackTrace();
                            }

                             System.gc();
                        }
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
        try{
        LoadLog.println(f.toString() + " Finished, with the following Stats.");
        LoadLog.println("" + totalRecords + " Total Records added in " + 
                    ( endTime -startTime)/1000 + " seconds " + 
                    (endTime-startTime)/(totalRecords/100) +" millis per 100 Records");
        LoadLog.println("                   File Read Time Per 100 Records (millis): "
                           + ReadTime / (totalRecords/100));
        LoadLog.println("          Foreign Key Lookup Time Per 100 Records (millis): "
                           + FKTime / (totalRecords/100));
        LoadLog.println("               Time in the database for FK Lookup (millis): "
                           +  ConnectionT.fkTimer/(totalRecords/100));
        LoadLog.println("               Record Update Time Per 100 Records (millis): "
                           + UpdateTime / (totalRecords/100));
        LoadLog.println("                JEO Record Create Per 100 Records (millis): "
                           + JEOTime / (totalRecords/100));
        LoadLog.println("                       Dummy Call tot All Records (millis): "
                           + dummyTime  );
        LoadLog.flush();
        }catch (ArithmeticException ae){
            System.out.println("Arithmetic Exception at line 539. I will attempt to continue processing Data.........");
        }
        return totalRecords;
    }
    */
    
    /**  This processes all the records in the file f, and put them into the database.
     *   @param File f, is the name of the xmlFile which represents Logging data.
     *   @param Connection con  is not used in here, but it should be a connection to a 
     *          database which you want to use.  In the future it may be used, or it may not.
     *   <dt>Postcondition: <dd>  Following the running of this method, file f will have been read,
     *    All the JspErrorObjects which were extracted from that xml file will be populated into 
     *    the database if they were infact valid.
     */
    static int Record(File f, Connection con){
        System.out.println("Processing File: " + f.toString());

        tc.startTime = System.currentTimeMillis();
        try{
            xmlOracleParser.parseXML(f);
            unloadJEOStack(xmlOracleParser.jeoObjects);
        }catch(SQLException se){
            System.out.println("SQLException");
        }catch(RecordRecordsException rre){
            System.out.println("RecordRecordsException");
        }
        tc.endTime=System.currentTimeMillis();
        return tc.totalRecords;
    }
    
    
    /**  This method is used to unload a stack of JEO Objects.
     *   @param Stack jeoStack is a stack of JspErrorObjects.  This could have been build
     *   from anny basic location, however presumably they came from a flatfile, or an xmlParser
     *   because at the time of writing those were  the only way to build these objects.
     *   <dt>Precondition: <dd>  It is assumed that the stack contains valid JEOObjects, and that 
     *    the objects are not null.
     *   <dt>Postcondtion: <dd>  The stack which is passed in will be emptied.
     */
    public static void unloadJEOStack(Stack jeoStack) throws SQLException, RecordRecordsException{
        Connection con = getConnection();
        String[] ForeignKeys = null;
        boolean hasForeignKeys=true;
        jspErrorObject jeoObj=null;

        while(jeoStack.size()>0){
            jeoObj = (jspErrorObject)jeoStack.pop();
            if(!jeoObj.isValid()){
                //A poorly formed log entry has been encountered.
                //record to the log file and exit.
                continue;
            }
            
            tc.startFKTime = System.currentTimeMillis();
            try{
                ForeignKeys = ConnectionT.getForiegnKeys2(jeoObj,con);
            }catch (NullPointerException ne){
                hasForeignKeys=false;
                pw.println(jeoObj.getOracleCSVRecord());
            }
            tc.FKTime = tc.FKTime +(System.currentTimeMillis() - tc.startFKTime);
            tc.startUpdateTime = System.currentTimeMillis();
            if(hasForeignKeys){
                if(ConnectionT.addRecord(ForeignKeys,con))
                     ++tc.totalRecords;
                else
                    System.out.println("Error Adding Record");
            }else{
                hasForeignKeys=true;
            }
            tc.UpdateTime = tc.UpdateTime + ( System.currentTimeMillis()-tc.startUpdateTime );
        }
	try{
            displayTimingData();
	}catch(ArithmeticException ae){
	    System.out.println("ArithmeticException thrown in displayTimingData");
	}
        con.commit();
        con.close();
        con=null;
    }
    
    /**  This method is used to display the timing data.
     *   Specifically, This method reads the static class tc which is implemented as
     *   a public static inner class.  The values of the public static inner class tc
     *   are updated by other classes which have some input into the total time  mechanisms.
     *   <dt>Postcondition: <dd> the file pointed to by the reference varriable LoadLog, will 
     *   be updated with the latest information.
     *   <dt>Note: <dd> The information is integral in nature, because there was no
     *   conveinent way of making the specific.  That would be a good to-do item.
     */
    public static  void displayTimingData() throws ArithmeticException{
        tc.endTime = System.currentTimeMillis();
        LoadLog.println("" + tc.totalRecords + " Total Records added in " + 
                    ( tc.endTime -tc.startTime)/1000 + " seconds " + 
                    (tc.endTime-tc.startTime)/(tc.totalRecords/100) +" millis per 100 Records");
        LoadLog.println("                     Parsing Time Per 100 Records (millis): "
                           + tc.ReadTime / (tc.totalRecords/100));
        LoadLog.println("          Foreign Key Lookup Time Per 100 Records (millis): "
                           + tc.FKTime / (tc.totalRecords/100));
        LoadLog.println("               Time in the database for FK Lookup (millis): "
                           +  ConnectionT.fkTimer/(tc.totalRecords/100));
        LoadLog.println("               Record Update Time Per 100 Records (millis): "
                           + tc.UpdateTime / (tc.totalRecords/100));
        LoadLog.flush();
    }
    
    /**  This is an inner class which represents all of the timing data, and the total records loaded.
     */
    public static class timingClass{
        public int totalRecords = 0;
        public long startTime = System.currentTimeMillis();
        public long endTime = System.currentTimeMillis();
        public long startFKTime = System.currentTimeMillis();
        public long FKTime = 0;
        public long startUpdateTime = System.currentTimeMillis();
        public long UpdateTime = 0;
        public long startReadTime = System.currentTimeMillis();
        public long ReadTime =0;
        public long startJEOTime = System.currentTimeMillis();
        public long JEOTime = 0;
        public long startdummyTime = System.currentTimeMillis();
        public long dummyTime = 0;
        public long startParsingTime = System.currentTimeMillis();
        public long ParsingTime =0;
    }
}

