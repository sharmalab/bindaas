package edu.emory.cci.bindaas.commons.xml2json.model;

import java.util.List;

import com.google.gson.annotations.Expose;


public class Mapping {

	@Expose private String name;
	@Expose private Type type;
	@Expose private String xpath;
	@Expose private String xpathArray;
	@Expose private List<Mapping> nestedFields;
	
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Type getType() {
		return type;
	}
	public void setType(Type type) {
		this.type = type;
	}
	public String getXpath() {
		return xpath;
	}
	public void setXpath(String xpath) {
		this.xpath = xpath;
	}
	public String getXpathArray() {
		return xpathArray;
	}
	public void setXpathArray(String xpathArray) {
		this.xpathArray = xpathArray;
	}
	public List<Mapping> getNestedFields() {
		return nestedFields;
	}
	public void setNestedFields(List<Mapping> nestedFields) {
		this.nestedFields = nestedFields;
	}
	

}
