/*
 * Created on Oct 22, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package com.bos.art.logParser.statistics;

import com.bos.art.logParser.records.AccessRecordsForeignKeys;
import com.bos.art.logParser.tools.MemoryTool;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * @author I0360D3
 *         <p/>
 *         To change the template for this generated type comment go to
 *         Window>Preferences>Java>Code Generation>Code and Comments
 */
public class TimeSpanEventContainer implements Serializable, IEventContainer {
    private static final int FIFTEEN_SECONDS = 15000;
    private static final int FIVE_SECONDS = 5000;

    private static final int MILLISECOND_RESOLUTION = 1000;
    private static final int MILLISECOND_BUCKET_RESOLUTION = 100;
    private static final int TEN_SECONDS = 10000;
    private static final int THIRTY_SECONDS = 30000;
    private static final int TWENTY_SECONDS = 20000;

    private static final Logger logger = (Logger) Logger.getLogger(TimeSpanEventContainer.class.getName());
    private static final DateTimeFormatter fdf = DateTimeFormat.forPattern("yyyy/MM/dd HH:mm:ss");
    //  Class Administative Parameters.
    //
    // dirty                 - determines whether to recalculate the Percentiles.
    // databaseDirty         - either use insert or update.
    // lastModDate           - Used for determining Closure (Imprtant to prevent Memory Leak
    // isReloaded            - Reloads can't determine Percentiles because the TreeMap is gone.
    // timesPersisted        - Count How Many Times this is Persisted.
    // persistOpportunities  - Count How Many Times this is Persisted.
    private boolean dirty = true;
    private boolean databaseDirty = true;
    private java.util.Date lastModDate = new java.util.Date();
    private boolean isReload = false;
    private int timesPersisted = 0;
    private int persistOpportunities = 0;
    // persistance Times
    //
    private int modDelayMinutes = 65;
    private int dataDelayMinutes = 65;
    private Calendar closeTimeForMod = null;
    private Calendar closeTimeForData = null;
    //  Identifying Attributes.
    //  Some of these won't be used, but I wanted it to contain all possiblilities.
    private String machine;
    private String instance;
    private String appName;
    private String context;
    private String remoteHost;
    private Calendar mtime;
    //	private String hashLookupKey;
    private AccessRecordsForeignKeys accessRecordsForeignKeys;
    // Statistical Data
    private int totalLoads;
    private int averageLoadTime;
    private long totalLoadTime;
    private int maxLoadTime;
    private int minLoadTime;
    private int distinctUsers;
    private int totalUsers;
    private int errorPages;
    private int thirtySecondLoads;
    private int twentySecondLoads;
    private int fifteenSecondLoads;
    private int tenSecondLoads;
    private int fiveSecondLoads;
    private int maxLoadTimeUserID;
    private int maxLoadTimePageID;
    //  Used for 90%, 75%, 50%, 25%
    private Map<Integer, AtomicInteger> tmTimeSliceBuckets = new ConcurrentSkipListMap<Integer, AtomicInteger>();
    private int recordCountTreeMap;
    //  Used only for Reloads
    private int reload90Percentile;
    private int reload75Percentile;
    private int reload50Percentile;
    private int reload25Percentile;

    public TimeSpanEventContainer(String machine, String app, String context, String remoteHost, Calendar time, String instance) {
        accessRecordsForeignKeys = new AccessRecordsForeignKeys(time.getTime());
        this.machine = machine;
        this.instance = instance;
        this.appName = app;
        this.context = context;
        this.remoteHost = remoteHost;
        this.mtime = time;
//		this.hashLookupKey = ""+machine+app+context+remoteHost+time.getTime();
        //tmLoadTimes = new TreeSet(new IntegerComparitor());
        closeTimeForData = GregorianCalendar.getInstance();
        closeTimeForData.setTime(mtime.getTime());
        closeTimeForData.add(Calendar.MINUTE, dataDelayMinutes);

        closeTimeForMod = GregorianCalendar.getInstance();
        closeTimeForMod.setTime(new Date());
        closeTimeForMod.add(Calendar.MINUTE, modDelayMinutes);
    }

