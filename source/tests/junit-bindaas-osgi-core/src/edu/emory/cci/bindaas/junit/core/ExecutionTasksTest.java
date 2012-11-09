package edu.emory.cci.bindaas.junit.core;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import edu.emory.cci.bindaas.core.api.IExecutionTasks;
import edu.emory.cci.bindaas.core.api.IManagementTasks;
import edu.emory.cci.bindaas.core.exception.ExecutionTaskException;
import edu.emory.cci.bindaas.core.model.DeleteEndpointRequestParameter;
import edu.emory.cci.bindaas.core.model.QueryEndpointRequestParameter;
import edu.emory.cci.bindaas.core.model.SubmitEndpointRequestParameter;
import edu.emory.cci.bindaas.framework.model.BindVariable;
import edu.emory.cci.bindaas.framework.model.DeleteEndpoint;
import edu.emory.cci.bindaas.framework.model.ModifierEntry;
import edu.emory.cci.bindaas.framework.model.Profile;
import edu.emory.cci.bindaas.framework.model.QueryEndpoint;
import edu.emory.cci.bindaas.framework.model.QueryResult;
import edu.emory.cci.bindaas.framework.model.Stage;
import edu.emory.cci.bindaas.framework.model.SubmitEndpoint;
import edu.emory.cci.bindaas.framework.model.Workspace;
import edu.emory.cci.bindaas.framework.util.GSONUtil;
import edu.emory.cci.bindaas.junit.mock.MockProvider;
import junit.framework.TestCase;

public class ExecutionTasksTest extends TestCase {
	
	private static String WORKSPACE_NAME = "junit";
	private static String CREATED_BY = "junit";
	private static String USER = "junit";
	private static String PROVIDER_ID = MockProvider.class.getName();
	private static int PROVIDER_VERSION = 1;
	private static String PROFILE_NAME = "testProfile";
	private static String QUERY_ENDPOINT_NAME = "testQuery";
	private static String DELETE_ENDPOINT_NAME = "testDelete";
	private static String SUBMIT_ENDPOINT_NAME = "testSubmit";

	private Map<String,BindVariable> bindVars = new HashMap<String,BindVariable>();
	private JsonObject outputFormat = new JsonObject();
	private String queryTemplate = "select * from mytable where arg1=$name$ and arg2=$phone$ and arg3=$address$";
	private String expectedQuery = "select * from mytable where arg1=JunitName and arg2=JunitPhone and arg3=JunitAddress";
	private JsonParser parser = new JsonParser();
	
	
	
