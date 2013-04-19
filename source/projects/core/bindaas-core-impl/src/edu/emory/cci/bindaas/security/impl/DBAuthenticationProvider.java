package edu.emory.cci.bindaas.security.impl;

import java.util.Date;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;

import edu.emory.cci.bindaas.core.bundle.Activator;
import edu.emory.cci.bindaas.core.model.hibernate.UserRequest;
import edu.emory.cci.bindaas.core.rest.security.AuthenticationProtocol;
import edu.emory.cci.bindaas.security.api.AuthenticationException;
import edu.emory.cci.bindaas.security.api.BindaasUser;
import edu.emory.cci.bindaas.security.api.IAuthenticationProvider;

public class DBAuthenticationProvider implements IAuthenticationProvider {

		
	private Log log = LogFactory.getLog(getClass());
	public void init()
	{
		Dictionary<String, String> props = new Hashtable<String, String>();
		props.put("class", DBAuthenticationProvider.class.getName());
		props.put("protocol", AuthenticationProtocol.API_KEY.toString());
		Activator.getContext().registerService(IAuthenticationProvider.class.getName(), this, props);
	}
	
	
	@Override
	public boolean isAuthenticationByUsernamePasswordSupported() {
		
		return false;
	}

	@Override
	public boolean isAuthenticationBySecurityTokenSupported() {
		
		return false;
	}

	@Override
	public BindaasUser login(String username, String password )
			throws AuthenticationException {
		throw new AuthenticationException("Not implemented [" + username +"]");
	}

	@Override
	public BindaasUser login(String securityToken)
			throws AuthenticationException {
		throw new AuthenticationException("Not implemented [" + securityToken +"]");
	}

	@Override
	public Map<String, String> getPropertyDescription() {
		
		return new HashMap<String, String>(); // TODO implement later
	}
	


	@Override
	public boolean isAuthenticationByAPIKeySupported() {

		return true;
	}


	@Override
	public BindaasUser loginUsingAPIKey(String api_key)
			throws AuthenticationException {
		
		SessionFactory sessionFactory = Activator.getService(SessionFactory.class);
		if(sessionFactory!=null)
		{
			Session session = sessionFactory.openSession();
			try{
				
				@SuppressWarnings("unchecked")
				List<UserRequest> listOfValidKeys = (List<UserRequest>) session.createCriteria(UserRequest.class).add(Restrictions.eq("stage",	"accepted")).add(Restrictions.eq("apiKey", api_key)).list();
				if(listOfValidKeys!=null && listOfValidKeys.size() > 0)
				{
					UserRequest request = listOfValidKeys.get(0);
					if(request.getDateExpires().after(new Date()))
					{
						BindaasUser bindaasUser = new BindaasUser(request.getEmailAddress());
						bindaasUser.addProperty(BindaasUser.EMAIL_ADDRESS, request.getEmailAddress());
						bindaasUser.addProperty(BindaasUser.FIRST_NAME, request.getFirstName());
						bindaasUser.addProperty(BindaasUser.LAST_NAME, request.getLastName());
						return bindaasUser;
					}
				}
			}
			catch(Exception e)
			{
				log.error(e);
			}
		}
	
		throw new AuthenticationException(api_key);
	}

}