    public TimeSpanEventContainer(String machine, String app, String context, String remoteHost, Calendar time, String instance,
                                  int ptotalLoads,
                                  int paverageLoadTime,
                                  long ptotalLoadTime,
                                  int pmaxLoadTime,
                                  int pminLoadTime,
                                  int pdistinctUsers,
                                  int ptotalUsers,
                                  int perrorPages,
                                  int pthirtySecondLoads,
                                  int ptwentySecondLoads,
                                  int pfifteenSecondLoads,
                                  int ptenSecondLoads,
                                  int pfiveSecondLoads,
                                  int pmaxLoadTimeUserID,
                                  int pmaxLoadTimePageID,
                                  int preload90Percentile,
                                  int preload75Percentile,
                                  int preload50Percentile,
                                  int preload25Percentile
    ) {
        this(machine, app, context, remoteHost, time, instance);
        totalLoads = ptotalLoads;
        averageLoadTime = paverageLoadTime;
        totalLoadTime = ptotalLoadTime;
        maxLoadTime = pmaxLoadTime;
        minLoadTime = pminLoadTime;
        distinctUsers = pdistinctUsers;
        totalUsers = ptotalUsers;
        errorPages = perrorPages;
        thirtySecondLoads = pthirtySecondLoads;
        twentySecondLoads = ptwentySecondLoads;
        fifteenSecondLoads = pfifteenSecondLoads;
        tenSecondLoads = ptenSecondLoads;
        fiveSecondLoads = pfiveSecondLoads;
        maxLoadTimeUserID = pmaxLoadTimeUserID;
        maxLoadTimePageID = pmaxLoadTimePageID;
        reload90Percentile = preload90Percentile;
        reload75Percentile = preload75Percentile;
        reload50Percentile = preload50Percentile;
        reload25Percentile = preload25Percentile;
        isReload = true;
        this.setTimesPersisted(1);
    }

    /*synchronized */
    public void tally(int loadtime, boolean firstTimeUser, boolean isErrorPage, int userId, int pageID) {
        tally(loadtime, firstTimeUser, isErrorPage);
        if (loadtime > maxLoadTime) {
            maxLoadTimeUserID = userId;
            maxLoadTimePageID = pageID;
        }
    }

    /*synchronized */
    public void tally(int loadtime, boolean firstTimeUser, boolean isErrorPage) {
        dirty = true;
        databaseDirty = true;
        lastModDate = new java.util.Date();
        closeTimeForMod.setTime(lastModDate);
        closeTimeForMod.add(Calendar.MINUTE, modDelayMinutes);

        ++totalLoads;
        totalLoadTime += loadtime;
        averageLoadTime = (int) (totalLoadTime / totalLoads);
        if (maxLoadTime < loadtime) {
            maxLoadTime = loadtime;
        } else if (minLoadTime > loadtime) {
            minLoadTime = loadtime;
        }
        if (firstTimeUser) {
            ++distinctUsers;
        }
        ++totalUsers;
        if (isErrorPage) {
            ++errorPages;
        }
        if (loadtime >= THIRTY_SECONDS) {
            ++thirtySecondLoads;
            ++twentySecondLoads;
            ++fifteenSecondLoads;
            ++tenSecondLoads;
            ++fiveSecondLoads;
        } else if (loadtime >= TWENTY_SECONDS) {
            ++twentySecondLoads;
            ++fifteenSecondLoads;
            ++tenSecondLoads;
            ++fiveSecondLoads;
        } else if (loadtime >= FIFTEEN_SECONDS) {
            ++fifteenSecondLoads;
            ++tenSecondLoads;
            ++fiveSecondLoads;
        } else if (loadtime >= TEN_SECONDS) {
            ++tenSecondLoads;
            ++fiveSecondLoads;
        } else if (loadtime >= FIVE_SECONDS) {
            ++fiveSecondLoads;
        }
        addRecordToPercentileUnit(loadtime);
    }

