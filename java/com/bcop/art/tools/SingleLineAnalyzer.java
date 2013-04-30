/*
 * SingleLineAnalyzer.java
 *
 * Created on June 4, 2002, 11:24 PM
 */

package com.bcop.art.tools;

import java.io.*;
import java.util.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.DriverManager;

/**
 *
 * @author  Bryce
 * @version 
 */
public class SingleLineAnalyzer {

    private static final String BEGIN_STACK_TRACE = "beginStackDump]";
    private static final String END_STACK_TRACE = "endStackDump";
    private static final String STACK_ELEMENT = "at ";
    private static final String START_JSPBEAN_CONTAINER = "pBeanContainer:";
    private static final String NEWLINE = "\n <BR> ";
    
    /** Creates new SingleLineAnalyzer */
    public SingleLineAnalyzer() {
    }

    /**
    * @param args the command line arguments
    */
    public static void main (String args[]) throws Exception {
        Connection con = null;
        try{
            con = getARTMConnection();
        }catch(Exception e){
            e.printStackTrace();
        }
        if (args == null || args.length < 2){
            System.out.println("Usage: SingleLineAnalyzer File Class ");
            System.exit(0);
        }
        DBObject slObject = null;
        Class slc = Class.forName(args[1]);
        
        slc.newInstance();
        File logFile = new File(args[0]);
        BufferedReader bfLogFile = new BufferedReader(new FileReader(logFile));
        boolean isEOF = false;
        ArrayList DBObjects = new ArrayList();
        int i =0;
        while(!isEOF){
            String str = bfLogFile.readLine();
            if(str== null){
                isEOF = true;
                continue;
            }
            slObject = (DBObject)slc.newInstance();
            slObject.initialize(str);
            DBObjects.add(slObject);
            slObject = null;
            if(DBObjects.size() > 1000){
                unloadObjects(DBObjects, con);
            }
            ++i;
        }
        System.out.println("ArrayList size before unload: " + DBObjects.size());
        unloadObjects(DBObjects, con);
        
        System.out.println("ArrayList size after unload:  " + DBObjects.size());
        try{
            con.close();
        }catch(Exception e){
            e.printStackTrace();
        }
        
    }
    private static  void unloadObjects(Collection traces, Connection con){
        Iterator iter = traces.iterator();
        while(iter.hasNext()){
            
            Object o = iter.next();
            
            //System.out.println(o.toString());
            if(o instanceof DBObject){
                try{
                    ((DBObject)o).updateToART(con);
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
            iter.remove();
        }
    }
    private static Connection getARTMConnection() throws ClassNotFoundException, InstantiationException,
                                            IllegalAccessException, SQLException{

        String connectionURL = "jdbc:mysql://art-db1.int.bcop.com:3306/artm";
        String driverName = "org.gjt.mm.mysql.Driver";
        Class.forName(driverName).newInstance();
        Connection con = DriverManager.getConnection(connectionURL,"art_user","stream1");
        con.setAutoCommit(true);
        return con;
    }
    

}
