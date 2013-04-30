package com.bos.art.logParser.broadcast.beans.delegate;

import com.bos.art.logParser.broadcast.beans.*;

public interface UserDelegate extends AppDelegate
{
    void didReceiveUserBean(org.jgroups.Message msg,TransferBean bean);

    void memberJoined(String name,int port,String localhost);
    void userLeft(String ipaddress);
    void memberLeft(String hostname,int port,String localhost);
    void membersJoined();
    
}

