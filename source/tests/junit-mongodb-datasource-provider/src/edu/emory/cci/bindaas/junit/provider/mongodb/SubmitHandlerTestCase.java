package edu.emory.cci.bindaas.junit.provider.mongodb;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;

import junit.framework.TestCase;

import org.apache.cxf.helpers.IOUtils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;

import edu.emory.cci.bindaas.core.api.IExecutionTasks;
import edu.emory.cci.bindaas.core.api.IManagementTasks;
import edu.emory.cci.bindaas.framework.model.Profile;
import edu.emory.cci.bindaas.framework.model.QueryResult;
import edu.emory.cci.bindaas.framework.model.SubmitEndpoint;
import edu.emory.cci.bindaas.framework.model.Workspace;
import edu.emory.cci.bindaas.framework.util.GSONUtil;

public class SubmitHandlerTestCase extends TestCase {
	
	private String testData1 = "testData/mongodb.workspace.1.testcase";
	private String workspaceName = "mongodb_junit";
	private String profileName = "audit2";
	private String mongoHost = "hudson.cci.emory.edu";
	private int mongoPort = 27017;
	private String db = "junit";
	private String collection = "audit2";
	
	
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

	
	public void test_createSubmitEndpoint()
	{
		IManagementTasks managementTask = getManagementTaskBean();
	
	
		try {
			File testDataFile = new File(testData1);
			String content = IOUtils.readStringFromStream(new FileInputStream(testDataFile));
			Workspace testWorkspace = GSONUtil.getGSONInstance().fromJson(content, Workspace.class);
			
			
			// 1. Create a new Workspace
			
			managementTask.createWorkspace(testWorkspace.getName(), testWorkspace.getParams(), testWorkspace.getCreatedBy());
			
			// 2. Create a new Profile
			Profile testProfile = testWorkspace.getProfiles().get("audit2");
			JsonObject profileParams = new JsonObject();
			profileParams.add("providerId", new JsonPrimitive(testProfile.getProviderId()) );
			profileParams.add("providerVersion", new JsonPrimitive(testProfile.getProviderVersion()) );
			profileParams.add("dataSource" , testProfile.getDataSource());
			
			managementTask.createProfile(testProfile.getName(), testWorkspace.getName(), profileParams, testProfile.getCreatedBy(), "");
			
			// 3. Create a new SubmitEndpoint
			
			SubmitEndpoint testJsonSubmitEndpoint = testProfile.getSubmitEndpoints().get("json");
			JsonObject params = GSONUtil.getGSONInstance().toJsonTree(testJsonSubmitEndpoint).getAsJsonObject();
			managementTask.createSubmitEndpoint(testJsonSubmitEndpoint.getName(), workspaceName, profileName,params, testJsonSubmitEndpoint.getCreatedBy());
			
			SubmitEndpoint testCsvSubmitEndpoint = testProfile.getSubmitEndpoints().get("csv");
			params = GSONUtil.getGSONInstance().toJsonTree(testCsvSubmitEndpoint).getAsJsonObject();
			managementTask.createSubmitEndpoint(testCsvSubmitEndpoint.getName(), workspaceName, profileName,params, testCsvSubmitEndpoint.getCreatedBy());
			
		
		}catch (Exception e) {
			fail(e.getMessage());
			
		}
	}
	
	public void test_createSubmitEndpointWithWrongParams()
	{
		IManagementTasks managementTask = getManagementTaskBean();
		
		
		try {
			File testDataFile = new File(testData1);
			String content = IOUtils.readStringFromStream(new FileInputStream(testDataFile));
			Workspace testWorkspace = GSONUtil.getGSONInstance().fromJson(content, Workspace.class);
			
			
			// 1. Create a new Workspace
			
			managementTask.createWorkspace(testWorkspace.getName(), testWorkspace.getParams(), testWorkspace.getCreatedBy());
			
			// 2. Create a new Profile
			Profile testProfile = testWorkspace.getProfiles().get("audit2");
			JsonObject profileParams = new JsonObject();
			profileParams.add("providerId", new JsonPrimitive(testProfile.getProviderId()) );
			profileParams.add("providerVersion", new JsonPrimitive(testProfile.getProviderVersion()) );
			profileParams.add("dataSource" , testProfile.getDataSource());
			
			managementTask.createProfile(testProfile.getName(), testWorkspace.getName(), profileParams, testProfile.getCreatedBy(), "");
			
			// 3. Create a new SubmitEndpoint
			
			SubmitEndpoint testJsonSubmitEndpoint = testProfile.getSubmitEndpoints().get("json");
			managementTask.createSubmitEndpoint(testJsonSubmitEndpoint.getName(), workspaceName, profileName, new JsonObject(), testJsonSubmitEndpoint.getCreatedBy());
			
			SubmitEndpoint testCsvSubmitEndpoint = testProfile.getSubmitEndpoints().get("csv");
			managementTask.createSubmitEndpoint(testCsvSubmitEndpoint.getName(), workspaceName, profileName, new JsonObject(), testCsvSubmitEndpoint.getCreatedBy());
			
			fail("Must throw an exception");
		}catch (Exception e) {
			
			
		}
	}
	
