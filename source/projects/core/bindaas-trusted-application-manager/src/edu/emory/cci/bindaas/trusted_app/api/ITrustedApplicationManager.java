package edu.emory.cci.bindaas.trusted_app.api;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

public interface ITrustedApplicationManager {
	
	@GET
	@Path("/issueShortLivedApiKey")
	public Response getAPIKey(@HeaderParam("_username") String username ,   @HeaderParam("_applicationID") String applicationID ,@HeaderParam("_salt") String salt , @HeaderParam("_digest") String digest  , @QueryParam("lifetime") Integer lifetime ) ;
	
	@GET
	@Path("/authorizeUser")
	public Response authorizeNewUser(@HeaderParam("_username") String username ,   @HeaderParam("_applicationID") String applicationID ,@HeaderParam("_salt") String salt , @HeaderParam("_digest") String digest  , @QueryParam("expires") Long epochTime  ,  @QueryParam("comments") String comments);
	
	@DELETE
	@Path("/revokeUser")
	public Response revokeAccess(@HeaderParam("_username") String username ,   @HeaderParam("_applicationID") String applicationID ,@HeaderParam("_salt") String salt , @HeaderParam("_digest") String digest ,  @QueryParam("comments") String comments);
	
	@GET
	@Path("/listAPIkeys")
	public Response listAPIKeys(@HeaderParam("_username") String username ,   @HeaderParam("_applicationID") String applicationID ,@HeaderParam("_salt") String salt , @HeaderParam("_digest") String digest );
}
