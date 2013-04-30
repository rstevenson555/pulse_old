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
public class DependentDataObject extends java.lang.Object {

    private java.util.Hashtable _values;
    private String _type;
    private String _min;
    private String _max;
    private String _heading;
    private java.util.HashMap _hashMapValues;
    
    /** Creates new IndependentDataObject */
    public DependentDataObject() {
        _hashMapValues = new HashMap();
        _values = new Hashtable();
        _type="na";
        _min="na";
        _max="na";
 
    }
   
    public String getHeading(){
        return _heading;
    }
    
    public void setHeading(String heading){
        _heading = heading;
    }
    
    public DependentDataObject(Hashtable ht) {
        _values = ht;
    }

    public void addObject(Integer num,java.lang.String value) {
        _values.put(num,value);
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

    public String getMax() {
        return _max;
    }

    public String getMin() {
        return _min;
    }

    public int getCount(){
        return _values.size();
    }
    

    protected void mapToIDO(HashMap hm){
        Iterator iter = hm.keySet().iterator();
        while (iter.hasNext()){
            Integer key = (Integer)iter.next();
            String newKey = (String)hm.get(key);
            String value =(String)_values.get(key);
            if(value != null){
                
                _hashMapValues.put(newKey,value);
            }
        }
    }
    
    String getHMV(String s){
        return (String)_hashMapValues.get(s);
    }

}
