package com.bos.art.logParser.broadcast.beans.delegate;

import com.bos.art.logParser.broadcast.beans.*;

public interface ExternalAccessRecordsDelegate extends AppDelegate
{
    void didReceiveExternalAccessRecordsBean(org.jgroups.Message msg,TransferBean bean);
}
