/*
 * jsperrorFileFilter.java
 *
 * Created on May 1, 2001, 12:20 PM
 */

package logParser;
import java.io.*;
import java.text.*;
import java.util.*;
/**
 *
 * @author  i0360d3
 * @version 
 */
public class orderpointJspLogFileFilter implements java.io.FileFilter{

    /** Creates new jsperrorFileFilter */
    public orderpointJspLogFileFilter() {
    }

    public boolean accept(java.io.File file) {
        return accept(file,"orderpoint");
    }
    
    public boolean accept(java.io.File file,java.lang.String str) {
        if(file.isFile()){ 
            if(file.canRead()){
                String fname = file.getName();
                StringTokenizer st = new StringTokenizer(fname,".");
                if(st.countTokens()==4){
                    ParsePosition pp = new ParsePosition(0);
                    String sfront = st.nextToken();
                    if(sfront.equalsIgnoreCase("orderpoint")){
                        String nextToken = st.nextToken();
                        if(nextToken.equalsIgnoreCase("jsp")){
                            return true;
                        }
                    }
                }
            }
        } 
        return false;
    }
}
