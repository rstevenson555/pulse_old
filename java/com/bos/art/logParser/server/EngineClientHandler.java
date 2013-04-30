/*
 * Created on Oct 21, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package com.bos.art.logParser.server;


import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.EOFException;
import java.io.ObjectInputStream;
import java.io.DataInputStream;
import java.net.Socket;

import org.apache.log4j.Logger;

import com.bos.art.logParser.collector.LiveLogPriorityQueue;
import com.bos.art.logParser.records.ExternalEventTiming;
import com.bos.art.logParser.records.ILiveLogPriorityQueueMessage;
import com.bos.art.logParser.records.PageRecordEvent;
import com.bos.art.logParser.records.SystemTask;
import com.bos.art.logParser.records.AccumulatorEventTiming;


/**
 * @author I0360D3
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class EngineClientHandler implements Runnable {
    private Socket incoming;
    private int counter;

    private static final Logger logger = (Logger) Logger.getLogger(EngineClientHandler.class.getName());
    public EngineClientHandler(Socket i, int c) {
        incoming = i;
        counter = c;
    }

    private Object readData(ObjectInputStream dis) throws IOException,ClassNotFoundException
    {
        return dis.readObject();
    }

    public void run() {
        LiveLogPriorityQueue queue = LiveLogPriorityQueue.getInstance();
        LiveLogPriorityQueue systemTaskQueue = LiveLogPriorityQueue.getSystemTaskQueue();

        try {
            //ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(incoming.getInputStream()));
            ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(incoming.getInputStream(),1024*8)); // 16k

            while (true) {
                try {
					
                    //Object o = in.readObject();
                    Object o = readData(in);

                    if (o instanceof SystemTask) {
                        // if(logger.isInfoEnabled()){
                        logger.warn("SystemTask Entering Priority Queue " + o.toString() + ":Time:" + System.currentTimeMillis());
                        logger.warn("SystemTask Queue " + systemTaskQueue.toString());
                        // }
                        systemTaskQueue.addObject(o);
                        continue;
                    }
                    // logger.debug("Object Read in" + o.toString());
                    // logger.debug("Class Name " + o.getClass().getName());
                    if (o instanceof ILiveLogPriorityQueueMessage) {

                        // insert into the priority queue at this point.
                        ILiveLogPriorityQueueMessage llpr = (ILiveLogPriorityQueueMessage) o;

                        if (o instanceof ExternalEventTiming) {
                            logger.debug("ExternalEventTiming: " + o.toString());
                        } else if (o instanceof AccumulatorEventTiming) {
                            AccumulatorEventTiming aet = (AccumulatorEventTiming) o;

                            if (aet.getClassification() >= 100000 && aet.getClassification() < 499999) {
                                continue;
                                // logger.warn("AET Database EventFound...");
                                // if(aet.getClassification() >=300000 && aet.getClassification()<399999){
                                // logger.warn("Classification : "+aet.getClassification()+"\n\n"+aet.getValue());
                                // }
                            }
                        }
                        queue.addObject(llpr);
                    } else {
                        logger.warn("Unknown Message entering Art Engine: " + o.getClass().getName());
                    }
                } catch (EOFException eofe) {
                    // This is the end.
                    logger.info(
                            "End of File Reached for Connection: " + counter + " on thread Id : " + Thread.currentThread().getName());
                    break;
                }
            }
        } catch (Throwable t) {
            // Figure out what to do if an exception happens.
            logger.error("Error in EngineClientHandler: ", t);

        }
    }
}
