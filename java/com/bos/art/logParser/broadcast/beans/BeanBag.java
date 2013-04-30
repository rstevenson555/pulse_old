package com.bos.art.logParser.broadcast.beans;

import java.util.Collection;

public class BeanBag extends TransferBean implements java.io.Serializable
{
    private Collection<TransferBean> beans;


    public BeanBag()
    {
    }

    public Collection<TransferBean> getBeans()
    {
        return beans;
    }

    public void setBeans(Collection<TransferBean> beans)
    {
        this.beans = beans;
    }

    public void processBean(org.jgroups.Message msg)
    {
        getClient().process(msg,this);
    }

}
