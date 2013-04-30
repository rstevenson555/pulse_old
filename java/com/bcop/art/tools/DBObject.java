/*
 * DBObject.java
 *
 * Created on June 18, 2002, 5:50 PM
 */

package com.bcop.art.tools;
import java.sql.Connection;

/**
 *
 * @author  I0360D3
 * @version 
 */
public interface DBObject {
    
    public void updateToART(Connection con);
    public void initialize(String s);
    

}

