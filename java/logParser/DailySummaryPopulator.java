/*
 * UserNamePopulator.java
 *
 * Created on January 17, 2002, 3:05 PM
 */

package logParser;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.io.*;
import java.text.*;
/**
 *
 * @author  i0360d3
 * @version 
 */

public class DailySummaryPopulator {
    public static final String SQL_UPDATE_DAILY_SUMMARY = "UPDATE DAILY_SUMMARY SET "+
                        " THIRTYSECLOADS=?, TOTALLOADS=?, AVERAGELOADTIME=?, NINETIETHPERCENT=?, "+
                        " MAXLOAD=?, MAXLOAD_USER_ID=?, MAXLOAD_PAGE_ID=?, DISTINCT_USERS=?, ERROR_PAGES=? "+
                        " WHERE to_char(DAY,'yyyymmdd')=? ";
    public static final String SQL_UPDATE_DAILY_SUMMARY_BROWSER_INFO = "UPDATE DAILY_SUMMARY SET "+
                        " MSIE50=?, MSIE55=?, MSIE60=?, Gecko=?, Netscape=?, Other=? "+
                        " WHERE to_char(DAY,'yyyymmdd')=? ";
    
    
    public static final String SQL_INSERT_DAILY_SUMMARY = "INSERT INTO DAILY_SUMMARY "+
                        " (DAY, THIRTYSECLOADS, TOTALLOADS, AVERAGELOADTIME, NINETIETHPERCENT, "+
                        " MAXLOAD, MAXLOAD_USER_ID, MAXLOAD_PAGE_ID, DISTINCT_USERS, ERROR_PAGES) VALUES "+
                        " (to_date(?,'yyyymmdd'),?,?,?,?,?,?,?,?,?) ";
    public static final String SQL_GET_DAILY_SUMMARY_PK = " SELECT * from DAILY_SUMMARY where "+
                        " to_char(DAY,'yyyymmdd') = ? ";
    
    public static final String SQL_30SECOND_MONTHLY_SELECT = "select to_char(time,'yyyymmdd') as DAY, "+
                        "count(recordpk) as THIRTYSECLOADS from accessrecords a, " +
                        "machines m where m.machine_id=a.machine_id and m.machine_type='P' "+
						"and loadtime>30000 group by to_char(time,'yyyymmdd')";
    
    public static final String SQL_TOTALLOAD_MONTHLY_SELECT ="select to_char(time,'yyyymmdd') as DAY, "+
                        "count(recordpk) as TOTALLOADS, AVG(loadtime) as AVERAGELOADTIME, "+
                        "count(distinct(user_id)) as DISTINCT_USERS from accessrecords "+
                        "group by to_char(time,'yyyymmdd')";    
    
    public static final String SQL_MAXUSER_MONTHLY_SELECT = "select to_char(a.time,'yyyymmdd') as DAY, "+
                        "a.loadtime as MAXLOAD, a.user_id as MAX_USER_ID, a.page_id as MAX_PAGE_ID"+
                        " from accessrecords a "+
                        "where a.loadtime in (select max(loadtime) from accessrecords "+
                        "group by to_char(time,'yyyymmdd'))";
    
    public static final String SQL_30SECOND_DAILY_SELECT = "select to_char(time,'yyyymmdd') as DAY, "+
                        "count(recordpk) as THIRTYSECLOADS from accessrecords a, " + 
                        " machines m where m.machine_id=a.machine_id and m.machine_type='P' "+
						"and loadtime>30000 and to_char(time,'yyyymmdd')=? group by to_char(time,'yyyymmdd')";
    
    public static final String SQL_TOTALLOAD_DAILY_SELECT ="select to_char(time,'yyyymmdd') as DAY, "+
                        "count(recordpk) as TOTALLOADS, AVG(loadtime) as AVERAGELOADTIME, "+
                        "count(distinct(user_id)) as DISTINCT_USERS from accessrecords a, " + 
                        " machines m where m.machine_id=a.machine_id and m.machine_type='P' "+
                        "and to_char(time,'yyyymmdd')=? group by to_char(time,'yyyymmdd')";    
    
