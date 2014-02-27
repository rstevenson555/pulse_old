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
import com.bos.helper.SingletonInstanceHelper;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;

/**
 * @author I0360D3
 *         <p/>
 *         To change the template for this generated type comment go to Window>Preferences>Java>Code Generation>Code and Comments
 */
public class HtmlPageRecordPersistanceStrategy extends BasePersistanceStrategy implements PersistanceStrategy {

    private final static int MAXBATCHINSERTSIZE = 800;
    private final static int INCREMENT_AMOUNT = 10;
    private final static int MINBATCHINSERTSIZE = 700;
    private static int currentBatchInsertSize = MINBATCHINSERTSIZE;
    private static final Object initLock = new Object();
    private static final Logger logger = (Logger) Logger.getLogger(HtmlPageRecordPersistanceStrategy.class.getName());
    private static double timePerInsert = 5000.0;
    private static SingletonInstanceHelper instance = new SingletonInstanceHelper<HtmlPageRecordPersistanceStrategy>(HtmlPageRecordPersistanceStrategy.class) {
        @Override
        public java.lang.Object createInstance() {
            return new HtmlPageRecordPersistanceStrategy();
        }
    };
    private static long batch = 0;
    private static DateTime batchOneMinute = new DateTime().plusMinutes(1);
    private static DateTime batchNow = new DateTime();
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
    private static ThreadLocal threadLocalExperienceCon = new ThreadLocal() {

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
                                + "Instance_ID, "
                                + "experience "
                                + " ) "
                                + " values (?,?,?,?,?,?,?,?,?,?,?)");
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
    private DateTime now = null;
    private DateTime oneMinute = new DateTime().plusMinutes(1);
    private long recordsPerMinute = 0;

    protected HtmlPageRecordPersistanceStrategy() {
    }

    public static HtmlPageRecordPersistanceStrategy getInstance() {
        return (HtmlPageRecordPersistanceStrategy) instance.getInstance();
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
                                    + "Instance_ID, "
                                    + "experience"
                                    + ") "
                                    + " values (?,?,?,?,?,?,?,?,?,?,?)");
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
            batchNow = new DateTime();

