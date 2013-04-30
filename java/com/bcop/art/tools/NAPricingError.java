/*
 * PricingError.java
 *
 * Created on June 18, 2002, 6:02 PM
 */

package com.bcop.art.tools;
import java.util.*;
import org.apache.oro.text.regex.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


/**
 *
 * @author  I0360D3
 * @version 
 */
public class NAPricingError implements DBObject {
    
    String loggerFormat = "EEE MMM dd HH:mm:ss:SSS zzz yyyy ";
    String mysqlTimestampFormat = "yyyyMMddHHmmss";
    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(loggerFormat);
    java.text.SimpleDateFormat sdfMySQL = new java.text.SimpleDateFormat(mysqlTimestampFormat);
    private static final String UPDATE_PRICINGERROR_ID = " UPDATE SequenceTable set count = LAST_INSERT_ID(count+1) " + 
                        " where SequenceName = \"NAPricing_Sequence\"";
    private static final String SELECT_PRICINGERROR_ID = " SELECT LAST_INSERT_ID() ";

    private static final String INSERT_NAPRICING = "INSERT into NAPricings (NAPricing_id, " +
                        " Event_Time, Class_Method, DataQueueSequence, NAPricing_Message, PricingMachine_IP, Pricing_Request, " +
                        " Pricing_Response, Request_Type, Detail_Error) VALUES (?,?,?,?,?,?,?,?,?,?) ";
    /** Creates new PricingError */
    public NAPricingError() {
    }
      int NAPricing_id;// INT UNSIGNED NOT NULL,
      Date Event_Time;// TIMESTAMP,
      String Class_Method;// VARCHAR(50),
      String DataQueueSequence;// VARCHAR(16),
      String NAPricing_Message;// VARCHAR(255),
      String PricingMachine_IP;
      String Pricing_Request;// TEXT,
      String Pricing_Response;// TEXT,
      String Request_Type;// VARCHAR(10),
      int Detail_Error;// INT(3)
/*  Date Time;
    String Class_Method;
    String Class_Process;
    String Error_Exception;
    String Exception_Message;
    String DataQueue_Sequence;
    String Machine_id;
  */  
    Hashtable ht = new Hashtable();
    
    public void updateToART(Connection con){
        try{
            NAPricing_id = getPrimaryKey(con);
            PreparedStatement pstmt = con.prepareStatement(INSERT_NAPRICING);
            pstmt.setInt(1,NAPricing_id);
            pstmt.setString(2,sdfMySQL.format(Event_Time));
            pstmt.setString(3,Class_Method);
            pstmt.setString(4,DataQueueSequence);
            pstmt.setString(5,NAPricing_Message);
            pstmt.setString(6,PricingMachine_IP);
            pstmt.setString(7,Pricing_Request);
            pstmt.setString(8,Pricing_Response);
            pstmt.setString(9,Request_Type);
            pstmt.setInt(10,Detail_Error);
            
            pstmt.execute();
            pstmt.close();
        }catch(SQLException e){
            e.printStackTrace();
        }
    }

    private int getPrimaryKey(Connection con) throws SQLException{
        PreparedStatement pstmt = con.prepareStatement(UPDATE_PRICINGERROR_ID);
        pstmt.execute();
        pstmt.close();
        pstmt = con.prepareStatement(SELECT_PRICINGERROR_ID);
        ResultSet rs = pstmt.executeQuery();
        if(rs.next()){
            return rs.getInt(1);
        }
        System.out.println("nextTraceID FAILED!!");
        return 0;
        
    }
    public void initialize(String s){
        Collection c = getReturnPatterns(s +"~");
        Iterator iter = c.iterator();
        int i = 0;
        
        while(iter.hasNext()){
            ht.put(new Integer(i),iter.next());
            ++i;
        }
      Event_Time = getTime((String)ht.get(new Integer(0)));
      Class_Method =getClassMethod((String)ht.get(new Integer(1)));
      DataQueueSequence =getSequece((String)ht.get(new Integer(3)));
      NAPricing_Message = getMessage((String)ht.get(new Integer(4)));
      PricingMachine_IP = getMachine((String)ht.get(new Integer(5)));
      Pricing_Request = getRequest((String)ht.get(new Integer(6)));
      Pricing_Response = getResponse((String)ht.get(new Integer(7)));
      
      
      Request_Type = getRequestType ((String)ht.get(new Integer(6)));
      
      Detail_Error = getDetailError ((String)ht.get(new Integer(4)));
    }
    private String getRequest(String s){
        return s;
    }
    private String getResponse(String s){
        return s;
    }
    private String getRequestType(String s){
        return s.substring(0,10);
    }
    
