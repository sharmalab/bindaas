package edu.emory.cci.bindaas.datasource.provider.mongodb.outputformat;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import au.com.bytecode.opencsv.CSVWriter;

import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import edu.emory.cci.bindaas.datasource.provider.mongodb.model.OutputFormat;
import edu.emory.cci.bindaas.datasource.provider.mongodb.model.OutputFormatProps;
import edu.emory.cci.bindaas.framework.model.QueryResult;
import edu.emory.cci.bindaas.framework.util.StandardMimeType;

// TODO implement this later
public class CSVFormatHandler extends AbstractFormatHandler {

	@SuppressWarnings("restriction")
	@Override
	public QueryResult format(OutputFormatProps outputFormatProps,
			DBCursor cursor , OnFinishHandler finishHandler) throws Exception {
		try{

			List<Map<String,String>> listOfValues = new ArrayList<Map<String,String>>();
			Set<String> uniqueColumns = new HashSet<String>();
			
			while(cursor.hasNext())
			{
				DBObject dbObject = cursor.next();
				Map<String,String> map = new HashMap<String, String>();
				for(String key : dbObject.keySet())
				{
					map.put(key, dbObject.get(key) != null ? dbObject.get(key).toString() : "");
					uniqueColumns.add(key);
				}
				listOfValues.add(map);
			}
			
			// construct CSV
			StringWriter sw = new StringWriter();
			CSVWriter writer = new CSVWriter(sw, ',',CSVWriter.DEFAULT_QUOTE_CHARACTER);
			
			String[] columns = uniqueColumns.toArray(new String[uniqueColumns.size()]);
			
			writer.writeNext(columns);
			for(Map<String,String> row : listOfValues)
			{
				String[] csvVals = new String[columns.length];
				for(int index = 0; index < columns.length ; index++)
				{
					csvVals[index] = row.get(columns[index]);
				}
				writer.writeNext(csvVals);
			}
			
			writer.close();
			
			QueryResult queryResult = new QueryResult();
			queryResult.setData(new ByteArrayInputStream(sw.toString().getBytes()));
			queryResult.setMimeType(StandardMimeType.CSV.toString());
			return queryResult;
		}catch(Exception e)
		{
			throw e;
		}
		finally{
			finishHandler.finish() ; // TODO implement streaming
		}
		
		
	}

	@Override
	public OutputFormat getOutputFormat() {
	
		return OutputFormat.CSV; 
	}

	@Override
	public void validate(OutputFormatProps outputFormatProps) throws Exception {
		check(outputFormatProps.getOutputFormat()!=null, "OutputFormat not specified");
		check(outputFormatProps.getOutputFormat() == OutputFormat.CSV, "OutputFormat must be CSV");
		
	}
	
	private void check(boolean condition , String message) throws Exception
	{
		if(!condition) throw new Exception(message);
	}
	
}
