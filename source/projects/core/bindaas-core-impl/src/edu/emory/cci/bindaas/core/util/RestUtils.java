package edu.emory.cci.bindaas.core.util;

import java.io.InputStream;

import javax.ws.rs.core.Response;

import edu.emory.cci.bindaas.framework.util.StandardMimeType;

public class RestUtils {

	public static Response createSuccessResponse(String message)
	{
		return Response.ok(message).type("text/plain").header("Access-Control-Allow-Origin", "*").build();
	}
	
	public static Response createJsonResponse(String message)
	{
		return Response.ok(message).type(StandardMimeType.JSON.toString()).header("Access-Control-Allow-Origin", "*").build();
	}
	
	
	
	public static Response createSuccessResponse(String message,String mimeType)
	{
		mimeType = mimeType == null? "text/plain" : mimeType;
		return Response.ok(message).type(mimeType).header("Access-Control-Allow-Origin", "*").build();
	}
	
	public static Response createMimeResponse(byte[] inputData , String mimeType)
	{
		return Response.ok().type(mimeType).entity(inputData).header("Access-Control-Allow-Origin", "*").build();
	}
	
	public static Response createMimeResponse(InputStream inputStream , String mimeType)
	{
		return Response.ok().type(mimeType).entity(inputStream).header("Access-Control-Allow-Origin", "*").build();
	}
	
	public static Response createErrorResponse(String message)
	{
		return Response.serverError().entity(message).header("Access-Control-Allow-Origin", "*").build();
	}
	
	
	
	public static Response createResponse(String message, int code)
	{
		
	 	return Response.status(code).entity(message).header("Access-Control-Allow-Origin", "*").build();
	}
}
