package edu.emory.cci.bindaas.framework.model;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Properties;

import edu.emory.cci.bindaas.framework.provider.exception.AbstractHttpCodeException;


public class QueryResult {

	private boolean isError;
	private String errorMessage;
	private String mimeType; 
	private InputStream data;
	private ResultSetIterator intermediateResult;
	private Callback callback;
	private Map<String,Object> responseHeaders;
	
	public Map<String, Object> getResponseHeaders() {
		return responseHeaders;
	}
	public void setResponseHeaders(Map<String, Object> responseHeaders) {
		this.responseHeaders = responseHeaders;
	}
	
	
	public ResultSetIterator getIntermediateResult() {
		return intermediateResult;
	}
	public void setIntermediateResult(ResultSetIterator intermediateResult) {
		this.intermediateResult = intermediateResult;
	}
	public Callback getCallback() {
		return callback;
	}
	public void setCallback(Callback callback) {
		this.callback = callback;
	}
	public boolean isError() {
		return isError;
	}
	public void setError(boolean isError) {
		this.isError = isError;
	}
	public String getErrorMessage() {
		return errorMessage;
	}
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	
	public String getMimeType() {
		return mimeType;
	}
	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}
	
	public InputStream getData() {
		return data;
	}
	public void setData(InputStream data) {
		this.data = data;
	}

	public static interface Callback
	{
		public void callback(OutputStream servletOutputStream , Properties context) throws AbstractHttpCodeException; 
	}
	
}
