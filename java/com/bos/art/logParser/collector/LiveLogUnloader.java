/*
 * Created on Oct 21, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package com.bos.art.logParser.collector;

import com.bos.art.logParser.broadcast.network.CommunicationChannel;
import com.bos.art.logParser.db.QueryParamCleaner;
import com.bos.art.logParser.records.ILiveLogParserRecord;
import com.bos.art.logParser.records.ILiveLogPriorityQueueMessage;
import com.bos.art.logParser.records.SystemTask;
import com.bos.art.logParser.statistics.StatisticsModule;
import com.bos.art.logParser.statistics.StatisticsUnit;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventHandler;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.log4j.Logger;

import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author I0360D3
 *         <p/>
 *         To change the template for this generated type comment go to Window>Preferences>Java>Code Generation>Code and Comments
 */
public class LiveLogUnloader implements Runnable {

    private static final String ST_LIST_STATISTICS_MODULES = "LISTSTATUNITS";
    private static final String ST_LOAD_STAT_UNIT = "LOADSTATUNIT";
    private static final String ST_START_HEAP_THREAD = "STARTHEAPTHREAD";
    private static final String ST_STOP_HEAP_THREADS = "STOPHEAPTHREADS";
    private static final String ST_START_DB_THEAD = "STARTDBTHREAD";
    private static final String ST_STOP_DB_THREADS = "STOPDBTHREADS";
    private static final String ST_START_QUERY_PARAM_PROCESSOR = "STARTQUERYPARAMPROCESSOR";
    private static final String ST_START_QUERY_PARAM_UNLOADER = "STARTQUERYPARAMUNLOADER";
    private static final String ST_START_QUERY_PARAM_CLEANER = "STARTQUERYPARAMCLEANER";
    private static final String ST_STOP_QUERY_PARAM_CLEANER = "STOPQUERYPARAMCLEANER";
    private static final String ST_PRINT_QUERY_PARM_PROC = "PrintQueryParmProc";
    private static final String ST_PRINT_QUERY_PARM_UNLOADER = "PrintQueryParmUnloader";
    private static final String ST_PRINT_DB_WRITE_QUEUE = "PrintDBWriteQueue";
    private static final String ST_UNLOAD_STAT_UNIT = "UNLOADSTATUNIT";
    private static final String ST_PRINT_STATISTICS_STATS = "PRINTSTATISTICS";
    private static final String ST_PRINT_BY_STAT_UNIT = "PRINTBYUNIT";
    private static final String ST_SET_OUTPUT_FILE = "SETOUTPUTFILE";
    private static final String ST_FLUSH_ALL_UNITS = "FLUSHALLUNITS";
    private static final String ST_FLUSH_UNIT = "FLUSHUNIT";
    private static final String ST_SHUTDOWN = "SHUTDOWN";
    private static final String ST_GC = "GC";
    private static final String ST_MEMORY_STATS = "MEMORYSTATS";
    private static Logger logger = (Logger) Logger.getLogger(LiveLogUnloader.class.getName());
    private static Logger systemTaskLogger = (Logger) Logger.getLogger("systemTaskLogger");
    private static boolean unloadHeap = true;
    private static int statUnitCounter = 0;
    private static int systemTaskUnloader = 1;
    private LiveLogPriorityQueue queue = null;
    private boolean runstate = false;
    private DatabaseWriteQueue databaseWriteQueue = new DatabaseWriteQueue();

    private LiveLogUnloader() {
        queue = LiveLogPriorityQueue.getInstance();
    }

    public LiveLogUnloader(LiveLogPriorityQueue parmQueue, boolean rs) {
        runstate = rs;
        queue = parmQueue;
    }

    public static void startDBThread() {
//        DatabaseWriteQueue.unloadDB = true;
//
//        BasicThreadFactory factory = new BasicThreadFactory.Builder()
//                .namingPattern("Database-Unloader No.-%d")
//                .build();
//
//        ExecutorService executor = Executors.newSingleThreadExecutor(factory);
//        executor.execute(DatabaseWriteQueue.getInstance());
    }

