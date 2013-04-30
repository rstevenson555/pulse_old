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
    
	public static Connection getConnection() throws SQLException{
        if(!conInit){
            synchronized(lock){
                if(!conInit){
                    com.bos.art.logParser.server.Engine.initializeDatabaseConnectionPooling();
                    conInit = true;
                }
            }
        }
		return DriverManager.getConnection("jdbc:apache:commons:dbcp:art-db-pool");
	}

}
