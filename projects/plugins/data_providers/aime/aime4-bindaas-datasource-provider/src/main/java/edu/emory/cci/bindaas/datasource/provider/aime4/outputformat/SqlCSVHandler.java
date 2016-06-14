package edu.emory.cci.bindaas.datasource.provider.aime4.outputformat;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import au.com.bytecode.opencsv.CSVWriter;
import au.com.bytecode.opencsv.ResultSetHelperService;

import edu.emory.cci.bindaas.framework.model.QueryResult;
import edu.emory.cci.bindaas.framework.util.StandardMimeType;
import edu.emory.cci.bindaas.datasource.provider.aime4.bundle.Activator;
import edu.emory.cci.bindaas.datasource.provider.aime4.model.OutputFormatProps;
import edu.emory.cci.bindaas.datasource.provider.aime4.model.OutputFormatProps.OutputFormat;
import edu.emory.cci.bindaas.datasource.provider.aime4.model.OutputFormatProps.QueryType;

public class SqlCSVHandler implements IFormatHandler{
	

	public SqlCSVHandler()
	{
		Activator.getContext().registerService(IFormatHandler.class.getName(), this, null);
	}
	
	@Override
	public QueryResult format(OutputFormatProps outputFormatProps,
			ResultSet queryResult) throws Exception {
		
		StringWriter sw = new StringWriter();
		CSVWriter csvWriter = new CSVWriter(sw,',' , CSVWriter.NO_QUOTE_CHARACTER);
		csvWriter.setResultService(new ResultSetHelperService(){
			
			public String[] getColumnValues(ResultSet rs) throws SQLException, IOException {

		        List<String> values = new ArrayList<String>();
		        ResultSetMetaData metadata = rs.getMetaData();

		        for (int i = 0; i < metadata.getColumnCount(); i++) {
		            values.add(rs.getString(i + 1));
		        }

		        String[] valueArray = new String[values.size()];
		        return values.toArray(valueArray);
		    }

			
			
		});
		csvWriter.writeAll(queryResult,true);
		csvWriter.close();
		
		QueryResult qr = new QueryResult();
		qr.setData(new ByteArrayInputStream(sw.toString().getBytes()));
		qr.setMimeType(StandardMimeType.CSV.toString());
		return qr;


	}

	@Override
	public QueryType getQueryType() {

		return QueryType.SQL;
	}

	@Override
	public OutputFormat getOutputFormat() {

		return OutputFormat.CSV;
	}

	@Override
	public void validate(OutputFormatProps outputFormatProps) throws Exception {
		if(outputFormatProps.getOutputFormat()!=OutputFormat.CSV || outputFormatProps.getQueryType() != QueryType.SQL)
			throw new Exception("Incompatible OutputFormat and/or QueryType specified. Expected QueryType =[" + QueryType.SQL + "] and OutputFormat=["+ OutputFormat.CSV + "]");
		
	}

}
