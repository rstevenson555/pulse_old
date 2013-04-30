package com.bos.art.logParser.broadcast.beans.delegate;

import com.bos.art.logParser.broadcast.beans.*;

public interface AccessRecordsDelegate extends AppDelegate
{
    void didReceiveAccessRecordsBean(org.jgroups.Message msg,TransferBean bean);
}
