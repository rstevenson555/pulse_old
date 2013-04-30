/*
 * QueryObject.java
 *
 * Created on April 26, 2001, 3:38 PM
 */

package logParser;
import java.util.*;

/**
 *This class represents a Query To be executed.
 *it is very similar to a prepared statement in that it has
 *a Query, and a hashtable of parameters.
 *
 *
 * @author  Bryce L. Alcock
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
    
    /**
     *This creates a new query object with a sql string as a query, and a Hashtable ht
     *as a set of parameters.
     *@param sql  This is a string that represents an SQL query.
     *@param ht This is a Hashtable which represents the parameters to be used
     *to build the PreparedStatement of the Query.
     */
    public QueryObject(String sql, Hashtable ht) {
        _ht = ht;
        _Statement = sql;
    }
    
    /**
     *Get the SQL
     *@return a string representing the SQL.
     */
    public String  getSqlStatement(){
        return _Statement;
    }
    
    /**
     *Get the Parameters
     *@return a Hashtable representing the Parameters for the SQL.
     */
    public Hashtable getParameters(){
        return _ht;
    }
    
}