    ;

    public static void startQueryParamCleaner() {
        QueryParamCleaner.shouldContinue = true;
        BasicThreadFactory factory = new BasicThreadFactory.Builder()
                .namingPattern("QueryParamCleaner")
                .build();

        ExecutorService executor = Executors.newSingleThreadExecutor(factory);
        executor.execute(new QueryParamCleaner());
    }

    /*
     * (non-Javadoc) @see java.lang.Runnable#run()
     */

    public static void stopQueryParamCleaner() {
        QueryParamCleaner.shouldContinue = false;
    }

    public static void stopDBThreads() {
        DatabaseWriteQueue.unloadDB = false;
    }

    public static void startHeapThread() {
        unloadHeap = true;
        LiveLogUnloader llu = new LiveLogUnloader();

        BasicThreadFactory factory = new BasicThreadFactory.Builder()
                .namingPattern("Heap-Unloader No.-%d")
                .build();

        ExecutorService executor = Executors.newSingleThreadExecutor(factory);
        executor.execute(llu);
    }

    public static void startQueryParamProcessor() {
//        QueryParameterProcessingQueue qppq = QueryParameterProcessingQueue.getInstance();
//
//        BasicThreadFactory factory = new BasicThreadFactory.Builder()
//                .namingPattern("QueryParam-Proccessor No.-%d")
//                .build();
//
//        ExecutorService executor = Executors.newSingleThreadExecutor(factory);
//        executor.execute(qppq);
//
//        logger.warn("Starting QueryParamProcessor ..." + (queryParamProccessingQueue - 1));
        //t.start();
    }

    public static void startQueryParamUnloader() {
//        QueryParameterWriteQueue qpwq = QueryParameterWriteQueue.getInstance();
//
//        BasicThreadFactory factory = new BasicThreadFactory.Builder()
//                .namingPattern("QueryParam-Unloader No. -%d")
//                .build();
//
//        ExecutorService executor = Executors.newSingleThreadExecutor(factory);
//        executor.execute(qpwq);
//
//        logger.warn("Starting QueryParamUnloader ..." + (queryParamUnloader - 1));
    }

    public static void startSystemTaskThread() {
        BasicThreadFactory factory = new BasicThreadFactory.Builder()
                .namingPattern("SystemTask-Unloader No. -%d")
                .build();

        ExecutorService executor = Executors.newSingleThreadExecutor(factory);
        executor.execute(new LiveLogUnloader(LiveLogPriorityQueue.getSystemTaskQueue(), true));

    }

    public void run() {
        CommunicationChannel channel = CommunicationChannel.getInstance();
        StatisticsModule sm = StatisticsModule.getInstance();

        while (unloadHeap || runstate) {
            if (logger.isDebugEnabled()) {
                logger.debug("Starting the LogUnloader.");
            }
            ILiveLogPriorityQueueMessage llpr = queue.getFirst();

            if (logger.isInfoEnabled()) {
                if (llpr.getPriority() != 20) {
                    logger.debug(
                            "Unloader Priority: " + llpr.getPriority() + " : " + llpr.toString() + ":Time:"
                                    + System.currentTimeMillis());
                }
            }
            if (llpr instanceof ILiveLogParserRecord) {
                Iterator iter = sm.iterator();

                while (iter.hasNext()) {
                    if (logger.isDebugEnabled()) {
                        logger.debug(" Process Record called!");
                    }
                    ((StatisticsUnit) iter.next()).processRecord((ILiveLogParserRecord) llpr);
                }
                //DatabaseWriteQueue.getInstance().addLast(llpr);
                // FileWriteQueue.getInstance().addLast(llpr);
            } else if (llpr instanceof SystemTask) {
                logger.debug("System Task Found " + ((SystemTask) llpr).getTask());
                performSystemTask((SystemTask) llpr);
            }
        }
    }

