/*
 * Created on Oct 22, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package com.bos.art.logParser.statistics;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.TreeSet;


/**
 * @author I0360D3
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class DailyEventContainer implements Serializable, IEventContainer {
	
	private static final String MSIE60_STRING = "MSIE 6.0";
	private static final String MSIE55_STRING = "MSIE 5.5";
	private static final String MSIE50_STRING = "MSIE 5.0";
	private static final String GECKO_STRING = "Mozilla/5.0";
	private static final String NETSCAPE47_STRING = "Mozilla/4.7";

	//  Class Administative Parameters.
	//
	// dirty          - determines whether to recalculate the Percentiles.
	// databaseDirty  - either use insert or update.
	// lastModDate    - Used for determining Closure (Imprtant to prevent Memory Leak
	// isReloaded     - Reloads can't determine Percentiles because the TreeMap is gone.
	// timesPersisted - Count How Many Times this is Persisted.
	private boolean dirty = true;
	private boolean databaseDirty = true;
	private java.util.Date lastModDate;
	private boolean isReload = false;
	private int timesPersisted;
	// persistance Times
	// Modification - 3 hour delay
	// Data  24 hour delay. 
	private int modDelayMinutes       = 3*60;
	private int dataDelayMinutes      = 24*60;
	private Calendar closeTimeForMod     = null;
	private Calendar closeTimeForData    = null;

	//  Object Identity
	private String machine;
	private String appName;
	private String context;
	private String remoteHost;
	private Calendar time;
	private String hashLookupKey;
	
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
	private int msie50;
	private int msie55;
	private int msie60;
	private int gecko;
	private int ns47;
	private int other;
	private int maxLoadTimeUserID;
	private int maxLoadTimePageID;
	//  Used for 90%, 75%, 50%, 25%
	private TreeSet tmLoadTimes;
	private Integer[] arrayLoadTimes;
	//  Used only for Reloads
	private int reload90Percentile;
	private int reload75Percentile;
	private int reload50Percentile;
	private int reload25Percentile;
	
	
	public DailyEventContainer(String machine, String app,String context, String remoteHost, Calendar time){
		
		this.closeTimeForData = GregorianCalendar.getInstance();
		this.closeTimeForData.setTime(new java.util.Date());
		
		this.closeTimeForMod = GregorianCalendar.getInstance();
		this.closeTimeForMod.setTime(new java.util.Date());
		
		this.closeTimeForData.add(Calendar.MINUTE,dataDelayMinutes);
		this.closeTimeForMod.add(Calendar.MINUTE, modDelayMinutes);
		
		
		this.machine = machine;
		this.appName = app;
		this.context = context;
		this.remoteHost = remoteHost;
		this.time = time;
		this.hashLookupKey = ""+machine+app+context+remoteHost+time;
		tmLoadTimes = new TreeSet(new IntegerComparitor());
	}
	
	/*synchronized */public void tally(int loadtime, boolean firstTimeUser, boolean isErrorPage, String browser, int page_id, int user_id){
		
		lastModDate = new java.util.Date();
		closeTimeForMod.setTime(lastModDate);
		closeTimeForMod.add(Calendar.MINUTE,modDelayMinutes);
		
		dirty = true;
		databaseDirty = true;
		++totalLoads;
		totalLoadTime += loadtime;
		averageLoadTime = (int)(totalLoadTime/totalLoads);
		if(maxLoadTime < loadtime){
			maxLoadTime = loadtime;
			maxLoadTimePageID = page_id;
			maxLoadTimeUserID = user_id;
		}else if(minLoadTime > loadtime){
			minLoadTime = loadtime;
		}
		if(firstTimeUser){
			++distinctUsers;
		}
		++totalUsers;
		if(isErrorPage){
			++errorPages;
		}
		if(loadtime >= 30000){
			++thirtySecondLoads;
			++twentySecondLoads;
			++fifteenSecondLoads;
			++tenSecondLoads;
			++fiveSecondLoads;
		}else if(loadtime >=20000){
			++twentySecondLoads;
			++fifteenSecondLoads;
			++tenSecondLoads;
			++fiveSecondLoads;
		}else if(loadtime >= 15000){
			++fifteenSecondLoads;
			++tenSecondLoads;
			++fiveSecondLoads;
		}else if(loadtime >= 10000){
			++tenSecondLoads;
			++fiveSecondLoads;
		}else if(loadtime >= 5000){
			++fiveSecondLoads;
		}
		
		//  Now we will add the browser stuff here.
		if(browser != null){
		if(browser.indexOf(MSIE60_STRING)>0){
			++msie60;
		}else if(browser.indexOf(MSIE55_STRING)>0){
			++msie55;
		}else if(browser.indexOf(MSIE50_STRING)>0){
			++msie50;
		}else if(browser.indexOf(GECKO_STRING)>0){
			++gecko;
		}else if(browser.indexOf(NETSCAPE47_STRING)>0){
			++ns47;
		}else{
			//TODO  Log the Browser type...
			//  logger.
			++other;
		}
		}
		tmLoadTimes.add(new Integer(loadtime));
	}
	public String getHashKey(){
		return hashLookupKey;
	}
	
	/**
	 * @return
	 */
	public String getAppName() {
		return appName;
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
	public int getFiveSecondLoads() {
		return fiveSecondLoads;
	}

	/**
	 * @return
	 */
	public String getHashLookupKey() {
		return hashLookupKey;
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
		return time;
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
	
	public int get25Percentile(){
		//TODO
		return 0;
	}
	
	public int get50Percentile(){
		//TODO
		return 0;
	}
	
	public int get75Percentile(){
		//TODO
		return 0;
	}
	
	public int get90Percentile(){
		//TODO
		return 0;
	}
	
	public String toString(){
		StringBuffer sb = new StringBuffer();
		sb.append("\n\t\tmachine:.................").append(machine);
		sb.append("\n\t\tappName:.................").append(appName);
		sb.append("\n\t\tcontext:.................").append(context);
		sb.append("\n\t\tremoteHost:..............").append(remoteHost);
		sb.append("\n\t\ttime:....................").append(time);
		sb.append("\n\t\thash:....................").append(hashLookupKey);
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
		sb.append("\n\n\n");
		return sb.toString();

	}
	private class IntegerComparitor implements Comparator, Serializable {
		
			/* (non-Javadoc)
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(Object o1, Object o2) {
			Integer i1 = (Integer)o1;
			Integer i2 = (Integer)o2;
			int i = i1.compareTo(i2);
			if(i==0){
				return 1;
			}
			return i;
		}

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
	public int getGecko() {
		return gecko;
	}

	/**
	 * @return
	 */
	public int getMsie50() {
		return msie50;
	}

	/**
	 * @return
	 */
	public int getMsie55() {
		return msie55;
	}

	/**
	 * @return
	 */
	public int getMsie60() {
		return msie60;
	}

	/**
	 * @return
	 */
	public int getNs47() {
		return ns47;
	}

	/**
	 * @return
	 */
	public int getOther() {
		return other;
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

	/* (non-Javadoc)
	 * @see com.bos.art.logParser.statistics.IEventContainer#getCloseTimeForData()
	 */
	public Calendar getCloseTimeForData() {
		return this.closeTimeForData;
	}

	/* (non-Javadoc)
	 * @see com.bos.art.logParser.statistics.IEventContainer#getCloseTimeForMod()
	 */
	public Calendar getCloseTimeForMod() {
		return this.closeTimeForMod;
	}
	
	public int getTimesPersisted(){
		return this.timesPersisted;
	}
}