    public static final String SQL_MAXUSER_DAILY_SELECT = "select to_char(a.time,'yyyymmdd') as DAY, "+
                        "a.loadtime as MAXLOAD, a.user_id as MAX_USER_ID, a.page_id as MAX_PAGE_ID, u.FULL_NAME as FULL_NAME, "+
                        "u.COMPANY_NAME as COMPANY_NAME, p.PageName as Page_NAME from accessrecords a, users u, pages p "+
                        "where a.page_id=p.page_id and u.user_id=a.user_id and a.loadtime in (select max(loadtime) from accessrecords  a, " + 
                        " machines m where m.machine_id=a.machine_id and m.machine_type='P' "+
                        " and (time>= to_date(?,'YYYYMMDDHH24MI')) and " +
                        " time < to_date(?,'YYYYMMDDHH24MI')))";
    
    public static final String SQL_MAX_DAILY_LOADTIME = "select max(loadtime) from accessrecords  a, " + 
                        " machines m where m.machine_id=a.machine_id and m.machine_type='P' " + 
                        " and (time>= to_date(?,'YYYYMMDDHH24MI') and " + 
                        " time < to_date(?,'YYYYMMDDHH24MI')) ";

    public static final String SQL_MAX_DAILY_RECORDPK = "select recordpk from accessrecords where loadtime=? " ; 

    public static final String SQL_MAXUSER_DAILY_DATA = "select to_char(a.time,'yyyymmdd') as DAY, " + 
                        " a.loadtime as MAXLOAD, a.user_id as MAX_USER_ID, a.page_id as MAX_PAGE_ID, u.FULL_NAME as FULL_NAME, " + 
                        " u.COMPANY_NAME as COMPANY_NAME, p.PageName as Page_NAME from accessrecords a, users u, pages p " + 
                        " where a.page_id=p.page_id and u.user_id=a.user_id and recordpk=? ";
    
    public static final String SQL_MSIE50 = "select count(s.session_id) as TOTAL from sessions s where session_id in "+
                        "(select distinct(a.session_id) from accessrecords  a, " + 
                        " machines m where m.machine_id=a.machine_id and m.machine_type='P' "+
                        " and to_char(time,'yyyymmdd')=?)"+
                        " and s.browsertype like '%MSIE 5.0%'";

    public static final String SQL_MSIE55 = "select count(s.session_id) as TOTAL from sessions s where session_id in "+
                        "(select distinct(a.session_id) from accessrecords a, " + 
                        " machines m where m.machine_id=a.machine_id and m.machine_type='P' "+
                        " and to_char(time,'yyyymmdd')=?)"+
                        "and s.browsertype like '%MSIE 5.5%'";


    public static final String SQL_MSIE60 = "select count(s.session_id) as TOTAL from sessions s where session_id in"+
                        "(select distinct(a.session_id) from accessrecords a, " + 
                        " machines m where m.machine_id=a.machine_id and m.machine_type='P' "+
                        " and  to_char(time,'yyyymmdd')=?)"+
                        "and s.browsertype like '%MSIE 6.0%'";

    public static final String SQL_Gecko = "select count(s.session_id) as TOTAL from sessions s where session_id in"+
                        "(select distinct(a.session_id) from accessrecords a, " + 
                        " machines m where m.machine_id=a.machine_id and m.machine_type='P' "+
                        " and  to_char(time,'yyyymmdd')=?)"+
                        "and s.browsertype like '%Gecko%'";
    
    public static final String SQL_Netscape = "select count(s.session_id) as TOTAL from sessions s where session_id in"+
                        "(select distinct(a.session_id) from accessrecords a, " + 
                        " machines m where m.machine_id=a.machine_id and m.machine_type='P' "+
                        " and  to_char(time,'yyyymmdd')=?)"+
                        "and s.browsertype like '%Mozilla/4.%' and s.browsertype not like '%MSIE%'";
    
    public static final String SQL_Total_Sessions = "select count(s.session_id) as TOTAL from sessions s where session_id in "+
                        "(select distinct(a.session_id) from accessrecords a, " + 
                        " machines m where m.machine_id=a.machine_id and m.machine_type='P' "+
                        " and  to_char(time,'yyyymmdd')=?) and s.browsertype is not null";

    
    public static final String SQL_10SEC_LOAD = "select  a.RECORDPK,a.PAGE_ID,a.USER_ID,"+
                        "to_char(a.TIME,'yyyymmddhh24MIss') as TIME,a.SESSION_ID,a.MACHINE_ID,a.LOADTIME "+
                        "from accessrecords a, " + 
                        " machines m where m.machine_id=a.machine_id and m.machine_type='P' "+
                        " and loadtime>10000 and to_char(time,'yyyymmdd')=?";
    