    private void performSystemTask(SystemTask st) {
        String taskString = st.getTask();

        if (taskString == null || taskString.trim().length() == 0) {
            logger.warn("performSystemTask Called  with an EMPTY Task String: " + taskString);
            return;
        }
        StringTokenizer tokenizer = new StringTokenizer(taskString, "=");
        String task = tokenizer.nextToken();
        String instruction = "";

        if (tokenizer.hasMoreTokens()) {
            instruction = tokenizer.nextToken();
        }
        logger.warn("performSystemTask Called : " + taskString);
        logger.warn("performSystemTask task:instruction " + task + ":" + instruction);
        if (task.equalsIgnoreCase(ST_LIST_STATISTICS_MODULES)) {
            listStatUnits();
        } else if (task.equalsIgnoreCase(ST_LOAD_STAT_UNIT)) {
            String clazzName = instruction;

            systemTaskLogger.info("performSystemTask Called : " + taskString);
            systemTaskLogger.info("performSystemTask task:instruction " + task + ":" + instruction);
            systemTaskLogger.info("Loading : " + clazzName);
            if (clazzName.length() > 0) {

                loadStatUnit(clazzName);
            }
        } else if (task.equalsIgnoreCase(ST_UNLOAD_STAT_UNIT)) {
            String clazzName = instruction;

            systemTaskLogger.info("Unloading : " + clazzName);
            if (clazzName.length() > 0) {
                unLoadStatUnit(clazzName);
            }
        } else if (task.equalsIgnoreCase(ST_PRINT_STATISTICS_STATS)) {
            logger.debug("calling printAllStats");
            printAllStats();
        } else if (task.equalsIgnoreCase(ST_PRINT_BY_STAT_UNIT)) {
            String clazzName = instruction;

            if (clazzName.length() > 0) {
                printStatUnit(clazzName);
            }
        } else if (task.equalsIgnoreCase(ST_SET_OUTPUT_FILE)) {
            String fileName = instruction;

            if (fileName.length() > 0) {
                setOutputFile(fileName);
            }
        } else if (task.equalsIgnoreCase(ST_FLUSH_ALL_UNITS)) {
            flushAllUnits();
        } else if (task.equalsIgnoreCase(ST_FLUSH_UNIT)) {
            String fileName = instruction;

            if (fileName.length() > 0) {
                setOutputFile(fileName);
            }
        } else if (task.equalsIgnoreCase(ST_SHUTDOWN)) {
            shutdown();
        } else if (task.equalsIgnoreCase(ST_GC)) {
            System.gc();
        } else if (task.equalsIgnoreCase(ST_MEMORY_STATS)) {
            printMemory();
        } else if (task.equalsIgnoreCase(ST_START_HEAP_THREAD)) {
            startHeapThread();
        } else if (task.equalsIgnoreCase(ST_START_DB_THEAD)) {
            startDBThread();
        } else if (task.equalsIgnoreCase(ST_STOP_HEAP_THREADS)) {
            stopHeapThreads();
        } else if (task.equalsIgnoreCase(ST_STOP_DB_THREADS)) {
            stopDBThreads();
        } else if (task.equalsIgnoreCase(ST_START_QUERY_PARAM_PROCESSOR)) {
            startQueryParamProcessor();
        } else if (task.equalsIgnoreCase(ST_START_QUERY_PARAM_UNLOADER)) {
            startQueryParamUnloader();
        } else if (task.equalsIgnoreCase(ST_PRINT_DB_WRITE_QUEUE)) {
            printDBWriteQueue();
        } else if (task.equalsIgnoreCase(ST_PRINT_QUERY_PARM_PROC)) {
            printQueryParmProc();
        } else if (task.equalsIgnoreCase(ST_PRINT_QUERY_PARM_UNLOADER)) {
            printQueryParmUnloader();
        } else if (task.equals(ST_START_QUERY_PARAM_CLEANER)) {
            startQueryParamCleaner();
        } else if (task.equals(ST_STOP_QUERY_PARAM_CLEANER)) {
            stopQueryParamCleaner();
        }

    }

