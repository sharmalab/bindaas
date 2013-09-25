package edu.emory.cci.bindaas.security.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import edu.emory.cci.bindaas.core.bundle.Activator;
import edu.emory.cci.bindaas.security.api.IAuditProvider;
import edu.emory.cci.bindaas.security.model.hibernate.AuditMessage;

public class DBAuditProvider implements IAuditProvider{

	private Log log = LogFactory.getLog(getClass());
	private static Integer MAX_DISPLAY_THRESHOLD = 1000 ; 
	
	@Override
	public void audit(AuditMessage auditMessage)
			throws Exception {
		SessionFactory sessionFactory = Activator.getService(SessionFactory.class);
		if(sessionFactory!=null)
		{
			Session session = sessionFactory.openSession();
			Transaction tx = null ;
			try{
				
				tx = session.beginTransaction();
				session.save(auditMessage);
				tx.commit();
			}
			catch(Exception e)
			{
				log.error(e);
				if(tx!=null) tx.rollback();
			}
			finally
			{
				session.close();
			}
		}

	
	}
	
	@Override
	public List<AuditMessage> getAuditLogs() throws Exception {
		SessionFactory sessionFactory = Activator.getService(SessionFactory.class);
		if(sessionFactory!=null)
		{
			Session session = sessionFactory.openSession();
			try{
				
				@SuppressWarnings("unchecked")
				List<AuditMessage> auditMessages = (List<AuditMessage>) session.createCriteria(AuditMessage.class).setMaxResults(MAX_DISPLAY_THRESHOLD).list();
				return auditMessages;
			}
			catch(Exception e)
			{
				log.error(e);
			}
		}
		return new ArrayList<AuditMessage>();

	}

	private int hqlTruncate(String myTable , Session session){
	    String hql = String.format("delete from %s",myTable);
	    Query query = session.createQuery(hql);
	    return query.executeUpdate();
	}
	
	@Override
	public int clean() throws Exception {
		SessionFactory sessionFactory = Activator.getService(SessionFactory.class);
		if(sessionFactory!=null)
		{
			Session session = sessionFactory.openSession();
			try{
				session.beginTransaction();
				int rowsDeleted = hqlTruncate(AuditMessage.class.getName(), session);
				session.getTransaction().commit();
				return rowsDeleted;
			}
			catch(Exception e)
			{
				Transaction t = session.getTransaction();
				if(t!=null)
					t.rollback();
				log.error(e);
				throw e;
			}
			finally{
				session.close();
			}
		}
		return 0;
		
	}

}
