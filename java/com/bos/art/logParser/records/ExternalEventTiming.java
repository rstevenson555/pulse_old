/*

 * Created on Oct 21, 2003

 *

 * To change the template for this generated file go to

 * Window>Preferences>Java>Code Generation>Code and Comments

 */
package com.bos.art.logParser.records;

import com.bos.art.logParser.db.ExternalTimingPersistanceStrategy;
import com.bos.art.logParser.db.PersistanceStrategy;
import org.apache.log4j.Logger;

/**
 *
 * @author I0360D3
 *
 *
 *
 * To change the template for this generated type comment go to
 *
 * Window>Preferences>Java>Code Generation>Code and Comments
 *
 */
public class ExternalEventTiming extends UserRequestTiming {

    private static final Logger logger = (Logger) Logger.getLogger(ExternalEventTiming.class.getName());
    transient private AccessRecordsForeignKeys foreignKeys;
    transient private PersistanceStrategy pStrat;
    private String payLoad;
    private int classification;

    public ExternalEventTiming() {

        super();

        setPriority(10);

    }

    public String getPayLoad() {

        return payLoad;

    }

    public void setPayLoad(String payload) {

        this.payLoad = payload;

    }

    public int getClassification() {

        return classification;

    }

    public void setClassification(int c) {

        classification = c;

    }

    @Override
    public boolean isExternalAccessEvent() {

        return true;

    }

    @Override
    public boolean isAccessRecord() {

        return false;

    }

    @Override
    public String toString() {

        String s = super.toString();

        s += "\nExternalEventTiming: " + ": ";

        s += getPayLoad();

        return s;

    }

    /*
     * (non-Javadoc)
     *
     * @see com.bos.art.logParser.records.LiveLogParserRecord#getRemoteHost()
     *
     */
    @Override
    public String getRemoteHost() {

        // TODO Auto-generated method stub

        return null;

    }

    /*
     * (non-Javadoc)
     *
     * @see com.bos.art.logParser.records.LiveLogParserRecord#isErrorPage()
     *
     */
    @Override
    public boolean isErrorPage() {

        // TODO Auto-generated method stub

        return false;

    }

    /*
     * (non-Javadoc)
     *
     * @see com.bos.art.logParser.records.LiveLogParserRecord#isFirstTimeUser()
     *
     */
    @Override
    public boolean isFirstTimeUser() {

        // TODO Auto-generated method stub

        return false;

    }

    @Override
    public boolean writeToDatabase() {

        logger.info("writeToDatabase Called for ExternalTimingPersistanceStrategy ... " + this.toString());

        if (pStrat == null) {

            pStrat = ExternalTimingPersistanceStrategy.getInstance();

        }

        return pStrat.writeToDatabase(this);

    }
}
