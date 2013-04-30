package com.bos.model;

/**
 * @author <a href="http://boss.bekk.no/boss/middlegen/">Middlegen</a>
 *
 * @jdo.bean
 *    identity-type="datastore"
 * @jdo.rdbmapping table="child"
 *
 * @jdo.finder todo...
 */
public class ChildBean {

   // fields
   /**
    * The id field
    * @jdo.field 
    *    modifier="persistent"
    *    null-value="none"
    * @jdo.rdbmapping column="id"
    */
   private int id;


   // relations
   /**
    * The related ParentBean
    *
    * @jdo.field modifier="persistent" 
    */
   private com.bos.model.ParentBean parent = null;


   /**
    * Constructs a new ChildBean with only mandatory (non-nullable) parameters
    */
   public ChildBean() {
   }

   /**
    * Constructs a new ChildBean with both nullable and not nullable parameters
    * @param id the id value
    * @param parent the parent value
    */
   public ChildBean(int id, ParentBean parent) {
      setId(id);
      setParent(parent);
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
    * Returns the related ParentBean
    *
    * @return the related ParentBean
    */
   public com.bos.model.ParentBean getParent() {
      return parent;
   }

   /**
    * Sets the related ParentBean
    *
    * @param parent the related parent
    */
   public void setParent(com.bos.model.ParentBean parent) {
      this.parent = parent;
   }

}
