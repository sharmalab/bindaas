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
import com.google.gson.JsonPrimitive;

import edu.emory.cci.bindaas.core.api.IExecutionTasks;
import edu.emory.cci.bindaas.core.api.IManagementTasks;
import edu.emory.cci.bindaas.core.model.QueryEndpointRequestParameter;
import edu.emory.cci.bindaas.framework.model.Profile;
import edu.emory.cci.bindaas.framework.model.QueryEndpoint;
import edu.emory.cci.bindaas.framework.model.QueryResult;
import edu.emory.cci.bindaas.framework.model.Workspace;
import edu.emory.cci.bindaas.framework.util.GSONUtil;

public class QueryHandlerTestCase extends TestCase {

	private String testData1 = "testData/mongodb.workspace.1.testcase";
	private String workspaceName = "mongodb_junit";
	
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		IManagementTasks managementTask = getManagementTaskBean();
		managementTask.deleteWorkspace(workspaceName);

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


	
	
	public void test_createQueryEndpoint()
	{
		IManagementTasks managementTask = getManagementTaskBean();
	
		try {
			File testDataFile = new File(testData1);
			String content = IOUtils.readStringFromStream(new FileInputStream(testDataFile));
			Workspace testWorkspace = GSONUtil.getGSONInstance().fromJson(content, Workspace.class);
			
			
			// 1. Create a new Workspace
			
			managementTask.createWorkspace(testWorkspace.getName(), testWorkspace.getParams(), testWorkspace.getCreatedBy());
			
			// 2. Create a new Profile
			Profile testProfile = testWorkspace.getProfiles().get("audit");
			JsonObject profileParams = new JsonObject();
			profileParams.add("providerId", new JsonPrimitive(testProfile.getProviderId()) );
			profileParams.add("providerVersion", new JsonPrimitive(testProfile.getProviderVersion()) );
			profileParams.add("dataSource" , testProfile.getDataSource());
			
			managementTask.createProfile(testProfile.getName(), testWorkspace.getName(), profileParams, testProfile.getCreatedBy(), "");
			
			for(QueryEndpoint testQueryEndpoint : testProfile.getQueryEndpoints().values())
			{
				QueryEndpointRequestParameter qerp = new QueryEndpointRequestParameter();
				qerp.setBindVariables(testQueryEndpoint.getBindVariables());
				qerp.setDescription(testQueryEndpoint.getDescription());
				qerp.setMetaData(testQueryEndpoint.getMetaData());
				qerp.setOutputFormat(testQueryEndpoint.getOutputFormat());
				qerp.setQueryModifiers(testQueryEndpoint.getQueryModifiers());
				qerp.setQueryResultModifiers(testQueryEndpoint.getQueryResultModifiers());
				qerp.setQueryTemplate(testQueryEndpoint.getQueryTemplate());
				qerp.setTags(testQueryEndpoint.getTags());
				
				JsonObject params = GSONUtil.getGSONInstance().toJsonTree(qerp).getAsJsonObject();
				
				managementTask.createQueryEndpoint(testQueryEndpoint.getName(), testWorkspace.getName(), testProfile.getName(), params, testQueryEndpoint.getCreatedBy());
				
			}
		
		}catch (Exception e) {
			fail(e.getMessage());	
		}
	}
	
