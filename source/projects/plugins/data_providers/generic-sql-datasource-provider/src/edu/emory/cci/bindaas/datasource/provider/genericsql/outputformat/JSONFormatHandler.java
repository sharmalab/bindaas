package edu.emory.cci.bindaas.datasource.provider.genericsql.outputformat;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import edu.emory.cci.bindaas.datasource.provider.genericsql.model.OutputFormat;
import edu.emory.cci.bindaas.datasource.provider.genericsql.model.OutputFormatProps;
import edu.emory.cci.bindaas.framework.model.QueryResult;
import edu.emory.cci.bindaas.framework.util.StandardMimeType;

public class JSONFormatHandler extends AbstractFormatHandler {

	
	@Override
	public QueryResult format(OutputFormatProps outputFormatProps,
			ResultSet queryResult) throws Exception {

		ResultSetMetaData metadata = queryResult.getMetaData();
		JsonArray array = new JsonArray();
		
		while(queryResult.next())
		{
			JsonObject obj = new JsonObject();
			for(int colIndex = 1 ; colIndex < metadata.getColumnCount() ; colIndex++)
			{
				obj.add(metadata.getColumnLabel(colIndex), new JsonPrimitive(queryResult.getObject(colIndex).toString()));
			}
			array.add(obj);
		}
		
		QueryResult qr = new QueryResult();
		qr.setData(array.toString().getBytes());
		qr.setMimeType(StandardMimeType.JSON.toString());
		return qr;
	}

	@Override
	public OutputFormat getOutputFormat() {

		return OutputFormat.JSON;
	}

	@Override
	public void validate(OutputFormatProps outputFormatProps) throws Exception {
		// nothing to validate
	
	}

}
