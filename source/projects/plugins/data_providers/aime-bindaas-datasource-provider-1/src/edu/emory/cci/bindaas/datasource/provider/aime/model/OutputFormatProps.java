package edu.emory.cci.bindaas.datasource.provider.aime.model;

import com.google.gson.annotations.Expose;

public class OutputFormatProps {

	
	@Expose private QueryType queryType;
	@Expose private OutputFormat outputFormat;
	@Expose private String rootElement = "results";
	@Expose private String[] csvHeader;
	
	
	public static enum QueryType{
		SQL,XQUERY
	}
	
	public static enum OutputFormat 
	{
		HTML,CSV,JSON,XML,ANY
	}

	public QueryType getQueryType() {
		return queryType;
	}

	public void setQueryType(QueryType queryType) {
		this.queryType = queryType;
	}

	public OutputFormat getOutputFormat() {
		return outputFormat;
	}

	public void setOutputFormat(OutputFormat outputFormat) {
		this.outputFormat = outputFormat;
	}

	public String getRootElement() {
		return rootElement;
	}

	public void setRootElement(String rootElement) {
		this.rootElement = rootElement;
	}

	public String[] getCsvHeader() {
		return csvHeader;
	}

	public void setCsvHeader(String[] csvHeader) {
		this.csvHeader = csvHeader;
	}
	
	
}
