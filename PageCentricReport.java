/*
 * PageCentricReport.java
 *
 * Created on April 30, 2001, 9:44 AM
 */

package logParser;
import java.util.*;
import java.io.*;
import java.text.*;

/**
 *
 * @author  i0360d3
 * @version 
 */
public class PageCentricReport implements ReportBuilder {
    Stack _sdataObj;
    String _fsn;
    String _title;
    String _idoType;
    

    /** Creates new PageCentricReport */
    public PageCentricReport() {
    }

    public PageCentricReport(Stack s, String st){
        _sdataObj = s;
        _fsn = st;
    }

    private LinkedList getDependentDataObjectList(){
        Iterator li = _sdataObj.iterator();
        DataObject ldo ;
        LinkedList lLinkedList = new LinkedList();
        while(li.hasNext()){
            Iterator i = ((DataObject)li.next()).getIteratorDependentData();
            while(i.hasNext()){
                lLinkedList.addLast((DependentDataObject)i.next());
            }
        }
        return lLinkedList;
    }
    
    private Vector getVectorIDO(){
        Vector vido = null;
        Enumeration e = _sdataObj.elements();
        DataObject tldo = (DataObject)e.nextElement();
        if(Integer.parseInt(tldo.getIndependentType()) == java.sql.Types.DATE){
            _idoType= "Date";
        }
        if(Integer.parseInt(tldo.getIndependentType()) == java.sql.Types.VARCHAR){
            System.out.println("Fond a VARCHAR type");
            _idoType = "VARCHAR";
            DataObject ldo;
            IndependentDataObject ido;
            Enumeration le = _sdataObj.elements();
            vido = new Vector();
            while(le.hasMoreElements()){
                ldo = (DataObject)le.nextElement();
                ido = ldo.getIDO();
                vido.add(ido);
            } 
        }
        if(Integer.parseInt(tldo.getIndependentType()) == java.sql.Types.NUMERIC){
            System.out.println("Fond a Numeric type");
            _idoType = "NUMERIC";
            DataObject ldo;
            IndependentDataObject ido;
            Enumeration le = _sdataObj.elements();
            vido = new Vector();
            while(le.hasMoreElements()){
                ldo = (DataObject)le.nextElement();
                ido = ldo.getIDO();
                vido.add(ido);
            } 
        }
        return vido;
    }
    
    private Hashtable getMergedIDO(Vector v){
        Hashtable ht = new Hashtable();
        for (int i = 0; i<v.size(); ++i){
            IndependentDataObject lido=(IndependentDataObject)v.elementAt(i);
            for(int j=0;j<lido.getCount(); ++j){
                ht.put(lido.getObject(new Integer(j+1)),""+j);
            }
        }
        Enumeration e = ht.keys();
        Vector svec = new Vector();
        while(e.hasMoreElements()){
            svec.add(e.nextElement());
        }
        return sortVectorOfStringsIntoHashtable(svec);
    }

    
    private Hashtable sortVectorOfStringsIntoHashtable( Vector v){
        int i=1;
        Hashtable ht = new Hashtable();
        Iterator iter = v.iterator();
        //while(iter.hasNext()){
        
          while(iter.hasNext()){
            ht.put(new Integer(i),iter.next());
            ++i;
        }
        
        return ht;
    }
   
    
    
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

    private LinkedList getDataQueue(){
        LinkedList rowQueue = null;
        if(isValid()){
            rowQueue = new LinkedList();
            rowQueue.addLast(getHeader());
            for(int i = 1;i<getSize();++i){        
                rowQueue.addLast(getNextRow(i));
            
            }
        }
        return rowQueue;
    }
    
    
    private String[] getHeader(){
        //String[] rsa = new String[_sdataObj.size()];
        String[] rsa = new String[countCols()];
        Enumeration e = _sdataObj.elements();

        int i=0;
        int j=0;
        while(e.hasMoreElements()){
            DataObject ldo = ((DataObject)e.nextElement());
            String[] h = ldo.getHeadings();
            for(j =0;j<h.length;++j){
                rsa[j+i] = h[j]; //(((DataObject)e.nextElement()).getDDO()).getHeading();
            }
            i=i+j;
        }
        return rsa;
    }
    
