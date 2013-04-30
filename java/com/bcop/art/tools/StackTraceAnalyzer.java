/*
 * StackTraceAnalyzer.java
 *
 * Created on June 4, 2002, 11:24 PM
 */

package com.bcop.art.tools;

import java.io.*;
import java.util.*;

/**
 *
 * @author  Bryce
 * @version 
 */
public class StackTraceAnalyzer {

    private static final String BEGIN_STACK_TRACE = "beginStackDump]";
    private static final String END_STACK_TRACE = "endStackDump";
    private static final String STACK_ELEMENT = "at ";
    private static final String START_JSPBEAN_CONTAINER = "pBeanContainer:";
    private static final String NEWLINE = "\n <BR> ";
    
    /** Creates new StackTraceAnalyzer */
    public StackTraceAnalyzer() {
    }

    /**
    * @param args the command line arguments
    */
    public static void main (String args[]) throws Exception {
        File logFile = new File(args[0]);
        BufferedReader bfLogFile = new BufferedReader(new FileReader(logFile));
        boolean isEOF = false;
        int i = 0;
        int j = 0;
        int k = 0;
        boolean inStackDump = false;
        StackTrace currentStackTrace = null;
        ArrayList traces = new ArrayList();
        StringBuffer sJspBeanContainer = null ;
        boolean inJSPBeanContainer = false;
        while(!isEOF){
            String str = bfLogFile.readLine();
            if(str== null){
                isEOF = true;
                continue;
            }
            if(str.indexOf(BEGIN_STACK_TRACE) > 0){
                inStackDump = true;
                currentStackTrace = new StackTrace();
                currentStackTrace.setTraceTimestamp(getTraceTimestamp(str));
                str=bfLogFile.readLine();
                currentStackTrace.setTraceKey(getTraceKey(str));
                str=bfLogFile.readLine();
                currentStackTrace.setMessage(str);
                str=bfLogFile.readLine();
                ++j;
            }else if(str.indexOf(END_STACK_TRACE) > 0 ){
                inStackDump = false;
                /* Cheryl: JspBeanContainer should be set in currentStatckTrace object */
                inJSPBeanContainer = false;
                if(sJspBeanContainer != null){
                    currentStackTrace.setJSPBeanContainer(sJspBeanContainer.toString());
                    sJspBeanContainer = null;
                }
                /* Cheryl: End of change */
                traces.add(currentStackTrace);
                if(traces.size()%1000 == 0){
                    long Time = System.currentTimeMillis();
                    StackTrace.resetTimers();
                    unloadTraces(traces);
                    System.out.println("Size of Traces after removal: " + traces.size());
                    System.out.println("Unloaded in : ..........." + ((System.currentTimeMillis()-Time)/1000) + " seconds");
                    System.out.println("Bean Container: ........." + (StackTrace.getBeanContainerUpdateTime()/1000));
                    System.out.println("Row ID LOOKUP Time: ....." + (StackTrace.getRowIDLookupTime()/1000));
                    System.out.println("Stack Detail Time : ....." +(StackTrace.getStackDatailTime()/1000) );
                    System.out.println("Trace Update Time : ....." + (StackTrace.getTraceUpdateTime()/1000));
                    System.out.println("Trace ID Time : ........." + (StackTrace.getTraceIDTime()/1000));
                }
                currentStackTrace = null;
                ++k;
            }else if (str.indexOf(START_JSPBEAN_CONTAINER) >0){
                inJSPBeanContainer = true;
                sJspBeanContainer = new StringBuffer();
                sJspBeanContainer.append(str).append(NEWLINE);
            }else if(str.indexOf(STACK_ELEMENT) >0 ) {
                //System.out.println("Adding to the stackdepth");
                if(currentStackTrace != null){
                    String stackElement = str.substring(str.indexOf(STACK_ELEMENT));
                    currentStackTrace.addStackElement(stackElement);

                }
            }else if(inJSPBeanContainer) {
                sJspBeanContainer.append(str).append(NEWLINE);
            }
            ++i;
        }
        unloadTraces(traces);
        System.out.println("There are Begins = " + j);
        System.out.println("There are Ends = " +k);
        System.out.println("The end of the file is here " + i);
        System.out.println("ArrayLists added =" +traces.size());
    }
    private static  void unloadTraces(Collection traces){
        Iterator iter = traces.iterator();
        while(iter.hasNext()){
            
            Object o = iter.next();
            
            //System.out.println(o.toString());
            if(o instanceof StackTrace){
                try{
                    ((StackTrace)o).updateToART();
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
            iter.remove();
        }
        
        
    }
    
    private static String getTraceKey(String tks){
        return tks.trim();//substring(tks.indexOf(BEGIN_STACK_TRACE)+15);
    }

    private static String getTraceTimestamp (String tks){
        // Some additional parsing will go here.
        return tks.substring(1,15);
        //return "20020607010001";
    }
}
