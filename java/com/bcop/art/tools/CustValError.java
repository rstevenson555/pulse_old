/*
 * CustValError.java
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
public class CustValError implements DBObject {
    private static final PatternCompiler compiler = new Perl5Compiler();
    
    private static Pattern DATE_PATTERN = null;// = compiler.compile("(.*? 200. )"); 
    private static Pattern DATE_GROUP_PATTERN= null;// = compiler.compile(".*? 200. (.*)");
    private static Pattern METHOD_BRACE_PATTERN= null;//  = compiler.compile("(.*?\\(\\))");
    private static Pattern POST_METHOD_BRACE_PATTERN=null;// = compiler.compile("\\(\\)(.*)");
    private static Pattern DID_WRITE_PATTERN=null;// = compiler.compile("(.*?(true|false))");
    private static Pattern POST_TF_PATTERN=null;// = compiler.compile(".*?(true|false)(.*)");
    private static Pattern SEQUENCE_PATTERN=null;// = compiler.compile("(...........?[0123456789])(.*)");
    private static Pattern TOKEN_PATTERN = null;//compiler.compile("(.*?)~");
    static {
        try{
            DATE_PATTERN = compiler.compile("(.*? 200. )"); 
            DATE_GROUP_PATTERN = compiler.compile(".*? 200. (.*)");
            METHOD_BRACE_PATTERN  = compiler.compile("(.*?\\(\\))");
            POST_METHOD_BRACE_PATTERN = compiler.compile("\\(\\)(.*)");
            DID_WRITE_PATTERN = compiler.compile("(.*?(true|false))");
            POST_TF_PATTERN = compiler.compile(".*?(true|false)(.*)");
            SEQUENCE_PATTERN = compiler.compile("(...........?[0123456789])(.*)");
            TOKEN_PATTERN = compiler.compile("(.*?)~");
        }catch(Exception e){
            e.printStackTrace();
        }
    }


    String loggerFormat = "EEE MMM dd HH:mm:ss:SSS zzz yyyy ";
    String mysqlTimestampFormat = "yyyyMMddHHmmss";
    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(loggerFormat);
    java.text.SimpleDateFormat sdfMySQL = new java.text.SimpleDateFormat(mysqlTimestampFormat);
    private static final String UPDATE_PRICINGERROR_ID = " UPDATE SequenceTable set count = LAST_INSERT_ID(count+1) " + 
                        " where SequenceName = \"CustValError_Sequence\"";
    private static final String SELECT_PRICINGERROR_ID = " SELECT LAST_INSERT_ID() ";

    private static final String INSERT_PRICINGERROR = "INSERT into CustValErrors (PricingError_id, " +
                        " Error_Time, Class_Method, Class_Process, Error_Exception, Exception_Message, " +
                        " DataQueueSequence, PricingMachine_IP) VALUES (?,?,?,?,?,?,?,?) ";
    /** Creates new CustValError */
    public CustValError() {
    }
    Date Time;
    String Class_Method;
    String Class_Process;
    String Error_Exception;
    String Exception_Message;
    String DataQueue_Sequence;
    String Machine_id;
    String Person_Name;
    
    Hashtable ht = new Hashtable();
    
    public void updateToART(Connection con){
        /*
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
         */
        System.out.println("Bingo Baby: " + this.toString());
        
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
        if(c.size() == 4 ){
            DataQueue_Sequence = getSequece((String)ht.get(new Integer(2)));
            //Person_Name  = getName((String)ht.get(new Integer(2)));
            Machine_id = getMachine((String)ht.get(new Integer(3)));
            Exception_Message=Error_Exception;
        }
        if(c.size() == 5 ){
            Exception_Message = getExceptionMessage((String)ht.get(new Integer(2)));
            DataQueue_Sequence = getSequece((String)ht.get(new Integer(3)));
            //Person_Name  = getName((String)ht.get(new Integer(3)));
            Machine_id = getMachine((String)ht.get(new Integer(4)));
        }
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
        sb.append(Person_Name);
        sb.append("\n");
        return sb.toString();
        
    }
    private Date getTime(String s){
        PatternCompiler compiler = new Perl5Compiler();
        PatternMatcher matcher = new Perl5Matcher();
        Pattern pattern = null;
        MatchResult result = null;
        PatternMatcherInput input = new PatternMatcherInput(s);

        pattern = DATE_PATTERN;//compiler.compile("(.*? 200. )");
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
        //System.out.println("Date : " +sdfMySQL.format(d) );
        return d;
        
    }
    private String getClassMethod(String s){
        PatternCompiler compiler = new Perl5Compiler();
        PatternMatcher matcher = new Perl5Matcher();
        Pattern pattern = null;
        MatchResult result = null;
        PatternMatcherInput input = new PatternMatcherInput(s);
        pattern = DATE_GROUP_PATTERN ;//compiler.compile(".*? 200. (.*)");
        String lString = null;
        if(matcher.contains(input, pattern)){
            result = matcher.getMatch();
            int groups = result.groups();
            return new String(result.group(1));
        }
        return "Not Found";
    }
    private String getClassProcess(String s){
        if(s != null){
            PatternCompiler compiler = new Perl5Compiler();
            PatternMatcher matcher = new Perl5Matcher();
            Pattern pattern = null;
            Pattern pattern2 = null;
            MatchResult result = null;
            PatternMatcherInput input = new PatternMatcherInput(s);
            pattern = METHOD_BRACE_PATTERN ;//compiler.compile("(.*?\\(\\))");
            pattern2 = DID_WRITE_PATTERN; // compiler.compile("(.*?(true|false))");
            String lString = null;
            if(matcher.contains(input, pattern)){
                result = matcher.getMatch();
                int groups = result.groups();
                return new String(result.group(1));
            }else if(matcher.contains(input, pattern2)){
                result = matcher.getMatch();
                int groups = result.groups();
                return new String(result.group(1));
            }
        }
        return "Not Found";
        
    }
    private String getErrorExcpetion(String s){
        if(s != null){
            PatternCompiler compiler = new Perl5Compiler();
            PatternMatcher matcher = new Perl5Matcher();
            Pattern pattern = null;
            Pattern pattern2 = null;
            MatchResult result = null;
            PatternMatcherInput input = new PatternMatcherInput(s);
            pattern = POST_METHOD_BRACE_PATTERN; //= compiler.compile("\\(\\)(.*)");
            pattern2 = POST_TF_PATTERN; // = compiler.compile(".*?(true|false)(.*)");
            String lString = null;
            if(matcher.contains(input, pattern)){
                result = matcher.getMatch();
                int groups = result.groups();
                return new String(result.group(1));
            }else if(matcher.contains(input,pattern2)){
                result = matcher.getMatch();
                int groups = result.groups();
                if (groups > 1){
                    return new String(result.group(2));
                }
            }
        }
        return "Not Found";
        
    }
    private String getExceptionMessage(String s){
        return s;
    }
    private String getSequece(String s){
        PatternCompiler compiler = new Perl5Compiler();
        PatternMatcher matcher = new Perl5Matcher();
        Pattern pattern = null;
        MatchResult result = null;
        PatternMatcherInput input = new PatternMatcherInput(s);
        pattern = SEQUENCE_PATTERN; // = compiler.compile("(...........?[0123456789])(.*)");
        String lString = null;
        if(matcher.contains(input, pattern)){
            result = matcher.getMatch();
            int groups = result.groups();
            if(groups > 1){
                Person_Name = new String(result.group(2));
            }
            return new String(result.group(1));
        }
        return "Not Found";
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
        pattern = TOKEN_PATTERN;//compiler.compile("(.*?)~");
        int i = 0;
        while(matcher.contains(input, pattern)){
            result = matcher.getMatch();
            int groups = result.groups();
            list.add(new String(result.group(1)));
            ++i;
        }
        //System.out.println("Matches found : " +i);
        return list;
    }

}
