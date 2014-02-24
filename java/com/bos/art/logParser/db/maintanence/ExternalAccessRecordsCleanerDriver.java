/*
 * Created on Dec 31, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.bos.art.logParser.db.maintanence;

import com.bos.art.logParser.server.Engine;
import com.bos.helper.SingletonInstanceHelper;
import org.apache.log4j.Logger;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimerTask;

/**
 * @author I0360D3
 *         <p/>
 *         TODO To change the template for this generated type comment go to Window - Preferences - Java - Code Style - Code
 *         Templates
 */
public class ExternalAccessRecordsCleanerDriver extends TimerTask {

    private static final Logger logger = (Logger) Logger.getLogger(ExternalAccessRecordsCleanerDriver.class.getName());
    private static final SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM HH:mm:ss yyyy ");
    private static SingletonInstanceHelper instance = new SingletonInstanceHelper<ExternalAccessRecordsCleanerDriver>(ExternalAccessRecordsCleanerDriver.class) {
        @Override
        public java.lang.Object createInstance() {
            return new ExternalAccessRecordsCleanerDriver();
        }
    };
    public static ExternalAccessRecordsCleanerDriver getInstance() {
        return (ExternalAccessRecordsCleanerDriver)instance.getInstance();
    }

    public static void main(String args[]) {
        //  If we are in main, then we don't have a logger yet:
        //
        System.out.println("Main Called. ");
        //Properties p = com.bos.art.logParser.tools.URIResourceLoader.loadPropertiesFile("logParser.properties");
        //PropertyConfigurator.configure(p);
        Engine.init();
        Engine.initializeDatabaseConnectionPooling();

        ExternalAccessRecordsCleaner arc = new ExternalAccessRecordsCleaner();
        System.out.println("CAlling ARC.restart()");
        logger.warn("CAlling ARC.RESTART()");
        arc.restart();
        printStatus(arc);
        while (arc.hasNext()) {
            Object o = arc.next();
            int i = ((Integer) o).intValue();
            System.out.println(" % COMPLETED: -> " + i + " %");
            printStatus(arc);
            try {
                Thread.sleep(3 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * @param arc
     */
    private static void printStatus(ExternalAccessRecordsCleaner arc) {
        System.out.println("-------------");
        System.out.println("Max RecordPK " + arc.getMaxRecordPK());
        System.out.println("Min RecordPK " + arc.getMinRecordPK());
        System.out.println("Cur RecordPK " + arc.getCurrentRecordPK());
        System.out.println("Est. Fin. T  " + sdf.format(new Date(arc.getEstimatedFinishTime())));
        System.out.println("Increment sz " + arc.getInitialIncrementAmount());
        System.out.println("\n");
    }

    public void run() {
        com.bos.art.logParser.server.Engine.initializeDatabaseConnectionPooling();
        ExternalAccessRecordsCleaner arc = new ExternalAccessRecordsCleaner();
        arc.restart();
        printStatus(arc);
        while (arc.hasNext()) {
            Object o = arc.next();
            int i = ((Integer) o).intValue();
            System.out.println(" % COMPLETED: -> " + i + " %");
            printStatus(arc);
            try {
                Thread.sleep(3 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
