package edu.emory.cci.bindaas.trusted_app_client.test ;

import java.util.Date;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import edu.emory.cci.bindaas.trusted_app_client.app.exception.ServerException;
import edu.emory.cci.bindaas.trusted_app_client.core.TrustedAppClientImpl;

public class TestTrustedAppClient {

	private Log log = LogFactory.getLog(getClass());

	@Test
	public void testAuthorizeNewUser()
	{
		String url = "http://localhost:9099/trustedApplication";
		String applicationID = "demo-id";
		String applicationSecret = "demo-secret-key";
		String randomUser = UUID.randomUUID().toString();
		String protocol = "api_key";
		Long epochTimeExpires = (new Date()).getTime() + 1000*60; // expires in 1 min
		String comments = "junit testing";
		TrustedAppClientImpl client = new TrustedAppClientImpl(url,applicationID, applicationSecret);
		try {
			JsonObject response = client.authorizeNewUser(protocol, randomUser, epochTimeExpires, comments);
			Assert.assertNotNull(response);
			log.info(response);
		} catch (Exception e) {
			log.error(e);
			Assert.fail(e.getMessage());
			
		}
	}
	
	@Test
	public void testAuthorizeExistingUser()
	{
		String url = "http://localhost:9099/trustedApplication";
		String applicationID = "demo-id";
		String applicationSecret = "demo-secret-key";
		String randomUser = UUID.randomUUID().toString();
		String protocol = "api_key";
		Long epochTimeExpires = (new Date()).getTime() + 1000*60; // expires in 1 min
		String comments = "junit testing";
		TrustedAppClientImpl client = new TrustedAppClientImpl(url,applicationID, applicationSecret);
		try {
			JsonObject response = client.authorizeNewUser(protocol, randomUser, epochTimeExpires, comments);
			Assert.assertNotNull(response);
			log.info(response);
		} catch (Exception e) {
			log.error(e);
			Assert.fail(e.getMessage());
		}
		
		try {
			
			client.authorizeNewUser(protocol, randomUser, epochTimeExpires, comments);
			Assert.fail("API Key should never be generated for existing user");
			
		} catch (Exception e) {
			Assert.assertTrue(e instanceof ServerException);
			Assert.assertEquals(new Integer(590) , ServerException.class.cast(e).getErrorCode() );
		}
	}
	
	@Test
	public void testGetShortLivedAPIKey()
	{
		String url = "http://localhost:9099/trustedApplication";
		String applicationID = "demo-id";
		String applicationSecret = "demo-secret-key";
		String randomUser = UUID.randomUUID().toString();
		String protocol = "api_key";
		Long epochTimeExpires = (new Date()).getTime() + 1000*60; // expires in 1 min
		String comments = "junit testing";
		TrustedAppClientImpl client = new TrustedAppClientImpl(url,applicationID, applicationSecret);
		try {
			JsonObject masterResponse = client.authorizeNewUser(protocol, randomUser, epochTimeExpires, comments);
			Assert.assertNotNull(masterResponse);
			log.info(masterResponse);
			JsonObject shortResponse = client.getShortLivedAuthenticationToken(protocol, randomUser, 10);
			log.info(shortResponse);
			
		} catch (Exception e) {
			log.error(e);
			Assert.fail(e.getMessage());
			
		}
	}

	@Test
	public void testGetShortLivedAPIKeyWithoutRegistering()
	{
		String url = "http://localhost:9099/trustedApplication";
		String applicationID = "demo-id";
		String applicationSecret = "demo-secret-key";
		String randomUser = UUID.randomUUID().toString();
		String protocol = "api_key";
		TrustedAppClientImpl client = new TrustedAppClientImpl(url,applicationID, applicationSecret);
		try {
			
		client.getShortLivedAuthenticationToken(protocol, randomUser, 10);
		Assert.fail("Short-Lived Key should not be generated");
			
		} catch (Exception e) {
			
			Assert.assertTrue(e instanceof ServerException);
			Assert.assertEquals(new Integer(591) , ServerException.class.cast(e).getErrorCode() );
		}
	}

	@Test
	public void testListAPIKey()
	{
		String url = "http://localhost:9099/trustedApplication";
		String applicationID = "demo-id";
		String applicationSecret = "demo-secret-key";
		String protocol = "api_key";
		
		TrustedAppClientImpl client = new TrustedAppClientImpl(url,applicationID, applicationSecret);
		try {
			JsonArray response = client.listAuthenticationTokens(protocol);
			Assert.assertNotNull(response);
			Assert.assertTrue(response.size() > 0);
			log.info(response);
			log.info(response.size());
			
		} catch (Exception e) {
			log.error(e);
			Assert.fail(e.getMessage());
			
		}
		
	}
	
	@Test	
	public void testRevokeAccess()
	{
		String url = "http://localhost:9099/trustedApplication";
		String applicationID = "demo-id";
		String applicationSecret = "demo-secret-key";
		String randomUser = UUID.randomUUID().toString();
		String protocol = "api_key";
		Long epochTimeExpires = (new Date()).getTime() + 1000*60; // expires in 1 min
		String comments = "junit testing";
		TrustedAppClientImpl client = new TrustedAppClientImpl(url,applicationID, applicationSecret);
		try {
			JsonObject masterResponse = client.authorizeNewUser(protocol, randomUser, epochTimeExpires, comments);
			Assert.assertNotNull(masterResponse);
			log.info(masterResponse);
			JsonObject revokeResponse = client.revokeAccess(protocol, randomUser, comments);
			Assert.assertNotNull(revokeResponse);
			log.info(revokeResponse);
		} catch (Exception e) {
			log.error(e);
			Assert.fail(e.getMessage());
			
		}
	}
	
	@Test	
	public void testAuthroizeRevokeReauthroize()
	{
		String url = "http://localhost:9099/trustedApplication";
		String applicationID = "demo-id";
		String applicationSecret = "demo-secret-key";
		String randomUser = UUID.randomUUID().toString();
		String protocol = "api_key";
		Long epochTimeExpires = (new Date()).getTime() + 1000*60; // expires in 1 min
		String comments = "junit testing";
		TrustedAppClientImpl client = new TrustedAppClientImpl(url,applicationID, applicationSecret);
		try {
			JsonObject masterResponse = client.authorizeNewUser(protocol, randomUser, epochTimeExpires, comments);
			Assert.assertNotNull(masterResponse);
			log.info(masterResponse);
			JsonObject revokeResponse = client.revokeAccess(protocol, randomUser, comments);
			Assert.assertNotNull(revokeResponse);
			log.info(revokeResponse);
		} catch (Exception e) {
			log.error(e);
			Assert.fail(e.getMessage());
			
		}
		
		try {

			JsonObject masterResponse = client.authorizeNewUser(protocol, randomUser, epochTimeExpires, comments);
			Assert.assertNotNull(masterResponse);
			log.info(masterResponse);
			
		}catch(Exception e)
		{
			log.error(e);
			Assert.fail(e.getMessage());
		}
	}
}
