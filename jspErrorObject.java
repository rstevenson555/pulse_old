/*
 * jspErrorObject.java
 *
 * Created on March 19, 2001, 4:36 PM
 */

package logParser;
import edu.colorado.io.EasyReader;
import java.util.*;
import java.text.*;

/**
 *
 * @author  i0360d3
 * @version 
 */
public class jspErrorObject extends java.lang.Object {
    private static final String dip = "1.0.0.127";
    private static ParsePosition pos = new ParsePosition(0);
    private static final SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss aa");
    private static final SimpleDateFormat logFileFormat = new SimpleDateFormat("MM/dd/yyyy, hh:mm:ss aa");
    private static final SimpleDateFormat MySQLTimeStampFormat = new SimpleDateFormat("yyyyMMddHHmmss");
    private static final SimpleDateFormat TimeStampFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.000000000");
    private static int linesread = 0;
    private boolean _valid=false;

    private String _Page;
    private String _type;
    private String _sdate;
    private String _stime;
    private java.util.Date _utilDate;
    private String _userId;
    private String _fullSessionId;
    private String _IPaddress;
    private String _machine;
    private String _loadTime;
    private String _browser;
    private String _status;
    private boolean allowCharsInSession = false;
    
    /** Creates new jspErrorObject */
    public jspErrorObject() {
    }
    public jspErrorObject(String s) {

        StringTokenizer st = new StringTokenizer(s,"~");
        int tokens = st.countTokens();
        if(tokens ==8 || tokens ==9){
            ++linesread;
            _Page = st.nextToken();
            _type = st.nextToken();
            _sdate = st.nextToken();
            _userId = st.nextToken();
            _fullSessionId = st.nextToken();
            _IPaddress = st.nextToken();
            _machine = st.nextToken();
            _loadTime = st.nextToken();

            pos.setIndex(0);
        
            try{
                _utilDate = logFileFormat.parse(_sdate, pos);
            }catch (Exception e){
                System.out.println("Error Parsing*********************** "+ _sdate);
            }
            if(tokens==8){
                _valid = true;
            }else if(_Page.equalsIgnoreCase("docs/index")){
                _browser = st.nextToken();
                _valid = true;
            }
        }else{
           // System.out.println("******************  Ratical Error ******************");
        }
        
        if(LPConstants.MachineNameMethod.equalsIgnoreCase("LOCAL")){
            _machine = LPConstants.MachineName;          
        }else if(LPConstants.MachineNameMethod.equalsIgnoreCase("SYSTEM")){
            _machine = System.getProperty("_INIT_UTS_NODENAME");
        }else {}
        
        
     }
     
     public String getOracleCSVRecord(){
         StringBuffer sb = new StringBuffer();
         sb.append(_Page+"~").append(_status+"~").append(_sdate+", ")
           .append(_userId+"~")
           .append(_fullSessionId+"~").append(_IPaddress+"~")
           .append(_machine+"~").append(_loadTime+"~");
         return sb.toString();
     }
     
     public jspErrorObject(String s, String queueName, Hashtable ht){
         //Hashkeys should includ the following:
         //EVENT_type
         //EVENT_id
         //EVENT_appname
         //EVENT_servername
         //PAGE_name
         //PAGE_begin
         //DATE_chars
         //TIME_chars
         //USERINFO_sessionid
         //IP_chars
         //USERKEY_chars
         //ELAPSED_chars
    _Page= (String)ht.get("PAGE_name");
    _status= (String) ht.get("PAGE_begin");
    if(_status.equalsIgnoreCase("true"))
        _status="begin";
    else
        _status="end";
    _type=  (String)ht.get("EVENT_type");
    _sdate=  ((String)ht.get("DATE_CHARS")).trim();
    _stime=  ((String)ht.get("TIME_CHARS")).trim();
    //_utilDate ht.get("");
    _userId=  (String)ht.get("USERKEY_CHARS");
    _fullSessionId=  (String)ht.get("USERINFO_sessionid");
    _IPaddress=  (String)ht.get("IP_CHARS");
    _machine=  (String)ht.get("EVENT_servername");
    _loadTime=  (String)ht.get("ELAPSED_CHARS");
    _browser=  (String)ht.get("BROWSER_CHARS");
    _sdate=_sdate +", "+_stime;
    
    
    if(_Page!=null && 
               _sdate != null &&
               _stime != null &&
               _userId != null &&
               _fullSessionId != null &&
               _machine != null &&
               _loadTime != null
      )
        _valid=true;
    else
        _valid=false;
         
         
     }
     

     
     public java.util.Date getDate(){
         return _utilDate;
     }
     public boolean getAllowCharsInSession(){
         return allowCharsInSession;
     }
     public String getFullSessionID(){
         return _fullSessionId;
     }
     public String getUserID(){
         return _userId;
     }
     public String getPage(){
         return _Page;
     }
     public String getIPAddress(){
         return _IPaddress;
     }
     public String getFormatDate(){
        return sdf.format(_utilDate);
     }
     public String getTimeStampFormatDate(){
         //System.out.println("Getting s: "+_sdate.toString());
                     pos.setIndex(0);
        
            try{
                _utilDate = logFileFormat.parse(_sdate, pos);
            }catch (Exception e){
                System.out.println("Error Parsing*********************** "+ _sdate);
            }
          if(_utilDate == null){
              System.out.println("Null utilDate " + _sdate);
              System.out.println("session id: "+ _fullSessionId);
          }
         String s =TimeStampFormat.format(_utilDate); 
         //System.out.println("Got s: ");
        return s;
     }
     public String getSessionValue(){
         //This is not Working so Don't Use it
         return "10";   
     }
     public boolean isValid(){
         return _valid;
     }
     public String getStatus(){
        return _status;   
     }
     public String getMachine(){
        return _machine;
     }
     public String getLoadTime(){
        return _loadTime;
     }
     public String getBrowser(){
        return _browser;
     }

}









