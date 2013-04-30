package com.bos.model;

/**
 * @author <a href="http://boss.bekk.no/boss/middlegen/">Middlegen</a>
 *
 * @jdo.bean
 *    identity-type="datastore"
 * @jdo.rdbmapping table="parent"
 *
 * @jdo.finder todo...
 */
public class ParentBean {

   // fields
   /**
    * The id field
    * @jdo.field 
    *    modifier="persistent"
    *    null-value="exception"
    *    primary-key="true"
    * @jdo.rdbmapping column="id"
    */
   private int id;


   // relations
   /**
    * The collection of related ChildBean
    *
    * @jdo.field collection-type="collection" element-type="airline.jdo.ChildBean"
    */
   private java.util.Collection childs = new java.util.HashSet();


   /**
    * Constructs a new ParentBean with only mandatory (non-nullable) parameters
    * @param id the id value
    */
   public ParentBean(int id) {
      setId(id);
   }


   /**
    * Returns the id
    *
    * @return the id
    */
   public int getId() {
      return id;
   }

   /**
    * Sets the id
    *
    * @param int the new id
    */
   public void setId(int newId) {
      id = newId;
   }


   /**
    * Returns a collection of related ChildBean
    *
    * @return a collection of related ChildBean
    */
   public java.util.Collection getChilds() {
      return childs;
   }

   /**
    * Sets a collection of related ChildBean
    *
    * @param a collection of related ChildBean
    */
   public void setChilds(java.util.Collection childs) {
      this.childs = childs;
   }

}
