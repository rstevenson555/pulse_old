/*
 * Created on Nov 18, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package com.bos.art.logParser.statistics;

import java.util.Calendar;

/**
 * @author I0360D3
 *         <p/>
 *         To change the template for this generated type comment go to
 *         Window>Preferences>Java>Code Generation>Code and Comments
 */
public interface IEventContainer {
    /**
     * @return
     */
    public abstract Calendar getTime();

    /**
     * @return
     */
    public abstract boolean isDatabaseDirty();

    /**
     * @param b
     */
    public abstract void setDatabaseDirty(boolean b);

    /**
     * @return
     */
    public abstract Calendar getCloseTimeForData();

    /**
     * @return
     */
    public abstract Calendar getCloseTimeForMod();

    public abstract int getTimesPersisted();
}