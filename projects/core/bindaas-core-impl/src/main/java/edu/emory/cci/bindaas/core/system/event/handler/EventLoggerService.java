package edu.emory.cci.bindaas.core.system.event.handler;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.Projections;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

import edu.emory.cci.bindaas.core.bundle.Activator;
import edu.emory.cci.bindaas.core.model.hibernate.BindaasEventInfo;
import edu.emory.cci.bindaas.framework.event.BindaasEvent;

public class EventLoggerService implements EventHandler {

	private static final int EXPORT_BATCH_THRESHOLD = 2000;
	private SessionFactory sessionFactory;

	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	private Log log = LogFactory.getLog(getClass());

	public void init() {
		String[] topics = new String[] { "edu/emory/cci/bindaas/framework/event/*" };

		Dictionary<String, Object> props = new Hashtable<String, Object>();
		props.put(EventConstants.EVENT_TOPIC, topics);

		Activator.getContext().registerService(EventHandler.class, this, props);
		Activator.getContext().registerService(EventLoggerService.class, this,
				props);
	}

	@Override
	public void handleEvent(Event event) {

		if (event instanceof BindaasEvent) {

			BindaasEvent bindaasEvent = BindaasEvent.class.cast(event);
			BindaasEventInfo eventInfo = new BindaasEventInfo(bindaasEvent);
			Session session = sessionFactory.openSession();
			try {
				session.beginTransaction();
				session.save(eventInfo);
				session.getTransaction().commit();
			} catch (Exception e) {
				log.error("Error persisting event information in database:\n"
						+ event, e);
			} finally {
				session.close();
			}
		}

	}

	private int hqlTruncate(String myTable, Session session) {
		String hql = String.format("delete from %s", myTable);
		Query query = session.createQuery(hql);
		return query.executeUpdate();
	}

	public int clean() throws Exception {
		SessionFactory sessionFactory = Activator
				.getService(SessionFactory.class);
		if (sessionFactory != null) {
			Session session = sessionFactory.openSession();
			try {
				session.beginTransaction();
				int rowsDeleted = hqlTruncate(BindaasEventInfo.class.getName(),
						session);
				session.getTransaction().commit();
				return rowsDeleted;
			} catch (Exception e) {
				Transaction t = session.getTransaction();
				if (t != null)
					t.rollback();
				log.error(e);
				throw e;
			} finally {
				session.close();
			}
		}
		return 0;

	}

	public void dump(Writer writer) throws Exception {

		Session session = sessionFactory.openSession();
		try {

			Number rowCount = (Number) session
					.createCriteria(BindaasEventInfo.class)
					.setProjection(Projections.rowCount()).uniqueResult();
			if (rowCount.intValue() < EXPORT_BATCH_THRESHOLD) {
				List<BindaasEventInfo> messages = (List<BindaasEventInfo>) session
						.createCriteria(BindaasEventInfo.class).list();
				for (BindaasEventInfo message : messages) {
					writer.write(message);
				}
			} else {
				int startingRowCursor = 0;
				int outstandingRecords = rowCount.intValue();

				while (outstandingRecords > 0) {
					int nextWriteBatchSize = Math.min(outstandingRecords,
							EXPORT_BATCH_THRESHOLD);
					List<BindaasEventInfo> messages = (List<BindaasEventInfo>) session
							.createCriteria(BindaasEventInfo.class)
							.setFirstResult(startingRowCursor)
							.setMaxResults(nextWriteBatchSize).list();
					for (BindaasEventInfo message : messages) {
						writer.write(message);
					}

					outstandingRecords -= nextWriteBatchSize;
					startingRowCursor += nextWriteBatchSize;
				}
			}
		} catch (Exception e) {
			log.error(e);
		}
	}

	public static interface Writer {
		public void write(BindaasEventInfo messge) throws IOException;

		public void flush() throws IOException;

	}

}
