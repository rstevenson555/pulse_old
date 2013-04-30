/*
 * CMP20Data.java
 *
 * Created on May 15, 2002, 8:01 PM
 */

package logParser.classbuilder;

/**
 *
 * @author  Will Houck
 * @version
 */
public class CMP20Data extends ClassList
{

    /** Creates new CMP20Data */
    public CMP20Data() {}

    public void loadClasses()
    {
      addClass(CMP20CodeBuilder.GetCmpBean(this));
      addClass(CMP20CodeBuilder.GetCmpLocalHome(this));
    }
}
