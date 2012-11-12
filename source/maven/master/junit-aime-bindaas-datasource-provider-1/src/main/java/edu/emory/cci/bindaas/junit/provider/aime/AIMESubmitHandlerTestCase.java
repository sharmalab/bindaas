package edu.emory.cci.bindaas.junit.provider.aime;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;

import org.apache.cxf.helpers.IOUtils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import edu.emory.cci.bindaas.core.api.IExecutionTasks;
import edu.emory.cci.bindaas.core.api.IManagementTasks;
import edu.emory.cci.bindaas.framework.model.Profile;
import edu.emory.cci.bindaas.framework.model.SubmitEndpoint;
import edu.emory.cci.bindaas.framework.model.Workspace;
import edu.emory.cci.bindaas.framework.util.GSONUtil;
import junit.framework.TestCase;

public class AIMESubmitHandlerTestCase extends TestCase {
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		IManagementTasks managementTask = getManagementTaskBean();
		managementTask.deleteWorkspace("aime_junit");

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

	
	public void test_createSubmitEndpoint()
	{
		
	}
	
	public void test_createSubmitEndpointWithWrongParams()
	{
		
	}
	
	public void test_executeSubmit_1()
	{
		IManagementTasks managementTask = getManagementTaskBean();
		IExecutionTasks executionTasks = getExecutionTaskBean();
	
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
			
			managementTask.createProfile(testProfile.getName(), testWorkspace.getName(), profileParams, testProfile.getCreatedBy());
		
		}catch (Exception e) {
			fail(e.getMessage());
			
		}
	
		Profile profile;
		try {
			profile = managementTask.getProfile("aime_junit", "GBM");
			SubmitEndpoint se = profile.getSubmitEndpoints().get("xml");
			
			File annotationDirectory = new File("testData/gbm.annotations");
			
			File[] listOfFiles = annotationDirectory.listFiles(new FileFilter() {
				int count = 0;
				
				@Override
				public boolean accept(File arg0) {
					boolean flag = arg0.isFile() && arg0.getName().endsWith("xml"); 
					if(flag && count < 10)
					{
						count++;
						return true;
					}
					else
						return false;
				}
			});
			
			for(File xmlFile : listOfFiles)
			{
				String content = IOUtils.readStringFromStream(new FileInputStream(xmlFile));
				executionTasks.executeSubmitEndpoint("junit", content,profile , se);
			}
			
		} catch (Exception e) {

				fail(e.getMessage());
		}
		
	}
	
	public void test_executeSubmit_2()
	{
		
	}
	
	public void test_executeSubmit_3()
	{
		
	}
}
