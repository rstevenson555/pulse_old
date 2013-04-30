/*
 * Created on Oct 21, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package com.bos.art.logParser.collector;


import EDU.oswego.cs.dl.util.concurrent.BoundedPriorityQueue;
import com.bos.art.logParser.records.ILiveLogPriorityQueueMessage;
import java.io.Serializable;
import org.apache.log4j.Logger;


/**
 * @author I0360D3
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class LiveLogPriorityQueue implements Serializable {
    private static final int BOUNDED_QUEUE_CAPACITY = 2000;
    private BoundedPriorityQueue queue = new BoundedPriorityQueue(BOUNDED_QUEUE_CAPACITY, new PriorityQueueComparator());
    private static final Logger heapLogger = (Logger) Logger.getLogger(LiveLogPriorityQueue.class.getName());
    private long addTime;
    private long removeTime;
    private int objectsAdded;
    private int objectsRemoved;
    private int inappropriateObjects;
    public static final int PRINT_FREQUENCY = 100000;
    private int maxDepth;
    private static LiveLogPriorityQueue instance;
    private static LiveLogPriorityQueue systemTaskInstance;

    private LiveLogPriorityQueue() {}

    synchronized public static LiveLogPriorityQueue getInstance() {
        if (instance == null) {
            instance = new LiveLogPriorityQueue();
        }
        return instance;
    }
	
    synchronized public static LiveLogPriorityQueue getSystemTaskQueue() {
        if (systemTaskInstance == null) {
            systemTaskInstance = new LiveLogPriorityQueue();
        }
        return systemTaskInstance;
    }

    public void addObject(Object o) {
        long startTime = System.currentTimeMillis();

        if (o instanceof ILiveLogPriorityQueueMessage) {
            try {              
                queue.put(o);
                
            } catch (Exception e) {
                e.printStackTrace();
            }
            ++objectsAdded;
        } else {
            ++inappropriateObjects;
            if (heapLogger.isDebugEnabled()) {
                heapLogger.debug(o.toString());
            }
        }
        if (maxDepth < (objectsAdded - objectsRemoved)) {
            maxDepth = objectsAdded - objectsRemoved;
        }
        if (objectsAdded % PRINT_FREQUENCY == 0) {
            heapLogger.info(this.toString());
        }
        addTime += (System.currentTimeMillis() - startTime);
    }

    public ILiveLogPriorityQueueMessage getFirst() {
        try {
            if (objectsRemoved % PRINT_FREQUENCY == 0) {
                heapLogger.info(this.toString());
            }
            long startTime = System.currentTimeMillis();
            // if empty wait until someone puts something in
            
            Object o = queue.take();

            ++objectsRemoved;
            if (o instanceof ILiveLogPriorityQueueMessage) {
                removeTime += (System.currentTimeMillis() - startTime);
                return (ILiveLogPriorityQueueMessage) o;
            }
            removeTime += (System.currentTimeMillis() - startTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean isEmpty() {
        return queue.size() == 0;
    }
	
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("LiveLogPriorityQueue\n").append("\n\tItems Added.......................").append(objectsAdded).append("\n\t\tAdd Time..................").append(addTime).append("\n\tItems Removed.....................").append(objectsRemoved).append("\n\t\tRemove Time...............").append(removeTime).append("\n\tHeap Depth .......................").append(queue.size()).append("\n\tMax Heap Depth....................").append(
                maxDepth);
        return sb.toString();
    }
	
}