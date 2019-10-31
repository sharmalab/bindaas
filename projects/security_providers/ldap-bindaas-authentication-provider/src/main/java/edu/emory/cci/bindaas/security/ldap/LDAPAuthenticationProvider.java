package edu.emory.cci.bindaas.security.ldap;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.InitialDirContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;

import edu.emory.cci.bindaas.core.util.DynamicProperties;
import edu.emory.cci.bindaas.security.api.AuthenticationException;
import edu.emory.cci.bindaas.security.api.BindaasUser;
import edu.emory.cci.bindaas.security.api.IAuthenticationProvider;
import edu.emory.cci.bindaas.security.ldap.bundle.Activator;

public class LDAPAuthenticationProvider implements IAuthenticationProvider {

	private static Log log = LogFactory.getLog(LDAPAuthenticationProvider.class);
	
	private Properties defaultProperties;
	public Properties getDefaultProperties() {
		return defaultProperties;
	}

	public void setDefaultProperties(Properties defaultProperties) {
		this.defaultProperties = defaultProperties;
	}

	private DynamicProperties dynamicProperties;

	public void init()
	{		
		BundleContext context = Activator.getContext();
		dynamicProperties = new DynamicProperties("bindaas.authentication.ldap", defaultProperties , context);
		Dictionary<String, String> props = new Hashtable<String, String>();
		props.put("class", LDAPAuthenticationProvider.class.getName());
		context.registerService(IAuthenticationProvider.class.getName(), this, props);

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
	public BindaasUser login(final String username, String password)
			throws AuthenticationException {
		Properties props = (Properties) dynamicProperties.getProperties().clone();
		
		String ldapServer = props.getProperty("ldap.url");
		String dnPattern = props.getProperty("ldap.dn.pattern");
		
		String dn =  String.format(dnPattern, username);
		Properties env = new Properties();
		env.put(Context.INITIAL_CONTEXT_FACTORY,
				"com.sun.jndi.ldap.LdapCtxFactory");
		env.put(Context.PROVIDER_URL, ldapServer);
		env.put(Context.SECURITY_PRINCIPAL, dn);
		env.put(Context.SECURITY_CREDENTIALS, password);

		try {
			new InitialDirContext(env);
			log.debug("LDAP Auth succeeded for [" + dn + "]");
		} catch (NamingException e) {
			log.error("Failed LDAP Auth using DN [" + dn  +"]",e);
			throw new AuthenticationException(username);
		}

		
		return new BindaasUser(username);
	}

	public static BindaasUser login(final String username, String password , String ldapServer , String dnPattern)
			throws AuthenticationException {
		
		
		String dn =  String.format(dnPattern, username);
		Properties env = new Properties();
		env.put(Context.INITIAL_CONTEXT_FACTORY,
				"com.sun.jndi.ldap.LdapCtxFactory");
		env.put(Context.PROVIDER_URL, ldapServer);
		env.put(Context.SECURITY_PRINCIPAL, dn);
		env.put(Context.SECURITY_CREDENTIALS, password);

		try {
			new InitialDirContext(env);
			log.debug("LDAP Auth succeeded for [" + dn + "]");
		} catch (NamingException e) {
			log.error("Failed LDAP Auth using DN [" + dn  +"]",e);
			throw new AuthenticationException(username);
		}

		
		return new BindaasUser(username);
	}

	@Override
	public BindaasUser login(String securityToken)
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

	@Override
	public boolean isAuthenticationByAPIKeySupported() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public BindaasUser loginUsingAPIKey(String apiKey)
			throws AuthenticationException {
		log.error("Login via ApiKey not supported. Authentication failed");
		throw new AuthenticationException(apiKey);
	}
	
	public static boolean testConnection(Properties props , String username , String password)
	{

		String ldapServer = props.getProperty("ldap.url");
		String dnPattern = props.getProperty("ldap.dn.pattern");
		
		String dn =  String.format(dnPattern, username);
		Properties env = new Properties();
		env.put(Context.INITIAL_CONTEXT_FACTORY,
				"com.sun.jndi.ldap.LdapCtxFactory");
		env.put(Context.PROVIDER_URL, ldapServer);
		env.put(Context.SECURITY_PRINCIPAL, dn);
		env.put(Context.SECURITY_CREDENTIALS, password);

		try {
			new InitialDirContext(env);
			return true;
		} catch (NamingException e) {
			log.error(e);
		}
		return false;
	}

	@Override
	public boolean isAuthenticationByJWTSupported() {
		return false;
	}

	@Override
	public BindaasUser loginUsingJWT(String jwt)
			throws AuthenticationException {
		log.error("Login via JWT not supported. Authentication failed");
		throw new AuthenticationException(jwt);
	}
}
