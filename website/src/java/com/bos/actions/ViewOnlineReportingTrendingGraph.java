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
//import com.bos.helper.ViewHistoricalChartsHelper;
import com.bos.helper.ChartGeneratorHelper;
import com.bos.helper.TimeSliceDetailReport;

/**
 * This action class is used for the Main screen.<Br>
 */

public class ViewOnlineReportingTrendingGraph extends BaseAction {

	public static final String GRAPH="JFreeGraph";
	static {
		logger =
			(Logger) Logger.getLogger(ViewOnlineReportingTrendingGraph.class.getName());
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
        System.out.println("ViewOnlineReportingTrendingGraph    -> ");
		ActionForward actionForward=null;
		String forwardString = "error";
		String requestId = null;
        actionForward = mapping.findForward("success");
		//run XMLBuilder
		String selectedDate = request.getParameter("selectedDate");
		String startTime = request.getParameter("start");
		String endTime = request.getParameter("end");
		String context = request.getParameter("context");
        
        
        if(selectedDate != null && !selectedDate.equals("")) {
        	StringBuilder b = new StringBuilder(selectedDate);
        	StringBuilder s = b.insert(4,'-');
        	StringBuilder b2 = new StringBuilder(s.toString());
        	StringBuilder s2 = b2.insert(7,'-');
        	
        } else {
        	Calendar calendar = Calendar.getInstance();
         	StringBuilder todaysDate = new StringBuilder(); 
         	todaysDate.append(calendar.get(Calendar.DAY_OF_MONTH));
         	todaysDate.append(calendar.get(Calendar.MONTH));
         	todaysDate.append(calendar.get(Calendar.YEAR));
         	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
         	Date d = new Date();
         	String dateString = sdf.format(d);
         	
        }
        ChartGeneratorHelper cgh = new ChartGeneratorHelper();
        
        if(startTime == null || endTime==null){
        	startTime="20040709090000";
        	endTime="20040709150000";
        }

        System.out.println("online reporting: " + startTime + " endTime: " + endTime);
        request.setAttribute(GRAPH, cgh.generateMonthlyVolAvg(context + " : " + startTime + " to " + endTime,context,startTime,endTime));

		return actionForward;
	}
}

