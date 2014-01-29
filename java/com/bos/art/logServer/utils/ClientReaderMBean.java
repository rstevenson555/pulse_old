package com.bos.art.logServer.utils;

import com.bos.art.logServer.Queues.MessageUnloaderMBean;

/**
 * Created with IntelliJ IDEA.
 * User: i0360b6
 * Date: 1/29/14
 * Time: 9:10 AM
 * To change this template use File | Settings | File Templates.
 */
public interface ClientReaderMBean  {
    long getMessagesPerSecond();
}
