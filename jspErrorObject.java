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
    private static final SimpleDateFormat MySQLTimeStampFormat = new SimpleDateFormat("yyyyMMddHHmmss");
    private static final SimpleDateFormat TimeStampFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.000000000");
    

    private String _Page;
    private String _sdate;
    private String _stime;
    private java.util.Date _utilDate;
    private String _userId;
    private String _fullSessionId;
    private String _IPaddress;
    private boolean allowCharsInSession = false;
    
    /** Creates new jspErrorObject */
    public jspErrorObject() {
    }
    public jspErrorObject(String s) {

        StringTokenizer st = new StringTokenizer(s,",");
        int tokens = st.countTokens();
        if(tokens >4){
            _Page = st.nextToken();
            _sdate = st.nextToken();
            _stime = st.nextToken();
            _userId = st.nextToken();
            _fullSessionId = st.nextToken();
            if(st.hasMoreTokens())
                _IPaddress = st.nextToken();
            else
                _IPaddress = dip;
            pos.setIndex(0);
            String sdate = _sdate + _stime;
        
            try{
                _utilDate = sdf.parse(sdate, pos);
            }catch (Exception e){
                System.out.println("Error Parsing "+ sdate);
            }
        }
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
         
        return TimeStampFormat.format(_utilDate);
     }
     public String getSessionValue(){
         //This is not Working so Don't Use it
         return "10";   
     }

}