    public static final String SQL_10SEC_LOAD_INSERT = "insert into tensecload "+
                        "(RECORDPK,PAGE_ID,USER_ID,TIME,SESSION_ID,MACHINE_ID,LOADTIME) "+
                        "values (?,?,?,to_date(?,'yyyymmddhh24MIss'),?,?,?)";
    


    /** Creates new UserNamePopulator */
    public DailySummaryPopulator() {
    }
    
    
    public static Connection getArtConnection() throws SQLException {
        String connectionURL;//
        connectionURL="jdbc:oracle:thin:art_user/stream1@10.7.209.208:1521:artp";
        Connection con = null;
        DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
        con = DriverManager.getConnection(connectionURL);
        con.setAutoCommit(true);
        return con;
     
    }
    
    
   public static Connection getIOETConnection() throws SQLException {
        String connectionURL;//
        connectionURL="jdbc:oracle:thin:i0360d3/sunlight@prod-ec-db1:5793:ioep";
        Connection con = null;
        DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
        con = DriverManager.getConnection(connectionURL);
        con.setAutoCommit(false);
        return con;
    }

    /**
    * @param args the command line arguments
    */
    public static void main (String args[]) {
        
        try{
            if(args != null && args.length>0){
                if(args[0] != null && args[0].equalsIgnoreCase("monthly")){
                    runMonthlyUpdate();
                }else if(args[0] != null && args[0].equalsIgnoreCase("TenSec")){
                    run10SecLoad();
                }else if(args[0] != null && args[0].equalsIgnoreCase("help")){
                    displayHelp();
                }else if(args[0] != null && args[0].equalsIgnoreCase("BrowserInfo")){
                    browserInfoUpdate(browserInfo(), getYesterdaysDate("yyyyMMdd"));
                }else if (args[0] != null){
                    String sdate = args[0];
                    runDailyUpdate(sdate);
                }else{
                    runDailyUpdate();
                }
            }else{
                runDailyUpdate();
            }
        }catch (SQLException se){
            se.printStackTrace();
        }
	}
	public static void runMonthlyUpdate() throws SQLException{
        System.out.println("Starting Monthly Update");
        System.out.println("getting ThritySecLoad");
		Hashtable thirtySecLoad = getThirtySecLoadHash();
        System.out.println("getting TotalLoad Data");
		Hashtable totalLoadData = getTotalLoadData();
        System.out.println("getting MaxUserData");
		Hashtable maxUserData = getMaxUserData();
        Enumeration enum2 = thirtySecLoad.keys();
        while(enum2.hasMoreElements()){
            String ckey = (String) enum2.nextElement();
            Integer thirtySecCount = (Integer)thirtySecLoad.get(ckey);
            TotalLoadDataClass tldc = (TotalLoadDataClass)totalLoadData.get(ckey);
            MaxUserDataClass mud = (MaxUserDataClass)maxUserData.get(ckey);
            String stsc = (thirtySecCount==null)?"null":thirtySecCount.toString();
            String tldcs = (tldc==null)?"null":tldc.toString();
            String smud = (mud==null)?"null":mud.toString();
            updateDailySummaryTable(ckey,    stsc,    tldc,    mud );
            System.out.println(ckey + ": " + stsc +tldcs + smud);
        }
	}

	
    public static Hashtable getMaxUserData(){
		Hashtable ht = new Hashtable();

        ResultSet rs;
        int maxTime, maxUserID, maxPageID;
        String strDay;
        Connection con;
        PreparedStatement pstmt;
        // get a Hashtable of all the usernames, and user_id (keys)
        try{
            con = getArtConnection();
            pstmt = con.prepareStatement(SQL_MAXUSER_MONTHLY_SELECT);
            rs = pstmt.executeQuery();
            if(rs != null){
                while (rs.next()) {
                    maxTime = rs.getInt("MAXLOAD");
                    maxUserID = rs.getInt("MAX_USER_ID");
					maxPageID= rs.getInt("MAX_PAGE_ID");
					strDay = rs.getString("DAY");
					ht.put(strDay, new MaxUserDataClass(maxTime, maxUserID, maxPageID));
                }
                rs.close();
                pstmt.close();
                con.close();
                con=null;
                rs=null;
                pstmt=null;
            }
        }catch (SQLException se){
                    se.printStackTrace();
                    System.out.println("Error getting Max User Data");
        }finally{

        }
		return ht;
	}

