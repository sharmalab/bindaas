package edu.emory.cci.bindaas.core.util;

import java.io.InputStream;
import java.util.Map;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.StreamingOutput;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import edu.emory.cci.bindaas.core.bundle.Activator;
import edu.emory.cci.bindaas.framework.provider.exception.AbstractHttpCodeException;
import edu.emory.cci.bindaas.framework.util.StandardMimeType;
import edu.emory.cci.bindaas.version_manager.api.IVersionManager;

public class RestUtils {

	private IVersionManager versionManager;
	
	
	public IVersionManager getVersionManager() {
		return versionManager;
	}
	public void setVersionManager(IVersionManager versionManager) {
		this.versionManager = versionManager;
	}
	
	
	public  Response createErrorResponse(AbstractHttpCodeException abstractHttpCodeException)
	{
		ResponseBuilder builder =  Response.status(abstractHttpCodeException.getHttpStatusCode()).type(StandardMimeType.JSON.toString()) . entity(abstractHttpCodeException.toString());
		
		return createResponse(builder);
	}
	public  Response createSuccessResponse(String message)
	{
		return createResponse(Response.ok(message).type("text/plain"));
	}
	
	public  Response createJsonResponse(String message)
	{
		return createResponse(Response.ok(message).type(StandardMimeType.JSON.toString()));
	}
	
	
	public  Response createSuccessResponse(String message,String mimeType)
	{
		mimeType = mimeType == null? "text/plain" : mimeType;
		return createResponse(Response.ok(message).type(mimeType));
	}
	
	public  Response createMimeResponse(byte[] inputData , String mimeType)
	{
		return createResponse(Response.ok().type(mimeType).entity(inputData));
	}
	
	public  Response createMimeResponse(InputStream inputStream , String mimeType)
	{
		return createResponse(Response.ok().type(mimeType).entity(inputStream));
	}
	
	public  Response createErrorResponse(String message)
	{
		
		JsonObject jsonResp = new JsonObject();
		jsonResp.add("errorMessage", new JsonPrimitive(message));
		
		ResponseBuilder builder =  Response.status(500).type(StandardMimeType.JSON.toString()) . entity(jsonResp.toString());
		return createResponse(builder);
	}
	
	public  Response createResponse(String message, int code)
	{
	 	return createResponse(Response.status(code).type(StandardMimeType.JSON.toString()).entity(String.format("{ 'message' : '%s'}", message)));
	}
	
	public  Response createSuccessResponse(String message , Map<String,Object> headers)
	{
		ResponseBuilder builder = Response.ok(message).type("text/plain");
		for(String key : headers.keySet())
		{
			builder = builder.header(key, headers.get(key));
		}
		return createResponse(builder);
	}
	
	public  Response createJsonResponse(String message, Map<String,Object> headers)
	{
		ResponseBuilder builder = Response.ok(message).type(StandardMimeType.JSON.toString());
		for(String key : headers.keySet())
		{
			builder = builder.header(key, headers.get(key));
		}
		return createResponse(builder);
	}
	
	
	public  Response createSuccessResponse(String message,String mimeType, Map<String,Object> headers)
	{
		
		mimeType = mimeType == null? "text/plain" : mimeType;
		
		ResponseBuilder builder = Response.ok(message).type(mimeType);
		for(String key : headers.keySet())
		{
			builder = builder.header(key, headers.get(key));
		}
		return createResponse(builder);
	}
	
	public  Response createMimeResponse(StreamingOutput streamingOutput , String mimeType, Map<String,Object> headers)
	{
		ResponseBuilder builder = Response.ok(streamingOutput).type(mimeType);
		for(String key : headers.keySet())
		{
			builder = builder.header(key, headers.get(key));
		}
		return createResponse(builder);
	}
	
	public  Response createMimeResponse(byte[] inputData , String mimeType, Map<String,Object> headers)
	{
		ResponseBuilder builder = Response.ok().type(mimeType).entity(inputData);
		for(String key : headers.keySet())
		{
			builder = builder.header(key, headers.get(key));
		}
		return createResponse(builder);
	}
	
	public  Response createMimeResponse(InputStream inputStream , String mimeType, Map<String,Object> headers)
	{
		ResponseBuilder builder = Response.ok().type(mimeType).entity(inputStream);
		for(String key : headers.keySet())
		{
			builder = builder.header(key, headers.get(key));
		}
		return createResponse(builder);
	}
	
	public  Response createErrorResponse(String message, Map<String,Object> headers)
	{
		JsonObject jsonResp = new JsonObject();
		jsonResp.add("errorMessage", new JsonPrimitive(message));
		ResponseBuilder builder = Response.serverError().entity(jsonResp.toString()).type(StandardMimeType.JSON.toString());
		for(String key : headers.keySet())
		{
			builder = builder.header(key, headers.get(key));
		}
		return createResponse(builder);
	}
	
	public  Response createResponse(String message, int code, Map<String,Object> headers)
	{
		ResponseBuilder builder = Response.status(code).entity(message);
		for(String key : headers.keySet())
		{
			builder = builder.header(key, headers.get(key));
		}
		 	return createResponse(builder);
	}
	
	public  Response createResponse(int code, StandardMimeType mime,  Map<String,Object> headers)
	{
		ResponseBuilder builder = Response.status(code).type(mime.toString());
		for(String key : headers.keySet())
		{
			builder = builder.header(key, headers.get(key));
		}
		 	return createResponse(builder);
	}
	
	
	public  Response createResponse(ResponseBuilder builder)
	{
		builder = builder.header("Access-Control-Allow-Origin", "*");
		builder = builder.header("Bindaas-version", versionManager.getSystemBuild() );
		builder = builder.header("Vendor", Activator.getContext().getBundle().getHeaders().get("Bundle-Vendor"));
		return builder.build();
	}
	
	
}
