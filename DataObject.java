/*
 * DataObject.java
 *
 * Created on April 26, 2001, 11:07 AM
 */

package logParser;

/**
 *
 * @author  i0360d3
 * @version 
 */
public class DataObject extends java.lang.Object {

    private logParser.IndependentDataObject _id;
    private logParser.DependentDataObject _dd;
    private int _currentPair =0;
    
    /** Creates new DataObject */
    public DataObject() {
    
    }
    public DataObject(IndependentDataObject i, DependentDataObject d) {
    
        _id = i;
    
        _dd = d;
    }

    IndependentDataObject getIDO(){
        return _id;
    }
    
    DependentDataObject getDDO(){
        return _dd;
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

}
