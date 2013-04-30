/*
 * Created on Dec 12, 2003
 * 
 * To change the template for this generated file go to Window - Preferences -
 * Java - Code Generation - Code and Comments
 */
package com.bos.helper;

import com.bos.model.CalendarBean;
import java.util.Calendar;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
/**
 * @author I0360D4
 * 
 * To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Generation - Code and Comments
 */
public class ViewLoadTestHelper {
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
    public ViewLoadTestHelper(String selectedDate) {
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
        Element loadTestElement = null;//appendLoadTests(MainScreenDoc, payload);
        //Element e =	appendDailySessionStatistics(MainScreenDoc,payload);
        pageElement.appendChild(headerElement);
        pageElement.appendChild(leftPanelElement);
        pageElement.appendChild(bodyElement);
        pageElement.appendChild(payload);
        MainScreenDoc.appendChild(pageElement);
        System.out.println(com.bcop.arch.builder.XMLUtils.documentToString(MainScreenDoc));
    }
    private static final String LOAD_TEST_QUERY =
        "select LoadTest_ID, testName, c.contextName, b.branchName, startTime,endTime "
            + "from LoadTests lt, Contexts c, Branches b "
            + "where lt.Context_ID=c.Context_ID and b.Branch_Tag_ID=lt.Branch_ID and lt.Status!=\"Remove\" ";
    /*private Element appendLoadTests(Document doc, Element payload) {
        Element loadTests = null;
        Session session = null;
        loadTests = doc.createElement("LoadTests");
        Element calendarElement = null;
        Connection con = null;
        try {
            calendarElement = doc.createElement("Calendar");
            calendarElement.appendChild(createCalendarElement(doc));
            payload.appendChild(calendarElement);
            session = HibernateUtil.currentSession();
            con = session.connection();
            PreparedStatement pstmt = con.prepareStatement(LOAD_TEST_QUERY);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                int loadTestId = rs.getInt("LoadTest_ID");
                String testName = rs.getString("testName");
                String contextName = rs.getString("contextName");
                String branchName = rs.getString("branchName");
                String startTime = rs.getString("startTime");
                String endTime = rs.getString("endTime");
                Element loadTest = doc.createElement("LoadTest");
                Element loadTestIDElement = doc.createElement("LoadTestID");
                Element loadTestNameElement = doc.createElement("LoadTestName");
                Element contextElement = doc.createElement("Context");
                Element branchElement = doc.createElement("Branch");
                Element startTimeElement = doc.createElement("StartTime");
                Element endTimeElement = doc.createElement("EndTime");
                loadTestIDElement.appendChild(doc.createTextNode("" + loadTestId));
                loadTestNameElement.appendChild(doc.createTextNode(testName));
                contextElement.appendChild(doc.createTextNode(contextName));
                branchElement.appendChild(doc.createTextNode(branchName));
                startTimeElement.appendChild(doc.createTextNode(startTime));
                endTimeElement.appendChild(doc.createTextNode(endTime));
                loadTest.appendChild(loadTestNameElement);
                loadTest.appendChild(loadTestIDElement);
                loadTest.appendChild(contextElement);
                loadTest.appendChild(branchElement);
                loadTest.appendChild(startTimeElement);
                loadTest.appendChild(endTimeElement);
                loadTests.appendChild(loadTest);
            }
        } catch (Exception e1) {
            e1.printStackTrace();
            //        } catch (HibernateException e) {
            //          e.printStackTrace();
        } finally {
            
                if (con != null) {
                    try {
                        con.close();
                    } catch (SQLException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                //                HibernateUtil.closeSession();
                //} catch (HibernateException e2) {
                //  e2.printStackTrace();
            
        }
        payload.appendChild(loadTests);
        return loadTests;
    } */
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
