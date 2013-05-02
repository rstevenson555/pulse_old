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
import java.sql.SQLException;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
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
public class ClickStreamPageViewHelper {
    private Document MainScreenDoc = null;
    DocumentBuilderFactory factory = null;
    DocumentBuilder builder = null;
    Document doc = null;
    String selectedDate = null;
    String sessionTXT=null;
    /*
        public ViewHistoricalChartsHelper() {
    		initialize();
    	}
    */
    public ClickStreamPageViewHelper(String selectedDate, String sessiontxt) {
        setSelectedDate(selectedDate);
        System.out.println("selectedDate : " + this.selectedDate);
        sessionTXT=sessiontxt;
        System.out.println(sessionTXT);
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
        Element exceptionElement = appendPageLinks(MainScreenDoc, payload);
        
        pageElement.appendChild(headerElement);
        pageElement.appendChild(leftPanelElement);
        pageElement.appendChild(bodyElement);
        pageElement.appendChild(payload);
        MainScreenDoc.appendChild(pageElement);
        System.out.println(com.bcop.arch.builder.XMLUtils.documentToString(MainScreenDoc));
    }
    
    private static final String SELECT_PAGE_VIEWS =
        " Select p.pageName, h.requestToken, h.requestTokenCount, h.HtmlPageResponse_ID "+
		" from HtmlPageResponse h, Pages p where p.Page_ID=h.Page_ID and sessionTXT=? order by h.requestToken ";
    
    private Element appendPageLinks(Document doc, Element element) {
        Session session = null;
        Element calendarElement = null;
        Element ClickElements = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Connection con = null;
        try {
            calendarElement = doc.createElement("Calendar");
            calendarElement.appendChild(createCalendarElement(doc));
            element.appendChild(calendarElement);
            ClickElements = doc.createElement("ClickElements");
            session = HibernateUtil.currentSession();
            con = session.connection();
            pstmt = con.prepareStatement(SELECT_PAGE_VIEWS);
            pstmt.setString(1, sessionTXT);
            rs = pstmt.executeQuery();
            int recordID=1;
            while (rs.next()) {
                String HtmlPageResponse_ID = rs.getString("HtmlPageResponse_ID");
                String requestToken = rs.getString("requestToken");
                String pageName   = rs.getString("pageName");
                String requestTokenCount = rs.getString("requestTokenCount");
                
                Element HtmlPageResponseElement = doc.createElement("HtmlPageResponse_ID");
                Element requestTokenElement = doc.createElement("requestToken");
                Element recordIDElement = doc.createElement("RecordID");
                Element pageNameElement = doc.createElement("PageName");
                Element requestTokenCountElement = doc.createElement("requestTokenCount");
                
                HtmlPageResponseElement.appendChild(doc.createTextNode(HtmlPageResponse_ID));
                requestTokenElement.appendChild(doc.createTextNode(requestToken));
                recordIDElement.appendChild(doc.createTextNode(""+recordID++));
                pageNameElement.appendChild(doc.createTextNode(pageName));
                requestTokenCountElement.appendChild(doc.createTextNode(requestTokenCount));
                
                Element ClickElement = doc.createElement("ClickElement");
                ClickElement.appendChild(HtmlPageResponseElement);
                ClickElement.appendChild(requestTokenElement);
                ClickElement.appendChild(recordIDElement);
                ClickElement.appendChild(pageNameElement);
                ClickElement.appendChild(requestTokenCountElement);
                ClickElements.appendChild(ClickElement);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                rs.close();
            } catch (SQLException ex) {
                Logger.getLogger(ClickStreamPageViewHelper.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                pstmt.close();
            } catch (SQLException ex) {
                Logger.getLogger(ClickStreamPageViewHelper.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                HibernateUtil.closeSession();
            } catch (HibernateException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            try {
                con.close();
            } catch (SQLException ex) {
                Logger.getLogger(ClickStreamPageViewHelper.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        element.appendChild(ClickElements);
        return ClickElements;
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
