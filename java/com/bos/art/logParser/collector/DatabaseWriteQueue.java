/*
 * Created on Oct 21, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package com.bos.art.logParser.collector;


import com.bos.art.logParser.records.ILiveLogParserRecord;
import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.util.concurrent.*;

import com.bos.art.logServer.utils.TPSCalculator;
import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;
import com.lmax.disruptor.util.Util;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.log4j.Logger;

import javax.management.*;

/**
 * @author I0360D3
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class DatabaseWriteQueue implements DatabaseWriteQueueMBean,Serializable {
    private static final Logger logger = (Logger) Logger.getLogger(DatabaseWriteQueue.class.getName());
//    private static DatabaseWriteQueue instance = new DatabaseWriteQueue();
    private int objectsRemoved;
    private int objectsWritten;                 
    private long totalWriteTime;
    protected static boolean unloadDB = true;
    private static final int MAX_DB_QUEUE_SIZE = 4500;
    private BasicThreadFactory tFactory = new BasicThreadFactory.Builder()
                .namingPattern("DatabaseWriteQueue-%d")
                .build();

    private final ExecutorService executor = Executors.newSingleThreadExecutor(tFactory);
    private static TPSCalculator tpsCalculator = new TPSCalculator();

    private Disruptor<ILiveLogParserRecordEvent> disruptor = new Disruptor<ILiveLogParserRecordEvent>(ILiveLogParserRecordEvent.FACTORY, 2*1024, executor,
                ProducerType.SINGLE, new SleepingWaitStrategy());

    // guards for boundaries

    private static class ILiveLogParserRecordEvent
    {
        private ILiveLogParserRecord record;

        public static final EventFactory<ILiveLogParserRecordEvent> FACTORY = new EventFactory<ILiveLogParserRecordEvent>()
        {
            public ILiveLogParserRecordEvent newInstance()
            {
                return new ILiveLogParserRecordEvent();
            }
        };
    };

    private class LiveLogParserRecordEventHandler implements EventHandler<ILiveLogParserRecordEvent>
    {
        public LiveLogParserRecordEventHandler()
        {
        }

        public void onEvent(ILiveLogParserRecordEvent event, long sequence, boolean endOfBatch) throws Exception
        {
            ILiveLogParserRecord ilpr = event.record;
            ++objectsRemoved;

            long writeStartTime = System.currentTimeMillis();

            tpsCalculator.incrementTransaction();

            ilpr.writeToDatabase();
            totalWriteTime += (System.currentTimeMillis() - writeStartTime);
            ++objectsWritten;
        }
    }

    public DatabaseWriteQueue() {

        disruptor.handleExceptionsWith(new FatalExceptionHandler());

        LiveLogParserRecordEventHandler handler = new LiveLogParserRecordEventHandler();
        disruptor.handleEventsWith(handler);
        disruptor.start();

        registerWithMBeanServer();
    }

    // JMX Interface exposed
    public long getBufferSize() {
        return disruptor.getBufferSize();
    }

    /**
     * get the remaining capacity of the ringbuffer
     * @return
     */
    public long getRemainingCapacity() {
        return disruptor.getRingBuffer().remainingCapacity();
    }

    /**
     * change the buffer size to nearest power of two
     * @param sz
     */
    public void setBufferSize(long sz) {
        disruptor.shutdown();

        int psize = Util.ceilingNextPowerOfTwo((int) sz);

        disruptor = new Disruptor<ILiveLogParserRecordEvent>(ILiveLogParserRecordEvent.FACTORY, psize, executor,
                ProducerType.SINGLE, new SleepingWaitStrategy());
        disruptor.handleExceptionsWith(new FatalExceptionHandler());

        LiveLogParserRecordEventHandler handler = new LiveLogParserRecordEventHandler();
        disruptor.handleEventsWith(handler);
        disruptor.start();
    }

    /**
     * return messages per second calculation
     * @return
     */
    public long getMessagesPerSecond() {
        return tpsCalculator.getMessagesPerSecond();
    }

    public long getWriteCount() {
        return tpsCalculator.getTransactionCount();
    }

    private static int instance = 0;

    private void registerWithMBeanServer() {
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        ObjectName name = null;
        try {
            name = new ObjectName("com.omx.engine:type=DatabaseWriteQueueMBean,name=instance-"+(++instance));
            mbs.registerMBean(this, name);
        } catch (MalformedObjectNameException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (InstanceAlreadyExistsException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (MBeanRegistrationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (NotCompliantMBeanException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

//    public static DatabaseWriteQueue getInstance() {
//        return instance;
//    }


    public void addLast(Object o) {
        addLast((ILiveLogParserRecord)o);
    }

    public void addLast(final ILiveLogParserRecord o) {
        disruptor.publishEvent(new EventTranslator<ILiveLogParserRecordEvent>() {
            public void translateTo(ILiveLogParserRecordEvent event, long sequence)
            {
                event.record = o;
            }

        });
    }

    public long size() {
        return (disruptor.getRingBuffer().getBufferSize() - disruptor.getRingBuffer().remainingCapacity());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("Database Write Queue size:");
        sb.append(size());
        sb.append("disruptor cursor: " + disruptor.getCursor());       
        sb.append("\t\t this thread: ");
        sb.append(Thread.currentThread().getName());
        sb.append("\n\tObjects Popped              :  ").append(objectsRemoved);
        sb.append("\n\tObjects Written             :  ").append(objectsWritten);
        if (objectsWritten > 1000) {
            sb.append("\n\tWrite Time millis per 1000  :  ").append(totalWriteTime / (objectsWritten / 1000));
        } else {
            sb.append("\n\tWrite Time millis per 1000  :  0");
        }
        return sb.toString();
    }

}

