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
       //addToArchive();        
        
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
     *@return a linkedlist of java.util.Date[] objects where ts[0] represents the 
     *file start time and ts[1] represents the file last modified time;
     **/
    static LinkedList getUpTimes(File[] fa){
        java.util.Date[] tsa;
        LinkedList ll = new LinkedList();
        for(int i = 0;i<fa.length;++i){
            tsa = new java.util.Date[2];
            //  The precondition must be met or a time of 
            //  11/11/1919 11:00:00 will be put into the file start time.
            tsa[0] = getStartTime(fa[i]);
            tsa[1] = new java.util.Date(fa[i].lastModified());
            ll.addLast(tsa);
        }
        return ll;
    }
    
    
    /**
     *This method gets the start time of a file that is in the format jsperror.txtMMDDhhmm
     *@param file which is the file called jsperror.txt########
     *@return a java.util.Date object representing the starting time for this file.
     *if the precondtion of the file name format is not met, then a Timestamp representing
     * 11/11/1919 11:00:00 AM is returned.
     *@note This uses depricated api and should be rewritten to use something other than 
     *depricated apis.
     **/
    static java.util.Date getStartTime(File file){
   
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
                           try{
                               return LPConstants.LogFileNameFormat.parse(sback.substring(3,11) + getYear()+"00");
                           }catch (ParseException pe){
                                pe.printStackTrace();
                           }

                        }
                    }
                }
            }
        }  
        System.out.println("Setting default start time");
        try{
            return LPConstants.FileNameFormat.parse("11111919110000");
        }catch (ParseException pe){
            pe.printStackTrace();
        }
        return new java.util.Date();
    }

    /**
     *This method uses a depricated api to return the current year.
     *@return int the current year
     *It would be a good idea to rewrite this so that it does not use 
     *a depricated API.
     **/
     static String getYear(){
        java.util.Date d = new java.util.Date(System.currentTimeMillis());
        return LPConstants.YearFormat.format(d);
    }
    
    
    /**
     *This method adds the array of files that represent the log files into an Archive jar.
     *a new archive jar is created each month, and is named MachineNameMM.jar (NAS104.jar for Nas1 in April).
     *@param fa is an array of files to be archived.
     *@param Archive is the directory where the Archive is located.  If Archive is not a directory,
     *the program will not execute. and will just return.
     *@return true if the archives were created successfully, False if Archive was not a directory.
     */
    static boolean addToArchive(File[] fa, File Archive){
        if(Archive.isDirectory()){
            int i = 0;
            String aname = LPConstants.MachineName + LPConstants.MonthFormat.format(new java.util.Date()) + ".jar";
            File jarArchive = new File(Archive.getAbsolutePath() + File.separator + aname);
            String[] saa = new String[2+fa.length];
            saa[1] = jarArchive.getAbsolutePath();
            boolean createFile=!jarArchive.exists();
            sun.tools.jar.Main main1 = new sun.tools.jar.Main(System.out, System.err, "jar");
            System.out.println("in add To Archive: " + fa.length);
            synchronized (jarArchive){
                if(!createFile){
                    saa[0] ="-uf";
                    for(int k = 0;k<fa.length;++k){
                        saa[2+k] = fa[k].getPath();
                    }
                    System.out.println("jar "+ saa[0] + saa[1] + saa[2]);
                    main1.run(saa);


                }else{
                    createFile = false;
                    saa[0] = "-cf";
                    for(int k = 0;k<fa.length;++k){
                        saa[2+k] = fa[k].getPath();
                    }
                    System.out.println("jar "+ saa[0] + saa[1] + saa[2]);
                    main1.run(saa);
                    System.out.println("I am pausing");
                    edu.colorado.io.EasyReader.pause(20000);
                    System.out.println("I am done pausing");


                }
            }
            return true;
        }
        return false;
    }

    
    /**
     *This method will delete all the files in File[] fa from your hard drive.
     *@param fa is an array of files.
     *@return is true if all the files were deleted successfully, and
     *false if any one of the files was not.
     */
    static boolean deleteFiles(File[] fa){
        boolean ret = true;
        for(int i = 0; i< fa.length; i++){
            File f = fa[i];
            if(f.exists()){
                if(!f.delete()){
                    ret=false;
                }
            }
        }
        return ret;
    }
}
