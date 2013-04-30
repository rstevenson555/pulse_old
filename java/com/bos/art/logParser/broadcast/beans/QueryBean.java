package com.bos.art.logParser.broadcast.beans;

import java.io.*;

public class QueryBean extends TransferBean implements java.io.Serializable
{
    private String query;
    private byte[] response;
    private String serial;

    public QueryBean() 
    {
    }

    public void setQuery( String query )
    {
        this.query = query;
    }

    public void setResponse(byte[] resp)
    {
        response = resp;
    }

    public byte[] getResponse()
    {
        return response;
    }

    public void setSerial(String sn)
    {
        serial = sn;
    }

    public String getSerial()
    {
        return serial;
    }

    public String getQuery()
    {
        return query;
    }

    public void processBean(org.jgroups.Message msg)
    {
        getClient().process(msg,this);
    }
}
