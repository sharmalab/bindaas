package edu.emory.cci.bindaas.framework.api;

import java.io.InputStream;

import com.google.gson.JsonObject;

import edu.emory.cci.bindaas.framework.model.RequestContext;
import edu.emory.cci.bindaas.framework.model.SubmitEndpoint;

public interface ISubmitPayloadModifier extends IModifier {

	public  InputStream transformPayload( InputStream data , SubmitEndpoint submitEndpoint , JsonObject modifierProperties, RequestContext requestContext) throws Exception;
	public  String transformPayload( String data , SubmitEndpoint submitEndpoint , JsonObject modifierProperties , RequestContext requestContext) throws Exception;
	
	
	
}
