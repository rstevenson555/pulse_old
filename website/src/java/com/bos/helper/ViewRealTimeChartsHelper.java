/*
 * Created on Dec 12, 2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package com.bos.helper;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * @author I0360D4
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ViewRealTimeChartsHelper {
	
	private Document MainScreenDoc = null;
	DocumentBuilderFactory factory = null;
	DocumentBuilder builder = null;
	Document doc = null;
	
	public ViewRealTimeChartsHelper() {
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
		pageElement.appendChild(headerElement);
		pageElement.appendChild(leftPanelElement);
		pageElement.appendChild(bodyElement);
		pageElement.appendChild(payload);
		
		MainScreenDoc.appendChild(pageElement);
	}
	
	public Document getXMLDocument() {
		return this.doc;
	}
}
