package com.bos.art.logParser.broadcast.beans.delegate;

import com.bos.art.logParser.broadcast.beans.*;

public interface BeanBagDelegate extends AppDelegate
{
    void didReceiveBeanBagBean(org.jgroups.Message msg,TransferBean bean);
}

