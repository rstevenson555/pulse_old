package com.bos.art.logParser.records;

import java.io.Serializable;
import java.util.Calendar;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class UserRequestEventDesc implements Serializable, Comparable {

    private String type;
    private int priority;
    private String eventId;
    private String appName;
    private String branchName;
    private String serverName;
    private String className;
    private String context;
    private String instance = "no_instance";
    transient private String date;
    transient private String time;
    private Calendar eventTime;
    private static DateTimeFormatter sdf = DateTimeFormat.forPattern("MM/dd/yyyy, hh:mm:ss aa"); 
    transient private UserRequestTiming timing;
    transient private ExternalEventTiming externalTiming;
    transient private AccumulatorEventTiming accumulator;
    transient private ConfigMessage configMessage;
    transient private PageRecordEvent pageRecordEvent;
    transient private ExceptionRecordEvent exceptionRecordEvent;
    
    public void clear() {
        date = null;
        time = null;
        timing = null;
        externalTiming = null;
        accumulator = null;
        configMessage = null;
        exceptionRecordEvent = null;
    }
    

    public UserRequestEventDesc() {
        //System.out.println("constructor called");
        priority = 40;
    }

    public void setTiming(UserRequestTiming timing) {
        this.timing = timing;
        //System.out.println("in userRequestEventDesc settiming");
    }

    public void setPayLoadReference(ExternalEventTiming timing) {
        this.externalTiming = timing;
        //System.out.println("setting externalTiming");
    }

    public void setAccumulatorReference(AccumulatorEventTiming accumulator) {
        this.accumulator = accumulator;
        //System.out.println("setting externalTiming");
    }

    public void setExceptionRecordEvent(ExceptionRecordEvent ere) {
        exceptionRecordEvent = ere;
    }

    public void setHtmlPageRecordReference(PageRecordEvent pre) {
        this.pageRecordEvent = pre;
    }

    public void setConfigMessage(ConfigMessage msg) {
        this.configMessage = msg;
    }

    public PageRecordEvent retrievePageRecordEvent() {
        return this.pageRecordEvent;
    }

    public ConfigMessage retrieveConfigMessage() {
        return this.configMessage;
    }

    public ExternalEventTiming retrieveExternalTiming() {
        return externalTiming;
    }

    public AccumulatorEventTiming retrieveArtAccumulator() {
        return accumulator;
    }

    public ExceptionRecordEvent retrieveExceptionRecordEvent() {
        return exceptionRecordEvent;
    }

    public UserRequestTiming retrieveUserRequestTiming() {
        return timing;
    }

    public static void main(String[] args) {
        int val = 98;      // 01100010
        // 64 + 32 + 2
        // 00001000 = 8
        // 00001010
        System.out.println("shife: " + (val));
        System.out.println(" " + ((val >> 0xf) & 0xff));
        String date = "12132003";
        String time = "093923";
        long longdate = Long.valueOf(date + time).longValue();
        long mask = 0xffffffffffffffL;
        long second = longdate & 0xffffffffffff00L;
        long minute = (longdate >> 0xff) & 0xffffffffffff00L;
        long hour = (longdate >> 0xffff) & 0xffffffffffff00L;
        long year = (longdate >> 0xffffff) & 0xffffffffffffL;
    }

    public void copyFrom(UserRequestEventDesc desc) {
        type = desc.type;
        if (priority == -1) {
            priority = desc.priority;
        }
        eventId = desc.eventId;
        appName = desc.appName;
        branchName = desc.branchName;
        serverName = desc.serverName;
        className = desc.className;
        instance = desc.instance;
        if (date == null) {
            date = desc.date;
        }
        if (time == null) {
            time = desc.time;
        }
        // don't overwrite context from the base class, because context
        // is set in the UserRequestTiming object (derived from the page-name), which has a context
        if (context == null) {
            context = desc.context;
        }
        if (eventTime == null) {

            try {                           
                DateTime dt = sdf.parseDateTime(date + ", " + time);
                eventTime = Calendar.getInstance();
                //eventTime.setTime(dt.toDate());
                eventTime.setTime(dt.toDate());

            } catch (IllegalArgumentException nfe) {
                //if we can't parse the message comming down the wire then just set the start date to today's date.
                eventTime = Calendar.getInstance();
                System.out.println("numberFormat copyfrom date, time " + date + ", " + time);
            }

        }
    }

    // temporarily store the timing event, while digester is parsing
    // it's transient so it does not get persisted here
    public void setTimingEvent(UserRequestTiming timing) {
        this.timing = timing;
    }

    public UserRequestTiming retrieveTimingEvent() {
        return (UserRequestTiming) timing;
    }

    /**
     * @param date
     */
    public void setEventTime(Calendar date) {
        eventTime = date;
    }

    /**
     * @return
     */
    public Calendar getEventTime() {
        return eventTime;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String cn) {
        this.className = cn;
    }

    /**
     * @return
     */
    public String getServerName() {
        return serverName;
    }

    public String getInstance() {
        return instance;
    }
    
    /**
     * @return
     */
    public String getType() {
        return type;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String string) {
        time = string;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String string) {
        date = string;
    }

    /**
     * @param string
     */
    public void setAppName(String string) {
        appName = string;
    }

    /**
     * @param string
     */
    public void setBranchName(String string) {
        branchName = string;
    }

    public void setInstance(String string) {
        instance = string;
    }

    /**
     * @param string
     */
    public void setServerName(String string) {
        int pos = string.indexOf("/");
        if (pos < 1) {
            //
            //System.out.println("ServerName w/o slash setting pos to string.length() " + string);
            try {
                //System.out.println("ServerName w/o slash setting pos to string.length() " + this.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
            pos = string.length();
        }
        serverName = string.substring(0, pos);
    }

    /**
     * @param string
     */
    public void setEventId(String string) {
        eventId = string;
    }

    public String getEventId() {
        return eventId;
    }

    /**
     * @param string
     */
    public void setType(String string) {
        type = string;
    }

    /**
     * @return
     */
    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    /**
     * @return
     */
    public String getAppName() {
        return appName;
    }

    /**
     * @return
     */
    public String getBranchName() {
        return branchName;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\nUserRequestEventDesc: ").append(type).append(" ").append(priority).append(" ").append(eventId).append(" ").append(appName).append(" ").append(branchName).append(" ").append(instance).append(" ").append(serverName);
        sb.append(" ").append(context).append(" ").append(eventTime).append(" ").append(date).append(" ").append(time);
        return sb.toString();
    }

    /**
     * @param string
     */
    public void setContext(String string) {
        context = string;
    }

    /**
     * @return
     */
    public String getContext() {
        return context;
    }

    public void setFileName(String string) {
        configMessage.setFileName(string);
    }

    public void setMaxSize(int size) {
        configMessage.setMaxSize(size);
    }

    public void setMaxAge(int age) {
        configMessage.setMaxAge(age);
    }

    /*
     * (non-Javadoc) @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object o) {
        if (o instanceof UserRequestEventDesc) {
            UserRequestEventDesc that = (UserRequestEventDesc) o;
            if (this.getPriority() < that.getPriority()) {
                return -1;
            } else if (this.getPriority() > that.getPriority()) {
                return 1;
            } else {
                int dateCompare = this.getEventTime().getTime().compareTo(that.getEventTime().getTime());
                if (dateCompare != 0) {
                    return dateCompare;
                } else {
                    return -1;
                }
            }
        }
        return -1;
    }

    /**
     * Replace String in string with newStr String.
     *
     * @param str Input String to be altered
     * @param oldString Character to be replaced
     * @param newStr String to replace character (oldChar) with
     *
     * @return String with characters replaced.
     */
    public static String replace(String str, String pattern, String replace) {
        int s = 0;
        int e = 0;
        StringBuilder result = new StringBuilder();

        while ((e = str.indexOf(pattern, s)) >= 0) {
            result.append(str.substring(s, e));
            result.append(replace);
            s = e + pattern.length();
        }

        result.append(str.substring(s));
        return result.toString();

    }
}
