package edu.emory.cci.bindaas.core.rest.security;

import java.security.Principal;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import javax.security.auth.Subject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.configuration.security.AuthorizationPolicy;
import org.apache.cxf.interceptor.security.DefaultSecurityContext;
import org.apache.cxf.jaxrs.ext.RequestHandler;
import org.apache.cxf.jaxrs.model.ClassResourceInfo;
import org.apache.cxf.jaxrs.utils.JAXRSUtils;
import org.apache.cxf.message.Message;
import org.apache.cxf.security.SecurityContext;
import org.apache.cxf.transport.http.AbstractHTTPDestination;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import edu.emory.cci.bindaas.core.api.ISecurityHandler;
import edu.emory.cci.bindaas.core.bundle.Activator;
import edu.emory.cci.bindaas.core.config.BindaasConfiguration;
import edu.emory.cci.bindaas.core.util.DynamicObject;
import edu.emory.cci.bindaas.security.api.AuthenticationException;
import edu.emory.cci.bindaas.security.api.IAuthenticationProvider;
import edu.emory.cci.bindaas.security.api.IAuthorizationProvider;


public class SecurityHandler implements RequestHandler,ISecurityHandler {
	private Log log = LogFactory.getLog(getClass());
	private static final Long DECISION_CACHE_MAX = 1000l;
	private static final Long DECISION_CACHE_TIMEOUT_MINUTES = 60l;
	private Cache<String,AuthenticationResponseEntry> authenticationDecisionCache;
	private Cache<AuthorizationRequestEntry ,Boolean> authorizationDecisionCache;
	
	private String authenticationProviderClass;
	private String authorizationProviderClass;
	
	private AuthenticationProtocol authenticationProtocol = AuthenticationProtocol.API_KEY; // default
	public final static String TOKEN = "token";
	public final static String API_KEY = "api_key";
	private ServiceTracker<DynamicObject<BindaasConfiguration>,DynamicObject<BindaasConfiguration>> bindaasConfigServiceTracker;
	
	
	public AuthenticationProtocol getAuthenticationProtocol() {
		return authenticationProtocol;
	}
	public void setAuthenticationProtocol(
			AuthenticationProtocol authenticationProtocol) {
		this.authenticationProtocol = authenticationProtocol;
	}
	public void setRequestId(Message message)
	{
		message.put("requestId", UUID.randomUUID().toString());
	}
	public void init() throws Exception
	{
		String filterExpression = "(&(objectclass=edu.emory.cci.bindaas.core.util.DynamicObject)(name=bindaas))";
		Filter filter = FrameworkUtil.createFilter(filterExpression);
		bindaasConfigServiceTracker = new ServiceTracker <DynamicObject<BindaasConfiguration>,DynamicObject<BindaasConfiguration>>(Activator.getContext(),filter, new ServiceTrackerCustomizer<DynamicObject<BindaasConfiguration>, DynamicObject<BindaasConfiguration>>() {

			@Override
			public DynamicObject<BindaasConfiguration> addingService(
					ServiceReference<DynamicObject<BindaasConfiguration>> arg0) {
				return	Activator.getContext().getService(arg0);
				
			}

			@Override
			public void modifiedService(
					ServiceReference<DynamicObject<BindaasConfiguration>> arg0,
					DynamicObject<BindaasConfiguration> arg1) {
				
			}

			@Override
			public void removedService(
					ServiceReference<DynamicObject<BindaasConfiguration>> arg0,
					DynamicObject<BindaasConfiguration> arg1) {

			}
		});
		bindaasConfigServiceTracker.open();
		
		// initialize caches
		authenticationDecisionCache = CacheBuilder.newBuilder().expireAfterAccess(DECISION_CACHE_TIMEOUT_MINUTES, TimeUnit.MINUTES).maximumSize(DECISION_CACHE_MAX).build();
		authorizationDecisionCache = CacheBuilder.newBuilder().expireAfterAccess(DECISION_CACHE_TIMEOUT_MINUTES, TimeUnit.MINUTES).maximumSize(DECISION_CACHE_MAX).build();
	}
	
