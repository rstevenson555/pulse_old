/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bos.servlets;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author i0360b6
 */
public class IPServlet extends HttpServlet {

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //super.service(req, resp);
        resp.getWriter().write(req.getRemoteAddr());
        
        resp.flushBuffer();
    }
   
}
