/*
 * StackTrace.java
 *
 * Created on June 4, 2002, 11:25 PM
 */
package com.bcop.art.tools;
import java.util.*;
import java.sql.*;
/**
 *
 * @author  Bryce
 * @version 
 */
public class StackTrace {

    private static long beanContainerUpdateTime =0;
    private static long rowIDLookup = 0;
    private static long stackDetailTime = 0;
    private static long traceUpdateTime = 0;
    private static long traceIDTime =0;
    private String traceKey;
    private String message;
    private ArrayList stack;
    private String jspBeanContainer;
    private String traceTimestamp;
    private static final String INSERT_TRACE_MYSQL = "insert into StackTraces " +
                        " (Trace_id,Trace_Key,Trace_Message,Trace_Time) Values (?,?,?,?) ";
    private static final String SELECT_ROW_ID = " select Row_id from StackTraceRows where Row_Message = ?";
    private static final String INSERT_TRACEROW_MYSQL = " insert into StackTraceRows (Row_Message) values (?) ";
    private static final String INSERT_TRACEDETAIL_MYSQL = " insert into StackTraceDetails " +
                        " (Trace_id,Stack_Depth, Row_id) values (?,?,?) ";
    private static final String UPDATE_TRACE_ID = " UPDATE SequenceTable set count = LAST_INSERT_ID(count+1) " + 
                        " where SequenceName = \"Trace_Sequence\"";
    private static final String SELECT_TRACE_ID = " SELECT LAST_INSERT_ID() ";
    private static final String INSERT_JSPBEAN_CONTAINER = " INSERT INTO StackTraceBeanContainers " +
                        " (Trace_id,JspBeanContainer) Values (?,?) ";
    /** Creates new StackTrace */
    public StackTrace() {
        stack = new ArrayList();
    }
    
    public void setTraceKey(String s){
        traceKey = s;
    }
    public void setTraceTimestamp(String s){
        traceTimestamp = s;
    }
    public void setMessage(String s){
        message = s;
    }
    public void addStackElement(String s){
        stack.add(s);
    }
    public void setJSPBeanContainer(String s){
        jspBeanContainer = s;
    }
    public String toString(){
        StringBuffer sb = new StringBuffer();
        sb.append(message).append("\n\t").append(traceKey).append("\n\t");
        if(stack.size() > 0){
            sb.append(stack.get(0)).append("\n\t").append("StackDepth: " + stack.size());
            
            sb.append("\n\tJSPBeanContainer length: " + ((jspBeanContainer == null)?0:jspBeanContainer.length()));
        }
        return sb.toString();
    }
    
    public boolean updateToART() throws SQLException, 
                                    ClassNotFoundException, 
                                    InstantiationException, 
                                    IllegalAccessException {
        Connection con = getARTMConnection();
        long Time = System.currentTimeMillis();
        int currentTraceID = nextTraceID( con);
        traceIDTime += (System.currentTimeMillis() -Time );
        Time  = System.currentTimeMillis();
        
        PreparedStatement pstmt = con.prepareStatement(INSERT_TRACE_MYSQL);

        pstmt.setInt(1,currentTraceID);
        pstmt.setString(2,traceKey);
        pstmt.setString(3,message);
        pstmt.setString(4,traceTimestamp);
        //System.out.println(INSERT_TRACE_MYSQL +"  traceKey:"+traceKey+" message:"+message);
        pstmt.execute();
        pstmt.close();
        traceUpdateTime += (System.currentTimeMillis() -Time);
        
        Time = System.currentTimeMillis();
        Iterator iter = stack.iterator();
        int iStackDepth = 0;
        int rowID=0;
        try{
            while (iter.hasNext()){
                iStackDepth++;
                String currentRow = (String)iter.next();
                long Time2 = System.currentTimeMillis();
                rowID = getRowID(currentRow,con);
                rowIDLookup += (System.currentTimeMillis() - Time2);
                PreparedStatement pstmt2 = con.prepareStatement(INSERT_TRACEDETAIL_MYSQL);
                pstmt2.setInt(1,currentTraceID);
                pstmt2.setInt(2,iStackDepth);
                pstmt2.setInt(3,rowID);
                pstmt2.execute();
                pstmt2.close();
                pstmt2 = null;
            }
        }catch(SQLException e){
            System.out.println("SQL:" + INSERT_TRACEDETAIL_MYSQL);
            System.out.println("currentTraceID:" + currentTraceID);
            System.out.println("iStackDepth: " + iStackDepth);
            
            System.out.println("rowID:" + rowID);
            e.printStackTrace();
        }
        pstmt.close();
        stackDetailTime += (System.currentTimeMillis() - Time);
        Time = System.currentTimeMillis();
        updateBeanContainerToDB( con,currentTraceID );
        con.close();
        con = null;
        beanContainerUpdateTime += (System.currentTimeMillis() - Time);
        //System.out.println("iStackDepth:" + iStackDepth);
        return true;
    }
    
    public static  void resetTimers(){
        beanContainerUpdateTime =0;
        rowIDLookup = 0;
        stackDetailTime = 0;
        traceUpdateTime = 0;
        traceIDTime = 0;
    }
    public static long getBeanContainerUpdateTime(){
        return beanContainerUpdateTime;
    }
    public static long getRowIDLookupTime(){
        return rowIDLookup;
    }
    public static long getStackDatailTime(){
        return stackDetailTime;
    }
    public static long getTraceUpdateTime(){
        return traceUpdateTime;
    }
    public static long getTraceIDTime(){
        return traceIDTime;
    }

    public int getRowID(String rowString, Connection con) throws SQLException{
        PreparedStatement pstmt = con.prepareStatement(SELECT_ROW_ID);
        pstmt.setString(1,rowString);
        ResultSet rs = pstmt.executeQuery();
        if(rs.next()){
            return rs.getInt("Row_id");
        }else{
            addTraceRow(rowString, con);
            return getRowID(rowString,con);
        }
        //return 0;
    }

    public void addTraceRow(String rowString, Connection con) throws SQLException{
        PreparedStatement pstmt = con.prepareStatement(INSERT_TRACEROW_MYSQL);
        pstmt.setString(1,rowString);
        pstmt.execute();
        pstmt.close();
    }

    private int nextTraceID(Connection con )throws SQLException{
        PreparedStatement pstmt = con.prepareStatement(UPDATE_TRACE_ID);
        pstmt.execute();
        pstmt.close();
        pstmt = con.prepareStatement(SELECT_TRACE_ID);
        ResultSet rs = pstmt.executeQuery();
        if(rs.next()){
            return rs.getInt(1);
        }
        System.out.println("nextTraceID FAILED!!");
        return 0;
    }
    
    private void updateBeanContainerToDB(Connection con, int traceID ) throws SQLException {
        PreparedStatement pstmt = con.prepareStatement (INSERT_JSPBEAN_CONTAINER);
        pstmt.setInt(1,traceID);
        pstmt.setString(2,jspBeanContainer);
        pstmt.execute();
    }

    private Connection getARTMConnection() throws ClassNotFoundException, InstantiationException,
                                            IllegalAccessException, SQLException{

        String connectionURL = "jdbc:mysql://art-db1.int.bcop.com:3306/artm";
        String driverName = "org.gjt.mm.mysql.Driver";
        Class.forName(driverName).newInstance();
        Connection con = DriverManager.getConnection(connectionURL,"art_user","stream1");
        con.setAutoCommit(true);
        return con;
    }
}