    private void printDBWriteQueue() {
        logger.warn("------------------------------------------------------------------");
        logger.warn("-----------------Database Write Queue ----------------------------");
        //logger.warn(DatabaseWriteQueue.getInstance().toString());
        logger.warn("-----------------Database Write Queue ----------------------------");
        logger.warn("------------------------------------------------------------------");
    }

    private void printQueryParmUnloader() {
        logger.warn("------------------------------------------------------------------");
        logger.warn("-----------------Query ParameterUnloader  Queue ------------------");
        logger.warn(QueryParameterWriteQueue.getInstance().toString());
        logger.warn("-----------------Query ParameterUnloader  Queue ------------------");
        logger.warn("------------------------------------------------------------------");
    }

    private void printQueryParmProc() {
        logger.warn("------------------------------------------------------------------");
        logger.warn("-----------------Query ParameterProcessing Queue------------------");
        logger.warn(QueryParameterProcessingQueue.getInstance().toString());
        logger.warn("-----------------Query ParameterProcessing Queue------------------");
        logger.warn("------------------------------------------------------------------");
    }

    private void stopHeapThreads() {
        unloadHeap = false;
    }

    private void listStatUnits() {
        systemTaskLogger.info("\n ------------------------\n Currently Loaded Stat Units: ");
        StatisticsModule sm = StatisticsModule.getInstance();
        Iterator iter = sm.iterator();
        int i = 0;

        while (iter.hasNext()) {
            systemTaskLogger.info("\t" + i++ + ":\t" + iter.next().getClass().getName());
        }
        systemTaskLogger.info("------------------------");
    }

    private void loadStatUnit(String unitClassName) {
        systemTaskLogger.info("Loading: " + unitClassName);
        StatisticsModule sm = StatisticsModule.getInstance();

        try {
            StatisticsUnit statisticsUnit = ((StatisticsUnit) (Class.forName(unitClassName).newInstance()));

            statisticsUnit.setInstance(statisticsUnit);
            ++statUnitCounter;
            logger.warn("LiveLogUnloader statunit load counter: " + statUnitCounter + " name: " + unitClassName);

            if (statisticsUnit instanceof StatisticsUnit) {
                sm.addStatUnit(statisticsUnit);
                systemTaskLogger.info("\tLoaded: " + unitClassName);
            }
        } catch (Exception e) {
            logger.error("Error Loading Statistics Unit ", e);
        }
    }

    private void unLoadStatUnit(String unitClazzName) {
        systemTaskLogger.info("UnLoading: " + unitClazzName);
        StatisticsModule sm = StatisticsModule.getInstance();

        try {
            Iterator iter = sm.iterator();

            while (iter.hasNext()) {
                StatisticsUnit su = (StatisticsUnit) iter.next();

                if (su.getClass().getName().equals(unitClazzName)) {
                    sm.removeStatUnit(su);
                    return;
                }
            }
        } catch (Exception e) {
            logger.error("Error Loading Statistics Unit ", e);
        }
    }

    private void printAllStats() {
        logger.info("printAllStats Called");
        StatisticsModule sm = StatisticsModule.getInstance();
        Iterator iter = sm.iterator();

        while (iter.hasNext()) {
            StatisticsUnit su = (StatisticsUnit) (iter.next());

            systemTaskLogger.info(su.toString());
        }
    }

    private void printStatUnit(String statUnit) {
        logger.info("printStatUnit called");
        logger.warn("printing for " + statUnit);
        StatisticsModule sm = StatisticsModule.getInstance();

        try {
            Iterator iter = sm.iterator();

            while (iter.hasNext()) {
                StatisticsUnit su = (StatisticsUnit) iter.next();

                if (su.getClass().getName().equals(statUnit)) {
                    // sm.removeStatUnit(su);
                    logger.warn("printing for " + statUnit);
                    logger.warn(su.toString());
                    return;
                }
            }
        } catch (Exception e) {
            logger.error("Error Loading Statistics Unit ", e);
        }
    }

