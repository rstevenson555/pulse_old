/*
 * Created on Oct 22, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package com.bos.art.logParser.statistics;

import com.bos.art.logParser.records.ILiveLogParserRecord;

/**
 * @author I0360D3
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class EchoStatisticsUnit extends StatisticsUnit {

	private static EchoStatisticsUnit instance;
	public  EchoStatisticsUnit (){
	}
	public static EchoStatisticsUnit getInstance(){
		if(instance == null){
			instance = new EchoStatisticsUnit();
		}
		return instance;
	}
	/* (non-Javadoc)s
	 * @see com.bos.art.logParser.statistics.StatisticsUnit#processRecord(com.bos.art.logParser.records.LiveLogParserRecord)
	 */
	public void processRecord(ILiveLogParserRecord record) {
		System.out.println(record.toString());
	}
	
	public void setInstance(StatisticsUnit su){
		if(su instanceof EchoStatisticsUnit){
            if (instance!=null) {
                instance.runnable=false;
            }
			instance = (EchoStatisticsUnit)su;
		}
	}


	/* (non-Javadoc)
	 * @see com.bos.art.logParser.statistics.StatisticsUnit#persistData()
	 */
	public void persistData() {
		// TODO Auto-generated method stub

	}
	public void flush(){
	}

}
