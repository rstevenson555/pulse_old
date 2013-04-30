package com.bos.art.model.jdo;

/**
 * @author <a href="http://boss.bekk.no/boss/middlegen/">Middlegen</a>
 *
 * @jdo.persistence-capable
 *    identity-type="datastore"
 * @sql.table table-name="Rload_Machines"
 * @kodo.table class-column="none" lock-column="none"
 *
 * @jdo.finder todo...
 */
public class RloadMachinBean {

  // fields
  /**
   * The machineId field
   * @jdo.field 
   *    modifier="persistent"
   *    null-value="exception"
   *    primary-key="true"
   * @sql.field column-name="Machine_ID"
   */
  private int machineId;

  /**
   * The machineName field
   * @jdo.field 
   *    modifier="persistent"
   *    null-value="none"
   * @sql.field column-name="MachineName"
   */
  private java.lang.String machineName;

  /**
   * The shortName field
   * @jdo.field 
   *    modifier="persistent"
   *    null-value="none"
   * @sql.field column-name="shortName"
   */
  private java.lang.String shortName;

  /**
   * The machineType field
   * @jdo.field 
   *    modifier="persistent"
   *    null-value="none"
   * @sql.field column-name="machineType"
   */
  private java.lang.String machineType;

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
   * Constructs a new RloadMachinBean with only mandatory (non-nullable) parameters
   * @param machineId the machineId value
   */
  public RloadMachinBean(int machineId) {
    setMachineId(machineId);
  }

  /**
   * Constructs a new RloadMachinBean with both nullable and not nullable parameters
   * @param machineId the machineId value
   * @param machineName the machineName value
   * @param shortName the shortName value
   * @param machineType the machineType value
   * @param lastModTime the lastModTime value
   */
  public RloadMachinBean(int machineId, java.lang.String machineName, java.lang.String shortName, java.lang.String machineType, java.sql.Timestamp lastModTime) {
    setMachineId(machineId);
    setMachineName(machineName);
    setShortName(shortName);
    setMachineType(machineType);
    setLastModTime(lastModTime);
  }


  /**
   * Returns the machineId
   *
   * @return the machineId
   */
  public int getMachineId() {
    return machineId;
  }

  /**
   * Sets the machineId
   *
   * @param newMachineId the new machineId
   */
  public void setMachineId(int newMachineId) {
    machineId = newMachineId;
  }


  /**
   * Returns the machineName
   *
   * @return the machineName
   */
  public java.lang.String getMachineName() {
    return machineName;
  }

  /**
   * Sets the machineName
   *
   * @param newMachineName the new machineName
   */
  public void setMachineName(java.lang.String newMachineName) {
    machineName = newMachineName;
  }


  /**
   * Returns the shortName
   *
   * @return the shortName
   */
  public java.lang.String getShortName() {
    return shortName;
  }

  /**
   * Sets the shortName
   *
   * @param newShortName the new shortName
   */
  public void setShortName(java.lang.String newShortName) {
    shortName = newShortName;
  }


  /**
   * Returns the machineType
   *
   * @return the machineType
   */
  public java.lang.String getMachineType() {
    return machineType;
  }

  /**
   * Sets the machineType
   *
   * @param newMachineType the new machineType
   */
  public void setMachineType(java.lang.String newMachineType) {
    machineType = newMachineType;
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
