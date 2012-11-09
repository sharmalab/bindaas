package edu.emory.cci.bindaas.installer;

import java.util.List;

import com.google.gson.annotations.Expose;

public class Repository {

	@Expose private String name;
	@Expose private String baseUrl;
	@Expose private List<String> bundles;
	
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getBaseUrl() {
		return baseUrl;
	}
	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}
	public List<String> getBundles() {
		return bundles;
	}
	public void setBundles(List<String> bundles) {
		this.bundles = bundles;
	}
}
