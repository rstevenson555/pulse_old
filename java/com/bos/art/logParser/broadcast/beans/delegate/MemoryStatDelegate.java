package com.bos.art.logParser.broadcast.beans.delegate;

import com.bos.art.logParser.broadcast.beans.*;

public interface MemoryStatDelegate extends AppDelegate
{
    void didReceiveMemoryStatBean(org.jgroups.Message msg,TransferBean bean);
}

