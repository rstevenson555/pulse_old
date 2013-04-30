/*
 * ClassBuilderDriver.java
 *
 * Created on May 15, 2002, 7:45 PM
 */

package logParser.classbuilder;

import java.util.ListIterator;

/**
 *
 * @author  Will Houck
 * @version
 */
public class Driver {

    /** Creates new ClassBuilderDriver */
    public Driver() {}

    /**
    * @param args the command line arguments
    */
    public static void main (String args[]){ new Driver().execute(); }

    public void execute()
    {
      BuilderInterface bi = new BuilderProxy().getBuilder();
      ClassListCollection clc = bi.buildClassListCollection();  //Get a collection of ClassList objects
      processClasses(clc);
    }


    private void processClasses(ClassListCollection clc)
    {
      ListIterator clcIter = clc.listIterator(0);

      while(clcIter.hasNext()){
        ClassList classList = (ClassList)clcIter.next();
        for(int i = 0; i < classList.size(); i++){
          writeClassToDisk(classList.getPackageName(), classList.getClassName(), (String)classList.get(i));
          /*Call a method here that adds xml for this bean to various descriptor files.
            The xml will also come from the objects in our collection.  Do not write the
            descriptors to disk in the loop.  We want them to have xml for all ejb's
            in the ClassListCollection.*/
        }
      }
      //Write the descriptor files to disk.
    }


    private void writeClassToDisk(String packageName, String className, String classCode)
    {
      System.out.println("A Directory Structure will be created for "+packageName);
      System.out.println("A java source file will be created for "+ className);
      System.out.println(classCode);
      System.out.println("\n");
    }
}
