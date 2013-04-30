/*
 * Created on Dec 31, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.bos.art.logParser.db.maintanence;

import com.bos.art.logParser.server.Engine;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author I0360D3
 *
 * TODO To change the template for this generated type comment go to Window - Preferences - Java - Code Style - Code
 * Templates
 */
public class StackTraceCleanerDriver {

    public static void main(String args[]) {
        //com.bos.art.logParser.server.Engine.initializeDatabaseConnectionPooling();
        System.out.println("Main Called. ");
        //Properties p = com.bos.art.logParser.tools.URIResourceLoader.loadPropertiesFile("logParser.properties");
        //PropertyConfigurator.configure(p);
        //com.bos.art.logParser.server.Engine.initializeDatabaseConnectionPooling();
        Engine.init();
        Engine.initializeDatabaseConnectionPooling();

        StackTraceCleaner arc = new StackTraceCleaner();
        arc.restart();
        printStatus(arc);
        while (arc.hasNext()) {
            Object o = arc.next();
            int i = ((Integer) o).intValue();
            System.out.println(" % COMPLETED: -> " + i + " %");
            printStatus(arc);
            /*
             * try { Thread.sleep(10); } catch (InterruptedException e) { e.printStackTrace(); }
             */
        }
    }
    private static final SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM HH:mm:ss  ");

    /**
     * @param arc
     */
    private static void printStatus(StackTraceCleaner arc) {
        System.out.println("-------------");
        System.out.println("Max Trace_ID " + arc.getMaxTraceID());
        System.out.println("Min Trace_ID " + arc.getMinTraceID());
        System.out.println("Cur Trace_ID " + arc.getCurrentTraceID());
        System.out.println("Est. Fin. T  " + sdf.format(new Date(arc.getEstimatedFinishTime())));
        System.out.println("Increment sz " + arc.getInitialIncrementAmount());
        System.out.println("\n");
    }
}
