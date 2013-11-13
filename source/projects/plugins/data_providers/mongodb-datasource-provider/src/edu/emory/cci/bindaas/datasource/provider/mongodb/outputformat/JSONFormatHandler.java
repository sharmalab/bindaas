package edu.emory.cci.bindaas.datasource.provider.mongodb.outputformat;

import com.mongodb.DBCursor;

import edu.emory.cci.bindaas.datasource.provider.mongodb.model.OutputFormat;
import edu.emory.cci.bindaas.datasource.provider.mongodb.model.OutputFormatProps;
import edu.emory.cci.bindaas.datasource.provider.mongodb.util.JsonResultSetInputStream;
import edu.emory.cci.bindaas.datasource.provider.mongodb.util.MongoResultSetIterator;
import edu.emory.cci.bindaas.framework.model.QueryResult;
import edu.emory.cci.bindaas.framework.util.StandardMimeType;

public class JSONFormatHandler extends AbstractFormatHandler{

	
	@Override
	public QueryResult format(OutputFormatProps outputFormatProps,
			DBCursor cursor , OnFinishHandler finishHandler) throws Exception {
		QueryResult queryResult = new QueryResult();
		queryResult.setData(new JsonResultSetInputStream(cursor , finishHandler));
		queryResult.setMimeType(StandardMimeType.JSON.toString());
		queryResult.setIntermediateResult(new MongoResultSetIterator(cursor, finishHandler));
		return queryResult;
	}

	@Override
	public OutputFormat getOutputFormat() {

		return OutputFormat.JSON;
	}

	@Override
	public void validate(OutputFormatProps outputFormatProps) throws Exception {
		check(outputFormatProps!=null, "outputFormat not specified ");
		check(outputFormatProps.getOutputFormat().equals(OutputFormat.JSON) , "Invalid output format. Expected = JSON");
		
	}
	
	private void check(boolean condition , String message) throws Exception
	{
		if(!condition) throw new Exception(message);
	}

}
