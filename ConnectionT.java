package logParser;
import java.sql.*;
import java.util.*;
import java.io.*;
import java.text.*;
//import edu.colorado.io.BufferedEasyReader;

class ConnectionT {
    private static int ARSTACKSIZE = 10000;
    private static Thread AROThread;   
    public static final boolean debug1 = false;
    public static final boolean debug2 = false;
    public static final boolean debug3 = false;
    private static int recordsAdded = 0;
    
    //////////////////////////////////////////////////////////////////////////////
    // SQL STATEMENTS
    //////////////////////////////////////////////////////////////////////////////
    /*    
    private static final String sqlUsersAll = "SELECT * FROM Users Where userName=?";
    //    private static final String sqlAddUser = "INSERT INTO USERS (userName) VALUES (?) ";
    private static final String sqlAddUser = "INSERT INTO NasAccess.Users (userName) VALUES (?) ";
    private static final String sqlPagesAll = "SELECT * FROM Pages Where PageName=?";
    private static final String sqlAddPages = "INSERT INTO PAGES (PageName) VALUES (?) ";
    private static final String sqlSessionsAll = "SELECT * FROM Sessions Where sessionTXT=?";
    private static final String sqlAddSession = "INSERT INTO Sessions (sessionTXT, IPAddress) "+
                         " VALUES (?,?) ";
    private static final String sqlFullRecordInsert = "INSERT INTO accessrecords "+
                         "(PageNo, UserNo, sessionPK, Machine ) VALUES (?,?,?,?)";
    private static final SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss aa");
    private static int flagSessions = 0;
    private static final String sqlFullLookup = "SELECT pageID, userID, sessionPK FROM "+
                         "NasAccess.Pages, NasAccess.Users, NasAccess.Sessions WHERE "+
                         "NasAccess.Pages.pageName=? AND NasAccess.Users.userName=? "+
                         " AND NasAccess.Sessions.sessionTXT=?";
    */
    private static final String sqlUsersAll = "SELECT * FROM NasAccess.Users Where userName=?";
    //    private static final String sqlAddUser = "INSERT INTO USERS (userName) VALUES (?) ";
    private static final String sqlAddUser = "INSERT INTO NasAccess.Users (userName) VALUES (?) ";
    private static final String sqlPagesAll = "SELECT * FROM NasAccess.Pages Where PageName=?";
    private static final String sqlAddPages = "INSERT INTO NasAccess.Pages (PageName) VALUES (?) ";
    private static final String sqlSessionsAll = "SELECT * FROM NasAccess.Sessions Where sessionTXT=?";
    private static final String sqlAddSession = "INSERT INTO NasAccess.Sessions (sessionTXT, IPAddress) "+
                         " VALUES (?,?) ";
    private static final String sqlFullRecordInsert = "INSERT INTO NasAccess.AccessRecords "+
                         "(PageNo, UserNo, Time, sessionPK, Machine ) VALUES (?,?,?,?,?)";
    private static final SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss aa");
    private static int flagSessions = 0;
    private static final String sqlFullLookup = "SELECT pageID, userID, sessionPK FROM "+
                         "NasAccess.Pages, NasAccess.Users, NasAccess.Sessions WHERE "+
                         "NasAccess.Pages.pageName=? AND NasAccess.Users.userName=? "+
                         " AND NasAccess.Sessions.sessionTXT=?";





    private static int fullLookup=0;
    private static Hashtable hashSessions = new Hashtable();
    private static Hashtable hashUsers = new Hashtable();
    private static Hashtable hashPages = new Hashtable();
    private static Hashtable hashMachines = new Hashtable();
    private static int totalCashedPages = 0;
    private static int totalCashedUsers = 0;
    private static int totalCashedSessions = 0;
    private static int rehashSessions=0;
    private static int rehashUsers=0;
    private static int rehashPages=0;
    private static int rehashMachines=0;
    private static Stack stackAccessRecords = new Stack();




public static void main(String args[]){
    String s1 = "order/index,03/21/2001, 09:33:16 "+
                "AM,019327ltest1223,GXLiteSessionID--4534014804865994530,10.3.10.45";

    Connection con;
    Statement stmt;
	ResultSet rs;
        jspErrorObject jeo = new jspErrorObject(s1);
        
	try{
		Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
	}catch (Exception e){
		e.printStackTrace();
	}



    try{
    	con = DriverManager.getConnection("jdbc:odbc:AccessRecords");
        String[] e = getForiegnKeys(jeo, con);
        
        addRecord(e,con);
        stmt = con.createStatement();
	    rs = stmt.executeQuery("SELECT * FROM Users");
        if(rs != null){
		    while (rs.next()) {
	 	        System.out.println("UserId: " + rs.getInt("userID") + "  UserName: " + rs.getString("userName"));
		    }//if
	    }
    }catch (Exception e){
	 e.printStackTrace();
    }
   }



