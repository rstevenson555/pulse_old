package com.bos.art.logParser.broadcast.beans;

import org.jgroups.Message;

public interface TransferClient 
{
    public void process(Message msg,BeanBag bag);
    public void process(Message msg,HistoryBean hb);
    public void process(Message msg,UserBean bean);
    public void process(Message msg,ChatBean bean);
    public void process(Message msg,QueryBean bean);
    public void process(Message msg,AccessRecordsMinuteBean obj);
    public void process(Message msg,MemoryStatBean bean);
    public void process(Message msg,ExternalAccessRecordsMinuteBean obj);
    public void process(Message msg,SessionDataBean obj);
    public void process(Message msg,BrowserBean obj);
    public void process(Message msg,ErrorStatBean obj);
    
}
