package edu.emory.cci.bindaas.datasource.provider.aime.outputformat;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

import edu.emory.cci.bindaas.framework.model.QueryResult;
import edu.emory.cci.bindaas.framework.util.StandardMimeType;
import edu.emory.cci.bindaas.datasource.provider.aime.Activator;
import edu.emory.cci.bindaas.datasource.provider.aime.model.OutputFormatProps;
import edu.emory.cci.bindaas.datasource.provider.aime.model.OutputFormatProps.OutputFormat;
import edu.emory.cci.bindaas.datasource.provider.aime.model.OutputFormatProps.QueryType;

public class XqueryCSVHandler implements IFormatHandler{
	
	

	public XqueryCSVHandler()
	{
		Activator.getContext().registerService(IFormatHandler.class.getName(), this, null);
	}
	
	@Override
	public QueryResult format(OutputFormatProps outputFormatProps,
			ResultSet queryResult) throws Exception {
		
		StringBuffer buff = new StringBuffer();
		if(outputFormatProps.getCsvHeader() != null)
		{
			for(String header : outputFormatProps.getCsvHeader())
			{
				buff.append(header).append(",");
			}
			if(buff.lastIndexOf(",")  >= 0 )
			buff.replace(buff.lastIndexOf(","), buff.length() , "\n");
			
		}
		
		while(queryResult.next())
		{
			buff.append(queryResult.getString(1)).append("\n");
		}
		
		QueryResult qr = new QueryResult();
		qr.setCallback(false);
		qr.setData(buff.toString().getBytes());
		qr.setError(false);
		qr.setMime(false);
		qr.setMimeType(StandardMimeType.CSV.toString());
		return qr;

	}

	@Override
	public QueryType getQueryType() {

		return QueryType.XQUERY;
	}

	@Override
	public OutputFormat getOutputFormat() {

		return OutputFormat.CSV;
	}

	@Override
	public void validate(OutputFormatProps outputFormatProps) throws Exception {
		if(outputFormatProps.getOutputFormat()!=OutputFormat.CSV || outputFormatProps.getQueryType() != QueryType.XQUERY)
			throw new Exception("Incompatible OutputFormat and/or QueryType specified. Expected QueryType =[" + QueryType.XQUERY + "] and OutputFormat=["+ OutputFormat.CSV + "]");
	}

}
