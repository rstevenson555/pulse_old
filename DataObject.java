/*
 * DataObject.java
 *
 * Created on April 26, 2001, 11:07 AM
 */

package logParser;
import java.util.*;

/**
 *
 * @author  i0360d3
 * @version 
 */
public class DataObject extends java.lang.Object {

    private logParser.IndependentDataObject _id;
    private logParser.DependentDataObject _dd;
    private LinkedList _LLdd;
    private int _currentPair =0;
    boolean containsMultipleDependentData = false;
    
    /** Creates new DataObject */
    public DataObject() {
    
    }
    public DataObject(IndependentDataObject i, DependentDataObject d) {
    
        _id = i;
    
        _dd = d;
        _LLdd = new LinkedList();
        _LLdd.addLast(d);
        _dd.mapToIDO(i.getHashMap());
    }
    public DataObject(IndependentDataObject i, DependentDataObject[] d) {
    
        _id = i;
        _LLdd = new LinkedList();
    
        _dd = d[0];
        for (int j=0;j<d.length;++j){
            _LLdd.addLast(d[j]);
        }
        _dd.mapToIDO(i.getHashMap());

    }
    int countDependentSets(){
        return _LLdd.size();
    }

    IndependentDataObject getIDO(){
        return _id;
    }
    
    DependentDataObject getDDO(){
        return _dd;
    }
    
    public void AddDependentData(DependentDataObject dd){
        _LLdd.addLast(dd);
        containsMultipleDependentData = true;
    }
    public boolean hasMultipleDD(){
        return containsMultipleDependentData;
    }
    
    public boolean isValidData() {
        if(_id.getCount() == _dd.getCount())  
            return true;
        else
            return false;
    }
    

    public int getDataSize() {
        if(isValidData())
            return _id.getCount();
        else
            return 0;
    }
    
    public String getFirstIndependentValue(){
        return _id.getMin();
    }
    
    
    public String getLastIndependentValue(){
        return _id.getMax();
    }
    

    public String[] getNextPair() {
        String id = (String)_id.getObject(new Integer(_currentPair));
        String dd = (String)_dd.getObject(new Integer(_currentPair));
        ++_currentPair;
        return new String[] {id,dd};
    }
    
    public String[] getNextSet(){
         String[] sa = new String[_LLdd.size() +1];
         sa[0] = (String)_id.getObject(new Integer(_currentPair));
         DependentDataObject[] dd = (DependentDataObject[])_LLdd.toArray();
         for(int i = 0; i<dd.length;++i){
             sa[i] = dd[i].getObject(new Integer(_currentPair));
         }
         return sa;

    }


    public String getDependentType() {
        return _dd.getType();
    }


    public String getIndependentType() {
        return _id.getType();
    }
    
    public void setCurrentPair(int i){
        _currentPair = i;
    }
    
    
    public int getCurrentPair(){
        return _currentPair;
    }
    protected String getDependent(int i){
        return (String)_dd.getObject(new Integer(i));
    }
    
    protected String[] getHeadings(){
        String[] headings = new String[_LLdd.size()];
        for(int i = 0;i<_LLdd.size();++i){
           headings[i]= ((DependentDataObject)_LLdd.get(i)).getHeading();

        }
        return headings;
        
    }
    protected String[] getDependentArray(int index) {
        String[] results = new String[_LLdd.size()];
        for(int i = 0;i<_LLdd.size();++i){
            results[i] =(String)(((DependentDataObject)_LLdd.get(i)).getObject(new Integer(index)));
        }
        return results;
        
        
    }
    
    protected void repair(Hashtable fixedIDO){    
        ListIterator li = _LLdd.listIterator();
        LinkedList IDODDOHashtables = new LinkedList();
        LinkedList repairedTables = new LinkedList();
        while(li.hasNext()){
            System.out.println("Building IDODDOHashtables;");
            Hashtable nht = new Hashtable();
            DependentDataObject ldd = (DependentDataObject)li.next();
            for(int i=1;i<_id.getCount();++i){
                System.out.println("Location 1");
                String key = _id.getObject(new Integer(i));
                System.out.println("Location 2");
                String value = ldd.getObject(new Integer(i));
                System.out.println("Location 3");
                nht.put(key,value);
            }
            IDODDOHashtables.addLast(nht);
        }
        li = IDODDOHashtables.listIterator();
        System.out.println("Out of createing first map");
        while(li.hasNext()){
            System.out.println("while in the repair phase");
            Hashtable nht = new Hashtable();
            Enumeration e = fixedIDO.elements();
            Hashtable liCurrent = (Hashtable)li.next();
            System.out.println("Starting for");
            for(int i = 1;i<fixedIDO.size();++i){
            System.out.println("for l1");
                String slk = ((String)fixedIDO.get(new Integer(i)));
            System.out.println("for l2");
                if(liCurrent.containsKey(slk)){
            System.out.println("for l3");
                    String val = (String)liCurrent.get(slk);
                    nht.put(new Integer(i), val);
            System.out.println("for l4");
                }else{
            System.out.println("for l5");
                    nht.put(new Integer(i),new Integer(0));
            System.out.println("for l6");
                }
            }
            System.out.println("for l7");
            repairedTables.addLast(nht);
            System.out.println("for l8");
        }
        _id.setData(fixedIDO);
        ListIterator testli = repairedTables.listIterator();
        //while(testli.hasNext(){
            
        //}
        _LLdd = repairedTables;
    } 
    
    Iterator getIteratorDependentData(){
        return _LLdd.iterator();
    }
    
    
}
