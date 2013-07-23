package edu.emory.cci.bindaas.security.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
				List<AuditMessage> auditMessages = (List<AuditMessage>) session.createCriteria(AuditMessage.class).setFetchSize(MAX_DISPLAY_THRESHOLD).list();
				return auditMessages;
			}
			catch(Exception e)
			{
				log.error(e);
			}
		}
		return new ArrayList<AuditMessage>();

	}

}