    private void setOutputFile(String fileName) {
        logger.info("setOutputFile called (Currently this does nothing)");
    }

    private void flushAllUnits() {
        logger.info("flushAllUnits Called");
        StatisticsModule sm = StatisticsModule.getInstance();
        Iterator iter = sm.iterator();

        while (iter.hasNext()) {
            StatisticsUnit su = (StatisticsUnit) (iter.next());

            su.flush();
        }
    }

    private void flushUnit(String statUnit) {
        logger.info("flushUnit called on :" + statUnit);
        StatisticsModule sm = StatisticsModule.getInstance();

        try {
            Iterator iter = sm.iterator();

            while (iter.hasNext()) {
                StatisticsUnit su = (StatisticsUnit) iter.next();

                if (su.getClass().getName().equals(statUnit)) {
                    su.flush();
                    return;
                }
            }
        } catch (Exception e) {
            logger.error("Error Loading Statistics Unit ", e);
        }
    }

    private void shutdown() {
        logger.info("shutdonw Called");
    }

    private void printMemory() {
        systemTaskLogger.info("--------------------------------------------");
        systemTaskLogger.info("----Message Queue--------------------------");
        systemTaskLogger.info("--------------------------------------------");
        systemTaskLogger.info(LiveLogPriorityQueue.getInstance().toString());
        systemTaskLogger.info("--------------------------------------------");
        systemTaskLogger.info("----System Task Queue-----------------------");
        systemTaskLogger.info("--------------------------------------------");
        systemTaskLogger.info(LiveLogPriorityQueue.getSystemTaskQueue().toString());
        long totalMemory = Runtime.getRuntime().totalMemory();
        long freeMemory = Runtime.getRuntime().freeMemory();

        systemTaskLogger.info("    Memory   total--free  : " + totalMemory / 1024 + "K--" + freeMemory / 1024 + "K");
        systemTaskLogger.info("             Used Memory  : " + (totalMemory / 1024 - freeMemory / 1024) + "K:");
    }

    public static class ObjectEvent {
        public static final EventFactory<ObjectEvent> FACTORY = new EventFactory<ObjectEvent>() {
            public ObjectEvent newInstance() {
                return new ObjectEvent();
            }
        };
        public Object record;
    }

    public static class ObjectEventHandler implements EventHandler<ObjectEvent> {
        LiveLogUnloader liveLogUnloader = new LiveLogUnloader();
        static DatabaseWriteQueue databaseWriteQueue = new DatabaseWriteQueue();

        public ObjectEventHandler() {
        }

        public void onEvent(ObjectEvent pevent, long sequence, boolean endOfBatch) throws Exception {
            StatisticsModule sm = StatisticsModule.getInstance();

            Object event = pevent.record;
            ILiveLogPriorityQueueMessage llpr = (ILiveLogPriorityQueueMessage) event;

            //logger.warn("got event: " + event);

            if (logger.isInfoEnabled()) {
                if (llpr.getPriority() != 20) {
                    logger.debug(
                            "Unloader Priority: " + llpr.getPriority() + " : " + llpr.toString() + ":Time:"
                                    + System.currentTimeMillis());
                }
            }
            if (llpr instanceof ILiveLogParserRecord) {
                Iterator iter = sm.iterator();

                while (iter.hasNext()) {
                    if (logger.isDebugEnabled()) {
                        logger.debug(" Process Record called!");
                    }
                    ((StatisticsUnit) iter.next()).processRecord((ILiveLogParserRecord) llpr);
                }
                //DatabaseWriteQueue.getInstance().addLast(llpr);
                databaseWriteQueue.addLast(llpr);
                // FileWriteQueue.getInstance().addLast(llpr);
            } else if (llpr instanceof SystemTask) {
                logger.debug("System Task Found " + ((SystemTask) llpr).getTask());
                liveLogUnloader.performSystemTask((SystemTask) llpr);
            }

        }

    }
}
