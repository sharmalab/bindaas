package edu.emory.cci.bindaas.security.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.emory.cci.bindaas.core.apikey.api.APIKeyManagerException;
import edu.emory.cci.bindaas.core.apikey.api.IAPIKeyManager;
import edu.emory.cci.bindaas.security.api.AuthenticationException;
import edu.emory.cci.bindaas.security.api.BindaasUser;
import edu.emory.cci.bindaas.security.api.IAuthenticationProvider;

public class DBAuthenticationProvider implements IAuthenticationProvider {

		
	private Log log = LogFactory.getLog(getClass());
	private IAPIKeyManager apiKeyManager;
	
	
	public IAPIKeyManager getApiKeyManager() {
		return apiKeyManager;
	}


	public void setApiKeyManager(IAPIKeyManager apiKeyManager) {
		this.apiKeyManager = apiKeyManager;
	}


	public void init()
	{

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
	public BindaasUser loginUsingAPIKey(String apiKey)
			throws AuthenticationException {
		try {
			BindaasUser retVal = apiKeyManager.lookupUser(apiKey); 
			
			if(retVal!=null) return retVal;
				else
			throw new AuthenticationException(apiKey);
		} catch (APIKeyManagerException e) {
			log.error("Exception in Authentication Module" , e);
			throw new AuthenticationException(apiKey);
		}
		
	
		
	}

}
