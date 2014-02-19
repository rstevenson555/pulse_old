/*
 * Created on Oct 23, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package com.bos.art.logParser.db;

import com.bos.art.logParser.broadcast.beans.BeanBag;
import com.bos.art.logParser.broadcast.beans.SessionDataBean;
import com.bos.art.logParser.broadcast.network.CommunicationChannel;
import com.bos.art.logParser.records.AccessRecordsForeignKeys;
import com.bos.helper.SingletonInstanceHelper;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.Serializable;
import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.logging.Level;

/**
 * @author I0360D3
 *         <p/>
 *         To change the template for this generated type comment go to Window>Preferences>Java>Code Generation>Code and Comments
 */
public class ForeignKeyStore extends TimerTask implements Serializable {

    public static final String FK_PAGES_PAGE_ID = "Page_ID";
    public static final String FK_USERS_USER_ID = "User_ID";
    public static final String FK_SESSIONS_SESSION_ID = "Session_ID";
    public static final String FK_MACHINES_MACHINE_ID = "Machine_ID";
    public static final String FK_INSTANCES_INSTANCE_ID = "Instance_ID";
    public static final String FK_CONTEXTS_CONTEXT_ID = "Context_ID";
    public static final String FK_DEPLOYEDAPPS_APP_ID = "App_ID";
    public static final String FK_BRANCH_TAG_ID = "Branch_Tag_ID";
    public static final String FK_QUERY_PARAMETER_ID = "QueryParameters_ID";
    public static final String FK_LOAD_TEST_SCRIPT_ID = "LoadTestScript_ID";
    public static final String FK_LOAD_TEST_TRANSACTION_ID = "LoadTestTransaction_ID";
    public static final String FK_STACKTRACEROW_ID = "StackTraceRow_ID";
    private static final String ALL_CONTEXTS = "ALL_CONTEXTS";
    private static final String BROWSER = "#BROWSER#";
    private static final String IPADDRESS = "#IPADDRESS#";
    private static final int SESSION_REMOVAL_MINUTES = 120;
    private static final String USERID = "#USERID#";
    private static final Logger logger = (Logger) Logger.getLogger(ForeignKeyStore.class.getName());
    private static final Logger browser_logger = (Logger) Logger.getLogger("com.bos.art.browser.log");
    private static final Logger FK_Cachelogger = (Logger) Logger.getLogger("com.bos.art.logparser.db.FK_Cachelogger");
    private static final DateTimeFormatter sdfMySQLTimestamp = DateTimeFormat.forPattern("yyyyMMddHHmmss");
    private static SingletonInstanceHelper instance = new SingletonInstanceHelper<AccumulatorEventPersistanceStrategy>(AccumulatorEventPersistanceStrategy.class);
    private static int sessionBroadcastCounter = 0;
    private static ThreadLocal threadLocalCon = new ThreadLocal() {

        @Override
        protected synchronized Object initialValue() {
            try {
                return ConnectionPoolT.getConnection();
            } catch (SQLException se) {
                logger.error("SQL Exception ", se);
            }
            return null;
        }
    };
    private final String UPDATE_SESSIONS =
            "UPDATE Sessions set "
                    + "IPAddress = ?, "
                    + "browserType = ?, "
                    + "User_ID = ?, "
                    + "Context_ID = ?, "
                    + "sessionStartTime = ?, "
                    + "sessionEndTime = ?, "
                    + "sessionHits = ?, "
                    + "sessionDuration = ? "
                    + " where Session_ID=?";
    private Map<String, Map> foreignKeyTables;
    private Map<String, LoadStats> loadStats;
    private HashMap<String, String> machineTypes;
    private int runCounter = 0;

    private ForeignKeyStore() {
        loadStats = new ConcurrentHashMap<String, LoadStats>();
        foreignKeyTables = new ConcurrentHashMap<String, Map>();
        initializeTables();
    }

    public static ForeignKeyStore getInstance() {
//        if (instance == null) {
//            instance = new ForeignKeyStore();
//            instance.updateTest();
//        }
//        return instance;
        ForeignKeyStore fk = (ForeignKeyStore) instance.getInstance();
        return fk;
    }

    private void initializeTables() {
        loadMachineTypes();
        //TODO:  The table initialization (Loading for the Data).
    }

    public Map getUserIdTree() {
        return foreignKeyTables.get(FK_USERS_USER_ID);
    }

