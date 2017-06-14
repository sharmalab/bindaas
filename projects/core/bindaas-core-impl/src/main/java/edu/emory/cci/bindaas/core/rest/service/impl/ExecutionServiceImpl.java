package edu.emory.cci.bindaas.core.rest.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.MessageContextImpl;
import org.apache.cxf.phase.PhaseInterceptorChain;

import edu.emory.cci.bindaas.core.api.IExecutionTasks;
import edu.emory.cci.bindaas.core.api.IManagementTasks;
import edu.emory.cci.bindaas.core.rest.service.api.IExecutionService;
import edu.emory.cci.bindaas.core.util.RestUtils;
import edu.emory.cci.bindaas.framework.model.DeleteEndpoint;
import edu.emory.cci.bindaas.framework.model.Profile;
import edu.emory.cci.bindaas.framework.model.QueryEndpoint;
import edu.emory.cci.bindaas.framework.model.QueryResult;
import edu.emory.cci.bindaas.framework.model.QueryResult.Callback;
import edu.emory.cci.bindaas.framework.model.SubmitEndpoint;
import edu.emory.cci.bindaas.framework.provider.exception.AbstractHttpCodeException;

public class ExecutionServiceImpl implements IExecutionService{

	private Log log = LogFactory.getLog(getClass());
	private IExecutionTasks executionTask;
	private IManagementTasks managementTask;
	private RestUtils restUtils;
	
	public RestUtils getRestUtils() {
		return restUtils;
	}

	public void setRestUtils(RestUtils restUtils) {
		this.restUtils = restUtils;
	}

	public IExecutionTasks getExecutionTask() {
		return executionTask;
	}

	public void setExecutionTask(IExecutionTasks executionTask) {
		this.executionTask = executionTask;
	}

	public IManagementTasks getManagementTask() {
		return managementTask;
	}

	public void setManagementTask(IManagementTasks managementTask) {
		this.managementTask = managementTask;
	}


	public MessageContext getMessageContext()
	{
		MessageContext  messageContext = new MessageContextImpl(PhaseInterceptorChain.getCurrentMessage());
		return messageContext;
		
	}
	
	public void init() throws Exception
	{
		// do init here 
	}
	
	
	public Response queryResultToResponse(final QueryResult queryResult , QueryEndpoint queryEndpoint , Long responseTime) throws Exception
	{
		Map<String,Object> headers = new HashMap<String, Object>();
		
		if(queryEndpoint!= null)
		{
			headers.put("metadata", queryEndpoint.getMetaData());
			headers.put("tags", queryEndpoint.getTags());
		}

		if(responseTime != null)
		{
			headers.put("responseTime", responseTime.toString());
		}
		
		
		Map<String,Object> responseHeaders = queryResult.getResponseHeaders();
		if(responseHeaders!=null && responseHeaders.size() > 0)
		{
			headers.putAll(responseHeaders);
		}


		if(queryResult.getCallback()!=null)
		{
			
			final Callback callback = queryResult.getCallback();
			StreamingOutput streamingOutput = new StreamingOutput() {
				
				@Override
				public void write(OutputStream os) throws IOException,
						WebApplicationException {
					try {
							callback.callback(os, null);
					}
					catch(Exception e)
					{
						log.error(e);
						throw new WebApplicationException(e);
					}
					
				}
			};
			
			return restUtils.createMimeResponse(streamingOutput, queryResult.getMimeType() , headers);
		}
		else if(queryResult.isError())
		{
			return restUtils.createErrorResponse(queryResult.getErrorMessage());
		}
		else
		{			
			return restUtils.createMimeResponse(queryResult.getData() , queryResult.getMimeType() ,headers);
		}
		
	}
	
	private String getUser()
	{
		SecurityContext securityContext = getMessageContext().getSecurityContext();
		return securityContext.getUserPrincipal().getName();
	}
	
