/*
 * @(#)SimpleQuery
 *
 * Copyright (c) 1998 Karl Moss. All Rights Reserved.
 *
 * You may study, use, modify, and distribute this software for any
 * purpose provided that this copyright notice appears in all copies.
 *
 * This software is provided WITHOUT WARRANTY either expressed or
 * implied.
 *
 * @author  Karl Moss
 * @version 1.0
 * @date    02Apr98
 *
 */

//package javaservlets.db;
 
import java.sql.*;

/**
 * <p>This simple application will connect to a Microsoft Access
 * database using the JDBC-ODBC Bridge, execute a query against
 * an employee database, display the results, and then perform
 * all of the necessary cleanup
 */

public class SimpleQuery
{
  /**
    * <p>Main entry point for the application
    */
  public static void main(String args[])
    {
      try {

        // Perform the simple query and display the results
        performQuery();
      }
      catch (Exception ex) {
        ex.printStackTrace();
      }
    }

  public static void performQuery() throws Exception
    {
      // The name of the JDBC driver to use
       String driverName = "org.gjt.mm.mysql.Driver";
	// String driverName = "twz1.jdbc.mysql.jdbcMysqlDriver";

      // The JDBC connection URL
       String connectionURL = "jdbc:mysql://localhost.localdomain:3306/BryceData";
//      String connectionURL = "jdbc:z1MySQL://localhost:3306/BryceData?user='root'?host='localhost.localdomain'";
       //String connectionURL = "jdbc:z1MySQL:";

      // The JDBC Connection object
      Connection con = null;

      // The JDBC Statement object
      Statement stmt = null;

      // The SQL statement to execute
      String sqlStatement =
        "SELECT bla1, bla1_ID FROM bla1";
      
      // The JDBC ResultSet object
      ResultSet rs = null;

      try {

        System.out.println("Registering " + driverName);
        
        // Create an instance of the JDBC driver so that it has
        // a chance to register itself
        Class.forName(driverName).newInstance();

        System.out.println("Connecting to " + connectionURL);
        
        // Create a new database connection. We're assuming that
        // additional properties (such as username and password)
        // are not necessary
        con = DriverManager.getConnection(connectionURL,"root","");
        System.out.println("Connection Established");
        // Create a statement object that we can execute queries
        // with
        stmt = con.createStatement();
        // Execute the query
        rs = stmt.executeQuery(sqlStatement);

        // Process the results. First dump out the column
        // headers as found in the ResultSetMetaData
        ResultSetMetaData rsmd = rs.getMetaData();

        int columnCount = rsmd.getColumnCount();

        System.out.println("");
        String line = "";
        for (int i = 0; i < columnCount; i++) {
          if (i > 0) {
            line += ", ";
          }

          // Note that the column index is 1-based
          line += rsmd.getColumnLabel(i + 1);
        }
        System.out.println(line);

        // Count the number of rows
        int rowCount = 0;
        
        // Now walk through the entire ResultSet and get each
        // row
        while (rs.next()) {
          rowCount++;
          
          // Dump out the values of each row
          line = "";
          for (int i = 0; i < columnCount; i++) {
            if (i > 0) {
              line += ", ";
            }

            // Note that the column index is 1-based
            line += rs.getString(i + 1);
          }
          System.out.println(line);
        }

        System.out.println("" + rowCount + " rows, " +
                           columnCount + " columns");
      }
      finally {

        // Always clean up properly!
        if (rs != null) {
          rs.close();
        }
        if (stmt != null) {
          stmt.close();
        }
        if (con != null) {
          con.close();
        }
      }
    }
  
}