   public static String[] getForiegnKeys2(jspErrorObject jeo, Connection con) throws SQLException {
    String page = jeo.getPage().trim();
    String uid = jeo.getUserID().trim();
    String fullsession = jeo.getFullSessionID().trim();
    String sip = jeo.getIPAddress().trim();
    String MachineNo = "NA";
    String[] results = new String[6];
    boolean gotfullset = false;


    PreparedStatement pstmt=null;
    ResultSet rs = null;
    PreparedStatement pstmt2 = null;
    ResultSet rs2 = null;

    String UserNo ="NA";
    String PageNo = "NA";
    String SessionNo ="NA";

    ////////////////////////////////////////////////////////////////////////////////
    //****************************************************************************//
    //   Check All the Cashed Memory to Prevent an unneeded database access       //
    //****************************************************************************//
    ////////////////////////////////////////////////////////////////////////////////
    
    UserNo = getCashedUser(uid);
    PageNo = getCashedPage(page);
    SessionNo = getCashedSession(fullsession);

    ////////////////////////////////////////////////////////////////////////////////
    //****************************************************************************//
    //   Confirm that we got what we needed.  If not perform a database query     //
    //****************************************************************************//
    ////////////////////////////////////////////////////////////////////////////////

    if(UserNo == null)
	UserNo = getUserNo(uid,con);
    else
	if(++totalCashedUsers == 1000)
            System.out.println("Total Cashed Users: "+ totalCashedUsers);
    if(PageNo == null)
	PageNo = getPageNo(page,con);
    else
	if(++totalCashedPages == 1000)
            System.out.println("Total Cashed Pages : "+ totalCashedPages);
    if(SessionNo == null)
	SessionNo = getSession(fullsession,sip,con);
    else 
	if(++totalCashedSessions == 1000)
            System.out.println("Total Cashed Sessions: "+ totalCashedSessions);
    
    results[0] = "Not Used";
    results[1] = ""+PageNo;
    results[2] = ""+UserNo;
    results[3] = ""+jeo.getTimeStampFormatDate();
    results[4] = "" +SessionNo ;
    results[5] = ""+MachineNo;
    if(debug2)
       displayFK(results);
    rs = null;
    rs2=null;
    pstmt=null;
    pstmt2=null;
    return results;
    
   }


    public static void dummyCall(){
	int i = 1;
	++i;
    }




    public static String getCashedSession(String session){
        Object ohu = hashSessions.get(session.trim());
	if(ohu != null)
	    return ((Integer)ohu).toString();
        else
            return null;

    }
    public static String getCashedUser(String user){
        Object ohu = hashUsers.get(user.trim());
	if(ohu != null)
	    return ((Integer)ohu).toString();
        else
            return null;

    }
    public static String getCashedPage(String page){
        Object ohu = hashPages.get(page.trim());
	if(ohu != null)
	    return ((Integer)ohu).toString();
        else
            return null;

    }
    public static String getCashedMachine(String machine){
        Object ohu = hashMachines.get(machine.trim());
	if(ohu != null)
	    return ((Integer)ohu).toString();
        else
            return null;

    }