	private Map<String,String> getRuntimeQueryParameters()
	{
		Map<String,String> params = new HashMap<String, String>();
		MultivaluedMap<String,String> queryParams = getMessageContext().getUriInfo().getQueryParameters();
		for(String key : queryParams.keySet()){
			params.put(key, queryParams.getFirst(key));
		}
		return params;
	}
	
	
	@Override
	@Path("{workspace}/{profile}/submit/{submitEndpoint}")
	@POST
	@Consumes("multipart/form-data")
	public Response executeMimeTypeSubmitEndpoint(
			@PathParam("workspace") String workspaceName,
			@PathParam("profile") String profileName,
			@PathParam("submitEndpoint") String submitEndpointName , InputStream is) {
		try {
			Profile profile = managementTask.getProfile(workspaceName, profileName);
			if(profile.getSubmitEndpoints().containsKey(submitEndpointName) )
			{
				SubmitEndpoint submitEndpoint = profile.getSubmitEndpoints().get(submitEndpointName);
				QueryResult queryResult = executionTask.executeSubmitEndpoint(getUser(), is , profile, submitEndpoint);
				return queryResultToResponse(queryResult , null , null);
				
			}
			else
			{
				throw new Exception("SubmitEndpoint [" + submitEndpointName + "] not found");
			}
		} 
		
		catch(AbstractHttpCodeException e)
		{
			log.error(e);
			return restUtils.createErrorResponse(e);
		}
		catch (Exception e) {
			log.error(e);
			return restUtils.createErrorResponse(e.getMessage());
		}
	}

	

	@Override
	@Path("{workspace}/{profile}/query/{queryEndpoint}/metadata")
	@GET
	public Response getQueryMetadata(@PathParam("workspace") String workspaceName,
			@PathParam("profile") String profileName,
			@PathParam("queryEndpoint") String queryEndpointName) {
		try {
			Profile profile = managementTask.getProfile(workspaceName, profileName);
			if(profile.getQueryEndpoints().containsKey(queryEndpointName) )
			{
				QueryEndpoint queryEndpoint = profile.getQueryEndpoints().get(queryEndpointName);
				return restUtils.createJsonResponse(queryEndpoint.getMetaData().toString());
			}
			else
			{
				throw new Exception("QueryEndpoint [" + queryEndpointName + "] not found");
			}
		} 
		catch(AbstractHttpCodeException e)
		{
			log.error(e);
			return restUtils.createErrorResponse(e);
		}
		catch (Exception e) {
			log.error(e);
			return restUtils.createErrorResponse(e.getMessage());
		}
	}

	@Override
	@Path("{workspace}/{profile}/submit/{submitEndpoint}")
	@POST
	public Response executeFormDataTypeSubmitEndpoint(
			@PathParam("workspace") String workspaceName,
			@PathParam("profile") String profileName,
			@PathParam("submitEndpoint") String submitEndpointName, String requestBody) {
		try {
			Profile profile = managementTask.getProfile(workspaceName, profileName);
			if(profile.getSubmitEndpoints().containsKey(submitEndpointName) )
			{
				SubmitEndpoint submitEndpoint = profile.getSubmitEndpoints().get(submitEndpointName);
				QueryResult queryResult = executionTask.executeSubmitEndpoint(getUser(), requestBody , profile, submitEndpoint);	
				return queryResultToResponse(queryResult ,  null , null);	
			}
			else
			{
				throw new Exception("SubmitEndpoint [" + submitEndpointName + "] not found");
			}
		} 
		catch(AbstractHttpCodeException e)
		{
			log.error(e);
			return restUtils.createErrorResponse(e);
		}
		catch (Exception e) {
			log.error(e);
			return restUtils.createErrorResponse(e.getMessage());
		}
	}

