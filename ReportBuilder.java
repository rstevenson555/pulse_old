/*
 * ReportBuilder.java
 *
 * Created on April 27, 2001, 8:39 PM
 */

package logParser;

/**
 *This Interface contains method signatures and void returns for each of the 
 *various types of reports that can be built.  If the method is called without
 *a PrintWriter, a default PrintWriter is generated.
 *
 * @author  Bryce L. Alcock
 * @version 
 */
public interface ReportBuilder {
    

    public void BuildCSVFile(java.io.PrintWriter pw);
    public void BuildFormattedFile(java.io.PrintWriter pw);
    public void BuildEmailReport(java.io.PrintWriter pw);
    public void BuildHTMLFile(java.io.PrintWriter pw);
    public void BuildXMLFile(java.io.PrintWriter pw);
    public void BuildExcelFile(java.io.PrintWriter pw);
    public void BuildCSVFile();
    public void BuildFormattedFile();
    public void BuildEmailReport();
    public void BuildHTMLFile();
    public void BuildXMLFile();
    public void BuildExcelFile();

}