	private Principal handleHTTP_BASIC(Message message , IAuthenticationProvider authenticationProvider) throws Exception
	{
		String[] usernamePassword = extractUsernamePassword(message);
		if(usernamePassword != null && authenticationProvider.isAuthenticationByUsernamePasswordSupported())
		{
			try {
				Principal authenticatedUser = authenticationProvider.login(usernamePassword[0], usernamePassword[1] );
				return authenticatedUser;
			} catch (AuthenticationException e) {
					log.error(e); // authentication failed
					throw e;
					
			}
			catch(Exception e)
			{
				log.error(e);
				throw e;
			}
		}
		else
		{
			if(usernamePassword == null) throw new AuthenticationException("");
			else
				throw new Exception("Authentication module does not support authentication protocol [HTTP Basic]");
		}
		
	}
	
	private Principal handleSecurityToken(Message message,IAuthenticationProvider authenticationProvider) throws Exception
	{
		String securityToken = extractSecurityToken(message);
		if(securityToken!=null && authenticationProvider.isAuthenticationBySecurityTokenSupported())
		{
			try {
				Principal authenticatedUser = authenticationProvider.login(securityToken );
				return authenticatedUser;
			} catch (AuthenticationException e) {
					log.error(e); // authentication failed
					throw e;
			}
			catch(Exception e)
			{
				log.error(e);
				throw e;
			}
		
		}
		else
		{
			throw new Exception("Security Token must be provided for authentication");
		}
			
	}
	
	private Principal handleAPI_KEY(Message message,final IAuthenticationProvider authenticationProvider) throws Exception
	{
		String apiKey = null;
		
		// get apiKey from the query parameters
		MultivaluedMap<String, String> queryMap =  JAXRSUtils.getStructuredParams((String) message.get(Message.QUERY_STRING), "&", true , true);
		
		if(queryMap!=null && queryMap.getFirst(API_KEY)!=null)
			apiKey = queryMap.getFirst(API_KEY);
		
		// if not present in query param , then look into http header
		
		if(apiKey == null)
		{
			Map<?,?> protocolHeaders = (Map<?,?>) message.get(Message.PROTOCOL_HEADERS);
			if(protocolHeaders!=null && protocolHeaders.get(API_KEY)!=null)
			{
				List<?> values = (List<?>) protocolHeaders.get(API_KEY);
				if(values!=null && values.size() > 0)
				{
					apiKey = values.get(0).toString();
				}
				
			}
		}
			
		if(apiKey != null)
		{
			try {
				 final String apiK = apiKey;
				 AuthenticationResponseEntry responseEntry = authenticationDecisionCache.get(apiKey, new Callable<AuthenticationResponseEntry>() {

					@Override
					public AuthenticationResponseEntry call() throws Exception {
						AuthenticationResponseEntry responseEntry = new AuthenticationResponseEntry();
						try {
						
							Principal authenticatedUser = authenticationProvider.loginUsingAPIKey( apiK );
							responseEntry.setDecision(true);
							responseEntry.setPrincipal(authenticatedUser);
						}catch(AuthenticationException authException)
						{
							responseEntry.setDecision(false);
						}
						
						return responseEntry;
					}
				});
				
				 if(responseEntry.getDecision().equals(true))
				 {
					 return responseEntry.getPrincipal();
				 }
				 else
					 throw new AuthenticationException(apiKey);
				 
			} 
			catch(Exception e)
			{
				log.error(e);
				throw e;
			}
		}
		else
		{
			throw new AuthenticationException();
		}
		
		
	}
	
	

