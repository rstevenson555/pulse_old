/*
 * Created on Mar 30, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

package com.bos.art.logParser.statistics;

import com.bos.art.logParser.broadcast.beans.MemoryStatBean;
import com.bos.art.logParser.broadcast.network.CommunicationChannel;
import com.bos.art.logParser.records.AccumulatorEventTiming;
import com.bos.art.logParser.records.ILiveLogParserRecord;
import org.apache.log4j.Logger;

/**
 * This class will just broadcast memory events 
 * back to the client.
 * 
 * @author I0360D3
 *
 * TODO To change the template for this generated type comment go to

 * Window - Preferences - Java - Code Style - Code Templates
 */
public class JVMStatBroadcastStatisticsUnit extends StatisticsUnit {

	private static final Logger logger = (Logger)Logger.getLogger(JVMStatBroadcastStatisticsUnit.class.getName());
	/**
	 * if the record is a memory stat, then broad cast to the java groups...
	 */
	public void processRecord(ILiveLogParserRecord record) {
		if( record.isAccumulatorEvent() ) {
    		AccumulatorEventTiming event = (AccumulatorEventTiming)record;
            switch(event.getClassification()) {
                case 5001:
                case 5002:
                case 5005:
                case 5006:
                    broadcast(event);
            }    		
		}
	}
	
	private void broadcast(AccumulatorEventTiming event) {
		MemoryStatBean bean = new MemoryStatBean(event);
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
