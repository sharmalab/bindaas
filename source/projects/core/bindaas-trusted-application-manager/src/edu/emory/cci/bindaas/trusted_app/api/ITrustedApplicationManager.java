package edu.emory.cci.bindaas.trusted_app.api;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

public interface ITrustedApplicationManager {
	
	@GET
	@Path("/issueShortLivedApiKey")
	public Response getAPIKey(@HeaderParam("_username") String username ,   @HeaderParam("_applicationID") String applicationID ,@HeaderParam("_salt") String salt , @HeaderParam("_digest") String digest  , @QueryParam("lifetime") Integer lifetime ) ;
	
	

}
