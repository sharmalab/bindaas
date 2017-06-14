package edu.emory.cci.bindaas.junit.provider.aime;

import java.io.File;
import java.io.FileFilter;
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

import edu.emory.cci.bindaas.core.api.IExecutionTasks;
import edu.emory.cci.bindaas.core.api.IManagementTasks;
import edu.emory.cci.bindaas.core.model.DeleteEndpointRequestParameter;
import edu.emory.cci.bindaas.framework.model.DeleteEndpoint;
import edu.emory.cci.bindaas.framework.model.Profile;
import edu.emory.cci.bindaas.framework.model.QueryResult;
import edu.emory.cci.bindaas.framework.model.SubmitEndpoint;
import edu.emory.cci.bindaas.framework.model.Workspace;
import edu.emory.cci.bindaas.framework.util.GSONUtil;
import edu.emory.cci.bindaas.junit.provider.aime.bundle.Activator;

public class AIMEDeleteHandlerTestCase extends TestCase {
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		IManagementTasks managementTask = getManagementTaskBean();
		managementTask.deleteWorkspace("aime_junit");

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
			File testDataFile = new File("testData/aime.workspace.1.testcase");
			String content = IOUtils.readStringFromStream(new FileInputStream(testDataFile));
			Workspace testWorkspace = GSONUtil.getGSONInstance().fromJson(content, Workspace.class);
			
			
			// 1. Create a new Workspace
			
			managementTask.createWorkspace(testWorkspace.getName(), testWorkspace.getParams(), testWorkspace.getCreatedBy());
			
			// 2. Create a new Profile
			Profile testProfile = testWorkspace.getProfiles().values().iterator().next();
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
		
	}
	
	public void test_executeDelete_1()
	{
		IManagementTasks managementTask = getManagementTaskBean();
		IExecutionTasks executionTask = getExecutionTaskBean();
		try {
			File testDataFile = new File("testData/aime.workspace.1.testcase");
			String content = IOUtils.readStringFromStream(new FileInputStream(testDataFile));
			Workspace testWorkspace = GSONUtil.getGSONInstance().fromJson(content, Workspace.class);
			
			
			// 1. Create a new Workspace
			
			managementTask.createWorkspace(testWorkspace.getName(), testWorkspace.getParams(), testWorkspace.getCreatedBy());
			
			// 2. Create a new Profile
			Profile testProfile = testWorkspace.getProfiles().values().iterator().next();
			JsonObject profileParams = new JsonObject();
			profileParams.add("providerId", new JsonPrimitive(testProfile.getProviderId()) );
			profileParams.add("providerVersion", new JsonPrimitive(testProfile.getProviderVersion()) );
			profileParams.add("dataSource" , testProfile.getDataSource());
			
			Profile profile = managementTask.createProfile(testProfile.getName(), testWorkspace.getName(), profileParams, testProfile.getCreatedBy() , "");
			
			
			// 3. put some test data
			
			profile = managementTask.getProfile("aime_junit", "GBM");
			SubmitEndpoint se = profile.getSubmitEndpoints().get("xml");
			
			File annotationDirectory = new File("testData/gbm.annotations");
			
			File[] listOfFiles = annotationDirectory.listFiles(new FileFilter() {
				
				@Override
				public boolean accept(File arg0) {
				
					return arg0.isFile() && arg0.getName().endsWith("xml");
				}
			});
			
			for(File xmlFile : listOfFiles)
			{
				content = IOUtils.readStringFromStream(new FileInputStream(xmlFile));
				executionTask.executeSubmitEndpoint("junit", content,profile , se);
			}

			
			// create Delete endpoints
			for(DeleteEndpoint testDeleteEndpoint : testProfile.getDeleteEndpoints().values())
			{
				DeleteEndpointRequestParameter derp = new DeleteEndpointRequestParameter();
				derp.setBindVariables(testDeleteEndpoint.getBindVariables());
				derp.setDescription(testDeleteEndpoint.getDescription());
				derp.setQueryTemplate(testDeleteEndpoint.getQueryTemplate());
				derp.setTags(testDeleteEndpoint.getTags());
				
				JsonObject params = GSONUtil.getGSONInstance().toJsonTree(derp).getAsJsonObject();
				
				DeleteEndpoint deleteEndpoint = managementTask.createDeleteEndpoint(testDeleteEndpoint.getName(), testWorkspace.getName(), testProfile.getName(), params, testDeleteEndpoint.getCreatedBy());
				
				
				
				Map<String,String> runtimeParameters = new HashMap<String,String>();
				runtimeParameters.put("reviewer", "rcolen");
				QueryResult queryResult = executionTask.executeDeleteEndpoint(testDeleteEndpoint.getCreatedBy(), runtimeParameters, profile, deleteEndpoint);
				assertNotNull(queryResult);
				assertTrue(queryResult.isError() == false);
				assertNotNull(queryResult.getData());
				JsonParser parser = new JsonParser();
				JsonObject result = parser.parse(edu.emory.cci.bindaas.framework.util.IOUtils.toString(queryResult.getData())).getAsJsonObject();
				assertEquals(27, result.get("rowsDeleted").getAsInt());
			}
		
		}catch (Exception e) {
			fail(e.getMessage());	
		}
		
		System.out.println("Stop");
	
	}
	
	public void test_executeDelete_2()
	{
		
	}
	
	public void test_executeDelete_3()
	{
		
	}
}
