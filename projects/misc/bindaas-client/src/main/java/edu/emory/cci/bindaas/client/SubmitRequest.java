package edu.emory.cci.bindaas.client;

import java.io.InputStream;

public class SubmitRequest implements IBindaasRequest {
	private String url;
	
	public SubmitRequest(String url)
	{
		this.url = url;
	}
	
	public void addBody(String data)
	{
		// TODO
	}
	
	public void addMimeBody(InputStream data)
	{
		// TODO
	}
	
	
}
