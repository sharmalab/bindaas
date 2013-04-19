package edu.emory.cci.bindaas.pseudosts.impl;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Dictionary;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import edu.emory.cci.bindaas.core.config.BindaasConfiguration;
import edu.emory.cci.bindaas.core.model.hibernate.HistoryLog;
import edu.emory.cci.bindaas.core.model.hibernate.UserRequest;
import edu.emory.cci.bindaas.core.util.DynamicObject;
import edu.emory.cci.bindaas.pseudosts.api.IPsuedoSecurityTokenService;
import edu.emory.cci.bindaas.pseudosts.bundle.Activator;
import edu.emory.cci.bindaas.security.api.BindaasUser;
import edu.emory.cci.bindaas.security.api.IAuthenticationProvider;
import edu.emory.cci.bindaas.webconsole.config.BindaasAdminConsoleConfiguration;
import edu.emory.cci.bindaas.webconsole.config.BindaasAdminConsoleConfiguration.UserConfiguration.AuthenticationMethod;

public class PseduoSecurityTokenServiceImpl implements
		IPsuedoSecurityTokenService {

	private Log log = LogFactory.getLog(getClass());
	private final static String DEFAULT_CLIENT_ID = "external.org";
	private final static Integer DEFAULT_LIFESPAN_OF_KEY_IN_SECONDS = 3600;
	
	public void init() throws InvalidSyntaxException {

		final BundleContext context = Activator.getContext();
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
						Dictionary<String, Object> cxfServiceProps = new Hashtable<String, Object>();
						cxfServiceProps.put("service.exported.interfaces", "*");
						cxfServiceProps.put("service.exported.intents", "HTTP");
						cxfServiceProps.put("service.exported.configs",
								"org.apache.cxf.rs");

						if (dynamicConfiguration != null
								&& dynamicConfiguration.getObject() != null) {
							BindaasConfiguration configuration = dynamicConfiguration
									.getObject();
							String publishUrl = "http://"
									+ configuration.getHost() + ":"
									+ configuration.getPort();
							cxfServiceProps.put("org.apache.cxf.rs.address",
									publishUrl + "/securityTokenService");
							context.registerService(
									IPsuedoSecurityTokenService.class, ref,
									cxfServiceProps);
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
							int lifespan = lifetime!=null ? lifetime : DEFAULT_LIFESPAN_OF_KEY_IN_SECONDS;
							clientId = clientId!=null ? clientId : DEFAULT_CLIENT_ID ;
							
							UserRequest sessionKey = generateApiKey(bindaasUser, lifespan, clientId);
							JsonObject retVal = new JsonObject();
							retVal.add("api_key", new JsonPrimitive(sessionKey.getApiKey()));
							retVal.add("clientId", new JsonPrimitive(clientId));
							retVal.add("expires", new JsonPrimitive(sessionKey.getDateExpires().toString()));
							
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
		Collection<ServiceReference<IAuthenticationProvider>> srfs = context.getServiceReferences( IAuthenticationProvider.class , "(class=edu.emory.cci.bindaas.security.ldap.LDAPAuthenticationProvider)");
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
	
	
	
	
	private UserRequest generateApiKey(BindaasUser principal , Integer lifespan , String clientId) throws Exception {

		SessionFactory sessionFactory = Activator
				.getService(SessionFactory.class);
		if (sessionFactory != null) {
			Session session = sessionFactory.openSession();
			try {
				session.beginTransaction();

				String emailAddress = principal
						.getProperty(BindaasUser.EMAIL_ADDRESS) != null ? principal
						.getProperty(BindaasUser.EMAIL_ADDRESS).toString()
						: principal.getName() + "@" + principal.getDomain();
				

				@SuppressWarnings("unchecked")
				List<UserRequest> listOfValidKeys = (List<UserRequest>) session
						.createCriteria(UserRequest.class)
						.add(Restrictions.eq("stage", "accepted"))
						.add(Restrictions.eq("emailAddress", emailAddress))
						.list();

				if (listOfValidKeys != null && listOfValidKeys.size() > 0) {
					UserRequest request = listOfValidKeys.get(0); 
					if (request.getDateExpires().after(new Date())) {

						// generate a short lived api-key for this user
						UserRequest sessionKey = new UserRequest();
						sessionKey.setApiKey(UUID.randomUUID().toString());
						sessionKey.setFirstName(request.getFirstName());
						sessionKey.setLastName(request.getLastName());
						sessionKey.setEmailAddress(principal.getName() + "@" + clientId  );
						sessionKey.setReason(request.getReason());
						sessionKey.setRequestDate(new Date());
						sessionKey.setStage("accepted");
						
						GregorianCalendar calendar = new GregorianCalendar();
						
						calendar.add(Calendar.SECOND , lifespan);
						Date newExpirationDate = request.getDateExpires().before(calendar.getTime()) ? request.getDateExpires() : calendar.getTime();
						sessionKey.setDateExpires(newExpirationDate);
						
						
						session.save(sessionKey);

						HistoryLog historyLog = new HistoryLog();
						historyLog.setActivityType("system-approve");
						historyLog.setComments("Auto generated by STS");
						historyLog.setInitiatedBy("system-STS");
						historyLog.setUserRequest(sessionKey);

						session.save(historyLog);
						session.getTransaction().commit();
						
						return sessionKey;
					} else {
						
						throw new AuthenticationException("The API Key of user [" + principal
								+ "] has expired. The User must reapply");
					}
				} else {
					throw new AuthenticationException("Cannot generate API-Key for [" + principal + "]. The User must apply for it first!!");
				}

			} catch (Exception e) {
				log.error(e);
				session.getTransaction().rollback();
				throw e;
			} finally {
				session.close();
			}

		}
		else
			throw new Exception("SessionFactory service not available");

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
