/*
 * MachineCentric.java
 *
 * Created on April 27, 2001, 9:44 PM
 */

package logParser;
import java.util.*;
import java.io.*;

/**
 *
 * @author  root
 * @version 
 */
public class MachineCentricReport implements ReportBuilder {

    Stack _sdataObj;
    String _fsn;
    String _title;
    

    private boolean isValid(){
        boolean valid = true;
        Enumeration e = _sdataObj.elements();
        DataObject ldo;
        ldo = (DataObject)e.nextElement();
        String firstValue = ldo.getFirstIndependentValue();
        String lastValue = ldo.getLastIndependentValue();
        int total = ldo.getDataSize();
        while(e.hasMoreElements()){
            ldo = (DataObject)e.nextElement();
            if(!ldo.getFirstIndependentValue().equalsIgnoreCase(firstValue) ||
               !ldo.getLastIndependentValue().equalsIgnoreCase(lastValue) ||
               ldo.getDataSize() != total)
                valid =false;
        }
        return valid;
    }
    
    private int getSize(){
        Enumeration e = _sdataObj.elements();
        DataObject ldo;
        ldo = (DataObject)e.nextElement();
        return ldo.getDataSize();
        
        
    }

    /** Creates new MachineCentric */
    public MachineCentricReport() {
    
    }
    
    public MachineCentricReport(Stack s, String st){
        _sdataObj = s;
        _fsn = st;
    }

    private LinkedList getDataQueue(){
        LinkedList rowQueue = null;
        System.out.println("MCR.getDataQueue() Location 1");
        if(isValid()){
        System.out.println("MCR.getDataQueue() Location 2");
            rowQueue = new LinkedList();
        System.out.println("MCR.getDataQueue() Location 3");
            rowQueue.addLast(getHeader());
        System.out.println("MCR.getDataQueue() Location 4");
            for(int i = 1;i<getSize();++i){        
        System.out.println("MCR.getDataQueue() Location 5");
                rowQueue.addLast(getNextRow(i));
            
            }
        }
        return rowQueue;
    }
    
    private String[] getHeader(){
        String[] rsa = new String[_sdataObj.size()];
        Enumeration e = _sdataObj.elements();

        int i=0;
        while(e.hasMoreElements()){
            rsa[i++] = (((DataObject)e.nextElement()).getDDO()).getHeading();
        }
        return rsa;
    }

    
    private String[] getNextRow(int i){
        String[] rsa = new String[_sdataObj.size()];
        Enumeration e = _sdataObj.elements();
        int j=0;
        while(e.hasMoreElements()){
            rsa[j++] = ((DataObject)e.nextElement()).getDependent(i);
        }
        return rsa;
    }
    
    
    public void BuildCSVFile(java.io.PrintWriter pw) {
        System.out.println("Starting BuildCSVfile from MachineCentric...");
        String[] rsa = null;
        System.out.println("Location 2");
        LinkedList dq = getDataQueue();
        System.out.println("Location 3");
        Enumeration e = _sdataObj.elements();
        System.out.println("Location 4");
        DataObject ldo;
        System.out.println("Location 5");
        ldo = (DataObject)e.nextElement();
        System.out.println("Location 6");
        Stack ivs = ldo.getIDO().getStackValues();
        System.out.println("Location 7");
        while(dq.size() > 0){
            System.out.println("while in BuildMachineCentric....");
            rsa = (String[])dq.removeFirst();
            pw.print((String)ivs.pop());
            for(int j=0;j<rsa.length;++j){
                pw.print(", " +rsa[j]);
            }
            pw.println();
        }
    }
    
    
    

    public void BuildFormattedFile(java.io.PrintWriter pw) {
    
    }
    
    public void BuildEmailReport(java.io.PrintWriter pw) {
    
    }
    
    public void BuildHTMLFile(java.io.PrintWriter pw) {
    
    }
    
    public void BuildXMLFile(java.io.PrintWriter pw) {
    
    }
    
    public void BuildExcelFile(java.io.PrintWriter pw) {
    
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
        //try{
            pw.flush();
            pw.close();
  
        //}catch(IOException ioe){
        //    ioe.printStackTrace();
        //}
    }
    
    public void BuildFormattedFile() {
    
    }
    
    public void BuildEmailReport() {
    
    }
    
    public void BuildHTMLFile() {
    
    }
    
    public void BuildXMLFile() {
    
    }
    
    public void BuildExcelFile() {
    
    }
    
}
