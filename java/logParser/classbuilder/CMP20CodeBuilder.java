/*
 * CMP20ClassBuilder.java
 *
 * Created on May 15, 2002, 11:03 PM
 */

package logParser.classbuilder;

/**
 *
 * @author  Will Houck
 * @version
 */
public class CMP20CodeBuilder {

    /** Creates new CMP20ClassBuilder */
    public CMP20CodeBuilder() {}

    public static String GetCmpBean(CMP20Data cmpData)
    {
        return "This is where CMP20CodeBuilder.GetCmpBean will build an EJB Bean Class for: "+cmpData.getClassName();
    }

    public static String GetCmpLocalHome(CMP20Data cmpData)
    {
        return "This is where CMP20CodeBuilder.GetCmpLocalHome will build a Local Home Interface for: "+cmpData.getClassName();
    }

}
