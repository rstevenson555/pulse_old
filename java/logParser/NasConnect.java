/*
 * NasConnect.java
 *
 * Created on March 22, 2001, 3:53 PM
 */

package logParser;
import java.sql.*;
import java.util.*;
import java.io.*;
import java.text.*;

/**
 *
 * @author  i0360d3
 * @version 
 */
public class NasConnect extends java.lang.Object {

    /** Creates new NasConnect */
    public NasConnect() {
    }

    /**
    * @param args the command line arguments
    */
    public static final boolean debug1 = false;
    public static final boolean debug2 = false;


public static void main(String args[]){
String s1 = "order/index,03/21/2001, 09:33:16 AM,019327ltest1223,GXLiteSessionID--4534014804865994530,10.3.10.45";
//order/index,03/21/2001, 09:33:16 AM,019327ltest1828,GXLiteSessionID-8647799256189946340,10.3.10.45
//order/index,03/21/2001, 09:33:16 AM,019327ltest1827,GXLiteSessionID-6740654538047533830,10.3.10.45
//order/index,03/21/2001, 09:33:16 AM,019327ltest1427,GXLiteSessionID-8754856920679545004,10.3.10.45
//order/index,03/21/2001, 09:33:16 AM,019327ltest1464,GXLiteSessionID--403299320174929570,10.3.10.45

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
    	con = DriverManager.getConnection("jdbc:odbc:NasAccess");
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
   
   public static String[] getForiegnKeys(jspErrorObject jeo, Connection con) throws SQLException {
    String page = jeo.getPage();
    String uid = jeo.getUserID();
    String fullsession = jeo.getFullSessionID();
    long lsession = Long.parseLong(jeo.getSessionValue());
    String sip = jeo.getIPAddress();
    Statement stmt = con.createStatement();
    String UserNo ="NA";
    String PageNo = "NA";
    String SessionNo ="NA";
    String MachineNo = "NA";
    String[] results = new String[6];
    ResultSet rs = null;
    
    ////////////////////////////////////////////////////////////////////////
    //Get UserNo
    ////////////////////////////////////////////////////////////////////////
    try{
        rs = stmt.executeQuery("SELECT * FROM Users Where userName='"+uid.trim()+"'");
        if(rs != null){
            boolean gotFK=false;
            if(debug1)
                System.out.println("The Result Set is not Null");
            while (rs.next()) {
                UserNo = ""+rs.getInt("userID");
                gotFK = true;
            }
            if(!gotFK){
                Statement stmt2 = con.createStatement();
                try{
                    if(stmt2.executeUpdate("INSERT INTO USERS (userName) VALUES ('"+uid.trim() +"') ") ==1)
                    {   
                        ResultSet rs2;
                        rs2 = stmt2.executeQuery("SELECT userID FROM USERS WHERE userName='"+uid.trim()+"'");
                        if(rs.next())
                            UserNo = ""+rs2.getInt("userID");
                        else
                            UserNo = "1";
                    }else
                        UserNo = "1";
                }catch (SQLException se){
                    System.out.println("Error Adding A User Setting To User 1");
                }
            }
            if(debug1)
                System.out.println("UserID Found: " + UserNo);
        }else{
            System.out.println("Not Found");
        }
    }catch (Exception e){
        System.out.println("Vauge exception somewhere in the Updating Users Setting UserNo to 1");
        UserNo = "1";
    }
    
    
    
    ////////////////////////////////////////////////////////////////////////
    //Page No
    ////////////////////////////////////////////////////////////////////////
    try{
        rs = stmt.executeQuery("SELECT * FROM Pages Where PageName='"+page.trim()+"'");
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
            if(!gotFK){
                Statement stmt2 = con.createStatement();
                try{
                    if(stmt2.executeUpdate("INSERT INTO PAGES (PageName) VALUES ('"+page.trim() +"') ") ==1)
                    {   
                        ResultSet rs2;
                        rs2 = stmt2.executeQuery("SELECT PageID FROM PAGES WHERE PageName='"+page.trim()+"'");
                        if(rs2.next())
                            PageNo = ""+rs2.getInt("PageID");
                        else
                            PageNo = "1";
                    }else
                        PageNo = "1";
                }catch (SQLException se){
                    System.out.println("Error Updating PageName Setting Value to 1");
                    PageNo = "1";
                }
            }
            if(debug1)
                System.out.println("PageID Found: " + PageNo);
        }else{
            if(debug1)
                System.out.println("Page ID Not Found");
        }
    }catch(Exception e){
        System.out.println("Vauge exception somewhere in the Pages Setting PageNo to 1");
        PageNo ="1";
    }
    
    
    
    ////////////////////////////////////////////////////////////////////////
    //Session Values
    ////////////////////////////////////////////////////////////////////////

    try{
        rs = stmt.executeQuery("SELECT * FROM Sessions Where sessionTXT='"+fullsession.trim()+"'");
        if(rs != null){
            boolean gotFK=false;
            if(debug1)
            System.out.println("The Result Set is not Null");
	    while (rs.next()) {
                SessionNo = ""+rs.getInt("sessionPK");
                gotFK = true;
            }
            if(!gotFK){
                Statement stmt2 = con.createStatement();
                String sqlString = "INSERT INTO Sessions (sessionTXT) VALUES ('"+fullsession.trim() +"') ";
                String sqlUpdate2 = "UPDATE Sessions SET sessionNo='"+lsession+"' WHERE sessionTXT='"+fullsession.trim()+"'";
                String sqlUpdate = "UPDATE Sessions SET IPAddress='"+sip.trim()+"' WHERE sessionTXT='"+fullsession.trim()+"'";
                
            if(debug1)
                System.out.println(sqlString);
                try{
                    if(stmt2.executeUpdate(sqlString) ==1)
                    {   
                        System.out.println("UPDATING SESSION");
                        int i = stmt2.executeUpdate(sqlUpdate);
                        i = stmt2.executeUpdate(sqlUpdate2);
                        System.out.println("Before 2");
                        ResultSet rs2;
                        rs2 = stmt2.executeQuery("SELECT sessionPK FROM Sessions WHERE sessionTXT='"+fullsession.trim()+"'");
                    
                        if(rs.next())
                            SessionNo = ""+rs2.getInt("sessionPK");
                        else
                            SessionNo = "10";
                    }else
                        SessionNo = "10";
                }catch (SQLException se){
                    System.out.println("Error Updating the Session so Setting the Value to 10");
                    SessionNo = "10";
                }
            if(debug1)
                System.out.println("Before 3");
            }
            if(debug1)
            System.out.println("SessionID Found: " + SessionNo);
        }else{
            if(debug1)
            System.out.println("Session ID Not Found");
        }
    }catch (Exception e){
        System.out.println("Vauge exception somewhere in the session Setting Session to 10");
        SessionNo="10";
    }
    
    
    results[0] = "Not Used";
    results[1] = ""+PageNo;
    results[2] = ""+UserNo;
    results[3] = ""+jeo.getFormatDate();
    results[4] = "" +SessionNo ;
    results[5] = ""+MachineNo;
    if(debug2)
       displayFK(results);
    return results;
    
   }
   
