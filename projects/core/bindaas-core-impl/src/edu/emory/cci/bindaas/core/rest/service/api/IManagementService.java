package edu.emory.cci.bindaas.core.rest.service.api;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

public interface IManagementService {

	/**
	 * Create operations
	 */
	
	@Path("{workspace}")
	@POST
	public Response createWorkspace(@PathParam("workspace") String workspace);
	
	@Path("{workspace}/{profile}")
	@POST
	public Response createProfile(@PathParam("workspace") String workspace , @PathParam("profile") String profile , @QueryParam("description") String description) ;
	
	@Path("{workspace}/{profile}/query/{queryEndpoint}")
	@POST
	public Response createQueryEndpoint(@PathParam("workspace") String workspace , @PathParam("profile") String profile , @PathParam("queryEndpoint") String queryEndpoint) ;
	
	@Path("{workspace}/{profile}/delete/{deleteEndpoint}")
	@POST
	public Response createDeleteEndpoint(@PathParam("workspace") String workspace , @PathParam("profile") String profile , @PathParam("deleteEndpoint") String deleteEndpoint) ;
	
	
	@Path("{workspace}/{profile}/submit/{submitEndpoint}")
	@POST
	public Response createSubmitEndpoint(@PathParam("workspace") String workspace , @PathParam("profile") String profile , @PathParam("submitEndpoint") String submitEndpoint) ;
	
	
	/**
	 *  Delete operations
	 */
	
	@Path("{workspace}")
	@DELETE
	public Response deleteWorkspace(@PathParam("workspace") String workspace) ;
	
	@Path("{workspace}/{profile}")
	@DELETE
	public Response deleteProfile(@PathParam("workspace") String workspace , @PathParam("profile") String profile) ;
	
	@Path("{workspace}/{profile}/query/{queryEndpoint}")
	@DELETE
	public Response deleteQueryEndpoint(@PathParam("workspace") String workspace , @PathParam("profile") String profile , @PathParam("queryEndpoint") String queryEndpoint) ;
	
	@Path("{workspace}/{profile}/delete/{deleteEndpoint}")
	@DELETE
	public Response deleteDeleteEndpoint(@PathParam("workspace") String workspace , @PathParam("profile") String profile , @PathParam("deleteEndpoint") String deleteEndpoint) ;
	
	@Path("{workspace}/{profile}/submit/{submitEndpoint}")
	@DELETE
	public Response deleteSubmitEndpoint(@PathParam("workspace") String workspace , @PathParam("profile") String profile , @PathParam("submitEndpoint") String submitEndpoint) ;
	
	
	/**
	 * Update operations
	 */
	
	@Path("{workspace}/{profile}")
	@PUT
	public Response updateProfile(@PathParam("workspace") String workspace , @PathParam("profile") String profile , @QueryParam("description") String description) ;
	
	@Path("{workspace}/{profile}/query/{queryEndpoint}")
	@PUT
	public Response updateQueryEndpoint(@PathParam("workspace") String workspace , @PathParam("profile") String profile , @PathParam("queryEndpoint") String queryEndpoint) ;
	
	@Path("{workspace}/{profile}/delete/{deleteEndpoint}")
	@PUT
	public Response updateDeleteEndpoint(@PathParam("workspace") String workspace , @PathParam("profile") String profile , @PathParam("deleteEndpoint") String deleteEndpoint) ;
	
	@Path("{workspace}/{profile}/submit/{submitEndpoint}")
	@PUT
	public Response updateSubmitEndpoint(@PathParam("workspace") String workspace , @PathParam("profile") String profile , @PathParam("submitEndpoint") String submitEndpoint) ;
	
	/**
	 *  Other operations
	 */
	
	@Path("workspaces")
	@GET 
	public Response listWorkspaces() ;
	
	@Path("{workspace}")
	@GET
	public Response getWorkspace(@PathParam("workspace") String workspace) ;
	
	@Path("{workspace}/{profile}")
	@GET
	public Response getProfile(@PathParam("workspace") String workspace , @PathParam("profile") String profile) ;
	
	@Path("{workspace}/{profile}/query/{queryEndpoint}")
	@GET
	public Response getQueryEndpoint(@PathParam("workspace") String workspace , @PathParam("profile") String profile , @PathParam("queryEndpoint") String queryEndpoint) ;
	
	@Path("{workspace}/{profile}/delete/{deleteEndpoint}")
	@GET
	public Response getDeleteEndpoint(@PathParam("workspace") String workspace , @PathParam("profile") String profile , @PathParam("deleteEndpoint") String deleteEndpoint) ;
	
	@Path("{workspace}/{profile}/submit/{submitEndpoint}")
	@GET
	public Response getSubmitEndpoint(@PathParam("workspace") String workspace , @PathParam("profile") String profile , @PathParam("submitEndpoint") String submitEndpoint) ;
	
	/**
	 * publish operations
	 * 
	 */
	
	@Path("{workspace}/{profile}/query/{queryEndpoint}/publish")
	@POST
	public Response publishQueryEndpoint(@PathParam("workspace") String workspace , @PathParam("profile") String profile , @PathParam("queryEndpoint") String queryEndpoint) throws Exception;
	
	
	@Path("{workspace}/{profile}/delete/{deleteEndpoint}/publish")
	@POST
	public Response publishDeleteEndpoint(@PathParam("workspace") String workspace , @PathParam("profile") String profile , @PathParam("deleteEndpoint") String deleteEndpoint) throws Exception;
}
