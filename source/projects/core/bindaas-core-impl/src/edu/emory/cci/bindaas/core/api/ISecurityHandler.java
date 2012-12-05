package edu.emory.cci.bindaas.core.api;

import java.util.Properties;

import edu.emory.cci.bindaas.security.api.IAuthenticationProvider;
import edu.emory.cci.bindaas.security.api.IAuthorizationProvider;

public interface ISecurityHandler {

	public boolean isEnableAuthorization() ;

	public boolean isEnableAuthentication() ;
	
	public Properties getAuthenticationProps() ;

	public Properties getAuthorizationProps() ;

	public IAuthenticationProvider locateAuthenticationProvider();
	
	public IAuthorizationProvider locateAuthorizationProvider();

	public String getAuthenticationProviderClass() ;
	
	public String getAuthorizationProviderClass() ;

	

}
