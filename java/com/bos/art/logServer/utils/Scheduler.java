package com.bos.art.logServer.utils;
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import EDU.oswego.cs.dl.util.concurrent.ClockDaemon;
import EDU.oswego.cs.dl.util.concurrent.ThreadFactory;
import java.util.Calendar;
import java.util.Date;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class is just a simple wrapper around the ClockDeamon object in the concurrent library
 * the purpose is to simplify re-use through the getInstance by name method
 * also this scheduler will allow us to control the name and priority used when creating the 
 * background thread that processes these jobs
 **/
public class Scheduler extends ClockDaemon
{
    private static final String DEFAULTSCHEDULERNAME="BOSDefaultScheduler";
    private static ConcurrentHashMap schedulerMap = new ConcurrentHashMap();

    /** Default thread factory. Creates worker threads. */
    private static final ThreadFactory FACTORY = new ThreadFactory() {
        public Thread newThread(Runnable command) {
            Thread t = new Thread(command, "LPScheduler");
            t.setPriority(Thread.MIN_PRIORITY+1);

            return t;
        }
    };

    
    /**
     * Just call the default constructor
     **/
    private Scheduler(String name,final int threadpriority)
    {
        super();
        setThreadFactory( new ThreadFactory() {
            public Thread newThread(Runnable command) {
                Thread t = new Thread(command, "LPScheduler");
                t.setPriority(threadpriority);

                return t;
            }
        });
        schedulerMap.put( name, this );
    }

    /**
     * returns the default scheduler instance if no name is specified
     **/
    synchronized public static Scheduler getInstance()
    {
        return getInstance(DEFAULTSCHEDULERNAME,Thread.MIN_PRIORITY+1);
    }

    /**
     * returns the named scheduler, if one doesn't exist we create it
     **/
    synchronized public static Scheduler getInstance(String name)
    {
        return getInstance(name,Thread.MIN_PRIORITY+1);
    }

    /**
     * returns the named scheduler, if one doesn't exist we create it
     **/
    synchronized public static Scheduler getInstance(String name,int threadpriority)
    {
        Scheduler scheduler = null;
        if ( (scheduler = (Scheduler)schedulerMap.get( name ))!=null)
            return scheduler;
        else {
            scheduler = new Scheduler( name, threadpriority );
            return scheduler;
        }
    }

    /**
     * schedule a task to run at the given time
     * @return taskID -- an opaque reference that can be used to cancel execution request
     **/
    public Object schedule(Runnable task, Date time) 
    {
        return super.executeAt( time, task );
    }

    /**
     * schedule a task to run after the given delay
     * @return taskID -- an opaque reference that can be used to cancel execution request
     **/
    public Object schedule(Runnable task, long delay) 
    {
        return super.executeAfterDelay( delay, task );
    }

    /**
     * task - the task to schedule, 
     * firstTime - the first time for the task to run
     * period - the time between run's
     * @return taskID -- an opaque reference that can be used to cancel execution request
     **/
    public Object scheduleAtFixedRate(Runnable task, Date firstTime, long period)
    {
        Date now = Calendar.getInstance().getTime();
        if ( now.getTime() >= firstTime.getTime()) {
            return super.executePeriodically(period,task,true);
        } else {
            Scheduler.DelayedStartTask ds = new Scheduler.DelayedStartTask(task,period);
            Object ftask = super.executeAt(firstTime,ds);
            return ftask;
        }
    }

/**
     * task - the task to schedule, 
     * firstTime - the first time for the task to run
     * period - the time between run's
     * @return taskID -- an opaque reference that can be used to cancel execution request
     **/
    public Object scheduleAtFixedRate(Runnable task, long firstTime, long period)
    {
        Date now = Calendar.getInstance().getTime();
        if ( now.getTime() >= firstTime) {
            return super.executePeriodically(period,task,true);
        } else {            
            Scheduler.DelayedStartTask ds = new Scheduler.DelayedStartTask(task,period);            
            Object ftask = super.executeAt(new Date(firstTime),ds);
            return ftask;
        }
    }
       

    /**
     * delayed start task, this task starts first for a delay startup
     */
    class DelayedStartTask extends TimerTask {
        private Runnable task = null;
        private long period;
        
        DelayedStartTask(Runnable task,long period ) {
            this.task = task;
            this.period = period;
        }
        public void run()
        {
            // now execute this first delayed start task
            task.run();
            // now schedule the next run
            executePeriodically( period, task, false );
                        
        }
    }

    /**
     * cancels the timer object
     **/
    public void cancel()
    {
        super.shutDown();
    }
}