    public static void addCashedSession(String sessionTXT, String sessionNo){
	if(hashSessions.size() > 2000){
	    hashSessions = new Hashtable();
	    System.out.println("Ran out of cashe room for session you may want to increase cashe size");
	}

        Object o =  hashSessions.put(sessionTXT,new Integer(sessionNo));
        if(o !=null)
	    if(++rehashSessions == 1000)
                System.out.println("Rehashing of Sessions : =" +rehashSessions);
    }
    public static void addCashedUser(String userName , String userNo ){
	Object o =  hashUsers.put(userName,new Integer(userNo));
        if(o !=null)
	    if(++rehashUsers == 1000)
                System.out.println("Rehashing of Users : =" +rehashUsers);
    }
    public static void addCashedPage(String pageName, String pageNo ){
	Object o =  hashPages.put(pageName,new Integer(pageNo));
        if(o !=null)
	    if(++rehashPages == 1000)
                System.out.println("Rehashing of Pages : =" +rehashPages);
    }
    public static void addCashedMachine(String machineName, String machineNo){
	Object o =  hashMachines.put(machineName, new Integer(machineNo));
        if(o !=null)
	    if(++rehashMachines == 1000)
                System.out.println("Rehashing of Machines : =" +rehashMachines);
   }

   
   public static String[] getForiegnKeys(jspErrorObject jeo, Connection con) throws SQLException {
    String page = jeo.getPage();
    String uid = jeo.getUserID();
    String fullsession = jeo.getFullSessionID();
    String sip = jeo.getIPAddress();
    String MachineNo = "NA";
    String[] results = new String[6];
    boolean gotfullset = false;


    PreparedStatement pstmt=null;
    ResultSet rs = null;
    PreparedStatement pstmt2 = null;
    ResultSet rs2 = null;

    String UserNo ="NA";
    String PageNo = "NA";
    String SessionNo ="NA";




//    private static String LastSessionID;
//    private static Vector vecUsers = new Vector();
//    private static Vector vecPages = new Vector()
   
    //    if(LastSessionID.equalsIgnoreCase(fullsession.trim()){ 
    //        foundSession=true;
    //  fullsession=LastSessionNo;
    //}
    int lf = 0;
    try{

        pstmt = con.prepareStatement(sqlFullLookup);
        lf=2;
        if (page == null)
	    System.out.println("Page = null");
        if(pstmt == null)
	    System.out.println("pstmt =null");

        pstmt.setString(1,page.trim());

        lf=3;
        pstmt.setString(2,uid.trim());
        lf=4;
        pstmt.setString(3,fullsession.trim());
        lf=5;

        rs = pstmt.executeQuery();
        lf=6;
        if(rs != null){
        lf=7;
            while(rs.next()){
        lf=8;
                PageNo = ""+rs.getInt(1);
        lf=9;
                UserNo = ""+rs.getInt(2);
        lf=10;
                SessionNo = ""+rs.getInt(3);
        lf=11;
                gotfullset=true;
        lf=12;
                ++fullLookup;
        lf=13;
                if(fullLookup%10000 ==0)
                    System.out.println("Full Lookup = " + fullLookup);
        lf=14;
            }
        lf=15;
        }
    }catch (Exception e){
        System.out.println("Error Getting the big set stop execution Location: " + lf);
        System.out.println(sqlFullLookup + ": " + page +", " + uid + ", " + fullsession);
    }finally{
        rs.close();
        pstmt.close();
        rs=null;
        pstmt= null;
    }
                
        
        
    if(!gotfullset) // Big if
    {
	int fuser =0;
    ////////////////////////////////////////////////////////////////////////
    //Get UserNo
    ////////////////////////////////////////////////////////////////////////
    try{
	fuser =0;
        pstmt = con.prepareStatement(sqlUsersAll);
	fuser =1;
        pstmt.setString(1,uid.trim());
	fuser =2;
        rs = pstmt.executeQuery();
	fuser =3;
        if(rs != null){
	fuser =4;
            boolean gotFK=false;
	fuser =5;
            if(debug1)
                System.out.println("The Result Set is not Null");
            while (rs.next()) {
	fuser =6;
                UserNo = ""+rs.getInt("userID");
                gotFK = true;
                if(debug3)
                    System.out.println("While in Results Set" + UserNo/*+ rs.getInt("userID")*/);
            }
	fuser =7;
            rs.close();
	fuser =8;
            rs=null;
	fuser =9;
            if(!gotFK){
                try{
	fuser =11;
                    pstmt2 = con.prepareStatement(sqlAddUser);
	fuser =12;
                    pstmt2.setString(1,uid.trim());
	fuser =13;
                    if(pstmt2.executeUpdate() ==1)
                    {   
	fuser =14;
                        pstmt2.close();
	fuser =15;
                        pstmt2=null;
	fuser =16;
                        pstmt2 = con.prepareStatement(sqlUsersAll);
	fuser =17;
                        pstmt2.setString(1,uid.trim());
	fuser =18;
                        rs2 = pstmt.executeQuery();
//                        rs2 = stmt2.executeQuery("SELECT userID FROM USERS WHERE userName='"+uid.trim()+"'");
	fuser =19;
                        if(rs2.next())
                            UserNo = ""+rs2.getInt("userID");
                        else
                            UserNo = "1";
                    }else
                        UserNo = "1";
                }catch (SQLException se){
                    System.out.println("Error Adding A User Setting To User 1 location: " + fuser);
                }finally{
                    try{
                        if(rs2 != null)
                            rs2.close();
                        if(pstmt2 != null)
                            pstmt2.close();
                    }catch(SQLException sqe){
                    }
                }
            }
            if(debug3)
                System.out.println(" Location Leaving User Query");
        }else{
            System.out.println("Not Found");
        }
    }catch (Exception e){
        System.out.println("Vauge exception somewhere in the Updating Users Setting UserNo to 1");
        UserNo = "1";
    }finally{
        if(debug3)
        System.out.println("Entering Finally of user Query");
        try{
            if(rs != null){
                rs.close();
            }
            if(pstmt != null){
                pstmt.close();
            }
        }catch(SQLException sqe){
            System.out.println("Entering Catch of Exception trying to class a statement");
        }
        if(debug3)
        System.out.println("Leaving Finally of user Query");
    }
    
    
    
    ////////////////////////////////////////////////////////////////////////
    //Page No
    ////////////////////////////////////////////////////////////////////////
    try{
//        rs = stmt.executeQuery("SELECT * FROM Pages Where PageName='"+page.trim()+"'");
        pstmt = con.prepareStatement(sqlPagesAll);
        pstmt.setString(1,page.trim());
        rs = pstmt.executeQuery();
        if(rs != null){
            boolean gotFK=false;
            if(debug1)
                System.out.println("The Result Set is not Null");
	    while (rs.next()) {
                PageNo = ""+rs.getInt("PageID");
                if(debug2)
                    System.out.println("Page Name : ID --  " + rs.getString("PageName") +
                                       "  :  " + PageNo +"   Desired Page: " + page.trim());
                gotFK = true;
            }
            rs.close();
            rs=null;
            if(!gotFK){
                pstmt2 = con.prepareStatement(sqlAddPages);
                pstmt2.setString(1,page.trim());
                try{
                    if(pstmt2.executeUpdate() ==1)
                    {   
                        pstmt2.close();
                        pstmt2 = null;
                        pstmt2 = con.prepareStatement(sqlPagesAll);
                        pstmt2.setString(1,page.trim());
                        rs2 = pstmt2.executeQuery();
                        if(rs2.next())
                            PageNo = ""+rs2.getInt("PageID");
                        else
                            PageNo = "1";
                    }else
                        PageNo = "1";
                }catch (SQLException se){
                    System.out.println("Error Updating PageName Setting Value to 1");
                    PageNo = "1";
                }finally{
                    try{
                        if(rs2 != null)
                            rs2.close();
                        if(pstmt2 != null)
                            pstmt2.close();
                    }catch(SQLException sqe){
                    }
                }
            }
       }else{
            if(debug1)
                System.out.println("Page ID Not Found");
        }
    }catch(Exception e){
        System.out.println("Vauge exception somewhere in the Pages Setting PageNo to 1");
        PageNo ="1";
    }finally{
        try{
            if(rs != null)
                rs.close();
            if(pstmt != null)
                pstmt.close();
        }catch(SQLException sqe){
        }
    }
    
    
    
    ////////////////////////////////////////////////////////////////////////
    //Session Values
    ////////////////////////////////////////////////////////////////////////

    try{

        pstmt = con.prepareStatement(sqlSessionsAll);
        pstmt.setString(1,fullsession.trim());
        rs = pstmt.executeQuery();
        if(rs != null){
            boolean gotFK=false;
            if(debug1)
                System.out.println("The Result Set is not Null");
	    while (rs.next()) {
                SessionNo = ""+rs.getInt("sessionPK");
                gotFK = true;
            }
            if(!gotFK){
//            private static final String sqlAddSesion = "INSERT INTO Sessions (sessionTXT, sessionNo, IPAddress) "+
//                         " VALUES (?,?,?) ";
                pstmt2 = con.prepareStatement(sqlAddSession);
                pstmt2.setString(1,fullsession.trim());
                pstmt2.setString(2,sip.trim());
                
                int flag1=0;
                try{
                    ++flagSessions;
                    if(pstmt2.executeUpdate() ==1)
                    {
                        flag1 = 1;
                        pstmt2.close();
                        flag1 = 2;
                        pstmt2 = null;
                        flag1 = 3;
                        pstmt2 = con.prepareStatement(sqlSessionsAll);
                        pstmt2.setString(1,fullsession.trim());
                        flag1 = 4;
                        rs2 = pstmt2.executeQuery();
                        flag1 = 5;
                        if(rs2.next())
                            SessionNo = ""+rs2.getInt("sessionPK");
                        else
                            SessionNo = "10";
                    }else
                        SessionNo = "10";
                        flag1 = 6;
                }catch (SQLException se){
                    System.out.println("Error Updating the Session so Setting the Value to 10");
                    System.out.println(sqlAddSession + ", " + fullsession.trim() + 
                              ", " + sip.trim() +" at: "+flag1 + " For session: " + flagSessions);
                    SessionNo = "10";
                }finally{
                    try{
                        if(rs2 != null)
                            rs2.close();
                        if(pstmt2 != null)
                            pstmt2.close();
                    }catch(SQLException sqe){
                    }
                }
            }
        }else{
            if(debug1)
                System.out.println("Session ID Not Found");
        }
    }catch (Exception e){
        System.out.println("Vauge exception somewhere in the session Setting Session to 10");
        SessionNo="10";
    }finally{
        try{
            if(rs != null)
                rs.close();
            if(pstmt != null)
                pstmt.close();
        }catch(SQLException sqe){
        }
    }
    }// Big if
    
    results[0] = "Not Used";
    results[1] = ""+PageNo;
    results[2] = ""+UserNo;
    results[3] = ""+jeo.getFormatDate();
    results[4] = "" +SessionNo ;
    results[5] = ""+MachineNo;
    if(debug2)
       displayFK(results);
    rs = null;
    rs2=null;
    pstmt=null;
    pstmt2=null;
    return results;
    
   }





