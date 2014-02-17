/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bos.art.logServer.utils;

import org.joda.time.DateTime;
import org.joda.time.Interval;

/**
 *
 * @author i0360b6
 * The constant definitions all express their time in milliseconds
 */
public interface TimeIntervalConstants {
    static final int FIFTEEN_SECONDS_MILLIS = (int)(new Interval(new DateTime(),new DateTime().plusSeconds(15)).toDurationMillis());
    static final int THIRTY_SECONDS_MILLIS = (int)(new Interval(new DateTime(),new DateTime().plusSeconds(30)).toDurationMillis());
    static final int FIVE_SECOND_DELAY = 5;

    static final int TWENTY_SECONDS_MILLIS = (int)(new Interval(new DateTime(),new DateTime().plusSeconds(20)).toDurationMillis());
    static final int TEN_SECONDS_MILLIS = (int)(new Interval(new DateTime(),new DateTime().plusSeconds(10)).toDurationMillis());

    static final long THREE_MINUTES_MILLIS = new Interval(new DateTime(),new DateTime().plusMinutes(3)).toDurationMillis();
    static final long THIRTY_MINUTES_MILLIS = new Interval(new DateTime(),new DateTime().plusMinutes(30)).toDurationMillis();
    static final long TEN_MINUTES_MILLIS = new Interval(new DateTime(),new DateTime().plusMinutes(10)).toDurationMillis();
    static final int TEN_MINUTE_DELAY = 10;

    static final long ONE_HOUR_MILLIS = new Interval(new DateTime(),new DateTime().plusHours(1)).toDurationMillis();
    static final long TWO_HOURS_MILLIS = new Interval(new DateTime(),new DateTime().plusHours(2)).toDurationMillis();
    static final long TWENTYFOUR_HOURS_MILLIS = new Interval(new DateTime(),new DateTime().plusHours(1)).toDurationMillis();
    
    static final long ONE_DAY_MILLIS = new Interval(new DateTime(),new DateTime().plusDays(1)).toDurationMillis();
    static final long SEVEN_DAYS_MILLIS = new Interval(new DateTime(),new DateTime().plusDays(7)).toDurationMillis();
    

}

