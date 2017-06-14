package edu.emory.cci.bindaas.client;

public class QueryRequest implements IBindaasRequest{
	private String url;
	
	public QueryRequest(String url)
	{
		this.url = url;
	}
	
	public void addParameter(String param , String value)
	{
		// TODO
	}
}
