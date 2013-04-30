package com.bos.art.logParser.broadcast.beans.delegate;

import com.bos.art.logParser.broadcast.beans.*;

public interface ChatDelegate extends AppDelegate
{
    void didReceiveChatBean(org.jgroups.Message msg,TransferBean bean);
}