    private int getDetailError(String s){
        return Integer.parseInt(s.substring(s.indexOf("de")+2));
    }
    
    public String toString(){
        StringBuffer sb = new StringBuffer();
        sb.append(sdfMySQL.format(Event_Time));
        sb.append("\n\t");
        sb.append(Class_Method);
        sb.append("\n\t");
        sb.append(DataQueueSequence);
        sb.append("\n\t");
        sb.append(NAPricing_Message);
        sb.append("\n\t");
        sb.append(Request_Type);
        sb.append("\n\t");
        sb.append(Detail_Error);
        sb.append("\n\t");
        return sb.toString();
        
    }
    private Date getTime(String s){
        PatternCompiler compiler = new Perl5Compiler();
        PatternMatcher matcher = new Perl5Matcher();
        Pattern pattern = null;
        MatchResult result = null;
        PatternMatcherInput input = new PatternMatcherInput(s);
        try{
            pattern = compiler.compile("(.*? 200. )");
        }catch (MalformedPatternException e){
            return null;
        }
        String lString = null;
        if(matcher.contains(input, pattern)){
            result = matcher.getMatch();
            int groups = result.groups();
            lString = new String(result.group(1));
        }
        Date d = null;
        if(lString != null){
            try{
                d = sdf.parse(lString);
            }catch (java.text.ParseException e){
            }
        }
        System.out.println("Date : " +sdfMySQL.format(d) );
        return d;
        
    }
    private String getClassMethod(String s){
        return s;
    }
    private String getClassProcess(String s){
        PatternCompiler compiler = new Perl5Compiler();
        PatternMatcher matcher = new Perl5Matcher();
        Pattern pattern = null;
        MatchResult result = null;
        PatternMatcherInput input = new PatternMatcherInput(s);
        try{
            pattern = compiler.compile("(.*?\\(\\))");
        }catch (MalformedPatternException e){
            return null;
        }
        String lString = null;
        if(matcher.contains(input, pattern)){
            result = matcher.getMatch();
            int groups = result.groups();
            return new String(result.group(1));
        }
        return "Not Found";
        
    }
    private String getErrorExcpetion(String s){
        PatternCompiler compiler = new Perl5Compiler();
        PatternMatcher matcher = new Perl5Matcher();
        Pattern pattern = null;
        MatchResult result = null;
        PatternMatcherInput input = new PatternMatcherInput(s);
        try{
            pattern = compiler.compile("\\(\\)(.*)");
        }catch (MalformedPatternException e){
            return null;
        }
        String lString = null;
        if(matcher.contains(input, pattern)){
            result = matcher.getMatch();
            int groups = result.groups();
            return new String(result.group(1));
        }
        return "Not Found";
        
    }
    private String getMessage(String s){
        return s;
    }
    private String getSequece(String s){
        
        return s;
    }
    private String getMachine(String s){
        
        return s;
    }
    
    private Collection getReturnPatterns(String rawData){
        ArrayList list = new ArrayList();
        PatternCompiler compiler = new Perl5Compiler();
        PatternMatcher matcher = new Perl5Matcher();
        Pattern pattern = null;
        MatchResult result = null;
        PatternMatcherInput input = new PatternMatcherInput(rawData);
        try{
            pattern = compiler.compile("(.*?)~");
        }catch (MalformedPatternException e){
            return list;
        }
        int i = 0;
        while(matcher.contains(input, pattern)){
            result = matcher.getMatch();
            int groups = result.groups();
            list.add(new String(result.group(1)));
            ++i;
        }
        System.out.println("Matches found : " +i);
        return list;
    }

}