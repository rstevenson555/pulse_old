/*
 * EJBData.java
 *
 * Created on May 15, 2002, 8:02 PM
 */

package logParser.classbuilder;

import java.util.LinkedList;
/**
 *
 * @author  Will Houck
 * @version
 */
public class ClassList extends LinkedList
{

    /** Creates new EJBData */
    public ClassList() {}


    public String getPackageName(){ return "default1.default2.default3";}
    public String getClassName(){ return "MyDefaultClass";}

    public boolean addClass(String sClass){ return add(sClass); }
}
