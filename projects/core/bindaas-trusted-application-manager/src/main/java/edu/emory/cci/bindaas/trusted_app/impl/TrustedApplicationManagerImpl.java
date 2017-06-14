package edu.emory.cci.bindaas.trusted_app.impl;

import java.security.MessageDigest;
import java.util.Date;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.xml.bind.DatatypeConverter;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import edu.emory.cci.bindaas.core.apikey.api.APIKey;
import edu.emory.cci.bindaas.core.apikey.api.APIKeyManagerException;
import edu.emory.cci.bindaas.core.apikey.api.IAPIKeyManager;
import edu.emory.cci.bindaas.core.config.BindaasConfiguration;
import edu.emory.cci.bindaas.core.model.hibernate.HistoryLog.ActivityType;
import edu.emory.cci.bindaas.core.model.hibernate.UserRequest;
import edu.emory.cci.bindaas.core.util.DynamicObject;
import edu.emory.cci.bindaas.framework.provider.exception.AbstractHttpCodeException;
import edu.emory.cci.bindaas.framework.util.GSONUtil;
import edu.emory.cci.bindaas.security.api.BindaasUser;
import edu.emory.cci.bindaas.trusted_app.TrustedApplicationRegistry;
import edu.emory.cci.bindaas.trusted_app.TrustedApplicationRegistry.TrustedApplicationEntry;
import edu.emory.cci.bindaas.trusted_app.api.ITrustedApplicationManager;
import edu.emory.cci.bindaas.trusted_app.bundle.Activator;
import edu.emory.cci.bindaas.trusted_app.exception.APIKeyDoesNotExistException;
import edu.emory.cci.bindaas.trusted_app.exception.DuplicateAPIKeyException;
import edu.emory.cci.bindaas.trusted_app.exception.NotAuthorizedException;

