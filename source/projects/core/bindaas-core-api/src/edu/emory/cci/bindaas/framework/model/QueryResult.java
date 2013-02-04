package edu.emory.cci.bindaas.framework.model;

import java.io.OutputStream;
import java.util.Properties;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;


public class QueryResult {

	private boolean isError;
	private String errorMessage;
	private boolean isMime;
	private String mimeType; 
	private byte[] data;
	private boolean isCallback;
	private JsonElement intermediateResult;
	private Callback callback;
	
	public JsonElement getIntermediateResult() {
		return intermediateResult;
	}
	public void setIntermediateResult(JsonElement intermediateResult) {
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
	
	public boolean isMime() {
		return isMime;
	}
	public void setMime(boolean isMime) {
		this.isMime = isMime;
	}
	public String getMimeType() {
		return mimeType;
	}
	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}
	public byte[] getData() {
		return data;
	}
	public void setData(byte[] data) {
		this.data = data;
	}
	public boolean isCallback() {
		return isCallback;
	}
	public void setCallback(boolean isCallback) {
		this.isCallback = isCallback;
	}
	
	public static interface Callback
	{
		public void callback(OutputStream servletOutputStream , Properties context); 
	}
	
}
