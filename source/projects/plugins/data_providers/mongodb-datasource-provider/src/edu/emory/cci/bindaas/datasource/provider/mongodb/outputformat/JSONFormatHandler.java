package edu.emory.cci.bindaas.datasource.provider.mongodb.outputformat;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import edu.emory.cci.bindaas.datasource.provider.mongodb.model.OutputFormat;
import edu.emory.cci.bindaas.datasource.provider.mongodb.model.OutputFormatProps;
import edu.emory.cci.bindaas.framework.model.QueryResult;
import edu.emory.cci.bindaas.framework.util.StandardMimeType;

public class JSONFormatHandler extends AbstractFormatHandler{

	private JsonParser parser = new JsonParser();
	@Override
	public QueryResult format(OutputFormatProps outputFormatProps,
			DBCursor cursor) throws Exception {
		
		JsonArray array = new JsonArray();
		while(cursor.hasNext())
		{
			DBObject dbObject = cursor.next();
			String jsonStr = dbObject.toString();
			JsonElement ele = parser.parse(jsonStr);
			array.add(ele);
		}
		
		QueryResult queryResult = new QueryResult();
		queryResult.setData(array.toString().getBytes());
		queryResult.setMimeType(StandardMimeType.JSON.toString());
		queryResult.setIntermediateResult(array);
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