    public Map getPageIdTree() {
        return foreignKeyTables.get(FK_PAGES_PAGE_ID);
    }

    public Map getSessionsIdTree() {
        return foreignKeyTables.get(FK_SESSIONS_SESSION_ID);
    }

    public Map getQueryParametersTree() {
        return foreignKeyTables.get(FK_QUERY_PARAMETER_ID);
    }

    public String getMachineType(String machine) {
        if (machineTypes == null) {
            loadMachineTypes();
        }
        String type = (String) machineTypes.get(machine);
        if (type == null) {
            type = "N";
        }
        return type;
    }

    ;

    private void loadMachineTypes() {
        machineTypes = new HashMap<String, String>();
        Connection conn = null;
        try {
            conn = ConnectionPoolT.getConnection();
            PreparedStatement pstmt = conn.prepareStatement("select * from Machines");
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String machine = rs.getString("MachineName");
                String type = rs.getString("machineType");
                if (machine == null) {
                    machine = "nullMachine";
                }
                if (type == null) {
                    type = "N";
                }
                machineTypes.put(machine, type);
            }
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    public int getForeignKey(
            AccessRecordsForeignKeys fk,
            String foreignKeyValue,
            String foreignKeyName,
            PersistanceStrategy ps) {
        LoadStats ls = (LoadStats) loadStats.get(foreignKeyName);
        if (ls == null) {
            ls = new LoadStats();
            ls.key = foreignKeyName;
            loadStats.put(foreignKeyName, ls);
        }
        long startTime = System.currentTimeMillis();
        int intForeignKey = foreignKeyObjectLookup(fk, foreignKeyName);
        if (intForeignKey != 0) {
            ls.objectHits++;
            ls.objectHitTime += (System.currentTimeMillis() - startTime);
            return intForeignKey;
        }
        intForeignKey = binaryTreeSearch(foreignKeyValue, foreignKeyName, fk);
        if (intForeignKey != 0) {
            setForeignKeyObjectLookup(fk, foreignKeyName, intForeignKey);
            ls.cacheHits++;
            ls.cacheHitTime += (System.currentTimeMillis() - startTime);
            return intForeignKey;
        }
        intForeignKey = ps.writeForeignKey(foreignKeyName, foreignKeyValue);
        setBinaryTreeSearch(foreignKeyName, foreignKeyValue, intForeignKey, fk);
        setForeignKeyObjectLookup(fk, foreignKeyName, intForeignKey);
        ls.cacheMisses++;
        ls.totalLoads++;
        long tempTime = (System.currentTimeMillis() - startTime);
        ls.totalLoadTime += tempTime;
        ls.cacheMissTime += tempTime;
        return intForeignKey;
    }

    private int foreignKeyObjectLookup(AccessRecordsForeignKeys fk, String foreignKeyName) {
        if (fk == null) {
            return 0;
        }
        if (foreignKeyName.equals(FK_PAGES_PAGE_ID)) {
            return fk.fkPageID;
        } else if (foreignKeyName.equals(FK_USERS_USER_ID)) {
            return fk.fkUserID;
        } else if (foreignKeyName.equals(FK_SESSIONS_SESSION_ID)) {
            return fk.fkSessionID;
        } else if (foreignKeyName.equals(FK_MACHINES_MACHINE_ID)) {
            return fk.fkMachineID;
        } else if (foreignKeyName.equals(FK_INSTANCES_INSTANCE_ID)) {
            return fk.fkInstanceID;
        } else if (foreignKeyName.equals(FK_CONTEXTS_CONTEXT_ID)) {
            return fk.fkContextID;
        } else if (foreignKeyName.equals(FK_DEPLOYEDAPPS_APP_ID)) {
            return fk.fkAppID;
        } else if (foreignKeyName.equals(FK_BRANCH_TAG_ID)) {
            return fk.fkBranchTagID;
        }
        return 0;
    }

    private void setForeignKeyObjectLookup(AccessRecordsForeignKeys fk, String foreignKeyName, int intForeignKey) {
        if (fk == null) {
            return;
        }
        if (foreignKeyName.equals(FK_PAGES_PAGE_ID)) {
            fk.fkPageID = intForeignKey;
        } else if (foreignKeyName.equals(FK_USERS_USER_ID)) {
            fk.fkUserID = intForeignKey;
        } else if (foreignKeyName.equals(FK_SESSIONS_SESSION_ID)) {
            fk.fkSessionID = intForeignKey;
        } else if (foreignKeyName.equals(FK_MACHINES_MACHINE_ID)) {
            fk.fkMachineID = intForeignKey;
        } else if (foreignKeyName.equals(FK_INSTANCES_INSTANCE_ID)) {
            fk.fkInstanceID = intForeignKey;
        } else if (foreignKeyName.equals(FK_CONTEXTS_CONTEXT_ID)) {
            fk.fkContextID = intForeignKey;
        } else if (foreignKeyName.equals(FK_DEPLOYEDAPPS_APP_ID)) {
            fk.fkAppID = intForeignKey;
        } else if (foreignKeyName.equals(FK_BRANCH_TAG_ID)) {
            fk.fkBranchTagID = intForeignKey;
        }
    }

    private int binaryTreeSearch(String foreignKeyValue, String foreignKeyName, AccessRecordsForeignKeys fk) {
        if (foreignKeyName.equals(ForeignKeyStore.FK_SESSIONS_SESSION_ID)) {
            Date now = new Date();
            SessionDataClass sdc = new SessionDataClass();
            sdc.firstRequestDate = fk.eventTime;
            sdc.lastRequestDate = fk.eventTime;
            sdc.touchDate = now;
            sdc.sessionTXT = foreignKeyValue;
            sdc.Context_ID = fk.fkContextID;
            return binaryTreeSearch(sdc, foreignKeyName);
        } else if (foreignKeyName.equals(ForeignKeyStore.FK_QUERY_PARAMETER_ID)) {
            Date now = new Date();
            QueryParamClass qpc = new QueryParamClass();
            qpc.entryDate = now;
            qpc.lastTouchDate = now;
            qpc.queryParam = foreignKeyValue;
            return binaryTreeSearch(qpc, foreignKeyName);
        } else {
            Map<String, Map> tm = foreignKeyTables.get(foreignKeyName);
            if (tm == null) {
                tm = new ConcurrentSkipListMap<String, Map>();
                foreignKeyTables.put(foreignKeyName, tm);
            }
            if (foreignKeyValue == null) {
                logger.warn("FK is Null " + foreignKeyName);
                return 0;
            }
            Object o = tm.get(foreignKeyValue);
            int iForeignKey = 0;
            if (o == null) {
                return iForeignKey;
            } else {
                if (o instanceof Integer) {
                    iForeignKey = ((Integer) o).intValue();
                } else {
                    logger.warn("Non Integer in ForeignKey Tree : " + o.getClass().getName());
                }
                return iForeignKey;
            }
        }
    }
    /*
     * (non-Javadoc) @see java.lang.Runnable#run()
     */

    private int binaryTreeSearch(QueryParamClass qpc, String foreignKeyName) {
        Map<String, Map> tm = foreignKeyTables.get(foreignKeyName);
        if (tm == null) {
            tm = new ConcurrentSkipListMap<String, Map>();
            foreignKeyTables.put(foreignKeyName, tm);
        }
        String foreignKeyValue = qpc.queryParam;
        Object o = tm.get(foreignKeyValue);
        int iForeignKey = 0;
        if (o == null) {
            return iForeignKey;
        } else {
            if (o instanceof QueryParamClass) {
                //boolean statechange = false;
                //StringBuilder sb = new StringBuilder();
                QueryParamClass qpcTree = (QueryParamClass) o;
                qpcTree.lastTouchDate = new java.util.Date();
                qpcTree.touchCount++;
                iForeignKey = qpcTree.queryParamID;

            } else {
                logger.error("Non QueryParamClass in ForeignKey Tree : " + o.getClass().getName());
            }
            return iForeignKey;
        }
    }

    private int binaryTreeSearch(SessionDataClass sessionData, String foreignKeyName) {
        Map<String, Map> tm = foreignKeyTables.get(foreignKeyName);
        if (tm == null) {
            tm = new ConcurrentSkipListMap<String, Map>();
            foreignKeyTables.put(foreignKeyName, tm);
        }
        String foreignKeyValue = sessionData.sessionTXT;
        int startIPAddress = foreignKeyValue.indexOf(IPADDRESS);
        int startBrowserType = foreignKeyValue.indexOf(BROWSER);
        int startUserID = foreignKeyValue.indexOf(USERID);
        String ip = foreignKeyValue.substring(startIPAddress + IPADDRESS.length(), startBrowserType);
        String browser = foreignKeyValue.substring(startBrowserType + BROWSER.length(), startUserID);
        //String sessiontxt = foreignKeyValue.substring(0, startIPAddress);
        int userID = Integer.parseInt(foreignKeyValue.substring(startUserID + 8));
        String sessionTxtKeyValue = sessionData.sessionTXT.substring(0, startBrowserType);
        Object o = tm.get(sessionTxtKeyValue);
        int iForeignKey = 0;
        if (o == null) {
            return iForeignKey;
        } else {
            if (o instanceof SessionDataClass) {
                boolean statechange = false;
                StringBuilder sb = new StringBuilder();
                SessionDataClass sdc = (SessionDataClass) o;
                sdc.touchDate = new java.util.Date();
                sdc.firstRequestDate =
                        (sdc.firstRequestDate.before(sessionData.firstRequestDate))
                                ? sdc.firstRequestDate
                                : sessionData.firstRequestDate;
                sdc.lastRequestDate =
                        (sdc.lastRequestDate.after(sessionData.lastRequestDate))
                                ? sdc.lastRequestDate
                                : sessionData.lastRequestDate;
                sdc.touchCount++;
                sb.append("Session_ID:").append(sdc.sessionID).append(":");
                if (browser != null) {
                    if (sdc.browserType == null || !(sdc.browserType.equals(browser))) {
                        statechange = true;
                        sb.append("Browser f:t - ").append(sdc.browserType).append(":").append(browser);
                        if (browser.length() > 2) {
                            sdc.browserType = browser;
                        }
                    }
                }
                if (sdc.Context_ID != sessionData.Context_ID) {
                    statechange = true;
                    sb.append("Context_ID f:t - ").append(sdc.Context_ID).append(":").append(sessionData.Context_ID);
                    if (sessionData.Context_ID > sdc.Context_ID) {
                        sdc.Context_ID = sessionData.Context_ID;
                    }
                }
                if (sdc.User_ID != userID) {
                    statechange = true;
                    sb.append("User_ID f:t - ").append(sdc.User_ID).append(":").append(userID);
                    if (userID > 2) {
                        sdc.User_ID = userID;
                    }
                }
                if (ip != null) {
                    if (sdc.IPAddress == null || !(sdc.IPAddress.equalsIgnoreCase(ip))) {
                        statechange = true;
                        sb.append("IPAddress f:t - ").append(sdc.IPAddress).append(":").append(ip);
                        if (ip.length() > 6) {
                            sdc.IPAddress = ip;
                        }
                    }
                }
                iForeignKey = sdc.sessionID;
                if (statechange) {
                    FK_Cachelogger.debug(sb.toString());
                }
            } else {
                logger.warn("Non SessionDataClass in ForeignKey Tree : " + o.getClass().getName());
            }
            return iForeignKey;
        }
    }

    private void setBinaryTreeSearch(
            String foreignKeyName,
            String foreignKeyValue,
            int intForeignKey,
            AccessRecordsForeignKeys fk) {
        if (foreignKeyName.equals(ForeignKeyStore.FK_SESSIONS_SESSION_ID)) {
            Map tm = foreignKeyTables.get(foreignKeyName);
            if (tm == null) {
                tm = new ConcurrentSkipListMap<String, Map>();
                foreignKeyTables.put(foreignKeyName, tm);
            }
            SessionDataClass sdc = new SessionDataClass();
            sdc.firstRequestDate = fk.eventTime;
            sdc.lastRequestDate = fk.eventTime;
            sdc.touchDate = new java.util.Date();
            sdc.sessionTXT = foreignKeyValue;
            sdc.sessionID = intForeignKey;
            sdc.touchCount = 1;
            int startBrowserType = foreignKeyValue.indexOf(BROWSER);
            String sessionTxtKeyValue = sdc.sessionTXT.substring(0, startBrowserType);
            Object o = tm.put(sessionTxtKeyValue, sdc);
        } else if (foreignKeyName.equals(ForeignKeyStore.FK_QUERY_PARAMETER_ID)) {
            Map tm = foreignKeyTables.get(foreignKeyName);
            if (tm == null) {
                tm = new ConcurrentSkipListMap<String, Map>();
                foreignKeyTables.put(foreignKeyName, tm);
            }
            QueryParamClass qpc = new QueryParamClass();
            qpc.entryDate = new java.util.Date();
            qpc.lastTouchDate = new java.util.Date();
            qpc.queryParam = foreignKeyValue;
            qpc.queryParamID = intForeignKey;
            qpc.touchCount = 1;
            Object o = tm.put(foreignKeyValue, qpc);
        } else {
            Map tm = foreignKeyTables.get(foreignKeyName);
            if (tm == null) {
                tm = new ConcurrentSkipListMap<String, Map>();
                foreignKeyTables.put(foreignKeyName, tm);
            }
            if (foreignKeyValue != null) {
                Object o = tm.put(foreignKeyValue, new Integer(intForeignKey));
            }
        }
    }

    public void run() {
        try {
            long beforeMemory = Runtime.getRuntime().freeMemory();
            System.gc();
            long afterMemory = Runtime.getRuntime().freeMemory();
            ++runCounter;

            if (runCounter % 100 == 0) {
                java.text.DecimalFormat df = new java.text.DecimalFormat("###,###,###,###,###.");
                logger.info("Free Memory Before/After gc : " + df.format(beforeMemory) + " Bytes  :" + df.format(afterMemory) + " Bytes");

                logStatistics();
            }
            persistSessionData();
        } catch (Throwable t) {
            logger.error("ForeignKeyStore: " + t);
        }
    }

    public void logStatistics() {
        if (logger.isInfoEnabled()) {
            logger.info(getStatistics());
        }
    }

    public String getStatistics() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n\n");
        for (String key : loadStats.keySet()) {
            LoadStats ls = (LoadStats) loadStats.get(key);
            Map tm = foreignKeyTables.get(key);
            sb.append("\nForeign Key Structure: ").append(key);
            sb.append("\n").append(ls.toString());
            if (tm != null) {
                sb.append("\nTree Map Size:").append(tm.size());
            }
        }
        return sb.toString();
    }

