/*
 * Created on Oct 29, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package com.bos.art.logParser.db;
import com.bos.art.logParser.collector.QueryParameterProcessingQueue;
import com.bos.art.logParser.records.AccessRecordsForeignKeys;
import com.bos.art.logParser.records.ILiveLogParserRecord;
import com.bos.art.logParser.records.QueryParameters;
import com.bos.art.logParser.records.UserRequestTiming;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.log4j.Logger;
/**
 * @author I0360D3
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class AccessRecordPersistanceStrategy extends BasePersistanceStrategy implements PersistanceStrategy {
    private static final int BATCH_INSERT_SIZE = 500;
    private static final String BROWSER = "#BROWSER#";
    private static final String IPADDRESS = "#IPADDRESS#";
    private final static int MAXBATCHINSERTSIZE = 2500;
    private final static int INCREMENT_AMOUNT = 10;
    private final static int MINBATCHINSERTSIZE = 900;
    private static final String USERID = "#USERID#";
    private static int lastBatchInsertSize = MINBATCHINSERTSIZE;
    private static int currentBatchInsertSize = MINBATCHINSERTSIZE;
    private static double timePerInsert = 5000.0;

    private static int globalAccessRecordCounter = 0;
    volatile private static int AccessRecordsRecordPK;
    //private static Object lock = new Object();
    private static AccessRecordPersistanceStrategy instance;
    private static final Object initLock = new Object();
    private static final Logger logger = (Logger)Logger.getLogger(AccessRecordPersistanceStrategy.class.getName());

    
    protected AccessRecordPersistanceStrategy() {
        AccessRecordsRecordPK = selectNextValidAccessRecordsPK();
        AccessRecordsRecordPK++;
    }


    public static AccessRecordPersistanceStrategy getInstance() {
        synchronized (initLock) {
            if (instance == null) {
                instance = new AccessRecordPersistanceStrategy();
            }
        }
        return instance;
    }

    private int selectNextValidAccessRecordsPK() {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = ConnectionPoolT.getConnection();
            //pstmt = con.prepareStatement("Select max(RecordPK) from AccessRecords");
            pstmt = con.prepareStatement("select recordpk from accessrecords order by recordpk desc limit 1");
            rs = pstmt.executeQuery();
            if(rs.next()){
            	return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (pstmt != null) {
                    pstmt.close();
                }
                if (con != null) {
                    con.close();
                }
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        }
        return 0;
    }
    //  logParser Stats Unit.
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
    private static ThreadLocal threadLocalPstmt = new ThreadLocal() {
        @Override
        protected synchronized Object initialValue() {
            try {
                return ((Connection)threadLocalCon.get()).prepareStatement(
                    "insert into AccessRecords "
                        + "(RecordPK, Page_ID,User_ID,Session_ID,Machine_ID,Context_ID,App_ID,Branch_Tag_ID,Time,LoadTime, requestType, requestToken, userServiceTime,Instance_ID) "
                        + "values (?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
            } catch (SQLException se) {
                logger.error("SQL Exception ", se);
            }
            return null;
        }
    };
    private static ThreadLocal threadLocalInserts = new ThreadLocal() {
        @Override
        protected synchronized Object initialValue() {
            return new Integer(0);
        }
    };
    public void resetThreadLocalPstmt() {
        logger.info("Resetting the Pstmt!");
        PreparedStatement ps = (PreparedStatement)threadLocalPstmt.get();
        Connection con = (Connection)threadLocalCon.get();
        try {
            try {
                if (ps != null) {
                    ps.close();
                    ps = null;
                }
                if (con != null) {
                    con.close();
                    con = null;
                }
            } catch (SQLException se) {
                logger.error("Exception resetting the ThreadLocal PreparedStatement", se);
            }
            con = ConnectionPoolT.getConnection();
            ps =
                con.prepareStatement(
                    "insert into AccessRecords "
                        + "(RecordPK, Page_ID,User_ID,Session_ID,Machine_ID,Context_ID,App_ID,Branch_Tag_ID,Time,LoadTime, requestType, requestToken, userServiceTime,Instance_ID) "
                        + "values (?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
            threadLocalCon.set(con);
            threadLocalPstmt.set(ps);
        } catch (Exception e) {
            logger.error("Exception ", e);
        }
    }

    public void blockInsert(PreparedStatement pstmt) {
        try {
            pstmt.addBatch();
            Integer count = (Integer)threadLocalInserts.get();
            int icount = count.intValue()+1;
            threadLocalInserts.set(new Integer(icount));

            if (icount % currentBatchInsertSize == 0) {
                long startTime = System.currentTimeMillis();
                pstmt.executeBatch();
                long elapsed = System.currentTimeMillis() - startTime;
                double currentTimePerInsert = (double)elapsed / (double)currentBatchInsertSize;

                if ( ((currentTimePerInsert <= timePerInsert) && (currentBatchInsertSize < MAXBATCHINSERTSIZE-INCREMENT_AMOUNT))) {
                    currentBatchInsertSize += INCREMENT_AMOUNT;
                    timePerInsert = currentTimePerInsert;
                    logger.warn("AccessRecordPersistanceStrategy currentBatchInsertSize set to-> : " + currentBatchInsertSize);
                } else if ( (currentTimePerInsert * .65) > timePerInsert && (currentBatchInsertSize > MINBATCHINSERTSIZE+INCREMENT_AMOUNT)) {
                    currentBatchInsertSize -= INCREMENT_AMOUNT;
                    timePerInsert = currentTimePerInsert;
                    logger.warn("AccessRecordPersistanceStrategy currentBatchInsertSize set to-> : " + currentBatchInsertSize);
                }
                if ( icount % 100000 == 0) {
                    logger.warn("AccessRecordPersistanceStrategy currentBatchInsertSize is-> : " + currentBatchInsertSize);
                }
            }
        } catch (SQLException se) {
            logger.error("Exception", se);
            logger.error("NextException ",se.getNextException());
            resetThreadLocalPstmt();
        }
    }
    
    
    /* (non-Javadoc)
     * @see com.bos.art.logParser.db.PersistanceStrategy#writeToDatabase(com.bos.art.logParser.records.ILiveLogParserRecord)
     */
    public boolean writeToDatabase(ILiveLogParserRecord record) {
        AccessRecordsForeignKeys fk = ((UserRequestTiming)record).obtainForeignKeys();
        fk.fkContextID =
            ForeignKeyStore.getInstance().getForeignKey(
                fk,
                record.getContext(),
                ForeignKeyStore.FK_CONTEXTS_CONTEXT_ID,
                this);
        String userKey = ((UserRequestTiming)record).getUserKey();
        fk.fkUserID = ForeignKeyStore.getInstance().getForeignKey(fk, userKey, ForeignKeyStore.FK_USERS_USER_ID, this);
        //  Sessions Format:
        //  jsessionid1234#IPADDRESS#127.0.0.1#BROWSER#Mozilla something / or / another#USERID#1234
        UserRequestTiming urt = ((UserRequestTiming)record);
        StringBuffer sessionValue =
            new StringBuffer()
                .append(urt.getSessionId())
                .append(IPADDRESS)
                .append(urt.getIpAddress())
                .append(BROWSER)
                .append(urt.getBrowser())
                .append(USERID)
                .append(fk.fkUserID);
        fk.fkSessionID =
            ForeignKeyStore.getInstance().getForeignKey(
                fk,
                sessionValue.toString(),
                ForeignKeyStore.FK_SESSIONS_SESSION_ID,
                this);
        fk.fkMachineID =
            ForeignKeyStore.getInstance().getForeignKey(
                fk,
                record.getServerName(),
                ForeignKeyStore.FK_MACHINES_MACHINE_ID,
                this);
        if ( record.getInstance()!=null) {
            fk.fkInstanceID =
                ForeignKeyStore.getInstance().getForeignKey(
                    fk,
                    record.getInstance(),
                    ForeignKeyStore.FK_INSTANCES_INSTANCE_ID,
                    this);
        }
        String pageName = ((UserRequestTiming)record).getPage();
        fk.fkPageID = ForeignKeyStore.getInstance().getForeignKey(fk, pageName, ForeignKeyStore.FK_PAGES_PAGE_ID, this);
        fk.fkAppID =
            ForeignKeyStore.getInstance().getForeignKey(
                fk,
                record.getAppName(),
                ForeignKeyStore.FK_DEPLOYEDAPPS_APP_ID,
                this);
        String branchName = ((UserRequestTiming)record).getBranchName();
        fk.fkBranchTagID =
            ForeignKeyStore.getInstance().getForeignKey(fk, branchName, ForeignKeyStore.FK_BRANCH_TAG_ID, this);
        //String queryParameters = ((UserRequestTiming)record).getQueryParams();
        
        String qParam = ((UserRequestTiming)record).getQueryParams();
        int requestType = ((UserRequestTiming)record).getRequestType();
        long requestEndTime = ((UserRequestTiming)record).getRequestEndTime();
        long tokenCreateTime = ((UserRequestTiming)record).getTokenCreationTime();
        int requestToken = ((UserRequestTiming)record).getRequestToken();
        int recordPK = -1;

        //synchronized(initLock){
            recordPK = ++AccessRecordsRecordPK;
        //}
        
        try {
            PreparedStatement pstmt = (PreparedStatement)threadLocalPstmt.get();
            pstmt.setInt(1, recordPK);
            pstmt.setInt(2, fk.fkPageID);
            pstmt.setInt(3, fk.fkUserID);
            pstmt.setInt(4, fk.fkSessionID);
            pstmt.setInt(5, fk.fkMachineID);
            pstmt.setInt(6, fk.fkContextID);
            pstmt.setInt(7, fk.fkAppID);
            pstmt.setInt(8, fk.fkBranchTagID);
            pstmt.setTimestamp(9, new java.sql.Timestamp( record.getEventTime().getTime().getTime() ));
            pstmt.setInt(10, record.getLoadTime());
            pstmt.setInt(11, requestType);
            pstmt.setInt(12, requestToken);
            pstmt.setInt(13, (int) (requestEndTime - tokenCreateTime));
            pstmt.setInt(14, fk.fkInstanceID);
            blockInsert(pstmt);
            //			requestType, requestToken, userServiceTime
        } catch (SQLException se) {
            logger.error("Exception", se);
            resetThreadLocalPstmt();
            return false;
        } finally {
            //  Removed because of Thread Local.
        }
        if(++globalAccessRecordCounter%10000 == 0){
        	logger.warn("recordPK:"+recordPK+":PageID:"+fk.fkPageID+":UserID:"+fk.fkUserID
        	+":SessionID:"+fk.fkSessionID
        	+":Machine:"+fk.fkMachineID+":AppID:"+fk.fkAppID+":BranchTagID:"+fk.fkBranchTagID+":loadTime:"+record.getLoadTime()+":requestToken:"+requestToken);
        }
        QueryParameterProcessingQueue.getInstance().addLast(new QueryParameters(qParam,recordPK));
        return true;
    }
}
