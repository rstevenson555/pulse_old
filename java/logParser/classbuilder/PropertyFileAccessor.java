/*
 * PropertyFileAccessor.java
 *
 * Created on May 15, 2002, 10:57 PM
 */

package logParser.classbuilder;

/**
 *
 * @author  Will Houck
 * @version
 */
public class PropertyFileAccessor implements BuilderInterface{

    /** Creates new PropertyFileAccessor */
    public PropertyFileAccessor() {
    }

    public ClassListCollection buildClassListCollection(){ return readPropertyFiles(); }

    private ClassListCollection readPropertyFiles()
    {
      ClassListCollection clc = new ClassListCollection();
      CMP20Data cmp20Data = new CMP20Data(); cmp20Data.loadClasses();
      clc.add(cmp20Data);
      return clc;
    }


}
