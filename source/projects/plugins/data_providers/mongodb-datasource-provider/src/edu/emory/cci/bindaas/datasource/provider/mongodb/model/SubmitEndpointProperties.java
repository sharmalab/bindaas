package edu.emory.cci.bindaas.datasource.provider.mongodb.model;

import com.google.gson.annotations.Expose;

public class SubmitEndpointProperties {

	@Expose private InputType inputType;
	@Expose private String[] csvHeader; // optional field for CSV data

	public String[] getCsvHeader() {
		return csvHeader;
	}

	public void setCsvHeader(String[] csvHeader) {
		this.csvHeader = csvHeader;
	}

	public InputType getInputType() {
		return inputType;
	}

	public void setInputType(InputType inputType) {
		this.inputType = inputType;
	}

	
	
	public static enum InputType {
		JSON,CSV,JSON_FILE,CSV_FILE
	}
}
