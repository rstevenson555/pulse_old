/*
 * DistinctSessionData.java
 *
 * Created on March 30, 2001, 8:43 AM
 */

package logParser;
import graph.*;
import java.util.*;
import java.io.*;
/**
 *
 * @author  i0360d3
 * @version 
 */
public class DistinctSessionData extends java.lang.Object implements DataQueryObject {

    private Vector _data = new Vector();
    private String _CSVfile;
    /** Creates new DistinctSessionData */
    
    public DistinctSessionData() {
    }
    
    
    public DistinctSessionData(String file) {
        _CSVfile = file;
    }

    
    public void writeCSVData() {
        PrintWriter pw = null;
        try{
            pw = getPrintWriter(_CSVfile);
        }catch (IOException ioe){
            ioe.printStackTrace();
        }
        Enumeration e = _data.elements();
        while(e.hasMoreElements()){
            Enumeration lineE = (Enumeration) e.nextElement();
            while (lineE.hasMoreElements()){
                pw.print((String)lineE.nextElement());
            }
            pw.println("End of Line:");
            System.out.println(" Wrote a line to the CSV");
        }
        try{
        pw.flush();
        pw.close();
        }catch(Exception ex){
            System.out.println("Error flushing and closing the buffer");
        }
    }
    
    
    public DataSet getDataSet() {
        return new DataSet();
    }

    
    public void populateRow(String[] row){
        Vector v = new Vector();
        for(int i=0;i<row.length; ++i){
            v.addElement(row[i]);
        }
        _data.addElement(v.elements());
    }

    
    private PrintWriter getPrintWriter(String fileName) throws IOException{
        return new PrintWriter(new BufferedWriter(new FileWriter(fileName)));
    }

}