    public static String getUserNo(String uid, Connection con){
    PreparedStatement pstmt=null;
    ResultSet rs = null;
    PreparedStatement pstmt2 = null;
    ResultSet rs2 = null;

    String UserNo ="NA";
    String PageNo = "NA";
    String SessionNo ="NA";

	int fuser =0;
    ////////////////////////////////////////////////////////////////////////
    //Get UserNo
    ////////////////////////////////////////////////////////////////////////
    try{
	fuser =0;
        pstmt = con.prepareStatement(sqlUsersAll);
	fuser =1;
        pstmt.setString(1,uid.trim());
	fuser =2;
        rs = pstmt.executeQuery();
	fuser =3;
        if(rs != null){
	fuser =4;
            boolean gotFK=false;
	fuser =5;
            if(debug1)
                System.out.println("The Result Set is not Null");
            while (rs.next()) {
	fuser =6;
                UserNo = ""+rs.getInt("userID");
                gotFK = true;
                if(debug3)
                    System.out.println("While in Results Set" + UserNo/*+ rs.getInt("userID")*/);
            }
	fuser =7;
            rs.close();
	fuser =8;
            rs=null;
	fuser =9;
            if(!gotFK){
                try{
	fuser =11;
                    pstmt2 = con.prepareStatement(sqlAddUser);
	fuser =12;
                    pstmt2.setString(1,uid.trim());
	fuser =13;
                    if(pstmt2.executeUpdate() ==1)
                    {   
                        //System.out.println("User was added to the user table");
	fuser =14;
                        pstmt2.close();
	fuser =15;
                        pstmt2=null;
	fuser =16;
                        pstmt2 = con.prepareStatement(sqlUsersAll);
	fuser =17;
                        pstmt2.setString(1,uid.trim());
	fuser =18;
                        rs2 = pstmt.executeQuery();
//                        rs2 = stmt2.executeQuery("SELECT userID FROM USERS WHERE userName='"+uid.trim()+"'");
	fuser =19;
                        if(rs2.next())
                            UserNo = ""+rs2.getInt("userID");
                        else
                            UserNo = "1";
                    }else
                        UserNo = "1";
                }catch (SQLException se){
                    System.out.println("Error Adding A User Setting To User 1 location: " + fuser);
                }finally{
                    try{
                        if(rs2 != null)
                            rs2.close();
                        if(pstmt2 != null)
                            pstmt2.close();
                    }catch(SQLException sqe){
                    }
                }
            }
            if(debug3)
                System.out.println(" Location Leaving User Query");
        }else{
            System.out.println("Not Found");
        }
    }catch (Exception e){
        System.out.println("Vauge exception somewhere in the Updating Users Setting UserNo to 1");
        UserNo = "1";
    }finally{
        if(debug3)
        System.out.println("Entering Finally of user Query");
        try{
            if(rs != null){
                rs.close();
            }
            if(pstmt != null){
                pstmt.close();
            }
        }catch(SQLException sqe){
            System.out.println("Entering Catch of Exception trying to class a statement");
        }
        if(debug3)
        System.out.println("Leaving Finally of user Query");
    }
    addCashedUser(uid,UserNo);
    return UserNo;


    }    
    
 