    private void persistSessionData() {
        List<SessionDataClass> allPersistableObjects = new ArrayList<SessionDataClass>();
        Map<String, CounterClass> htSessionCounts = new HashMap<String, CounterClass>();
        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            logger.warn("persistSessionData");
            Map<String, Object> tm = foreignKeyTables.get(ForeignKeyStore.FK_SESSIONS_SESSION_ID);

            if (tm != null) {
                java.util.Date removeDate;
                java.util.Date oneMinDate;
                java.util.Date fiveMinDate;
                java.util.Date tenMinDate;
                java.util.Date thirtyMinDate;

                DateTime now = new DateTime();

                oneMinDate = now.minusMinutes(1).toDate();
                fiveMinDate = now.minusMinutes(5).toDate();
                tenMinDate = now.minusMinutes(10).toDate();
                thirtyMinDate = now.minusMinutes(30).toDate();
                removeDate = now.minusMinutes(SESSION_REMOVAL_MINUTES).toDate();

                List<String> removeKeys = new ArrayList<String>();

                for (String key : tm.keySet()) {
                    Object o = tm.get(key);
                    if (o instanceof SessionDataClass) {
                        SessionDataClass sdc = (SessionDataClass) o;

                        if (sdc.lastRequestDate.after(oneMinDate)) {
                            sdc.persistOneMinuteSession = true;
                            allPersistableObjects.add(sdc);
                            incrementOneMinSessions(sdc, htSessionCounts);
                            incrementFiveMinSessions(sdc, htSessionCounts);
                            incrementTenMinSessions(sdc, htSessionCounts);
                            incrementThirtyMinSessions(sdc, htSessionCounts);

                        } else if (sdc.lastRequestDate.after(fiveMinDate)) {
                            incrementFiveMinSessions(sdc, htSessionCounts);
                            incrementTenMinSessions(sdc, htSessionCounts);
                            incrementThirtyMinSessions(sdc, htSessionCounts);

                        } else if (sdc.lastRequestDate.after(tenMinDate)) {
                            incrementTenMinSessions(sdc, htSessionCounts);
                            incrementThirtyMinSessions(sdc, htSessionCounts);

                        } else if (sdc.lastRequestDate.after(thirtyMinDate)) {
                            incrementThirtyMinSessions(sdc, htSessionCounts);

                        } else if (sdc.lastRequestDate.before(removeDate) && sdc.touchDate.before(removeDate)) {
                            removeKeys.add(key);
                        }
                    } else {
                        logger.warn("ForeignKeyStore object NOT a SessionDataClass");
                    }
                }
                boolean rone = false;
                for (String key : removeKeys) {
                    Object o = tm.remove(key);
                    if (o instanceof SessionDataClass) {
                        if (rone == false) {
                            //logger.warn("all persistableObjects.add "+removeKeys.size());
                        }
                        allPersistableObjects.add((SessionDataClass) o);
                    }
                }
            }

            boolean tone = false;
            con = (Connection) threadLocalCon.get();
            pstmt = con.prepareStatement(UPDATE_SESSIONS);

            int batchCount = 0;
            for (SessionDataClass sdc : allPersistableObjects) {
                if (tone == false) {
                    tone = true;
                    //logger.warn("about to updateSessionRecord "+allPersistableObjects.size());
                }
                //updateSessionRecord(sdc);
                updateSessionRecordWithConnection(con, pstmt, sdc);
                batchCount++;
            }
            logger.info("Batched " + batchCount + " Session Record updates ");
            pstmt.executeBatch();
            //con.commit();

        } catch (SQLException ex) {
            //java.util.logging.Logger.getLogger(ForeignKeyStore.class.getName()).log(Level.SEVERE, null, ex);
            logger.error("Error updatating Session Records ", ex);
//            if ( con!=null) {
//                try {
//                    con.rollback();
//                } catch (SQLException ex1) {
//                    java.util.logging.Logger.getLogger(ForeignKeyStore.class.getName()).log(Level.SEVERE, null, ex1);
//                }
//            }
        } finally {
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException ex) {
                    java.util.logging.Logger.getLogger(ForeignKeyStore.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
//            if ( con!=null) {
//                try {
//                    con.close();
//                } catch (SQLException ex) {
//                    java.util.logging.Logger.getLogger(ForeignKeyStore.class.getName()).log(Level.SEVERE, null, ex);
//                }
//            }
//
        }

        broadcastSessionDataBeans(htSessionCounts);

    }

