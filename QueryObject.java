/*
 * QueryObject.java
 *
 * Created on April 26, 2001, 3:38 PM
 */

package logParser;
import java.util.*;

/**
 *
 * @author  i0360d3
 * @version 
 */
public class QueryObject extends java.lang.Object {

    
    private String _Statement;
    private Hashtable _ht;
    String _DependentCol;
    String _IndependentCol;
    String _DepType;
    String _IndType;
    
    /** Creates new QueryObject */
    public QueryObject() {
    }
    public QueryObject(String sql, Hashtable ht) {
        _ht = ht;
        _Statement = sql;
    }
    public String  getSqlStatement(){
        return _Statement;
    }
    public Hashtable getParameters(){
        return _ht;
    }
    
    
    

}
