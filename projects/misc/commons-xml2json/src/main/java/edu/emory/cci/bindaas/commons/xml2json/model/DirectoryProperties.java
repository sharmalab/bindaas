package edu.emory.cci.bindaas.commons.xml2json.model;

import com.google.gson.annotations.Expose;

public class DirectoryProperties {
	@Expose private String patternContains;
	@Expose private boolean recursiveScan;
	
	public String getPatternContains() {
		return patternContains;
	}
	public void setPatternContains(String patternContains) {
		this.patternContains = patternContains;
	}
	public boolean isRecursiveScan() {
		return recursiveScan;
	}
	public void setRecursiveScan(boolean recursiveScan) {
		this.recursiveScan = recursiveScan;
	}
	
	
}
