/*

 * Created on Oct 29, 2003

 *

 * To change the template for this generated file go to

 * Window>Preferences>Java>Code Generation>Code and Comments

 */

package com.bos.art.logParser.records;


import java.io.Serializable;


/**
 * @author I0360D3
 *         <p/>
 *         <p/>
 *         <p/>
 *         To change the template for this generated type comment go to
 *         <p/>
 *         Window>Preferences>Java>Code Generation>Code and Comments
 */

public class AccessRecordsForeignKeys implements Serializable {

    public int fkContextID = 0;
    public int fkUserID = 0;
    public int fkSessionID = 0;
    public int fkMachineID = 0;
    public int fkInstanceID = 0;
    public int fkPageID = 0;
    public int fkAppID = 0;
    public int fkBranchTagID = 0;
    public int fkQueryParameterID = 0;
    public java.util.Date eventTime;

    public AccessRecordsForeignKeys(java.util.Date date) {

        eventTime = date;

    }

    /**
     * Don't create one of these with this method...
     */

    private AccessRecordsForeignKeys() {


    }


}

