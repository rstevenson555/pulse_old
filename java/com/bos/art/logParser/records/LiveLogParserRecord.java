/*
 * Created on Oct 21, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package com.bos.art.logParser.records;

import java.io.Serializable;

/**
 * @author I0360D3
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public abstract class LiveLogParserRecord implements Serializable, Comparable, ILiveLogParserRecord {
	//abstract public int obtainPriority();
	//abstract public Date obtainEventTime();
	//abstract public String obtainStringForComparison();
	
	//public boolean isExternalAccessEvent() { 
		//return false;
	//}
	//public boolean isAccessRecord(){
		//return false;
	//}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	/*public int compareTo(Object o) {
		LiveLogParserRecord that = (LiveLogParserRecord) o;
		if (this.obtainPriority() < that.obtainPriority()) {
			return -1;
		} else if (this.obtainPriority() > that.obtainPriority()) {
			return 1;
		} else {
			int dateCompare =
				this.obtainEventTime().compareTo(that.obtainEventTime());
			if (dateCompare != 0) {
				return dateCompare;
			} else {
				return this.obtainStringForComparison().compareTo(
					that.obtainStringForComparison());
			}
		}
	} */
	
	//abstract public Date getEventTime();
	//abstract public String getAppname();
	//abstract public String getServerName();
	//abstract public String getContext();
	//abstract public String getRemoteHost();
	//abstract public int getLoadTime();
	//abstract public boolean isFirstTimeUser();
	//abstract public boolean isErrorPage();
	abstract public String getPrimaryKey(String foreignKey);
	//abstract public boolean writeToDatabase();
	abstract public int DBWrite(String foreignKey);
	//abstract public String getBrowser();
	//Connection getConnection() throws SQLException{
		//return DriverManager.getConnection("jdbc:apache:commons:dbcp:example");
	//} 
}
