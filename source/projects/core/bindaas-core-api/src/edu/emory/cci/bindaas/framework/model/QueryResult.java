package edu.emory.cci.bindaas.framework.model;

import java.util.Properties;


public class QueryResult {

	private boolean isError;
	private String errorMessage;
	private boolean isMime;
	private String mimeType; 
	private byte[] data;
	private boolean isCallback;
	public void callback(Object httpResponse , Properties context){}
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
	
	
}
