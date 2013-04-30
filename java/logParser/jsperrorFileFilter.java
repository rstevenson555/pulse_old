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
public class jsperrorFileFilter implements java.io.FileFilter{

    /** Creates new jsperrorFileFilter */
    public jsperrorFileFilter() {
    }

    public boolean accept(java.io.File file) {
        return accept(file,"jsperror");
    }
    
    public boolean accept(java.io.File file,java.lang.String str) {
        if(file.isFile()){
            if(file.canRead()){
                String fname = file.getName();
                StringTokenizer st = new StringTokenizer(fname,".");
                if(st.countTokens()==2){
                    ParsePosition pp = new ParsePosition(0);
                    String sfront = st.nextToken();
                    if(sfront.equalsIgnoreCase("jsperror")){
                        return true;
                    }
                }
            }
        } 
        return false;
    }
}
