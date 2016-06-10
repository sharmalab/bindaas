package edu.emory.cci.bindaas.security_dashboard.util;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import com.google.gson.annotations.Expose;
import com.google.gson.reflect.TypeToken;

import edu.emory.cci.bindaas.core.apikey.api.APIKey;
import edu.emory.cci.bindaas.core.apikey.api.IAPIKeyManager;
import edu.emory.cci.bindaas.framework.util.GSONUtil;
import edu.emory.cci.bindaas.framework.util.IOUtils;
import edu.emory.cci.bindaas.security_dashboard.config.SecurityDashboardConfiguration;
import edu.emory.cci.bindaas.security_dashboard.model.Group;
import edu.emory.cci.bindaas.security_dashboard.model.User;

public class RakshakUtils {

	private static Log log = LogFactory.getLog(RakshakUtils.class);
	private static Type userSetType;
	private static Type groupSetType;

	static {
		userSetType = new TypeToken<Set<User>>() {
		}.getType();
		groupSetType = new TypeToken<Set<Group>>() {
		}.getType();

	}

	public static Set<User> getAllUsers(SecurityDashboardConfiguration config,
			IAPIKeyManager apiKeyManager) throws Exception {
		DefaultHttpClient httpClient = new DefaultHttpClient();
		try {

			String url = String.format("%s/extensions/getUsers",
					config.getRakshakBaseUrl());
			HttpGet getRequest = new HttpGet(url);
			HttpResponse resp = httpClient.execute(getRequest);
			InputStream is = null;

			if (resp.getStatusLine().getStatusCode() == 200
					&& resp.getEntity() != null
					&& (is = resp.getEntity().getContent()) != null) {
				String content = IOUtils.toString(is);
				Set<User> users = GSONUtil.getGSONInstance().fromJson(content,
						userSetType);
				for (User user : users) {
					APIKey apiKey = apiKeyManager.lookupAPIKeyByUsername(user
							.getName());
					if (apiKey != null) {
						user.setApiKey(apiKey.getValue());
						user.setExpirationDate(apiKey.getExpires().toString());
					}
				}

				return users;

			} else {

				String message = resp.getStatusLine().toString();
				if (resp.getEntity() != null
						&& (is = resp.getEntity().getContent()) != null) {
					message += "\n" + IOUtils.toString(is);
				}
				throw new Exception(message);
			}

		} catch (Exception e) {
			log.error(e);
			throw e;
		} finally {
			httpClient.getConnectionManager().closeExpiredConnections();
		}

	}

	public static Set<User> getUsersHavingAPIKey(
			SecurityDashboardConfiguration config, IAPIKeyManager apiKeyManager)
			throws Exception {
		DefaultHttpClient httpClient = new DefaultHttpClient();
		try {

			String url = String.format("%s/extensions/getUsers",
					config.getRakshakBaseUrl());
			HttpGet getRequest = new HttpGet(url);
			HttpResponse resp = httpClient.execute(getRequest);
			InputStream is = null;
			Set<User> retVal = new TreeSet<User>();

			if (resp.getStatusLine().getStatusCode() == 200
					&& resp.getEntity() != null
					&& (is = resp.getEntity().getContent()) != null) {
				String content = IOUtils.toString(is);
				Set<User> users = GSONUtil.getGSONInstance().fromJson(content,
						userSetType);
				for (User user : users) {
					APIKey apiKey = apiKeyManager.lookupAPIKeyByUsername(user
							.getName());
					if (apiKey != null) {
						user.setApiKey(apiKey.getValue());
						user.setExpirationDate(apiKey.getExpires().toString());
						retVal.add(user);
					}
				}

				return retVal;

			} else {
				String message = resp.getStatusLine().toString();
				if (resp.getEntity() != null
						&& (is = resp.getEntity().getContent()) != null) {
					message += "\n" + IOUtils.toString(is);
				}
				throw new Exception(message);
			}

		} catch (Exception e) {
			log.error(e);
			throw e;
		} finally {
			httpClient.getConnectionManager().closeExpiredConnections();
		}
	}

	public static Set<Group> getAllGroups(
			SecurityDashboardConfiguration config, IAPIKeyManager apiKeyManager)
			throws Exception {
		DefaultHttpClient httpClient = new DefaultHttpClient();
		try {

			String url = String.format("%s/extensions/getGroups",
					config.getRakshakBaseUrl());
			HttpGet getRequest = new HttpGet(url);
			HttpResponse resp = httpClient.execute(getRequest);
			InputStream is = null;

			if (resp.getStatusLine().getStatusCode() == 200
					&& resp.getEntity() != null
					&& (is = resp.getEntity().getContent()) != null) {
				String content = IOUtils.toString(is);
				Set<Group> groups = GSONUtil.getGSONInstance().fromJson(
						content, groupSetType);

				// filter users who do not have apiKey

				for (Group group : groups) {
					Set<String> users = group.getUsers();
					Iterator<String> iter = users.iterator();
					while (iter.hasNext()) {
						String user = iter.next();
						APIKey apiKey = apiKeyManager
								.lookupAPIKeyByUsername(user);
						if (apiKey == null)
							iter.remove();
					}
				}

				return groups;
			} else {
				String message = resp.getStatusLine().toString();
				if (resp.getEntity() != null
						&& (is = resp.getEntity().getContent()) != null) {
					message += "\n" + IOUtils.toString(is);
				}
				throw new Exception(message);
			}

		} catch (Exception e) {
			log.error(e);
			throw e;
		} finally {
			httpClient.getConnectionManager().closeExpiredConnections();
		}
	}

