/*
 * QueryMacro.java
 *
 * Created on April 26, 2001, 3:44 PM
 */

package logParser;
import java.util.*;
import java.sql.*;
/**
 *
 * @author  i0360d3
 * @version 
 */
public class QueryMacro extends Object {
    Stack _qStack;
    Connection con;
    static ConnectionPoolT cp = null;
    
    static {
        try{
            cp = new ConnectionPoolT();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    /** Creates new QueryMacro */
    public QueryMacro() {
    }
    public QueryMacro(Stack s) {
        _qStack = s;
        
    }

    //This runs all the non-result set quiries in the stack
    //This needs to be cleaned up in the future to remove any tables created in the process;
    public void runNRSQueries()throws SQLException{
        while(_qStack.size() > 1){
            QueryObject qo = (QueryObject)_qStack.pop();
            QueryService.executeNRSQuery(qo.getSqlStatement(),qo.getParameters(),con);
        }
    }
    
    public ResultSet getRS()throws SQLException{
        con = cp.getConnection();
        runNRSQueries();
        ResultSet rs= null;
        if(_qStack.size() == 1){
            QueryObject qo = (QueryObject)_qStack.pop();
            rs =QueryService.executeRSQuery(qo.getSqlStatement(),qo.getParameters(),con);
        }       
        return rs;
    }
    
    
    public ReportObject getReportObject(){

/*
 
        IndependentDataObject ido = new IndependentDataObject();
        DependentDataObject ddo = new DependentDataObject();
        try{
            process(getRS(), ido,ddo);
        }catch (SQLException se){
            se.printStackTrace();
        }
*/      
        
        DataObject dataobject = getDataObject();
        return new ReportObject(dataobject);
    }
    
    public DataObject getDataObject(){
        IndependentDataObject ido = new IndependentDataObject();
        DependentDataObject ddo = new DependentDataObject();
        try{
            process(getRS(), ido,ddo);
        }catch (SQLException se){
            se.printStackTrace();
        }
        return new DataObject(ido,ddo);
        
    }
    
    
    public ReportObject getReportObject(String s){
        IndependentDataObject ido = new IndependentDataObject();
        DependentDataObject ddo = new DependentDataObject();
        try{
            process(getRS(), ido,ddo);
        }catch (SQLException se){
            se.printStackTrace();
        }
        DataObject dataobject = new DataObject(ido,ddo);
        return new ReportObject(dataobject,s);
    }

    
    
    void process(ResultSet rs,IndependentDataObject ido, DependentDataObject ddo) throws SQLException {
        System.out.println("Displaying the result set");
        ResultSetMetaData rsmd = rs.getMetaData();
        int colcount = rsmd.getColumnCount();
        String[] colnames = new String[colcount+1];
        int[] colTypes = new int[colcount+1];
         
        System.out.println("Starting to use the metadata");
        for(int i = 1; i<=colcount;++i){
            colnames[i] = rsmd.getColumnName(i);
            colTypes[i] = rsmd.getColumnType(i);
        }

        for(int i = 1; i<=colcount;++i){
            System.out.print(" " + colnames[i]);
        }
        
        System.out.println();
        int row = 1;
        while(rs.next()){
            ido.addObject(new Integer(row),getString(rs,1,colTypes[1]));
            ddo.addObject(new Integer(row),getString(rs,2,colTypes[2]));
            row++;
        }
    }
    
    String getString(ResultSet rs, int col, int colType) throws SQLException{
                 
                if(colType == java.sql.Types.INTEGER){
                    return ""+rs.getInt(col);
                }else if(colType == java.sql.Types.CHAR){
                    return ""+rs.getString(col);
                }else if(colType == java.sql.Types.DATE){
                    return ""+rs.getDate(col);
                }else if(colType == java.sql.Types.TIMESTAMP){
                    return ""+rs.getTimestamp(col);
                }else if(colType == java.sql.Types.VARCHAR){
                    return ""+rs.getString(col);
                }else if(colType == java.sql.Types.NUMERIC){
                    return ""+rs.getInt(col);
                }else {
                    //System.out.println("System Maintenance May be Required: You have obtained a Database column of java.sql.Types:" + colType);
                    return ""+rs.getString(col);
                }
    }

    
    public static void main(String[] args){
        System.out.println("java.sql.Types.ARRAY :" + java.sql.Types.ARRAY );
        System.out.println("java.sql.Types.BINARY :" + java.sql.Types.BINARY );
        System.out.println("java.sql.Types.BIT :" + java.sql.Types.BIT );
        System.out.println("java.sql.Types.BLOB :" + java.sql.Types.BLOB );
        System.out.println("java.sql.Types.CHAR :" + java.sql.Types.CHAR );
        System.out.println("java.sql.Types.CLOB :" + java.sql.Types.CLOB );
        System.out.println("java.sql.Types.DISTINCT :" + java.sql.Types.DISTINCT );
        System.out.println("java.sql.Types.FLOAT :" + java.sql.Types.FLOAT );
        System.out.println("java.sql.Types.JAVA_OBJECT :" + java.sql.Types.JAVA_OBJECT );
        System.out.println("java.sql.Types.LOGVARBINARY :" + java.sql.Types.LONGVARBINARY );
        System.out.println("java.sql.Types.LOGVARCHAR :" + java.sql.Types.LONGVARCHAR );
        System.out.println("java.sql.Types.OTHER :" + java.sql.Types.OTHER );
        System.out.println("java.sql.Types.REAL :" + java.sql.Types.REAL );
        System.out.println("java.sql.Types.REF :" + java.sql.Types.REF );
        System.out.println("java.sql.Types.SMALLINT :" + java.sql.Types.SMALLINT );
        System.out.println("java.sql.Types.STRUCT :" + java.sql.Types.STRUCT );
        System.out.println("java.sql.Types.TIME :" + java.sql.Types.TIME );
        System.out.println("java.sql.Types.VARBINARY :" + java.sql.Types.VARBINARY );
        System.out.println("java.sql.Types.INTEGER :" + java.sql.Types.INTEGER );
        System.out.println("java.sql.Types.NUMERIC :" + java.sql.Types.NUMERIC );
        System.out.println("java.sql.Types.DATE :" + java.sql.Types.DATE );
        System.out.println("java.sql.Types.DOUBLE :" + java.sql.Types.DOUBLE );
        System.out.println("java.sql.Types.TIMESTAMP :" + java.sql.Types.TIMESTAMP );
        System.out.println("java.sql.Types.VARCHAR :" + java.sql.Types.VARCHAR );
        System.out.println("java.sql.Types.NULL :" + java.sql.Types.NULL );
        System.out.println("java.sql.Types.TINYINT :" + java.sql.Types.TINYINT );
        System.out.println("java.sql.Types.BIGINT :" + java.sql.Types.BIGINT );
        
    }
    
    
    
}
