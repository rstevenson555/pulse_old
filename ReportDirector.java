/*
 * ReportDirector.java
 *
 * Created on April 27, 2001, 8:46 PM
 */

package logParser;
import java.util.*;
/**
 * This class is the Director class in the Builder Design Pattern
 * See GOF PP 97-106 for more detail.
 * @author  Bryce L. Alcock
 * @version 
 */
public class ReportDirector extends java.lang.Object {

    /** Creates new ReportDirector */
    public ReportDirector() {
    }
    
    /**
     *This method steps through the Enumeration sr and Builds the CSV 
     *(Comma Separated Varriable) File
     *for each of the reports in the Enumeration.
     *@param sr is an Enumeration of all the <<ReportBuilder>> object to be built.
     *<dt><b>Precondition:</b><dd>  The Enumeration objects must all implement the 
     *ReportBuilder interface, and the BuildCSVReports method will determine the 
     *file name and location.
     *<dt><b>Postcondition:</b><dd> A CSV file will be generated and written to 
     *the approprite place on your harddrive for each ReportBuilder object in the 
     *Enummeration
     */
    public void BuildCSVReports(Enumeration sr){
        while(sr.hasMoreElements()){
            ((ReportBuilder)sr.nextElement()).BuildCSVFile();
        }
        
    }
    public void BuildHTMLReports(Enumeration sr){
        while(sr.hasMoreElements()){
            ((ReportBuilder)sr.nextElement()).BuildHTMLFile();
        }
        
    }

}
