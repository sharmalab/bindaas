package edu.emory.cci.bindaas.junit.provider.mongodb;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.cxf.helpers.IOUtils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;

import edu.emory.cci.bindaas.core.api.IExecutionTasks;
import edu.emory.cci.bindaas.core.api.IManagementTasks;
import edu.emory.cci.bindaas.core.model.DeleteEndpointRequestParameter;
import edu.emory.cci.bindaas.framework.model.DeleteEndpoint;
import edu.emory.cci.bindaas.framework.model.Profile;
import edu.emory.cci.bindaas.framework.model.QueryResult;
import edu.emory.cci.bindaas.framework.model.SubmitEndpoint;
import edu.emory.cci.bindaas.framework.model.Workspace;
import edu.emory.cci.bindaas.framework.util.GSONUtil;

public class DeleteHandlerTestCase extends TestCase {

	private String testData1 = "testData/mongodb.workspace.1.testcase";
	private String workspaceName = "mongodb_junit";
	private String profileName = "audit2";
	private String mongoHost = "hudson.cci.emory.edu";
	private int mongoPort = 27017;
	private String db = "junit";
	private String collection = "audit2";
	private JsonParser parser = new JsonParser();
	
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		IManagementTasks managementTask = getManagementTaskBean();
		managementTask.deleteWorkspace(workspaceName);
		