    private void broadcastSessionDataBeans(Map<String, CounterClass> htCounterClass) {
        BeanBag beanBag = new BeanBag();
        List sdl = new ArrayList<SessionDataBean>();

        //for(String sNext:htCounterClass.keySet()) {
        //    CounterClass cc = (CounterClass) htCounterClass.get(sNext);
        for (Map.Entry<String, CounterClass> entry : htCounterClass.entrySet()) {
            CounterClass cc = entry.getValue();
            String sNext = entry.getKey();

            SessionDataBean sessionBean = new SessionDataBean();
            sessionBean.setCurrentTime(new java.util.Date());
            sessionBean.setOneMinSessions(cc.oneMin);
            sessionBean.setFiveMinSessions(cc.fiveMin);
            sessionBean.setTenMinSessions(cc.tenMin);
            sessionBean.setThirtyMinSessions(cc.thirtyMin);
            sessionBean.setContext(sNext);

            if (sessionBroadcastCounter++ % 100 == 0) {
                logger.warn(
                        "BroadCasting the Session Data : "
                                + sNext
                                + ":"
                                + sessionBean.getOneMinSessions()
                                + ":"
                                + sessionBean.getFiveMinSessions()
                                + ":"
                                + sessionBean.getTenMinSessions()
                                + ":"
                                + sessionBean.getThirtyMinSessions());
            }
            sdl.add(sessionBean);
            //broadcast(sessionBean);
        }
        beanBag.setBeans(sdl);
        broadcast(beanBag);
    }

