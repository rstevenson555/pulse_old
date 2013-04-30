/* Generated by Together */

package com.bos.actions;
//  java
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.bcop.arch.logger.Logger;
import com.bos.helper.ViewExceptionTypeHelper;
import com.bos.helper.ViewHistoricalChartsHelper;

/**
 * This action class is used for the Main screen.<Br>
 */

public class ViewExceptionTypeAction extends BaseAction {

	static {
		logger =
			(Logger) Logger.getLogger(ViewExceptionTypeAction.class.getName());
	}
	
	/**
	 * Process the specified HTTP request, and create the corresponding HTTP
	 * response (or forward to another web component that will create it).
	 * Return an <code>ActionForward</code> instance describing where and how
	 * control should be forwarded, or <code>null</code> if the response has already been completed.
	 * @param mapping    - The ActionMapping used to select this instance
	 * @param actionForm - The optional ActionForm bean for this request (if any)
	 * @param request    - The HTTP request we are processing
	 * @param response   - The HTTP response we are creating
	 * @exception Exception if business logic throws an exception
	 * @return - returns the ActionForward.
	 */
	public ActionForward processAction(
	ActionMapping mapping,
	ActionForm actionForm,
	HttpServletRequest request,
	HttpServletResponse response) {
        System.out.println("!!! Action Hit !!!");
		ActionForward actionForward=null;
		String forwardString = "error";
		String requestId = null;
        actionForward = mapping.findForward("success");
		//run XMLBuilder
        String selectedDate = request.getParameter("selectedDate");
        String el = request.getParameter("el");
        ViewExceptionTypeHelper mh = null;
        if(el == null || el.equals("")){
        	el = "java.lang.NullPointerException";
        }
        if(selectedDate != null && !selectedDate.equals("")) {
        	
        	mh = new ViewExceptionTypeHelper(selectedDate,el);
        } else {
        	Calendar calendar = Calendar.getInstance();
         	StringBuffer todaysDate = new StringBuffer(); 
         	todaysDate.append(calendar.get(Calendar.DAY_OF_MONTH));
         	todaysDate.append(calendar.get(Calendar.MONTH));
         	todaysDate.append(calendar.get(Calendar.YEAR));
         	SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
         	Date d = new Date();
         	String dateString = sdf.format(d);
         	
        	mh = new ViewExceptionTypeHelper(dateString,el);
        }

        request.setAttribute(DOM, mh.getXMLDocument());

		return actionForward;
	}
}

