package edu.emory.cci.bindaas.security.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import edu.emory.cci.bindaas.core.bundle.Activator;
import edu.emory.cci.bindaas.core.util.DynamicProperties;
import edu.emory.cci.bindaas.security.api.AuthenticationException;
import edu.emory.cci.bindaas.security.api.BindaasUser;
import edu.emory.cci.bindaas.security.api.IAuthenticationProvider;

public class FileSystemAuthenticationProvider implements IAuthenticationProvider{

	private DynamicProperties dynamicProperties;
	private Properties defaultProperties;
	
	public DynamicProperties getDynamicProperties() {
		return dynamicProperties;
	}


	public void setDynamicProperties(DynamicProperties dynamicProperties) {
		this.dynamicProperties = dynamicProperties;
	}


	public Properties getDefaultProperties() {
		return defaultProperties;
	}


	public void setDefaultProperties(Properties defaultProperties) {
		this.defaultProperties = defaultProperties;
	}



	public void init()
	{
		dynamicProperties = new DynamicProperties("bindaas.authentication", defaultProperties , Activator.getContext());
	}
	
	
	@Override
	public boolean isAuthenticationByUsernamePasswordSupported() {
		
		return true;
	}

	@Override
	public boolean isAuthenticationBySecurityTokenSupported() {
		
		return false;
	}

	@Override
	public BindaasUser login(String username, String password)
			throws AuthenticationException {
		String pass = (String) dynamicProperties.get(username);
		if(pass!=null && pass.equals(password))
		{
			return new BindaasUser(username);
		}
		else
			throw new AuthenticationException(username);
	}

	@Override
	public BindaasUser login(String securityToken)
			throws AuthenticationException {
		throw new AuthenticationException(securityToken);
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
	public BindaasUser loginUsingAPIKey(String api_key )
			throws AuthenticationException {
		String username = (String) dynamicProperties.get(api_key);
		if(username!=null)
		{
			return new BindaasUser(username);
		}
		else
			throw new AuthenticationException(api_key);
	}

}
