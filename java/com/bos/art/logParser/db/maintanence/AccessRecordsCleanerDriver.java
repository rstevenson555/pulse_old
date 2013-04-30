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
import java.util.TimerTask;
import org.apache.log4j.Logger;

/**
 * @author I0360D3
 *
 * TODO To change the template for this generated type comment go to Window - Preferences - Java - Code Style - Code
 * Templates
 */
public class AccessRecordsCleanerDriver extends TimerTask {

    private static AccessRecordsCleanerDriver instance = new AccessRecordsCleanerDriver();

    public static AccessRecordsCleanerDriver getInstance() {
        return instance;
    }
    private static final Logger logger = (Logger) Logger.getLogger(AccessRecordsCleanerDriver.class.getName());

    public static void main(String args[]) {
        //  If we are in main, then we don't have a logger yet:
        //
        //Properties p = com.bos.art.logParser.tools.URIResourceLoader.loadPropertiesFile("logParser.properties");
        //PropertyConfigurator.configure(p);
        Engine.init();
        Engine.initializeDatabaseConnectionPooling();

        AccessRecordsCleaner arc = new AccessRecordsCleaner();
        arc.restart();
        printStatus(arc);
        while (arc.hasNext()) {
            Object o = arc.next();
            int i = ((Integer) o).intValue();
            logger.info(" % COMPLETED: -> " + i + " %");
            printStatus(arc);
            try {
                Thread.sleep(3 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

    public void run() {
        try {
            com.bos.art.logParser.server.Engine.initializeDatabaseConnectionPooling();
            AccessRecordsCleaner arc = new AccessRecordsCleaner();
            arc.restart();
            printStatus(arc);
            while (arc.hasNext()) {
                Object o = arc.next();
                int i = ((Integer) o).intValue();
                logger.info(" % COMPLETED: -> " + i + " %");
                printStatus(arc);
                try {
                    Thread.sleep(3 * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (Throwable t) {
            logger.error("AccessRecordsCleanerDriver error: " + t);
        }
    }
    private static final SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM HH:mm:ss  ");

    /**
     * @param arc
     */
    private static void printStatus(AccessRecordsCleaner arc) {
        logger.info("-------------");
        logger.info("Max RecordPK " + arc.getMaxRecordPK());
        logger.info("Min RecordPK " + arc.getMinRecordPK());
        logger.info("Cur RecordPK " + arc.getCurrentRecordPK());
        logger.info("Est. Fin. T  " + sdf.format(new Date(arc.getEstimatedFinishTime())));
        logger.info("Increment sz " + arc.getInitialIncrementAmount());
        logger.info("\n");
    }
}
