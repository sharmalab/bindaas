package edu.emory.cci.bindaas.commons.xml2json.model;

import java.util.Map;

import com.google.gson.annotations.Expose;

public class HttpProperties {

	@Expose private Map<String,String> httpHeaders;
	@Expose private Map<String,String> queryParameters;
	
	public Map<String, String> getHttpHeaders() {
		return httpHeaders;
	}
	public void setHttpHeaders(Map<String, String> httpHeaders) {
		this.httpHeaders = httpHeaders;
	}
	public Map<String, String> getQueryParameters() {
		return queryParameters;
	}
	public void setQueryParameters(Map<String, String> queryParameters) {
		this.queryParameters = queryParameters;
	}
	
}
