package edu.emory.cci.bindaas.core.api;


public interface ISecurityHandler {

	public boolean isEnableAuthorization() ;

	public boolean isEnableAuthentication() ;
	
	public String getAuthenticationProviderClass() ;
	
	public String getAuthorizationProviderClass() ;

	

}
