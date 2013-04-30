/*
 * Created on Jul 23, 2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package com.bos.helper;
import com.bos.arch.HibernateUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
/**
 * @author I0360D3
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class CompanyHelper {
    public static void main(String[] args) {
    }
    
	private static final String SELECT_USER_NAME_COMPANY =
	"	select cl.company_name as COMPANY_NAME, i.mr_ms as MR_MS, i.FIRST_NAME as FIRST_NAME, i.LAST_NAME as LAST_NAME "+
	"	from company_list cl, info i "+
	"	where cl.company_id =  "+
	"	(select company_id from Account_group_list where account_group = ( "+
	"		  select account_group from Authentication where user_key=?) "+
	"	) " +
	"	and i.user_key=?";
		public static String getCompanyAndUser(String userkey, String[] sa) {
			Session session = null;
			try {
				session = HibernateUtil.currentOracleSession();
				Connection con = session.connection();
				PreparedStatement pstmt = con.prepareStatement(SELECT_USER_NAME_COMPANY);
				pstmt.setString(1, userkey);
				pstmt.setString(2, userkey);
				ResultSet rs = pstmt.executeQuery();
				if (rs.next()) {
					StringBuffer fullName = new StringBuffer();
					String mr_ms = rs.getString("MR_MS");
					if (mr_ms != null && !mr_ms.equalsIgnoreCase("none")) {
						fullName.append(mr_ms).append(" ");
					}
					String firstName = rs.getString("FIRST_NAME");
					if (firstName != null) {
						fullName.append(firstName).append(" ");
					}
					String secondName = rs.getString("LAST_NAME");
					if (secondName != null) {
						fullName.append(secondName).append(" ");
					}
					sa[0] = fullName.toString();
					String company = rs.getString("COMPANY_NAME");
					if(company == null){
						company = null;
					}
					sa[1] = company;
                
				}
				if(sa[0] == null || sa[1] == null){
					sa[0]= "Force NULL";
					sa[1]= "Force NULL";
				}
			} catch (HibernateException e) {
				e.printStackTrace();
			} catch (SQLException e) {
				e.printStackTrace();
			}finally{
					if(session != null){
							try {
								HibernateUtil.closeOracleSession();
							} catch (HibernateException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
					}
			}
			return "NULL";
		}
    
}
