
package logParser.Tools;

import java.util.*;
import java.sql.*;
import logParser.*;
public class PercentileTool {
    TreeMap pagesTree =new TreeMap();


    private static final String DAILY_LOAD_TIME_QUERY = "Select LOADTIME from accessrecords where" +
                         " time > to_date(?,'yyyymmddhh24mi') and time< to_date(?,'yyyymmddhh24mi') ";
    private static final String DAILY_DISTINCT_PAGE_ID_QUERY = "Select distinct (page_id) " +
                         " from accessrecords where " +
                         " time > to_date(?,'yyyymmddhh24mi') and time< to_date(?,'yyyymmddhh24mi') ";


    public static void main(String args[]) throws SQLException{
        long curTime = System.currentTimeMillis();

        PercentileTool pt = new PercentileTool();
        if(args != null){
            System.out.println("NineteithPercentile: " 
                    + pt.getNinetiethPercentile(args[0]));
        }else{
            System.out.println("you really suck");
        }
        System.out.println("Results in : " + ((System.currentTimeMillis() - curTime)/1000));
    }

    public int getNinetiethPercentile(String sDate) throws SQLException{
            TreeMap c = getDailyLoadtimes(sDate);
            int elementCount = getElementCount(c);
            int nineteithPercentileValue = getPercentileValue(c,(int)(.9*elementCount));
            return nineteithPercentileValue;
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


    public TreeMap getDailyLoadtimes(String sDate) throws SQLException{
        Connection con = getArtConnection();
        PreparedStatement pstmt = con.prepareStatement(DAILY_LOAD_TIME_QUERY);
        pstmt.setString(1,sDate.trim()+"0001");
        pstmt.setString(2,sDate.trim()+"2359");
        ResultSet rs = pstmt.executeQuery();
        rs.setFetchSize(5000);
        int count = 0;
        TreeMap tm = new TreeMap();
        while(rs.next()){
            int l = rs.getInt("LOADTIME");
            addElement(tm,new Integer(l));
            ++count;
        }
        System.out.println("Count = " + count);
            return tm;
    }

    private void addElement(TreeMap tm, Integer l){
        Integer cl = (Integer)tm.get(l);
        if(cl != null){
            int i = cl.intValue();
            i +=1;
            tm.put(l,new Integer(i));
        }else{
            tm.put(l,new Integer(1));
        }
    }
    private int getElementCount(TreeMap t){
        Collection cc = t.keySet();
        Iterator iter = cc.iterator();
        int total = 0;
        while(iter.hasNext()){
            Object ll = iter.next();
            total += ((Integer)t.get(ll)).intValue();
        }
        return total;
    }
    private int getPercentileValue(TreeMap t,int i){
        Collection cc = t.keySet();
        Iterator iter = cc.iterator();
        int total = 0;
        Object ll = new Integer(0);
        while(iter.hasNext() && total < i){
            ll = iter.next();
            total += ((Integer)t.get(ll)).intValue();
        }
        return ((Integer)ll).intValue();
    }
}