	public static Hashtable getTotalLoadData(){
		Hashtable ht = new Hashtable();

        ResultSet rs;
        int totalLoads, averageLoadTime, distinctUsers, errorPages;
        String strDay;
        Connection con;
        PreparedStatement pstmt;
        // get a Hashtable of all the usernames, and user_id (keys)
        try{
            con = getArtConnection();
            pstmt = con.prepareStatement(SQL_TOTALLOAD_MONTHLY_SELECT);
            rs = pstmt.executeQuery();
            if(rs != null){
                while (rs.next()) {
                    totalLoads = rs.getInt("TOTALLOADS");
                    averageLoadTime = rs.getInt("AVERAGELOADTIME");
					distinctUsers = rs.getInt("DISTINCT_USERS");
					//errorPages = rs.getInt("ERROR_PAGES");
					strDay = rs.getString("DAY");
					ht.put(strDay, new TotalLoadDataClass(totalLoads, averageLoadTime, 0, distinctUsers, 0));
                }
                rs.close();
                pstmt.close();
                con.close();
                con=null;
                rs=null;
                pstmt=null;
            }
        }catch (SQLException se){
                    se.printStackTrace();
                    System.out.println("Error gettting user keys");
        }finally{

        }
		return ht;
	}

    public static BrowserDataClass browserInfo(){
        return browserInfo(getYesterdaysDate("yyyyMMdd"));
    }
    public static BrowserDataClass browserInfo(String date){

        ResultSet rs;
        int MSIE50=0, MSIE55=0, MSIE60=0, Gecko=0, Netscape=0, other=0, total=0;
        Connection con;
        PreparedStatement pstmt;
        // get a Hashtable of all the usernames, and user_id (keys)
        try{
            con = getArtConnection();
            System.out.println("Getting MSIE05 Info");
            MSIE50 = getBrowserNumber(SQL_MSIE50,date,con);
            System.out.println("Getting MSIE55 Info");
            MSIE55 = getBrowserNumber(SQL_MSIE55,date,con);
            System.out.println("Getting MSIE60 Info");
            MSIE60 = getBrowserNumber(SQL_MSIE60,date,con);
            System.out.println("Getting Gecko Info");
            Gecko = getBrowserNumber(SQL_Gecko,date,con);
            //System.out.println("Getting Netscape Info");
            //Netscape = getBrowserNumber(SQL_Netscape,date,con);
            System.out.println("Getting total Info");
            total = getBrowserNumber(SQL_Total_Sessions, date,con);
            other = total - Gecko - MSIE60 - MSIE55 -MSIE50;
        }catch (SQLException se){
                    se.printStackTrace();
                    System.out.println("Getting the Browser Count");
        }finally{

        }
        return new BrowserDataClass(MSIE50, MSIE55, MSIE60,Gecko, Netscape, other,total);
	}
    
    public static int getBrowserNumber(String query,String date, Connection con) throws SQLException{
        int result;
        PreparedStatement pstmt;
        pstmt = con.prepareStatement(query);
        pstmt.setString(1,date);
        ResultSet rs = pstmt.executeQuery();
        if(rs.next()){
            result = rs.getInt("TOTAL");
        }else{
            result = 0;
        }
        rs.close();
        pstmt.close();
        return result;
    }

    /**
     *This method gets a hash table of the number of users who experienced a 
     */
	public static Hashtable getThirtySecLoadHash(){
		Hashtable ht = new Hashtable();

        ResultSet rs;
        String strDay;
        int thirtySecCount;
        Connection con;
        PreparedStatement pstmt;
        // get a Hashtable of all the usernames, and user_id (keys)
        try{
            con = getArtConnection();
            pstmt = con.prepareStatement(SQL_30SECOND_MONTHLY_SELECT);
            rs = pstmt.executeQuery();
            if(rs != null){
                while (rs.next()) {
                    strDay = rs.getString("DAY");
                    thirtySecCount = rs.getInt("THIRTYSECLOADS");
                    ht.put(strDay, new Integer(thirtySecCount));
                }
                rs.close();
                pstmt.close();
                con.close();
                con=null;
                rs=null;
                pstmt=null;
            }
        }catch (SQLException se){
                    se.printStackTrace();
                    System.out.println("Error gettting user keys");
        }finally{

        }
		return ht;
	}

    public static void run10SecLoad(){
        for(int i=0;i<30;++i){
            SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyyMMdd");
            GregorianCalendar gc = new GregorianCalendar();
            gc.add(GregorianCalendar.DATE, -i);
            System.out.println("Updating 10 Sec Loads for date: " + sdf.format(gc.getTime()));
            populate10SecLoadTable(sdf.format(gc.getTime()));
        }            
    }

