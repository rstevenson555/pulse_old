/*
 * userStats.java
 *
 * Created on March 20, 2001, 10:31 AM
 */

package logParser;
import java.util.*;

/**
 *
 * @author  i0360d3
 * @version 
 */
public class userStats extends java.lang.Object {

    private java.util.Date _startTime;
    private java.util.Date _endTime;
    private int _totalRequests;
    private int _distinctSessionRequests;
    
    /** Creates new userStats */
    public userStats() {
    }
    public static int countDistinct(java.util.Stack s){
       int Total =0;
       Enumeration es = s.elements();
       jspErrorObject jeo;
       HashSet hs = new HashSet();
       while(es.hasMoreElements()){
           jeo = (jspErrorObject)es.nextElement();
           if (!jeo.getAllowCharsInSession()){
               if(hs.add(new Long(jeo.getSessionValue()))){
                   ++Total;
               }else{
//                   System.out.println("Found Dup");
               }
           }else{
               if(hs.add(new String(jeo.getFullSessionID()))){
                   ++Total;
               }else{
//                   System.out.println("Found Dup");
               }
           }
       }
       return Total;
    }

    public userStats(java.util.Stack statsStack, java.util.Date ds, java.util.Date de) {
        _startTime = ds;
        _endTime = de;
        _totalRequests = statsStack.size();
        _distinctSessionRequests = countDistinct(statsStack);
//        System.out.println("finished userStats");
        System.out.println("Processing Time: "+_startTime);
//        System.out.println("Total Requests: "+ _totalRequests);
//        System.out.println("Distinct Requests: "+ _distinctSessionRequests);
        
    }
    public java.util.Date getStartTime(){
        return _startTime;
    }
    public java.util.Date getEndTime(){
        return _endTime;
    }
    public int getTotalRequests(){
        return _totalRequests;
    }
    public int getDistinctSessionRequests(){
        return _distinctSessionRequests;
    }

}
