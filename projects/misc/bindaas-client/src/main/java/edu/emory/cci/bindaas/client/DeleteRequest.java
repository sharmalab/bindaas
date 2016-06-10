package edu.emory.cci.bindaas.client;

public class DeleteRequest implements IBindaasRequest{

	private String url;
	public DeleteRequest(String url)
	{
		this.url = url;
	}
	
	public void addParameter(String param , String value)
	{
		// TODO
	}
}
