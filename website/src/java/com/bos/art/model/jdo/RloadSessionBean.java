package com.bos.art.model.jdo;

/**
 * @author <a href="http://boss.bekk.no/boss/middlegen/">Middlegen</a>
 *
 * @jdo.persistence-capable
 *    identity-type="datastore"
 * @sql.table table-name="Rload_Sessions"
 * @kodo.table class-column="none" lock-column="none"
 *
 * @jdo.finder todo...
 */
public class RloadSessionBean {

  // fields
  /**
   * The sessionId field
   * @jdo.field 
   *    modifier="persistent"
   *    null-value="exception"
   *    primary-key="true"
   * @sql.field column-name="Session_ID"
   */
  private int sessionId;

  /**
   * The ipaddress field
   * @jdo.field 
   *    modifier="persistent"
   *    null-value="none"
   * @sql.field column-name="IPAddress"
   */
  private java.lang.String ipaddress;

  /**
   * The sessionTxt field
   * @jdo.field 
   *    modifier="persistent"
   *    null-value="none"
   * @sql.field column-name="sessionTXT"
   */
  private java.lang.String sessionTxt;

  /**
   * The browserType field
   * @jdo.field 
   *    modifier="persistent"
   *    null-value="none"
   * @sql.field column-name="browserType"
   */
  private java.lang.String browserType;

  /**
   * The userId field
   * @jdo.field 
   *    modifier="persistent"
   *    null-value="none"
   * @sql.field column-name="User_ID"
   */
  private int userId;

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
   * Constructs a new RloadSessionBean with only mandatory (non-nullable) parameters
   * @param sessionId the sessionId value
   */
  public RloadSessionBean(int sessionId) {
    setSessionId(sessionId);
  }

  /**
   * Constructs a new RloadSessionBean with both nullable and not nullable parameters
   * @param sessionId the sessionId value
   * @param ipaddress the ipaddress value
   * @param sessionTxt the sessionTxt value
   * @param browserType the browserType value
   * @param userId the userId value
   * @param lastModTime the lastModTime value
   */
  public RloadSessionBean(int sessionId, java.lang.String ipaddress, java.lang.String sessionTxt, java.lang.String browserType, int userId, java.sql.Timestamp lastModTime) {
    setSessionId(sessionId);
    setIpaddress(ipaddress);
    setSessionTxt(sessionTxt);
    setBrowserType(browserType);
    setUserId(userId);
    setLastModTime(lastModTime);
  }


  /**
   * Returns the sessionId
   *
   * @return the sessionId
   */
  public int getSessionId() {
    return sessionId;
  }

  /**
   * Sets the sessionId
   *
   * @param newSessionId the new sessionId
   */
  public void setSessionId(int newSessionId) {
    sessionId = newSessionId;
  }


  /**
   * Returns the ipaddress
   *
   * @return the ipaddress
   */
  public java.lang.String getIpaddress() {
    return ipaddress;
  }

  /**
   * Sets the ipaddress
   *
   * @param newIpaddress the new ipaddress
   */
  public void setIpaddress(java.lang.String newIpaddress) {
    ipaddress = newIpaddress;
  }


  /**
   * Returns the sessionTxt
   *
   * @return the sessionTxt
   */
  public java.lang.String getSessionTxt() {
    return sessionTxt;
  }

  /**
   * Sets the sessionTxt
   *
   * @param newSessionTxt the new sessionTxt
   */
  public void setSessionTxt(java.lang.String newSessionTxt) {
    sessionTxt = newSessionTxt;
  }


  /**
   * Returns the browserType
   *
   * @return the browserType
   */
  public java.lang.String getBrowserType() {
    return browserType;
  }

  /**
   * Sets the browserType
   *
   * @param newBrowserType the new browserType
   */
  public void setBrowserType(java.lang.String newBrowserType) {
    browserType = newBrowserType;
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
