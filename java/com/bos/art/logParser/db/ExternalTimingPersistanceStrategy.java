/*

 * Created on Oct 29, 2003

 *

 * To change the template for this generated file go to

 * Window>Preferences>Java>Code Generation>Code and Comments

 */
package com.bos.art.logParser.db;

import com.bos.art.logParser.records.AccessRecordsForeignKeys;
import com.bos.art.logParser.records.ExternalEventTiming;
import com.bos.art.logParser.records.ILiveLogParserRecord;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.apache.log4j.Logger;

/**
 *
 * @author I0360D3
 *
 *
 *
 * Hello World
 *
 * TEST 2
 *
 * To change the template for this generated type comment go to
 *
 * Window>Preferences>Java>Code Generation>Code and Comments
 *
 */
public class ExternalTimingPersistanceStrategy extends BasePersistanceStrategy implements PersistanceStrategy {

    protected static final Logger logger = (Logger) Logger.getLogger(ExternalTimingPersistanceStrategy.class.getName());
    private static final int BATCH_INSERT_SIZE = 200;

    private ExternalTimingPersistanceStrategy() {
    }
    private static ExternalTimingPersistanceStrategy instance;

    public static ExternalTimingPersistanceStrategy getInstance() {

        if (instance == null) {

            instance = new ExternalTimingPersistanceStrategy();

        }

        return instance;

    }
    private static ThreadLocal threadLocalCon = new ThreadLocal() {

        @Override
        protected synchronized Object initialValue() {

            try {

                Connection con = ConnectionPoolT.getConnection();
                //con.setAutoCommit(false);                
                return con;

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
                        "insert into ExternalAccessRecords "
                        + "(Machine_ID,App_ID,Classification_ID,Context_ID,Branch_Tag_ID,Time,LoadTime,Instance_ID) "
                        + "values (?,?,?,?,?,?,?,?)");

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
            //con.setAutoCommit(false);

            ps =
                    con.prepareStatement(
                    "insert into ExternalAccessRecords "
                    + "(Machine_ID,App_ID,Classification_ID,Context_ID,Branch_Tag_ID,Time,LoadTime,Instance_ID) "
                    + "values (?,?,?,?,?,?,?,?)");

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

            if (icount % BATCH_INSERT_SIZE == 0) {

                pstmt.executeBatch();
                //((Connection) threadLocalCon.get()).commit();


            }

        } catch (SQLException se) {
//            try {
//                ((Connection) threadLocalCon.get()).rollback();
//            } catch (SQLException ex) {
//                java.util.logging.Logger.getLogger(ExternalTimingPersistanceStrategy.class.getName()).log(Level.SEVERE, null, ex);
//            }

            logger.error("Exception", se);

            resetThreadLocalPstmt();

        }

    }

    /*
     * (non-Javadoc)
     *
     * @see com.bos.art.logParser.db.PersistanceStrategy#writeToDatabase(com.bos.art.logParser.records.ILiveLogParserRecord)
     *
     */
    public boolean writeToDatabase(ILiveLogParserRecord record) {

        AccessRecordsForeignKeys fk = ((ExternalEventTiming) record).obtainForeignKeys();

        ExternalEventTiming eet = ((ExternalEventTiming) record);

        fk.fkMachineID =
                ForeignKeyStore.getInstance().getForeignKey(
                fk,
                record.getServerName(),
                ForeignKeyStore.FK_MACHINES_MACHINE_ID,
                this);

        if (record.getInstance() != null) {
            fk.fkInstanceID =
                    ForeignKeyStore.getInstance().getForeignKey(
                    fk,
                    record.getInstance(),
                    ForeignKeyStore.FK_INSTANCES_INSTANCE_ID,
                    this);
        }

        fk.fkAppID =
                ForeignKeyStore.getInstance().getForeignKey(
                fk,
                record.getAppName(),
                ForeignKeyStore.FK_DEPLOYEDAPPS_APP_ID,
                this);

        fk.fkBranchTagID =
                ForeignKeyStore.getInstance().getForeignKey(fk, eet.getBranchName(), ForeignKeyStore.FK_BRANCH_TAG_ID, this);

        if (eet.getContext() != null) {

            fk.fkContextID =
                    ForeignKeyStore.getInstance().getForeignKey(
                    fk,
                    eet.getContext(),
                    ForeignKeyStore.FK_CONTEXTS_CONTEXT_ID,
                    this);

        } else {

            fk.fkContextID = 0;

        }

        Connection con = null;

        try {

            PreparedStatement pstmt = (PreparedStatement) threadLocalPstmt.get();

            pstmt.setInt(1, fk.fkMachineID);

            pstmt.setInt(2, fk.fkAppID);

            pstmt.setInt(3, eet.getClassification());

            pstmt.setInt(4, fk.fkContextID);

            pstmt.setInt(5, fk.fkBranchTagID);

            pstmt.setTimestamp(6, new java.sql.Timestamp(record.getEventTime().getTime().getTime()));

            pstmt.setInt(7, eet.getLoadTime());

            pstmt.setInt(8, fk.fkInstanceID);


            blockInsert(pstmt);

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
