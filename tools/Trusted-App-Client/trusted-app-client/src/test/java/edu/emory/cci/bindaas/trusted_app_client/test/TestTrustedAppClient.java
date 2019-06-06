package edu.emory.cci.bindaas.trusted_app_client.test ;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

import edu.emory.cci.bindaas.trusted_app_client.app.exception.ServerException;
import edu.emory.cci.bindaas.trusted_app_client.core.APIKey;
import edu.emory.cci.bindaas.trusted_app_client.core.TrustedAppClientImpl;

public class TestTrustedAppClient {

	@Test
	public void testAuthorizeNewUser()
	{
		String url = "http://localhost:9099/trustedApplication";
		String applicationID = "demo-id";
		String applicationSecret = "demo-secret-key";
		String randomUser = UUID.randomUUID().toString();
		Long epochTimeExpires = (new Date()).getTime() + 1000*60; // expires in 1 min
		String comments = "junit testing";
		TrustedAppClientImpl client = new TrustedAppClientImpl(url,applicationID, applicationSecret);
		try {
			APIKey apiKey = client.authorizeNewUser(randomUser, epochTimeExpires, comments);
			Assert.assertNotNull(apiKey);
			System.out.println(apiKey);
		} catch (Exception e) {
			e.printStackTrace();
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
		Long epochTimeExpires = (new Date()).getTime() + 1000*60; // expires in 1 min
		String comments = "junit testing";
		TrustedAppClientImpl client = new TrustedAppClientImpl(url,applicationID, applicationSecret);
		try {
			APIKey apiKey = client.authorizeNewUser(randomUser, epochTimeExpires, comments);
			Assert.assertNotNull(apiKey);
			System.out.println(apiKey);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
		
		try {
			
			client.authorizeNewUser(randomUser, epochTimeExpires, comments);
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
		Long epochTimeExpires = (new Date()).getTime() + 1000*60; // expires in 1 min
		String comments = "junit testing";
		TrustedAppClientImpl client = new TrustedAppClientImpl(url,applicationID, applicationSecret);
		try {
			APIKey masterKey = client.authorizeNewUser(randomUser, epochTimeExpires, comments);
			Assert.assertNotNull(masterKey);
			System.out.println(masterKey);
			APIKey shortKey = client.getShortLivedAPIKey(randomUser, 10);
			System.out.println(shortKey);
			
		} catch (Exception e) {
			e.printStackTrace();
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
		TrustedAppClientImpl client = new TrustedAppClientImpl(url,applicationID, applicationSecret);
		try {
			
		client.getShortLivedAPIKey(randomUser, 10);
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
		
		TrustedAppClientImpl client = new TrustedAppClientImpl(url,applicationID, applicationSecret);
		try {
			List<APIKey> list = client.listAPIKeys();
			Assert.assertNotNull(list);
			Assert.assertTrue(list.size() > 0);
			System.out.println(list);
			System.out.println(list.size());
			
		} catch (Exception e) {
			e.printStackTrace();
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
		Long epochTimeExpires = (new Date()).getTime() + 1000*60; // expires in 1 min
		String comments = "junit testing";
		TrustedAppClientImpl client = new TrustedAppClientImpl(url,applicationID, applicationSecret);
		try {
			APIKey masterKey = client.authorizeNewUser(randomUser, epochTimeExpires, comments);
			Assert.assertNotNull(masterKey);
			System.out.println(masterKey);
			String resp = client.revokeAccess(randomUser, comments);
			Assert.assertNotNull(resp);
			System.out.println(resp);
		} catch (Exception e) {
			e.printStackTrace();
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
		Long epochTimeExpires = (new Date()).getTime() + 1000*60; // expires in 1 min
		String comments = "junit testing";
		TrustedAppClientImpl client = new TrustedAppClientImpl(url,applicationID, applicationSecret);
		try {
			APIKey masterKey = client.authorizeNewUser(randomUser, epochTimeExpires, comments);
			Assert.assertNotNull(masterKey);
			System.out.println(masterKey);
			String resp = client.revokeAccess(randomUser, comments);
			Assert.assertNotNull(resp);
			System.out.println(resp);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
			
		}
		
		try {
			
			APIKey masterKey = client.authorizeNewUser(randomUser, epochTimeExpires, comments);
			Assert.assertNotNull(masterKey);
			System.out.println(masterKey);
			
		}catch(Exception e)
		{
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}
}