	public void test_createQueryEndpointWithWrongParams()
	{
		IManagementTasks managementTask = getManagementTaskBean();
		
	
		try {
			File testDataFile = new File(testData1);
			String content = IOUtils.readStringFromStream(new FileInputStream(testDataFile));
			Workspace testWorkspace = GSONUtil.getGSONInstance().fromJson(content, Workspace.class);
			
			
			// 1. Create a new Workspace
			
			managementTask.createWorkspace(testWorkspace.getName(), testWorkspace.getParams(), testWorkspace.getCreatedBy());
			
			// 2. Create a new Profile
			Profile testProfile = testWorkspace.getProfiles().get("audit");
			JsonObject profileParams = new JsonObject();
			profileParams.add("providerId", new JsonPrimitive(testProfile.getProviderId()) );
			profileParams.add("providerVersion", new JsonPrimitive(testProfile.getProviderVersion()) );
			profileParams.add("dataSource" , testProfile.getDataSource());
			
			managementTask.createProfile(testProfile.getName(), testWorkspace.getName(), profileParams, testProfile.getCreatedBy(), "");
			
			for(QueryEndpoint testQueryEndpoint : testProfile.getQueryEndpoints().values())
			{
				
				managementTask.createQueryEndpoint(testQueryEndpoint.getName(), testWorkspace.getName(), testProfile.getName(), new JsonObject(), testQueryEndpoint.getCreatedBy());
				
			}
			fail("Must throw exception");
		}catch (Exception e) {
				
		}
	}
	
	public void test_executeQuery_1()
	{
		
		IManagementTasks managementTask = getManagementTaskBean();
		IExecutionTasks executionTask = getExecutionTaskBean();
		try {
			File testDataFile = new File(testData1);
			String content = IOUtils.readStringFromStream(new FileInputStream(testDataFile));
			Workspace testWorkspace = GSONUtil.getGSONInstance().fromJson(content, Workspace.class);
			
			
			// 1. Create a new Workspace
			
			managementTask.createWorkspace(testWorkspace.getName(), testWorkspace.getParams(), testWorkspace.getCreatedBy());
			
			// 2. Create a new Profile
			Profile testProfile = testWorkspace.getProfiles().get("audit");
			JsonObject profileParams = new JsonObject();
			profileParams.add("providerId", new JsonPrimitive(testProfile.getProviderId()) );
			profileParams.add("providerVersion", new JsonPrimitive(testProfile.getProviderVersion()) );
			profileParams.add("dataSource" , testProfile.getDataSource());
			
			Profile profile = managementTask.createProfile(testProfile.getName(), testWorkspace.getName(), profileParams, testProfile.getCreatedBy(), "");
			
			for(QueryEndpoint testQueryEndpoint : testProfile.getQueryEndpoints().values())
			{
				QueryEndpointRequestParameter qerp = new QueryEndpointRequestParameter();
				qerp.setBindVariables(testQueryEndpoint.getBindVariables());
				qerp.setDescription(testQueryEndpoint.getDescription());
				qerp.setMetaData(testQueryEndpoint.getMetaData());
				qerp.setOutputFormat(testQueryEndpoint.getOutputFormat());
				qerp.setQueryModifiers(testQueryEndpoint.getQueryModifiers());
				qerp.setQueryResultModifiers(testQueryEndpoint.getQueryResultModifiers());
				qerp.setQueryTemplate(testQueryEndpoint.getQueryTemplate());
				qerp.setTags(testQueryEndpoint.getTags());
				
				JsonObject params = GSONUtil.getGSONInstance().toJsonTree(qerp).getAsJsonObject();
				
				QueryEndpoint queryEndpoint = managementTask.createQueryEndpoint(testQueryEndpoint.getName(), testWorkspace.getName(), testProfile.getName(), params, testQueryEndpoint.getCreatedBy());
				Map<String,String> runtimeParameters = new HashMap<String,String>();
				runtimeParameters.put("user", "admin");
				
				QueryResult queryResult = executionTask.executeQueryEndpoint(testQueryEndpoint.getCreatedBy(), runtimeParameters, profile, queryEndpoint);
				assertNotNull(queryResult);
				assertTrue(queryResult.isError() == false);
				assertNotNull(queryResult.getData());
				System.out.println(new String(queryResult.getData()));
				
			}
		
		}catch (Exception e) {
			fail(e.getMessage());	
		}
		
		
		System.out.println("Stop");
		
	}
	
	public void test_executeQuery_2()
	{
		
	}
	
	public void test_executeQuery_3()
	{
		
	}
	
	
}
