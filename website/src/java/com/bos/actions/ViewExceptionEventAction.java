/* Generated by Together */

package com.bos.actions;
//  java

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.bcop.arch.logger.Logger;
import com.bos.helper.ViewExceptionEventHelper;
/**
 * This action class is used for the Main screen.<Br>
 */

public class ViewExceptionEventAction extends BaseAction {

	static {
		logger =
			(Logger) Logger.getLogger(ViewExceptionEventAction.class.getName());
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
        System.out.println("!!! ViewExceptionEventAction Hit !!!");
		ActionForward actionForward=null;
		String forwardString = "error";
		String requestId = null;
        actionForward = mapping.findForward("success");
		//run XMLBuilder
        String traceid = request.getParameter("traceid");
        ViewExceptionEventHelper mh = null;
       	mh = new ViewExceptionEventHelper(traceid);

        request.setAttribute(DOM, mh.getXMLDocument());

		return actionForward;
	}
}