   public static String getPageNo(String page, Connection con){
    PreparedStatement pstmt=null;
    ResultSet rs = null;
    PreparedStatement pstmt2 = null;
    ResultSet rs2 = null;

    String UserNo ="NA";
    String PageNo = "NA";
    String SessionNo ="NA";
    
    ////////////////////////////////////////////////////////////////////////
    //Page No
    ////////////////////////////////////////////////////////////////////////
    try{
//        rs = stmt.executeQuery("SELECT * FROM Pages Where PageName='"+page.trim()+"'");
        pstmt = con.prepareStatement(sqlPagesAll);
        pstmt.setString(1,page.trim());
        rs = pstmt.executeQuery();
        if(rs != null){
            boolean gotFK=false;
            if(debug1)
                System.out.println("The Result Set is not Null");
	    while (rs.next()) {
                PageNo = ""+rs.getInt("PageID");
                if(debug2)
                    System.out.println("Page Name : ID --  " + rs.getString("PageName") +
                                       "  :  " + PageNo +"   Desired Page: " + page.trim());
                gotFK = true;
            }
            rs.close();
            rs=null;
            if(!gotFK){
                pstmt2 = con.prepareStatement(sqlAddPages);
                pstmt2.setString(1,page.trim());
                try{
                    if(pstmt2.executeUpdate() ==1)
                    {   
                        pstmt2.close();
                        pstmt2 = null;
                        pstmt2 = con.prepareStatement(sqlPagesAll);
                        pstmt2.setString(1,page.trim());
                        rs2 = pstmt2.executeQuery();
                        if(rs2.next())
                            PageNo = ""+rs2.getInt("PageID");
                        else
                            PageNo = "1";
                    }else
                        PageNo = "1";
                }catch (SQLException se){
                    System.out.println("Error Updating PageName Setting Value to 1");
                    PageNo = "1";
                }finally{
                    try{
                        if(rs2 != null)
                            rs2.close();
                        if(pstmt2 != null)
                            pstmt2.close();
                    }catch(SQLException sqe){
                    }
                }
            }
       }else{
            if(debug1)
                System.out.println("Page ID Not Found");
        }
    }catch(Exception e){
        System.out.println("Vauge exception somewhere in the Pages Setting PageNo to 1");
        PageNo ="1";
    }finally{
        try{
            if(rs != null)
                rs.close();
            if(pstmt != null)
                pstmt.close();
        }catch(SQLException sqe){
        }
    }

    addCashedPage(page.trim(),PageNo);
    return PageNo;
    }












