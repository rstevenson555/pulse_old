package com.bos.art.logParser.broadcast.beans;

import org.jgroups.Message;

abstract public class TransferBean implements java.io.Serializable
{
    transient TransferClient client = null;

    public TransferBean() {}

    public void registerClient(TransferClient client)
    {
        this.client = client;
    }

    public TransferClient getClient()
    {
        return client;
    }

    abstract public void processBean(Message msg);
}
