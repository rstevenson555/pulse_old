/*
 * QueryService.java
 *
 * Created on April 26, 2001, 12:48 PM
 */

package logParser;
import java.sql.*;
import java.util.*;

/**
 *
 * @author  i0360d3
 * @version 
 */
public class QueryService extends Object {

    /** Creates new QueryService */
    public QueryService() {
    }
    
    public static boolean executeNRSQuery(String Query, java.util.Hashtable hs, Connection con) throws SQLException{

        PreparedStatement pstmp = con.prepareStatement(Query);
        Enumeration ekeys = hs.keys();
        while(ekeys.hasMoreElements()){
            Integer inv = ((Integer)ekeys.nextElement());
            buildPS(pstmp,inv.intValue(), hs.get(inv));
        }
        int rows = pstmp.executeUpdate();
        pstmp.close();
        return true;
    }
    
    public static ResultSet executeRSQuery(String Query, java.util.Hashtable hs, Connection con) throws SQLException{
        //System.out.println("Location 1");
        PreparedStatement pstmp = con.prepareStatement(Query);
        //System.out.println("Location 2");
        Enumeration ekeys = hs.keys();
        //System.out.println("Location 3");
        if(ekeys != null){ 
        //System.out.println("Location 4");
            while(ekeys.hasMoreElements()){
        //System.out.println("Location 5");
                Integer inv = ((Integer)ekeys.nextElement());
                buildPS(pstmp,inv.intValue(), hs.get(inv));
            }
        }
        //System.out.println("Location 6");
            ResultSet rs = pstmp.executeQuery();
            
            return rs;
        
    }

    private static void buildPS(PreparedStatement ps, int location, Integer value) throws SQLException {
        ps.setInt(location,value.intValue());
    }
    private static void buildPS(PreparedStatement ps, int location, Long value) throws SQLException {
        ps.setLong(location,value.longValue());
    }
    private static void buildPS(PreparedStatement ps, int location, String value) throws SQLException {
        ps.setString(location,value.trim());
    }
    private static void buildPS(PreparedStatement ps, int location, java.sql.Date value) throws SQLException {
        ps.setDate(location,value);
    }
    private static void buildPS(PreparedStatement ps, int location, java.sql.Timestamp value) throws SQLException {
        ps.setTimestamp(location,value);
    } 
    private static void buildPS(PreparedStatement ps, int location, Object value) throws SQLException {
        if(value instanceof Integer)
            buildPS(ps,location,(Integer)value);
        else if(value instanceof Long)
            buildPS(ps,location,(Long)value);
        else if(value instanceof String)
            buildPS(ps,location,(String)value);
        else if(value instanceof java.sql.Date)
            buildPS(ps,location,(java.sql.Date)value);
        else if(value instanceof java.sql.Timestamp)
            buildPS(ps,location,(java.sql.Timestamp)value);
        else 
            System.out.println("We need to add a type in the QueryService Class");
    } 
    
}
