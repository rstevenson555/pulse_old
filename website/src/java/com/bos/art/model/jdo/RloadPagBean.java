package com.bos.art.model.jdo;

/**
 * @author <a href="http://boss.bekk.no/boss/middlegen/">Middlegen</a>
 *
 * @jdo.persistence-capable
 *    identity-type="datastore"
 * @sql.table table-name="Rload_Pages"
 * @kodo.table class-column="none" lock-column="none"
 *
 * @jdo.finder todo...
 */
public class RloadPagBean {

  // fields
  /**
   * The pageId field
   * @jdo.field 
   *    modifier="persistent"
   *    null-value="exception"
   *    primary-key="true"
   * @sql.field column-name="Page_ID"
   */
  private int pageId;

  /**
   * The pageName field
   * @jdo.field 
   *    modifier="persistent"
   *    null-value="none"
   * @sql.field column-name="pageName"
   */
  private java.lang.String pageName;

  /**
   * The isErrorPage field
   * @jdo.field 
   *    modifier="persistent"
   *    null-value="none"
   * @sql.field column-name="isErrorPage"
   */
  private java.lang.String isErrorPage;

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
   * Constructs a new RloadPagBean with only mandatory (non-nullable) parameters
   * @param pageId the pageId value
   */
  public RloadPagBean(int pageId) {
    setPageId(pageId);
  }

  /**
   * Constructs a new RloadPagBean with both nullable and not nullable parameters
   * @param pageId the pageId value
   * @param pageName the pageName value
   * @param isErrorPage the isErrorPage value
   * @param lastModTime the lastModTime value
   */
  public RloadPagBean(int pageId, java.lang.String pageName, java.lang.String isErrorPage, java.sql.Timestamp lastModTime) {
    setPageId(pageId);
    setPageName(pageName);
    setIsErrorPage(isErrorPage);
    setLastModTime(lastModTime);
  }


  /**
   * Returns the pageId
   *
   * @return the pageId
   */
  public int getPageId() {
    return pageId;
  }

  /**
   * Sets the pageId
   *
   * @param newPageId the new pageId
   */
  public void setPageId(int newPageId) {
    pageId = newPageId;
  }


  /**
   * Returns the pageName
   *
   * @return the pageName
   */
  public java.lang.String getPageName() {
    return pageName;
  }

  /**
   * Sets the pageName
   *
   * @param newPageName the new pageName
   */
  public void setPageName(java.lang.String newPageName) {
    pageName = newPageName;
  }


  /**
   * Returns the isErrorPage
   *
   * @return the isErrorPage
   */
  public java.lang.String getIsErrorPage() {
    return isErrorPage;
  }

  /**
   * Sets the isErrorPage
   *
   * @param newIsErrorPage the new isErrorPage
   */
  public void setIsErrorPage(java.lang.String newIsErrorPage) {
    isErrorPage = newIsErrorPage;
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
