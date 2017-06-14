package edu.emory.cci.bindaas.datasource.provider.http.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import com.google.gson.annotations.Expose;

import edu.emory.cci.bindaas.framework.util.GSONUtil;

public class HTTPQuery {

	@Expose private METHOD method;
	@Expose private String url;
	@Expose private Map<String,String> queryParameters;
	@Expose private Map<String,String> headers;
	
	private Log log = LogFactory.getLog(getClass());
	
	public METHOD getMethod() {
		return method;
	}
	public void setMethod(METHOD method) {
		this.method = method;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public Map<String, String> getQueryParameters() {
		return queryParameters;
	}
	public void setQueryParameters(Map<String, String> queryParameters) {
		this.queryParameters = queryParameters;
	}
	public Map<String, String> getHeaders() {
		return headers;
	}
	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}
	
	
	public String toString()
	{
		return GSONUtil.getGSONInstance().toJson(this);
	}
	
	public void validate() throws Exception
	{
		log.debug("Validating HttpQuery [" + toString() + "]");
		
		if(method == null)
			throw new Exception("Validation of HTTPQuery Failed. Invalid method specified");
		
	}
	
	public HttpResponse execute() throws Exception
	{
		DefaultHttpClient defaultHttpClient = new DefaultHttpClient();
		
		HttpRequestBase request = null;
		
		switch(method)
		{
			case GET :
			case DELETE :
				{
				URIBuilder uriBuilder = new URIBuilder(url);
				
				
				if(queryParameters!=null)
				{
					for(String qParam : queryParameters.keySet())
					{
						uriBuilder.addParameter(qParam, queryParameters.get(qParam));
					}
				}
				
				request = new HttpGet(uriBuilder.build());
				}
				
				break;
			case POST :
			{
				URIBuilder uriBuilder = new URIBuilder(url);
				HttpPost postRequest = new HttpPost(uriBuilder.build());
				
				if(queryParameters!=null)
				{
					List<NameValuePair> listOfNameValuePairs = new ArrayList<NameValuePair>();
					for(String qParam : queryParameters.keySet())
					{
						NameValuePair nvp = new BasicNameValuePair(qParam, queryParameters.get(qParam));
						listOfNameValuePairs.add(nvp);
					} 
					postRequest.setEntity(new UrlEncodedFormEntity(listOfNameValuePairs));
				}
				request = postRequest;
			}
			break;
			case PUT  :
			{
				URIBuilder uriBuilder = new URIBuilder(url);
				HttpPut putRequest = new HttpPut(uriBuilder.build());
				
				if(queryParameters!=null)
				{
					List<NameValuePair> listOfNameValuePairs = new ArrayList<NameValuePair>();
					for(String qParam : queryParameters.keySet())
					{
						NameValuePair nvp = new BasicNameValuePair(qParam, queryParameters.get(qParam));
						listOfNameValuePairs.add(nvp);
					} 
					putRequest.setEntity(new UrlEncodedFormEntity(listOfNameValuePairs));
				}
				request = putRequest;
			}
			break;
			default :
				throw new  Exception("Method [" + method + "] not supported");
				
		}
		
		// Add headers
		if(headers!=null)
		{
			for(String headerKey : headers.keySet())
			{
				request.addHeader(headerKey, headers.get(headerKey));
			}
		}
		
		log.debug("Connecting remote site [" + request.getURI().toString() + "]");
		HttpResponse response = defaultHttpClient.execute(request);
		return response;
			
	}
}

enum METHOD {
	GET,POST,DELETE,PUT
}
