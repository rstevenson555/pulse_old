package com.bos.art.logServer.utils;

import org.joda.time.DateTime;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created with IntelliJ IDEA.
 * User: i0360b6
 * Date: 1/29/14
 * Time: 8:45 AM
 * To change this template use File | Settings | File Templates.
 */
public class TPSCalculator {
    private AtomicLong lifeTimeCount;

    public TPSCalculator() {
        lifeTimeCount = new AtomicLong(0);
    }

    private class CalcRecord {
        AtomicInteger periodCount;
        DateTime periodStart;

        CalcRecord() {
            periodStart = new DateTime();
            periodCount = new AtomicInteger(0);
        }
    }

    private Map<Integer,CalcRecord> tpsRecordMap = new LinkedHashMap<Integer,CalcRecord>()
    {
        protected boolean removeEldestEntry(Map.Entry eldest) {
            return size() > 1;
        }
    };

    /**
     * increment the transaction record for the current minute
     */
    public long incrementTransaction() {
        DateTime now = new DateTime();
        int minuteOfHour = now.getMinuteOfHour();

        CalcRecord calcRecord = tpsRecordMap.get(minuteOfHour);
        if ( calcRecord == null) {
            calcRecord = new CalcRecord();
            tpsRecordMap.put(minuteOfHour, calcRecord);
        }
        calcRecord.periodCount.incrementAndGet();
        return lifeTimeCount.incrementAndGet();
    }

    /**
     * do the tps calc
     * @return
     */
    public long getMessagesPerSecond() {
        DateTime now = new DateTime();
        CalcRecord calcRecord = tpsRecordMap.get(now.getMinuteOfHour());
        return calcRecord.periodCount.get() / ((now.toDateTime().getMillis() - calcRecord.periodStart.toDateTime().getMillis())/1000);
    }

    public long getTransactionCount() {
        return lifeTimeCount.get();
    }
}
