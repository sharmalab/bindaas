package edu.emory.cci.bindaas.junit.provider.aime;

import java.io.File;
import java.io.FileInputStream;

import junit.framework.TestCase;

import org.apache.cxf.helpers.IOUtils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import edu.emory.cci.bindaas.core.api.IManagementTasks;
import edu.emory.cci.bindaas.core.exception.ProviderNotFoundException;
import edu.emory.cci.bindaas.framework.model.Profile;
import edu.emory.cci.bindaas.framework.model.Workspace;
import edu.emory.cci.bindaas.framework.util.GSONUtil;
import edu.emory.cci.bindaas.junit.provider.aime.bundle.Activator;

public class AIMEProviderTestCase extends TestCase{

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		IManagementTasks managementTask = getManagementTaskBean();
		managementTask.deleteWorkspace("aime_junit");

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
	
	
	public void test_createProfile()
	{

		try {
			File testDataFile = new File("testData/aime.workspace.2.testcase");
			String content = IOUtils.readStringFromStream(new FileInputStream(testDataFile));
			Workspace testWorkspace = GSONUtil.getGSONInstance().fromJson(content, Workspace.class);
			IManagementTasks managementTask = getManagementTaskBean();
			
			// 1. Create a new Workspace
			
			managementTask.createWorkspace(testWorkspace.getName(), testWorkspace.getParams(), testWorkspace.getCreatedBy());
			
			// 2. Create a new Profile
			Profile testProfile = testWorkspace.getProfiles().values().iterator().next();
			JsonObject profileParams = new JsonObject();
			profileParams.add("providerId", new JsonPrimitive(testProfile.getProviderId()) );
			profileParams.add("providerVersion", new JsonPrimitive(testProfile.getProviderVersion()) );
			profileParams.add("dataSource" , testProfile.getDataSource());
			
			managementTask.createProfile(testProfile.getName(), testWorkspace.getName(), profileParams, testProfile.getCreatedBy() , "");
		
		}catch (Exception e) {
			fail(e.getMessage());
			
		}
		
	}
	
	public void test_createProfileWithInitialization()
	{
		
		try {
			File testDataFile = new File("testData/aime.workspace.1.testcase");
			String content = IOUtils.readStringFromStream(new FileInputStream(testDataFile));
			Workspace testWorkspace = GSONUtil.getGSONInstance().fromJson(content, Workspace.class);
			IManagementTasks managementTask = getManagementTaskBean();
			
			// 1. Create a new Workspace
			
			managementTask.createWorkspace(testWorkspace.getName(), testWorkspace.getParams(), testWorkspace.getCreatedBy());
			
			// 2. Create a new Profile
			Profile testProfile = testWorkspace.getProfiles().values().iterator().next();
			JsonObject profileParams = new JsonObject();
			profileParams.add("providerId", new JsonPrimitive(testProfile.getProviderId()) );
			profileParams.add("providerVersion", new JsonPrimitive(testProfile.getProviderVersion()) );
			profileParams.add("dataSource" , testProfile.getDataSource());
			
			managementTask.createProfile(testProfile.getName(), testWorkspace.getName(), profileParams, testProfile.getCreatedBy() , "");
		
		}catch (Exception e) {
			fail(e.getMessage());
			
		}
		
		
	}
	
	public void test_createProfileWithWrongParameters_1()
	{

		try {
			File testDataFile = new File("testData/aime.workspace.1.testcase");
			String content = IOUtils.readStringFromStream(new FileInputStream(testDataFile));
			Workspace testWorkspace = GSONUtil.getGSONInstance().fromJson(content, Workspace.class);
			IManagementTasks managementTask = getManagementTaskBean();
			
			// 1. Create a new Workspace
			
			managementTask.createWorkspace(testWorkspace.getName(), testWorkspace.getParams(), testWorkspace.getCreatedBy());
			
			// 2. Create a new Profile
			Profile testProfile = testWorkspace.getProfiles().values().iterator().next();
			JsonObject profileParams = new JsonObject();
			profileParams.add("providerId", new JsonPrimitive(testProfile.getProviderId()) );
			profileParams.add("providerVersion", new JsonPrimitive(testProfile.getProviderVersion() + 2) ); // wrong version specified here
			profileParams.add("dataSource" , testProfile.getDataSource());
			
			managementTask.createProfile(testProfile.getName(), testWorkspace.getName(), profileParams, testProfile.getCreatedBy() ,"");
			fail("Must throw an exception");
		}catch (Exception e) {
			
			assertTrue(e instanceof ProviderNotFoundException);
		}
		
	}
	
	public void test_createProfileWithWrongParameters_2()
	{
		try {
			File testDataFile = new File("testData/aime.workspace.1.testcase");
			String content = IOUtils.readStringFromStream(new FileInputStream(testDataFile));
			Workspace testWorkspace = GSONUtil.getGSONInstance().fromJson(content, Workspace.class);
			IManagementTasks managementTask = getManagementTaskBean();
			
			// 1. Create a new Workspace
			
			managementTask.createWorkspace(testWorkspace.getName(), testWorkspace.getParams(), testWorkspace.getCreatedBy());
			
			// 2. Create a new Profile
			Profile testProfile = testWorkspace.getProfiles().values().iterator().next();
			JsonObject profileParams = new JsonObject();
			profileParams.add("providerId", new JsonPrimitive(testProfile.getProviderId()) );
			profileParams.add("providerVersion", new JsonPrimitive(testProfile.getProviderVersion() ) ); 
			profileParams.add("dataSource" , new JsonObject());
			
			managementTask.createProfile(testProfile.getName(), testWorkspace.getName(), profileParams, testProfile.getCreatedBy(),"");
			fail("Must throw an exception");
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void test_createProfileWithWrongParameters_3()
	{
		try {
			File testDataFile = new File("testData/aime.workspace.1.testcase");
			String content = IOUtils.readStringFromStream(new FileInputStream(testDataFile));
			Workspace testWorkspace = GSONUtil.getGSONInstance().fromJson(content, Workspace.class);
			IManagementTasks managementTask = getManagementTaskBean();
			
			// 1. Create a new Workspace
			
			managementTask.createWorkspace(testWorkspace.getName(), testWorkspace.getParams(), testWorkspace.getCreatedBy());
			
			// 2. Create a new Profile
			Profile testProfile = testWorkspace.getProfiles().values().iterator().next();
			
			managementTask.createProfile(testProfile.getName(), testWorkspace.getName(), null, testProfile.getCreatedBy(),"");
			fail("Must throw an exception");
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
	
}