	public static void addNewGroup(SecurityDashboardConfiguration config,
			Set<String> users, String group, String description)
			throws Exception {
		DefaultHttpClient httpClient = new DefaultHttpClient();
		try {

			String url = String.format("%s/extensions/createOrModifyGroup",
					config.getRakshakBaseUrl());
			HttpPost postRequest = new HttpPost(url);
			CreateOrModifyGroupPayload createOrModifyGroupPayload = new CreateOrModifyGroupPayload();
			createOrModifyGroupPayload.createGroupIfNotExist = true;
			createOrModifyGroupPayload.groupDescription = description;
			createOrModifyGroupPayload.groupName = group;
			createOrModifyGroupPayload.users = users;

			String jsonized = GSONUtil.getGSONInstance().toJson(
					createOrModifyGroupPayload);
			postRequest.setEntity(new StringEntity(jsonized));
			HttpResponse resp = httpClient.execute(postRequest);

			if (resp.getStatusLine().getStatusCode() != 200) {
				throw new Exception(
						"Request to Rakshshak returned with error code ["
								+ resp.getStatusLine().getStatusCode() + "]");
			}

		} catch (Exception e) {
			log.error(e);
			throw e;
		} finally {
			httpClient.getConnectionManager().closeExpiredConnections();
		}
	}

	public static void addUsersToGroup(SecurityDashboardConfiguration config,
			Set<String> users, String group) throws Exception {
		DefaultHttpClient httpClient = new DefaultHttpClient();
		try {

			String url = String.format("%s/extensions/createOrModifyGroup",
					config.getRakshakBaseUrl());
			HttpPost postRequest = new HttpPost(url);
			CreateOrModifyGroupPayload createOrModifyGroupPayload = new CreateOrModifyGroupPayload();
			createOrModifyGroupPayload.createGroupIfNotExist = false;
			createOrModifyGroupPayload.groupName = group;
			createOrModifyGroupPayload.users = users;

			String jsonized = GSONUtil.getGSONInstance().toJson(
					createOrModifyGroupPayload);
			postRequest.setEntity(new StringEntity(jsonized));
			HttpResponse resp = httpClient.execute(postRequest);

			if (resp.getStatusLine().getStatusCode() != 200) {
				throw new Exception(
						"Request to Rakshshak returned with error code ["
								+ resp.getStatusLine().getStatusCode() + "]");
			}

		} catch (Exception e) {
			log.error(e);
			throw e;
		} finally {
			httpClient.getConnectionManager().closeExpiredConnections();
		}
	}

	public static void removeUsersFromGroup(
			SecurityDashboardConfiguration config, Set<String> users,
			String group) throws Exception {
		DefaultHttpClient httpClient = new DefaultHttpClient();
		try {

			String url = String.format("%s/extensions/removeUsersFromGroup",
					config.getRakshakBaseUrl());
			HttpPost postRequest = new HttpPost(url);
			RemoveUsersFromGroupPayload payload = new RemoveUsersFromGroupPayload();

			payload.group = group;
			payload.users = users;

			String jsonized = GSONUtil.getGSONInstance().toJson(payload);
			postRequest.setEntity(new StringEntity(jsonized));
			HttpResponse resp = httpClient.execute(postRequest);

			if (resp.getStatusLine().getStatusCode() != 200) {
				throw new Exception(
						"Request to Rakshshak returned with error code ["
								+ resp.getStatusLine().getStatusCode() + "]");
			}

		} catch (Exception e) {
			log.error(e);
			throw e;
		} finally {
			httpClient.getConnectionManager().closeExpiredConnections();
		}
	}

	public static Group getGroup(SecurityDashboardConfiguration config,
			String groupName, IAPIKeyManager apiManager) throws Exception {
		Set<Group> allGroups = getAllGroups(config, apiManager);
		for (Group g : allGroups) {
			if (g.getName().equals(groupName)) {
				return g;
			}
		}
		return null;
	}

	public static void removeGroup(SecurityDashboardConfiguration config,
			String groupName) throws Exception {
		DefaultHttpClient httpClient = new DefaultHttpClient();
		try {

			String url = String.format("%s/extensions/removeGroup",
					config.getRakshakBaseUrl());
			HttpPost postRequest = new HttpPost(url);
			RemoveGroupPayload payload = new RemoveGroupPayload();

			payload.group = groupName;

			String jsonized = GSONUtil.getGSONInstance().toJson(payload);
			postRequest.setEntity(new StringEntity(jsonized));
			HttpResponse resp = httpClient.execute(postRequest);

			if (resp.getStatusLine().getStatusCode() != 200) {
				throw new Exception(
						"Request to Rakshshak returned with error code ["
								+ resp.getStatusLine().getStatusCode() + "]");
			}

		} catch (Exception e) {
			log.error(e);
			throw e;
		} finally {
			httpClient.getConnectionManager().closeExpiredConnections();
		}
	}

	public static class CreateOrModifyGroupPayload {
		@Expose
		private String groupName;
		@Expose
		private String groupDescription;
		@Expose
		private Set<String> users;
		@Expose
		private boolean createGroupIfNotExist;

	}

	public static class RemoveUsersFromGroupPayload {
		@Expose
		private String group;
		@Expose
		private Set<String> users;

	}

	public static class RemoveGroupPayload {
		@Expose
		private String group;
	}

}
