/*
 * IndependentDataObject.java
 *
 * Created on April 26, 2001, 10:50 AM
 */

package logParser;
import java.util.*;

/**
 *
 * @author  i0360d3
 * @version 
 */
public class IndependentDataObject extends java.lang.Object {

    private java.util.Hashtable _values;
    private String _type;
    private String _min;
    private String _max;
    private String _heading;
    private java.util.HashMap _hashMapValues;
    
    
    /** Creates new IndependentDataObject */
    public IndependentDataObject() {
        _hashMapValues = new HashMap();
        _values = new Hashtable();
        _type="na";
        _min="na";
        _max="na";
    }
    
    protected Stack getStackValues(){
        Stack ls = new Stack();
        for(int j=_values.size(); j>0;--j){
            ls.addElement(_values.get(new Integer(j)));
            
        }
        ls.addElement(_type);
        return ls;
    }

    
    public IndependentDataObject(Hashtable ht) {
        _values = ht;
        mapToHashMap();
    }
    
    
    public IndependentDataObject(Hashtable ht,String type) {
        _values = ht;
        _type = type;
        _max = (String)_values.get(new Integer(_values.size()));
        _min = (String)_values.get(new Integer(1));
        mapToHashMap();
    }

    public void addObject(Integer num,java.lang.String value) {
        _values.put(num,value);
        _hashMapValues.put(num,value);
    }
    
    public String getObject(Integer num) {
        return (String)_values.get(num);
    }

    public String getType() {
        return _type;
    }

    public void setType(java.lang.String type) {
        _type = type;
    }

    public String getHeading() {
        return _heading;
    }

    public void setHeading(java.lang.String heading) {
        _heading = heading;
    }

    public String getMax() {
        if(_max.equalsIgnoreCase("na"))
            findMaxMin();
        return _max;
    }

    public String getMin() {
        if(_min.equalsIgnoreCase("na"))
            findMaxMin();
        return _min;
    }
    
    public int getCount(){
        return _values.size();
    }
    
    private void findMaxMin(){
        _max = (String)_values.get(new Integer(_values.size()));
        _min = (String)_values.get(new Integer(1));
    }
    void setData(Hashtable ht){
        _values = ht;
        mapToHashMap();
    }
    
    
    void mapToHashMap(){
        _hashMapValues = new HashMap(_values);
    }
    
    Iterator getIterator(){
        return _hashMapValues.keySet().iterator();
    }
    
    HashMap getHashMap(){
        return _hashMapValues;
    }
        
}