    private int countCols(){
        Enumeration e = _sdataObj.elements();
        int i = 0;
        while(e.hasMoreElements()){
            i = i + (((DataObject)e.nextElement()).countDependentSets());
        }
        return i;
    }
    
    
    
    private String[] getNextRow(int i){

        
        String[] rsa = new String[countCols()];
        Enumeration e = _sdataObj.elements();

        int k=0;
        int j=0;
        while(e.hasMoreElements()){
            DataObject ldo = ((DataObject)e.nextElement());
            String[] h = ldo.getDependentArray(i);
            for(j =0;j<h.length;++j){
                rsa[j+k] = h[j]; //(((DataObject)e.nextElement()).getDDO()).getHeading();
            }
            k=k+j;
        }
        return rsa;
        
        
        /*String[] rsa = new String[_sdataObj.size()];
        Enumeration e = _sdataObj.elements();
        int j=0;
        while(e.hasMoreElements()){
            rsa[j++] = ((DataObject)e.nextElement()).getDependent(i);
        }
        return rsa;
         */
    }

    
    
    public void BuildCSVFile(java.io.PrintWriter pw) {
        LinkedList dq = getDependentDataObjectList();
        Hashtable htIDO = getMergedIDO(getVectorIDO());
        String[] headings = getHeader();
        pw.print(_idoType);
        for(int i=0;i<headings.length;++i){
            pw.print(", "+headings[i]);
        }
        pw.println();
        for(int i=1;i<=htIDO.size();++i){
            System.out.println((String)htIDO.get(new Integer(i)));
            Iterator iter = dq.iterator();
            pw.print((String) htIDO.get(new Integer(i)));
            while(iter.hasNext()){
                DependentDataObject ddo = (DependentDataObject)iter.next();
                pw.print(", "+ddo.getHMV((String) htIDO.get(new Integer(i))));
            }
            pw.println();
        }

        
        
        
        
/*        
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
 */
    }
    
    public void BuildFormattedFile(java.io.PrintWriter pw) {
    }
    
    public void BuildEmailReport(java.io.PrintWriter pw) {
    }
    
    public void BuildHTMLFile(java.io.PrintWriter pw) {
        LinkedList dq = getDependentDataObjectList();
        System.out.println("calling getMergedIDOj");
        Hashtable htIDO = getMergedIDO(getVectorIDO());
        System.out.println("Returning from getMergedIDO");
        String[] headings = getHeader();
        pw.println("<HTML>");
        pw.println("<HEAD><TITLE>PAGE CENTRIC REPORT STYLE  HTML</TITLE></HEAD>");
        pw.println("<TABLE border=2><TR><TD><TABLE>");

        pw.println("<TR>");

        pw.println("<TH>");
        
        pw.print(_idoType);
        
        pw.println("</TH>");

        for(int i=0;i<headings.length;++i){
            pw.print("<TH>"+headings[i]+"</TH>");
        }
        pw.println("</TR>");
        for(int i=1;i<=htIDO.size();++i){
            pw.println("<TR>");
//            System.out.println((String)htIDO.get(new Integer(i)));
            Iterator iter = dq.iterator();
            pw.print("<TD ALIGN=RIGHT>"+(String) htIDO.get(new Integer(i))+"</TD>");
            while(iter.hasNext()){
                DependentDataObject ddo = (DependentDataObject)iter.next();
                pw.print("<TD ALIGN=RIGHT>"+ddo.getHMV((String) htIDO.get(new Integer(i)))+"</TD>");
            }
            pw.println("</TR>");
        }
        pw.println("</TABLE></TD></TR></TABLE>");
        pw.println("</HTML>");


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
            pw.flush();
            pw.close();
    }
    
    public void BuildFormattedFile() {
    }
    
    public void BuildEmailReport() {
    }
    
    public void BuildHTMLFile() {
        PrintWriter pw = null;
        if(_fsn != null){
            try{
                pw =RecordRecords.getPrintWriter(_fsn,".html");
            }catch(IOException ioe){
                ioe.printStackTrace();
            }
        }
        BuildHTMLFile(pw);
            pw.flush();
            pw.close();
        
    }
    
    public void BuildXMLFile() {
    }
    
    public void BuildExcelFile() {
    }
    
}
