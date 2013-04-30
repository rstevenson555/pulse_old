/*
 * ReportObject.java
 *
 * Created on April 26, 2001, 12:30 PM
 */



package logParser;
import java.io.*;
/**
 *
 * @author  i0360d3
 * @version 
 */
public class ReportObject implements ReportBuilder {

    private String _title;
    private String _fsn;
    private String _sql;
    private logParser.DataObject _do;
    private String _dependentLabel;
    private String _independentLabel; // 
    
    /** Creates new ReportObject */
    public ReportObject() {
        
    }
    public ReportObject(DataObject d) {
        _do = d;
    }
    
    public ReportObject(DataObject d,String fileName) {
        _do = d;
        _fsn = fileName;
    }


    public void writeToFile(java.io.PrintWriter pw) {
        String[] s;
        if(_do.isValidData()){
            for(int i =1 ; i<=_do.getDataSize(); ++i){
                s = _do.getNextPair();
                pw.println(""+s[0]+", "+s[1]);
            }
        }else{
            pw.println("Your dataObject does not represent valid data contact this products maintainer");
        }
        pw.flush();
        pw.close();
    }
    
    public void BuildExcelFile() {
    }
    
    public void BuildXMLFile() {
    }
    
    public void BuildHTMLFile() {
    }
    
    public void BuildEmailReport() {
    }
    
    public void BuildFormattedFile() {
    }
    
    public void BuildCSVFile() {
        PrintWriter pw = null;
        if(_fsn != null){
            try{
                pw =RecordRecords.getPrintWriter(_fsn,".csv");
            }catch(IOException ioe){
                ioe.printStackTrace();
            }
        }
        BuildCSVFile(pw);
    }

    public void BuildCSVFile(java.io.PrintWriter pw){
        writeToFile(pw);

    }
    public void BuildFormattedFile(java.io.PrintWriter pw){
    }
    public void BuildEmailReport(java.io.PrintWriter pw){
    }
    public void BuildHTMLFile(java.io.PrintWriter pw){
    }
    public void BuildXMLFile(java.io.PrintWriter pw){
    }
    public void BuildExcelFile(java.io.PrintWriter pw){
    }

    
}
