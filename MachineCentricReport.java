/*
 * MachineCentric.java
 *
 * Created on April 27, 2001, 9:44 PM
 */

package logParser;
import java.util.*;
import java.io.*;
import java.text.*;

/**
 *
 * @author  root
 * @version 
 */
public class MachineCentricReport implements ReportBuilder {

    Stack _sdataObj;
    String _fsn;
    String _title;
    String _idoType;
    /** Creates new MachineCentric */
    public MachineCentricReport() {
    
    }
    /**
     *Creates a new MachineCentricReport Object.
     *@param s Stack, this is a stack of DataObjects.
     *Note: DataObject has been changed and my now take multiple dependent
     *data objects in the _LLdd.  See DataObject for more detail.  Keep this
     *fact in mind while coding here.
     *@param st String  This is a string which will represent the prefix name.
     *for example if the ReportDirector direcects the building of a csv file,
     *and the prefix is YYZ, then the file name will be YYZCSV.csv.
     *<dt><b>Postcondition:</b><dd>  A new MachineCentricReport Object will 
     *be created with a Stack s of DataObjects and an output file prefix of st.
     */
    public MachineCentricReport(Stack s, String st){
        _sdataObj = s;
        _fsn = st;
    }
    /**
     *This static method is the intended way to create one of these reports for the 
     *standard Machine Centric Reports.  However, it can be extended for any of the
     *Machine Centric type of reports where the only two parameters are the yyyymmdd,
     *and Records where Records can be any string which represents the seconds Parameter.
     *@param Query This is the Query you wich to run it must take three parameters, where
     *the first parameter is MachineName from the Machines.MachineName table.column, a data
     *in the form of yyyymmdd as a char.  (ie. TO_CHAR(Date, 'yyyymmdd')=?). and the third
     *param can be any string you want, typically something to thin down the records
     *(i.e. Queries.QueryName=?).
     *@param bnas1 boolean run this for NAS1.
     *@param bnas3 boolean run this for NAS3.
     *@param bnas4 boolean run this for NAS4.
     *@param yyyymmdd, this is the day for which the query will be run.
     *@title this turns out to be the title heading of the report you generate.
     *(e.g. if you are generating a CSV report on 12/04/2002, and this parameter
     *is "BLA" then your filename will be BLA1204.csv
     *@param Records this is the last string used in the parameters passed to the
     *query.
     *<DD><B>NOTE:</b><dt>  This query is intended to be used to create a file, or
     *at least a result set.  you can do substatial damage to a production database with
     *a malformed query so if you don't know what you are doing, ask.
     */
    public static MachineCentricReport createReport(String Query,
                                                    boolean bnas1, 
                                                    boolean bnas3, 
                                                    boolean bnas4, 
                                                    String yyyymmdd, 
                                                    String title,
                                                    String Records){
        Stack sDataObject = new Stack();
        if(bnas1){
            Hashtable ht = new Hashtable();
            ht.put(new Integer(1),"NAS1");
            ht.put(new Integer(2),Records);
            ht.put(new Integer(3),yyyymmdd);
            System.out.println("Location 1");
            Stack sQueryObj = new Stack();
            System.out.println("Location 2");
            sQueryObj.add(new QueryObject(Query,ht));
            System.out.println("Location 3");
            QueryMacro qm = new QueryMacro(sQueryObj);
            System.out.println("Location 4"); 
            sDataObject.add(qm.getDataObject());
            System.out.println("Locaiton 4b");
            //hashData.add(sQueryObj);
            

        }
        
        if(bnas3){
            Hashtable ht = new Hashtable();
            System.out.println("Location 5");
            ht.put(new Integer(1),"NAS3");
            ht.put(new Integer(2),Records);
            ht.put(new Integer(3),yyyymmdd);
            System.out.println("Location 6");
            Stack sQueryObj = new Stack();
            sQueryObj.add(new QueryObject(Query,ht));
            System.out.println("Location 7");
            QueryMacro qm = new QueryMacro(sQueryObj);
            System.out.println("Location 8");
            sDataObject.add(qm.getDataObject());
        }
        
        if(bnas4){
            System.out.println("Location 9");
            Hashtable ht = new Hashtable();
            ht.put(new Integer(1),"NAS4");
            ht.put(new Integer(2),Records);
            ht.put(new Integer(3),yyyymmdd);
            System.out.println("Location 10");
            Stack sQueryObj = new Stack();
            System.out.println("Location 11");
            sQueryObj.add(new QueryObject(Query,ht));
            System.out.println("Location 12");
            QueryMacro qm = new QueryMacro(sQueryObj);
            System.out.println("Location 13");
            sDataObject.add(qm.getDataObject());
        }
        
        return new MachineCentricReport(sDataObject, title);
        
    }
    
