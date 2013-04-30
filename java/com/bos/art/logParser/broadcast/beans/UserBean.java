package com.bos.art.logParser.broadcast.beans;

import java.io.*;

public class UserBean extends TransferBean implements Serializable {
    private String ipaddress;
    private String name;

    public UserBean()
    {
        ipaddress = "";
        name = "";
    }

    public void setIpAddress(String string)
    {
        ipaddress = string;
    }

    public String getIpAddress()
    {
        return ipaddress;
    }

    public void setName(String string)
    {
        name = string;
    }

    public String getName()
    {
        return name;
    }

    public void processBean(org.jgroups.Message msg)
    {
        getClient().process(msg,this);
    }
    
}
