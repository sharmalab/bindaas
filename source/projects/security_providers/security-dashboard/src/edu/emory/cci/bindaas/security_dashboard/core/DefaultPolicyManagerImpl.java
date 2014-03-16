package edu.emory.cci.bindaas.security_dashboard.core;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

import edu.emory.cci.bindaas.core.apikey.api.IAPIKeyManager;
import edu.emory.cci.bindaas.security_dashboard.api.IPolicyManager;
import edu.emory.cci.bindaas.security_dashboard.config.SecurityDashboardConfiguration;
import edu.emory.cci.bindaas.security_dashboard.model.User;
import edu.emory.cci.bindaas.security_dashboard.model.hibernate.Policy;
import edu.emory.cci.bindaas.security_dashboard.util.RakshakUtils;

public class DefaultPolicyManagerImpl implements IPolicyManager {

	private SessionFactory sessionFactory;
	private Log log = LogFactory.getLog(getClass());
	private Joiner joiner;
	private Splitter splitter;
	
	private IAPIKeyManager apiKeyManager;
	
	public IAPIKeyManager getApiKeyManager() {
		return apiKeyManager;
	}

	public void setApiKeyManager(IAPIKeyManager apiKeyManager) {
		this.apiKeyManager = apiKeyManager;
	}

	
	public void init()
	{
		joiner = Joiner.on(",").skipNulls();
		splitter = Splitter.on(",").omitEmptyStrings(); 
	}
	
	
	@Override
	public void addAuthorizedMember(String resource, Set<String> groups) {
		
		Session session = sessionFactory.openSession();
		try {
			session.beginTransaction();
			List<Policy> policies = (List<Policy>) session.createCriteria(Policy.class).add(Restrictions.eq("resource", resource)).list();
			if(policies!=null && policies.size() > 0)
			{
				Policy policy = policies.get(0);
				String authorizedGroups = policy.getAuthorizedGroups();
				Set<String> setOfAuthorizedGroups  = new TreeSet<String>( );
				Iterator<String> itr = splitter.split(authorizedGroups).iterator() ;
				while(itr.hasNext())
				{
					setOfAuthorizedGroups.add(itr.next());
				}
				
				setOfAuthorizedGroups.addAll(groups);
				authorizedGroups = joiner.join(setOfAuthorizedGroups);
				policy.setAuthorizedGroups(authorizedGroups);
				
				session.save(policy);
				session.getTransaction().commit();
				
			}
			else
			{
				Policy policy = new Policy();
				policy.setResource(resource);

				Set<String> setOfAuthorizedGroups  = new TreeSet<String>( );
				setOfAuthorizedGroups.addAll(groups);
				String authorizedGroups = joiner.join(setOfAuthorizedGroups);
				policy.setAuthorizedGroups(authorizedGroups);
				
				session.save(policy);
				session.getTransaction().commit();
			}

		} 
		
	
		catch (Exception e) {
			log.error("Rolling back transaction" ,e);
			session.getTransaction().rollback();
	
		} finally {
			session.close();
		}

	}

	@Override
	public Set<String> getAuthorizedGroups(String resource) throws Exception {
		Session session = sessionFactory.openSession();
		try {
			session.beginTransaction();
			List<Policy> policies = (List<Policy>) session.createCriteria(Policy.class).add(Restrictions.eq("resource", resource)).list();
			if(policies!=null && policies.size() > 0)
			{
				Policy policy = policies.get(0);
				String authorizedGroups = policy.getAuthorizedGroups();
				Set<String> setOfAuthorizedGroups  = new TreeSet<String>( );
				Iterator<String> itr = splitter.split(authorizedGroups).iterator() ;
				while(itr.hasNext())
				{
					setOfAuthorizedGroups.add(itr.next());
				}
				
				return setOfAuthorizedGroups;
				
			}
			else
			{
				return new TreeSet<String>();
			}

		} 
		
		catch (Exception e) {
			log.error("Rolling back transaction" ,e);
			throw e;
	
		} finally {
			session.close();
		}


	}

	@Override
	public void removeAuthorizedMember(String resource, Set<String> groups) {
		Session session = sessionFactory.openSession();
		try {
			session.beginTransaction();
			List<Policy> policies = (List<Policy>) session.createCriteria(Policy.class).add(Restrictions.eq("resource", resource)).list();
			if(policies!=null && policies.size() > 0)
			{
				Policy policy = policies.get(0);
				String authorizedGroups = policy.getAuthorizedGroups();
				Set<String> setOfAuthorizedGroups  = new TreeSet<String>( );
				Iterator<String> itr = splitter.split(authorizedGroups).iterator() ;
				while(itr.hasNext())
				{
					setOfAuthorizedGroups.add(itr.next());
				}
				
				setOfAuthorizedGroups.removeAll(groups);
				
				authorizedGroups = joiner.join(setOfAuthorizedGroups);
				policy.setAuthorizedGroups(authorizedGroups);
				
				session.save(policy);
				session.getTransaction().commit();
				
			}
			else
			{
				return ;
			}
		} 
		catch (Exception e) {
			log.error("Rolling back transaction" ,e);
			session.getTransaction().rollback();
	
		} finally {
			session.close();
		}

		
	}

	@Override
	public boolean isAllowedAccess(String user, String resource,
			SecurityDashboardConfiguration configuration) throws Exception {
		
		Set<String> authorizedGroups = getAuthorizedGroups(resource);
		Set<User> setOfUsers = RakshakUtils.getUsersHavingAPIKey(configuration , apiKeyManager );
		
		for (User usr : setOfUsers) {
			String name = usr.getName();
			if (name != null && name.equals(user)) {
				Set<String> userGroups = usr.getGroups();
				if (userGroups != null) {
					for (String groupName : userGroups) {
						if (authorizedGroups.contains(groupName)) {
							return true;
						}
					}
				}
			}
		}
		
		return false;
		
	}

	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	
	
}
