/*
 * ConcreteQueryExample.java
 *
 * Created on March 30, 2001, 9:36 AM
 */

package logParser;
import java.sql.*;
/**
 *
 * @author  i0360d3
 * @version 
 */
public class ConcreteQueryExample extends java.lang.Object {
    
    /** Creates new ConcreteQueryExample */
    public ConcreteQueryExample() {
    }

    /**
    * @param args the command line arguments
    */
    public static void main (String args[]) {

        System.out.println("Location A");
        ConcreteQueryExample cqe = new ConcreteQueryExample();
        System.out.println("Location B");
        cqe.DropTempTables();
        System.out.println("Location C");
        cqe.CreateTempTables();
        System.out.println("Location D");
        DataQueryObject dqo = cqe.runQuery();
        System.out.println("Location E");
        dqo.writeCSVData();
    }
    
    public void DropTempTables(){
        String st = "Drop Table xyz";
        Connection con = getConnection();
        try{
            Statement s = con.createStatement();
            s.execute(st);
            s.close();
            con.close();
        }catch(Exception e){
            System.out.println("An error has occured");
        }
    }
    public void CreateTempTables(){
        String st = "Create table xyz as " + 
                    "select DISTINCT sessionPK, DATE_FORMAT(Time, \"%H\") aa" +
                    " from accessrecords where DATE_FORMAT(Time,\"%d\")='13'";
        Connection con = getConnection();
        try{
            Statement s = con.createStatement();
            s.execute(st);
            s.close();
            con.close();
        }catch(Exception e){
            System.out.println("An error has occured");
        }
    }
    public DataQueryObject runQuery(){
        String st = "select DATE_FORMAT(Time, \"%d %H %M\"), count(*) from "+
                    "accessrecords group by DATE_FORMAT(Time, \" %H:%m\")";
        Connection con = getConnection();
        ResultSet rs = null;
        DistinctSessionData dsa = new DistinctSessionData("d:\\Projects\\logParser\\ftest.txt");
        try{
            Statement s = con.createStatement();
            rs = s.executeQuery(st);
            while(rs.next()){
                System.out.println("In ResultSet Iterating Through");
                String[] row = new String[2];
                System.out.println("In ResultSet Iterating Through Location 1");
                row[0] = rs.getString(1);//Timestamp(1).toString();
                System.out.println("In ResultSet Iterating Through Location 2");
                row[1] = ""+rs.getInt(2);
                System.out.println("In ResultSet Iterating Through Location 3");
                dsa.populateRow(row);
            }
            s.close();
            con.close();
        }catch(Exception e){
            System.out.println("An error has occured");
        }
        return dsa;
    }
    
    public Connection getConnection(){
        boolean type4Driver = false;
        String connectionURL;
        String driverName;

        if(type4Driver){
	    connectionURL = "jdbc:mysql://localhost.localdomain:3306/NasAccess";
	    driverName = "org.gjt.mm.mysql.Driver";
        }else{
            driverName = "sun.jdbc.odbc.JdbcOdbcDriver";
            connectionURL = "jdbc:odbc:NasAccess";
        }
     
        Connection con = null;
        String nextLine=null;
        jspErrorObject jeoObj = null;
    	try{
            Class.forName(driverName).newInstance();
        
            if(type4Driver){
	        con = DriverManager.getConnection(connectionURL,"root","");
            }else{
	        con = DriverManager.getConnection("jdbc:odbc:NasAccess");
            }
	}catch (Exception e){
		e.printStackTrace();
	}

        return con;
    }

}
