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
public class PricingError implements DBObject {
    
    String loggerFormat = "EEE MMM dd HH:mm:ss:SSS zzz yyyy ";
    String mysqlTimestampFormat = "yyyyMMddHHmmss";
    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(loggerFormat);
    java.text.SimpleDateFormat sdfMySQL = new java.text.SimpleDateFormat(mysqlTimestampFormat);
    private static final String UPDATE_PRICINGERROR_ID = " UPDATE SequenceTable set count = LAST_INSERT_ID(count+1) " + 
                        " where SequenceName = \"PricingError_Sequence\"";
    private static final String SELECT_PRICINGERROR_ID = " SELECT LAST_INSERT_ID() ";

    private static final String INSERT_PRICINGERROR = "INSERT into PricingErrors (PricingError_id, " +
                        " Error_Time, Class_Method, Class_Process, Error_Exception, Exception_Message, " +
                        " DataQueueSequence, PricingMachine_IP) VALUES (?,?,?,?,?,?,?,?) ";
    /** Creates new PricingError */
    public PricingError() {
    }
    Date Time;
    String Class_Method;
    String Class_Process;
    String Error_Exception;
    String Exception_Message;
    String DataQueue_Sequence;
    String Machine_id;
    
    Hashtable ht = new Hashtable();
    
    public void updateToART(Connection con){
        try{
            int i = getPrimaryKey(con);
            PreparedStatement pstmt = con.prepareStatement(INSERT_PRICINGERROR);
            pstmt.setInt(1,i);
            pstmt.setString(2,sdfMySQL.format(Time));
            pstmt.setString(3,Class_Method);
            pstmt.setString(4,Class_Process);
            pstmt.setString(5,Error_Exception);
            pstmt.setString(6,Exception_Message);
            pstmt.setString(7,DataQueue_Sequence);
            pstmt.setString(8,Machine_id);
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
        Time = getTime((String)ht.get(new Integer(0)));
        Class_Method = getClassMethod((String)ht.get(new Integer(0)));
        Class_Process = getClassProcess((String)ht.get(new Integer(1)));
        Error_Exception = getErrorExcpetion((String)ht.get(new Integer(1)));
        Exception_Message = getExceptionMessage((String)ht.get(new Integer(2)));
        DataQueue_Sequence = getSequece((String)ht.get(new Integer(3)));
        Machine_id = getMachine((String)ht.get(new Integer(4)));
        
    }
    
    public String toString(){
        StringBuffer sb = new StringBuffer();
        sb.append(sdfMySQL.format(Time));
        sb.append("\n\t");
        sb.append(Class_Method);
        sb.append("\n\t");
        sb.append(Class_Process);
        sb.append("\n\t");
        sb.append(Error_Exception);
        sb.append("\n\t");
        sb.append(Exception_Message);
        sb.append("\n\t");
        sb.append(DataQueue_Sequence);
        sb.append("\n\t");
        sb.append(Machine_id);
        sb.append("\n");
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
        PatternCompiler compiler = new Perl5Compiler();
        PatternMatcher matcher = new Perl5Matcher();
        Pattern pattern = null;
        MatchResult result = null;
        PatternMatcherInput input = new PatternMatcherInput(s);
        try{
            pattern = compiler.compile(".*? 200. (.*)");
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
    private String getExceptionMessage(String s){
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