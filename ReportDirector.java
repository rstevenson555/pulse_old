/*
 * ReportDirector.java
 *
 * Created on April 27, 2001, 8:46 PM
 */

package logParser;
import java.util.*;
/**
 *
 * @author  root
 * @version 
 */
public class ReportDirector extends java.lang.Object {

    /** Creates new ReportDirector */
    public ReportDirector() {
    }
    
    
    public void BuildCSVReports(Enumeration sr){
        while(sr.hasMoreElements()){
            ((ReportBuilder)sr.nextElement()).BuildCSVFile();
        }
        
    }

}