   public static boolean addRecord(String[] cols, Connection con) throws SQLException, RecordRecordsException{
       ParsePosition pos = new ParsePosition(0);
       int i;
       int recordPK=0;
       SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss aa");
       String sqlUpdate1 = "INSERT INTO AccessRecords (userNo)" +
                           " VALUES ("+Integer.parseInt(cols[2])+")";
       String findPK = "SELECT RecordPK FROM AccessRecords Where PageNo=1  AND sessionPK=10 " +
                       "AND Machine=1 AND userNo="+Integer.parseInt(cols[2]);
       Statement stmt = con.createStatement();
       if(cols[5].equalsIgnoreCase("NA"))
           cols[5]="1";
       try{
           i = stmt.executeUpdate(sqlUpdate1);
           ResultSet rs = stmt.executeQuery(findPK);
           if(rs != null){
               while(rs.next()){
                   recordPK=rs.getInt("RecordPK");
               }
           }
           String sqlUpdate2 = "UPDATE AccessRecords SET PageNo="+Integer.parseInt(cols[1])+
                               " WHERE RecordPK="+recordPK+"";
           String sqlUpdate3 = "UPDATE AccessRecords SET sessionPK="+Integer.parseInt(cols[4])+
                               " WHERE RecordPK="+recordPK+"";
           String sqlUpdate4 = "UPDATE AccessRecords SET Machine="+Integer.parseInt(cols[5])+
                               " WHERE RecordPK="+recordPK+"";
       
           long lj=(sdf.parse(cols[3],pos)).getTime();
           String sqlUpdate5 = "UPDATE AccessRecords SET dateAsStringNo='"+lj+
                               "' WHERE RecordPK="+recordPK+"";
           String sqlUpdate6 = "UPDATE AccessRecords SET TimeString='"+sdf.format(new java.util.Date(lj))+
                               "' WHERE RecordPK="+recordPK+"";
       
           i = stmt.executeUpdate(sqlUpdate2);
           i = stmt.executeUpdate(sqlUpdate3);
           i = stmt.executeUpdate(sqlUpdate4);
           i = stmt.executeUpdate(sqlUpdate5);
           i = stmt.executeUpdate(sqlUpdate6);
           if(i==1)
               return true;
           else
               return false;
       }catch (SQLException se){
           System.out.println("Error Adding Record Throwing New RecordRecrodsException");
           throw new RecordRecordsException();
       }
       
   }
private static void displayFK(String[] s){
       for (int i = 0; i < 6 ; ++i){   
           System.out.println(""+i+": " + s[i]);
       }
   }

}
