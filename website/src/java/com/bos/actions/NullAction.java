package com.bos.actions;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts.action.*;

public class NullAction extends Action
{

    public NullAction()
    {
    }

    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)
    {
        request.setAttribute("nullString", "This is an Attribute from request to " + request.getRequestURI());
        return mapping.findForward("success");
    }
}
