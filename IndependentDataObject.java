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
    
    /** Creates new IndependentDataObject */
    public IndependentDataObject() {
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
        return (String)_values.get(new Integer(_values.size()));
    }

    public String getMin() {
        return (String)_values.get(new Integer(1));
    }
    
    public int getCount(){
        return _values.size();
    }
    

}
