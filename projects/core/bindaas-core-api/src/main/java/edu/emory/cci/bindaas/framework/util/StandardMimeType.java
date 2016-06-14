package edu.emory.cci.bindaas.framework.util;

public enum StandardMimeType {

	JSON("application/json"),XML("application/xml"),HTML("text/html"),CSV("application/csv"),TEXT("text/plain") , ZIP("application/zip");
	
	private String value;
	
	StandardMimeType(String value)
	{
		this.value = value;
	}
	
	public String toString()
	{
		return value;
	}
	
	
	
}
