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
public class ViewLoadTestDetailHelper {
    private Document MainScreenDoc = null;
    DocumentBuilderFactory factory = null;
    DocumentBuilder builder = null;
    Document doc = null;
    String selectedDate = null;
    int loadTestID=0;
    /*
        public ViewHistoricalChartsHelper() {
    		initialize();
    	}
    */
    public ViewLoadTestDetailHelper(String selectedDate, String id) {
        setSelectedDate(selectedDate);
        loadTestID = Integer.parseInt(id);
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
        Element loadTestElement = appendLoadTests(MainScreenDoc,payload);
        //Element e =	appendDailySessionStatistics(MainScreenDoc,payload);
        pageElement.appendChild(headerElement); 
        pageElement.appendChild(leftPanelElement);
        pageElement.appendChild(bodyElement);
        pageElement.appendChild(payload);
        MainScreenDoc.appendChild(pageElement);
		System.out.println(com.bcop.arch.builder.XMLUtils.documentToString(MainScreenDoc));
    }
    
    
//    private static final String LOAD_TEST_QUERY =
//	"select transactionName, count, avg, max, min, NinetiethPercentile, FiftiethPercentile "+
//	"FROM LoadTestTransactionSummary ltts, LoadTestTransactions ltt "+
//	"where ltts.Transaction_ID = ltt.Transaction_ID "+
//	"and ltts.LoadTest_ID=? order by transactionName ";

private static final String LOAD_TEST_QUERY =
" select au.classID , au.cnt , au.avgLT, au.maxLT, au.minLT, ag.name as transactionName, ag.tid, ag.tDesc, "+
" ltts.count, ltts.avg, ltts.max, ltts.NinetiethPercentile, (au.avgLT - ltts.avg) as network"+
" From "+
"  LoadTests lt,  "+
" (select ear.Classification_ID as classID,  "+
"	   count(*) as cnt,  "+
"	   avg(LoadTime) as avgLT,  "+
"	   max(LoadTime) as maxLT,  "+
"	   min(LoadTime) as minLT "+
"		 from ExternalAccessRecords ear  "+
"			where Time>=? and Time<=? "+
"			and Classification_ID>100 "+
"			  group by Classification_ID) as au,  "+
" "+
"(select es.Classification_ID as classify, "+ 
"	   es.Description as name,  "+
"	   ls.Transaction_ID as tid,  "+
"	   ls.transactionDesc as tDesc "+
"		   from LoadTestTransactions ls, ExternalStats es "+
"			  where ls.transactionName=es.Description) as ag, "+
"	LoadTestTransactionSummary ltts "+
"	where  "+
"	   ag.classify=au.classID "+
"	   and ag.tid=ltts.Transaction_ID "+
"	   and ltts.LoadTest_ID=lt.LoadTest_ID "+
"	   and lt.LoadTest_ID=?";

	
	private static final String LOAD_TEST_SUMMARY_QUERY =
	"select scriptName, count, avg, max, min, NinetiethPercentile, FiftiethPercentile "+
	"FROM LoadTestSummary ltts, LoadTestScript lts "+
	"where ltts.Script_ID = lts.Script_ID "+
	"and ltts.LoadTest_ID=? order by scriptName ";


    private static final String LOAD_TEST = "SELECT startTime, endTime from LoadTests where LoadTest_ID=?";

