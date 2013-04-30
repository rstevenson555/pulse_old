
package com.bos.art.logParser.broadcast.history;

public abstract class QueryBroadcast implements Runnable {


    public void processRequest(){
        Thread t = new Thread(this);
        t.start();
    }
}
