package edu.emory.cci.bindaas.core.rest.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.MessageContextImpl;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.utils.multipart.AttachmentUtils;
import org.apache.cxf.phase.PhaseInterceptorChain;

import edu.emory.cci.bindaas.core.api.IExecutionTasks;
import edu.emory.cci.bindaas.core.api.IManagementTasks;
import edu.emory.cci.bindaas.core.bundle.Activator;
import edu.emory.cci.bindaas.core.rest.service.api.IExecutionService;
import edu.emory.cci.bindaas.core.util.RestUtils;
import edu.emory.cci.bindaas.framework.model.DeleteEndpoint;
import edu.emory.cci.bindaas.framework.model.Profile;
import edu.emory.cci.bindaas.framework.model.QueryEndpoint;
import edu.emory.cci.bindaas.framework.model.QueryResult;
import edu.emory.cci.bindaas.framework.model.Stage;
import edu.emory.cci.bindaas.framework.model.SubmitEndpoint;
import edu.emory.cci.bindaas.framework.model.SubmitEndpoint.Type;
import edu.emory.cci.bindaas.framework.util.StandardMimeType;

public class ExecutionServiceImpl implements IExecutionService{

	private Log log = LogFactory.getLog(getClass());
	private IExecutionTasks executionTask;
	private IManagementTasks managementTask;
	
	
	public ExecutionServiceImpl()
	{
		Dictionary<String, String> props = new Hashtable<String, String>();
		props.put("class", getClass().getName());
		Activator.getContext().registerService(IExecutionService.class.getName(), this, props);
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
	
	public Response queryResultToResponse(QueryResult queryResult) throws Exception
	{
		if(queryResult.isCallback())
		{
			queryResult.getCallback().callback(getMessageContext().getHttpServletResponse().getOutputStream(), null); // TODO here instead of null a context should be passed
			return Response.ok().build();
		}
		else if(queryResult.isError())
		{
			return RestUtils.createErrorResponse(queryResult.getErrorMessage());
		}
		else if(queryResult.isMime())
		{
			return RestUtils.createMimeResponse(queryResult.getData(), queryResult.getMimeType());
		}
		else
		{
			return RestUtils.createSuccessResponse(new String(queryResult.getData()) , queryResult.getMimeType());
		}
		
	}
	public Response queryResultToResponse(QueryResult queryResult , QueryEndpoint queryEndpoint , long responseTime) throws Exception
	{
		if(queryResult.isCallback())
		{
			HttpServletResponse response = getMessageContext().getHttpServletResponse(); 
			response.setContentType(queryResult.getMimeType());
			response.setHeader("metadata", queryEndpoint.getMetaData().toString());
			response.setHeader("tags", queryEndpoint.getTags().toString());
			response.setHeader("responseTime(ms)", responseTime+ "");
			if(queryResult.getMimeType().equals(StandardMimeType.ZIP.toString()))
			{
				response.setHeader("Content-Disposition","attachment;filename=\"" + queryEndpoint.getName() + ".zip\"");
			}
			
			queryResult.getCallback().callback(getMessageContext().getHttpServletResponse().getOutputStream(), null); // TODO here instead of null a context should be passed
			return Response.ok().build();
		}
		else if(queryResult.isError())
		{
			return RestUtils.createErrorResponse(queryResult.getErrorMessage());
		}
		else if(queryResult.isMime())
		{
			Map<String,Object> headers = new HashMap<String, Object>();
			headers.put("metadata", queryEndpoint.getMetaData());
			headers.put("tags", queryEndpoint.getTags());
			headers.put("responseTime(ms)", responseTime+ "");
			return RestUtils.createMimeResponse(queryResult.getData(), queryResult.getMimeType() , headers);
		}
		else
		{
			Map<String,Object> headers = new HashMap<String, Object>();
			headers.put("metadata", queryEndpoint.getMetaData());
			headers.put("tags", queryEndpoint.getTags());
			headers.put("responseTime(ms)",responseTime+ "");
			return RestUtils.createSuccessResponse(new String(queryResult.getData()) , queryResult.getMimeType() ,headers);
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
	
	private InputStream getMimeRequestAsStream() throws Exception
	{
		
		List<Attachment> attachments = AttachmentUtils.getAttachments(getMessageContext());
		if(attachments.size() > 0)
		{
			return attachments.get(0).getDataHandler().getInputStream();
		}
		else
		{
			throw new IOException("No data found in the submit request");
		}
			
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
				
				if(submitEndpoint.getType().equals(Type.MULTIPART))
				{
					QueryResult queryResult = executionTask.executeSubmitEndpoint(getUser(), is , profile, submitEndpoint);
					return queryResultToResponse(queryResult);
				}
				else
				{
					throw new Exception("SubmitEndpoint [" + submitEndpointName + "] does not support Multipart/Mime data");
				}
				
			}
			else
			{
				throw new Exception("SubmitEndpoint [" + submitEndpointName + "] not found");
			}
		} catch (Exception e) {
			log.error(e);
			return RestUtils.createErrorResponse(e.getMessage());
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
				return RestUtils.createJsonResponse(queryEndpoint.getMetaData().toString());
			}
			else
			{
				throw new Exception("QueryEndpoint [" + queryEndpointName + "] not found");
			}
		} catch (Exception e) {
			log.error(e);
			return RestUtils.createErrorResponse(e.getMessage());
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
				if(submitEndpoint.getType().equals(Type.FORM_DATA))
				{
					QueryResult queryResult = executionTask.executeSubmitEndpoint(getUser(), requestBody , profile, submitEndpoint);
					
					return queryResultToResponse(queryResult);	
				}
				else
				{
					throw new Exception("SubmitEndpoint [" + submitEndpointName + "] does not support simple data");
				}
				
			}
			else
			{
				throw new Exception("SubmitEndpoint [" + submitEndpointName + "] not found");
			}
		} catch (Exception e) {
			log.error(e);
			return RestUtils.createErrorResponse(e.getMessage());
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
		} catch (Exception e) {
			log.error(e);
			return RestUtils.createErrorResponse(e.getMessage());
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
				
				return queryResultToResponse(queryResult);
			}
			else
			{
				throw new Exception("DeleteEndpoint [" + deleteEndpointName + "] not found");
			}
		} catch (Exception e) {
			log.error(e);
			return RestUtils.createErrorResponse(e.getMessage());
		}
	}

}
