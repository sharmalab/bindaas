package edu.emory.cci.bindaas.junit.core;




import java.security.Principal;

import javax.ws.rs.core.Response;

import junit.framework.TestCase;

import org.apache.cxf.phase.PhaseInterceptorChain;
import org.apache.cxf.security.SecurityContext;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import edu.emory.cci.bindaas.core.rest.service.api.IManagementService;

public class ManagementServiceTest extends TestCase {

	/**
	 * Create operations
	 */
	protected void setUp() throws Exception {
		super.setUp();
		SecurityContext context = new SecurityContext() {
			
			@Override
			public boolean isUserInRole(String arg0) {
				return false;
			}
			
			@Override
			public Principal getUserPrincipal() {
				
				return new Principal() {
					
					@Override
					public String getName() {

						return "junit";
					}
				};
			}
		};
		
		PhaseInterceptorChain.getCurrentMessage().put(SecurityContext.class, context);

	};
	
	private IManagementService getManagementServiceBean()
	{
		BundleContext context = Activator.getContext();
		ServiceReference sf = context.getServiceReference(IManagementService.class.getName());
		if(sf!=null)
		{
			Object service = context.getService(sf);
			if(service!=null)
			return (IManagementService) service;
			else
				fail("Management Service not available for testing");
		}
		else
			fail("Management Service not available for testing");
		
		return null;
	}
	
	public void test_createWorkspace()
	{
		IManagementService managementService = getManagementServiceBean();
		String workspaceName = "testWorkspace";
		Response actualResponse = managementService.createWorkspace(workspaceName);
				
	}
	
//	public Response createWorkspace(@PathParam("workspace") String workspace);
	
	
//	public Response createProfile(@PathParam("workspace") String workspace , @PathParam("profile") String profile) ;
	
	
//	public Response createQueryEndpoint(@PathParam("workspace") String workspace , @PathParam("profile") String profile , @PathParam("queryEndpoint") String queryEndpoint) ;
	
//	public Response createDeleteEndpoint(@PathParam("workspace") String workspace , @PathParam("profile") String profile , @PathParam("deleteEndpoint") String deleteEndpoint) ;
	
	
//	public Response createSubmitEndpoint(@PathParam("workspace") String workspace , @PathParam("profile") String profile , @PathParam("submitEndpoint") String submitEndpoint) ;
	
	
	/**
	 *  Delete operations
	 */
	
//	public Response deleteWorkspace(@PathParam("workspace") String workspace) ;
	
//	public Response deleteProfile(@PathParam("workspace") String workspace , @PathParam("profile") String profile) ;
	
//	public Response deleteQueryEndpoint(@PathParam("workspace") String workspace , @PathParam("profile") String profile , @PathParam("queryEndpoint") String queryEndpoint) ;
	
//	public Response deleteDeleteEndpoint(@PathParam("workspace") String workspace , @PathParam("profile") String profile , @PathParam("deleteEndpoint") String deleteEndpoint) ;
	
//	public Response deleteSubmitEndpoint(@PathParam("workspace") String workspace , @PathParam("profile") String profile , @PathParam("submitEndpoint") String submitEndpoint) ;
	
	
	/**
	 * Update operations
	 */
	
//	public Response updateProfile(@PathParam("workspace") String workspace , @PathParam("profile") String profile) ;
	
//	public Response updateQueryEndpoint(@PathParam("workspace") String workspace , @PathParam("profile") String profile , @PathParam("queryEndpoint") String queryEndpoint) ;
	
//	public Response updateDeleteEndpoint(@PathParam("workspace") String workspace , @PathParam("profile") String profile , @PathParam("deleteEndpoint") String deleteEndpoint) ;
	
//	public Response updateSubmitEndpoint(@PathParam("workspace") String workspace , @PathParam("profile") String profile , @PathParam("submitEndpoint") String submitEndpoint) ;
	
	/**
	 *  Other operations
	 */
	
//	public Response listWorkspaces() ;
	
//	public Response getWorkspace(@PathParam("workspace") String workspace) ;
	
//	public Response getProfile(@PathParam("workspace") String workspace , @PathParam("profile") String profile) ;
	
//	public Response getQueryEndpoint(@PathParam("workspace") String workspace , @PathParam("profile") String profile , @PathParam("queryEndpoint") String queryEndpoint) ;
	

//	public Response getDeleteEndpoint(@PathParam("workspace") String workspace , @PathParam("profile") String profile , @PathParam("deleteEndpoint") String deleteEndpoint) ;
	

//	public Response getSubmitEndpoint(@PathParam("workspace") String workspace , @PathParam("profile") String profile , @PathParam("submitEndpoint") String submitEndpoint) ;
	
	/**
	 * publish operations
	 * 
	 */
	

//	public Response publishQueryEndpoint(@PathParam("workspace") String workspace , @PathParam("profile") String profile , @PathParam("queryEndpoint") String queryEndpoint) throws Exception;
	
	
	
//	public Response publishDeleteEndpoint(@PathParam("workspace") String workspace , @PathParam("profile") String profile , @PathParam("deleteEndpoint") String deleteEndpoint) throws Exception;

}
