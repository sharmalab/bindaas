package edu.emory.cci.bindaas.datasource.provider.mongodb.outputformat;

import com.mongodb.DBCursor;

import edu.emory.cci.bindaas.datasource.provider.mongodb.model.OutputFormat;
import edu.emory.cci.bindaas.datasource.provider.mongodb.model.OutputFormatProps;
import edu.emory.cci.bindaas.framework.model.QueryResult;

public interface IFormatHandler {

	public QueryResult format(OutputFormatProps outputFormatProps,DBCursor cursor , OnFinishHandler finishHandler) throws Exception;
	
	public OutputFormat getOutputFormat();
	public void validate(OutputFormatProps outputFormatProps) throws Exception;
	
	public static interface OnFinishHandler {
		public void finish() throws Exception ;
		public boolean isFinished();
	}
}
