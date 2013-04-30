package com.bos.art.logParser.broadcast.beans;

public class ChatBean extends TransferBean
{
    private String message;

    public ChatBean() 
    {
    }

    public void setMessage(String msg)
    {
        message = msg;
    }

    public String getMessage()
    {
        return message;
    }

    public void processBean(org.jgroups.Message msg)
    {
        getClient().process(msg,this);
    }
}