	@Override
	protected void setUp() throws Exception {
	
		super.setUp();
		List<String> workspacesToDelete = new ArrayList<String>();
		IManagementTasks managementTasks = getManagementTaskBean();
		for(Workspace workspace : managementTasks.listWorkspaces())
		{
			if(workspace.getName().startsWith(WORKSPACE_NAME))
				workspacesToDelete.add(workspace.getName());
				
		}
		for(String workspace : workspacesToDelete)
		{
			managementTasks.deleteWorkspace(workspace);	
		}
		
		// create test Workspace/Profile/Query/Delete/Submit
		createWorkspace(managementTasks);
		createProfile(managementTasks);
		createQueryEndpoint(managementTasks);
		createDeleteEndpoint(managementTasks);
		createSubmitEndpoint(managementTasks);
	}
	private void createWorkspace(IManagementTasks managementTasks)
	{
		JsonObject props = new JsonObject();
		try {
			managementTasks.createWorkspace(WORKSPACE_NAME, props , CREATED_BY);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	private void createProfile(IManagementTasks managementTasks)
	{
		JsonObject parameters = new JsonObject();
		parameters.add("providerId", new JsonPrimitive(PROVIDER_ID));
		parameters.add("providerVersion" , new JsonPrimitive(PROVIDER_VERSION));
		
		JsonObject dataSource = new JsonObject();
		parameters.add("dataSource", dataSource);
		
		try {
			 managementTasks.createProfile(PROFILE_NAME, WORKSPACE_NAME, parameters, CREATED_BY);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	private void createQueryEndpoint(IManagementTasks managementTasks)
	{
		QueryEndpointRequestParameter params = new QueryEndpointRequestParameter();
		
		JsonObject metadata = new JsonObject();
		
		outputFormat.add("type", new JsonPrimitive("xml"));
		
		List<String> tags = Arrays.asList(new String[]{"tag1"});
		Map<Integer,ModifierEntry> queryModifierList = new HashMap<Integer, ModifierEntry>();
		Map<Integer,ModifierEntry> queryResultModifierList = new HashMap<Integer, ModifierEntry>();
		String description = "description:";
		
		BindVariable name = new BindVariable();
		name.setName("name");
		name.setRequired(true);
		name.setDescription("");
		
		BindVariable phone = new BindVariable();
		phone.setName("phone");
		phone.setRequired(true);
		phone.setDescription("");
		
		BindVariable address = new BindVariable();
		address.setName("address");
		address.setRequired(false);
		address.setDescription("");
		address.setDefaultValue("JunitAddress");
		
		bindVars.put("name", name);
		bindVars.put("phone", phone);
		bindVars.put("address", address);
		
		
		params.setBindVariables(bindVars);
		params.setMetaData(metadata);
		params.setOutputFormat(outputFormat);
		params.setQueryModifiers(queryModifierList);
		params.setQueryResultModifiers(queryResultModifierList);
		params.setQueryTemplate(queryTemplate);
		params.setDescription(description);
		params.setTags(tags);
		
		
		try {
			 managementTasks.createQueryEndpoint(QUERY_ENDPOINT_NAME, WORKSPACE_NAME, PROFILE_NAME, GSONUtil.getGSONInstance().toJsonTree(params).getAsJsonObject(), CREATED_BY);
			
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	
	}
	
	private void createDeleteEndpoint(IManagementTasks managementTasks)
	{
		
		DeleteEndpointRequestParameter params = new DeleteEndpointRequestParameter();
		
		List<String> tags = Arrays.asList(new String[]{"tag1"});

		
		params.setBindVariables(bindVars);
		params.setQueryTemplate(queryTemplate);
		params.setTags(tags);
		
		
		try {
			managementTasks.createDeleteEndpoint(DELETE_ENDPOINT_NAME, WORKSPACE_NAME, PROFILE_NAME, GSONUtil.getGSONInstance().toJsonTree(params).getAsJsonObject(), CREATED_BY);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

	}
	
	private void createSubmitEndpoint(IManagementTasks managementTasks)
	{
		SubmitEndpointRequestParameter params = new SubmitEndpointRequestParameter();
		
		JsonObject properties = new JsonObject();
		Map<Integer,ModifierEntry> payloadModifier = new HashMap<Integer, ModifierEntry>();
		
		params.setProperties(properties);
		params.setSubmitPayloadModifiers(payloadModifier);
		
		
		
		try {
			managementTasks.createSubmitEndpoint(SUBMIT_ENDPOINT_NAME, WORKSPACE_NAME, PROFILE_NAME, GSONUtil.getGSONInstance().toJsonTree(params).getAsJsonObject(), CREATED_BY);
			
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	private IManagementTasks getManagementTaskBean()
	{
		BundleContext context = Activator.getContext();
		ServiceReference sf = context.getServiceReference(IManagementTasks.class.getName());
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
	
	private IExecutionTasks getExecutionTaskBean()
	{
		BundleContext context = Activator.getContext();
		ServiceReference sf = context.getServiceReference(IExecutionTasks.class.getName());
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
	
	// execute a QueryEndpoint
	
		public void test_executeQueryEndpoint()
		{
			IExecutionTasks executionTaks = getExecutionTaskBean();
			IManagementTasks managementTasks = getManagementTaskBean();
			
			Profile profile;
			try {
				profile = managementTasks.getProfile(WORKSPACE_NAME, PROFILE_NAME);
				QueryEndpoint queryEndpoint = profile.getQueryEndpoints().get(QUERY_ENDPOINT_NAME);
				
				Map<String,String> runtimeParameters = new HashMap<String, String>();
				
				String expectedName = "JunitName";
				String expectedPhone = "JunitPhone";
				String expectedAddress = "JunitAddress";
				runtimeParameters.put("name", expectedName);
				runtimeParameters.put("phone", expectedPhone);
				runtimeParameters.put("address", expectedAddress);
				QueryResult queryResult = executionTaks.executeQueryEndpoint(USER, runtimeParameters, profile , queryEndpoint);
				
				assertNotNull(queryResult);
				assertEquals(false, queryResult.isCallback());
				assertEquals("text", queryResult.getMimeType());
				assertNull(queryResult.getErrorMessage());
				assertEquals(false, queryResult.isError());
				assertNotNull(queryResult.getData());
				
				JsonObject result = parser.parse(new String(queryResult.getData())).getAsJsonObject();
				String actualQuery = result.get("query").getAsString();
				assertEquals(expectedQuery, actualQuery);
				
			} catch (Exception e) {
					fail(e.getMessage());
			}
			
		}

		public void test_executeQueryEndpointMissingOptionalParam()
		{
			IExecutionTasks executionTaks = getExecutionTaskBean();
			IManagementTasks managementTasks = getManagementTaskBean();
			
			Profile profile;
			try {
				profile = managementTasks.getProfile(WORKSPACE_NAME, PROFILE_NAME);
				QueryEndpoint queryEndpoint = profile.getQueryEndpoints().get(QUERY_ENDPOINT_NAME);
				
				Map<String,String> runtimeParameters = new HashMap<String, String>();
				
				String expectedName = "JunitName";
				String expectedPhone = "JunitPhone";
				String expectedAddress = "JunitAddress";
				runtimeParameters.put("name", expectedName);
				runtimeParameters.put("phone", expectedPhone);
				
				QueryResult queryResult = executionTaks.executeQueryEndpoint(USER, runtimeParameters, profile , queryEndpoint);
				
				assertNotNull(queryResult);
				assertEquals(false, queryResult.isCallback());
				assertEquals("text", queryResult.getMimeType());
				assertNull(queryResult.getErrorMessage());
				assertEquals(false, queryResult.isError());
				assertNotNull(queryResult.getData());
				
				JsonObject result = parser.parse(new String(queryResult.getData())).getAsJsonObject();
				String actualQuery = result.get("query").getAsString();
				assertEquals(expectedQuery, actualQuery);
				
			} catch (Exception e) {
					fail(e.getMessage());
			}

		}

		public void test_executeQueryEndpointMissingMandatoryParam()
		{
			IExecutionTasks executionTaks = getExecutionTaskBean();
			IManagementTasks managementTasks = getManagementTaskBean();
			
			Profile profile;
			try {
				profile = managementTasks.getProfile(WORKSPACE_NAME, PROFILE_NAME);
				QueryEndpoint queryEndpoint = profile.getQueryEndpoints().get(QUERY_ENDPOINT_NAME);
				
				Map<String,String> runtimeParameters = new HashMap<String, String>();
				
				String expectedName = "JunitName";
				
				String expectedAddress = "JunitAddress";
				runtimeParameters.put("name", expectedName);
				
				runtimeParameters.put("address", expectedAddress);
				executionTaks.executeQueryEndpoint(USER, runtimeParameters, profile , queryEndpoint);
				
				fail("Must throw an exception");
				
			} catch (ExecutionTaskException e) {
					assertEquals("Mandatory attribute ["+ "phone" + "] not provided", e.getMessage());
			}
			catch(Exception e)
			{
				fail(e.getMessage());
			}

		}
		
		// execute Delete Endpoint
		
		public void test_executeDeleteEndpoint()
		{
			IExecutionTasks executionTaks = getExecutionTaskBean();
			IManagementTasks managementTasks = getManagementTaskBean();
			
			Profile profile;
			try {
				profile = managementTasks.getProfile(WORKSPACE_NAME, PROFILE_NAME);
				DeleteEndpoint deleteEndpoint = profile.getDeleteEndpoints().get(DELETE_ENDPOINT_NAME);
				
				Map<String,String> runtimeParameters = new HashMap<String, String>();
				
				String expectedName = "JunitName";
				String expectedPhone = "JunitPhone";
				String expectedAddress = "JunitAddress";
				runtimeParameters.put("name", expectedName);
				runtimeParameters.put("phone", expectedPhone);
				runtimeParameters.put("address", expectedAddress);
				QueryResult queryResult = executionTaks.executeDeleteEndpoint(USER, runtimeParameters, profile, deleteEndpoint);
				
				assertNotNull(queryResult);
				assertEquals(false, queryResult.isCallback());
				assertEquals("text", queryResult.getMimeType());
				assertNull(queryResult.getErrorMessage());
				assertEquals(false, queryResult.isError());
				assertNotNull(queryResult.getData());
				
				JsonObject result = parser.parse(new String(queryResult.getData())).getAsJsonObject();
				String actualQuery = result.get("query").getAsString();
				assertEquals(expectedQuery, actualQuery);
				
			} catch (Exception e) {
					fail(e.getMessage());
			}

		}
		
		public void test_executeDeleteEndpointMissingOptionalParam()
		{
			IExecutionTasks executionTaks = getExecutionTaskBean();
			IManagementTasks managementTasks = getManagementTaskBean();
			
			Profile profile;
			try {
				profile = managementTasks.getProfile(WORKSPACE_NAME, PROFILE_NAME);
				DeleteEndpoint deleteEndpoint = profile.getDeleteEndpoints().get(DELETE_ENDPOINT_NAME);
				
				Map<String,String> runtimeParameters = new HashMap<String, String>();
				
				String expectedName = "JunitName";
				String expectedPhone = "JunitPhone";
				
				runtimeParameters.put("name", expectedName);
				runtimeParameters.put("phone", expectedPhone);
				
				QueryResult queryResult = executionTaks.executeDeleteEndpoint(USER, runtimeParameters, profile, deleteEndpoint);
				
				assertNotNull(queryResult);
				assertEquals(false, queryResult.isCallback());
				assertEquals("text", queryResult.getMimeType());
				assertNull(queryResult.getErrorMessage());
				assertEquals(false, queryResult.isError());
				assertNotNull(queryResult.getData());
				
				JsonObject result = parser.parse(new String(queryResult.getData())).getAsJsonObject();
				String actualQuery = result.get("query").getAsString();
				assertEquals(expectedQuery, actualQuery);
				
			} catch (Exception e) {
					fail(e.getMessage());
			}

		}
		
		public void test_executeDeleteEndpointMissingMandatoryParam()
		{
			IExecutionTasks executionTaks = getExecutionTaskBean();
			IManagementTasks managementTasks = getManagementTaskBean();
			
			Profile profile;
			try {
				profile = managementTasks.getProfile(WORKSPACE_NAME, PROFILE_NAME);
				DeleteEndpoint deleteEndpoint = profile.getDeleteEndpoints().get(DELETE_ENDPOINT_NAME);
				
				Map<String,String> runtimeParameters = new HashMap<String, String>();
				
				String expectedName = "JunitName";
				
				String expectedAddress = "JunitAddress";
				runtimeParameters.put("name", expectedName);
				
				runtimeParameters.put("address", expectedAddress);
				
				executionTaks.executeDeleteEndpoint(USER, runtimeParameters, profile, deleteEndpoint);
				
				fail("Must throw an exception");
				
			} catch (ExecutionTaskException e) {
					assertEquals("Mandatory attribute ["+ "phone" + "] not provided", e.getMessage());
			}
			catch(Exception e)
			{
				fail(e.getMessage());
			}
	
		}
	
		// execute Submit Endpoint
		
		public void test_executeSubmitEndpointWithInputStream()
		{
			IExecutionTasks executionTaks = getExecutionTaskBean();
			IManagementTasks managementTasks = getManagementTaskBean();
			
			Profile profile;
			try {
				String testData = "this is my test data right here";
				ByteArrayInputStream bis = new ByteArrayInputStream(testData.getBytes());
				
				profile = managementTasks.getProfile(WORKSPACE_NAME, PROFILE_NAME);
				SubmitEndpoint submitEndpoint = profile.getSubmitEndpoints().get(SUBMIT_ENDPOINT_NAME);
				
				QueryResult queryResult = executionTaks.executeSubmitEndpoint(USER,bis,profile,submitEndpoint);
				
				assertNotNull(queryResult);
				assertEquals(false, queryResult.isCallback());
				assertEquals("text", queryResult.getMimeType());
				assertNull(queryResult.getErrorMessage());
				assertEquals(false, queryResult.isError());
				assertNotNull(queryResult.getData());
				assertEquals(testData, new String(queryResult.getData()));
				
			} catch (Exception e) {
					fail(e.getMessage());
			}

		}
		public void test_executeSubmitEndpointWithStringData()
		{
			IExecutionTasks executionTaks = getExecutionTaskBean();
			IManagementTasks managementTasks = getManagementTaskBean();
			
			Profile profile;
			try {
				String testData = "this is my test data right here";
				
				profile = managementTasks.getProfile(WORKSPACE_NAME, PROFILE_NAME);
				SubmitEndpoint submitEndpoint = profile.getSubmitEndpoints().get(SUBMIT_ENDPOINT_NAME);
				
				QueryResult queryResult = executionTaks.executeSubmitEndpoint(USER,testData,profile,submitEndpoint);
				
				assertNotNull(queryResult);
				assertEquals(false, queryResult.isCallback());
				assertEquals("text", queryResult.getMimeType());
				assertNull(queryResult.getErrorMessage());
				assertEquals(false, queryResult.isError());
				assertNotNull(queryResult.getData());
				assertEquals(testData, new String(queryResult.getData()));
				
			} catch (Exception e) {
					fail(e.getMessage());
			}

		}

}
