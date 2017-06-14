package edu.emory.cci.bindaas.datasource.provider.aime4.outputformat;

import java.io.ByteArrayInputStream;
import java.sql.ResultSet;

import edu.emory.cci.bindaas.framework.model.QueryResult;
import edu.emory.cci.bindaas.framework.util.StandardMimeType;
import edu.emory.cci.bindaas.datasource.provider.aime4.bundle.Activator;
import edu.emory.cci.bindaas.datasource.provider.aime4.model.OutputFormatProps;
import edu.emory.cci.bindaas.datasource.provider.aime4.model.OutputFormatProps.OutputFormat;
import edu.emory.cci.bindaas.datasource.provider.aime4.model.OutputFormatProps.QueryType;

public class XqueryXMLHandler implements IFormatHandler {

	
	
	public XqueryXMLHandler()
	{
		Activator.getContext().registerService(IFormatHandler.class.getName(), this, null);
	}
	
	@Override
	public QueryResult format(OutputFormatProps outputFormatProps,
			ResultSet queryResult) throws Exception {
		QueryResult qr = new QueryResult();
		StringBuffer retVal = new StringBuffer();
		retVal.append(String.format("<%s>", outputFormatProps.getRootElement())).append("\n");
		 while (queryResult.next())
	      {
			 retVal.append(queryResult.getString(1)).append("\n");
	      }
		 retVal.append(String.format("</%s>",outputFormatProps.getRootElement()));
	     

		 qr.setData(new ByteArrayInputStream(retVal.toString().getBytes()));
		 qr.setMimeType(StandardMimeType.XML.toString());
		return qr;
		
	}

	@Override
	public QueryType getQueryType() {
		
		return QueryType.XQUERY;
	}

	@Override
	public OutputFormat getOutputFormat() {

		return OutputFormat.XML;
	}

	@Override
	public void validate(OutputFormatProps outputFormatProps) throws Exception {
		if(outputFormatProps.getOutputFormat()!=OutputFormat.XML || outputFormatProps.getQueryType() != QueryType.XQUERY)
			throw new Exception("Incompatible OutputFormat and/or QueryType specified. Expected QueryType =[" + QueryType.XQUERY + "] and OutputFormat=["+ OutputFormat.XML + "]");
		if(outputFormatProps.getRootElement() == null) throw new Exception("Root XML Element not provided");
	}

}
