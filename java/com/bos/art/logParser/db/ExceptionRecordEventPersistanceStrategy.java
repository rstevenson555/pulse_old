/*
 * Created on Oct 29, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package com.bos.art.logParser.db;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import org.apache.log4j.Logger;
import java.util.ArrayList;
import java.util.Iterator;
import org.apache.commons.codec.binary.Base64;
import java.io.StringReader;
import java.io.BufferedReader;

import com.bos.art.logParser.records.AccessRecordsForeignKeys;
import com.bos.art.logParser.records.ILiveLogParserRecord;
import com.bos.art.logParser.records.ExceptionRecordEvent;
/**
 * @author I0360D3
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class ExceptionRecordEventPersistanceStrategy extends BasePersistanceStrategy implements PersistanceStrategy {

    private static boolean base64Encoded = true;
    static {
        if (System.getProperty("base64Encoded")!=null)
            base64Encoded = Boolean.parseBoolean(System.getProperty("base64Encoded"));
    }
    private static int BATCH_INSERT_SIZE = 1;
    private final static int MAXBATCHINSERTSIZE = 100;
    private final static int INCREMENT_AMOUNT = 10;
    private final static int MINBATCHINSERTSIZE = 10;
    private static int currentBatchInsertSize =     MINBATCHINSERTSIZE;
    private static int currentBatchItemInsertSize = MINBATCHINSERTSIZE;
    private static double timePerInsert = 5000.0;
    private static double timePerItemInsert = 5000.0;

    
    private static int globalAccessRecordCounter = 0;
    private static int AccessRecordsRecordPK;
    private static int nextTraceID ;
    private static Object lock = new Object();
    protected ExceptionRecordEventPersistanceStrategy() {
        nextTraceID = selectNextValidTraceID();
        nextTraceID++;
    }
    private static ExceptionRecordEventPersistanceStrategy instance;
    private static final Object initLock = new Object();

    public static ExceptionRecordEventPersistanceStrategy getInstance() {
        if (instance == null) {
            synchronized (initLock) {
                if (instance == null) {
                    instance = new ExceptionRecordEventPersistanceStrategy();
                }
            }
        }
        return instance;
    }
    private static final Logger logger = (Logger)Logger.getLogger(ExceptionRecordEventPersistanceStrategy.class.getName());

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

    private static final String STACK_TRACE_INSERT = "insert into stacktraces (trace_id,trace_key,trace_message,trace_time,art_user_id) values (?,?,?,?,?)";
    private static final String STACK_TRACE_ITEM_INSERT = "insert into stacktracedetails (trace_id,row_id,stack_depth) values (?,?,?)";

    
    private static ThreadLocal threadLocalPstmt = new ThreadLocal() {
        protected synchronized Object initialValue() {
            try {
                return ((Connection)threadLocalCon.get()).prepareStatement(STACK_TRACE_INSERT);
            } catch (SQLException se) {
                logger.error("SQL Exception ", se);
            }

            return null;
        }
    };

    private static ThreadLocal threadLocalItemPstmt = new ThreadLocal() {
        protected synchronized Object initialValue() {
            try {
                return ((Connection)threadLocalCon.get()).prepareStatement(STACK_TRACE_ITEM_INSERT);
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
    private static ThreadLocal threadLocalItemInserts = new ThreadLocal() {
        protected synchronized Object initialValue() {
            return new Integer(0);
        }
    };
    public void resetThreadLocalPstmts() {
        logger.info("Resetting the Pstmt ExceptionRecordEventPersistanceStrategy !");
        PreparedStatement ps = (PreparedStatement)threadLocalPstmt.get();
        PreparedStatement itemps = (PreparedStatement)threadLocalItemPstmt.get();
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
                con.prepareStatement(STACK_TRACE_INSERT);
            itemps = con.prepareStatement(STACK_TRACE_ITEM_INSERT);

            threadLocalCon.set(con);
            threadLocalPstmt.set(ps);
            threadLocalItemPstmt.set(itemps);
            synchronized(initLock){
                nextTraceID = selectNextValidTraceID();
            }

        } catch (Exception e) {
            logger.error("Exception ", e);
        }
    }

    private int selectNextValidTraceID() {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = ConnectionPoolT.getConnection();
            pstmt = con.prepareStatement("Select trace_id from StackTraces order by trace_id desc limit 1");
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

                if ( (currentTimePerInsert * 1.05) < timePerInsert && (currentBatchInsertSize < MAXBATCHINSERTSIZE-INCREMENT_AMOUNT)) {
                    currentBatchInsertSize += INCREMENT_AMOUNT;
                    timePerInsert = currentTimePerInsert;
                    logger.debug("AccessRecordPersistanceStrategy currentBatchInsertSize set to-> : " + currentBatchInsertSize);
                } else if ( (currentTimePerInsert * .85) > timePerInsert && (currentBatchInsertSize > MINBATCHINSERTSIZE+INCREMENT_AMOUNT)) {
                    currentBatchInsertSize -= INCREMENT_AMOUNT;
                    timePerInsert = currentTimePerInsert;
                    logger.debug("AccessRecordPersistanceStrategy currentBatchInsertSize set to-> : " + currentBatchInsertSize);
                }
            }        
        } catch (SQLException se) {
            logger.error("Exception", se);

            if(se.getNextException() != null){
                logger.error("nextException", se.getNextException());
            }

            resetThreadLocalPstmts();
        }
    }

    
    public void blockItemInsert(PreparedStatement pstmt) {
        try {
            pstmt.addBatch();
            Integer count = (Integer)threadLocalItemInserts.get();
            int icount = count.intValue()+1;
            threadLocalItemInserts.set(new Integer(icount));
            if (icount % currentBatchItemInsertSize == 0) {
                long startTime = System.currentTimeMillis();
                pstmt.executeBatch();
                long elapsed = System.currentTimeMillis() - startTime;
                double currentTimePerItemInsert = (double)elapsed / (double)currentBatchInsertSize;

                if ( (currentTimePerItemInsert * 1.05) < timePerItemInsert && (currentBatchItemInsertSize < MAXBATCHINSERTSIZE - INCREMENT_AMOUNT)) {
                    currentBatchItemInsertSize += INCREMENT_AMOUNT;
                    timePerItemInsert = currentTimePerItemInsert;
                    logger.info("AccessRecordPersistanceStrategy currentBatchInsertSize set to-> : " + currentBatchItemInsertSize);
                } else if ( (currentTimePerItemInsert * .85) > timePerItemInsert && (currentBatchItemInsertSize > MINBATCHINSERTSIZE+INCREMENT_AMOUNT)) {
                    currentBatchItemInsertSize -= INCREMENT_AMOUNT;
                    timePerItemInsert = currentTimePerItemInsert;
                    logger.info("AccessRecordPersistanceStrategy currentBatchInsertSize set to-> : " + currentBatchItemInsertSize);
                }
            }        
        } catch (SQLException se) {
            logger.error("Exception", se);

            if(se.getNextException() != null){
                logger.error("nextException", se.getNextException());
            }

            resetThreadLocalPstmts();
        }
    }

    //private static final SimpleDateFormat sdfMySQLDate = new SimpleDateFormat("yyyyMMddHHmmss");
    /* (non-Javadoc)
     * @see com.bos.art.logParser.db.PersistanceStrategy#writeToDatabase(com.bos.art.logParser.records.ILiveLogParserRecord)
     */
    public boolean writeToDatabase(ILiveLogParserRecord record) {
        if(!(record instanceof ExceptionRecordEvent)){
            return false;
        }
        ExceptionRecordEvent ere = (ExceptionRecordEvent)record;
        ExceptionHeaderInformation header = new ExceptionHeaderInformation();
        synchronized (initLock) {
            nextTraceID++;
            header.traceId = nextTraceID;
        }
        header.traceKey = ere.getSessionId();
        header.traceTime = ere.getEventTime().getTime();
        header.userName = ere.getUserKey();
        header.traceMessage = ere.getMessage();

        ArrayList listStack = getStack(ere, header);
        try{
            int userid=0;
            if(header.userName != null){
                userid = ForeignKeyStore.getInstance().getForeignKey( null, header.userName, ForeignKeyStore.FK_USERS_USER_ID,this);
            }
            PreparedStatement pstmt=  (PreparedStatement)threadLocalPstmt.get();
            pstmt.setInt(1,header.traceId);
            pstmt.setString(2,header.traceKey);
            if(header.traceMessage != null && header.traceMessage.length()>250){
                logger.warn("Trunkating a long Message : " + header.traceMessage);
                header.traceMessage= header.traceMessage.substring(0,249);
            }
            pstmt.setString(3,header.traceMessage);
            pstmt.setTimestamp(4,new java.sql.Timestamp(header.traceTime.getTime()));
            pstmt.setInt(5,userid);
            //blockInsert(pstmt);
            pstmt.execute();
        }catch (java.sql.SQLException e){
            logger.error("Error with write to db..."+header.traceId+"\t:"+header.traceKey+"\t:"+header.traceTime+"\t:"+header.userName,e);
            resetThreadLocalPstmts();
            return false;
        }
                

        Iterator iter = listStack.iterator();
        while(iter.hasNext()){
            ExceptionItemInformation item = (ExceptionItemInformation)iter.next();
            try{

                PreparedStatement pstmtItem =  (PreparedStatement)threadLocalItemPstmt.get();
                pstmtItem.setInt(1,item.traceId);
                pstmtItem.setInt(2,item.rowId);
                pstmtItem.setInt(3,item.depth);
                logger.debug("StackTraceItemBI:   "+item.traceId+"\t:"+item.depth+"\t:"+item.rowId);
               // blockItemInsert(pstmtItem);
                pstmtItem.execute();
            }catch (java.sql.SQLException e){
                logger.error("Error with write to db..."+item.traceId+"\t:"+item.rowId+"\t:"+item.depth,e);
                resetThreadLocalPstmts();
                return false;
            }
        }
        return true;
    }

    private ArrayList getStack(ExceptionRecordEvent ere, ExceptionHeaderInformation header){
        ArrayList stack = new ArrayList();
        byte[] decodeBA = null;
        String stackTrace = null;

        if (base64Encoded) {
            decodeBA = com.bos.art.logParser.tools.Base64.decodeFast(ere.getEncodedException().getBytes());
            stackTrace = new String(decodeBA);
        } else {
            stackTrace = ere.getEncodedException();
        }

        logger.warn("stackTrace is: " + stackTrace);

        if ( logger.isDebugEnabled())
            logger.debug("stackTrace" + stackTrace);
        
        String stackMessage = null;
        if ( base64Encoded) {
            decodeBA = com.bos.art.logParser.tools.Base64.decodeFast(ere.getMessage().getBytes());
            stackMessage = new String(decodeBA);
        } else {
            stackMessage = ere.getMessage();
        }
        //System.out.println("stackMessage before: " + stackMessage);
        if(stackMessage  == null || stackMessage.equalsIgnoreCase("null")){
            stackMessage = null;        
        }
        if (logger.isDebugEnabled())
            logger.debug("traceMessage" + stackMessage);
        
        BufferedReader br = new BufferedReader(new StringReader(stackTrace));
        String line = null;
        int depth = 0;
        try{
            while((line = br.readLine())!=null){
                
             if(line.indexOf("at ")>-1){
                    int detailId = getDetailId(line);
                    ExceptionItemInformation eii = new ExceptionItemInformation();
                    eii.traceId = header.traceId;
                    eii.rowId = detailId;
                    eii.depth = ++depth;
                    stack.add(eii);
             }else if(depth == 0){
                 stackMessage = line ;//+ stackMessage;
                 //System.out.println("stackMessage altered : " + stackMessage);
                 header.traceMessage=stackMessage;
             }
            }
        }catch(java.io.IOException e){
            logger.error("Exception processing a stack trace detail!  IO type exception ! ", e);
        }
        return stack;
    }

    private int getDetailId(String line){
        //System.out.println("value: " + line);
        int fk =ForeignKeyStore.getInstance().getForeignKey( null, line, ForeignKeyStore.FK_STACKTRACEROW_ID,this);
        //System.out.println("fk : " + fk);
        return fk;
    }

    private class ExceptionHeaderInformation {
        int traceId;
        String traceKey;
        String traceMessage;
        java.util.Date traceTime;
        String userName;
    }

    private class ExceptionItemInformation{
        int traceId;
        int rowId;
        int depth;
    }
        
        
}
