	public void test_executeSubmit_1()
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
			Profile testProfile = testWorkspace.getProfiles().get("audit2");
			JsonObject profileParams = new JsonObject();
			profileParams.add("providerId", new JsonPrimitive(testProfile.getProviderId()) );
			profileParams.add("providerVersion", new JsonPrimitive(testProfile.getProviderVersion()) );
			profileParams.add("dataSource" , testProfile.getDataSource());
			
			managementTask.createProfile(testProfile.getName(), testWorkspace.getName(), profileParams, testProfile.getCreatedBy(), "");
		
			// 3. Create a new SubmitEndpoint
			
			SubmitEndpoint testJsonSubmitEndpoint = testProfile.getSubmitEndpoints().get("json");
			JsonObject params = GSONUtil.getGSONInstance().toJsonTree(testJsonSubmitEndpoint).getAsJsonObject();
			managementTask.createSubmitEndpoint(testJsonSubmitEndpoint.getName(), workspaceName, profileName, params, testJsonSubmitEndpoint.getCreatedBy());
						
						
		}catch (Exception e) {
			fail(e.getMessage());
			
		}
	
		Profile profile;
		try {
			profile = managementTask.getProfile(workspaceName, profileName );
			SubmitEndpoint se = profile.getSubmitEndpoints().get("json");
			
			File auditData = new File("testData/audit.data");
			JsonParser parser = new JsonParser();
			JsonArray dataArray = parser.parse(new FileReader(auditData)).getAsJsonArray();
			
			for(int index = 0; index < dataArray.size() ; index ++)
			{
				String content = dataArray.get(index).toString();
				QueryResult queryResult = executionTasks.executeSubmitEndpoint("junit", content,profile , se);
				assertNotNull(queryResult);
				assertNotNull(queryResult.getData());
				assertFalse(queryResult.isError());
				assertFalse(queryResult.isCallback());
				System.out.println(new String(queryResult.getData()));
			}
			
				
				
			
			
		} catch (Exception e) {

				fail(e.getMessage());
		}
		
	}
	
	public void test_executeSubmit_2()
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
			Profile testProfile = testWorkspace.getProfiles().get("audit2");
			JsonObject profileParams = new JsonObject();
			profileParams.add("providerId", new JsonPrimitive(testProfile.getProviderId()) );
			profileParams.add("providerVersion", new JsonPrimitive(testProfile.getProviderVersion()) );
			profileParams.add("dataSource" , testProfile.getDataSource());
			
			managementTask.createProfile(testProfile.getName(), testWorkspace.getName(), profileParams, testProfile.getCreatedBy(), "");
		
			// 3. Create a new SubmitEndpoint
						
			SubmitEndpoint testCsvSubmitEndpoint = testProfile.getSubmitEndpoints().get("csv");
			JsonObject params = GSONUtil.getGSONInstance().toJsonTree(testCsvSubmitEndpoint).getAsJsonObject();
			managementTask.createSubmitEndpoint(testCsvSubmitEndpoint.getName(), workspaceName, profileName, params, testCsvSubmitEndpoint.getCreatedBy());
						
		}catch (Exception e) {
			fail(e.getMessage());
			
		}
	
		Profile profile;
		try {
			profile = managementTask.getProfile(workspaceName, profileName );
			SubmitEndpoint se = profile.getSubmitEndpoints().get("csv");
			
			File auditData = new File("testData/audit.data.csv");
			String content = IOUtils.toString(new FileInputStream(auditData));
			QueryResult queryResult = executionTasks.executeSubmitEndpoint("junit", content,profile , se);
			assertNotNull(queryResult);
			assertNotNull(queryResult.getData());
			assertFalse(queryResult.isError());
			assertFalse(queryResult.isCallback());
			System.out.println(new String(queryResult.getData()));
				
		} catch (Exception e) {

				fail(e.getMessage());
		}

	}
	
	public void test_executeSubmit_3()
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
			Profile testProfile = testWorkspace.getProfiles().get("audit2");
			JsonObject profileParams = new JsonObject();
			profileParams.add("providerId", new JsonPrimitive(testProfile.getProviderId()) );
			profileParams.add("providerVersion", new JsonPrimitive(testProfile.getProviderVersion()) );
			profileParams.add("dataSource" , testProfile.getDataSource());
			
			managementTask.createProfile(testProfile.getName(), testWorkspace.getName(), profileParams, testProfile.getCreatedBy(), "");
		
			// 3. Create a new SubmitEndpoint
						
			SubmitEndpoint testCsvSubmitEndpoint = testProfile.getSubmitEndpoints().get("csvWithHeader");
			JsonObject params = GSONUtil.getGSONInstance().toJsonTree(testCsvSubmitEndpoint).getAsJsonObject();
			managementTask.createSubmitEndpoint(testCsvSubmitEndpoint.getName(), workspaceName, profileName, params, testCsvSubmitEndpoint.getCreatedBy());
						
		}catch (Exception e) {
			fail(e.getMessage());
			
		}
	
		Profile profile;
		try {
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
				
		} catch (Exception e) {

				fail(e.getMessage());
		}

	}
}
