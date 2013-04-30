package com.bos.art.logParser.broadcast.beans.delegate;

import com.bos.art.logParser.broadcast.beans.*;

public interface SessionDataDelegate extends AppDelegate
{
    void didReceiveSessionDataBean(org.jgroups.Message msg,TransferBean bean);
}