    /**
     *This method determines if the Stack of DataObjects represents a valid 
     *stack.  If the Stack is not a valid stack, then the objects can either be 
     *reparied or thrown away.  <Br><Br>
     *Validity is established as follows:<BR>
     *1.  Do all of the different stack members represent the same query?<BR>
     *2.  Do all of the different stack members contian the same number and 
     *values for both dependent and independent varriables.<BR>
     *@return boolean true if the stacks are consistant and valid.
     *false if not.  However, if it passes 1, but fails on 2 from above, a call 
     *to repairStack will be made.  If the stack is repairable then a true will
     *be returned.
     *<dt><b>Postcondtion</b><dd> The stack will be vaild, repaired if needed, and
     *a value of true will be returned<BR>
     *If the stack was not valid and is not repairable, the false will be returned.
     *A return of false would indicate a glitch in the program that assembles the 
     *Stack
     */
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
    
    
    /**
     *This gets the size of the first DataObject in the set.  
     *@return int the size of the first DataObject in the set.
     *<dt><b>NOTE:</b><dd>  If the dataObject stack is valid, this
     *will be the size for all the dataobjects in the stack. If the
     *dataObject stack is not valid you should not be using this function.
     */
    private int getSize(){
        Enumeration e = _sdataObj.elements();
        DataObject ldo;
        ldo = (DataObject)e.nextElement();
        return ldo.getDataSize();
    }


    /**
     *This method gets a linked list of all the dependent dataobjects in this specific
     *ReportBuilder (MachineCentricReport).
     *@return LinkedList of DependentDataObjects in the MachineCentricReport. 
     */
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
    /**
     *This gets a vector list of all the IDO objects in the machine centric Report Object.
     *if the machineCentricReport is composed of multible DataObjects it will 
     *put these together.
     *@return Vector
     *This returns a vector of IndependentDataObjects.
     */
    private Vector getVectorIDO(){
        Vector vido = null;
        Enumeration e = _sdataObj.elements();
        DataObject tldo = (DataObject)e.nextElement();
        if(Integer.parseInt(tldo.getIndependentType()) == java.sql.Types.DATE){
            _idoType= "Date";
            System.out.println("Fond a Date type");
        }
        if(Integer.parseInt(tldo.getIndependentType()) == java.sql.Types.TIMESTAMP){
            System.out.println("Fond a Timestamp type");
            _idoType = "Timestamp";
            DataObject ldo;
            IndependentDataObject ido;
            Enumeration le = _sdataObj.elements();
            vido = new Vector();
            while(le.hasMoreElements()){
                System.out.println("Location 1");
                ldo = (DataObject)le.nextElement();
                System.out.println("Location 2");
                ido = ldo.getIDO();
                System.out.println("This IDO has " + ido.getCount());
                vido.add(ido);
            }
        }
        return vido;
    }
    
    

    
    /**
     *This takes a Vector of IndependentDataObjects, and merges them into one
     *independent data object. which represents all the values in the original
     *independent data object.
     *@param Vector is a vector of independent dataobjects it is assumed for the 
     *purpose of this method that the IDO is a type Date or Timestamp.  If it
     *is anything else you will have to add handling for the specific type of 
     *interest to you.
     *@return Hashtable where the objects are ordered with the earliest mapped to
     *the key new Integer(1) and the latest mapped to the key new Integer(ht.size()). 
     */ 
    private Hashtable getMergedIDO(Vector v){
        Hashtable ht = new Hashtable();
        for (int i = 0; i<v.size(); ++i){
            IndependentDataObject lido=(IndependentDataObject)v.elementAt(i);
            for(int j=0;j<lido.getCount(); ++j){
                ht.put(lido.getObject(new Integer(j+1)),"na");
            }
        }
        Enumeration e = ht.keys();
        Vector svec = new Vector();
        while(e.hasMoreElements()){
            svec.add(e.nextElement());
        }
        return sortVectorOfStringIntsIntoHashtable(svec);
    }
    
    /**
     *This method should be removed in the future when we implement
     *compare for most of these objects.  Then the data structures will
     *just use the specified sort.
     *@param Vector V this is the vector which represents the values which
     *are ints stored as strings to be sorted.
     *@return Hashtable where the objects are ordered with the earliest mapped to
     *the key new Integer(1) and the latest mapped to the key new Integer(ht.size()). 
     **/
    private Hashtable sortVectorOfStringIntsIntoHashtable( Vector v){
        int i=1;
        Hashtable ht = new Hashtable();
        while(v.size() >0){
            try{
                ht.put(new Integer(i),getSmallest(v));
            }catch (ParseException pe){
                pe.printStackTrace();
            }
            ++i;
        }
        
        return ht;
    }
    /**
     *This method gets the smallest integer from a vector of strings
     *which represent integers, and returns the value as a string, and 
     *also mutates the vector by removing the returned value from the vector.
     *@param v is a vector of strings which represent ints.
     *@return String is the string representation of the smallest string int in
     *the Vector.
     *<DD><b>Postcondition</B><DT> whatever value was returned is also removed from the
     *vector V 
     */
    private String getSmallest(Vector v) throws ParseException{
     int indexOfSmallest = 0;
     Date svalue=null;
     if(!((String)v.elementAt(0)).equalsIgnoreCase("null")){
         svalue = LPConstants.TimeStampFormat2.parse((String)v.elementAt(0));
     }else{
         //svalue=0;
     }
     for (int i=1;i<v.size(); ++i){
         if(!((String)v.elementAt(i)).equalsIgnoreCase("null")){
             if(!svalue.before(LPConstants.TimeStampFormat2.parse((String)v.elementAt(i)))){
                 indexOfSmallest = i;
                 svalue = LPConstants.TimeStampFormat2.parse((String)v.elementAt(i));
             }      
         }
     }
     return (String)v.remove(indexOfSmallest);
    }
    

