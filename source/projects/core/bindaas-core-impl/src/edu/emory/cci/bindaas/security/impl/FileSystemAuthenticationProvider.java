package edu.emory.cci.bindaas.security.impl;

import java.security.Principal;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

import edu.emory.cci.bindaas.core.bundle.Activator;
import edu.emory.cci.bindaas.security.api.AuthenticationException;
import edu.emory.cci.bindaas.security.api.BindaasUser;
import edu.emory.cci.bindaas.security.api.IAuthenticationProvider;

public class FileSystemAuthenticationProvider implements IAuthenticationProvider{

	public void init()
	{
		Dictionary<String, String> props = new Hashtable<String, String>();
		props.put("class", FileSystemAuthenticationProvider.class.getName());
		Activator.getContext().registerService(IAuthenticationProvider.class.getName(), this, props);
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
	public BindaasUser login(String username, String password, Properties props)
			throws AuthenticationException {
		String pass = props.getProperty(username);
		if(pass!=null && pass.equals(password))
		{
			return new BindaasUser(username);
		}
		else
			throw new AuthenticationException(username);
	}

	@Override
	public BindaasUser login(String securityToken, Properties props)
			throws AuthenticationException {
		throw new AuthenticationException(securityToken);
	}

	@Override
	public Map<String, String> getPropertyDescription() {
		
		return new HashMap<String, String>(); // TODO implement later
	}
	
	public Principal createPrincipal(final String username)
	{
		return new Principal() {
			
			@Override
			public String getName() {
			
				return username;
			}
		};
	}

}
