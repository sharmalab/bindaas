package edu.emory.cci.bindaas.datasource.provider.genericsql.outputformat;

import java.sql.ResultSet;

import edu.emory.cci.bindaas.datasource.provider.genericsql.model.OutputFormat;
import edu.emory.cci.bindaas.datasource.provider.genericsql.model.OutputFormatProps;
import edu.emory.cci.bindaas.framework.model.QueryResult;

public interface IFormatHandler {

	public QueryResult format(OutputFormatProps outputFormatProps,ResultSet queryResult) throws Exception;
	
	public OutputFormat getOutputFormat();
	public void validate(OutputFormatProps outputFormatProps) throws Exception;
}