    private void incrementOneMinSessions(SessionDataClass sdc, Map<String, CounterClass> htData) {
        String context = getContext(sdc.Context_ID);
        if (htData.get(context) == null) {
            htData.put(context, new CounterClass(context));
        }
        if (htData.get(ALL_CONTEXTS) == null) {
            htData.put(ALL_CONTEXTS, new CounterClass(ALL_CONTEXTS));
        }
        CounterClass currentContext = htData.get(context);
        CounterClass allContext = htData.get(ALL_CONTEXTS);
        currentContext.oneMin++;
        allContext.oneMin++;
    }

    private void incrementFiveMinSessions(SessionDataClass sdc, Map<String, CounterClass> htData) {
        String context = getContext(sdc.Context_ID);
        if (htData.get(context) == null) {
            htData.put(context, new CounterClass(context));
        }
        if (htData.get(ALL_CONTEXTS) == null) {
            htData.put(ALL_CONTEXTS, new CounterClass(ALL_CONTEXTS));
        }
        CounterClass currentContext = htData.get(context);
        CounterClass allContext = htData.get(ALL_CONTEXTS);
        currentContext.fiveMin++;
        allContext.fiveMin++;
    }

    private void incrementTenMinSessions(SessionDataClass sdc, Map<String, CounterClass> htData) {
        String context = getContext(sdc.Context_ID);
        if (htData.get(context) == null) {
            htData.put(context, new CounterClass(context));
        }
        if (htData.get(ALL_CONTEXTS) == null) {
            htData.put(ALL_CONTEXTS, new CounterClass(ALL_CONTEXTS));
        }
        CounterClass currentContext = htData.get(context);
        CounterClass allContext = htData.get(ALL_CONTEXTS);
        currentContext.tenMin++;
        allContext.tenMin++;
    }