	@Override
	@Path("{workspace}/{profile}/query/{queryEndpoint}")
	@GET
	public Response executeQueryEndpoint(
			@PathParam("workspace") String workspaceName,
			@PathParam("profile") String profileName,
			@PathParam("queryEndpoint") String queryEndpointName) {
		long startTime = System.currentTimeMillis();
		try {
			Profile profile = managementTask.getProfile(workspaceName, profileName);
			if(profile.getQueryEndpoints().containsKey(queryEndpointName) )
			{
				QueryEndpoint queryEndpoint = profile.getQueryEndpoints().get(queryEndpointName);
				
				QueryResult queryResult = executionTask.executeQueryEndpoint(getUser(), getRuntimeQueryParameters() , profile, queryEndpoint);
				
				return queryResultToResponse(queryResult , queryEndpoint , System.currentTimeMillis() - startTime);
			}
			else
			{
				throw new Exception("QueryEndpoint [" + queryEndpointName + "] not found");
			}
		} 
		catch(AbstractHttpCodeException e)
		{
			log.error(e);
			return restUtils.createErrorResponse(e);
		}
		catch (Exception e) {
			log.error(e);
			return restUtils.createErrorResponse(e.getMessage());
		}
		
	}

	@Override
	@Path("{workspace}/{profile}/delete/{deleteEndpoint}")
	@DELETE
	public Response executeDeleteEndpoint(
			@PathParam("workspace") String workspaceName,
			@PathParam("profile") String profileName,
			@PathParam("deleteEndpoint") String deleteEndpointName) {
		try {
			Profile profile = managementTask.getProfile(workspaceName, profileName);
			if(profile.getDeleteEndpoints().containsKey(deleteEndpointName))
			{
				DeleteEndpoint deleteEndpoint = profile.getDeleteEndpoints().get(deleteEndpointName);
				
				QueryResult queryResult = executionTask.executeDeleteEndpoint(getUser(), getRuntimeQueryParameters(), profile, deleteEndpoint);
				
				return queryResultToResponse(queryResult , null , null );
			}
			else
			{
				throw new Exception("DeleteEndpoint [" + deleteEndpointName + "] not found");
			}
		} 
		catch(AbstractHttpCodeException e)
		{
			log.error(e);
			return restUtils.createErrorResponse(e);
		}
		
		catch (Exception e) {
			log.error(e);
			return restUtils.createErrorResponse(e.getMessage());
		}
	}

	@Override
	@Path("{workspace}/{profile}/query/{queryEndpoint}")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@POST
	public Response executeQueryEndpointPost(
			@PathParam("workspace") String workspaceName,
			@PathParam("profile") String profileName,
			@PathParam("queryEndpoint") String queryEndpointName , MultivaluedMap<String, String> postParams) {
		long startTime = System.currentTimeMillis();
		try {
			Profile profile = managementTask.getProfile(workspaceName, profileName);
			if(profile.getQueryEndpoints().containsKey(queryEndpointName) )
			{
				QueryEndpoint queryEndpoint = profile.getQueryEndpoints().get(queryEndpointName);
				
				QueryResult queryResult = executionTask.executeQueryEndpoint(getUser(), getMapFromMultivaluedMap(postParams) , profile, queryEndpoint);
				
				return queryResultToResponse(queryResult , queryEndpoint , System.currentTimeMillis() - startTime);
			}
			else
			{
				throw new Exception("QueryEndpoint [" + queryEndpointName + "] not found");
			}
		} 
		catch(AbstractHttpCodeException e)
		{
			log.error(e);
			return restUtils.createErrorResponse(e);
		}
		catch (Exception e) {
			log.error(e);
			return restUtils.createErrorResponse(e.getMessage());
		}
	}

	
	private Map<String, String> getMapFromMultivaluedMap(MultivaluedMap<String, String> mm) {
		Map<String,String> params = new HashMap<String, String>();
		
		for(String key : mm.keySet()){
			params.put(key, mm.getFirst(key));
		}
		return params;
	}

	@Override
	@Path("{workspace}/{profile}/delete/{deleteEndpoint}")
	@OPTIONS
	public Response getAllowableOptionsForDeleteEndpoint(
			@PathParam("workspace") String workspace,
			@PathParam("profile") String profile,
			@PathParam("deleteEndpoint") String deleteEndpoint) {
		
		Map<String,Object> headers = new HashMap<String, Object>();
		headers.put("Allow", "DELETE,OPTIONS");
		headers.put("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,HEAD,OPTIONS");
		headers.put("Access-Control-Allow-Headers", "Authorization, X-Authorization");
		return restUtils.createJsonResponse("{}", headers);
		
	}

}
