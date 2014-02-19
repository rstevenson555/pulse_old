/*
 * Created on Oct 29, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package com.bos.art.logParser.db;

import com.bos.art.logParser.records.AccessRecordsForeignKeys;
import com.bos.art.logParser.records.AccumulatorEventTiming;
import com.bos.art.logParser.records.ILiveLogParserRecord;
import com.bos.helper.SingletonInstanceHelper;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * @author I0360D3
 *         <p/>
 *         To change the template for this generated type comment go to Window>Preferences>Java>Code Generation>Code and Comments
 */
public class AccumulatorEventPersistanceStrategy extends BasePersistanceStrategy implements PersistanceStrategy {

    public static final String INTEGER_CLASS_NAME = "Integer";
    private static final int BATCH_INSERT_SIZE = 2;
    private static final Logger logger = (Logger) Logger.getLogger(AccumulatorEventPersistanceStrategy.class.getName());
    private static final String DOUBLE_CLASS_NAME = "Double";
    private static final String BIG_DECIMAL_CLASS_NAME = "BigDecimal";
    private static final String FLOAT_CLASS_NAME = "Float";
    private static final String LONG_CLASS_NAME = "Long";
    private static final String NUMBER_CLASS_NAME = "Number";
    private static SingletonInstanceHelper instance = new SingletonInstanceHelper<AccumulatorEventPersistanceStrategy>(AccumulatorEventPersistanceStrategy.class);

    private static ThreadLocal threadLocalCon = new ThreadLocal() {

        protected synchronized Object initialValue() {
            try {
                return ConnectionPoolT.getConnection();
            } catch (SQLException se) {
                logger.error("SQL Exception ", se);
            }
            return null;
        }
    };
    /*
     * (non-Javadoc) @see
     * com.bos.art.logParser.db.PersistanceStrategy#writeToDatabase(com.bos.art.logParser.records.ILiveLogParserRecord)
     */
    private static ThreadLocal threadLocalPstmt = new ThreadLocal() {

        protected synchronized Object initialValue() {
            try {
                return ((Connection) threadLocalCon.get()).prepareStatement(
                        "insert into AccumulatorEvent "
                                + "(AccumulatorStat_ID, Machine_ID, Context_ID, Branch_ID, App_ID, Time,intValue, doubleValue, stringValue, dataType,Instance_ID ) "
                                + "values (?,?,?,?,?,?,?,?,?,?,?)");
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

    private AccumulatorEventPersistanceStrategy() {
    }

    public static AccumulatorEventPersistanceStrategy getInstance() {
        return (AccumulatorEventPersistanceStrategy) instance.getInstance();
    }

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
                            "insert into AccumulatorEvent "
                                    + "(AccumulatorStat_ID, Machine_ID, Context_ID, Branch_ID, App_ID, Time,intValue, doubleValue, stringValue, dataType,Instance_ID ) "
                                    + "values (?,?,?,?,?,?,?,?,?,?,?)");
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
            }
        } catch (SQLException se) {
            logger.error("Exception", se);
            logger.error("Next Exception", se.getNextException());
            resetThreadLocalPstmt();
        }
    }

    public boolean writeToDatabase(ILiveLogParserRecord record) {
        //SimpleDateFormat sdfMySQLDate = new SimpleDateFormat("yyyyMMddHHmmss");
        AccessRecordsForeignKeys fk = ((AccumulatorEventTiming) record).obtainForeignKeys();
        AccumulatorEventTiming eet = ((AccumulatorEventTiming) record);
        //warnAccumulatorEvent(eet);
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
        /*
         * "insert into AccumulatorEvent " + "(AccumulatorStat_ID, Machine_ID, Context_ID, Branch_ID, App_ID, Time,intValue,
         * doubleValue, stringValue, dataType ) " + "values (?,?,?,?,?,?,?,?,?,?)");
         */
        try {
            PreparedStatement pstmt = (PreparedStatement) threadLocalPstmt.get();
            pstmt.setInt(1, eet.getClassification());
            pstmt.setInt(2, fk.fkMachineID);
            pstmt.setInt(3, fk.fkContextID);
            pstmt.setInt(4, fk.fkBranchTagID);
            pstmt.setInt(5, fk.fkAppID);
            //pstmt.setString(6, sdfMySQLDate.format(record.getEventTime().getTime()));
            pstmt.setTimestamp(6, new java.sql.Timestamp(record.getEventTime().getTime().getTime()));
            pstmt.setInt(7, getIntValue(eet));
            pstmt.setDouble(8, getDoubleValue(eet));
            pstmt.setString(9, eet.getValue());
            pstmt.setString(10, eet.getType());
            pstmt.setInt(11, fk.fkInstanceID);
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

    private int getIntValue(AccumulatorEventTiming aet) {
        if (aet.getType().indexOf(INTEGER_CLASS_NAME) > -1) {
            return new Integer(aet.getValue()).intValue();
        } else {
            return -1;
        }
    }

    private double getDoubleValue(AccumulatorEventTiming aet) {
        if ((aet.getType().indexOf(DOUBLE_CLASS_NAME) > -1)
                || (aet.getType().indexOf(BIG_DECIMAL_CLASS_NAME) > -1)
                || (aet.getType().indexOf(FLOAT_CLASS_NAME) > -1)
                || (aet.getType().indexOf(LONG_CLASS_NAME) > -1)
                || (aet.getType().indexOf(NUMBER_CLASS_NAME) > -1)) {
            return new Double(aet.getValue()).doubleValue();
        } else {
            return -1.0000;
        }
    }

    private void warnAccumulatorEvent(AccumulatorEventTiming aet) {
        StringBuilder sb = new StringBuilder("-------Accumulator---------");
        sb.append("\n");
        sb.append(aet.getAppName()).append(":>appname:").append(aet.getContext()).append(":>Context:").append(aet.getBranchName()).append(":>BranchName").append(aet.getClassification()).append(":>classification:").append(aet.getType()).append(":>type:").append(aet.getValue()).append(":>value:");
        logger.warn(sb.toString());
    }
}