    private void incrementThirtyMinSessions(SessionDataClass sdc, Map<String, CounterClass> htData) {
        String context = getContext(sdc.Context_ID);
        if (htData.get(context) == null) {
            htData.put(context, new CounterClass(context));
        }
        if (htData.get(ALL_CONTEXTS) == null) {
            htData.put(ALL_CONTEXTS, new CounterClass(ALL_CONTEXTS));
        }
        CounterClass currentContext = htData.get(context);
        CounterClass allContext = htData.get(ALL_CONTEXTS);
        currentContext.thirtyMin++;
        allContext.thirtyMin++;
    }

    private String getContext(int id) {
        Map tm = foreignKeyTables.get(FK_CONTEXTS_CONTEXT_ID);
        Iterator iter = tm.keySet().iterator();
        while (iter.hasNext()) {
            String s = (String) iter.next();
            Integer integer = (Integer) tm.get(s);
            if (new Integer(id).equals(integer)) {
                return s;
            }
        }
        return "NotInARTTree";
    }

    private void updateSessionRecord(SessionDataClass sdc) {
        Connection con = null;
        try {
            con = (Connection) threadLocalCon.get();
            PreparedStatement pstmt = con.prepareStatement(UPDATE_SESSIONS);
            updateSessionRecordWithConnection(con, pstmt, sdc);
            con.commit();
            pstmt.close();

        } catch (SQLException se) {
            // TODO Logger
            se.printStackTrace();
            try {
                con.rollback();
            } catch (SQLException sse) {
                sse.printStackTrace();
            }
        } finally {
//            if (con != null) {
//                try {
//                    //					con.commit();
//                    con.close();
//                } catch (Throwable t) {
//                    logger.error("exception trying to close a connection", t);
//                    //TODO Logger
//                }
//            }
        }
    }

