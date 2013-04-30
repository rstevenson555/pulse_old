/*
 * Created on Dec 31, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.bos.art.logParser.db.maintanence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.bos.art.logParser.db.ConnectionPoolT;

import edu.luc.cs.trull.task.Task;

/**
 * @author I0360D3
 *
 * TODO To change the template for this generated type comment go to Window - Preferences - Java - Code Style - Code
 * Templates
 */
public class StackTraceCleaner implements Task {

    private static final String DELETE_2 = "delete from StackTraceBeanContainers where Trace_ID>=? and Trace_ID<=?";
    private static final String DELETE_1 = "delete from StackTraceDetails where Trace_ID>=? and Trace_ID<=?";
    private static final String DELETE_3 = "delete from StackTraces where Trace_ID>=? and Trace_ID<=?";
    private static final int MAX_INCREMENT = 500000;
    //private static final String SELECT_MAX_TRACE_ID = "select max(Trace_id) as max_tid, min(Trace_id) as min_tid from StackTraces where Trace_Time <DATE_SUB(NOW(),INTERVAL 30 DAY)";
    private static final String SELECT_MAX_TRACE_ID = "select max(Trace_id) as max_tid, min(Trace_id) as min_tid from StackTraces where Trace_Time < NOW() - interval '15 days'";
    private int minTraceID;
    private int maxTraceID;
    private int currentTraceID;
    private long startTime;
    private long estimatedFinishTime;
    private int initialIncrementAmount = 2;
    private Connection removeCon;

    /*
     * (non-Javadoc) @see edu.luc.cs.trull.task.Task#hasNext()
     */
    public boolean hasNext() {
        return currentTraceID < maxTraceID;
    }

    /*
     * (non-Javadoc) @see edu.luc.cs.trull.task.Task#next()
     */
    public Object next() {
        long transactionStartTime = System.currentTimeMillis();
        if (removeCon == null) {
            try {
                removeCon = ConnectionPoolT.getConnection();
            } catch (Exception e) {
                e.printStackTrace();
                removeCon = null;
                return new Integer(0);
            }
        }
        PreparedStatement pstmt = null;
        PreparedStatement pstmt2 = null;
        PreparedStatement pstmt3 = null;
        try {
            int maxRemoveRecordPK = (currentTraceID + initialIncrementAmount < maxTraceID) ? currentTraceID + initialIncrementAmount : maxTraceID;

            pstmt = removeCon.prepareStatement(DELETE_1);
            pstmt2 = removeCon.prepareStatement(DELETE_2);
            pstmt3 = removeCon.prepareStatement(DELETE_3);

            pstmt.setInt(1, currentTraceID);
            pstmt.setInt(2, maxRemoveRecordPK);
            pstmt2.setInt(1, currentTraceID);
            pstmt2.setInt(2, maxRemoveRecordPK);
            pstmt3.setInt(1, currentTraceID);
            pstmt3.setInt(2, maxRemoveRecordPK);

            int d1Removed = pstmt.executeUpdate();
            int d2Removed = pstmt2.executeUpdate();
            int d3Removed = pstmt3.executeUpdate();
            System.out.println("Stacktrace d1: " + d1Removed);
            System.out.println("StackTrace d2:" + d2Removed);
            System.out.println("StackTrace d3:" + d3Removed);

            //System.out.println(" delete from AccessRecords where RecordPK>="+currentTraceID+" and RecordPK<="+maxRemoveRecordPK);

        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            pstmt.close();
            pstmt2.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        currentTraceID += initialIncrementAmount;
        long currentSystemTime = System.currentTimeMillis();
        long elapsedTime = currentSystemTime - transactionStartTime;
        if (elapsedTime < 2000) {
            if (initialIncrementAmount < MAX_INCREMENT) {
                initialIncrementAmount *= 2;

            } else {
                initialIncrementAmount = MAX_INCREMENT;
            }

        } else if (elapsedTime > 4000) {
            if (initialIncrementAmount > 4) {
                initialIncrementAmount /= 2;

            } else {
                initialIncrementAmount = 4;
            }
        }
        int totalRemovals = maxTraceID - minTraceID;
        int removedRemovals = currentTraceID - minTraceID;
        double percentCompleted = 1.0;
        if (totalRemovals > 0) {
            percentCompleted = ((double) removedRemovals / (double) totalRemovals);
        }
        long totalElapsedTime = currentSystemTime - startTime;
        estimatedFinishTime = startTime + (long) (totalElapsedTime / percentCompleted);
        int progressBarPercentage = ((int) ((double) 100 * percentCompleted));
        return new Integer(progressBarPercentage);
    }

    /*
     * (non-Javadoc) @see edu.luc.cs.trull.task.Task#restart()
     */
    public void restart() {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = ConnectionPoolT.getConnection();
            pstmt = con.prepareStatement(SELECT_MAX_TRACE_ID);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                minTraceID = rs.getInt("min_tid");
                maxTraceID = rs.getInt("max_tid");
                currentTraceID = minTraceID;
                startTime = System.currentTimeMillis();
                estimatedFinishTime = startTime + 1000 * 60 * 60 * 2;
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                rs.close();
                pstmt.close();
                con.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public int getCurrentTraceID() {
        return currentTraceID;
    }

    public void setCurrentTraceID(int currentTraceID) {
        this.currentTraceID = currentTraceID;
    }

    public long getEstimatedFinishTime() {
        return estimatedFinishTime;
    }

    public void setEstimatedFinishTime(long estimatedFinishTime) {
        this.estimatedFinishTime = estimatedFinishTime;
    }

    public int getInitialIncrementAmount() {
        return initialIncrementAmount;
    }

    public void setInitialIncrementAmount(int initialIncrementAmount) {
        this.initialIncrementAmount = initialIncrementAmount;
    }

    public int getMaxTraceID() {
        return maxTraceID;
    }

    public void setMaxTraceID(int maxTraceID) {
        this.maxTraceID = maxTraceID;
    }

    public int getMinTraceID() {
        return minTraceID;
    }

    public void setMinTraceID(int minTraceID) {
        this.minTraceID = minTraceID;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }
}
