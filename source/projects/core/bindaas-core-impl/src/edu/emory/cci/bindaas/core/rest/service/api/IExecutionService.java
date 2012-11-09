package edu.emory.cci.bindaas.core.rest.service.api;

import java.io.InputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;



public interface IExecutionService {

	
	// execute a QueryEndpoint
	
		@Path("{workspace}/{profile}/query/{queryEndpoint}")
		@GET
		public Response executeQueryEndpoint(@PathParam("workspace") String workspace , @PathParam("profile") String profile , @PathParam("queryEndpoint") String queryEndpoint) ;
		
		

	// execute Delete Endpoint
		@Path("{workspace}/{profile}/delete/{deleteEndpoint}")
		@DELETE
		public Response executeDeleteEndpoint(@PathParam("workspace") String workspace , @PathParam("profile") String profile , @PathParam("deleteEndpoint") String deleteEndpoint) ;
		
		
		
	// execute Submit Endpoint
		@Path("{workspace}/{profile}/submit/{submitEndpoint}")
		@POST
		@Consumes("multipart/form-data")
		public Response executeMimeTypeSubmitEndpoint(@PathParam("workspace") String workspace , @PathParam("profile") String profile , @PathParam("submitEndpoint") String submitEndpoint , InputStream is) ;
		
		@Path("{workspace}/{profile}/submit/{submitEndpoint}")
		@POST
		public Response executeFormDataTypeSubmitEndpoint(@PathParam("workspace") String workspace , @PathParam("profile") String profile , @PathParam("submitEndpoint") String submitEndpoint, String requestBody) ;
		
		
	// get meta-data
		@Path("{workspace}/{profile}/query/{stage}/{queryEndpoint}/metadata")
		@GET
		public Response getQueryMetadata(@PathParam("workspace") String workspace , @PathParam("profile") String profile , @PathParam("queryEndpoint") String queryEndpoint);
		
}