    public void addRecordToPercentileUnit(int loadtime) {
        //   Bucket Resolution thoughts:
        //   1000 ms = 1 second.
        //   going for .1 second resolutions could be accomplished by (int)(1000)/(int)(100) = 10
        //   with load times of about 20 seconds, that would create 200 buckets which would be
        //   fairly light in memory, and give the desired resolution.

        int loadTimeBucket = loadtime / MILLISECOND_BUCKET_RESOLUTION;
        Integer integerLoadTimeBucket = new Integer(loadTimeBucket);
        AtomicInteger count = tmTimeSliceBuckets.get(integerLoadTimeBucket);

        if (count != null) {
            count.incrementAndGet();
        } else {
            count = new AtomicInteger(1);
        }
        tmTimeSliceBuckets.put(integerLoadTimeBucket, count);
        ++recordCountTreeMap;
    }

    /**
     * @return
     */
    public String getAppName() {
        return appName;
    }

    public AccessRecordsForeignKeys getAccessRecordsForeignKeys() {
        return accessRecordsForeignKeys;
    }

    /**
     * @return
     */
    public int getAverageLoadTime() {
        return averageLoadTime;
    }

    /**
     * @return
     */
    public String getContext() {
        return context;
    }

    /**
     * @return
     */
    public int getDistinctUsers() {
        return distinctUsers;
    }

    /**
     * @return
     */
    public int getErrorPages() {
        return errorPages;
    }

    /**
     * @return
     */
/*	public String getHashLookupKey() {
		return hashLookupKey;
	}
*/

    /**
     * @return
     */
    public int getFiveSecondLoads() {
        return fiveSecondLoads;
    }

    /**
     * @return
     */
    public String getMachine() {
        return machine;
    }

    /**
     * @return
     */
    public String getInstance() {
        return instance;
    }

    /**
     * @return
     */
    public int getMaxLoadTime() {
        return maxLoadTime;
    }

    /**
     * @return
     */
    public int getMinLoadTime() {
        return minLoadTime;
    }

    /**
     * @return
     */
    public String getRemoteHost() {
        return remoteHost;
    }

    /**
     * @return
     */
    public int getTenSecondLoads() {
        return tenSecondLoads;
    }

    /**
     * @return
     */
    public int getThirtySecondLoads() {
        return thirtySecondLoads;
    }

    /**
     * @return
     */
    public Calendar getTime() {
        return mtime;
    }

    /**
     * @return
     */
    public int getTotalLoads() {
        return totalLoads;
    }

    /**
     * @return
     */
    public int getTotalUsers() {
        return totalUsers;
    }

    /**
     * @return
     */
    public int getTwentySecondLoads() {
        return twentySecondLoads;
    }

    public int getSize() {
        return recordCountTreeMap;
    }

    public int get25Percentile() {
        return getPercentile(25);
    }

    public int get50Percentile() {
        return getPercentile(50);

    }

    public int get75Percentile() {
        return getPercentile(75);

    }

    public int get90Percentile() {
        return getPercentile(90);

    }

    public int getPercentile(int percentile) {
        int elementAtPercentileValue = (int) ((double) recordCountTreeMap * (double) ((double) percentile / (double) MILLISECOND_BUCKET_RESOLUTION));
        if (elementAtPercentileValue == 0) {
            return 0;
        } else {
            int currentValue = 0;
            for (Integer key : tmTimeSliceBuckets.keySet()) {
                AtomicInteger li = tmTimeSliceBuckets.get(key);
                currentValue += li.intValue();
                if (currentValue >= elementAtPercentileValue) {
                    return key.intValue() * MILLISECOND_BUCKET_RESOLUTION;
                }
            }
        }
        return -1;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n\nmachine:").append(machine).append("  :time:").append(fdf.print(mtime.getTime().getTime()));
        return sb.toString();
    }

