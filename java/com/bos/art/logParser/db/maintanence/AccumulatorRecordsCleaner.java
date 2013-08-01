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
import java.sql.SQLException;
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
public class AccumulatorRecordsCleaner implements Task {
    //private static final int CLEAN_NUMBER_OF_DAYS = 1000 * 60 * 60 * 24 * 7 * 1;
    private static final long CLEAN_NUMBER_OF_DAYS = TimeIntervalConstants.SEVEN_DAYS_MILLIS;

    private static final int VACUUM_REMOVAL_THRESHOLD = 20 * 1000000;
    private static final int MAX_INCREMENT = 500000;
    private int minRecordPK;
    private int maxRecordPK;
    private int currentRecordPK;
    private long startTime;
    private long estimatedFinishTime;
    private int initialIncrementAmount = 2;
    private Connection removeCon;
    private static final Logger logger = (Logger) Logger.getLogger(AccumulatorRecordsCleaner.class.getName());

    /*
     * (non-Javadoc) @see edu.luc.cs.trull.task.Task#hasNext()
     */
    public boolean hasNext() {
        //return false;
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

            pstmt = removeCon.prepareStatement("delete from accumulatorevent where accumulatorevent_id>=? and accumulatorevent_id<=?");
            pstmt.setInt(1, currentRecordPK);
            pstmt.setInt(2, maxRemoveRecordPK);

            int rowsRemoved = pstmt.executeUpdate();
            logger.info("AccumulatorEvent Rows Removed: " + rowsRemoved);
            logger.info("delete from accumulatorevent where accumulatorevent_id>=" + currentRecordPK + " and accumulatorevent_id<=" + maxRemoveRecordPK);

        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            pstmt.close();
        } catch (Exception e) {
            logger.error("AccumulatorEvent error",e);
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
    private static final String SELECT_LOWER_RECORDPK = "select  accumulatorevent_id from accumulatorevent order by accumulatorevent_id asc limit 1";
    private static final String SELECT_UPPER_RECORDPK = "select accumulatorevent_id from accumulatorevent order by accumulatorevent_id desc limit 1";
    private static final String SELECT_RECORDPK_TIME = "select time from accumulatorevent where accumulatorevent_id =? ";
    private static final String SELECT_UPPER_BOUND = "select * from accumulatorevent where accumulatorevent_id>? order by accumulatorevent_id asc limit 1";
    /*
     * (non-Javadoc) @see edu.luc.cs.trull.task.Task#restart()
     */

    public void restart() {
        System.out.println("Restart Called");
        startTime = System.currentTimeMillis();
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        PreparedStatement pstmt2 = null;
        ResultSet rs2 = null;
        try {

            con = ConnectionPoolT.getConnection();
            pstmt = con.prepareStatement(SELECT_LOWER_RECORDPK);
            rs = pstmt.executeQuery();
            int systemMinRecordPK = 0;
            while (rs.next()) {
                systemMinRecordPK = rs.getInt("accumulatorevent_id");
            }
            minRecordPK = systemMinRecordPK;
            currentRecordPK = minRecordPK;

            pstmt2 = con.prepareStatement(SELECT_UPPER_RECORDPK);
            rs2 = pstmt2.executeQuery();
            int systemMaxRecordPK = 0;
            if (rs2.next()) {
                systemMaxRecordPK = rs2.getInt("accumulatorevent_id");
            }
            long targetTime = System.currentTimeMillis() - CLEAN_NUMBER_OF_DAYS;
            int recordPKUpperLimit = estimateBounds(systemMinRecordPK, systemMaxRecordPK, targetTime);
            maxRecordPK = recordPKUpperLimit;


        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                rs.close();
                pstmt.close();
                pstmt2.close();
                rs2.close();
                con.close();
            } catch (Exception e) {
                logger.error(e);
            }
        }
    }

    public int estimateBounds(int min, int max, long targetTime) throws SQLException {

        System.out.println("Estimating Bounds for : " + min + " : " + max + " : " + targetTime);
        if (max - min < 100) {
            return min;
        }
        Connection conn = null;
        conn = ConnectionPoolT.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(SELECT_RECORDPK_TIME);
        pstmt.setInt(1, min);
        ResultSet rs = pstmt.executeQuery();
        long lowerTime = 0;
        long upperTime = 0;
        if (rs.next()) {
            java.sql.Timestamp ts = rs.getTimestamp(1);
            lowerTime = ts.getTime();
        }
        pstmt.setInt(1, max);
        rs = pstmt.executeQuery();
        if (rs.next()) {
            java.sql.Timestamp ts = rs.getTimestamp(1);
            upperTime = ts.getTime();
        }
        double dlowerTime = (double) lowerTime;
        double dupperTime = (double) upperTime;
        int estimatedRPK = 0 - (int) ((dlowerTime - targetTime) / (dlowerTime - dupperTime) * ((double) (min - max)) - (double) min);
        if (estimatedRPK < min || estimatedRPK > max) {
            throw new RuntimeException("rpk out of bounds");
        }
        pstmt = conn.prepareStatement(SELECT_UPPER_BOUND);
        pstmt.setInt(1, estimatedRPK);
        rs = pstmt.executeQuery();
        int newMin = min;
        int newMax = max;
        if (rs.next()) {
            int recordpk = rs.getInt("accumulatorevent_id");
            java.sql.Timestamp ts = rs.getTimestamp("time");
            if (ts.getTime() < targetTime) {
                newMin = recordpk;
            } else {
                newMax = recordpk;
            }
            logger.info("NewMin : " + newMin);
            logger.info("NewMax : " + newMax);
            logger.info("Target : " + targetTime);
            logger.info("timeFound : " + ts.getTime());
            logger.info("minOff : " + ((double) ts.getTime() - (double) targetTime) / 1000.0 / 60.0);

        }
        rs.close();
        pstmt.close();
        conn.close();

        return estimateBounds(newMin, newMax, targetTime);

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

