package com.bos.art.model.jdo;

/**
 * @author <a href="http://boss.bekk.no/boss/middlegen/">Middlegen</a>
 *
 * @jdo.persistence-capable
 *    identity-type="datastore"
 * @sql.table table-name="Rload_Contexts"
 * @kodo.table class-column="none" lock-column="none"
 *
 * @jdo.finder todo...
 */
public class RloadContextBean {

  // fields
  /**
   * The contextId field
   * @jdo.field 
   *    modifier="persistent"
   *    null-value="exception"
   *    primary-key="true"
   * @sql.field column-name="Context_ID"
   */
  private int contextId;

  /**
   * The contextName field
   * @jdo.field 
   *    modifier="persistent"
   *    null-value="none"
   * @sql.field column-name="contextName"
   */
  private java.lang.String contextName;

  /**
   * The lastModTime field
   * @jdo.field 
   *    modifier="persistent"
   *    null-value="none"
   * @sql.field column-name="lastModTime"
   */
  private java.sql.Timestamp lastModTime;


  // relations

  /**
   * Constructs a new RloadContextBean with only mandatory (non-nullable) parameters
   * @param contextId the contextId value
   */
  public RloadContextBean(int contextId) {
    setContextId(contextId);
  }

  /**
   * Constructs a new RloadContextBean with both nullable and not nullable parameters
   * @param contextId the contextId value
   * @param contextName the contextName value
   * @param lastModTime the lastModTime value
   */
  public RloadContextBean(int contextId, java.lang.String contextName, java.sql.Timestamp lastModTime) {
    setContextId(contextId);
    setContextName(contextName);
    setLastModTime(lastModTime);
  }


  /**
   * Returns the contextId
   *
   * @return the contextId
   */
  public int getContextId() {
    return contextId;
  }

  /**
   * Sets the contextId
   *
   * @param newContextId the new contextId
   */
  public void setContextId(int newContextId) {
    contextId = newContextId;
  }


  /**
   * Returns the contextName
   *
   * @return the contextName
   */
  public java.lang.String getContextName() {
    return contextName;
  }

  /**
   * Sets the contextName
   *
   * @param newContextName the new contextName
   */
  public void setContextName(java.lang.String newContextName) {
    contextName = newContextName;
  }


  /**
   * Returns the lastModTime
   *
   * @return the lastModTime
   */
  public java.sql.Timestamp getLastModTime() {
    return lastModTime;
  }

  /**
   * Sets the lastModTime
   *
   * @param newLastModTime the new lastModTime
   */
  public void setLastModTime(java.sql.Timestamp newLastModTime) {
    lastModTime = newLastModTime;
  }

}
