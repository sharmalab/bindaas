package edu.emory.cci.bindaas.security.impl;


import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.emory.cci.bindaas.core.jwt.IJWTManager;
import edu.emory.cci.bindaas.core.jwt.JWTManagerException;
import edu.emory.cci.bindaas.security.api.AuthenticationException;
import edu.emory.cci.bindaas.security.api.BindaasUser;
import edu.emory.cci.bindaas.security.api.IAuthenticationProvider;



public class OAuthProvider implements IAuthenticationProvider{

	private Log log = LogFactory.getLog(getClass());
	private IJWTManager JWTManager;


	public IJWTManager getJWTManager() {
		return JWTManager;
	}


	public void setJWTManager(IJWTManager JWTManager) {
		this.JWTManager = JWTManager;
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
		return new BindaasUser("username");

	}

	@Override
	public Map<String, String> getPropertyDescription() {

		return new HashMap<String, String>(); // TODO implement later
	}

	@Override
	public boolean isAuthenticationByAPIKeySupported() {

		return false;
	}

	@Override
	public BindaasUser loginUsingAPIKey(String apiKey)
			throws AuthenticationException {
		log.error("Login via ApiKey not supported. Authentication failed");
		log.info("The supported authenticationProviderClass for API_KEY is edu.emory.cci.bindaas.security.impl.DBAuthenticationProvider");
		throw new AuthenticationException(apiKey);
	}

	@Override
	public boolean isAuthenticationByJWTSupported() {
		return true;
	}

	@Override
	public BindaasUser loginUsingJWT(String jwt)
			throws AuthenticationException {
		try {
			BindaasUser retVal = JWTManager.lookupUser(jwt);

			if(retVal!=null) return retVal;
			else
				throw new AuthenticationException(jwt);
		} catch (JWTManagerException e) {
			log.error("Exception in Authentication Module" , e);
			throw new AuthenticationException(jwt);
		}
	}

}
