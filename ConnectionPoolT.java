package logParser;

import java.sql.*;
import java.util.*;

/**
 * A class which maintains a pool of database connection objects.
 */
public class ConnectionPoolT {
  private final int CONNECTIONS = 6;
  private static Hashtable _hashConnections = null;
  public static final boolean type4Driver = false;
  private String driverName;
  private String connectionURL;
  

  /**
   * Creates a ConnectionPool object by taking the URL of the database
   * to connect, the user name and the password of the data base.
   * Creates connections, 6 initially, and stores in a hashtable.
   */
  public ConnectionPoolT () throws ClassNotFoundException,SQLException  {
        Connection con=null;

        
        if(LPConstants.Driver.equalsIgnoreCase("MySQL_Type4")){
	    connectionURL = "jdbc:mysql://localhost.localdomain:3306/NasAccess";
	    driverName = "org.gjt.mm.mysql.Driver";
        }else if(LPConstants.Driver.equalsIgnoreCase("MySQL_ODBC")){
            driverName = "sun.jdbc.odbc.JdbcOdbcDriver";
            connectionURL = "jdbc:odbc:NasAccess";
        }else if(LPConstants.Driver.equalsIgnoreCase("Oracle_Linux")){
            connectionURL = "jdbc:oracle:thin:Boise/boise@localhost.localdomain:1521:Dimok";
            driverName="oracle.jdbc.driver.OracleDriver";
        }else if(LPConstants.Driver.equalsIgnoreCase("Oracle_Boise")){
            driverName="oracle.jdbc.driver.OracleDriver";
            connectionURL="jdbc:oracle:thin:I97_USER/horton@10.7.209.73:5792:ioe";
        }

        try{
            Class.forName(driverName).newInstance();
        }catch (InstantiationException ie){
            ie.printStackTrace();
        }catch (IllegalAccessException iae){
            iae.printStackTrace();
        }
//        if(type4Driver){
//	    connectionURL = "jdbc:mysql://localhost.localdomain:3306/NasAccess";
//	    driverName = "org.gjt.mm.mysql.Driver";
//      }else{
//        driverName = "sun.jdbc.odbc.JdbcOdbcDriver";
//      connectionURL = "jdbc:odbc:NasAccess";
//}
        try{
        Class.forName(driverName).newInstance();
        }catch (Exception e){
            e.printStackTrace();
        }
        
        if(_hashConnections == null) {
	  _hashConnections = new Hashtable();

	  //put initial connections into the hash table.
	  // FALSE indicates the connections are available to use.
	  for(int ni = 0;ni<CONNECTIONS;ni++) {
            if(LPConstants.Driver.equalsIgnoreCase("MySQL_Type4")){
                con = DriverManager.getConnection(connectionURL,"root","");
            }else if(LPConstants.Driver.equalsIgnoreCase("MySQL_ODBC")){
                con = DriverManager.getConnection("jdbc:odbc:NasAccess");
            }else if(LPConstants.Driver.equalsIgnoreCase("Oracle_Linux")){
                con = DriverManager.getConnection(connectionURL);
                con.setAutoCommit(true);
            }else if(LPConstants.Driver.equalsIgnoreCase("Oracle_Boise")){
                con = DriverManager.getConnection(connectionURL);
                con.setAutoCommit(true);
            }
		_hashConnections.put(con,Boolean.FALSE);
	  }
	}
  }  
  /**
   * Returns a Connection object. If a Connection object is not available
   * in the pool, a new Connection object is created and returned to the user.
   */
  public Connection getConnection() {
	Connection con =  null;
	Enumeration cons = _hashConnections.keys();
	synchronized(_hashConnections)
	{
	  while(cons.hasMoreElements())
	  {
		con  = (Connection) cons.nextElement();
		Boolean b = (Boolean) _hashConnections.get(con);
		if(b.booleanValue() == false)
		{
		  return con;
		}
	  }
	}

	//create connections if not available
	for(int ni = 0;ni<2;ni++)
	{
	  try
	  {
	    Connection conn=null;
            if(LPConstants.Driver.equalsIgnoreCase("MySQL_Type4")){
                con = DriverManager.getConnection(connectionURL,"root","");
            }else if(LPConstants.Driver.equalsIgnoreCase("MySQL_ODBC")){
                con = DriverManager.getConnection("jdbc:odbc:NasAccess");
            }else if(LPConstants.Driver.equalsIgnoreCase("Oracle_Linux")){
                con = DriverManager.getConnection(connectionURL);
                con.setAutoCommit(true);
            }else if(LPConstants.Driver.equalsIgnoreCase("Oracle_Boise")){
                con = DriverManager.getConnection(connectionURL);
                con.setAutoCommit(true);
            }
            _hashConnections.put(conn, Boolean.FALSE);
	  }
	  catch(Exception exep)
	  {
	    System.out.println(exep);
	  }
	}
	return getConnection();
  }  
  /**
   * Takes the connection object returned by the user and puts it
   * back into the hashtable.
   */
  public void returnConnection(Connection returnedCon)
  {
	Connection con;
	Enumeration e = _hashConnections.keys();
	while(e.hasMoreElements())
	{
	  con = (Connection) e.nextElement();
	  if(con == returnedCon)
	  {
		_hashConnections.put(con,Boolean.FALSE);
	    break;
	  }
	}
  }  
} //end of main class
