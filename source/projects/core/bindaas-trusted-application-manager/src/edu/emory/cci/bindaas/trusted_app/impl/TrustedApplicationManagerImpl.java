package edu.emory.cci.bindaas.trusted_app.impl;

import java.security.MessageDigest;
import java.util.Date;
import java.util.Dictionary;
import java.util.Hashtable;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.xml.bind.DatatypeConverter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import edu.emory.cci.bindaas.core.apikey.api.APIKey;
import edu.emory.cci.bindaas.core.apikey.api.IAPIKeyManager;
import edu.emory.cci.bindaas.core.config.BindaasConfiguration;
import edu.emory.cci.bindaas.core.model.hibernate.HistoryLog.ActivityType;
import edu.emory.cci.bindaas.core.util.DynamicObject;
import edu.emory.cci.bindaas.security.api.BindaasUser;
import edu.emory.cci.bindaas.trusted_app.TrustedApplicationRegistry;
import edu.emory.cci.bindaas.trusted_app.TrustedApplicationRegistry.TrustedApplicationEntry;
import edu.emory.cci.bindaas.trusted_app.api.ITrustedApplicationManager;
import edu.emory.cci.bindaas.trusted_app.bundle.Activator;

public class TrustedApplicationManagerImpl implements
                ITrustedApplicationManager {

        private Log log = LogFactory.getLog(getClass());
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

        /**
         * thrown when authentication fails
         * 
         * @author nadir
         * 
         */
        public static class AuthenticationException extends Exception {

                private static final long serialVersionUID = -5379694325793032340L;

                public AuthenticationException() {
                }

                public AuthenticationException(Throwable e) {
                        super(e);
                }

                public AuthenticationException(String message) {
                        super(message);
                }
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
                        try {

                                TrustedApplicationEntry trustedAppEntry = authenticateTrustedApplication(
                                                applicationID, salt, digest, username);

                                BindaasUser bindaasUser = new BindaasUser(username);
                                int lifespan = lifetime != null ? lifetime
                                                : DEFAULT_LIFESPAN_OF_KEY_IN_SECONDS;
                                String applicationName = trustedAppEntry.getName();
                                try {
                                        APIKey sessionKey = generateApiKey(bindaasUser, lifespan,
                                                        applicationName);
                                        JsonObject retVal = new JsonObject();
                                        retVal.add("api_key",
                                                        new JsonPrimitive(sessionKey.getValue()));
                                        retVal.add("username", new JsonPrimitive(username));
                                        retVal.add("applicationID",
                                                        new JsonPrimitive(applicationID));
                                        retVal.add("expires", new JsonPrimitive(sessionKey
                                                        .getExpires().toString()));
                                        retVal.add("applicationName", new JsonPrimitive(
                                                        applicationName));
                                        return Response.ok().entity(retVal.toString())
                                                        .type("application/json").build();
                                } catch (AuthenticationException e) {
                                        JsonObject retVal = new JsonObject();
                                        retVal.add("applicationID",
                                                        new JsonPrimitive(applicationID));
                                        retVal.add("applicationName", new JsonPrimitive(
                                                        applicationName));
                                        retVal.add("username", new JsonPrimitive(username));
                                        retVal.add("error", new JsonPrimitive(e.getMessage()));
                                        return Response.status(401).entity(retVal.toString())
                                                        .type("application/json").build();
                                }
                        } catch (AuthenticationException e) {
                                throw e;
                        } catch (Exception e) {
                                throw new AuthenticationException(e);
                        }
                }

                catch (AuthenticationException authE) {
                        log.error(authE); // 401
                        return Response.status(401).build();
                } catch (Exception e) {
                        log.error(e);
                        return Response.serverError().build();
                        // error 500
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
                        throws Exception {
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
                                throw new AuthenticationException(
                                                "Failed to authenticate Trusted Application applicationID=["
                                                                + applicationID + "] . Wrong digest value");

                } else {
                        throw new AuthenticationException(
                                        "No TrustedApplication found for applicationID=["
                                                        + applicationID + "]");
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

                        } catch (AuthenticationException e) {
                                throw e;
                        } catch (Exception e) {
                                throw new AuthenticationException(e);
                        }
                }

                catch (AuthenticationException authE) {
                        log.error(authE); // 401
                        return Response.status(401).build();
                } catch (Exception e) {
                        log.error(e);
                        return Response.serverError().build();
                        // error 500
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
                        try {

                                TrustedApplicationEntry trustedAppEntry = authenticateTrustedApplication(
                                                applicationID, salt, digest, username);

                                comments = comments == null ? comments = "API Key Revoked via Trusted Application API"
                                                : comments;
                                Integer count = apiKeyManager.revokeAPIKey(new BindaasUser(
                                                username), trustedAppEntry.getName(), comments,
                                                ActivityType.REVOKE);

                                JsonObject retVal = new JsonObject();
                                retVal.add("username", new JsonPrimitive(username));
                                retVal.add("keys_deleted", new JsonPrimitive(count));
                                retVal.add("applicationID", new JsonPrimitive(applicationID));

                                retVal.add("applicationName",
                                                new JsonPrimitive(trustedAppEntry.getName()));
                                return Response.ok().entity(retVal.toString())
                                                .type("application/json").build();

                        } catch (AuthenticationException e) {
                                throw e;
                        } catch (Exception e) {
                                throw new AuthenticationException(e);
                        }
                }

                catch (AuthenticationException authE) {
                        log.error(authE); // 401
                        return Response.status(401).build();
                } catch (Exception e) {
                        log.error(e);
                        return Response.serverError().build();
                        // error 500
                }

        }

}