    private void updateSessionRecordWithConnection(Connection con, PreparedStatement pstmt, SessionDataClass sdc) throws SQLException {
        long duration = sdc.lastRequestDate.getTime() - sdc.firstRequestDate.getTime();
        //PreparedStatement pstmt = con.prepareStatement(UPDATE_SESSIONS);
        pstmt.setString(1, sdc.IPAddress);
        if (sdc != null && sdc.browserType != null && sdc.browserType.length() > 254) {
            pstmt.setString(2, sdc.browserType.substring(0, 254));
            browser_logger.info("browser Too long, chomping: " + sdc.browserType);
        } else {
            pstmt.setString(2, sdc.browserType);
        }
        pstmt.setInt(3, sdc.User_ID);
        pstmt.setInt(4, sdc.Context_ID);
        pstmt.setTimestamp(5, new Timestamp(sdc.firstRequestDate.getTime()));
        pstmt.setTimestamp(6, new Timestamp(sdc.lastRequestDate.getTime()));
        pstmt.setInt(7, sdc.touchCount);
        pstmt.setInt(8, (int) duration);
        pstmt.setInt(9, sdc.sessionID);
        //pstmt.execute();
        pstmt.addBatch();
    }

    /*
     * private void broadcast(SessionDataBean sessionBean){
     *
     * }
     */
    private void broadcast(SessionDataBean bean) {
        try {
            CommunicationChannel.getInstance().broadcast(bean, null);
        } catch (Exception e) {
            logger.error("Error broadcasting data: ", e);
        }
    }