    public static String getSession(String fullsession, String sip, Connection con){
    PreparedStatement pstmt=null;
    ResultSet rs = null;
    PreparedStatement pstmt2 = null;
    ResultSet rs2 = null;

    String UserNo ="NA";
    String PageNo = "NA";
    String SessionNo ="NA";
    ////////////////////////////////////////////////////////////////////////
    //Session Values
    ////////////////////////////////////////////////////////////////////////

    try{

        pstmt = con.prepareStatement(sqlSessionsAll);
        pstmt.setString(1,fullsession.trim());
        rs = pstmt.executeQuery();
        if(rs != null){
            boolean gotFK=false;
            if(debug1)
                System.out.println("The Result Set is not Null");
	    while (rs.next()) {
                SessionNo = ""+rs.getInt("sessionPK");
                gotFK = true;
            }
            if(!gotFK){
//            private static final String sqlAddSesion = "INSERT INTO Sessions (sessionTXT, sessionNo, IPAddress) "+
//                         " VALUES (?,?,?) ";
                pstmt2 = con.prepareStatement(sqlAddSession);
                pstmt2.setString(1,fullsession.trim());
                pstmt2.setString(2,sip.trim());
                
                int flag1=0;
                try{
                    ++flagSessions;
                    if(pstmt2.executeUpdate() ==1)
                    {
                        flag1 = 1;
                        pstmt2.close();
                        flag1 = 2;
                        pstmt2 = null;
                        flag1 = 3;
                        pstmt2 = con.prepareStatement(sqlSessionsAll);
                        pstmt2.setString(1,fullsession.trim());
                        flag1 = 4;
                        rs2 = pstmt2.executeQuery();
                        flag1 = 5;
                        if(rs2.next())
                            SessionNo = ""+rs2.getInt("sessionPK");
                        else
                            SessionNo = "10";
                    }else
                        SessionNo = "10";
                        flag1 = 6;
                }catch (SQLException se){
                    System.out.println("Error Updating the Session so Setting the Value to 10");
                    System.out.println(sqlAddSession + ", " + fullsession.trim() + 
                              ", " + sip.trim() +" at: "+flag1 + " For session: " + flagSessions);
                    SessionNo = "10";
                }finally{
                    try{
                        if(rs2 != null)
                            rs2.close();
                        if(pstmt2 != null)
                            pstmt2.close();
                    }catch(SQLException sqe){
                    }
                }
            }
        }else{
            if(debug1)
                System.out.println("Session ID Not Found");
        }
    }catch (Exception e){
        System.out.println("Vauge exception somewhere in the session Setting Session to 10");
        SessionNo="10";
    }finally{
        try{
            if(rs != null)
                rs.close();
            if(pstmt != null)
                pstmt.close();
        }catch(SQLException sqe){
        }
    }

    addCashedSession(fullsession.trim(),SessionNo);

    return SessionNo;
    }

   



