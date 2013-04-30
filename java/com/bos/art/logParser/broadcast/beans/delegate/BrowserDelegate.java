package com.bos.art.logParser.broadcast.beans.delegate;

import com.bos.art.logParser.broadcast.beans.*;

public interface BrowserDelegate extends AppDelegate
{
    void didReceiveBrowserBean(org.jgroups.Message msg,TransferBean bean);
}

