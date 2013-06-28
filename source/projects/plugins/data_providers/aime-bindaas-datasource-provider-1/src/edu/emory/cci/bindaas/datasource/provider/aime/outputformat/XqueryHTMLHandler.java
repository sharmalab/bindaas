package edu.emory.cci.bindaas.datasource.provider.aime.outputformat;

import java.io.ByteArrayInputStream;
import java.sql.ResultSet;

import edu.emory.cci.bindaas.framework.model.QueryResult;
import edu.emory.cci.bindaas.framework.util.StandardMimeType;
import edu.emory.cci.bindaas.datasource.provider.aime.bundle.Activator;
import edu.emory.cci.bindaas.datasource.provider.aime.model.OutputFormatProps;
import edu.emory.cci.bindaas.datasource.provider.aime.model.OutputFormatProps.OutputFormat;
import edu.emory.cci.bindaas.datasource.provider.aime.model.OutputFormatProps.QueryType;

public class XqueryHTMLHandler implements IFormatHandler {



	
	public XqueryHTMLHandler()
	{
		Activator.getContext().registerService(IFormatHandler.class.getName(), this, null);
	}
	
	@Override
	public QueryResult format(OutputFormatProps outputFormatProps,
			ResultSet queryResult) throws Exception {
		StringBuffer buff = new StringBuffer();
		while(queryResult.next())
		{
			buff.append(queryResult.getString(1)).append("\n");
		}
		
		
		QueryResult qr = new QueryResult();
		
		qr.setData(new ByteArrayInputStream(buff.toString().getBytes()));
		
		qr.setMimeType(StandardMimeType.HTML.toString());
		return qr;

	}

	@Override
	public QueryType getQueryType() {

		return QueryType.XQUERY;
	}

	@Override
	public OutputFormat getOutputFormat() {

		return OutputFormat.HTML;
	}

	@Override
	public void validate(OutputFormatProps outputFormatProps) throws Exception {
		if(outputFormatProps.getOutputFormat()!=OutputFormat.HTML || outputFormatProps.getQueryType() != QueryType.XQUERY)
			throw new Exception("Incompatible OutputFormat and/or QueryType specified. Expected QueryType =[" + QueryType.XQUERY + "] and OutputFormat=["+ OutputFormat.HTML + "]");
		if (outputFormatProps.getRootElement() == null)
			throw new Exception("Root XML Element not provided");
	}

}
