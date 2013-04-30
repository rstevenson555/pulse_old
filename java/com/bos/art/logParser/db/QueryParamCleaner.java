/*
 * Created on Aug 23, 2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package com.bos.art.logParser.db;

import com.bos.art.logServer.utils.TimeIntervalConstants;
import java.util.Iterator;
import java.util.Map;
import org.apache.log4j.Logger;
import static com.bos.art.logServer.utils.TimeIntervalConstants.*;

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
public class QueryParamCleaner extends Thread {

    private static final Logger logger = (Logger) Logger.getLogger(QueryParamCleaner.class.getName());
    private static int count = 1;
    public static boolean shouldContinue = true;

    public QueryParamCleaner() {
        this.setName("QueryParamCleaner No. " + count++);
    }

    public void run() {
        while (shouldContinue) {
            Map<String, ForeignKeyStore.QueryParamClass> tm = ForeignKeyStore.getInstance().getQueryParametersTree();
            int removed = 0;
            int removed24 = 0;
            if (tm == null) {
                try {
                    Thread.sleep(THIRTY_MINUTES);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                continue;
            }

            try {
                long timeCheck = System.currentTimeMillis() - ONE_HOUR;
                long timeCheck24 = System.currentTimeMillis() - TWENTYFOUR_HOURS;
                
                for (String key : tm.keySet()) {
                    if ( tm.get(key) instanceof ForeignKeyStore.QueryParamClass) {
                        ForeignKeyStore.QueryParamClass qpc = tm.get(key);
                        if (qpc.touchCount < 50 && qpc.lastTouchDate.getTime() < timeCheck) {
                            tm.remove(key);
                            ++removed;
                        } else if (qpc.lastTouchDate.getTime() < timeCheck24) {
                            tm.remove(key);
                            ++removed24;
                        }
                    } else {
                        logger.error("QueryParamCleaner object not a QueryParamClass " + tm.get(key));
                    }
                }
                logger.warn("QueryParamCleaner Removed         : " + removed);
                logger.warn("QueryParamCleaner Removed24 Hour  : " + removed24);
                try {
                    Thread.sleep(TimeIntervalConstants.TEN_MINUTES);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                logger.error("QueryParamCleaner serious error: " + e);
            }
        }
    }
}
