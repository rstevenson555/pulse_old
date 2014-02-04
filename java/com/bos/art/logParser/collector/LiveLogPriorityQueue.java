/*
 * Created on Oct 21, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package com.bos.art.logParser.collector;


import EDU.oswego.cs.dl.util.concurrent.BoundedPriorityQueue;
import com.bos.art.logParser.records.ILiveLogParserRecord;
import com.bos.art.logParser.records.ILiveLogPriorityQueueMessage;
import java.io.Serializable;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.bos.art.logParser.records.SystemTask;
import com.bos.art.logParser.statistics.StatisticsModule;
import com.bos.art.logParser.statistics.StatisticsUnit;
import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.log4j.Logger;


/**
 * @author I0360D3
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class LiveLogPriorityQueue implements Serializable {
    private static Logger logger = (Logger) Logger.getLogger(LiveLogPriorityQueue.class.getName());
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
    private static AtomicBoolean instanceLock = new AtomicBoolean(false);
    private static AtomicBoolean instanceTaskLock = new AtomicBoolean(false);
    private BasicThreadFactory tFactory = new BasicThreadFactory.Builder()
                .namingPattern("LiveLogProcessor-%d")
                .build();

    private final ExecutorService disruptorExecutor = Executors.newSingleThreadExecutor(tFactory);
    private Disruptor<ObjectEvent> disruptor = new Disruptor<ObjectEvent>(ObjectEvent.FACTORY, 4 * 1024, disruptorExecutor,
            ProducerType.SINGLE, new SleepingWaitStrategy());

    private static int NUM_DB_WRITE_HANDLERS = 1;
    private static LiveLogPriorityQueueHandler logPriorityQueueHandlers[] = new LiveLogPriorityQueueHandler[NUM_DB_WRITE_HANDLERS];
    private static BasicThreadFactory writeTFactory = new BasicThreadFactory.Builder()
            .namingPattern("LiveLogPriorityQueueHandler-%d")
            .build();
    // this controls how wide to scale the message handlers (very critical)
    private static ExecutorService dbWriteQueuePool = Executors.newFixedThreadPool(NUM_DB_WRITE_HANDLERS,writeTFactory);

    public static class ObjectEvent {
        public Object record;

        public static final EventFactory<ObjectEvent> FACTORY = new EventFactory<ObjectEvent>() {
            public ObjectEvent newInstance() {
                return new ObjectEvent();
            }
        };
    };

    private class LiveLogPriorityQueueHandler implements Runnable {

        ILiveLogPriorityQueueMessage iLiveLogPriorityQueueMessage;
        DatabaseWriteQueue databaseWriteQueue = new DatabaseWriteQueue();

        public void setPriorityQueueMessage(Object t) { iLiveLogPriorityQueueMessage = (ILiveLogPriorityQueueMessage)t;}

        public void run() {
            databaseWriteQueue.addLast(iLiveLogPriorityQueueMessage);
        }
    }

    static private Object initSyncLock = new Object ();

    public void initHandlers() {
        logger.info("LiveLogPriorityQueue.initHandlers");
        synchronized (initSyncLock) {
            for(int i = 0;i< logPriorityQueueHandlers.length;i++) {
                if ( logPriorityQueueHandlers[i]==null)
                    logPriorityQueueHandlers[i] = new LiveLogPriorityQueueHandler();
            }
        }
    }

    private static AtomicInteger handlerCount = new AtomicInteger(0);

    private LiveLogPriorityQueueHandler setNextHandler(ILiveLogPriorityQueueMessage iLiveLogPriorityQueueMessage) {

        int current = handlerCount.get();
        logPriorityQueueHandlers[current].setPriorityQueueMessage(iLiveLogPriorityQueueMessage);

        int nextHandlerCount = handlerCount.incrementAndGet();
        if ( nextHandlerCount > logPriorityQueueHandlers.length-1) {
            nextHandlerCount = 0;
            handlerCount.set(nextHandlerCount);
        }

        return logPriorityQueueHandlers[current];
    }

    public class ObjectEventHandler implements EventHandler<ObjectEvent> {
        LiveLogUnloader unloader = new LiveLogUnloader();

        public ObjectEventHandler() {
        }

        public void onEvent(ObjectEvent pevent, long sequence, boolean endOfBatch) throws Exception {
            StatisticsModule sm = StatisticsModule.getInstance();
            Object event = pevent.record;

            event = pevent.record;
            ILiveLogPriorityQueueMessage iLiveLogPriorityQueueMessage = (ILiveLogPriorityQueueMessage)event;

            if (logger.isInfoEnabled()) {
                if (iLiveLogPriorityQueueMessage.getPriority() != 20) {
                    logger.debug(
                            "Unloader Priority: " + iLiveLogPriorityQueueMessage.getPriority() + " : " + iLiveLogPriorityQueueMessage.toString() + ":Time:"
                                    + System.currentTimeMillis());
                }
            }
            if (iLiveLogPriorityQueueMessage instanceof ILiveLogParserRecord) {
                Iterator iter = sm.iterator();

                while (iter.hasNext()) {
                    if (logger.isDebugEnabled()) {
                        logger.debug(" Process Record called!");
                    }
                    ((StatisticsUnit) iter.next()).processRecord((ILiveLogParserRecord) iLiveLogPriorityQueueMessage);
                }
//                DatabaseWriteQueue.getInstance().addLast(llpr);
                LiveLogPriorityQueueHandler liveLogPriorityQueueHandler = setNextHandler(iLiveLogPriorityQueueMessage);
                dbWriteQueuePool.execute(liveLogPriorityQueueHandler);

                // FileWriteQueue.getInstance().addLast(llpr);
            } else if (iLiveLogPriorityQueueMessage instanceof SystemTask) {
                logger.debug("System Task Found " + ((SystemTask) iLiveLogPriorityQueueMessage).getTask());
                unloader.performSystemTask((SystemTask) iLiveLogPriorityQueueMessage);
            }

        }
    }

    private LiveLogPriorityQueue() {
        disruptor.handleExceptionsWith(new FatalExceptionHandler());

        ObjectEventHandler handler = new ObjectEventHandler();
        disruptor.handleEventsWith(handler);
        disruptor.start();
        initHandlers();

    }

    public static LiveLogPriorityQueue getInstance() {
        if (instanceTaskLock.compareAndSet(false,true)){
            instance = new LiveLogPriorityQueue();
        }
        return instance;
    }
	
    public static LiveLogPriorityQueue getSystemTaskQueue() {
        if (instanceLock.compareAndSet(false,true)){
            systemTaskInstance = new LiveLogPriorityQueue();
        }
        return systemTaskInstance;
    }

    public void addObject(final Object o) {
        long startTime = System.currentTimeMillis();

        if (o instanceof ILiveLogPriorityQueueMessage) {
            try {              
                //queue.put(o);                
                disruptor.publishEvent(new EventTranslator<ObjectEvent>() {
                    public void translateTo(ObjectEvent event, long sequence) {
                        event.record = o;
                    }
                });
                
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