	@Override
 	public Response handleRequest(Message message, ClassResourceInfo arg1) {
		setRequestId(message);
		Principal authenticatedUser =  null;
		if(isEnableAuthentication())
		{
			IAuthenticationProvider authenticationProvider = locateAuthenticationProvider();
			if(authenticationProvider == null)
			{
				// authenticationProvider not found
				log.error("Unable to locate IAuthenticationProvider");
				return Response.serverError().entity("Error communicating with Authentication Module").build();
			}
			
			try {
			
				switch(authenticationProtocol)
				{
						default : 
						case HTTP_BASIC : authenticatedUser = handleHTTP_BASIC(message, authenticationProvider); break;
						case SECURITY_TOKEN : authenticatedUser = handleSecurityToken(message, authenticationProvider); break;
						case API_KEY : authenticatedUser = handleAPI_KEY(message, authenticationProvider); break;		
				}
				
			} 
			catch(AuthenticationException authException)
			{
				if(authenticationProtocol.equals(AuthenticationProtocol.HTTP_BASIC))
					return Response.status(401).header("WWW-Authenticate", "Basic").build();
				else
					return Response.status(401).build();
			}
			catch(Exception e)
			{
				return Response.serverError().entity("Error in Authentication Module").build();
			}
			
			
			// at this stage user is authenticated and principal is set
			
			if(isEnableAuthorization())
			{
				IAuthorizationProvider authorizationProvider = locateAuthorizationProvider();
				if(authorizationProvider == null)
				{
					// authorizationProvider not found
					log.error("Unable to locate IAuthorizationProvider");
					return Response.serverError().entity("Error communicating with Authorization Module").build();
				}
				try{
					log.debug("Performing Authorization");
					boolean grantAccess = performAuthorization( message , authenticatedUser , authorizationProvider);
						if(!grantAccess)
						{
							return Response.status(
									Response.Status.UNAUTHORIZED.getStatusCode())
									.build();
						}
					}
				catch(Exception e)
				{
					log.error(e);
					return Response.serverError().entity("Error in Authorization Module").build();
				}
				
			}
		}		
		
		setSecurityContext(message , authenticatedUser);
		
		return null;
	}
	
	public boolean isEnableAuthorization() {
		DynamicObject<BindaasConfiguration> bindaasConfig = (DynamicObject<BindaasConfiguration>) bindaasConfigServiceTracker.getService();
		if(bindaasConfig!=null)
		{
			return bindaasConfig.getObject().getEnableAuthorization();
		}
		else
		{
			log.fatal("BindaasConfiguration not available");
			return false;
		}
	}


	


	public boolean isEnableAuthentication() {
		 
		DynamicObject<BindaasConfiguration> bindaasConfig = (DynamicObject<BindaasConfiguration>) bindaasConfigServiceTracker.getService();
		if(bindaasConfig!=null)
		{
			return bindaasConfig.getObject().getEnableAuthentication();
		}
		else
		{
			log.fatal("BindaasConfiguration not available");
			return false;
		}
		
	}



	


	private void setSecurityContext(Message message, Principal authenticatedUser) {
		if(authenticatedUser!=null)
		{
			message.put(SecurityContext.class,
					createSecurityContext(authenticatedUser.getName()));
		}
		else
		{
			message.put(SecurityContext.class,
					createSecurityContext("anonymous"));
		}
		
	}

	public SecurityContext createSecurityContext(final String username) {
		Principal principal =  new Principal() {
			
			public String getName() {
				
				return username;
			}
			
			public String toString()
			{
				return username;
			}
			
			public boolean equals(Object o)
			{
				if(o instanceof Principal)
				{
					if(((Principal)o).getName().equals(username)) return true;
				}
				return false;
			}
		};;
		Subject subject = new Subject();
		subject.getPrincipals().add(principal);
		return new DefaultSecurityContext(subject);
	}
	
	
	private boolean performAuthorization(Message message,
			final Principal authenticatedUser,
			final IAuthorizationProvider authorizationProvider) throws Exception {
		HttpServletRequest request = (HttpServletRequest) message
				.get(AbstractHTTPDestination.HTTP_REQUEST);
		
		final String pathInfo = (String) message.get("org.apache.cxf.request.url");
		
		
		final Map<String,String> userAttributes = new HashMap<String,String>();
		userAttributes.put(IAuthorizationProvider.IP_ADDRESS, request.getRemoteAddr());
		userAttributes.put(IAuthorizationProvider.TIME_OF_AUTHENTICATION, GregorianCalendar.getInstance().getTime().toString());
		
		final String actionId = request.getMethod();
		
		boolean result = authorizationDecisionCache.get(new AuthorizationRequestEntry(authenticatedUser.getName(), pathInfo), new Callable<Boolean>() {

			@Override
			public Boolean call() throws Exception {
				try{
				return authorizationProvider.isAuthorized(userAttributes, authenticatedUser.getName(), pathInfo, actionId);
				}catch(Exception e){
					log.error("Error in making authorization decision",e);
					throw e;
				}
			}
		});
		 
		return result;
	}