    public static void populate10SecLoadTable(String date){
        ResultSet rs;
        String strDay;
        Connection con;
        PreparedStatement pstmt;
        // get a Hashtable of all the usernames, and user_id (keys)
         try{
            con = getArtConnection();
            pstmt = con.prepareStatement(SQL_10SEC_LOAD);
            pstmt.setString(1,date);
            rs = pstmt.executeQuery();
            if(rs != null){
                while (rs.next()) {
                    int rpk = rs.getInt("RECORDPK");
                    int page_id = rs.getInt("PAGE_ID");
                    int user_id = rs.getInt("USER_ID");
                    String sdate = rs.getString("TIME");
                    int session_id = rs.getInt("SESSION_ID");
                    int machine_id = rs.getInt("MACHINE_ID");
                    int loadtime = rs.getInt("LOADTIME");
                    PreparedStatement pstmt2 = con.prepareStatement(SQL_10SEC_LOAD_INSERT);
                    pstmt2.setInt(1,rpk);
                    pstmt2.setInt(2,page_id);
                    pstmt2.setInt(3,user_id);
                    pstmt2.setString(4, sdate);
                    pstmt2.setInt(5,session_id);
                    pstmt2.setInt(6,machine_id);
                    pstmt2.setInt(7,loadtime);
                    try{
                        pstmt2.executeUpdate();
                    }catch (SQLException se){
                        System.out.println("Error Adding 10 Sec Load, It seems this is already there.");
                    }finally{
                        try{
                            pstmt2.close();
                            
                        }catch(SQLException se){
                            System.out.println("SQL Exception trying to close connection");
                        }
                    }
                }
                rs.close();
                pstmt.close();
                con.close();
                con=null;
                rs=null;
                pstmt=null;
            }
        }catch (SQLException se){
                    se.printStackTrace();
                    System.out.println("Error Populating 10 Sec Loads");
        }finally{

        }
	}

	public static void runDailyUpdate(String sdate) throws SQLException{
		String stsc = getThirtySecLoad(sdate);
		TotalLoadDataClass tldc = getTotalLoadData(sdate);
		MaxUserDataClass mud = getMaxUserData(sdate);
        updateDailySummaryTable(sdate,    stsc,    tldc,    mud );
        String HTMLTable = generateDailySummaryTable(sdate, stsc, tldc, mud);
        java.io.PrintWriter pw = getPrintWriter("DailySummary"+sdate.trim() + ".html");
        pw.print(HTMLTable);
        pw.flush();
        pw.close();
        populate10SecLoadTable(sdate);
        browserInfoUpdate(browserInfo(), sdate);
        
//        System.out.println(HTMLTable);
	}
    
    public static PrintWriter getPrintWriter(String s){
        try{
        return new PrintWriter(new FileOutputStream(s));
        }catch(FileNotFoundException fe){
            fe.printStackTrace();
        }
        return null;
    }
    

    
    public static String getThirtySecLoad(String date){
        ResultSet rs;
        String strDay; 
        int thirtySecCount = 0;
        Connection con;
        PreparedStatement pstmt;
        // get a Hashtable of all the usernames, and user_id (keys)
        try{
            con = getArtConnection();
            pstmt = con.prepareStatement(SQL_30SECOND_DAILY_SELECT);
            pstmt.setString(1,date);
            rs = pstmt.executeQuery();
            if(rs != null){
                while (rs.next()) {
                    strDay = rs.getString("DAY");
                    thirtySecCount = rs.getInt("THIRTYSECLOADS");
                }
                rs.close();
                pstmt.close();
                con.close();
                con=null;
                rs=null;
                pstmt=null;
            }
        }catch (SQLException se){
                    se.printStackTrace();
                    System.out.println("Error getThirtySecLoad: " + date);
        }finally{

        }
		return ""+thirtySecCount;
        
    }
    
