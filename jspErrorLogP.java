/*
 * jspErrorLogP.java
 *
 * Created on March 19, 2001, 3:41 PM
 */

package logParser;
import edu.colorado.io.EasyReader;
import java.util.*;
import java.text.*;
import java.io.*;
/**
 *
 * @author  i0360d3
 * @version 
 */
public class jspErrorLogP extends java.lang.Object {

    private static SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss aa");

    /** Creates new jspErrorLogP */
    public jspErrorLogP() {
    }

    /**
    * @param args the command line arguments
    */
    public static void main (String args[]) {
        Vector _vLogs = new Vector();
        Vector _results = new Vector();
        jspErrorLogP jep = new jspErrorLogP();
                
        char delimiter = ',';
        int d1,d2,d3,d4,d5;
        EasyReader erin = new EasyReader(System.in);
        String sfile = erin.stringQuery("Enter the file Name -->");
        String fname = erin.stringQuery("Enter the Results File -> ");
        String userStartTime = erin.stringQuery("Enter Start Time MM/dd/yyyy hh:mm:ss aa -> ");
        String userEndTime = erin.stringQuery("Enter Start Time MM/dd/yyyy hh:mm:ss aa -> ");
        int  min = erin.intQuery("Enter the time increment in Min -> ");
        
        EasyReader er = new EasyReader(sfile);
        String s = er.stringInputLine();
        jspErrorObject jeo = new jspErrorObject(s);
        java.text.ParsePosition pos = new ParsePosition(0);
        //int min = 60;
        java.util.Date startTime = sdf.parse(userStartTime, pos);
        pos = new ParsePosition(0);
        java.util.Date endTime = sdf.parse(userEndTime, pos);
        
        long timeIncrement = 60*min*1000;
        long startTimeLong = startTime.getTime();
        long endTimeLong = endTime.getTime();
        Vector vPeriods = new Vector();
        long currentEnd = startTimeLong + timeIncrement;
        while(currentEnd < endTimeLong){
            vPeriods.addElement(new java.util.Date(currentEnd));
            currentEnd = currentEnd + timeIncrement;
        }
        Enumeration eTimes = vPeriods.elements();
        java.util.Date sDate = startTime;
        while(eTimes.hasMoreElements()){
            java.util.Date d = (java.util.Date)eTimes.nextElement();
            Stack stackOfLogs = jep.getStackOfLogs(er,d);
            _results.addElement(new userStats(stackOfLogs,sDate,d));
            sDate = d;
        }
        writeResults(fname, _results);
//        while(!er.isEOF()){
//           System.out.print(".");
//           s = er.stringInputLine();
//           _vLogs.addElement(new jspErrorObject(s));   
//        }
//        jspErrorLogP jep = new jspErrorLogP();
//        jep.getGroup(_vLogs,2,new java.util.Date());

    }

    private String getGroup(Vector vLogs, int minutes, java.util.Date StartTime) 
    {
        boolean goOn = true;
        jspErrorObject jeo;
        Date d = new Date();
        Enumeration elogs = vLogs.elements();
        while(elogs.hasMoreElements() && goOn)
        {
            jeo = (jspErrorObject)elogs.nextElement();
            d = jeo.getDate();
            System.out.println("Date "+ d.toString());
        }
        return d.toString();
        
    }
    private Stack getStackOfLogs(EasyReader er, java.util.Date date){
        Stack result = new Stack();
        jspErrorObject jeo = new jspErrorObject();
        do {
            if(!er.isEOF()){
            String nextLine = er.stringInputLine();
            jeo = new jspErrorObject(nextLine);
            result.addElement(jeo);
            }
        }while((date.compareTo(jeo.getDate()) >= 0) && !er.isEOF());

        return result;
    }
    private static void writeResults(String fileName, Vector results){
          
        FileOutputStream fsoResult;
        OutputStreamWriter oswResult;
        PrintWriter pwResult;
        userStats us;
        try{
            fsoResult = new FileOutputStream(fileName);
            oswResult = new OutputStreamWriter(fsoResult);
            pwResult = new PrintWriter(oswResult,true);
            Enumeration enumResults = results.elements();
            while(enumResults.hasMoreElements()){
         
                us = (userStats)enumResults.nextElement();
                StringBuffer sb = new StringBuffer();
                sb.append(sdf.format((java.util.Date)us.getStartTime()) +" , ");
                sb.append(sdf.format((java.util.Date)us.getEndTime()) +" , ");
                sb.append(""+us.getTotalRequests() +" , ");
                sb.append(""+us.getDistinctSessionRequests());
                pwResult.println(sb.toString());
            }
        }catch (Exception e){
            System.out.println("There was an Error");
            e.printStackTrace();
        }
 
    }

}
