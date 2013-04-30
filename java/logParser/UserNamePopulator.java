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
/**
 *
 * @author  i0360d3
 * @version 
 */

public class UserNamePopulator {
    
    public static final String SQL_USERNAME_SELECT = "SELECT * from users where COMPANY_NAME is NULL";
    public static final String SQL_USERNAME_COMPANY_SELECT = "SELECT inf.USER_KEY, inf.FIRST_NAME, "+
                                    "inf.MID_INITIAL, inf.LAST_NAME, cl.COMPANY_NAME from INFO inf, "+
                                    "authentication au , ACCOUNT_GROUP_LIST agl, COMPANY_LIST cl "+
                                    "where au.USER_KEY=? and inf.USER_KEY=au.USER_KEY "+
                                    "and au.account_group=agl.account_group and agl.company_id=cl.company_id";
    public static final String SQL_UPDATE_USER = "UPDATE USERS SET FULL_NAME=?, COMPANY_NAME=? " + 
                " where user_id=? ";

    /** Creates new UserNamePopulator */
    public UserNamePopulator() {
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
        
        Hashtable ht = new Hashtable();
        ResultSet rs;
        String UserNo;
        String UserName;
        Connection con;
        PreparedStatement pstmt;
        // get a Hashtable of all the usernames, and user_id (keys)
        try{
            con = getArtConnection();
            pstmt = con.prepareStatement(SQL_USERNAME_SELECT);
            rs = pstmt.executeQuery();
            if(rs != null){
                while (rs.next()) {
                    UserNo = ""+rs.getInt("User_ID");
                    UserName = rs.getString("USERNAME");
                    ht.put(UserNo, UserName);
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
        
        Enumeration enum2 = ht.keys();
        Connection conioet; 
        PreparedStatement pstmtioet;
        ResultSet rsioet;
        
        try{
            conioet = getIOETConnection();
            String fullname;
            String firstName;
            String midinitial;
            String lastname;
            String company;
            int i = 0;
            
            while (enum2.hasMoreElements()){
                String key = (String)enum2.nextElement();
                pstmtioet = conioet.prepareStatement(SQL_USERNAME_COMPANY_SELECT);
                pstmtioet.setString(1,(String)ht.get(key));
                rsioet =pstmtioet.executeQuery();
                if(rsioet !=null){
                    if(rsioet.next()){
                        firstName = rsioet.getString("FIRST_NAME");
                        midinitial=rsioet.getString("MID_INITIAL");
                        lastname=rsioet.getString("LAST_NAME");
                        if(firstName == null)
                            firstName=" ";
                        if(lastname == null)
                            lastname=" ";
                        if(midinitial == null)
                            midinitial=" ";
                        else
                            midinitial=midinitial.trim()+".";
                       fullname =  firstName.trim() + " " + 
                        midinitial.trim() + " " +
                        lastname.trim();
                       company = rsioet.getString("COMPANY_NAME");
                       
                        System.out.println(i+": " + fullname + "    " + company);
                        updateUsersInfo(key,fullname,company);
                    }
                }
                rsioet.close();
                rsioet = null;
                pstmtioet.close();
                pstmtioet = null;
                ++i;
            }
        }catch (SQLException se){
            System.out.println("SQL EXCEPTION in ioet");
        }

    }
    
    public static void updateUsersInfo(String key, String fullname, String company)throws SQLException{
        Connection artcon = getArtConnection();
        //System.out.println("Location 2");
        PreparedStatement pstm = artcon.prepareStatement(SQL_UPDATE_USER);
        //System.out.println("Location 3");
        pstm.setString(1,fullname);
        //System.out.println("Location 4");
        pstm.setString(2,company);
        //System.out.println("Location 5");
        pstm.setInt(3,Integer.parseInt(key));
        //System.out.println("Location 6");
        pstm.executeUpdate();
        //System.out.println("Location 7");
        pstm.close();
        //System.out.println("Location 8");
        pstm = null;
        //System.out.println("Location 9");
        artcon.close();
        //System.out.println("Location 10");
        artcon=null;
    }

}
