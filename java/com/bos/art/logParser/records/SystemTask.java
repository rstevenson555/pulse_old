package com.bos.art.logParser.records;

public class SystemTask implements java.io.Serializable, ILiveLogPriorityQueueMessage {
    private String task;
    private java.util.Calendar now;

    public void setTask(String string)
    {
        task = string;
        now = java.util.Calendar.getInstance();
        System.out.println("task set: " + task);
    }
    
    public String getTask()
    {
        return task;
    }

    public java.util.Calendar getEventTime()
    {
        return now;
    }
    
    public int getPriority()
    {
        return 5;
    }
}
