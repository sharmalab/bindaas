package edu.emory.cci.bindaas.pseudosts.api;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

public interface IPsuedoSecurityTokenService {
	
	@GET
	@Path("/")
	public Response getAPIKey(@HeaderParam("Authorization") String authorizationHeader , @QueryParam("clientId") String clientId , @QueryParam("lifetime") Integer lifetime ) ;
		
}