    private void broadcast(BeanBag bean) {
        try {
            CommunicationChannel.getInstance().broadcast(bean, null);
        } catch (Exception e) {
            logger.error("Error broadcasting data: ", e);
        }
    }

    private void updateTest() {
        SessionDataClass sdc = new SessionDataClass();
        sdc.sessionTXT = "TEST INSERT";
        sdc.sessionID = 1;
        sdc.touchCount = 0;
        sdc.firstRequestDate = new java.util.Date();
        sdc.lastRequestDate = new java.util.Date();
        sdc.touchDate = new java.util.Date();
        sdc.IPAddress = "42.42.42.42";
        sdc.browserType = "NONE";
        sdc.User_ID = 1;
        sdc.Context_ID = 0;
        int duration = 2;
        logger.error(
                UPDATE_SESSIONS
                        + "  \n"
                        + sdc.IPAddress
                        + ":"
                        + sdc.browserType
                        + ":"
                        + sdc.User_ID
                        + ":"
                        + sdc.Context_ID
                        + ":"
                        + sdfMySQLTimestamp.print(sdc.firstRequestDate.getTime())
                        + ":"
                        + sdfMySQLTimestamp.print(sdc.lastRequestDate.getTime())
                        + ":"
                        + sdc.touchCount
                        + ":"
                        + duration
                        + ":"
                        + sdc.sessionID
                        + ":");
        updateSessionRecord(sdc);
    }

    public static class QueryParamClass implements Serializable {

        public String queryParam;
        public int queryParamID;
        public Date entryDate;
        public Date lastTouchDate;
        public int touchCount;
    }

    /**
     * private void setBinaryTreeSearch( String foreignKeyName, SessionDataClass sessionData) { TreeMap tm = (TreeMap)
     * foreignKeyTables.get(foreignKeyName); if (tm == null) { tm = new TreeMap(); foreignKeyTables.put(foreignKeyName, tm);
     * } synchronized(tm){ Object o = tm.put(sessionData.sessionTXT, sessionData); } }
     */
    private class LoadStats implements Serializable {

        public long totalLoadTime;
        public int totalLoads;
        public int objectHits;
        public long objectHitTime;
        public int cacheHits;
        public int cacheMisses;
        public long cacheHitTime;
        public long cacheMissTime;
        public String key;

        @Override
        public String toString() {
            String databaseMisKey = (String) BasePersistanceStrategy.databaseMisXRef.get(key);
            Integer i = null;
            int consecutiveDatabaseMisses = 0;
            if (databaseMisKey != null) {
                i = (Integer) BasePersistanceStrategy.databaseMisHashtable.get(databaseMisKey);
            }
            if (i != null) {
                consecutiveDatabaseMisses = i.intValue();
            }
            StringBuilder sb = new StringBuilder();
            java.text.DecimalFormat df = new java.text.DecimalFormat("###,###,###,###,###.");

            sb.append("\n\tTotal Load Time : ").append(df.format(totalLoadTime));
            sb.append("\n\tTotal Loads     : ").append(df.format(totalLoads));
            sb.append("\n\tObject Hits     : ").append(df.format(objectHits));
            sb.append("\n\tObject Hit Time : ").append(df.format(objectHitTime));
            sb.append("\n\tCache Hits      : ").append(df.format(cacheHits));
            sb.append("\n\tCache Hit Time  : ").append(df.format(cacheHitTime));
            sb.append("\n\tCache Misses    : ").append(df.format(cacheMisses));
            sb.append("\n\tCache Miss Time : ").append(df.format(cacheMissTime));
            sb.append("\n\tConsc DB Misses : ").append(df.format(consecutiveDatabaseMisses));

            return sb.toString();
        }
    }

    private class SessionDataClass implements Serializable {

        public String sessionTXT;
        public int sessionID;
        public int touchCount;
        public java.util.Date firstRequestDate;
        public java.util.Date lastRequestDate;
        public java.util.Date touchDate;
        public String IPAddress;
        public String browserType;
        public int User_ID;
        public int Context_ID;
        public boolean persistOneMinuteSession = false;
    }

    private class CounterClass implements Serializable {

        public String Context;
        public int oneMin;
        public int fiveMin;
        public int tenMin;
        public int thirtyMin;

        public CounterClass(String c) {
            Context = c;
        }
    }
}
