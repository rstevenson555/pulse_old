/*
 * CVSRecord.java
 *
 * Created on June 4, 2002, 11:25 PM
 */
package com.bcop.art.tools;
import java.util.*;
import java.sql.*;
import org.apache.oro.text.regex.*;

/**
 * This class Records CVSEvents to the 
 * MySQL/ARTM Database.
 * @author  Bryce
 * @version 
 */
public class CVSRecord {
    static String loggerFormat = "EEE MMM dd HH:mm:ss:SSS zzz yyyy ";
    static String mysqlTimestampFormat = "yyyyMMddHHmmss";
    static java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(loggerFormat);
    static java.text.SimpleDateFormat sdfMySQL = new java.text.SimpleDateFormat(mysqlTimestampFormat);


    private java.util.Date eventTime;
    private int developerId;
    private int tagId;
    private Collection files;
    private String pathName;
    private Collection modifiedVersions;
    private Collection commitedVersions;
    private String logMessage;
    private String developerKey;
    private String tagName;
    private String BoiseRef;

    private static final String INSERT_CVSEVENT_MYSQL = "insert into CVSEvents " +
                        " (CVSEvent_id,Event_Time, developer_id, tag_id, FileName, PathName, Modified_Version, Commited_Version, LogMessage, Boise_Ref )" +
                        "Values (?,?,?,?,?,?,?,?,?,?) ";
    private static final String SELECT_DEVELOPER_ID = " select Developer_id from Developers where developer_key = ?";
    private static final String SELECT_CVSTAG_ID = " select Tag_id from CVSTAGS where Tag_Name = ?";
    private static final String INSERT_DEVELOPER_MYSQL = " insert into Developers (Developer_id,Developer_key) values (?,?) ";
    private static final String INSERT_CVSTAG_MYSQL = " insert into CVSTAGS " +
                        " (Tag_id, Tag_Name) values (?,?) ";
    private static final String UPDATE_CVSEVENT_ID = " UPDATE SequenceTable set count = LAST_INSERT_ID(count+1) " + 
                        " where SequenceName = \"CVSEvents_Sequence\"";
    private static final String UPDATE_DEVELOPER_ID = " UPDATE SequenceTable set count = LAST_INSERT_ID(count+1) " + 
                        " where SequenceName = \"CVSTag_Sequence\"";
    private static final String UPDATE_CVSTAG_ID = " UPDATE SequenceTable set count = LAST_INSERT_ID(count+1) " + 
                        " where SequenceName = \"Developers_Sequence\"";
    private static final String SELECT_LAST_ID = " SELECT LAST_INSERT_ID() ";
    /** Creates new CVSRecord */
    public CVSRecord() {
    }
    public CVSRecord(String date, 
            String key, 
            String tag, 
            Collection cfiles,
            Collection cmodifiedVersions,
            Collection ccommitedVersions,
            String message,
            String path) throws Exception{

        Connection con = getARTMConnection();
        
        eventTime = getEventTime(date);
        developerKey = key;
        developerId = getDeveloperId(developerKey,con);
        tagName = tag;
        tagId = getTagId(tagName,con);
        files = cfiles;
        pathName = getPath(path); 
        modifiedVersions = cmodifiedVersions;
        commitedVersions = ccommitedVersions;
        logMessage = message;
        BoiseRef = getRef(logMessage);
        con.close();
        con= null;
    }
    
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("[cvsEvent]").append("\n");
        sb.append("\t Time: ").append(sdfMySQL.format(eventTime)).append("\n");
        sb.append("\t User: ").append(developerKey).append("-id:").append(developerId).append("\n");
        sb.append("\t Tag: ").append(tagName).append("-id:").append(tagId).append("\n");
        Iterator ifiles = files.iterator();
        Iterator imodifiedVersions = modifiedVersions.iterator();
        Iterator icommitedVersions = commitedVersions.iterator();
        while(ifiles.hasNext() && imodifiedVersions.hasNext() && icommitedVersions.hasNext()){
            sb.append(ifiles.next()).append("~").append(imodifiedVersions.next()).append("~").append(icommitedVersions.next()).append("~");
        }
        sb.append("message: " ).append(logMessage).append("\n");
        return sb.toString();
    }
    
    private String getRef(String s) {
        PatternCompiler compiler = new Perl5Compiler();
        PatternMatcher matcher = new Perl5Matcher();
        Pattern pattern = null;
        MatchResult result = null;
        PatternMatcherInput input = new PatternMatcherInput(s);
        try{
            pattern = compiler.compile("\\[Ref:# (.*)?~\\]");
        }catch (MalformedPatternException e){
            return "Bad Ticket/Pattern";
        }
        String lString = null;
        if(matcher.contains(input, pattern)){
            result = matcher.getMatch();
            int groups = result.groups();
            return new String(result.group(1));
        }
        return "Not Entered Correctly";
    }
    private String getPath(String s) {
        PatternCompiler compiler = new Perl5Compiler();
        PatternMatcher matcher = new Perl5Matcher();
        Pattern pattern = null;
        MatchResult result = null;
        PatternMatcherInput input = new PatternMatcherInput(s);
        try{
            pattern = compiler.compile(".*?\\\\cvsroot(.*)");
        }catch (MalformedPatternException e){
            return "Bad Path/Pattern";
        }
        String lString = null;
        if(matcher.contains(input, pattern)){
            result = matcher.getMatch();
            int groups = result.groups();
            return new String(result.group(1));
        }
        return "No Path Found";
    }

    private int getDeveloperId(String s, Connection con)throws Exception {
        PreparedStatement pstmt = con.prepareStatement(SELECT_DEVELOPER_ID);
        pstmt.setString(1,s);
        ResultSet rs = pstmt.executeQuery();
        try{
            if(rs.next()){
                //System.out.println("Result set has next for developer id");
                return rs.getInt("Developer_id");
            }else{
                //System.out.println("Result set does not have next for developer id");
                addDeveloper(s, con);
                return getDeveloperId(s,con);
            }
        }catch(Exception e){
            e.printStackTrace();

        }finally{
            rs.close();
            pstmt.close();
        }
        return 0;
        
    }

    private int getTagId(String s, Connection con)throws Exception {
        PreparedStatement pstmt = con.prepareStatement(SELECT_CVSTAG_ID);
        pstmt.setString(1,s);
        ResultSet rs = pstmt.executeQuery();
        try{
            if(rs.next()){
                return rs.getInt("Tag_id");
            }else{
                addTag(s, con);
                return getTagId(s,con);
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally{
            rs.close();
            pstmt.close();
        }
        return 0;
    }

    private void addDeveloper(String s, Connection con)throws Exception {
        standardForeignKeyAddition(UPDATE_DEVELOPER_ID,INSERT_DEVELOPER_MYSQL, s, con);

    }

    private void addTag(String s, Connection con)throws Exception {
        standardForeignKeyAddition(UPDATE_CVSTAG_ID,INSERT_CVSTAG_MYSQL, s, con);
    }


    private void standardForeignKeyAddition(String update, String insert, String value, Connection con)throws Exception {
        PreparedStatement pstmt = con.prepareStatement(update);
        pstmt.execute();
        pstmt.close();
        pstmt = con.prepareStatement(SELECT_LAST_ID);
        ResultSet rs = pstmt.executeQuery();
        int id = 0;
        if(rs.next()){
            id = rs.getInt(1);
        }
        rs.close();
        pstmt.close();
        pstmt = con.prepareStatement(insert);
        pstmt.setInt(1,id);
        pstmt.setString(2,value);
        pstmt.execute();
        pstmt.close();
    }

    private java.util.Date getEventTime(String s)throws Exception {
            try{
                return sdfMySQL.parse(s);
            }catch (java.text.ParseException e){
            }
            return new java.util.Date();
    }
    
    public boolean updateToART() throws SQLException, 
                                    ClassNotFoundException, 
                                    InstantiationException, 
                                    IllegalAccessException {
        Connection con = getARTMConnection();
        Iterator ifiles = files.iterator();
        Iterator imodifiedVersions = modifiedVersions.iterator();
        Iterator icommitedVersions = commitedVersions.iterator();
        while(ifiles.hasNext() && imodifiedVersions.hasNext() && icommitedVersions.hasNext()){
            PreparedStatement pstmt = con.prepareStatement(INSERT_CVSEVENT_MYSQL);

            try{
                pstmt.setInt(1,getEventId(con));
                pstmt.setString(2,sdfMySQL.format(eventTime));
                pstmt.setInt(3,developerId);
                pstmt.setInt(4,tagId);
                pstmt.setString(5,(String)ifiles.next());
                pstmt.setString(6,pathName);
                pstmt.setString(7,(String)imodifiedVersions.next());
                pstmt.setString(8,(String)icommitedVersions.next());
                pstmt.setString(9,logMessage);
                pstmt.setString(10,BoiseRef);
                pstmt.execute();
            }catch(Exception e){
                e.printStackTrace();
            }
            pstmt.close();
        }
        con.close();
        return true;
    }
    int getEventId(Connection con) throws Exception {
        PreparedStatement pstmt = con.prepareStatement(UPDATE_CVSTAG_ID);
        pstmt.execute();
        pstmt.close();
        pstmt = con.prepareStatement(SELECT_LAST_ID);
        ResultSet rs = pstmt.executeQuery();
        try{
            if(rs.next()){
                return rs.getInt(1);
            }   
        }finally{
            rs.close();
            pstmt.close();
        }

        return 0;
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
    
    
    
    
    public static void main(String args[]) throws Exception{
        ArrayList lfiles = new ArrayList();
        ArrayList cmversions = new ArrayList();
        ArrayList ccomVersions = new ArrayList();
        
        java.util.Date d = new java.util.Date();
        lfiles.add("TimingMath.java");
        cmversions.add("1.1.2.1");
        ccomVersions.add("1.1.2.2");
        lfiles.add("TimingSubtract.java");
        cmversions.add("1.1.2.1 bla");
        ccomVersions.add("1.1.2.2 bla");
        
        CVSRecord cvsr = new CVSRecord(sdfMySQL.format(d),"i0360b6","Release5_PBF2",lfiles,
                        cmversions, ccomVersions, " new additions to the logging math so [Ref:# OP1234~] that the math operations \n can be chained together", "f:\\mount_point\\dev_nt_e\\jupiter\\boise\\dev\\cvsroot/boiseop/html/boiseop/stylesheets/shop");
        //System.out.println(cvsr.toString());
        cvsr.updateToART();
    }
        

}
