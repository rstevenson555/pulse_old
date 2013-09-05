/*
 * Created on Oct 29, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package com.bos.art.logParser.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * @author I0360D3
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class ConnectionPoolT {
	
    private static boolean conInit = false;
    private static Object lock = new Object();
    private static final String JDBC_APACHE_COMMONS_DBCP_ART_DB_POOL = "jdbc:apache:commons:dbcp:art-db-pool";

    public static Connection getConnection() throws SQLException{
        if(!conInit){
            synchronized(lock){
                if(!conInit){
                    com.bos.art.logParser.server.Engine.initializeDatabaseConnectionPooling();
                    conInit = true;
                }
            }
        }
		return DriverManager.getConnection(JDBC_APACHE_COMMONS_DBCP_ART_DB_POOL);
	}

}
