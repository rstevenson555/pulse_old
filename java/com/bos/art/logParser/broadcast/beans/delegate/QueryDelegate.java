package com.bos.art.logParser.broadcast.beans.delegate;

import com.bos.art.logParser.broadcast.beans.*;

public interface QueryDelegate extends AppDelegate
{
    void didReceiveQueryBean(org.jgroups.Message msg,TransferBean bean);
}