    public static TotalLoadDataClass getTotalLoadData(String date){
        ResultSet rs;
        int totalLoads, averageLoadTime, distinctUsers, errorPages;
        String strDay;
        Connection con;
        PreparedStatement pstmt;
        TotalLoadDataClass tldc = null;
        // get a Hashtable of all the usernames, and user_id (keys)
        try{
            con = getArtConnection();
            pstmt = con.prepareStatement(SQL_TOTALLOAD_DAILY_SELECT);
            pstmt.setString(1,date);
            rs = pstmt.executeQuery();
            if(rs != null){
                while (rs.next()) {
                    totalLoads = rs.getInt("TOTALLOADS");
                    averageLoadTime = rs.getInt("AVERAGELOADTIME");
					distinctUsers = rs.getInt("DISTINCT_USERS");
					//errorPages = rs.getInt("ERROR_PAGES");
					strDay = rs.getString("DAY");
					tldc = new TotalLoadDataClass(totalLoads, averageLoadTime, 0, distinctUsers, 0);
                }
                rs.close();
                pstmt.close();
                con.close();
                con=null;
                rs=null;
                pstmt=null;
            }
        }catch (SQLException se){
                    se.printStackTrace();
                    System.out.println("Error getTotalLoadData: " + date);
        }finally{

        }
		return tldc;
        
    }
    
    public static MaxUserDataClass getMaxUserData(String date){

        ResultSet rs;
        int maxTime, maxUserID, maxPageID;
        String strDay;
        Connection con;
        PreparedStatement pstmt;
        MaxUserDataClass mudc = null;
        // get a Hashtable of all the usernames, and user_id (keys)
        try{
            con = getArtConnection();
            pstmt = con.prepareStatement(SQL_MAX_DAILY_LOADTIME);
            pstmt.setString(1,date.trim()+"0000");
            pstmt.setString(2,date.trim()+"2359");
            rs = pstmt.executeQuery();
            int recordpk = 0;
            int loadtime = 0;
            if(rs != null){
                while (rs.next()){
                    loadtime = rs.getInt(1);
                }
            }
            rs.close();
            rs = null;
            pstmt.close();
            pstmt = null;
            
            pstmt = con.prepareStatement(SQL_MAX_DAILY_RECORDPK);
            pstmt.setInt(1,loadtime);
            rs = pstmt.executeQuery();
            if(rs != null){
                while (rs.next()){
                    recordpk = rs.getInt(1);
                }
            }

            rs.close();
            rs = null;
            pstmt.close();
            pstmt = null;
            
            pstmt = con.prepareStatement(SQL_MAXUSER_DAILY_DATA);
            pstmt.setInt(1,recordpk);
            rs = pstmt.executeQuery();
            if(rs != null){
                while (rs.next()) {
                    maxTime = rs.getInt("MAXLOAD");
                    maxUserID = rs.getInt("MAX_USER_ID");
					maxPageID= rs.getInt("MAX_PAGE_ID");
					strDay = rs.getString("DAY");
					mudc= new MaxUserDataClass(maxTime, maxUserID, maxPageID);
                    mudc.MaxUserCompany = rs.getString("COMPANY_NAME");
                    mudc.MaxUserName = rs.getString("FULL_NAME");
					mudc.MaxPageName= rs.getString("PAGE_NAME");
                }
                rs.close();
                pstmt.close();
                con.close();
                con=null;
                rs=null;
                pstmt=null;
            }
        }catch (SQLException se){
                    se.printStackTrace();
                    System.out.println("Error getting Max User Data: " + date);
        }finally{

        }
		return mudc;
        
    }
    
