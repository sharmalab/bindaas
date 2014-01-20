package edu.emory.cci.sample.modifiers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.JsonObject;

import edu.emory.cci.bindaas.framework.api.ISubmitPayloadModifier;
import edu.emory.cci.bindaas.framework.model.ModifierException;
import edu.emory.cci.bindaas.framework.model.RequestContext;
import edu.emory.cci.bindaas.framework.model.SubmitEndpoint;
import edu.emory.cci.bindaas.framework.provider.exception.AbstractHttpCodeException;
import edu.emory.cci.bindaas.framework.provider.exception.SubmitExecutionFailedException;
import edu.emory.cci.bindaas.framework.util.IOUtils;

public class LoggingQRM implements ISubmitPayloadModifier{

	private Log log = LogFactory.getLog(getClass());
	@Override
	public JsonObject getDocumentation() {

		return new JsonObject();
	}
	@Override
	public void validate() throws ModifierException {
		// not implemented	
	}

	@Override
	public String getDescriptiveName() {

		return "Plugin for logging submit payload";
	}
	
	@Override
	public InputStream transformPayload(InputStream data, SubmitEndpoint submitEndpoint, JsonObject modifierProperties, RequestContext requestContext) throws AbstractHttpCodeException {
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			IOUtils.copyAndCloseInput(data, bos);
			bos.close();
			byte[] rawBytes = bos.toByteArray();
			log.info("Payload of [" + rawBytes.length + "] intercepted");
			return new ByteArrayInputStream(rawBytes);
		} catch (IOException e) {
			log.error("Error in handling payload",e);
			throw new SubmitExecutionFailedException(getClass().getName(), 1);
		}
		
	}
	@Override
	public String transformPayload(String data, SubmitEndpoint submitEndpoint,
			JsonObject modifierProperties, RequestContext requestContext)
			throws AbstractHttpCodeException {
		log.info("Data received from HTTP POST [" + data + "]");
		return data;
	}

	
}
