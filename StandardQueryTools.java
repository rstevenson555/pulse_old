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
 * @author  i0360d3
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
        try{
            ConnectionPoolT cpt = new ConnectionPoolT();
            Connection con = cpt.getConnection();
            StandardQueryTools sqt = new StandardQueryTools();
            sqt.UpdateStandardQueriesToDB(con);
        }catch (SQLException se){
            se.printStackTrace();
        }catch (ClassNotFoundException cnfe){
            cnfe.printStackTrace();
        }
        
    }
    
    void UpdateStandardQueriesToDB(Connection con)throws SQLException{
        String[] SQNames = new String[1];
        String[] SQueries = new String[1];
        SQNames[0] = "QuarterHourlySession";
        SQueries[0] = LPConstants.MySQL_CreateQuarterHourlyResultSet;
        
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
                PreparedStatement psmt = con.prepareStatement(LPConstants.addQueries);
                psmt.setString(1,SQueries[i]);
                psmt.setInt(2,1);
                psmt.setString(3,SQNames[i]);
                int rc = psmt.executeUpdate();
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
        return sQuery_ID;
    }

}
