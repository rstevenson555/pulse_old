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
    public static final long FIFTEEN_SECONDS = new Interval(new DateTime(),new DateTime().plusSeconds(15)).toDurationMillis();
    public static final long THIRTY_SECONDS = new Interval(new DateTime(),new DateTime().plusSeconds(30)).toDurationMillis();
    
    public static final long THREE_MINUTES = new Interval(new DateTime(),new DateTime().plusMinutes(3)).toDurationMillis();
    public static final long THIRTY_MINUTES = new Interval(new DateTime(),new DateTime().plusMinutes(30)).toDurationMillis();
    public static final long TEN_MINUTES = new Interval(new DateTime(),new DateTime().plusMinutes(10)).toDurationMillis();

    public static final long ONE_HOUR = new Interval(new DateTime(),new DateTime().plusHours(1)).toDurationMillis();
    public static final long TWO_HOURS = new Interval(new DateTime(),new DateTime().plusHours(2)).toDurationMillis();
    public static final long TWENTYFOUR_HOURS = new Interval(new DateTime(),new DateTime().plusHours(1)).toDurationMillis();
    
    public static final long ONE_DAY = new Interval(new DateTime(),new DateTime().plusDays(1)).toDurationMillis();
    public static final long SEVEN_DAYS = new Interval(new DateTime(),new DateTime().plusDays(7)).toDurationMillis();
    
}
