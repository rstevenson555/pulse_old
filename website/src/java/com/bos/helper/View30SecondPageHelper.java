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
import java.util.Date;
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
public class View30SecondPageHelper {
    private Document MainScreenDoc = null;
    DocumentBuilderFactory factory = null;
    DocumentBuilder builder = null;
    Document doc = null;
    String selectedDate = null;
    String sPageName = "";
    /*
        public ViewHistoricalChartsHelper() {
    		initialize();
    	}
    */
    public View30SecondPageHelper(String selectedDate, String page) {
        setSelectedDate(selectedDate);
        sPageName = page;
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
        Element exceptionElement = append30SecondPageSummary(MainScreenDoc, payload);
        pageElement.appendChild(headerElement);
        pageElement.appendChild(leftPanelElement);
        pageElement.appendChild(bodyElement);
        pageElement.appendChild(payload);
        MainScreenDoc.appendChild(pageElement);
        System.out.println(com.bcop.arch.builder.XMLUtils.documentToString(MainScreenDoc));
    }
    private static final String SELECT_30SECOND_PAGE =
        "	select u.companyName, u.fullName, u.userName,  "
            + "   p.pageName, c.contextName, b.branchName, a.LoadTime, a.Time, s.sessionTXT "
            + "	from AccessRecords a, Pages p , Contexts c, Branches b, Sessions s, Users u "
            + "	where a.Time>? "
            + "	and   a.Time<? "
            + "	and  a.LoadTime>30000 "
            + "	and a.Page_ID=p.Page_ID "
            + "	and a.Context_ID=c.Context_ID "
            + "	and a.Branch_Tag_ID=b.Branch_Tag_ID "
            + "	and p.pageName=? "
            + "	and a.User_ID=u.User_ID "
            + "	and s.Session_ID=a.Session_ID "
            + "	order by a.Time";
    private Element append30SecondPageSummary(Document doc, Element element) {
        Session session = null;
        Element ThirtySecondLoads = null;
        Element calendarElement = null;
        try {
            calendarElement = doc.createElement("Calendar");
            calendarElement.appendChild(createCalendarElement(doc));
            element.appendChild(calendarElement);
            ThirtySecondLoads = doc.createElement("ThirtySecondLoads");
            session = HibernateUtil.currentSession();
            Connection con = session.connection();
            char[] cDate = selectedDate.toCharArray();
            StringBuffer sDate =
                new StringBuffer()
                    .append(cDate[0])
                    .append(cDate[1])
                    .append(cDate[2])
                    .append(cDate[3])
                    .append(cDate[5])
                    .append(cDate[6])
                    .append(cDate[8])
                    .append(cDate[9]);
            PreparedStatement pstmt = con.prepareStatement(SELECT_30SECOND_PAGE);
	        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMddHHmmss");
            Date d  = null;
            try  {
                d = sdf2.parse( sDate.toString()+"000000" );
            }
            catch(java.text.ParseException pe)
            {
                d = new Date();
            }
            
            pstmt.setTimestamp(1,new java.sql.Timestamp( d.getTime() ));            

             // ***************************************************************
            Date endd  = null;
            try  {
                endd = sdf2.parse( sDate.toString()+"235959" );
            }
            catch(java.text.ParseException pe)
            {
                endd = new Date();
            }

            pstmt.setTimestamp(2,new java.sql.Timestamp( endd.getTime() ));
            //
            //
            pstmt.setString(3, sPageName);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String company = rs.getString(1);
                String fullName = rs.getString(2);
                String userName = rs.getString(3);
                String pageName = rs.getString(4);
                String contextName = rs.getString(5);
                String branchName = rs.getString(6);
                int iLoadTime = rs.getInt(7);
                String loadTime = ""+(((double)iLoadTime)/1000.00);
                String time = rs.getString(8);
                String sessionTxt = rs.getString(9);
                Element ThirtySecondLoadElement = doc.createElement("ThirtySecondLoadElement");
                if (company == null || fullName == null) {
                	String[] sa = new String[2];
					CompanyHelper.getCompanyAndUser(userName,sa);
                    company = sa[1];
                    fullName = sa[0];
                }else if(company.equalsIgnoreCase("null")){
                	System.out.println("Company is NYUlll;l");
                }
                Element companyElement = doc.createElement("company");
                Element fullNameElement = doc.createElement("fullName");
                Element userNameElement = doc.createElement("userName");
                Element pageNameElement = doc.createElement("pageName");
                Element contextNameElement = doc.createElement("contextName");
                Element branchNameElement = doc.createElement("branchName");
                Element loadTimeElement = doc.createElement("loadTime");
                Element timeElement = doc.createElement("time");
                Element sessionTxtElement = doc.createElement("sessionTxt");
                companyElement.appendChild(doc.createTextNode(company));
                fullNameElement.appendChild(doc.createTextNode(fullName));
                userNameElement.appendChild(doc.createTextNode(userName));
                pageNameElement.appendChild(doc.createTextNode(pageName));
                contextNameElement.appendChild(doc.createTextNode(contextName));
                branchNameElement.appendChild(doc.createTextNode(branchName));
                loadTimeElement.appendChild(doc.createTextNode(loadTime));
                timeElement.appendChild(doc.createTextNode(time));
                sessionTxtElement.appendChild(doc.createTextNode(sessionTxt));
                ThirtySecondLoadElement.appendChild(companyElement);
                ThirtySecondLoadElement.appendChild(fullNameElement);
                ThirtySecondLoadElement.appendChild(userNameElement);
                ThirtySecondLoadElement.appendChild(pageNameElement);
                ThirtySecondLoadElement.appendChild(contextNameElement);
                ThirtySecondLoadElement.appendChild(branchNameElement);
                ThirtySecondLoadElement.appendChild(loadTimeElement);
                ThirtySecondLoadElement.appendChild(timeElement);
                ThirtySecondLoadElement.appendChild(sessionTxtElement);
                ThirtySecondLoads.appendChild(ThirtySecondLoadElement);
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
        element.appendChild(ThirtySecondLoads);
        return ThirtySecondLoads;
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
        return (Element)doc.importNode(calendarBean.toDOM().getFirstChild(), true);
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