		Mongo mongo = new Mongo(mongoHost,mongoPort);
		DBCollection dbCollection = mongo.getDB(db).getCollection(collection);
		dbCollection.drop();

	}
	
	private IExecutionTasks getExecutionTaskBean()
	{
		BundleContext context = Activator.getContext();
		ServiceReference<?> sf = context.getServiceReference(IExecutionTasks.class.getName());
		if(sf!=null)
		{
			Object service = context.getService(sf);
			if(service!=null)
			return (IExecutionTasks) service;
			else
				fail("IExecutionTasks not available for testing");
		}
		else
			fail("IExecutionTasks not available for testing");
		
		return null;
	}
	
	private IManagementTasks getManagementTaskBean()
	{
		BundleContext context = Activator.getContext();
		ServiceReference<?> sf = context.getServiceReference(IManagementTasks.class.getName());
		if(sf!=null)
		{
			Object service = context.getService(sf);
			if(service!=null)
			return (IManagementTasks) service;
			else
				fail("IManagementTasks not available for testing");
		}
		else
			fail("IManagementTasks not available for testing");
		
		return null;
	}

	public void test_createDeleteEndpoint()
	{
		IManagementTasks managementTask = getManagementTaskBean();
		
		try {
			File testDataFile = new File(testData1);
			String content = IOUtils.readStringFromStream(new FileInputStream(testDataFile));
			Workspace testWorkspace = GSONUtil.getGSONInstance().fromJson(content, Workspace.class);
			
			
			// 1. Create a new Workspace
			
			managementTask.createWorkspace(testWorkspace.getName(), testWorkspace.getParams(), testWorkspace.getCreatedBy());
			
			// 2. Create a new Profile
			Profile testProfile = testWorkspace.getProfiles().get(profileName);
			JsonObject profileParams = new JsonObject();
			profileParams.add("providerId", new JsonPrimitive(testProfile.getProviderId()) );
			profileParams.add("providerVersion", new JsonPrimitive(testProfile.getProviderVersion()) );
			profileParams.add("dataSource" , testProfile.getDataSource());
			
			managementTask.createProfile(testProfile.getName(), testWorkspace.getName(), profileParams, testProfile.getCreatedBy() , "");
			
			for(DeleteEndpoint testDeleteEndpoint : testProfile.getDeleteEndpoints().values())
			{
				DeleteEndpointRequestParameter derp = new DeleteEndpointRequestParameter();
				derp.setBindVariables(testDeleteEndpoint.getBindVariables());
				derp.setDescription(testDeleteEndpoint.getDescription());
				derp.setQueryTemplate(testDeleteEndpoint.getQueryTemplate());
				derp.setTags(testDeleteEndpoint.getTags());
				
				JsonObject params = GSONUtil.getGSONInstance().toJsonTree(derp).getAsJsonObject();
				
				managementTask.createDeleteEndpoint(testDeleteEndpoint.getName(), testWorkspace.getName(), testProfile.getName(), params, testDeleteEndpoint.getCreatedBy());
				
			}
		
		}catch (Exception e) {
			fail(e.getMessage());	
		}
	}
	
	public void test_createDeleteEndpointWithWrongParams()
	{
		IManagementTasks managementTask = getManagementTaskBean();
		
		try {
			File testDataFile = new File(testData1);
			String content = IOUtils.readStringFromStream(new FileInputStream(testDataFile));
			Workspace testWorkspace = GSONUtil.getGSONInstance().fromJson(content, Workspace.class);
			
			
			// 1. Create a new Workspace
			
			managementTask.createWorkspace(testWorkspace.getName(), testWorkspace.getParams(), testWorkspace.getCreatedBy());
			
			// 2. Create a new Profile
			Profile testProfile = testWorkspace.getProfiles().get(profileName);
			JsonObject profileParams = new JsonObject();
			profileParams.add("providerId", new JsonPrimitive(testProfile.getProviderId()) );
			profileParams.add("providerVersion", new JsonPrimitive(testProfile.getProviderVersion()) );
			profileParams.add("dataSource" , testProfile.getDataSource());
			
			managementTask.createProfile(testProfile.getName(), testWorkspace.getName(), profileParams, testProfile.getCreatedBy() , "");
			
			for(DeleteEndpoint testDeleteEndpoint : testProfile.getDeleteEndpoints().values())
			{
				
				JsonObject params = new JsonObject();	
				managementTask.createDeleteEndpoint(testDeleteEndpoint.getName(), testWorkspace.getName(), testProfile.getName(), params, testDeleteEndpoint.getCreatedBy());
				
			}
			fail("Must throw an exception");
		
		}catch (Exception e) {
				
		}
	}
	
	public void test_executeDelete_1()
	{
		IManagementTasks managementTask = getManagementTaskBean();
		IExecutionTasks executionTasks = getExecutionTaskBean();
	
		try {
			File testDataFile = new File(testData1);
			String content = IOUtils.readStringFromStream(new FileInputStream(testDataFile));
			Workspace testWorkspace = GSONUtil.getGSONInstance().fromJson(content, Workspace.class);
			
			
			// 1. Create a new Workspace
			
			managementTask.createWorkspace(testWorkspace.getName(), testWorkspace.getParams(), testWorkspace.getCreatedBy());
			
			// 2. Create a new Profile
			Profile testProfile = testWorkspace.getProfiles().get(profileName);
			JsonObject profileParams = new JsonObject();
			profileParams.add("providerId", new JsonPrimitive(testProfile.getProviderId()) );
			profileParams.add("providerVersion", new JsonPrimitive(testProfile.getProviderVersion()) );
			profileParams.add("dataSource" , testProfile.getDataSource());
			
			managementTask.createProfile(testProfile.getName(), testWorkspace.getName(), profileParams, testProfile.getCreatedBy() , "");
		
			// 3. Create a new SubmitEndpoint
						
			SubmitEndpoint testCsvSubmitEndpoint = testProfile.getSubmitEndpoints().get("csvWithHeader");
			JsonObject params = GSONUtil.getGSONInstance().toJsonTree(testCsvSubmitEndpoint).getAsJsonObject();
			managementTask.createSubmitEndpoint(testCsvSubmitEndpoint.getName(), workspaceName, profileName, params, testCsvSubmitEndpoint.getCreatedBy());
			
			// 4. Create a new DeleteEndpoint
			
			DeleteEndpoint testDeleteEndpoint = testProfile.getDeleteEndpoints().get("deleteByUser");
			DeleteEndpointRequestParameter derp = new DeleteEndpointRequestParameter();
			derp.setBindVariables(testDeleteEndpoint.getBindVariables());
			derp.setDescription(testDeleteEndpoint.getDescription());
			derp.setQueryTemplate(testDeleteEndpoint.getQueryTemplate());
			derp.setTags(testDeleteEndpoint.getTags());
			
			params = GSONUtil.getGSONInstance().toJsonTree(derp).getAsJsonObject();
			
			managementTask.createDeleteEndpoint(testDeleteEndpoint.getName(), workspaceName, profileName, params, testDeleteEndpoint.getCreatedBy());
			
						
		}catch (Exception e) {
			fail(e.getMessage());
			
		}
	
		Profile profile;
		try {
			
			// Put Some data in the table before deleting
			profile = managementTask.getProfile(workspaceName, profileName );
			SubmitEndpoint se = profile.getSubmitEndpoints().get("csvWithHeader");
			
			File auditData = new File("testData/audit.dataWithHeaders.csv");
			String content = IOUtils.toString(new FileInputStream(auditData));
			QueryResult queryResult = executionTasks.executeSubmitEndpoint("junit", content,profile , se);
			assertNotNull(queryResult);
			assertNotNull(queryResult.getData());
			assertFalse(queryResult.isError());
			assertFalse(queryResult.isCallback());
			System.out.println(new String(queryResult.getData()));
			
			// Execute DeleteEndpoint and remove data
			DeleteEndpoint deleteEndpoint = profile.getDeleteEndpoints().get("deleteByUser");
			Map<String,String> runtimeParameters = new HashMap<String, String>();
			runtimeParameters.put("user", "admin");
			queryResult = executionTasks.executeDeleteEndpoint(deleteEndpoint.getCreatedBy(), runtimeParameters, profile, deleteEndpoint);
			assertNotNull(queryResult.getData());
			assertFalse(queryResult.isError());
			assertFalse(queryResult.isCallback());
			
			String str = new String(queryResult.getData());
			JsonObject json = parser.parse(str).getAsJsonObject();
			assertEquals(json.get("rowsDeleted").getAsInt(), 102);
			System.out.println(str);
			
			
			// Insert more data in the empty table
			queryResult = executionTasks.executeSubmitEndpoint("junit", content,profile , se);
			assertNotNull(queryResult);
			assertNotNull(queryResult.getData());
			assertFalse(queryResult.isError());
			assertFalse(queryResult.isCallback());
			System.out.println(new String(queryResult.getData()));
			
			// Execute Delete endoint again with different param this time
			runtimeParameters.put("user", "anonymous");
			queryResult = executionTasks.executeDeleteEndpoint(deleteEndpoint.getCreatedBy(), runtimeParameters, profile, deleteEndpoint);
			assertNotNull(queryResult.getData());
			assertFalse(queryResult.isError());
			assertFalse(queryResult.isCallback());
			
			str = new String(queryResult.getData());
			System.out.println(str);
			json = parser.parse(str).getAsJsonObject();
			assertEquals(json.get("rowsDeleted").getAsInt(), 0);
			
			
		} catch (Exception e) {

				fail(e.getMessage());
		}

	
	}
	
	public void test_executeDelete_2()
	{
		
	}
	
	public void test_executeDelete_3()
	{
		
	}
}
