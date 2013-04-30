
package com.bos.art.logParser.broadcast.history;

import org.apache.log4j.Logger;
import java.sql.Timestamp;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.bos.art.logParser.broadcast.beans.AccessRecordsMinuteBean;
import com.bos.art.logParser.broadcast.beans.BeanBag;
import com.bos.art.logParser.broadcast.network.CommunicationChannel;

import com.bos.art.logParser.db.ConnectionPoolT;

public class AvgChartQueryBroadcast extends QueryBroadcast {

    private static final Logger logger = (Logger)Logger.getLogger(AvgChartQueryBroadcast.class.getName());

    private java.util.Date time;
    private String direction;
    private int points;
    private String precision;
    private org.jgroups.Message msg;

    public AvgChartQueryBroadcast(
            java.util.Date ltime,
            String ldirection,
            int lpoints,
            String lprecision,
            org.jgroups.Message lmsg
            ){
        time = ltime;
        direction = ldirection;
        points = lpoints;
        precision = lprecision;
        msg = lmsg;
    }

    public void run(){
        logger.warn("AvgChartQueryBroadcast Called for time >"+time.getTime() 
                + ": direction >"+ direction
                +": points>"+ points
                +": precision>"+ precision);
        if(precision !=null && precision.equals("min")){
            processMinuteData();
        }

    }


    private void processMinuteData(){
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyyMMddHHmm");

        String sql="select * from minutestatistics where time>? and time<?";
        long milliseconds = 1000*60*points;
        java.sql.Timestamp startTime;
        java.sql.Timestamp endTime;
        if(direction.equals("F")){
            startTime = new java.sql.Timestamp(time.getTime());
            endTime = new java.sql.Timestamp(time.getTime()+milliseconds);
        }else{
            startTime = new java.sql.Timestamp(time.getTime()-milliseconds);
            endTime = new java.sql.Timestamp(time.getTime());
        }
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        ArrayList list = new ArrayList();
        try{
            con = ConnectionPoolT.getConnection(); 
            pstmt = con.prepareStatement(sql);
            pstmt.setTimestamp(1,startTime);
            pstmt.setTimestamp(2,endTime);
            rs = pstmt.executeQuery();
            while(rs.next()){
               java.sql.Timestamp time = rs.getTimestamp("time");
               int totalLoads = rs.getInt("totalloads");
               int avgloadtime = rs.getInt("averageloadtime");
               int ninept      = rs.getInt("ninetiethpercentile");
    
               logger.warn("RS Record time:" 
                       + time +":totalloads:"
                       +totalLoads+":avgloadtime:"
                       +avgloadtime+":ninept:"
                       +ninept+":");
               String stime = sdf.format(time);
               AccessRecordsMinuteBean armb = new AccessRecordsMinuteBean(stime,ninept,avgloadtime,totalLoads);
               list.add(armb);
           }
       }catch(SQLException e){
           logger.warn("Exception AvgChartQueryBroadcast ",e);
       }finally{
           try{
               if(rs != null){
                   rs.close();
               }
               if(pstmt != null){
                   pstmt.close();
               }
               if(con != null){
                   con.close();
               }
           }catch(SQLException e){
               logger.warn("Exception Closing Some Connection, pstmt or rs.",e);
           }
       }
       BeanBag bag = new BeanBag();
       bag.setBeans(list);
        broadcast(bag);
       

    }

	private void broadcast(BeanBag bag) {
        try {
		    CommunicationChannel.getInstance().broadcast( bag, msg.getDest());
        }
        catch(Exception e)
        {
            logger.error("Error broadcasting data",e);
        }
	}
}