    public static String generateDailySummaryTable(String sdate, String stsc, TotalLoadDataClass tldc, MaxUserDataClass mud){
        StringBuffer sb = new StringBuffer();
        String[] saProperty = new String[8];
        String[] saValue = new String[8];
        String[] saComment = new String[8];
        saProperty[0] = "No. Pages with > 30 Second Load Time";
        saValue[0] = stsc;
        saComment[0] = "All Production Boxes";
        saProperty[1] = "Total Pages Loaded";
		Integer in = new Integer(tldc.totalLoads);
        java.text.DecimalFormat decf= new DecimalFormat();
        decf.applyPattern("#,###,###");
        
        saValue[1] = ""+decf.format(tldc.totalLoads);
        saComment[1] = "All Production Boxes";
        saProperty[2] = "Average Page Load Time";
        saValue[2] = ""+ decf.format(tldc.averageLoadTime) + " &nbsp millis" ;
        saComment[2] = "All Production Boxes";
        saProperty[3] = "Ninetieth Percentile";
        saValue[3] = "Comming Soon";
        saComment[3] = "Across All Pages and All Boxes";
        saProperty[4] = "No. Distinct Users (Sessions)";
        saValue[4] = ""+ tldc.distinctUsers;
        saComment[4] = "Distinct Session ID";
        saProperty[5] = "No. of Times error.jsp was served";
        saValue[5] = "Comming Soon";
        saComment[5] = "All Production Boxes";
        saProperty[6] = "Worst User Experience";
        saValue[6] = ""+ (mud.MaxLoadTime/1000) + " &nbsp; &nbsp secs (" +(mud.MaxLoadTime/1000/60) + " mins)" ;
        saComment[6] = mud.MaxPageName +" page ";
        saProperty[7] = "Worst User ID/ Company Name";
        saValue[7] = mud.MaxUserName;
        saComment[7] = mud.MaxUserCompany + "";
        
       
        if(sdate !=null && stsc !=null && tldc!=null && mud !=null){
            sb.append("<HTML> \n");
            sb.append("<HEAD> \n");
            sb.append("<TITLE> \n");
            sb.append("</TITLE> \n");
            sb.append("<link rel='stylesheet' type='text/css' href='../common/reportstyle.css'> \n");
            sb.append("</HEAD> \n");
            sb.append("<BODY> \n");
            sb.append("<TABLE border =1> \n");
            sb.append("	<TR> \n");
            sb.append("	<TD> \n");
            sb.append("	<TABLE> \n");
            sb.append("		<TR> \n");
            sb.append("			<TD> \n");
            sb.append("			Date:"+ getYesterdaysDate("EEE, MMM d, yyyy")+" <BR> \n");
            sb.append("			<BR> \n");
            sb.append("			Any information that needs to be conveyed goes here.<BR> \n");
            sb.append("			</TD> \n");
            sb.append("		</TR> \n");
            sb.append("	</TABLE> \n");
            sb.append("	</TD> \n");
            sb.append("	</TR> \n");
            sb.append("	<TR> \n");
            sb.append("	<TD> \n");
            sb.append("	<TABLE> \n");
            sb.append("	<TR> \n");
            sb.append("		<TH BGCOLOR='wheat'> Summary Property \n");
            sb.append("		</TH> \n");
            sb.append("		<TH BGCOLOR='wheat'> Value \n");
            sb.append("		</TH> \n");
            sb.append("		<TH BGCOLOR='wheat'>  Comments \n");
            sb.append("		</TH> \n");
            sb.append("	</TR> \n");
            
            
            for(int i=0;i<saProperty.length; ++i){
                
                sb.append("	<TR> \n");
                sb.append("		<TD BGCOLOR='lightblue'>"+ saProperty[i]);
                sb.append("\n		</TD> \n");
                sb.append("		<TD BGCOLOR='lightblue'>"+saValue[i]);
                sb.append("\n		</TD> \n");
                sb.append("		<TD BGCOLOR='lightblue'>"+ saComment[i]);
                sb.append(" \n		</TD> \n");
                sb.append("	</TR> \n");
            }
            sb.append(" </TABLE> </TD></TR></TABLE> \n </BODY> \n</HTML> " );
        }   
        return sb.toString();
    }

	public static void runDailyUpdate() throws SQLException{
        String today = getYesterdaysDate("yyyyMMdd");
        // today = today's string;
        runDailyUpdate(today);
	}
    
    public static String getYesterdaysDate(String format){
        SimpleDateFormat sdf = new java.text.SimpleDateFormat(format);
        GregorianCalendar gc = new GregorianCalendar();
        gc.add(GregorianCalendar.DATE, -1);
        System.out.println("date: " + sdf.format(gc.getTime()));
        return sdf.format(gc.getTime());
    }
    
    public static void displayHelp(){
    }