	private String extractSecurityToken(Message message) {
		Map<?,?> protocolHeaders = (Map<?,?>) message.get(Message.PROTOCOL_HEADERS);
		if (protocolHeaders.get(TOKEN) != null) {
			List<?> listOfValues = (List<?>) protocolHeaders.get(TOKEN);
			String token = (String) listOfValues.get(0);
			return token;
		}
		else
			return null;
	}


	private String[] extractUsernamePassword(Message message) {
		AuthorizationPolicy policy = (AuthorizationPolicy) message
				.get(AuthorizationPolicy.class);
		if (policy != null) {
			String username = policy.getUserName();
			String password = policy.getPassword();
			return new String[]{username,password};
		}
		else
		return null;
	}


	public IAuthenticationProvider locateAuthenticationProvider()
	{
		
		final BundleContext context = Activator.getContext();
		@SuppressWarnings("rawtypes")
		ServiceReference[] serviceReferences;
		try {
			serviceReferences = context.getAllServiceReferences(IAuthenticationProvider.class.getName(), "(class=" +  authenticationProviderClass +")");
			if(serviceReferences!=null && serviceReferences.length > 0)
			{
				@SuppressWarnings("unchecked")
				Object service = context.getService(serviceReferences[0]);
				if(service!=null)
				{
					return (IAuthenticationProvider) service; 
				}
			}
		} catch (InvalidSyntaxException e) {
			log.error(e);
			
		}
		
		return null;
		
	}
	
	public IAuthorizationProvider locateAuthorizationProvider()
	{
		final BundleContext context = Activator.getContext();
		@SuppressWarnings("rawtypes")
		ServiceReference[] serviceReferences;
		try {
			serviceReferences = context.getAllServiceReferences(IAuthorizationProvider.class.getName(), "(class=" +  authorizationProviderClass +")");
			if(serviceReferences.length > 0)
			{
				@SuppressWarnings("unchecked")
				Object service = context.getService(serviceReferences[0]);
				if(service!=null)
				{
					return (IAuthorizationProvider) service; 
				}
			}
		} catch (InvalidSyntaxException e) {
			log.error(e);
			
		}
		
		return null;
		
	}


	public String getAuthenticationProviderClass() {
		return authenticationProviderClass;
	}


	public void setAuthenticationProviderClass(String authenticationProviderClass) {
		this.authenticationProviderClass = authenticationProviderClass;
	}


	public String getAuthorizationProviderClass() {
		return authorizationProviderClass;
	}


	public void setAuthorizationProviderClass(String authorizationProviderClass) {
		this.authorizationProviderClass = authorizationProviderClass;
	}
	
	
	private static  class AuthorizationRequestEntry {
		private String username;
		private String resource;
		
		public AuthorizationRequestEntry (String username , String resource)
		{
			this.username = username;
			this.resource = resource;
		}
		
		@Override
		public int hashCode() {
			return username.hashCode() + resource.hashCode();
		}


		public boolean equals(Object o)
		{
			if(o instanceof AuthorizationRequestEntry)
			{
				AuthorizationRequestEntry r = (AuthorizationRequestEntry) o;
				if(r.username.equals(username)  && r.resource.equals(resource)) return true;
			}
			
			return false;
		}
	}
	
	private static class AuthenticationResponseEntry {
		private Principal principal;
		private Boolean decision;
		public Principal getPrincipal() {
			return principal;
		}
		public void setPrincipal(Principal principal) {
			this.principal = principal;
		}
		public Boolean getDecision() {
			return decision;
		}
		public void setDecision(Boolean decision) {
			this.decision = decision;
		}
	}

}
