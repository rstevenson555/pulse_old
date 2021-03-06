/*
 * Created on Oct 21, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package com.bos.art.logParser.collector;


import com.bos.art.logParser.records.QueryParameters;
import com.bos.art.logServer.utils.TPSCalculator;
import com.bos.helper.SingletonInstanceHelper;
import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.Util;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.log4j.Logger;

import javax.management.*;
import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;


/**
 * @author I0360D3
 *         <p/>
 *         To change the template for this generated type comment go to
 *         Window>Preferences>Java>Code Generation>Code and Comments
 */
public class QueryParameterProcessingQueue implements QueryParameterProcessingQueueMBean, Serializable {
    private static final Logger logger = (Logger) Logger.getLogger(QueryParameterProcessingQueue.class.getName());
    private static final int MAX_DB_QUEUE_SIZE = 3000;
    protected static boolean unloadDB = true;
    private static SingletonInstanceHelper instance = new SingletonInstanceHelper<QueryParameterProcessingQueue>(QueryParameterProcessingQueue.class) {
        @Override
        public java.lang.Object createInstance() {
            return new QueryParameterProcessingQueue();
        }
    };
    private static long fullCount = 0;
    private static BasicThreadFactory tFactory = new BasicThreadFactory.Builder()
            .namingPattern("QueryParameterProcessingQueue-%d")
            .build();
    private final ExecutorService executor = Executors.newSingleThreadExecutor(tFactory);
    private static TPSCalculator tpsCalculator = new TPSCalculator();
    private static AtomicInteger qpInstance = new AtomicInteger(0);
    private int objectsRemoved;
    private int objectsProcessed;
    private AtomicLong totalSysTime = new AtomicLong(0);

    /**
     * create and start the disruptor to process query parameters.
     */
    private QueryParameterProcessingQueue() {

        disruptor.handleExceptionsWith(new FatalExceptionHandler());

        QueryParametersEventHandler handler = new QueryParametersEventHandler();
        disruptor.handleEventsWith(handler);
        disruptor.start();

        registerWithMBeanServer();
    }

    private Disruptor<QueryParametersEvent> disruptor = new Disruptor<QueryParametersEvent>(QueryParametersEvent.FACTORY, 512, executor,
            ProducerType.SINGLE, new BlockingWaitStrategy());

    public static QueryParameterProcessingQueue getInstance() {
        return (QueryParameterProcessingQueue) instance.getInstance();
    }

    public long size() {
        return (disruptor.getRingBuffer().getBufferSize() - disruptor.getRingBuffer().remainingCapacity());
    }

    /**
     * register the mbean with JMX
     */
    private void registerWithMBeanServer() {
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        ObjectName name = null;
        try {
            name = new ObjectName("com.omx.engine:type=QueryParameterProcessingQueueMBean,name=QueryParameterProcessingQueue-" + (qpInstance.incrementAndGet()));
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

    public void addLast(Object o) {
        addLast((QueryParameters) o);
    }

    public void addLast(final QueryParameters o) {
//        boolean success = disruptor.getRingBuffer().tryPublishEvent(new EventTranslator<QueryParametersEvent>() {
//            public void translateTo(QueryParametersEvent event, long sequence) {
//                event.record = o;
//            }
//
//        });
        disruptor.publishEvent(new EventTranslator<QueryParametersEvent>() {
            public void translateTo(QueryParametersEvent event, long sequence) {
                event.record = o;
            }

        });
//        if (!success && (fullCount++ % 100) == 0) {
//            logger.error("Failed adding to the QueryParameterProcessingQueue Queue: ");
//        }
    }

    // JMX Interface exposed
    public long getBufferSize() {
        return disruptor.getBufferSize();
    }

    /**
     * change the buffer size to nearest power of two
     *
     * @param sz
     */
    public void setBufferSize(long sz) {
        disruptor.shutdown();

        int psize = Util.ceilingNextPowerOfTwo((int) sz);

        disruptor = new Disruptor<QueryParametersEvent>(QueryParametersEvent.FACTORY, psize, executor,
                ProducerType.SINGLE, new BlockingWaitStrategy());

        disruptor.handleExceptionsWith(new FatalExceptionHandler());

        QueryParametersEventHandler handler = new QueryParametersEventHandler();
        disruptor.handleEventsWith(handler);
        disruptor.start();
    }

    /**
     * get the remaining capacity of the ringbuffer
     *
     * @return
     */
    public long getRemainingCapacity() {
        return disruptor.getRingBuffer().remainingCapacity();
    }

    /**
     * return messages per second calculation
     *
     * @return
     */
    public long getMessagesPerSecond() {
        return tpsCalculator.getMessagesPerSecond();
    }

    public long getWriteCount() {
        return tpsCalculator.getTransactionCount();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("QueryParameterProcessing Queue size:");
        sb.append(size());
        sb.append("disruptor cursor: " + disruptor.getCursor());
        sb.append("\t\t this thread: ");
        sb.append(Thread.currentThread().getName());
        sb.append("\n\tObjects Popped              :  ").append(objectsRemoved);
        sb.append("\n\tObjects Processed           :  ").append(objectsProcessed);
        if (objectsProcessed > 1000) {
            sb.append("\n\tProc Time millis per 1000  :  ").append(totalSysTime.get() / (objectsProcessed / 1000));
        } else {
            sb.append("\n\tProc Time millis per 1000  :  0");
        }
        return sb.toString();
    }

    private static class QueryParametersEvent {
        public static final EventFactory<QueryParametersEvent> FACTORY = new EventFactory<QueryParameterProcessingQueue.QueryParametersEvent>() {
            public QueryParametersEvent newInstance() {
                return new QueryParametersEvent();
            }
        };
        private QueryParameters record;
    }

    private class QueryParametersEventHandler implements EventHandler<QueryParametersEvent> {

        public QueryParametersEventHandler() {
        }

        public void onEvent(QueryParametersEvent event, long sequence, boolean endOfBatch) throws Exception {
            if (logger.isInfoEnabled()) {
                if (objectsRemoved % 10000 == 0) {
                    logger.info(toString());
                }
            }
            QueryParameters qp = event.record;

            ++objectsRemoved;

            tpsCalculator.incrementTransaction();

            long sTime = System.currentTimeMillis();

            String str = qp.processQueryParameters();
            qp.writeQueryParameter(str);

            long sTime2 = System.currentTimeMillis();

            ++objectsProcessed;
            //totalSysTime += (sTime2 - sTime);
            totalSysTime.set((sTime2 - sTime));
        }
    }


}