    /**
     *This method returns the header information for a specific row of a DataObject.
     *@return This String[] represents the header information by row for this entire
     *set of DataObjects in the _sdataObj Stack.
     */
    private String[] getHeader(){
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

    
    /**
     *Build the CSV File for this Report.
     */
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
   }
    
    /**
     *This returns the number of dependent data sets in the MachineCentricReport Object.
     *@return int is the number of dependent data objects total in this MachineCentricReport
     *Object.
     */
    private int countCols(){
        Enumeration e = _sdataObj.elements();
        int i = 0;
        while(e.hasMoreElements()){
            i = i + (((DataObject)e.nextElement()).countDependentSets());
        }
        return i;
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
    
    
    /**
     *Build the CSV file for this report
     *<dt><b>NOTE:</b><dd>  This creates a default file name whose name 
     *is generated as _fsn + MMDD+ .csv <BR>
     *See the RecordRecords.getPrintWriter for more detail.<BR>
     *Note in the future the getPrintWriter method should be moved to the FileTool Class
     *which should be bundled into Utils package.  This comment should be removed at
     *said time.
     */
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
  
    
    ////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    //------------------------------------------------------------------------//
    //DEPRICATED API -- DEPRICATED API -- DEPRICATED API -- DEPRICATED API -- //
    //------------------------------------------------------------------------//
    //WARNING  THE METHODS BELOW ARE DEPRICATED, AND SHOULD BE PHASED OUT     //
    //THESE METHODS WERE USED IN A PREVIOUS DESIGN AND ARE NO LONGER VALID    //
    //ONCE TESTING OF THE CODE, WITHOUT THESE METHODS, IS COMPLETE AND        //
    //SUCCESSFUL, THESE METHODS WILL BE REMOVED.                              //
    ////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    
    /**
     *This method returns the next row of data from the Dataobjects as a String[].
     *<B>DEPRICATED!!</B>     
     *@param int i this int is the row of data to return.
     *@return String[] this is the array of strings which represents the specified
     *row of data.
     *<DD><B>DEPRICATED!!</B><DT>
     */
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
    }

        /**
        *<DD><B>DEPRICATED!!</B><DT>
       */ 
        boolean repair(){
        Enumeration e = _sdataObj.elements();
        DataObject tldo = (DataObject)e.nextElement();
        if(Integer.parseInt(tldo.getIndependentType()) == java.sql.Types.DATE)
            System.out.println("Fond a Date type");
        if(Integer.parseInt(tldo.getIndependentType()) == java.sql.Types.TIMESTAMP){
            System.out.println("Fond a Timestamp type");
            DataObject ldo;
            IndependentDataObject ido;
            Enumeration le = _sdataObj.elements();
            Vector vido = new Vector();
            while(le.hasMoreElements()){
                System.out.println("Location 1");
                ldo = (DataObject)le.nextElement();
                System.out.println("Location 2");
                ido = ldo.getIDO();
                System.out.println("This IDO has " + ido.getCount());
                vido.add(ido);
            }
            Hashtable lhtido = getMergedIDO(vido);
            ListIterator li = _sdataObj.listIterator();
            while(li.hasNext()){
                ((DataObject)li.next()).repair(lhtido);
                
            
            }
                
            
            for(int k=1;k<=lhtido.size();++k){
                System.out.println((String)lhtido.get(new Integer(k)));
            }
        }
        
        
        System.out.println("Repair was called !!!!!!!!!!!" +Integer.parseInt(((DataObject)e.nextElement()).getIndependentType()));
        return true;
    }
    
    
        /**
     *This produces a linked list of String[] which represent the
     *header information as the first element, then the rows of data
     *where it will contain a number of rows equal to getSiz().  Assuming 
     *that the data is valid.<B>DEPRICATED!!</B>
     *@return a LinkedList of String[] objects is returned.  The first String[]
     *represents the header information, and the the subsequent values represent
     *various rows of the data.  The String[]s will represent all of the _LLdd data
     *from all of the elements in the DataObject stack, not just the first dependent 
     *data Object.  If you want the first DependentDataObject you will have to write your
     *own method to do that.
     *<DD><B>DEPRICATED!!</B><DT>
     */
    private LinkedList getDataQueue(){
        LinkedList rowQueue = null;
        if(!isValid()){
            repair();
        }
        
//        if(isValid()){
            rowQueue = new LinkedList();
            rowQueue.addLast(getHeader());
            for(int i = 1;i<getSize();++i){        
                rowQueue.addLast(getNextRow(i));
            }
//        }
        return rowQueue;
    }
    
}
