package edu.emory.cci.bindaas.commons.xml2json.model;

import java.util.List;
import java.util.Map;

import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;

public class ImportDescriptor {

	@Expose private String rootDocumentSelector;
	@Expose private String url;
	@Expose private JsonObject urlProperties;
	public String getRootDocumentSelector() {
		return rootDocumentSelector;
	}
	public void setRootDocumentSelector(String rootDocumentSelector) {
		this.rootDocumentSelector = rootDocumentSelector;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public JsonObject getUrlProperties() {
		return urlProperties;
	}
	public void setUrlProperties(JsonObject urlProperties) {
		this.urlProperties = urlProperties;
	}
	public boolean isNamespaceAware() {
		return namespaceAware;
	}
	public void setNamespaceAware(boolean namespaceAware) {
		this.namespaceAware = namespaceAware;
	}
	public Map<String, String> getPrefixMapping() {
		return prefixMapping;
	}
	public void setPrefixMapping(Map<String, String> prefixMapping) {
		this.prefixMapping = prefixMapping;
	}
	@Expose private boolean namespaceAware;
	@Expose private Map<String,String> prefixMapping;
	@Expose private List<Mapping> mappings;
	
	public List<Mapping> getMappings() {
		return mappings;
	}
	public void setMappings(List<Mapping> mappings) {
		this.mappings = mappings;
	}
}
