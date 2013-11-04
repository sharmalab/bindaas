package edu.emory.cci.bindaas.aime.dataprovider.model;

import com.google.gson.annotations.Expose;

public class SubmitEndpointProperties {

	@Expose private String tableName;
	@Expose private InputType inputType;

	public InputType getInputType() {
		return inputType;
	}

	public void setInputType(InputType inputType) {
		this.inputType = inputType;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	
	
	public static enum InputType {
		XML , ZIP
	}
}
