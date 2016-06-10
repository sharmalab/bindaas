package edu.emory.cci.bindaas.datasource.provider.genericsql.outputformat;

import java.sql.ResultSet;

import edu.emory.cci.bindaas.datasource.provider.genericsql.model.OutputFormat;
import edu.emory.cci.bindaas.datasource.provider.genericsql.model.OutputFormatProps;
import edu.emory.cci.bindaas.framework.model.QueryResult;

public interface IFormatHandler {

	public QueryResult format(OutputFormatProps outputFormatProps,ResultSet queryResult , OnFinishHandler finishHandler) throws Exception;
	
	public OutputFormat getOutputFormat();
	public void validate(OutputFormatProps outputFormatProps) throws Exception;
	
	public static interface OnFinishHandler {
		public void finish() throws Exception ;
		public boolean isFinished();
	}
}