    public static void updateDailySummaryTable(String key, String thirtySecCount,       
                                    TotalLoadDataClass tldc, MaxUserDataClass mudc)throws SQLException{
        Connection artcon = getArtConnection();
        PreparedStatement pstm = artcon.prepareStatement(SQL_GET_DAILY_SUMMARY_PK);
        pstm.setString(1,key);
        ResultSet lrs = pstm.executeQuery();
        boolean isUpdate = false;
        if(lrs != null){
            while(lrs.next()){
                isUpdate= true;
            }
        }
        lrs.close();
        lrs = null;
        pstm.close();
        pstm = null;
        
        if(isUpdate){
            pstm = artcon.prepareStatement(SQL_UPDATE_DAILY_SUMMARY);
            pstm.setInt(1,Integer.parseInt(thirtySecCount));
            pstm.setInt(2,tldc.totalLoads);
            pstm.setInt(3,tldc.averageLoadTime);
            pstm.setInt(4, 0 /*tldc.nineteithPercentile*/);
            pstm.setInt(5,mudc.MaxLoadTime);
            pstm.setInt(6,mudc.MaxUserID);
            pstm.setInt(7,mudc.MaxPageID);
            pstm.setInt(8, tldc.distinctUsers);
            pstm.setInt(9, 0);
            pstm.setString(10,key);
            pstm.executeUpdate();
        }else{
            pstm = artcon.prepareStatement(SQL_INSERT_DAILY_SUMMARY);
            pstm.setString(1,key);
            pstm.setInt(2,Integer.parseInt(thirtySecCount));
            pstm.setInt(3,tldc.totalLoads);
            pstm.setInt(4,tldc.averageLoadTime);
            pstm.setInt(5, 0 /*tldc.nineteithPercentile*/);
            pstm.setInt(6,mudc.MaxLoadTime);
            pstm.setInt(7,mudc.MaxUserID);
            pstm.setInt(8,mudc.MaxPageID);
            pstm.setInt(9, tldc.distinctUsers);
            pstm.setInt(10, 0);
            pstm.executeUpdate();
          
        }
        pstm.close();
        pstm = null;
        artcon.close();
        artcon = null;
    }
    
    public static void updateDailySummaryTable(String key, String thirtySecCount,       
                                    TotalLoadDataClass tldc, MaxUserDataClass mudc, BrowserDataClass bcd)throws SQLException{
        updateDailySummaryTable(key,thirtySecCount,tldc, mudc);
        browserInfoUpdate(bcd, key);
    }

    static void browserInfoUpdate(BrowserDataClass bdc, String key) throws SQLException{
        Connection artcon = getArtConnection();
        PreparedStatement pstm = artcon.prepareStatement(SQL_GET_DAILY_SUMMARY_PK);
        pstm.setString(1,key);

        ResultSet lrs = pstm.executeQuery();
        boolean isUpdate = false;
        if(lrs != null){
            while(lrs.next()){
                isUpdate= true;
            }
        }
        lrs.close();
        lrs = null;
        pstm.close();
        pstm = null;
        
        if(isUpdate){
            pstm = artcon.prepareStatement(SQL_UPDATE_DAILY_SUMMARY_BROWSER_INFO);
            pstm.setInt(1,bdc.MSIE50);
            pstm.setInt(2,bdc.MSIE55);
            pstm.setInt(3,bdc.MSIE60);
            pstm.setInt(4,bdc.Gecko);
            pstm.setInt(5,bdc.Netscape);
            pstm.setInt(6,bdc.other);
            pstm.setString(7,key);
            pstm.executeUpdate();
        }else{

            System.out.println("A problem has been encountered trying to update Browser Data");
        }
        pstm.close();
        pstm = null;
        artcon.close();
        artcon = null;
        
        
    }
	static class TotalLoadDataClass {
		public TotalLoadDataClass(){
		}
		public int totalLoads, averageLoadTime, nineteithPercentile, distinctUsers, errorPages;
		public TotalLoadDataClass(int tl, int alt, int np, int du, int ep){
			totalLoads = tl;
			averageLoadTime = alt;
			nineteithPercentile = np;
			distinctUsers = du;
			errorPages = ep;
		}
        public String toString(){
            return "totalLoads: " + totalLoads + " averageLoadTime: " + averageLoadTime + " distinctUsers: " + distinctUsers;
        }

	}
	static class MaxUserDataClass {
		public MaxUserDataClass(){
		}
		public int MaxLoadTime, MaxUserID, MaxPageID;
        public String MaxPageName, MaxUserName, MaxUserCompany;
		public MaxUserDataClass(int mlt, int muid, int mpid){
			MaxLoadTime = mlt;
			MaxUserID = muid;
			MaxPageID = mpid;
		}
        public String toString(){
            return "MaxLoadTime: " + MaxLoadTime + " muid: " + MaxUserID + " mpid: " + MaxPageID;
        }
	}
    
    
    static class BrowserDataClass {
        public BrowserDataClass(){
        }
        public int MSIE50, MSIE55, MSIE60, Gecko, Netscape , other, total;
        public BrowserDataClass(int MSIE50, int MSIE55, int MSIE60, int Gecko, int Netscape, int other, int total){
            this.MSIE50 = MSIE50;
            this.MSIE55 = MSIE55;
            this.MSIE60 = MSIE60;
            this.Gecko = Gecko;
            this.Netscape = Netscape;
            this.other = other;
            this.total = total;
        }
    }
        
}
