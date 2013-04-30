/*
 * Created on Oct 29, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package com.bos.art.logParser.db;

import com.bos.art.logParser.records.AccessRecordsForeignKeys;
import com.bos.art.logParser.records.ILiveLogParserRecord;
import com.bos.art.logParser.records.PageRecordEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import org.apache.log4j.Logger;

/**
 * @author I0360D3
 *
 * To change the template for this generated type comment go to Window>Preferences>Java>Code Generation>Code and Comments
 */
public class HtmlPageRecordPersistanceStrategy extends BasePersistanceStrategy implements PersistanceStrategy {

    private final static int MAXBATCHINSERTSIZE = 5000;
    private final static int INCREMENT_AMOUNT = 10;
    private final static int MINBATCHINSERTSIZE = 10;
    private static int currentBatchInsertSize = MINBATCHINSERTSIZE;
    private static double timePerInsert = 5000.0;
    private static HtmlPageRecordPersistanceStrategy instance;
    private static final Object initLock = new Object();

    protected HtmlPageRecordPersistanceStrategy() {
    }

    public static HtmlPageRecordPersistanceStrategy getInstance() {
        synchronized (initLock) {
            if (instance == null) {
                instance = new HtmlPageRecordPersistanceStrategy();
            }
        }
        return instance;
    }
    private static final Logger logger = (Logger) Logger.getLogger(HtmlPageRecordPersistanceStrategy.class.getName());
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
                return ((Connection) threadLocalCon.get()).prepareStatement(
                        "insert into HtmlPageResponse "
                        + "(Branch_ID  , "
                        + "Machine_ID , "
                        + "Context_ID , "
                        + "Page_ID    , "
                        + "Time       , "
                        + "sessionTXT , "
                        + "requestToken , "
                        + "requestTokenCount , "
                        + "encodedPage, "
                        + "Instance_ID "
                        + " ) "
                        + " values (?,?,?,?,?,?,?,?,?,?)");
            } catch (SQLException se) {
                logger.error("SQL Exception ", se);
            }
            return null;
        }
    };
    private static ThreadLocal threadLocalInserts = new ThreadLocal() {

        protected synchronized Object initialValue() {
            return new Integer(0);
        }
    };

    public void resetThreadLocalPstmt() {
        logger.info("Resetting the Pstmt!");
        PreparedStatement ps = (PreparedStatement) threadLocalPstmt.get();
        Connection con = (Connection) threadLocalCon.get();
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
                    "insert into HtmlPageResponse "
                    + "(Branch_ID  , "
                    + "Machine_ID , "
                    + "Context_ID , "
                    + "Page_ID    , "
                    + "Time       , "
                    + "sessionTXT , "
                    + "requestToken , "
                    + "requestTokenCount , "
                    + "encodedPage, " 
                    + "Instance_ID" 
                    + ") " 
                    + " values (?,?,?,?,?,?,?,?,?,?)");
            threadLocalCon.set(con);
            threadLocalPstmt.set(ps);
        } catch (Exception e) {
            logger.error("Exception ", e);
        }
    }

    public void blockInsert(PreparedStatement pstmt) {
        try {
            pstmt.addBatch();
            Integer count = (Integer) threadLocalInserts.get();
            int icount = count.intValue() + 1;
            threadLocalInserts.set(new Integer(icount));
            if (icount % currentBatchInsertSize == 0) {
                long startTime = System.currentTimeMillis();
                pstmt.executeBatch();
                long elapsed = System.currentTimeMillis() - startTime;
                double currentTimePerInsert = (double) elapsed / (double) currentBatchInsertSize;

                if ((currentTimePerInsert * 1.05) < timePerInsert && (currentBatchInsertSize < MAXBATCHINSERTSIZE - INCREMENT_AMOUNT)) {
                    currentBatchInsertSize += INCREMENT_AMOUNT;
                    timePerInsert = currentTimePerInsert;
                    logger.warn("AccessRecordPersistanceStrategy currentBatchInsertSize set to-> : " + currentBatchInsertSize);
                } else if ((currentTimePerInsert * .85) > timePerInsert && (currentBatchInsertSize > MINBATCHINSERTSIZE + INCREMENT_AMOUNT)) {
                    currentBatchInsertSize -= INCREMENT_AMOUNT;
                    timePerInsert = currentTimePerInsert;
                    logger.warn("AccessRecordPersistanceStrategy currentBatchInsertSize set to-> : " + currentBatchInsertSize);
                }
            }
        } catch (SQLException se) {
            logger.error("Exception", se);

            if (se.getNextException() != null) {
                logger.error("nextException", se.getNextException());
            }

            resetThreadLocalPstmt();
        }
    }
    //private static final SimpleDateFormat sdfMySQLDate = new SimpleDateFormat("yyyyMMddHHmmss");
    /*
     * (non-Javadoc) @see
     * com.bos.art.logParser.db.PersistanceStrategy#writeToDatabase(com.bos.art.logParser.records.ILiveLogParserRecord)
     */

    public boolean writeToDatabase(ILiveLogParserRecord record) {
        PageRecordEvent pre = (PageRecordEvent) record;
        AccessRecordsForeignKeys fk = pre.obtainForeignKeys();

        fk.fkContextID =
                ForeignKeyStore.getInstance().getForeignKey(
                fk,
                record.getContext(),
                ForeignKeyStore.FK_CONTEXTS_CONTEXT_ID,
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

        String pageName = pre.getPageName();
        fk.fkPageID = ForeignKeyStore.getInstance().getForeignKey(fk, pageName, ForeignKeyStore.FK_PAGES_PAGE_ID, this);
        fk.fkAppID =
                ForeignKeyStore.getInstance().getForeignKey(
                fk,
                record.getAppName(),
                ForeignKeyStore.FK_DEPLOYEDAPPS_APP_ID,
                this);
        String branchName = pre.getBranchName();
        fk.fkBranchTagID =
                ForeignKeyStore.getInstance().getForeignKey(fk, branchName, ForeignKeyStore.FK_BRANCH_TAG_ID, this);


        Connection con = null;
        int requestTokenCount = pre.getRequestTokenCount();
        int requestToken = pre.getRequestToken();
        String encodedText = pre.getEncodedPage();

        try {
            PreparedStatement pstmt = (PreparedStatement) threadLocalPstmt.get();

            pstmt.setInt(1, fk.fkBranchTagID);
            pstmt.setInt(2, fk.fkMachineID);
            pstmt.setInt(3, fk.fkContextID);
            pstmt.setInt(4, fk.fkPageID);
            pstmt.setTimestamp(5, new java.sql.Timestamp(record.getEventTime().getTime().getTime()));
            pstmt.setString(6, pre.getSessionId());
            pstmt.setInt(7, requestToken);
            pstmt.setInt(8, requestTokenCount);
            pstmt.setString(9, encodedText);
            pstmt.setInt(10, fk.fkInstanceID);

            blockInsert(pstmt);
            //			requestType, requestToken, userServiceTime
        } catch (SQLException se) {
            logger.error("Exception", se);
            resetThreadLocalPstmt();
            return false;
        } finally {
            //  Removed because of Thread Local.
        }
        return true;
    }
}