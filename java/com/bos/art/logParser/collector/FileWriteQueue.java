/*
 * Created on Oct 21, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package com.bos.art.logParser.collector;


import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.commons.collections.UnboundedFifoBuffer;
import org.apache.log4j.Logger;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;


/**
 * @author I0360D3
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class FileWriteQueue extends Thread implements Serializable {
    private static final Logger logger = (Logger) Logger.getLogger(FileWriteQueue.class.getName());
    private static FileWriteQueue instance;
    transient private UnboundedFifoBuffer dequeue;
    private int objectsRemoved;
    private int objectsWritten;
    private long totalWriteTime;

    private final static DateTimeFormatter fdfYear  = DateTimeFormat.forPattern("yyyy");
    private final static DateTimeFormatter fdfFile  = DateTimeFormat.forPattern("yyyyMMdd");
    private static final String FILE_SYSTEM_ROOT;
    private static final String BASE_DIR;
    private OutputStream out;
    private static final String FILE_EXTENSION = ".art";
    private static final String BASE_FILE = "AccessRecords.";
    Calendar openDay = GregorianCalendar.getInstance();

    static { 
        String root = File.listRoots()[0].getAbsolutePath();

        if (root.equalsIgnoreCase("/")) {// ! Great you are using Unix!!!!
        } else {
            if (root.indexOf('A') > -1) {
                root = root.replace('A', 'D');
            }
        }
        FILE_SYSTEM_ROOT = root;
        BASE_DIR = FILE_SYSTEM_ROOT + "opt" + File.separator + "art" + File.separator + "logParser" + File.separator + "Historical"
                + File.separator + fdfYear.print(new java.util.Date().getTime());
							
    }

    private FileWriteQueue() {
        dequeue = new UnboundedFifoBuffer();
    }

    synchronized public static FileWriteQueue getInstance() {
        if (instance == null) {
            instance = new FileWriteQueue();
        }
        return instance;
    }

    synchronized public void addLast(Object o) {
        dequeue.add(o);
    }

    synchronized public Object removeFirst() {
        if (!dequeue.isEmpty()) {
            return dequeue.remove();
        }
        return null;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();

        sb.append("FileWriteQueue Size:");
        sb.append(dequeue.size());
        sb.append("\t\t this thread: ");
        sb.append(Thread.currentThread().getName());
        sb.append("Current Output File : " + BASE_DIR + File.separator + BASE_FILE + fdfFile.print(openDay.getTimeInMillis()) + FILE_EXTENSION);
        sb.append("\n\tObjects Popped              :  " + objectsRemoved);
        sb.append("\n\tObjects Written             :  " + objectsWritten);
        if (objectsWritten > 0) {
            sb.append("\n\tWrite Time millis per 1000  :  " + totalWriteTime / (objectsWritten / 1000));			
        } else {
            sb.append("\n\tWrite Time millis per 1000  :  0");			
        }
		
        return sb.toString();
    }
	
    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void run() {
        ObjectOutputStream fileOut = null;

        while (true) {
            if (this.dequeue.isEmpty()) {
                try {
                    Thread.sleep(1000 * 30);
                    fileOut = getOut(fileOut);
                    continue;
                } catch (InterruptedException ie) {
                    logger.error("Sleep Exception", ie);
                } catch (Exception e) {
                    logger.error("Exception in Sleep of File Write Queue ", e);
                }
            } else {
				
                if (logger.isInfoEnabled()) {
                    if (objectsRemoved % 10000 == 0) {
                        logger.info(toString());
                    }
                }
                try {
                    if (fileOut == null) {
                        fileOut = getOut(fileOut);
                    }
                    Object o = this.removeFirst();

                    ++objectsRemoved;
                    if (o == null) {
                        logger.error("removeFirst Returned Null!");
                        continue;
                    }
                    long writeStartTime = System.currentTimeMillis();

                    // Write to the file.
                    fileOut.writeObject(o);
                    totalWriteTime += (System.currentTimeMillis() - writeStartTime);
                    ++objectsWritten;
                    if (objectsWritten % 10000 == 0) {
                        fileOut.flush();
                        fileOut.reset();
                    }
                } catch (Exception t) {
                    logger.error("Throwable in FileWriteQueue Thread! " + Thread.currentThread().getName() + ":", t);
                }
            }
        }
    }
    int iOpenDay = 0;
    private File outFile;
    Calendar cToday = GregorianCalendar.getInstance();
    synchronized private ObjectOutputStream getOut(ObjectOutputStream out) throws IOException {
        int iToday;

        cToday.setTime(new Date());
        iToday = cToday.get(Calendar.DAY_OF_YEAR);
        if (iToday != iOpenDay || out == null) {
            logger.info("Entering Output File Close Sequence");
            if (out != null) {
				
                ((ObjectOutputStream) out).reset();
			
                out.flush();
                out.close();
                out = null;
			
                try {
                    logger.info("Testing OutFile For Null.");
                    if (outFile != null) {
                        logger.info("Attempting to Close Outfile: " + outFile.toString());
                        String sOutputFile = outFile.getCanonicalPath() + File.separator + outFile.getName();
                        String Command = "/usr/bin/bzip2 " + sOutputFile;

                        logger.info("Command : " + Command);
                        long startTime = System.currentTimeMillis();

                        Runtime.getRuntime().exec(Command);
                        logger.info("Command Executed in : " + (System.currentTimeMillis() - startTime));
                    }
                } catch (IOException e) {
                    logger.error("IOException Trying to compress old file ", e);
                } catch (RuntimeException re) {
                    logger.error("RuntimeException Trying to compress old file ", re);
                }
            }
			
            openDay.setTime(new java.util.Date());
            iOpenDay = openDay.get(Calendar.DAY_OF_YEAR);
            outFile = new File(BASE_DIR + File.separator + BASE_FILE + fdfFile.print(openDay.getTimeInMillis()) + FILE_EXTENSION);
            try {
                out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(outFile.toString(), true)));
            } catch (IOException ie) {
                logger.error("Exception opening :" + outFile.toString(), ie);
            }
        }
        return (ObjectOutputStream) out;
    }
	
}
