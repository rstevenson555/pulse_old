/*
 * Created on Oct 22, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package com.bos.art.logParser.statistics;


import org.apache.log4j.Logger;

import com.bos.art.logParser.records.ILiveLogParserRecord;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * @author I0360D3
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class ExternalAccessHourlyStats extends StatisticsUnit {

	private static final Logger logger = (Logger)Logger.getLogger(ExternalAccessHourlyStats.class.getName());
	private static ExternalAccessHourlyStats instance;
    private static DateTimeFormatter sdf = DateTimeFormat.forPattern("yyyyMMddHH"); 

	private Map<String,TimeSpanEventContainer> hours;
	private int calls;
	private int eventsProcessed;
	private int timeSlices;
    
	public  ExternalAccessHourlyStats(){
		hours = new ConcurrentHashMap<String,TimeSpanEventContainer>();
	}
	public static ExternalAccessHourlyStats getInstance(){
		if(instance == null){
			instance = new ExternalAccessHourlyStats();
		}
		return instance;
	}
	
	public void setInstance(StatisticsUnit su){
		if(su instanceof ExternalAccessHourlyStats){
            if (instance!=null) {
                instance.runnable=false;
            }
			instance = (ExternalAccessHourlyStats)su;
		}
	}

	
	/* (non-Javadoc)
	 * @see com.bos.art.logParser.statistics.StatisticsUnit#processRecord(com.bos.art.logParser.records.LiveLogParserRecord)
	 */
	public void processRecord(ILiveLogParserRecord record) {
		++calls;
		if ( record.isExternalAccessEvent() ) {
			TimeSpanEventContainer container = getTimeSpanEventContainer(record);
			container.tally(record.getLoadTime(),record.isFirstTimeUser(), record.isErrorPage());
			++eventsProcessed;
		}
		if(calls%50000 == 0){
			logger.debug(this.toString());
		}
		return;
	}
	
	synchronized private TimeSpanEventContainer getTimeSpanEventContainer(ILiveLogParserRecord record){
        String key = sdf.print(record.getEventTime().getTime().getTime());
		TimeSpanEventContainer container = (TimeSpanEventContainer)hours.get(key);
		if(container == null){
			++timeSlices;
			container = new TimeSpanEventContainer(record.getServerName(),record.getAppName(),record.getContext(),record.getRemoteHost(),record.getEventTime(),record.getInstance());
			hours.put(key,container);
		}
		return container;
	}
	
	
    @Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append(calls).append(":").append(eventsProcessed).append(":").append(timeSlices).append("\n");
		sb.append(hours.toString());
		return sb.toString();
	}
	
	

	/* (non-Javadoc)
	 * @see com.bos.art.logParser.statistics.StatisticsUnit#persistData()
	 */
	public void persistData() {
		// TODO Auto-generated method stub
		if (logger.isDebugEnabled()) {
			logger.debug(
				"persistData() called: "
					+ Thread.currentThread().getName()
					+ ": next Write Date :"
					+ "Not/applicable"
					+ " :current date:"
					+ new java.util.Date());
		}

	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
    @Override
	public void run() {
		persistData();
	}
	
	public void flush(){
	}

}