   public static boolean addRecord(String[] cols, Connection con) throws SQLException, RecordRecordsException{
       ParsePosition pos = new ParsePosition(0);
       int i;
       int recordPK=0;
       //System.out.println("Time Stamp: " + cols[3].trim());
       Timestamp ts = Timestamp.valueOf(cols[3].trim());
       //System.out.println("After construction of timestamp");

       if(cols[5].equalsIgnoreCase("NA"))
           cols[5]="1";
       PreparedStatement pstmp = con.prepareStatement(sqlFullRecordInsert);
       pstmp.setInt(1,Integer.parseInt(cols[1]));
       pstmp.setInt(2,Integer.parseInt(cols[2]));
       pstmp.setTimestamp(3,ts);
       pstmp.setInt(4,Integer.parseInt(cols[4]));
       pstmp.setInt(5,Integer.parseInt(cols[5]));
	try{
	        int rows = pstmp.executeUpdate();
		pstmp.close();
                recordsAdded = recordsAdded+rows;
	}catch(SQLException se){
	    System.out.println("Error Adding Cashed Blocked Records");
	    throw new RecordRecordsException();
	}

       //stackAccessRecords.addElement(new AccessRecordObject(pstmp,con));
       //boolean ret = true;



       //if(stackAccessRecords.size() == 2000){
	   //   AROThread = new Thread(new AccessRecordObject(stackAccessRecords));
	   //AROThread.start();
	   //ARSTACKSIZE += 5000;
	   //BufferedEasyReader.pause(1000);
	 //  	ret =   emptyAccessRecords();
       //}

       return true;

       /*       
       try{
           int rows= pstmp.executeUpdate();
           return true;
       }catch (SQLException se){
           System.out.println("Error Adding Record Throwing New RecordRecrodsException");
           throw new RecordRecordsException();
       }finally{
           try{
               if(pstmp != null)
                   pstmp.close();
           }catch(SQLException sqe){
           }
       }
       */
   }

   
   
   
   private static boolean  emptyAccessRecords() throws RecordRecordsException {
	try{
	    AccessRecordObject aro;
	    while(!stackAccessRecords.empty()){
	        aro =(AccessRecordObject) stackAccessRecords.pop();
	        int rows = aro.ps.executeUpdate();
		aro.ps.close();
	    }
	}catch(SQLException se){
	    System.out.println("Error Adding Cashed Blocked Records");
	    throw new RecordRecordsException();
	}
	return true;


    }







private static void displayFK(String[] s){
       for (int i = 0; i < 6 ; ++i){   
           System.out.println(""+i+": " + s[i]);
       }
   }





   
}