    public String tooString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n\t\tmachine:.................").append(machine);
        sb.append("\n\t\tappName:.................").append(appName);
        sb.append("\n\t\tcontext:.................").append(context);
        sb.append("\n\t\tremoteHost:..............").append(remoteHost);
        sb.append("\n\t\ttime:....................").append(mtime.toString());
        sb.append("\n\t\ttotalLoads:..............").append(totalLoads);
        sb.append("\n\t\taverageLoadTime:.........").append(averageLoadTime);
        sb.append("\n\t\ttotalLoadTime:...........").append(totalLoadTime);
        sb.append("\n\t\tmaxLoadTime:.............").append(maxLoadTime);
        sb.append("\n\t\tminLoadTime:.............").append(minLoadTime);
        sb.append("\n\t\tdistinctUsers:...........").append(distinctUsers);
        sb.append("\n\t\ttotalUsers:..............").append(totalUsers);
        sb.append("\n\t\terrorPages:..............").append(errorPages);
        sb.append("\n\t\tthirtySecondLoads:.......").append(thirtySecondLoads);
        sb.append("\n\t\ttwentySecondLoads:.......").append(twentySecondLoads);
        sb.append("\n\t\ttenSecondLoads:..........").append(tenSecondLoads);
        sb.append("\n\t\tfiveSecondLoads:.........").append(fiveSecondLoads);
        sb.append("\n\t\t:90th Percentile.........").append(get90Percentile());
        sb.append("\n\t\t:75th Percentile.........").append(get75Percentile());
        sb.append("\n\t\t:50th Percentile.........").append(get50Percentile());
        sb.append("\n\t\t:25th Percentile.........").append(get25Percentile());
        sb.append("\n\t\tSize in bytes............").append(MemoryTool.showSize(this, "DailyEventContainer"));
        return sb.toString();
    }

    /**
     * @return
     */
    public java.util.Date getLastModDate() {
        return lastModDate;
    }

    /**
     * @return
     */
    public int getFifteenSecondLoads() {
        return fifteenSecondLoads;
    }

    /**
     * @return
     */
    public boolean isDatabaseDirty() {
        return databaseDirty;
    }

    /**
     * @param b
     */
    public void setDatabaseDirty(boolean b) {
        databaseDirty = b;
    }

    /**
     * @return
     */
    public long getTotalLoadTime() {
        return totalLoadTime;
    }

    /**
     * @return
     */
    public int getTimesPersisted() {
        return timesPersisted;
    }

    /**
     * @param i
     */
    public void setTimesPersisted(int i) {
        timesPersisted = i;
    }

    /**
     * @return
     */
    public int getPersistOpportunities() {
        return persistOpportunities;
    }

    /**
     * @param i
     */
    public void setPersistOpportunities(int i) {
        persistOpportunities = i;
    }

    /**
     * @return
     */
    public Calendar getCloseTimeForData() {
        return closeTimeForData;
    }

    /**
     * @return
     */
    public Calendar getCloseTimeForMod() {
        return closeTimeForMod;
    }

    /**
     * @param i
     */
    public void setDataDelayMinutes(int i) {
        dataDelayMinutes = i;
    }

    /**
     * @param i
     */
    public void setModDelayMinutes(int i) {
        modDelayMinutes = i;
    }

    /**
     * @return
     */
    public int getMaxLoadTimePageID() {
        return maxLoadTimePageID;
    }

    /**
     * @return
     */
    public int getMaxLoadTimeUserID() {
        return maxLoadTimeUserID;
    }

    private class IntegerComparitor implements Comparator, Serializable {

        /* (non-Javadoc)
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
        public int compare(Object o1, Object o2) {
            Integer i1 = (Integer) o1;
            Integer i2 = (Integer) o2;
            int i = i1.compareTo(i2);
            if (i == 0) {
                return 1;
            }
            return i;
        }
    }

}


