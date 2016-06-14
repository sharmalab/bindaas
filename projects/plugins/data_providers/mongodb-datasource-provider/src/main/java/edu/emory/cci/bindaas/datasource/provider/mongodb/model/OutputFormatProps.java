package edu.emory.cci.bindaas.datasource.provider.mongodb.model;

import com.google.gson.annotations.Expose;

public class OutputFormatProps {

	
	
	@Expose private OutputFormat outputFormat;

	public OutputFormat getOutputFormat() {
		return outputFormat;
	}

	public void setOutputFormat(OutputFormat outputFormat) {
		this.outputFormat = outputFormat;
	}
	
	
}
