package edu.emory.cci.bindaas.security.ldap;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.emory.cci.bindaas.security.api.AuthenticationException;
import edu.emory.cci.bindaas.security.api.IAuthenticationProvider;

public class LDAPAuthenticationProvider implements IAuthenticationProvider {

	private Log log = LogFactory.getLog(getClass());
	
	@Override
	public boolean isAuthenticationByUsernamePasswordSupported() {
		return true;
	}

	@Override
	public boolean isAuthenticationBySecurityTokenSupported() {

		return false;
	}

	@Override
	public Principal login(final String username, String password, Properties props)
			throws AuthenticationException {
		String ldapServer = props.getProperty("LDAP_SERVER");
		String dnPattern = props.getProperty("DN_PATTERN");
		
		String dn =  String.format(dnPattern, username);
		Properties env = new Properties();
		env.put(Context.INITIAL_CONTEXT_FACTORY,
				"com.sun.jndi.ldap.LdapCtxFactory");
		env.put(Context.PROVIDER_URL, ldapServer);
		env.put(Context.SECURITY_PRINCIPAL, dn);
		env.put(Context.SECURITY_CREDENTIALS, password);

		try {
			DirContext ctx = new InitialDirContext(env);
			log.debug("LDAP Auth succeeded for [" + dn + "]");
		} catch (NamingException e) {
			log.error("Failed LDAP Auth using DN [" + dn  +"]",e);
			throw new AuthenticationException(username);
		}

		
		
		return new Principal() {
			
			@Override
			public String getName() {
				
				return username;
			}
		};
	}

	@Override
	public Principal login(String securityToken, Properties props)
			throws AuthenticationException {
		// method not implemented
		log.error("Login via SecurityToken not supported. Authentication failed");
		throw new AuthenticationException(securityToken);
	}

	@Override
	public Map<String, String> getPropertyDescription() {
		// TODO  implement this
		return new HashMap<String, String>();
	}

}
