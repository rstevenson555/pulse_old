/*
 * Created on Dec 12, 2003
 * 
 * To change the template for this generated file go to Window - Preferences -
 * Java - Code Generation - Code and Comments
 */
package com.bos.helper;

import com.bos.arch.HibernateUtil;
import com.bos.model.CalendarBean;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author I0360D4
 * 
 * To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Generation - Code and Comments
 */
public class ViewCurrentDeploymentsHelper {

	private Document MainScreenDoc = null;
	DocumentBuilderFactory factory = null;
	DocumentBuilder builder = null;
	Document doc = null;
	String selectedDate = null;

	/*
	    public ViewHistoricalChartsHelper() {
			initialize();
		}
	*/

	public ViewCurrentDeploymentsHelper(String selectedDate) {
		setSelectedDate(selectedDate);
		initialize();
	}

	public void initialize() {
		try {
			factory = DocumentBuilderFactory.newInstance();
			builder = factory.newDocumentBuilder();
			doc = builder.newDocument();
		} catch (Exception ex) {
		}
		buildXMLDocument();
	}

	private void buildXMLDocument() {
		MainScreenDoc = doc;
		Element pageElement = MainScreenDoc.createElement("Page");
		Element headerElement = MainScreenDoc.createElement("DashBoard");
		Element leftPanelElement = MainScreenDoc.createElement("LeftPanel");
		Element bodyElement = MainScreenDoc.createElement("Body");
		Element payload = MainScreenDoc.createElement("Payload");
		//Element dailySummary =
		//	appendDailySummaryElement(MainScreenDoc, payload);
		//Element dailyPageSummary =
	   // 	appendDailyPageSummaryElement(MainScreenDoc, payload);
		Element exceptionElement =
		appendCurrentDeployments(MainScreenDoc, payload);
		//Element e =	appendDailySessionStatistics(MainScreenDoc,payload);
		pageElement.appendChild(headerElement);
		pageElement.appendChild(leftPanelElement);
		pageElement.appendChild(bodyElement);
		pageElement.appendChild(payload);
		

		MainScreenDoc.appendChild(pageElement);
		System.out.println(com.bcop.arch.builder.XMLUtils.documentToString(MainScreenDoc));
	}


//private static final String SELECT_CURRENT_DEPLOYMENTS = "   select * from Deployments  "+
//"   where IsCurrent='Y' order by Server_Group, Product, Machine,Application_Context ";
    /**
     * String product = rs.getString(1);
				String machine = rs.getString(2);
				String serverGroup = rs.getString(3);
				String propertiesFile = rs.getString(4);
				String releaseTag = rs.getString(5);
				String applicationContext = rs.getString(6);
				String deployTime = rs.getString(7);
				String isCurrentString = rs.getString(8);
				String someComment = rs.getString(9);
				String NovellUserId = rs.getString(10);
				String changeControll = rs.getString(11);
     */

//private static final String SELECT_CURRENT_DEPLOYMENTS =  "select Product,Machine,Server_Group,Properties_file,Release_tag,application_context,deploy_time,iscurrent,somecomment,novelluserid,changecontrollnumber from Deployments   " +
//"   where IsCurrent='Y' and deploy_time in(select max(deploy_time) from deployments where iscurrent='Y' group by Server_Group, Application_Context) order by product,Server_Group, Machine,Application_Context ";

    private static final String SELECT_CURRENT_DEPLOYMENTS =  "select Product,Machine,Server_Group,Properties_file,Release_tag,application_context,deploy_time,iscurrent,somecomment,novelluserid,changecontrollnumber from Deployments   " +
"   where IsCurrent='Y' and deploy_time in(select max(deploy_time) from deployments where iscurrent='Y' group by product,Server_Group, Machine,Application_Context) order by product,Server_Group, Machine,Application_Context ";
        
//    private static final String SELECT_CURRENT_DEPLOYMENTS =  "select Product,Machine,Server_Group,Properties_file,Release_tag,application_context,deploy_time,iscurrent,somecomment,novelluserid,changecontrollnumber from Deployments   " +
//"   where IsCurrent='Y' order by product,Server_Group, Machine,Application_Context ";

    
	SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
	SimpleDateFormat sdfPretty = new SimpleDateFormat("[MM/dd/yyyy]{HH:mm}");
	
