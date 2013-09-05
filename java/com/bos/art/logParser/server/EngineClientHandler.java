/*
 * Created on Oct 21, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package com.bos.art.logParser.server;


import com.bos.art.logParser.collector.LiveLogPriorityQueue;
import com.bos.art.logParser.records.AccumulatorEventTiming;
import com.bos.art.logParser.records.ExternalEventTiming;
import com.bos.art.logParser.records.ILiveLogPriorityQueueMessage;
import com.bos.art.logParser.records.SystemTask;
import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import org.apache.log4j.Logger;


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

    private Object readData(ObjectInputStream ois) throws IOException,ClassNotFoundException
    {
        return ois.readObject();
    }

    public void run() {
        LiveLogPriorityQueue queue = LiveLogPriorityQueue.getInstance();
        LiveLogPriorityQueue systemTaskQueue = LiveLogPriorityQueue.getSystemTaskQueue();

        try {
            ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(incoming.getInputStream(),1024*16)); // 16k

            while (true) {
                try {
                    Object o = readData(in);

                    if (o instanceof SystemTask) {
                        logger.warn("SystemTask Entering Priority Queue " + o.toString() + ":Time:" + System.currentTimeMillis());
                        logger.warn("SystemTask Queue " + systemTaskQueue.toString());
                        systemTaskQueue.addObject(o);
                        continue;
                    }
                    if (o instanceof ILiveLogPriorityQueueMessage) {
                        // insert into the priority queue at this point.
                        ILiveLogPriorityQueueMessage llpr = (ILiveLogPriorityQueueMessage) o;

                        if (o instanceof ExternalEventTiming) {
                            logger.debug("ExternalEventTiming: " + o.toString());
                        } else if (o instanceof AccumulatorEventTiming) {
                            AccumulatorEventTiming aet = (AccumulatorEventTiming) o;

                            if (aet.getClassification() >= 100000 && aet.getClassification() < 499999) {
                                continue;
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
