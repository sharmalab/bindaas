package edu.emory.cci.bindaas.junit.core;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;


import org.eclipse.osgi.framework.console.CommandProvider;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import edu.emory.cci.bindaas.core.api.IManagementTasks;
import edu.emory.cci.bindaas.core.bundle.BindaasOSGIConsole;
import edu.emory.cci.bindaas.core.exception.DuplicateException;
import edu.emory.cci.bindaas.core.exception.NotFoundException;
import edu.emory.cci.bindaas.core.exception.FrameworkEntityException.Type;
import edu.emory.cci.bindaas.core.model.DeleteEndpointRequestParameter;
import edu.emory.cci.bindaas.core.model.QueryEndpointRequestParameter;
import edu.emory.cci.bindaas.core.model.SubmitEndpointRequestParameter;
import edu.emory.cci.bindaas.framework.model.BindVariable;
import edu.emory.cci.bindaas.framework.model.DeleteEndpoint;
import edu.emory.cci.bindaas.framework.model.ModifierEntry;
import edu.emory.cci.bindaas.framework.model.Profile;
import edu.emory.cci.bindaas.framework.model.QueryEndpoint;
import edu.emory.cci.bindaas.framework.model.Stage;
import edu.emory.cci.bindaas.framework.model.SubmitEndpoint;
import edu.emory.cci.bindaas.framework.model.Workspace;
import edu.emory.cci.bindaas.framework.util.GSONUtil;
import edu.emory.cci.bindaas.junit.mock.MockProvider;

public class ManagementTasksTest extends TestCase {
	
	private static String WORKSPACE_NAME = "junit";
	private static String CREATED_BY = "junit";
	private static String PROVIDER_ID = MockProvider.class.getName();
	private static int PROVIDER_VERSION = 1;
	private static String PROFILE_NAME = "testProfile";
	private static String QUERY_ENDPOINT_NAME = "testQuery";
	private static String DELETE_ENDPOINT_NAME = "testDelete";
	private static String SUBMIT_ENDPOINT_NAME = "testSubmit";
	
	@Override
	protected void setUp() throws Exception {
	
		super.setUp();
		List<String> workspacesToDelete = new ArrayList<String>();
		for(Workspace workspace : getManagementTaskBean().listWorkspaces())
		{
			if(workspace.getName().startsWith(WORKSPACE_NAME))
				workspacesToDelete.add(workspace.getName());
				
		}
		for(String workspace : workspacesToDelete)
		{
			getManagementTaskBean().deleteWorkspace(workspace);	
		}
	}
	
