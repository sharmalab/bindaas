package edu.emory.cci.bindaas.core.rest.service.api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;


public interface IInformationService {

	@Path("providers")
	@GET
	public Response listProviders();
	
	@Path("providers/{providerId}")
	@GET
	public Response getProvider(@PathParam("providerId") String providerId);
	
	@Path("providers/{providerId}/{providerVersion}")
	@GET
	public Response getProvider(@PathParam("providerId") String providerId , @PathParam("providerVersion") String providerVersion);
	
	
	@Path("queryModifiers")
	@GET
	public Response listQueryModifiers();
	
	@Path("queryModifiers/{queryModifier}")
	@GET
	public Response getQueryModifier(@PathParam("queryModifier") String queryModifier);
	
	
	@Path("queryResultModifiers")
	@GET
	public Response listQueryResultModifiers();
	
	@Path("queryResultModifiers/{queryResultModifier}")
	@GET
	public Response getQueryResultModifier(@PathParam("queryResultModifier") String queryResultModifier);
	
	
	@Path("submitPayloadModifiers")
	@GET
	public Response listSubmitPayloadModifiers();
	
	@Path("submitPayloadModifiers/{submitPayloadModifier}")
	@GET
	public Response getSubmitPayloadModifier(@PathParam("submitPayloadModifier") String submitPayloadModifier);
	
	
	
	
}
