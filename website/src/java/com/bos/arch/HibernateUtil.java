/*
 * Created on Dec 8, 2003
 *
 */
package com.bos.arch;

import net.sf.hibernate.Databinder;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.SessionFactory;
import net.sf.hibernate.cfg.Configuration;

import com.bcop.arch.logger.Logger;

public class HibernateUtil {

    public static SessionFactory sessionFactory = null;
    public static SessionFactory sessionFactoryOracle  = null;
    public static final ThreadLocal session = new ThreadLocal();
    public static final ThreadLocal sessionoracle = new ThreadLocal();
    
    private static final Logger logger = (Logger)Logger.getLogger(HibernateUtil.class.getName());

    static {
        try {
            sessionFactory = new Configuration().configure().buildSessionFactory();
            sessionFactoryOracle = new Configuration().configure("/OracleHibernate.cfg.xml").buildSessionFactory();
        } catch (Throwable ex) {
            logger.error(ex.getMessage());
            ex.printStackTrace();
        }
    }


    public static Databinder getDataBinder() throws HibernateException {
    	return sessionFactory.openDatabinder();
    }

    public static Databinder getOracleDataBinder() throws HibernateException {
    	return sessionFactoryOracle.openDatabinder();
    }

    public static Session currentSession()
         throws HibernateException {
             Session s = null;
       try {
            logger.info("HibernateUtil HIT!!!");
        s = (Session) session.get();

       // Open a new Session, if this Thread has none yet
       if (s == null) {
          s = sessionFactory.openSession();
          session.set(s);
       }
       } catch (Throwable t) {
           t.printStackTrace();
       }
       return s;
     }

     public static void closeSession()
         throws HibernateException {

        Session s = (Session) session.get();
        session.set(null);
        if (s != null) s.close();
     }

     public static Session currentOracleSession() throws HibernateException {
             Session s = null;
       try {
            logger.info("HibernateUtil HIT!!!");
            s = (Session) sessionoracle.get();

       // Open a new Session, if this Thread has none yet
       if (s == null) {
          s = sessionFactoryOracle.openSession();
          sessionoracle.set(s);
       }
       } catch (Throwable t) {
           t.printStackTrace();
       }
       return s;
     }

     public static void closeOracleSession()
         throws HibernateException {

        Session s = (Session) sessionoracle.get();
        sessionoracle.set(null);
        if (s != null) s.close();
     }

}
