package edu.emory.cci.bindaas.datasource.provider.http.model;

import com.google.gson.annotations.Expose;

public class SubmitEndpointProperties {

	@Expose private String outputfileName;
	@Expose private String[] csvHeader; // optional field for CSV data
	@Expose private String url; //The backend url to send the POST query to.

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String[] getCsvHeader() {
		return csvHeader;
	}

	public void setCsvHeader(String[] csvHeader) {
		this.csvHeader = csvHeader;
	}

	public String getOutputfileName() {
		return outputfileName;
	}

	public void setOutputfileName(String outputfileName) {
		this.outputfileName = outputfileName;
	}
}