public class TrustedApplicationManagerImpl implements
		ITrustedApplicationManager {

	private final static Integer DEFAULT_LIFESPAN_OF_KEY_IN_SECONDS = 3600;
	private TrustedApplicationRegistry defaultTrustedApplications;
	private DynamicObject<TrustedApplicationRegistry> trustedApplicationRegistry;
	private final static long ROUNDOFF_FACTOR = 100000;
	private IAPIKeyManager apiKeyManager;

	public IAPIKeyManager getApiKeyManager() {
		return apiKeyManager;
	}

	public void setApiKeyManager(IAPIKeyManager apiKeyManager) {
		this.apiKeyManager = apiKeyManager;
	}

	public TrustedApplicationRegistry getDefaultTrustedApplications() {
		return defaultTrustedApplications;
	}

	public void setDefaultTrustedApplications(
			TrustedApplicationRegistry defaultTrustedApplications) {
		this.defaultTrustedApplications = defaultTrustedApplications;
	}

	public void init() throws Exception {

		final BundleContext context = Activator.getContext();
		trustedApplicationRegistry = new DynamicObject<TrustedApplicationRegistry>(
				"trusted-applications", defaultTrustedApplications, context);

		String filterExpression = "(&(objectclass=edu.emory.cci.bindaas.core.util.DynamicObject)(name=bindaas))";
		Filter filter = FrameworkUtil.createFilter(filterExpression);

		final ITrustedApplicationManager ref = this;
		@SuppressWarnings({ "rawtypes", "unchecked" })
		ServiceTracker<?, ?> serviceTracker = new ServiceTracker(context,
				filter, new ServiceTrackerCustomizer() {

					@Override
					public Object addingService(ServiceReference srf) {
						@SuppressWarnings("unchecked")
						DynamicObject<BindaasConfiguration> dynamicConfiguration = (DynamicObject<BindaasConfiguration>) context
								.getService(srf);
						Dictionary<String, Object> testProps = new Hashtable<String, Object>();
						testProps
								.put("edu.emory.cci.bindaas.commons.cxf.service.name",
										"Trusted Application Manager");

						if (dynamicConfiguration != null
								&& dynamicConfiguration.getObject() != null) {
							BindaasConfiguration configuration = dynamicConfiguration
									.getObject();
							String publishUrl = "http://"
									+ configuration.getHost() + ":"
									+ configuration.getPort();
							testProps
									.put("edu.emory.cci.bindaas.commons.cxf.service.address",
											publishUrl + "/trustedApplication");
							context.registerService(
									ITrustedApplicationManager.class, ref,
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

	public DynamicObject<TrustedApplicationRegistry> getTrustedApplicationRegistry() {
		return trustedApplicationRegistry;
	}

	public void setTrustedApplicationRegistry(
			DynamicObject<TrustedApplicationRegistry> trustedApplicationRegistry) {
		this.trustedApplicationRegistry = trustedApplicationRegistry;
	}

	private APIKey generateApiKey(BindaasUser principal, Integer lifespan,
			String clientId) throws Exception {

		APIKey apiKey = apiKeyManager.createShortLivedAPIKey(principal,
				lifespan, clientId);
		return apiKey;
	}

	private Response exceptionToResponse(AbstractHttpCodeException exception,
			String applicationID, String applicationName, String username) {
		JsonObject retVal = new JsonObject();
		retVal.add("applicationID", new JsonPrimitive(applicationID));
		retVal.add("applicationName", new JsonPrimitive(applicationName));
		retVal.add("username", new JsonPrimitive(username));
		retVal.add("error", exception.toJson());
		return Response.status(exception.getHttpStatusCode())
				.entity(retVal.toString()).type("application/json").build();
	}

	@Override
	@GET
	@Path("/issueShortLivedApiKey")
	public Response getAPIKey(@HeaderParam("_username") String username,
			@HeaderParam("_applicationID") String applicationID,
			@HeaderParam("_salt") String salt,
			@HeaderParam("_digest") String digest,
			@QueryParam("lifetime") Integer lifetime) {

		try {

			TrustedApplicationEntry trustedAppEntry = authenticateTrustedApplication(
					applicationID, salt, digest, username);

			BindaasUser bindaasUser = new BindaasUser(username);
			int lifespan = lifetime != null ? lifetime
					: DEFAULT_LIFESPAN_OF_KEY_IN_SECONDS;
			String applicationName = trustedAppEntry.getName();

			APIKey sessionKey = generateApiKey(bindaasUser, lifespan,
					applicationName);
			JsonObject retVal = new JsonObject();
			retVal.add("api_key", new JsonPrimitive(sessionKey.getValue()));
			retVal.add("username", new JsonPrimitive(username));
			retVal.add("applicationID", new JsonPrimitive(applicationID));
			retVal.add("expires", new JsonPrimitive(sessionKey.getExpires()
					.toString()));
			retVal.add("applicationName", new JsonPrimitive(applicationName));
			return Response.ok().entity(retVal.toString())
					.type("application/json").build();

		} catch (NotAuthorizedException e) {
			return exceptionToResponse(e, applicationID, "not-resolved",
					username);

		} catch (APIKeyManagerException apiKeyManagerException) {
			switch (apiKeyManagerException.getReason()) {
			case KEY_ALREADY_EXIST:
				return exceptionToResponse(new DuplicateAPIKeyException(
						username), applicationID, "not-resolved", username);
			case KEY_DOES_NOT_EXIST:
				return exceptionToResponse(new APIKeyDoesNotExistException(
						username), applicationID, "not-resolved", username);
			default:
				return Response.status(500)
						.entity(apiKeyManagerException.getMessage()).build();
			}
		} catch (Exception e) {
			return Response.status(500).entity(e.getMessage()).build();
		}

	}

	public static String calculateDigestValue(String applicationID,
			String applicationKey, String salt, String username)
			throws Exception {
		long roundoff = System.currentTimeMillis() / ROUNDOFF_FACTOR;
		String prenonce = roundoff + "|" + applicationKey;
		byte[] nonceBytes = MessageDigest.getInstance("SHA-1").digest(
				prenonce.getBytes("UTF-8"));
		String nonce = DatatypeConverter.printBase64Binary(nonceBytes);

		String predigest = String.format("%s|%s|%s|%s", username, nonce,
				applicationID, salt);
		String digest = DatatypeConverter.printBase64Binary(MessageDigest
				.getInstance("SHA-1").digest(predigest.getBytes("UTF-8")));

		return digest;
	}

	private TrustedApplicationEntry authenticateTrustedApplication(
			String applicationID, String salt, String digest, String username)
			throws NotAuthorizedException {
		try {
			TrustedApplicationRegistry registry = trustedApplicationRegistry
					.getObject();
			TrustedApplicationEntry entry = registry.lookup(applicationID);
			if (entry != null) {
				String applicationKey = entry.getApplicationKey();
				String caclulatedDigest = calculateDigestValue(applicationID,
						applicationKey, salt, username);
				if (caclulatedDigest.equals(digest)) {
					return entry;
				} else
					throw new NotAuthorizedException(
							"Failed to authenticate Trusted Application applicationID=["
									+ applicationID + "] . Wrong digest value");

			} else {
				throw new NotAuthorizedException(
						"No TrustedApplication found for applicationID=["
								+ applicationID + "]");
			}

		} catch (NotAuthorizedException e) {
			throw e;
		} catch (Exception e) {
			throw new NotAuthorizedException(e.getMessage());
		}

	}

	@Override
	@GET
	@Path("/authorizeUser")
	public Response authorizeNewUser(@HeaderParam("_username") String username,
			@HeaderParam("_applicationID") String applicationID,
			@HeaderParam("_salt") String salt,
			@HeaderParam("_digest") String digest,
			@QueryParam("expires") Long epochTime,
			@QueryParam("comments") String comments) {

		try {

			TrustedApplicationEntry trustedAppEntry = authenticateTrustedApplication(
					applicationID, salt, digest, username);
			Date dateExpires = new Date(epochTime);
			comments = comments == null ? comments = "API Key Generated via Trusted Application API"
					: comments;
			APIKey apiKey = apiKeyManager.generateAPIKey(new BindaasUser(
					username), dateExpires, trustedAppEntry.getName(),
					comments, ActivityType.APPROVE, true);

			JsonObject retVal = new JsonObject();
			retVal.add("api_key", new JsonPrimitive(apiKey.getValue()));
			retVal.add("username", new JsonPrimitive(username));
			retVal.add("applicationID", new JsonPrimitive(applicationID));
			retVal.add("expires", new JsonPrimitive(apiKey.getExpires()
					.toString()));
			retVal.add("applicationName",
					new JsonPrimitive(trustedAppEntry.getName()));
			return Response.ok().entity(retVal.toString())
					.type("application/json").build();

		} catch (NotAuthorizedException e) {
			return exceptionToResponse(e, applicationID, "not-resolved",
					username);

		} catch (APIKeyManagerException apiKeyManagerException) {
			switch (apiKeyManagerException.getReason()) {
			case KEY_ALREADY_EXIST:
				return exceptionToResponse(new DuplicateAPIKeyException(
						username), applicationID, "not-resolved", username);
			case KEY_DOES_NOT_EXIST:
				return exceptionToResponse(new APIKeyDoesNotExistException(
						username), applicationID, "not-resolved", username);
			default:
				return Response.status(500)
						.entity(apiKeyManagerException.getMessage()).build();
			}
		} catch (Exception e) {
			return Response.status(500).entity(e.getMessage()).build();
		}

	}

	@Override
	@DELETE
	@Path("/revokeUser")
	public Response revokeAccess(@HeaderParam("_username") String username,
			@HeaderParam("_applicationID") String applicationID,
			@HeaderParam("_salt") String salt,
			@HeaderParam("_digest") String digest,
			@QueryParam("comments") String comments) {
		try {
			TrustedApplicationEntry trustedAppEntry = authenticateTrustedApplication(
					applicationID, salt, digest, username);

			comments = comments == null ? comments = "API Key Revoked via Trusted Application API"
					: comments;
			Integer count = apiKeyManager.revokeAPIKey(
					new BindaasUser(username), trustedAppEntry.getName(),
					comments, ActivityType.REVOKE);

			JsonObject retVal = new JsonObject();
			retVal.add("username", new JsonPrimitive(username));
			retVal.add("keys_deleted", new JsonPrimitive(count));
			retVal.add("applicationID", new JsonPrimitive(applicationID));

			retVal.add("applicationName",
					new JsonPrimitive(trustedAppEntry.getName()));
			return Response.ok().entity(retVal.toString())
					.type("application/json").build();

		} catch (NotAuthorizedException e) {
			return exceptionToResponse(e, applicationID, "not-resolved",
					username);

		} catch (APIKeyManagerException apiKeyManagerException) {
			switch (apiKeyManagerException.getReason()) {
			case KEY_ALREADY_EXIST:
				return exceptionToResponse(new DuplicateAPIKeyException(
						username), applicationID, "not-resolved", username);
			case KEY_DOES_NOT_EXIST:
				return exceptionToResponse(new APIKeyDoesNotExistException(
						username), applicationID, "not-resolved", username);
			default:
				return Response.status(500)
						.entity(apiKeyManagerException.getMessage()).build();
			}
		} catch (Exception e) {
			return Response.status(500).entity(e.getMessage()).build();
		}

	}

	@Override
	@GET
	@Path("/listAPIkeys")
	public Response listAPIKeys(@HeaderParam("_username") String username,
			@HeaderParam("_applicationID") String applicationID,
			@HeaderParam("_salt") String salt,
			@HeaderParam("_digest") String digest) {

		try {

			TrustedApplicationEntry trustedAppEntry = authenticateTrustedApplication(
					applicationID, salt, digest, username);

			List<UserRequest> listOfApiKeys = apiKeyManager.listAPIKeys();
			JsonArray array = GSONUtil.getGSONInstance()
					.toJsonTree(listOfApiKeys).getAsJsonArray();

			JsonObject retVal = new JsonObject();

			retVal.add("apiKeys", array);
			retVal.add("applicationID", new JsonPrimitive(applicationID));
			retVal.add("applicationName",
					new JsonPrimitive(trustedAppEntry.getName()));
			return Response.ok().entity(retVal.toString())
					.type("application/json").build();

		} catch (NotAuthorizedException e) {
			return exceptionToResponse(e, applicationID, "not-resolved",
					username);

		} catch (APIKeyManagerException apiKeyManagerException) {
			switch (apiKeyManagerException.getReason()) {
			case KEY_ALREADY_EXIST:
				return exceptionToResponse(new DuplicateAPIKeyException(
						username), applicationID, "not-resolved", username);
			case KEY_DOES_NOT_EXIST:
				return exceptionToResponse(new APIKeyDoesNotExistException(
						username), applicationID, "not-resolved", username);
			default:
				return Response.status(500)
						.entity(apiKeyManagerException.getMessage()).build();
			}
		} catch (Exception e) {
			return Response.status(500).entity(e.getMessage()).build();
		}

	}

}