	private Element appendCurrentDeployments(
		Document doc,
		Element element) {
		Session session = null;
		Element CurrentDeployments = null;
		Element calendarElement = null;
		try {
			calendarElement = doc.createElement("Calendar");

			calendarElement.appendChild(createCalendarElement(doc));
			element.appendChild(calendarElement);
			CurrentDeployments = doc.createElement("CurrentDeployments");
			session = HibernateUtil.currentSession();
			Connection con = session.connection();
            
            
            char[] cDate = selectedDate.toCharArray();
            StringBuffer sDate = new StringBuffer().append(cDate[0]).append(cDate[1])
                .append(cDate[2]).append(cDate[3])
                .append(cDate[5]).append(cDate[6])
                .append(cDate[8]).append(cDate[9]);
			
			PreparedStatement pstmt = con.prepareStatement(SELECT_CURRENT_DEPLOYMENTS);
			
			
			ResultSet rs = pstmt.executeQuery();
			
            while(rs.next()){
//"	select count(*) as cnt, p.pageName, c.contextName, b.branchName "+

				
				String product = rs.getString(1);
				String machine = rs.getString(2);
				String serverGroup = rs.getString(3);
				String propertiesFile = rs.getString(4);
				String releaseTag = rs.getString(5);
				String applicationContext = rs.getString(6);
				String deployTime = rs.getString(7);
				String isCurrentString = rs.getString(8);
				String someComment = rs.getString(9);
				String NovellUserId = rs.getString(10);
				String changeControll = rs.getString(11);
				
				if(changeControll == null ){
					changeControll = "Not_Entered_At_Deploy_Time";
				}
				
				try{
					
					java.util.Date date = sdf.parse(deployTime);
					
					deployTime=sdfPretty.format(date);
					
				}catch(Exception e){
					e.printStackTrace();
				}
				
				
				Element CurrentElement = doc.createElement("CurrentElement");


				Element productElement  = doc.createElement("Product");
				Element machineElement = doc.createElement("Machine");
				Element serverGroupElement = doc.createElement("ServerGroup");
				Element propertiesFileElement = doc.createElement("PropertiesFile");
				Element releaseTagElement = doc.createElement("ReleaseTag");
				Element applicationContextElement = doc.createElement("ApplicationContext");
				Element deployTimeElement = doc.createElement("DeployTime");
				Element isCurrentElement = doc.createElement("CurrentFlag");
				Element someCommentElement = doc.createElement("Comment");
				Element NovellUserElement = doc.createElement("UserID");
				Element changeControllElement = doc.createElement("ChangeControll");
				
				productElement.appendChild(doc.createTextNode(product));
				machineElement.appendChild(doc.createTextNode(machine));
				serverGroupElement.appendChild(doc.createTextNode(serverGroup));
				propertiesFileElement.appendChild(doc.createTextNode(propertiesFile));
				releaseTagElement.appendChild(doc.createTextNode(releaseTag));
				applicationContextElement.appendChild(doc.createTextNode(applicationContext));
				deployTimeElement.appendChild(doc.createTextNode(deployTime));
				isCurrentElement.appendChild(doc.createTextNode(isCurrentString));
				someCommentElement.appendChild(doc.createTextNode(someComment));
				NovellUserElement.appendChild(doc.createTextNode(NovellUserId));
				changeControllElement.appendChild(doc.createTextNode(changeControll));

				CurrentElement.appendChild(productElement);
				CurrentElement.appendChild(machineElement);
				CurrentElement.appendChild(serverGroupElement);
				CurrentElement.appendChild(propertiesFileElement);
				CurrentElement.appendChild(releaseTagElement);
				CurrentElement.appendChild(applicationContextElement);
				CurrentElement.appendChild(deployTimeElement);
				CurrentElement.appendChild(isCurrentElement);
				CurrentElement.appendChild(someCommentElement);
				CurrentElement.appendChild(NovellUserElement);
				CurrentElement.appendChild(changeControllElement);

				CurrentDeployments.appendChild(CurrentElement);

            }				
				
				
                
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				HibernateUtil.closeSession();
			} catch (HibernateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
        element.appendChild(CurrentDeployments);
		return CurrentDeployments;
    }

	public Element createCalendarElement(Document doc) {
		Calendar c = Calendar.getInstance();
		if (this.getSelectedDate() != null) {
			//get calendar for selected DATE
			String selDate = this.getSelectedDate();
			String yearS = selDate.substring(0, 4);
			String monthS = selDate.substring(5, 7);
			String dateS = selDate.substring(8, 10);

			int year = new Integer(yearS).intValue();
			int month = new Integer(monthS).intValue();
			int date = new Integer(dateS).intValue();

			c.set(year, month - 1, date);
		}

		CalendarBean calendarBean = new CalendarBean(c);

		return (Element) doc.importNode(
			calendarBean.toDOM().getFirstChild(),
			true);
	}

	public Document getXMLDocument() {
		return this.doc;
	}
	/**
	 * @return Returns the selectedDate.
	 */
	public String getSelectedDate() {
		return selectedDate;
	}

	/**
	 * @param selectedDate The selectedDate to set.
	 */
	public void setSelectedDate(String selectedDate) {
		this.selectedDate = selectedDate;
	}



}
