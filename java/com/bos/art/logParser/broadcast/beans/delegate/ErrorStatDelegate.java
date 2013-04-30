package com.bos.art.logParser.broadcast.beans.delegate;

import com.bos.art.logParser.broadcast.beans.*;

public interface ErrorStatDelegate extends AppDelegate
{
    void didReceiveErrorStatBean(org.jgroups.Message msg,TransferBean bean);
}

