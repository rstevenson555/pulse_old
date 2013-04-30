/*
 * Created on Oct 24, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package com.bos.art.logParser.statistics;

import org.apache.log4j.Logger;

import com.bos.art.logParser.records.ILiveLogParserRecord;

/**
 * @author I0360D3
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class RecordPersistor extends StatisticsUnit {
	private static final Logger logger = (Logger)Logger.getLogger(RecordPersistor.class.getName());

	private static RecordPersistor instance;
	
	public static RecordPersistor getInstance(){
		if(instance == null){
			instance = new RecordPersistor();
		}
		return instance;
	}
	public void setInstance(StatisticsUnit su){
		if(su instanceof RecordPersistor){
            if (instance!=null) {
                runnable =false;
            }
			RecordPersistor.instance = (RecordPersistor)su;
		}
	}
	
	public RecordPersistor(){
		
	}
	
	/* (non-Javadoc)
	 * @see com.bos.art.logParser.statistics.StatisticsUnit#processRecord(com.bos.art.logParser.records.LiveLogParserRecord)
	 */
	public void processRecord(ILiveLogParserRecord record) {
		if(logger.isDebugEnabled()){
			logger.debug("RecordPersistor Called : " + record.toString());
		}
		record.writeToDatabase();
	}

	/* (non-Javadoc)
	 * @see com.bos.art.logParser.statistics.StatisticsUnit#persistData()
	 */
	public void persistData() {

	}
	public void flush(){
	}


}
