/*
 * Created on Dec 12, 2003
 * 
 * To change the template for this generated file go to Window - Preferences -
 * Java - Code Generation - Code and Comments
 */
package com.bos.helper;
import com.bos.arch.HibernateUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import org.apache.commons.codec.binary.Base64;
/**
 * @author I0360D4
 * 
 * To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Generation - Code and Comments
 */
public class HtmlPageHelper {
    private static final String PAGE_QUERY = "Select h.encodedPage as encodedPage,c.contextName as contextName from HtmlPageResponse h,contexts c where h.HtmlPageResponse_ID=? and h.context_id = c.context_id";
    
    public HtmlPageHelperResponse getPage(String pageid){
    	int pageId = Integer.parseInt(pageid);
        String encodedPage = null;
    	Connection con = null;
		Session session = null;
        String contextName = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
    	try {
			session = HibernateUtil.currentSession();
			con = session.connection();
			pstmt = con.prepareStatement(PAGE_QUERY);
			pstmt.setInt(1,pageId);
			rs = pstmt.executeQuery();
			
			while (rs.next()) {
				encodedPage = rs.getString("encodedPage");
                contextName = rs.getString("contextName");
			}
		} catch (HibernateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			try {
                try {
                    pstmt.close();
                } catch (SQLException ex) {
                    Logger.getLogger(HtmlPageHelper.class.getName()).log(Level.SEVERE, null, ex);
                }
                try {
                    rs.close();
                } catch (SQLException ex) {
                    Logger.getLogger(HtmlPageHelper.class.getName()).log(Level.SEVERE, null, ex);
                }
				HibernateUtil.closeSession();
			} catch (HibernateException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		
        HtmlPageHelperResponse response = new HtmlPageHelperResponse();
        response.setPage(decode(encodedPage));
        response.setContextName(contextName);
		//return decode(encodedPage);		
        return response;
        
    }
    
    // don't have to decode anymore because the page is not encoded
    private String decode(String ep){
    	//byte[] ba = ep.getBytes();
    	//byte[] baDecoded = Base64.decodeBase64(ba); 
        //byte[] baDecoded = com.bos.art.logParser.tools.Base64.decodeFast(ep);
    	return ep;
    }
}

