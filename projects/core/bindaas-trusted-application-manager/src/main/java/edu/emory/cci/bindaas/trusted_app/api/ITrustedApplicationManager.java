package edu.emory.cci.bindaas.trusted_app.api;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

public interface ITrustedApplicationManager {
	
	@GET
	@Path("/issueShortLivedAuthenticationToken")
	public Response issueShortLivedAuthenticationToken(@HeaderParam("protocol") String protocol, @HeaderParam("_username") String username ,   @HeaderParam("_applicationID") String applicationID ,@HeaderParam("_salt") String salt , @HeaderParam("_digest") String digest  , @QueryParam("lifetime") Integer lifetime ) ;
	
	@GET
	@Path("/authorizeUser")
	public Response authorizeNewUser(@HeaderParam("protocol") String protocol, @HeaderParam("_username") String username ,   @HeaderParam("_applicationID") String applicationID ,@HeaderParam("_salt") String salt , @HeaderParam("_digest") String digest  , @QueryParam("expires") Long epochTime  ,  @QueryParam("comments") String comments);
	
	@DELETE
	@Path("/revokeUser")
	public Response revokeAccess(@HeaderParam("protocol") String protocol, @HeaderParam("_username") String username ,   @HeaderParam("_applicationID") String applicationID ,@HeaderParam("_salt") String salt , @HeaderParam("_digest") String digest ,  @QueryParam("comments") String comments);
	
	@GET
	@Path("/listAuthenticationTokens")
	public Response listAuthenticationTokens(@HeaderParam("protocol") String protocol, @HeaderParam("_username") String username ,   @HeaderParam("_applicationID") String applicationID ,@HeaderParam("_salt") String salt , @HeaderParam("_digest") String digest );
}
