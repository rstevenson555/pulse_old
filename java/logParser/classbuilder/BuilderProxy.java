/*
 * ClassMetaDataProxy.java
 *
 * Created on May 15, 2002, 7:57 PM
 */

package logParser.classbuilder;

/**
 *
 * @author  Will Houck
 * @version
 */
public class BuilderProxy
{

    /** Creates new ClassMetaDataProxy */
    public BuilderProxy() {}

    public BuilderInterface getBuilder()
    {
      return new PropertyFileAccessor();
    }
}
