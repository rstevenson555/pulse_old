package com.bos.art.logParser.statistics;

import com.bos.art.logParser.broadcast.beans.ErrorStatBean;
import com.bos.art.logParser.broadcast.network.CommunicationChannel;
import com.bos.art.logParser.records.AccumulatorEventTiming;
import com.bos.art.logParser.records.ILiveLogParserRecord;
import org.apache.log4j.Logger;

/**
 * This class will just critical error events 
 * back to the client.
 * 
 *
 * TODO To change the template for this generated type comment go to

 * Window - Preferences - Java - Code Style - Code Templates
 */
public class CriticalErrorBroadcastStatisticsUnit extends StatisticsUnit {

	private static final Logger logger = (Logger)Logger.getLogger(CriticalErrorBroadcastStatisticsUnit.class.getName());
	/**
	 * if the record is a memory stat, then broad cast to the java groups...
	 */
	public void processRecord(ILiveLogParserRecord record) {
		if( record.isAccumulatorEvent() ) {
    		AccumulatorEventTiming event = (AccumulatorEventTiming)record;
            switch(event.getClassification()) {
                case 8016:
                case 8018:
                    broadcast(event);
            }    		
		}
	}
	
	private void broadcast(AccumulatorEventTiming event) {
		ErrorStatBean bean = new ErrorStatBean(event);
        try {
        	//logger.error("Sending the bean down the channel " + bean.toString());
		    CommunicationChannel.getInstance().broadcast( bean, null);
		    //logger.error("bean sent down the channel........");
        }
        catch(Exception e)
        {
            logger.error("Error broadcasting data",e);
        }
	}

	public void persistData() {
		//  These stats are not persisted...
		
	}

	public void flush() {
		// Since we don't persist, we don't need to flush()...
	}

	public void setInstance(StatisticsUnit su) {
		//  Got to think about this one...........
	}

}

