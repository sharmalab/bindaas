package edu.emory.cci.bindaas.pseudosts.impl;

import java.util.Collection;
import java.util.Dictionary;
import java.util.Hashtable;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import edu.emory.cci.bindaas.core.apikey.api.APIKey;
import edu.emory.cci.bindaas.core.apikey.api.IAPIKeyManager;
import edu.emory.cci.bindaas.core.config.BindaasConfiguration;
import edu.emory.cci.bindaas.core.util.DynamicObject;
import edu.emory.cci.bindaas.pseudosts.api.IPsuedoSecurityTokenService;
import edu.emory.cci.bindaas.pseudosts.bundle.Activator;
import edu.emory.cci.bindaas.pseudosts.conf.Configuration;
import edu.emory.cci.bindaas.security.api.BindaasUser;
import edu.emory.cci.bindaas.security.api.IAuthenticationProvider;
import edu.emory.cci.bindaas.webconsole.config.BindaasAdminConsoleConfiguration;
import edu.emory.cci.bindaas.webconsole.config.BindaasAdminConsoleConfiguration.UserConfiguration.AuthenticationMethod;

public class PseduoSecurityTokenServiceImpl implements
		IPsuedoSecurityTokenService {

	private Log log = LogFactory.getLog(getClass());
	
	private Configuration defaultConfiguration;
	private DynamicObject<Configuration> dynamicConfiguration;
	
	private IAPIKeyManager apiKeyManager;
	
	
	public Configuration getDefaultConfiguration() {
		return defaultConfiguration;
	}

	public void setDefaultConfiguration(Configuration defaultConfiguration) {
		this.defaultConfiguration = defaultConfiguration;
	}

	public IAPIKeyManager getApiKeyManager() {
		return apiKeyManager;
	}

	public void setApiKeyManager(IAPIKeyManager apiKeyManager) {
		this.apiKeyManager = apiKeyManager;
	}

	public void init() throws Exception {

		final BundleContext context = Activator.getContext();
		
		this.dynamicConfiguration = new DynamicObject<Configuration>("bindaas-pseudo-sts", defaultConfiguration, context);
		
		String filterExpression = "(&(objectclass=edu.emory.cci.bindaas.core.util.DynamicObject)(name=bindaas))";
		Filter filter = FrameworkUtil.createFilter(filterExpression);
		
		final IPsuedoSecurityTokenService ref = this;
		@SuppressWarnings({ "rawtypes", "unchecked" })
		ServiceTracker<?,?> serviceTracker = new ServiceTracker(context, filter,
				new ServiceTrackerCustomizer() {

					@Override
					public Object addingService(ServiceReference srf) {
						@SuppressWarnings("unchecked")
						DynamicObject<BindaasConfiguration> dynamicConfiguration = (DynamicObject<BindaasConfiguration>) context
								.getService(srf);
						Dictionary<String, Object> testProps = new Hashtable<String, Object>();
						testProps.put("edu.emory.cci.bindaas.commons.cxf.service.name", "Security Token Service");
						

						if (dynamicConfiguration != null
								&& dynamicConfiguration.getObject() != null) {
							BindaasConfiguration configuration = dynamicConfiguration
									.getObject();
							String publishUrl = "http://"
									+ configuration.getHost() + ":"
									+ configuration.getPort();
							testProps.put("edu.emory.cci.bindaas.commons.cxf.service.address", publishUrl + "/securityTokenService");
							context.registerService(
									IPsuedoSecurityTokenService.class, ref,
									testProps);
						}

						return null;
					}

					@Override
					public void modifiedService(ServiceReference arg0,
							Object arg1) {
						// do nothing

					}

					@Override
					public void removedService(ServiceReference arg0,
							Object arg1) {
						// do nothing

					}

				});

		serviceTracker.open();

	}

	@Override
	@GET
	@Path("/")
	public Response getAPIKey(
			@HeaderParam("Authorization") String authorizationHeader , @QueryParam("clientId") String clientId , @QueryParam("lifetime") Integer lifetime ) {
		try {
			@SuppressWarnings("unchecked")
			DynamicObject<BindaasConfiguration> dynaBc = getConfiguration("bindaas");
			@SuppressWarnings("unchecked")
			DynamicObject<BindaasAdminConsoleConfiguration> dynaAcc = getConfiguration("bindaas.adminconsole");
			
			if (dynaBc != null && dynaBc.getObject() != null && dynaAcc!=null && dynaAcc.getObject()!=null) {
				BindaasConfiguration configuration = dynaBc.getObject();
				BindaasAdminConsoleConfiguration adminConsoleConfiguration = dynaAcc.getObject();
				
				if (configuration.getEnableAuthentication() && adminConsoleConfiguration.getUserConfiguration().getAuthenticationMethod().equals(AuthenticationMethod.ldap) ) 
				{
					
					String[] userPass = getUsernamePassword(authorizationHeader.replace("Basic ", ""));
					
					IAuthenticationProvider authProvider = getAuthenticationProvider();
					if(authProvider!=null)
					{
						try{
						
							BindaasUser bindaasUser = authProvider.login(userPass[0] , userPass[1]);
							bindaasUser = new BindaasUser(userPass[0]);
							int lifespan = lifetime!=null ? lifetime : this.dynamicConfiguration.getObject().getDefaultLifespanOfKeysInSeconds();
							clientId = clientId!=null ? clientId : this.dynamicConfiguration.getObject().getDefaultClientId() ;
							
							APIKey sessionKey = generateApiKey(bindaasUser, lifespan, clientId);
							JsonObject retVal = new JsonObject();
							retVal.add("api_key", new JsonPrimitive(sessionKey.getValue()));
							retVal.add("clientId", new JsonPrimitive(clientId));
							retVal.add("expires", new JsonPrimitive(sessionKey.getExpires().toString()));
							
							return Response.ok().entity(retVal.toString()).type("application/json").build();
						}
						catch(AuthenticationException e)
						{
							throw e;
						}
						catch(Exception e)
						{
							throw new AuthenticationException(e);
						}
						
					}
					else throw new Exception("Authentication Service not available");
					
				}
				else throw new AuthenticationMethodNotEnabled("Server not configured to use LDAP for authentication");
			} else {

				throw new Exception("Unable to obtain BindaasConfiguration");
			}
		} 
		catch(AuthenticationMethodNotEnabled authne)
		{
			log.error(authne); // 401
			return Response.status(403).entity(authne.getMessage()).build();
		}
		catch(AuthenticationException authE)
		{
			log.error(authE); // 401
			return Response.status(401).build();
		}
		catch (Exception e) {
			log.error(e);
			return Response.serverError().build();
			// error 500
		}

	}
	private String[] getUsernamePassword(String credentials) throws  AuthenticationException
	{
		try {
		 if(credentials == null)  throw new AuthenticationException("Credential null");
		 String userPass = new String(javax.xml.bind.DatatypeConverter.parseBase64Binary(credentials));
		 int p = userPass.indexOf(":");
         if (p != -1) {
            String userID = userPass.substring(0, p);
            String  password = userPass.substring(p+1);

            // Validate user ID and password
            // and set valid true true if valid.
            // In this example, we simply check
            // that neither field is blank

            if ((!userID.trim().equals("")) &&
                (!password.trim().equals(""))) {
            	return new String[]{userID,password};
            }
         }
		}catch(Exception e){}
		
		throw new AuthenticationException();
	}
	private IAuthenticationProvider getAuthenticationProvider() throws Exception
	{
		BundleContext context = Activator.getContext();
		Collection<ServiceReference<IAuthenticationProvider>> srfs = context.getServiceReferences( IAuthenticationProvider.class , String.format("(class=%s)", this.dynamicConfiguration.getObject().getLdapProviderClass()));
		if(srfs!=null && srfs.size() > 0 )
		{
			IAuthenticationProvider authProvider = context.getService(srfs.iterator().next());
			return authProvider;
		}
		else
			return null;
	}

	@SuppressWarnings("rawtypes")
	private DynamicObject getConfiguration(String name)
			throws InvalidSyntaxException {
		BundleContext context = Activator.getContext();
		
		Collection<ServiceReference<DynamicObject>> srfs = context
				.getServiceReferences(DynamicObject.class,
						String.format("(name=%s)", name));
		if (srfs != null && srfs.size() > 0) {
			return context.getService(srfs.iterator().next());
		}
		return null;
	}
	
	
	
	
	private APIKey generateApiKey(BindaasUser principal , Integer lifespan , String clientId) throws Exception {

		APIKey apiKey = apiKeyManager.createShortLivedAPIKey(principal, lifespan, clientId);
		return apiKey;
	}
	
	
	/**
	 * thrown when authentication fails
	 * @author nadir
	 *
	 */
	public static class AuthenticationException extends Exception
	{

		private static final long serialVersionUID = -5379694325793032340L;
		public AuthenticationException()
		{}
		public AuthenticationException(Throwable e)
		{
			super(e);
		}
		
		public AuthenticationException(String message)
		{
			super(message);
		}
	}
	
	public static class AuthenticationMethodNotEnabled extends Exception
	{
		public AuthenticationMethodNotEnabled(String message)
		{
			super(message);
		}
		private static final long serialVersionUID = -5379694325793032341L;
	}
	

}
