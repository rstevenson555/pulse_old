/*
 * Created on Dec 12, 2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */

package com.bos.helper;
import com.bos.arch.HibernateUtil;
import com.bos.art.model.jdo.DailySummaryBean;
import java.util.Iterator;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import net.sf.hibernate.Databinder;
import net.sf.hibernate.Hibernate;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import org.apache.axis.utils.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
/**
 * @author I0360D4
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class MainScreenHelper {
	
	private Document MainScreenDoc = null;
	DocumentBuilderFactory factory = null;
	DocumentBuilder builder = null;
	Document doc = null;
	
	public MainScreenHelper() {
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
		Element dailySummary = appendDailySummaryElement(MainScreenDoc, payload);
		pageElement.appendChild(headerElement);
		pageElement.appendChild(leftPanelElement);
		pageElement.appendChild(bodyElement);
		pageElement.appendChild(payload);
		
		MainScreenDoc.appendChild(pageElement);
	}
	
	private Element appendDailySummaryElement(Document doc, Element element) {
		Session session = null;
		Element dailySummaryElement = null;
		
		try {
			System.out.println("^^^^^^^^^^^NEW CODE^^^^^^^^^^^^^^^");
			dailySummaryElement = doc.createElement("DailySummary");
			//Transaction tx= session.beginTransaction();
			session = HibernateUtil.currentSession();
						
			List dailySummaryList = session.find(
					"from DailySummary as DailySummary where DailySummary.TotalLoads > ?",
					new Integer(1000),
					Hibernate.INTEGER
			);
			Databinder db = HibernateUtil.getDataBinder();
			db.setInitializeLazy(true);
			Iterator iter = dailySummaryList.iterator();			
			for (; iter.hasNext();) {
				DailySummaryBean dailySummary = (DailySummaryBean) iter.next();
System.out.println("DailySummary: "+dailySummary.getState());
				
db.bind(dailySummary);
Node element2 = doc.importNode(db.toDOM().getFirstChild(), true);
System.out.println("element2: "+XMLUtils.ElementToString((Element)element2));
dailySummaryElement.appendChild(element2);
element.appendChild(dailySummaryElement);
				//Marshaller m = new Marshaller(doc);
				//m.marshal(dailySummary, element);
				System.out.println("daily summary: " + dailySummary.getDay());
				System.out.println("daily summary: " + dailySummary.getTotalLoads());
				System.out.println("daily summary: " + dailySummary.getDistinctUsers());
			}
			System.out.println("finished query");
			//tx.commit();
			
		} catch (Exception ex) {
			ex.printStackTrace();            
		}finally{
			try {
				HibernateUtil.closeSession();
			} catch (HibernateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return dailySummaryElement;
	}
	
	public Document getXMLDocument() {
		return this.doc;
	}
}
