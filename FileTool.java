/*
 * FileTool.java
 *
 * Created on May 1, 2001, 11:54 AM
 */

package logParser;
import java.io.*;
import java.text.*;
import java.util.*;
import java.sql.*;

/**
 *
 * @author  i0360d3
 * @version 
 */
public class FileTool extends Object {

    /** Creates new FileTool */
    public FileTool() {
    }
    
    public static void main(String args[]){
        int i = 12;
        DecimalFormat df = new DecimalFormat("000");
        System.out.println(df.format(i) + "  : from the ### number format");
        System.out.println(System.getProperty("BOISEOP.errors.redirect"));
        Display(getFiles("d:\\Projects\\logs\\nas1"));
        
    }
    

    /**
     *This returns an array of File objects which represent the jsperror.* files
     *from the directory entered as a param.
     *@param pathname is the machine dependent name to the path, as a string  in the 
     *future this should be rewritten to reperesent a machine independent way of thinking about things.
     *@return File[] of File objects which represent all of the objects that should be processed.
     */
    static File[] getFiles(String pathname){
        File currentDir = new File(pathname);
        return currentDir.listFiles(new jsperrorFileFilter());
    }
    
    /**
     *Used for demo only should be called toString and done differently
     */
    static void Display(File[] f){
        for(int i = 0; i < f.length; ++i){
            System.out.println("  :" +(f[i].toString()));
        }
    }

    /**
     *This method creates of linked list of Timestamp objects
     *@param File of jsperror.txtMMDDhhmm  files where the MMDDhhmm 
     *is a precondition on the file
     *@return a linkedlist of Timestamp[] objects where ts[0] represents the 
     *file start time and ts[1] represents the file last modified time;
     **/
    static LinkedList getUpTimes(File[] fa){
        Timestamp[] tsa;
        LinkedList ll = new LinkedList();
        for(int i = 0;i<fa.length;++i){
            tsa = new Timestamp[2];
            //  The precondition must be met or a time of 
            //  11/11/1919 11:00:00 will be put into the file start time.
            tsa[0] = getStartTime(fa[i]);
            tsa[1] = new Timestamp(fa[i].lastModified());
            ll.addLast(tsa);
        }
        return ll;
    }
    
    
    /**
     *This method gets the start time of a file that is in the format jsperror.txtMMDDhhmm
     *@param file which is the file called jsperror.txt########
     *@return a timestamp object representing the starting time for this file.
     *if the precondtion of the file name format is not met, then a Timestamp representing
     * 11/11/1919 11:00:00 AM is returned.
     *@note This uses depricated api and should be rewritten to use something other than 
     *depricated apis.
     **/
    static Timestamp getStartTime(File file){
   
        if(file.isFile()){
            if(file.canRead()){
                String fname = file.getName();
                StringTokenizer st = new StringTokenizer(fname,".");
                if(st.countTokens()==2){
                    ParsePosition pp = new ParsePosition(0);
                    String sfront = st.nextToken();
                    String sback = st.nextToken();
                    if(sfront.equalsIgnoreCase("jsperror")){
                        if(sback.length() == 11){
                            
                            String stime = sback.substring(2);
                            return new Timestamp(getYear(),Integer.parseInt(sback.substring(2,4)),  
                                                 Integer.parseInt(sback.substring(4,6)),
                                                 Integer.parseInt(sback.substring(6,8)),
                                                 Integer.parseInt(sback.substring(8,10)),0,0);
                        }
                    }
                }
            }
        } 
        return new Timestamp(1919,11,11,11,0,0,0);
    }

    /**
     *This method uses a depricated api to return the current year.
     *@return int the current year
     *It would be a good idea to rewrite this so that it does not use 
     *a depricated API.
     **/
     static int getYear(){
        java.util.Date d = new java.util.Date(System.currentTimeMillis());
        return d.getYear();
    }
}
