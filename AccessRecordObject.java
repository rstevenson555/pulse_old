
package logParser;
import java.sql.*;

public class AccessRecordObject implements Runnable {


    public java.sql.Connection con;
    public java.sql.PreparedStatement ps;
    public static java.util.Stack AROStack = null;
    //    private Thread worker;


    public AccessRecordObject(java.util.Stack s){
	AROStack = s;
	//        worker = new Therad(this);
	//worker.start();
    }


    public AccessRecordObject(java.sql.PreparedStatement p, Connection con){
	ps=p;
    }



    public void run(){

	try{
	if(emptyAccessRecords())
	    System.out.println("Ending EmptyAccessRecords Thread");
	}catch (RecordRecordsException rre){
	    System.out.println("RRE thrown in run of spawned thread you better stop execute");
	}


    }


       
    private static boolean  emptyAccessRecords() throws RecordRecordsException {
	try{
	    AccessRecordObject aro;
	    while(!AROStack.empty()){
	        aro =(AccessRecordObject) AROStack.pop();
	        int rows = aro.ps.executeUpdate();
		aro.ps.close();
	    }
	}catch(SQLException se){
	    System.out.println("Error Adding Cashed Blocked Records");
	    throw new RecordRecordsException();
	}
	return true;


    }


}
