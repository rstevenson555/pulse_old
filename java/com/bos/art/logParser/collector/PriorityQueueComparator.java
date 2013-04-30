/*
 * Created on Nov 10, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package com.bos.art.logParser.collector;


import java.util.Comparator;

import org.apache.log4j.Logger;

import com.bos.art.logParser.records.ILiveLogPriorityQueueMessage;


/**
 * @author I0360D3
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class PriorityQueueComparator implements Comparator {
    private static final Logger logger = (Logger) Logger.getLogger(PriorityQueueComparator.class.getName());

    /* (non-Javadoc)
     * @see com.sun.corba.se.internal.io.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    public int compare(Object arg0, Object arg1) {
        if (arg0 instanceof ILiveLogPriorityQueueMessage) {
            if (arg1 instanceof ILiveLogPriorityQueueMessage) {
                ILiveLogPriorityQueueMessage a1 = (ILiveLogPriorityQueueMessage) arg0;
                ILiveLogPriorityQueueMessage a2 = (ILiveLogPriorityQueueMessage) arg1;

                if (a1.getPriority() < a2.getPriority()) {
                    return -1;
                } else if (a1.getPriority() > a2.getPriority()) {
                    return 1;
                } else if (a1.getEventTime().getTime().after(a2.getEventTime().getTime())) {
                    return 1;
                } else if (a1.getEventTime().getTime().equals(a2.getEventTime().getTime())) {
                    return 1;
                } else {
                    return -1;
                }
            }
        }
        logger.warn(
                "Trying to compare non-ILiveLogPriorityQueueMessage:" + arg0.getClass().getName() + ":" + arg1.getClass().getName());
        // Place it at the bottom of the queue.
        return 1;
    }
}