            if (icount % currentBatchInsertSize == 0) {
                long startTime = System.currentTimeMillis();
                pstmt.executeBatch();
                batch++;

                if (batchNow.isAfter(batchOneMinute)) {
                    logger.warn("HtmlPageRecordPersistanceStrategy " + " batch per minute: " + (batch) + " records per minute: " + (currentBatchInsertSize * batch));

                    batchOneMinute = batchNow.plusMinutes(1);
                    batch = 0;
                }

                long elapsed = System.currentTimeMillis() - startTime;
                double currentTimePerInsert = (double) elapsed / (double) currentBatchInsertSize;

                if ((currentTimePerInsert * 1.05) < timePerInsert && (currentBatchInsertSize < MAXBATCHINSERTSIZE - INCREMENT_AMOUNT)) {
                    currentBatchInsertSize += INCREMENT_AMOUNT;
                    timePerInsert = currentTimePerInsert;
                    logger.warn("HtmlPageRecordPersistanceStrategy currentBatchInsertSize set to-> : " + currentBatchInsertSize + " time per insert: " + timePerInsert + " elapsed: " + elapsed);

                } else if ((currentTimePerInsert * .85) > timePerInsert && (currentBatchInsertSize > MINBATCHINSERTSIZE + INCREMENT_AMOUNT)) {
                    currentBatchInsertSize -= INCREMENT_AMOUNT;
                    timePerInsert = currentTimePerInsert;
                    logger.warn("HtmlPageRecordPersistanceStrategy currentBatchInsertSize set to-> : " + currentBatchInsertSize + " time per insert: " + timePerInsert + " elapsed: " + elapsed);
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

    /**
     * determine the user - "experience" level
     *
     * @param pagehtml
     * @param experience
     * @return
     */
    private int determineUserExperience(String pagehtml, int experience) {
        int error = 1;
        int order = 1 << 1;
        int four_0_four = 1 << 2;
        int no_search_results = 1 << 3;
        if (StringUtils.isNotBlank(pagehtml)) {
            if (pagehtml.indexOf("We're sorry") != -1)
                experience |= error;
            if (pagehtml.indexOf("Sorry unexpected") != -1)
                experience |= error;
            if (pagehtml.indexOf("Thank you for your order") != -1)
                experience |= order;
            if (pagehtml.indexOf("we couldn't find that page") != -1)
                experience |= four_0_four;
            if (pagehtml.indexOf("did not return any results") != -1)
                experience |= no_search_results;
        }
        return experience;
    }
    //private static final SimpleDateFormat sdfMySQLDate = new SimpleDateFormat("yyyyMMddHHmmss");
    /*
     * (non-Javadoc) @see
     * com.bos.art.logParser.db.PersistanceStrategy#writeToDatabase(com.bos.art.logParser.records.ILiveLogParserRecord)
     */

    public boolean writeToDatabase(ILiveLogParserRecord record) {
        PageRecordEvent pre = (PageRecordEvent) record;
        AccessRecordsForeignKeys fk = pre.obtainForeignKeys();
        now = new DateTime();
        recordsPerMinute++;

        if (now.isAfter(oneMinute)) {
            logger.warn("HtmlPageRecordPersistanceStrategy records per minute: " + (recordsPerMinute));
            oneMinute = now.plusMinutes(1);
            recordsPerMinute = 0;
        }

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
        if (record.getInstance() != null) {
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

        int requestTokenCount = pre.getRequestTokenCount();
        int requestToken = pre.getRequestToken();
        String nonEncodedText = pre.getEncodedPage();

        try {
            PreparedStatement pstmt = (PreparedStatement) threadLocalPstmt.get();

            pstmt.setInt(1, fk.fkBranchTagID);
            pstmt.setInt(2, fk.fkMachineID);
            pstmt.setInt(3, fk.fkContextID);
            pstmt.setInt(4, fk.fkPageID);
            pstmt.setTimestamp(5, new java.sql.Timestamp(record.getEventTime().getTimeInMillis()));
            pstmt.setString(6, pre.getSessionId());
            pstmt.setInt(7, requestToken);
            pstmt.setInt(8, requestTokenCount);
            String pagehtml = nonEncodedText;

            int experience = readSessionUserExperience((Connection) threadLocalExperienceCon.get(), pre.getSessionId());

            // don't store PDF's
            //pstmt.setString(9, pagehtml.indexOf("%PDF-")==0 ? "" : pagehtml);

            if (StringUtils.isNotBlank(pagehtml)) {
                if (pagehtml.indexOf("%PDF-") == 0)
                    pagehtml = "";
                if (pagehtml.indexOf("<U+0089>PNG") == 0)
                    pagehtml = "";

                //byte []bytes = Charset.forName("UTF8").encode(CharBuffer.wrap(pagehtml.toCharArray())).array();
                //pagehtml = new String(bytes,0,pagehtml.length()).replace('\u0000',' ');  //strip null byte

                experience = determineUserExperience(pagehtml, experience);

                //pagehtml = StringEscapeUtils.escapeHtml4(pagehtml);
            } else {
                pagehtml = "";
            }

            pstmt.setString(9, pagehtml);
            pstmt.setInt(10, fk.fkInstanceID);
            pstmt.setString(11, String.valueOf(experience));

            blockInsert(pstmt);

            if (experience != 0) {
                updateSessionUserExperience(experience, pre);
            }
        } catch (SQLException se) {
            logger.error("Exception", se);
            resetThreadLocalPstmt();
            return false;
        } finally {
            //  Removed because of Thread Local.
        }
        return true;
    }

    /**
     * read the user experience from session
     *
     * @param con
     * @param sessiontxt
     * @return
     */
    private int readSessionUserExperience(Connection con, String sessiontxt) {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("select experience from sessions where session_id = (select max(session_id) from sessions where sessiontxt = ? )");
            stmt.setString(1, sessiontxt);
            rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException ex) {
            java.util.logging.Logger.getLogger(HtmlPageRecordPersistanceStrategy.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (stmt != null)
                    stmt.close();
                if (rs != null)
                    rs.close();
            } catch (SQLException ex) {
                java.util.logging.Logger.getLogger(HtmlPageRecordPersistanceStrategy.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return 0;
    }

    /**
     * update the experience field
     *
     * @param experience
     * @param pre
     * @throws SQLException
     */
    private void updateSessionUserExperience(int experience, PageRecordEvent pre) throws SQLException {
        // update session
        PreparedStatement sessionpsmt = ((Connection) threadLocalExperienceCon.get()).prepareStatement("update sessions set experience = ? where session_id = (select max(session_id) from sessions where sessiontxt = ? )");
        sessionpsmt.setInt(1, experience);
        sessionpsmt.setString(2, pre.getSessionId());
        sessionpsmt.executeUpdate();
        sessionpsmt.close();
    }
}
                                                                                        