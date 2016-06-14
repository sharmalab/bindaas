package edu.emory.cci.bindaas.security.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;

import edu.emory.cci.bindaas.core.bundle.Activator;
import edu.emory.cci.bindaas.security.api.IAuditProvider;
import edu.emory.cci.bindaas.security.model.hibernate.AuditMessage;

public class DBAuditProvider implements IAuditProvider{

	private Log log = LogFactory.getLog(getClass());
	private static Integer MAX_DISPLAY_THRESHOLD = 10000 ;
	private static Integer EXPORT_BATCH_THRESHOLD = 4000 ;
	
	
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
				List<AuditMessage> auditMessages = (List<AuditMessage>) session.createCriteria(AuditMessage.class).setMaxResults(MAX_DISPLAY_THRESHOLD).addOrder(Order.desc("timestamp")).list();
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

	@Override
	public void dump(Writer writer) throws Exception {
		SessionFactory sessionFactory = Activator.getService(SessionFactory.class);
		if(sessionFactory!=null)
		{
			Session session = sessionFactory.openSession();
			try{
				
				Number rowCount = (Number) session.createCriteria(AuditMessage.class).setProjection(Projections.rowCount()).uniqueResult();
				if(rowCount.intValue() < EXPORT_BATCH_THRESHOLD)
				{
					List<AuditMessage> auditMessages = (List<AuditMessage>) session.createCriteria(AuditMessage.class).list();
					for(AuditMessage auditMessage : auditMessages)
					{
						writer.write(auditMessage);
					}
				}
				else
				{
					int startingRowCursor = 0;
					int outstandingRecords = rowCount.intValue();
					
					while(outstandingRecords > 0)
					{
						int nextWriteBatchSize = Math.min(outstandingRecords, EXPORT_BATCH_THRESHOLD);
						List<AuditMessage> auditMessages = (List<AuditMessage>) session.createCriteria(AuditMessage.class).setFirstResult(startingRowCursor).setMaxResults(nextWriteBatchSize).list();
						for(AuditMessage auditMessage : auditMessages)
						{
							writer.write(auditMessage);
						}
						
						outstandingRecords -= nextWriteBatchSize;
						startingRowCursor += nextWriteBatchSize;
					}
				}
			}
			catch(Exception e)
			{
				log.error(e);
			}
		}
		
	}

}