    private Element appendLoadTests(Document doc,Element payload){
    	Element loadTest = null;
		Session session = null;
		loadTest = doc.createElement("LoadTest");
		Element calendarElement = null;
		
		
        try {
			calendarElement = doc.createElement("Calendar");

						calendarElement.appendChild(createCalendarElement(doc));
						payload.appendChild(calendarElement);
        	
                session = HibernateUtil.currentSession();
                Connection con = session.connection();
                PreparedStatement pstmt = con.prepareStatement(LOAD_TEST_QUERY);
                pstmt.setString(1,getTIDTime(con,loadTestID,"startTime"));
                pstmt.setString(2,getTIDTime(con,loadTestID,"endTime"));
                pstmt.setInt(3,loadTestID);
                ResultSet rs =pstmt.executeQuery();
                while(rs.next()){

                	String transactionName = rs.getString("transactionName");
                	String count           = rs.getString("count");
                	String avg             = rs.getString("avg");
					String max             = rs.getString("max");
					String p90ile          = rs.getString("NinetiethPercentile");
					String LRcnt           = rs.getString("cnt");
					String LRavg           = rs.getString("avgLT");
					String LRmax           = rs.getString("maxLT");
					String LRnetwork       = rs.getString("network");
					//String min             = rs.getString("min");
					//String p50ile          = rs.getString("FiftiethPercentile");

					Element loadTestTransaction         = doc.createElement("LoadTestTransaction");
					Element transactionNameElement      = doc.createElement("TransactionName");
					Element countElement                = doc.createElement("count");
					Element avgElement                  = doc.createElement("average");
					Element maxElement                  = doc.createElement("maximum");
					Element p90ileElement               = doc.createElement("NinetiethPercentile");
					Element LRavgElement                = doc.createElement("LoadRunnerAverage");
					Element LRmaxElement                = doc.createElement("LoadRunnerMax");
					Element LRnetworkElement            = doc.createElement("LoadRunnerNetwork");
					Element LRcntElement                = doc.createElement("LoadRunnerCount"); 
//					Element minElement                  = doc.createElement("minimum");
//					Element p50ileElement               = doc.createElement("FiftiethPercentile");
					
					transactionNameElement.appendChild(doc.createTextNode(transactionName));
					countElement.appendChild(doc.createTextNode(count));
					avgElement.appendChild(doc.createTextNode(avg));
					maxElement.appendChild(doc.createTextNode(max));
//					minElement.appendChild(doc.createTextNode(min));
					p90ileElement.appendChild(doc.createTextNode(p90ile));
//					p50ileElement.appendChild(doc.createTextNode(p50ile));
					LRavgElement.appendChild(doc.createTextNode(LRavg));
					LRmaxElement.appendChild(doc.createTextNode(LRmax));
					LRnetworkElement.appendChild(doc.createTextNode(LRnetwork));
					LRcntElement.appendChild(doc.createTextNode(LRcnt));

					loadTestTransaction.appendChild(transactionNameElement);
					loadTestTransaction.appendChild(countElement);
					loadTestTransaction.appendChild(avgElement);
					loadTestTransaction.appendChild(maxElement);
//					loadTestTransaction.appendChild(minElement);
					loadTestTransaction.appendChild(p90ileElement);
//					loadTestTransaction.appendChild(p50ileElement);
                    loadTestTransaction.appendChild(LRavgElement);
                    loadTestTransaction.appendChild(LRmaxElement);
                    loadTestTransaction.appendChild(LRnetworkElement);
                    loadTestTransaction.appendChild(LRcntElement);
					loadTest.appendChild(loadTestTransaction);
                }
                
                
                
                
			PreparedStatement pstmt2 = con.prepareStatement(LOAD_TEST_SUMMARY_QUERY);
			pstmt2.setInt(1,loadTestID);
			ResultSet rs2 =pstmt2.executeQuery();
			while(rs2.next()){

				String scriptName = rs2.getString("scriptName");
				String count           = rs2.getString("count");
				String avg = rs2.getString("avg");
				String max = rs2.getString("max");
				String min   = rs2.getString("min");
				String p90ile= rs2.getString("NinetiethPercentile");
				String p50ile   = rs2.getString("FiftiethPercentile");

				Element loadTestSummaryTransaction         = doc.createElement("LoadTestSummaryTransaction");
				Element scriptNameElement      = doc.createElement("ScriptName");
				Element countElement                = doc.createElement("count");
				Element avgElement                  = doc.createElement("average");
				Element maxElement                  = doc.createElement("maximum");
				Element minElement                  = doc.createElement("minimum");
				Element p90ileElement               = doc.createElement("NinetiethPercentile");
				Element p50ileElement               = doc.createElement("FiftiethPercentile");
					
				scriptNameElement.appendChild(doc.createTextNode(scriptName));
				countElement.appendChild(doc.createTextNode(count));
				avgElement.appendChild(doc.createTextNode(avg));
				maxElement.appendChild(doc.createTextNode(max));
				minElement.appendChild(doc.createTextNode(min));
				p90ileElement.appendChild(doc.createTextNode(p90ile));
				p50ileElement.appendChild(doc.createTextNode(p50ile));

				loadTestSummaryTransaction.appendChild(scriptNameElement);
				loadTestSummaryTransaction.appendChild(countElement);
				loadTestSummaryTransaction.appendChild(avgElement);
				loadTestSummaryTransaction.appendChild(maxElement);
				loadTestSummaryTransaction.appendChild(minElement);
				loadTestSummaryTransaction.appendChild(p90ileElement);
				loadTestSummaryTransaction.appendChild(p50ileElement);
				loadTest.appendChild(loadTestSummaryTransaction);
			}

                
        } catch (SQLException e1) {
            e1.printStackTrace();
        } catch (HibernateException e) {
            e.printStackTrace();
        }finally{
            try {
                HibernateUtil.closeSession();
            } catch (HibernateException e2) {
                e2.printStackTrace();
            }
        }
    	payload.appendChild(loadTest);
    	return loadTest;
    }

    
    public String getTIDTime(Connection con, int tid, String whichTime) throws SQLException{
		PreparedStatement pstmt = con.prepareStatement(LOAD_TEST);
		pstmt.setInt(1,tid);
		ResultSet rs = pstmt.executeQuery();
		String s=null;
		if(rs.next()){
			s = rs.getString(whichTime);
			System.out.println("Time: "+s);
		}
    	rs.close();
    	pstmt.close();
    	return s;
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