	private boolean doesWorkspaceExist(String workspaceName)
	{
		File file = new File("bindaas-metadata/" + workspaceName + ".workspace");
		return file.isFile();
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
	
	
//	/**
//	 * Create operations
//	 */
//	
//	public Workspace createWorkspace(String name , JsonObject parameters , String createdBy) throws Exception;
//	public Profile createProfile(String name , String workspaceName,JsonObject parameters , String createdBy) throws Exception;
//	public QueryEndpoint createQueryEndpoint(String name , String workspaceName,String profileName ,JsonObject parameters, String createdBy) throws Exception;
//	public DeleteEndpoint createDeleteEndpoint(String name , String workspaceName,String profileName ,JsonObject parameters, String createdBy) throws Exception;
//	public SubmitEndpoint createSubmitEndpoint(String name , String workspaceName,String profileName ,JsonObject parameters, String createdBy) throws Exception;
	
	public void test_createWorkspace()
	{
		IManagementTasks managementTasks = getManagementTaskBean();
		
		JsonObject props = new JsonObject();
		try {
			Workspace workspace = managementTasks.createWorkspace(WORKSPACE_NAME, props , CREATED_BY);
			assertEquals(WORKSPACE_NAME, workspace.getName());
			assertEquals(CREATED_BY, workspace.getCreatedBy());
			assertEquals(props, workspace.getParams());
			assertTrue(doesWorkspaceExist(WORKSPACE_NAME));
			
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	
		
	}
	
	public void test_createProfile()
	{
		test_createWorkspace();
		IManagementTasks managementTasks = getManagementTaskBean();
		JsonObject parameters = new JsonObject();
		parameters.add("providerId", new JsonPrimitive(PROVIDER_ID));
		parameters.add("providerVersion" , new JsonPrimitive(PROVIDER_VERSION));
		
		JsonObject dataSource = new JsonObject();
		parameters.add("dataSource", dataSource);
		
		try {
			Profile profile = managementTasks.createProfile(PROFILE_NAME, WORKSPACE_NAME, parameters, CREATED_BY);
			assertEquals(PROFILE_NAME, profile.getName());
			assertEquals(CREATED_BY, profile.getCreatedBy());
			assertEquals(PROVIDER_ID, profile.getProviderId());
			assertEquals(PROVIDER_VERSION, profile.getProviderVersion());
			assertEquals(new JsonPrimitive(true), profile.getDataSource().get("validated"));
			assertEquals(profile, managementTasks.getProfile(WORKSPACE_NAME, PROFILE_NAME));
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
	}
	
	public void test_createQueryEndpoint()
	{
		test_createProfile();
		
		IManagementTasks managementTasks = getManagementTaskBean();
		QueryEndpointRequestParameter params = new QueryEndpointRequestParameter();
		Map<String,BindVariable> bindVars = new HashMap<String,BindVariable>();
		JsonObject metadata = new JsonObject();
		JsonObject outputFormat = new JsonObject();
		String queryTemplate = "select * from mytable";
		List<String> tags = Arrays.asList(new String[]{"tag1"});
		Map<Integer,ModifierEntry> queryModifierList = new HashMap<Integer, ModifierEntry>();
		Map<Integer,ModifierEntry> queryResultModifierList = new HashMap<Integer, ModifierEntry>();
		String description = "description:";
		
		params.setBindVariables(bindVars);
		params.setMetaData(metadata);
		params.setOutputFormat(outputFormat);
		params.setQueryModifiers(queryModifierList);
		params.setQueryResultModifiers(queryResultModifierList);
		params.setQueryTemplate(queryTemplate);
		params.setDescription(description);
		params.setTags(tags);
		
		
		try {
			QueryEndpoint qe = managementTasks.createQueryEndpoint(QUERY_ENDPOINT_NAME, WORKSPACE_NAME, PROFILE_NAME, GSONUtil.getGSONInstance().toJsonTree(params).getAsJsonObject(), CREATED_BY);
			assertEquals(QUERY_ENDPOINT_NAME, qe.getName());
			assertEquals(CREATED_BY, qe.getCreatedBy());
			assertEquals(bindVars.size(), qe.getBindVariables().size());
			assertEquals(metadata, qe.getMetaData());
			assertEquals(outputFormat, qe.getOutputFormat());
			assertEquals(queryModifierList.size(), qe.getQueryModifiers().size());
			assertEquals(queryResultModifierList.size(), qe.getQueryResultModifiers().size());
			assertEquals(queryTemplate, qe.getQueryTemplate());
			assertEquals(Stage.UNVERIFIED, qe.getStage());
			assertEquals(tags.iterator().next(), qe.getTags().iterator().next());
			assertEquals(description + "verified", qe.getDescription());
			assertEquals(qe,managementTasks.getQueryEndpoint(WORKSPACE_NAME, PROFILE_NAME, QUERY_ENDPOINT_NAME));
			
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		
	}
	
	public void test_createDeleteEndpoint()
	{
		
		test_createProfile();
		
		IManagementTasks managementTasks = getManagementTaskBean();
		DeleteEndpointRequestParameter params = new DeleteEndpointRequestParameter();

		Map<String,BindVariable> bindVars = new HashMap<String,BindVariable>();
		String queryTemplate = "delete from mytable";
		List<String> tags = Arrays.asList(new String[]{"tag1"});

		
		params.setBindVariables(bindVars);
		params.setQueryTemplate(queryTemplate);
		params.setTags(tags);
		
		
		try {
			DeleteEndpoint de = managementTasks.createDeleteEndpoint(DELETE_ENDPOINT_NAME, WORKSPACE_NAME, PROFILE_NAME, GSONUtil.getGSONInstance().toJsonTree(params).getAsJsonObject(), CREATED_BY);
			assertEquals(DELETE_ENDPOINT_NAME, de.getName());
			assertEquals(CREATED_BY, de.getCreatedBy());
			assertEquals(bindVars.keySet(), de.getBindVariables().keySet());
			assertEquals(queryTemplate, de.getQueryTemplate());
			assertEquals(Stage.UNVERIFIED, de.getStage());
			assertEquals(tags.iterator().next(), de.getTags().iterator().next());
			assertEquals(de,managementTasks.getDeleteEndpoint(WORKSPACE_NAME, PROFILE_NAME, DELETE_ENDPOINT_NAME));
			
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	
	}
	
	public void test_createSubmitEndpoint()
	{
		
		test_createProfile();
		
		IManagementTasks managementTasks = getManagementTaskBean();
		SubmitEndpointRequestParameter params = new SubmitEndpointRequestParameter();
		
		JsonObject properties = new JsonObject();
		Map<Integer,ModifierEntry> payloadModifier = new HashMap<Integer, ModifierEntry>();
		
		params.setProperties(properties);
		params.setSubmitPayloadModifiers(payloadModifier);
		
		
		
		try {
			SubmitEndpoint se = managementTasks.createSubmitEndpoint(SUBMIT_ENDPOINT_NAME, WORKSPACE_NAME, PROFILE_NAME, GSONUtil.getGSONInstance().toJsonTree(params).getAsJsonObject(), CREATED_BY);
			assertEquals(SUBMIT_ENDPOINT_NAME, se.getName());
			assertEquals(CREATED_BY, se.getCreatedBy());
			assertEquals(properties, se.getProperties());
			assertEquals(payloadModifier.size(), se.getSubmitPayloadModifiers().size());
			assertEquals(se, managementTasks.getSubmitEndpoint(WORKSPACE_NAME, PROFILE_NAME, SUBMIT_ENDPOINT_NAME));
			
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
//	
//	
//	/**
//	 *  Delete operations
//	 */
//	
//	public void deleteWorkspace(String workspaceName) throws Exception;
//	public void deleteProfile(String workspaceName,String profileName) throws Exception;
//	public QueryEndpoint deleteQueryEndpoint(String workspaceName,String profileName , String queryEndpointName) throws Exception;
//	public DeleteEndpoint deleteDeleteEndpoint(String workspaceName,String profileName , String deleteEndpointName) throws Exception;
//	public SubmitEndpoint deleteSubmitEndpoint(String workspaceName,String profileName , String submitEndpointName) throws Exception;
//	
	
	
	public void test_deleteWorkspace()
	{
		test_createWorkspace();
		IManagementTasks managementTasks = getManagementTaskBean();
		try {
			managementTasks.deleteWorkspace(WORKSPACE_NAME);
			assertFalse(doesWorkspaceExist(WORKSPACE_NAME));
			try{
				managementTasks.getWorkspace(WORKSPACE_NAME);
			}
			catch(NotFoundException nfe)
			{
				assertEquals(nfe.getName(), WORKSPACE_NAME);
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		
	}
	
	public void test_deleteProfile()
	{
		test_createProfile();
		IManagementTasks managementTasks = getManagementTaskBean();
		try {
			managementTasks.deleteProfile(WORKSPACE_NAME, PROFILE_NAME);
			try{
				managementTasks.getProfile(WORKSPACE_NAME, PROFILE_NAME);
			}
			catch(NotFoundException nfe)
			{
				assertEquals(nfe.getName(), PROFILE_NAME);
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
	}
	
	public void test_deleteQueryEndpoint()
	{
		test_createQueryEndpoint();
		IManagementTasks managementTasks = getManagementTaskBean();
		
		try {
			managementTasks.deleteQueryEndpoint(WORKSPACE_NAME, PROFILE_NAME, QUERY_ENDPOINT_NAME);
			
				try{
					managementTasks.getQueryEndpoint(WORKSPACE_NAME, PROFILE_NAME, QUERY_ENDPOINT_NAME);
				}
				catch(NotFoundException nfe)
				{
					assertEquals(nfe.getName(), QUERY_ENDPOINT_NAME);
				}
		
		
			
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	public void test_deleteDeleteEndpoint()
	{
		test_createDeleteEndpoint();
		IManagementTasks managementTasks = getManagementTaskBean();
		
		try {
			managementTasks.deleteDeleteEndpoint(WORKSPACE_NAME, PROFILE_NAME, DELETE_ENDPOINT_NAME);
			try{
				 managementTasks.getDeleteEndpoint(WORKSPACE_NAME, PROFILE_NAME, DELETE_ENDPOINT_NAME);
			}
			catch(NotFoundException nfe)
			{
				assertEquals(nfe.getName(), DELETE_ENDPOINT_NAME);
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	public void test_deleteSubmitEndpoint()
	{
		test_createSubmitEndpoint();
		IManagementTasks managementTasks = getManagementTaskBean();
		
		try {
			managementTasks.deleteSubmitEndpoint(WORKSPACE_NAME, PROFILE_NAME, SUBMIT_ENDPOINT_NAME);
			try{
				managementTasks.getSubmitEndpoint(WORKSPACE_NAME, PROFILE_NAME, SUBMIT_ENDPOINT_NAME);
			}
			catch(NotFoundException nfe)
			{
				assertEquals(nfe.getName(), SUBMIT_ENDPOINT_NAME);
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

	}
	
//	
//	/**
//	 * Update operations
//	 */
//	
//	
//	public Profile updateProfile(String name , String workspaceName,JsonObject parameters, String updatedBy) throws Exception;
//	public QueryEndpoint updateQueryEndpoint(String name , String workspaceName,String profileName ,JsonObject parameters, String updatedBy) throws Exception;
//	public DeleteEndpoint updateDeleteEndpoint(String name , String workspaceName,String profileName ,JsonObject parameters, String updatedBy) throws Exception;
//	public SubmitEndpoint updateSubmitEndpoint(String name , String workspaceName,String profileName ,JsonObject parameters, String updatedBy) throws Exception;
//	

	public void test_updateProfile()
	{
		test_createProfile();
		IManagementTasks managementTasks = getManagementTaskBean();
		try {
			Profile oldProfile = managementTasks.getProfile(WORKSPACE_NAME, PROFILE_NAME);
			String oldTimeCreated = oldProfile.getTimeCreated();

			JsonObject parameters = new JsonObject();
			parameters.add("providerId", new JsonPrimitive(PROVIDER_ID));
			parameters.add("providerVersion" , new JsonPrimitive(PROVIDER_VERSION));
			
			JsonObject dataSource = new JsonObject();
			parameters.add("dataSource", dataSource);

			Profile updatedProfile = managementTasks.updateProfile(PROFILE_NAME, WORKSPACE_NAME, parameters, CREATED_BY);
			assertNotSame(oldTimeCreated, updatedProfile.getTimeCreated());
			assertEquals(updatedProfile, managementTasks.getProfile(WORKSPACE_NAME, PROFILE_NAME));
			
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
	
	public void test_updateQueryEndpoint()
	{
		test_createQueryEndpoint();
		
		IManagementTasks managementTasks = getManagementTaskBean();
		
		
		QueryEndpointRequestParameter params = new QueryEndpointRequestParameter();
		Map<String,BindVariable> bindVars = new HashMap<String,BindVariable>();
		JsonObject metadata = new JsonObject();
		JsonObject outputFormat = new JsonObject();
		String queryTemplate = "select * from mytable";
		List<String> tags = Arrays.asList(new String[]{"tag1"});
		Map<Integer,ModifierEntry> queryModifierList = new HashMap<Integer, ModifierEntry>();
		Map<Integer,ModifierEntry> queryResultModifierList = new HashMap<Integer, ModifierEntry>();
		String description = "description:";
		
		params.setBindVariables(bindVars);
		params.setMetaData(metadata);
		params.setOutputFormat(outputFormat);
		params.setQueryModifiers(queryModifierList);
		params.setQueryResultModifiers(queryResultModifierList);
		params.setQueryTemplate(queryTemplate);
		params.setDescription(description);
		params.setTags(tags);
		
		
		try {
			QueryEndpoint oldQueryEndpoint = managementTasks.getQueryEndpoint(WORKSPACE_NAME, PROFILE_NAME, QUERY_ENDPOINT_NAME);
			QueryEndpoint qe = managementTasks.updateQueryEndpoint(QUERY_ENDPOINT_NAME, WORKSPACE_NAME, PROFILE_NAME, GSONUtil.getGSONInstance().toJsonTree(params).getAsJsonObject(), CREATED_BY);
			assertEquals(QUERY_ENDPOINT_NAME, qe.getName());
			assertEquals(CREATED_BY, qe.getCreatedBy());
			assertEquals(bindVars.size(), qe.getBindVariables().size());
			assertEquals(metadata, qe.getMetaData());
			assertEquals(outputFormat, qe.getOutputFormat());
			assertEquals(queryModifierList.size(), qe.getQueryModifiers().size());
			assertEquals(queryResultModifierList.size(), qe.getQueryResultModifiers().size());
			assertEquals(queryTemplate, qe.getQueryTemplate());
			assertEquals(Stage.UNVERIFIED, qe.getStage());
			assertEquals(tags.iterator().next(), qe.getTags().iterator().next());
			assertEquals(description + "verified", qe.getDescription());
			assertEquals(qe,managementTasks.getQueryEndpoint(WORKSPACE_NAME, PROFILE_NAME, QUERY_ENDPOINT_NAME));
			assertNotSame(oldQueryEndpoint	, qe);
			
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		
		
	}
	
	public void test_updateDeleteEndpoint()
	{
		test_createDeleteEndpoint();
		
		IManagementTasks managementTasks = getManagementTaskBean();
		DeleteEndpointRequestParameter params = new DeleteEndpointRequestParameter();

		Map<String,BindVariable> bindVars = new HashMap<String,BindVariable>();
		String queryTemplate = "delete from mytable";
		List<String> tags = Arrays.asList(new String[]{"tag1"});

		
		params.setBindVariables(bindVars);
		params.setQueryTemplate(queryTemplate);
		params.setTags(tags);
		
		
		try {
			DeleteEndpoint oldDeleteEndpoint = managementTasks.getDeleteEndpoint(WORKSPACE_NAME, PROFILE_NAME, DELETE_ENDPOINT_NAME); 
			DeleteEndpoint de = managementTasks.updateDeleteEndpoint(DELETE_ENDPOINT_NAME, WORKSPACE_NAME, PROFILE_NAME, GSONUtil.getGSONInstance().toJsonTree(params).getAsJsonObject(), CREATED_BY);
			assertEquals(DELETE_ENDPOINT_NAME, de.getName());
			assertEquals(CREATED_BY, de.getCreatedBy());
			assertEquals(bindVars.keySet(), de.getBindVariables().keySet());
			assertEquals(queryTemplate, de.getQueryTemplate());
			assertEquals(Stage.UNVERIFIED, de.getStage());
			assertEquals(tags.iterator().next(), de.getTags().iterator().next());
			assertEquals(de,managementTasks.getDeleteEndpoint(WORKSPACE_NAME, PROFILE_NAME, DELETE_ENDPOINT_NAME));
			assertNotSame(oldDeleteEndpoint, de);
			
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

	}
	
	public void test_updateSubmitEndpoint()
	{
		test_createSubmitEndpoint();
		
		IManagementTasks managementTasks = getManagementTaskBean();
		SubmitEndpointRequestParameter params = new SubmitEndpointRequestParameter();
		
		JsonObject properties = new JsonObject();
		Map<Integer,ModifierEntry> payloadModifier = new HashMap<Integer, ModifierEntry>();
		
		params.setProperties(properties);
		params.setSubmitPayloadModifiers(payloadModifier);
		
		
		
		try {
			SubmitEndpoint oldSubmitEndpoint = managementTasks.getSubmitEndpoint(WORKSPACE_NAME, PROFILE_NAME, SUBMIT_ENDPOINT_NAME);
			SubmitEndpoint se = managementTasks.updateSubmitEndpoint(SUBMIT_ENDPOINT_NAME, WORKSPACE_NAME, PROFILE_NAME, GSONUtil.getGSONInstance().toJsonTree(params).getAsJsonObject(), CREATED_BY);
			assertEquals(SUBMIT_ENDPOINT_NAME, se.getName());
			assertEquals(CREATED_BY, se.getCreatedBy());
			assertEquals(properties, se.getProperties());
			assertEquals(payloadModifier.size(), se.getSubmitPayloadModifiers().size());
			assertEquals(se, managementTasks.getSubmitEndpoint(WORKSPACE_NAME, PROFILE_NAME, SUBMIT_ENDPOINT_NAME));
			assertNotSame(oldSubmitEndpoint, se);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	
//	/**
//	 *  Other operations
//	 */
//	
//	public Collection<Workspace> listWorkspaces() throws Exception;
//	public Workspace getWorkspace(String name) throws Exception;
//	public Profile getProfile(String workspaceName , String profileName) throws Exception;
//	public QueryEndpoint getQueryEndpoint(String workspaceName,String profileName, String queryEndpointName) throws Exception;
//	public DeleteEndpoint getDeleteEndpoint(String workspaceName,String profileName,String deleteEndpointName) throws Exception;
//	public SubmitEndpoint getSubmitEndpoint(String workspaceName,String profileName,String submitEndpointName) throws Exception;
//	
	
	public void test_listWorkspaces()
	{
		IManagementTasks managementTasks = getManagementTaskBean();
		JsonObject props = new JsonObject();
		try {
			Workspace workspace1 = managementTasks.createWorkspace(WORKSPACE_NAME + "_1", props , CREATED_BY);
			Workspace workspace2 = managementTasks.createWorkspace(WORKSPACE_NAME + "_2", props , CREATED_BY);
			Workspace workspace3 = managementTasks.createWorkspace(WORKSPACE_NAME + "_3", props , CREATED_BY);
			
			Collection<Workspace> listOfWorkspaces = managementTasks.listWorkspaces(); 
			assertTrue(listOfWorkspaces.contains(workspace1));
			assertTrue(listOfWorkspaces.contains(workspace2));
			assertTrue(listOfWorkspaces.contains(workspace3));
			assertTrue(listOfWorkspaces.size() >= 3);
			
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
	}
	
	public void test_duplicateWorkspace()
	{
		test_createWorkspace();
		IManagementTasks managementTasks = getManagementTaskBean();
		try {
			managementTasks.createWorkspace(WORKSPACE_NAME, null, CREATED_BY);
			fail("Duplicate Exception not thrown");
		} catch (DuplicateException e) {
			assertEquals(WORKSPACE_NAME, e.getName());
			assertEquals(Type.Workspace, e.getType());
		}
		catch(Exception e)
		{
			fail(e.getMessage());
		}
		
		
	}
	
	public void test_duplicateProfile()
	{
		test_createProfile();
		IManagementTasks managementTasks = getManagementTaskBean();
		try {
			managementTasks.createProfile(PROFILE_NAME,WORKSPACE_NAME,null,CREATED_BY);
			fail("Duplicate Exception not thrown");
		} catch (DuplicateException e) {
			assertEquals(PROFILE_NAME, e.getName());
			assertEquals(Type.Profile, e.getType());
		}
		catch(Exception e)
		{
			fail(e.getMessage());
		}
		
		
	}
	
	public void test_duplicateQueryEndpoint()
	{
		test_createQueryEndpoint();
		IManagementTasks managementTasks = getManagementTaskBean();
		try {
			managementTasks.createQueryEndpoint(QUERY_ENDPOINT_NAME, WORKSPACE_NAME, PROFILE_NAME, null, CREATED_BY);
			fail("Duplicate Exception not thrown");
		} catch (DuplicateException e) {
			assertEquals(QUERY_ENDPOINT_NAME, e.getName());
			assertEquals(Type.QueryEndpoint, e.getType());
		}
		catch(Exception e)
		{
			fail(e.getMessage());
		}
		
		
	}
	
	public void test_duplicateDeleteEndpoint()
	{
		test_createDeleteEndpoint();
		IManagementTasks managementTasks = getManagementTaskBean();
		try {
			managementTasks.createDeleteEndpoint(DELETE_ENDPOINT_NAME, WORKSPACE_NAME, PROFILE_NAME, null, CREATED_BY);
			fail("Duplicate Exception not thrown");
		} catch (DuplicateException e) {
			assertEquals(DELETE_ENDPOINT_NAME, e.getName());
			assertEquals(Type.DeleteEndpoint, e.getType());
		}
		catch(Exception e)
		{
			fail(e.getMessage());
		}
		
		
	}
	
	public void test_duplicateSubmitEndpoint()
	{
		test_createSubmitEndpoint();
		IManagementTasks managementTasks = getManagementTaskBean();
		try {
			managementTasks.createSubmitEndpoint(SUBMIT_ENDPOINT_NAME, WORKSPACE_NAME, PROFILE_NAME, null, CREATED_BY);
			fail("Duplicate Exception not thrown");
		} catch (DuplicateException e) {
			assertEquals(SUBMIT_ENDPOINT_NAME, e.getName());
			assertEquals(Type.SubmitEndpoint, e.getType());
		}
		catch(Exception e)
		{
			fail(e.getMessage());
		}
		
		
	}
	
	
//	/**
//	 * publish operations
//	 * 
//	 */
//	
//	public void publishQueryEndpoint(String workspaceName,String profileName,String queryEndpointName) throws Exception;
//	public void publishDeleteEndpoint(String workspaceName,String profileName,String deleteEndpointName) throws Exception;
	
	
	public void test_publishQueryEndpoint()
	{
		test_createQueryEndpoint();
		IManagementTasks managementTasks = getManagementTaskBean();
		try {
			Stage oldStage = managementTasks.getQueryEndpoint(WORKSPACE_NAME, PROFILE_NAME, QUERY_ENDPOINT_NAME).getStage();
			assertEquals(Stage.UNVERIFIED, oldStage);
			managementTasks.publishQueryEndpoint(WORKSPACE_NAME, PROFILE_NAME, QUERY_ENDPOINT_NAME);
			Stage newStage = managementTasks.getQueryEndpoint(WORKSPACE_NAME, PROFILE_NAME, QUERY_ENDPOINT_NAME).getStage();
			assertEquals(Stage.VERIFIED, newStage);
			
			
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	public void test_publishDeleteEndpoint()
	{
		test_createDeleteEndpoint();
		IManagementTasks managementTasks = getManagementTaskBean();
		try {
			Stage oldStage = managementTasks.getDeleteEndpoint(WORKSPACE_NAME, PROFILE_NAME, DELETE_ENDPOINT_NAME).getStage();
			assertEquals(Stage.UNVERIFIED, oldStage);
			managementTasks.publishDeleteEndpoint(WORKSPACE_NAME, PROFILE_NAME, DELETE_ENDPOINT_NAME);
			Stage newStage = managementTasks.getDeleteEndpoint(WORKSPACE_NAME, PROFILE_NAME, DELETE_ENDPOINT_NAME).getStage();
			assertEquals(Stage.VERIFIED, newStage);
			
			
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	

	// add negative test cases
	
	public void test_createProfileWithWrongProvider()
	{
		
	}
	
	
	
}
