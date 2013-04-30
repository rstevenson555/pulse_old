package com.bos.art.model.jdo;

/**
 * @author <a href="http://boss.bekk.no/boss/middlegen/">Middlegen</a>
 *
 * @jdo.persistence-capable
 *    identity-type="datastore"
 * @sql.table table-name="Rload_Users"
 * @kodo.table class-column="none" lock-column="none"
 *
 * @jdo.finder todo...
 */
public class RloadUserBean {

  // fields
  /**
   * The userId field
   * @jdo.field 
   *    modifier="persistent"
   *    null-value="exception"
   *    primary-key="true"
   * @sql.field column-name="User_ID"
   */
  private int userId;

  /**
   * The userName field
   * @jdo.field 
   *    modifier="persistent"
   *    null-value="none"
   * @sql.field column-name="userName"
   */
  private java.lang.String userName;

  /**
   * The fullName field
   * @jdo.field 
   *    modifier="persistent"
   *    null-value="none"
   * @sql.field column-name="fullName"
   */
  private java.lang.String fullName;

  /**
   * The companyName field
   * @jdo.field 
   *    modifier="persistent"
   *    null-value="none"
   * @sql.field column-name="companyName"
   */
  private java.lang.String companyName;

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
   * Constructs a new RloadUserBean with only mandatory (non-nullable) parameters
   * @param userId the userId value
   */
  public RloadUserBean(int userId) {
    setUserId(userId);
  }

  /**
   * Constructs a new RloadUserBean with both nullable and not nullable parameters
   * @param userId the userId value
   * @param userName the userName value
   * @param fullName the fullName value
   * @param companyName the companyName value
   * @param lastModTime the lastModTime value
   */
  public RloadUserBean(int userId, java.lang.String userName, java.lang.String fullName, java.lang.String companyName, java.sql.Timestamp lastModTime) {
    setUserId(userId);
    setUserName(userName);
    setFullName(fullName);
    setCompanyName(companyName);
    setLastModTime(lastModTime);
  }


  /**
   * Returns the userId
   *
   * @return the userId
   */
  public int getUserId() {
    return userId;
  }

  /**
   * Sets the userId
   *
   * @param newUserId the new userId
   */
  public void setUserId(int newUserId) {
    userId = newUserId;
  }


  /**
   * Returns the userName
   *
   * @return the userName
   */
  public java.lang.String getUserName() {
    return userName;
  }

  /**
   * Sets the userName
   *
   * @param newUserName the new userName
   */
  public void setUserName(java.lang.String newUserName) {
    userName = newUserName;
  }


  /**
   * Returns the fullName
   *
   * @return the fullName
   */
  public java.lang.String getFullName() {
    return fullName;
  }

  /**
   * Sets the fullName
   *
   * @param newFullName the new fullName
   */
  public void setFullName(java.lang.String newFullName) {
    fullName = newFullName;
  }


  /**
   * Returns the companyName
   *
   * @return the companyName
   */
  public java.lang.String getCompanyName() {
    return companyName;
  }

  /**
   * Sets the companyName
   *
   * @param newCompanyName the new companyName
   */
  public void setCompanyName(java.lang.String newCompanyName) {
    companyName = newCompanyName;
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
