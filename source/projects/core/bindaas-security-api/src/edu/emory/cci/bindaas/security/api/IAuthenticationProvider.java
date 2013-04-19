package edu.emory.cci.bindaas.security.api;

import java.util.Map;

/**
 * All authentication providers must implement this interface
 * @author nadir
 *
 */
public interface IAuthenticationProvider {

	/**
	 *  Return true if authentication by Username & Password is supported by this provider
	 * @return
	 */
	public boolean isAuthenticationByUsernamePasswordSupported();
	
	/**
	 * Return true if authentication by Security Token is supported
	 * @return
	 */
	public boolean isAuthenticationBySecurityTokenSupported();
	
	/**
	 * 
	 * @param username 
	 * @param password
	 * @param props used to configure this provider. May contain props to connect to underlying Idp
	 * @return
	 * @throws Exception when authentication fails
	 */
	public BindaasUser login(String username , String password ) throws AuthenticationException;
	
	/**
	 * 
	 * @param securityToken
	 * @param props
	 * @return Username of the authentication user
	 * @throws Exception when authentication fails
	 */
	public BindaasUser login(String securityToken ) throws AuthenticationException;
	
	/**
	 * Return description of properties required to configure this auth provider. key = property name , value = description
	 * @return
	 */
	public Map<String,String> getPropertyDescription() ;
	
	/**
	 * Support for API Key
	 * @return
	 */
	public boolean isAuthenticationByAPIKeySupported();
	public BindaasUser loginUsingAPIKey(String securityToken ) throws AuthenticationException;
	
}
