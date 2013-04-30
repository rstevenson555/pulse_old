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
import java.util.Properties;

import com.bos.art.logParser.db.ConnectionPoolT;
import com.bos.art.logServer.utils.TimeIntervalConstants;

import edu.luc.cs.trull.task.Task;
import org.apache.log4j.Logger;

/**
 * @author I0360D3
 *
 * TODO To change the template for this generated type comment go to Window - Preferences - Java - Code Style - Code
 * Templates
 */
public class AccessRecordsCleaner implements Task {

    private static final int VACUUM_REMOVAL_THRESHOLD = 20 * 1000000;
    private static final int MAX_INCREMENT = 500000;
    private static final String SELECT_MAX_RECORDPK =// "select max(RecordPK) as max_rpk, min(RecordPK) as min_rpk from AccessRecords where Time<NOW() - interval '30 days')";
            " select a.maxRPK as max_rpk , b.minRPK  as min_rpk from "
            + " (select recordpk as maxRPK from AccessRecords where time < now() - interval '12 days' - interval '12 hours' and time > now() - interval '16 days' - interval '12 hours' order by time desc limit 1) a, "
            + "  (select recordpk as minRPK from AccessRecords order by recordpk asc limit 1) b limit 1";
    private int minRecordPK;
    private int maxRecordPK;
    private int currentRecordPK;
    private long startTime;
    private long estimatedFinishTime;
    private int initialIncrementAmount = 2;
    private Connection removeCon;
    private static final Logger logger = (Logger) Logger.getLogger(AccessRecordsCleaner.class.getName());

    /*
     * (non-Javadoc) @see edu.luc.cs.trull.task.Task#hasNext()
     */
    public boolean hasNext() {
        return currentRecordPK < maxRecordPK;
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
        try {
            int maxRemoveRecordPK = (currentRecordPK + initialIncrementAmount < maxRecordPK) ? currentRecordPK + initialIncrementAmount : maxRecordPK;

            pstmt = removeCon.prepareStatement("delete from AccessRecords where RecordPK>=? and RecordPK<=?");
            pstmt2 = removeCon.prepareStatement("delete from QueryParamRecords where RecordPK>=? and RecordPK<=?");
            pstmt.setInt(1, currentRecordPK);
            pstmt.setInt(2, maxRemoveRecordPK);
            
            pstmt2.setInt(1, currentRecordPK);
            pstmt2.setInt(2, maxRemoveRecordPK);

            int rowsRemoved = pstmt.executeUpdate();
            int qprowsRemoved = pstmt2.executeUpdate();
            logger.info("AccessRecords Rows Removed: " + rowsRemoved);
            logger.info("QueryParamRecords Removed :" + qprowsRemoved);

            logger.info("delete from AccessRecords where RecordPK>=" + currentRecordPK + " and RecordPK<=" + maxRemoveRecordPK);

        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            pstmt.close();
            pstmt2.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        currentRecordPK += initialIncrementAmount;
        long currentSystemTime = System.currentTimeMillis();
        long elapsedTime = currentSystemTime - transactionStartTime;
        if (elapsedTime < 4000) {
            if (initialIncrementAmount < MAX_INCREMENT) {
                initialIncrementAmount *= 2;

            } else {
                initialIncrementAmount = MAX_INCREMENT;
            }

        } else if (elapsedTime > 8000) {
            if (initialIncrementAmount > 4) {
                initialIncrementAmount /= 2;

            } else {
                initialIncrementAmount = 4;
            }
        }
        int totalRemovals = maxRecordPK - minRecordPK;
        int removedRemovals = currentRecordPK - minRecordPK;
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
            pstmt = con.prepareStatement(SELECT_MAX_RECORDPK);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                minRecordPK = rs.getInt("min_rpk");
                maxRecordPK = rs.getInt("max_rpk");
                currentRecordPK = minRecordPK;
                startTime = System.currentTimeMillis();
                estimatedFinishTime = startTime + TimeIntervalConstants.TWO_HOURS;
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

    public int getCurrentRecordPK() {
        return currentRecordPK;
    }

    public void setCurrentRecordPK(int currentRecordPK) {
        this.currentRecordPK = currentRecordPK;
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

    public int getMaxRecordPK() {
        return maxRecordPK;
    }

    public void setMaxRecordPK(int maxRecordPK) {
        this.maxRecordPK = maxRecordPK;
    }

    public int getMinRecordPK() {
        return minRecordPK;
    }

    public void setMinRecordPK(int minRecordPK) {
        this.minRecordPK = minRecordPK;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }
}
