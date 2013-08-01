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
public class ExternalAccessRecordsCleaner implements Task {
    private static final long CLEAN_NUMBER_OF_DAYS = TimeIntervalConstants.SEVEN_DAYS_MILLIS;

    private static final int VACUUM_REMOVAL_THRESHOLD = 20 * 1000000;
    private static final int MAX_INCREMENT = 500000;
    private long minRecordPK;
    private long maxRecordPK;
    private long currentRecordPK;
    private long startTime;
    private long estimatedFinishTime;
    private long initialIncrementAmount = 2;
    private Connection removeCon;
    private static final Logger logger = (Logger) Logger.getLogger(ExternalAccessRecordsCleaner.class.getName());

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
            long maxRemoveRecordPK = (currentRecordPK + initialIncrementAmount < maxRecordPK) ? currentRecordPK + initialIncrementAmount : maxRecordPK;

            pstmt = removeCon.prepareStatement("delete from ExternalAccessRecords where RecordPK>=? and RecordPK<=?");
            pstmt.setLong(1, currentRecordPK);
            pstmt.setLong(2, maxRemoveRecordPK);

            int rowsRemoved = pstmt.executeUpdate();
            System.out.println("ExternalAccessRecords Rows Removed: " + rowsRemoved);

            System.out.println("delete from ExternalAccessRecords where RecordPK>=" + currentRecordPK + " and RecordPK<=" + maxRemoveRecordPK);

        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            pstmt.close();
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
        long totalRemovals = maxRecordPK - minRecordPK;
        long removedRemovals = currentRecordPK - minRecordPK;
        double percentCompleted = 1.0;
        if (totalRemovals > 0) {
            percentCompleted = ((double) removedRemovals / (double) totalRemovals);
        }
        long totalElapsedTime = currentSystemTime - startTime;
        estimatedFinishTime = startTime + (long) (totalElapsedTime / percentCompleted);
        int progressBarPercentage = ((int) ((double) 100 * percentCompleted));

        return new Integer(progressBarPercentage);
    }
    private static final String SELECT_LOWER_RECORDPK = "select recordpk from externalaccessrecords order by recordpk asc limit 1";
    private static final String SELECT_UPPER_RECORDPK = "select recordpk from externalaccessrecords order by recordpk desc limit 1";
    private static final String SELECT_RECORDPK_TIME = "select time from externalaccessrecords where recordpk =? ";
    private static final String SELECT_UPPER_BOUND = "select * from externalaccessrecords where recordpk>? order by recordpk asc limit 1";
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
            long systemMinRecordPK = 0;
            while (rs.next()) {
                systemMinRecordPK = rs.getLong("recordpk");
            }
            minRecordPK = systemMinRecordPK;
            currentRecordPK = minRecordPK;

            pstmt2 = con.prepareStatement(SELECT_UPPER_RECORDPK);
            rs2 = pstmt2.executeQuery();
            long systemMaxRecordPK = 0;
            while (rs2.next()) {
                systemMaxRecordPK = rs2.getLong("recordpk");
            }
            long targetTime = System.currentTimeMillis() - CLEAN_NUMBER_OF_DAYS;
            long recordPKUpperLimit = estimateBounds(systemMinRecordPK, systemMaxRecordPK, targetTime);
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
                e.printStackTrace();
            }
        }
    }

    public long estimateBounds(long min, long max, long targetTime) throws SQLException {

        System.out.println("Estimating Bounds for : " + min + " : " + max + " : " + targetTime);
        if (max - min < 100) {
            return min;
        }
        Connection conn = null;
        conn = ConnectionPoolT.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(SELECT_RECORDPK_TIME);
        pstmt.setLong(1, min);
        ResultSet rs = pstmt.executeQuery();
        long lowerTime = 0;
        long upperTime = 0;
        if (rs.next()) {
            java.sql.Timestamp ts = rs.getTimestamp(1);
            lowerTime = ts.getTime();
        }
        pstmt.setLong(1, max);
        rs = pstmt.executeQuery();
        if (rs.next()) {
            java.sql.Timestamp ts = rs.getTimestamp(1);
            upperTime = ts.getTime();
        }
        double dlowerTime = (double) lowerTime;
        double dupperTime = (double) upperTime;
        long estimatedRPK = 0 - (long) ((dlowerTime - targetTime) / (dlowerTime - dupperTime) * ((double) (min - max)) - (double) min);
        System.out.println("estimatedRPK " + estimatedRPK + " min " + min + " max " + max);
        //estimatedRPK = (estimatedRPK<min) ? min : estimatedRPK;
        //estimatedRPK = (estimatedRPK>max) ? max : estimatedRPK;
        if (estimatedRPK < min || estimatedRPK > max) {
            System.out.println("Returning max" + (max));
            return max;
            //throw new RuntimeException("rpk out of bounds");
        }
        pstmt = conn.prepareStatement(SELECT_UPPER_BOUND);
        pstmt.setLong(1, estimatedRPK);
        rs = pstmt.executeQuery();
        long newMin = min;
        long newMax = max;
        if (rs.next()) {
            long recordpk = rs.getLong("recordpk");
            java.sql.Timestamp ts = rs.getTimestamp("time");
            if (ts.getTime() < targetTime) {
                newMin = recordpk;
            } else {
                newMax = recordpk;
            }
            System.out.println("NewMin : " + newMin);
            System.out.println("NewMax : " + newMax);
            System.out.println("Target : " + targetTime);
            System.out.println("timeFound : " + ts.getTime());
            System.out.println("minOff : " + ((double) ts.getTime() - (double) targetTime) / 1000.0 / 60.0);

        }
        rs.close();
        pstmt.close();
        conn.close();

        return estimateBounds(newMin, newMax, targetTime);

    }

    public long getCurrentRecordPK() {
        return currentRecordPK;
    }

    public void setCurrentRecordPK(long currentRecordPK) {
        this.currentRecordPK = currentRecordPK;
    }

    public long getEstimatedFinishTime() {
        return estimatedFinishTime;
    }

    public void setEstimatedFinishTime(long estimatedFinishTime) {
        this.estimatedFinishTime = estimatedFinishTime;
    }

    public long getInitialIncrementAmount() {
        return initialIncrementAmount;
    }

    public void setInitialIncrementAmount(long initialIncrementAmount) {
        this.initialIncrementAmount = initialIncrementAmount;
    }

    public long getMaxRecordPK() {
        return maxRecordPK;
    }

    public void setMaxRecordPK(long maxRecordPK) {
        this.maxRecordPK = maxRecordPK;
    }

    public long getMinRecordPK() {
        return minRecordPK;
    }

    public void setMinRecordPK(long minRecordPK) {
        this.minRecordPK = minRecordPK;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }
}

