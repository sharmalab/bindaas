package edu.emory.cci.bindaas.client;

public class BindaasClient {

	private String apiKey;
	
	
	public BindaasClient(String apiKey)
	{
		this.apiKey = apiKey;
	}
	
	public BindaasResponse execute(IBindaasRequest request) throws Exception
	{
		return null;
	}
	
}
