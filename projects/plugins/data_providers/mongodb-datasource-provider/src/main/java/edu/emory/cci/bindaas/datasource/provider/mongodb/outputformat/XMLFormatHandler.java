package edu.emory.cci.bindaas.datasource.provider.mongodb.outputformat;

import java.io.ByteArrayInputStream;
import java.sql.SQLException;

import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import edu.emory.cci.bindaas.datasource.provider.mongodb.model.OutputFormat;
import edu.emory.cci.bindaas.datasource.provider.mongodb.model.OutputFormatProps;
import edu.emory.cci.bindaas.framework.model.QueryResult;
import edu.emory.cci.bindaas.framework.util.StandardMimeType;


public class XMLFormatHandler extends AbstractFormatHandler {
	@Override
	public QueryResult format(OutputFormatProps outputFormatProps,
			DBCursor cursor, OnFinishHandler finishHandler) throws Exception {
		try{
			String xmlContent = toXML(cursor);
			QueryResult queryResult = new QueryResult();
			queryResult.setData(new ByteArrayInputStream(xmlContent.getBytes()));
			queryResult.setMimeType(StandardMimeType.XML.toString());
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
	
		return OutputFormat.XML; 
	}

	@Override
	public void validate(OutputFormatProps outputFormatProps) throws Exception {
		check(outputFormatProps.getOutputFormat()!=null, "OutputFormat not specified");
		check(outputFormatProps.getOutputFormat() == OutputFormat.XML, "OutputFormat must be XML");
		
	}
	
	private void check(boolean condition , String message) throws Exception
	{
		if(!condition) throw new Exception(message);
	}
	
	 @SuppressWarnings("restriction")
	public static String toXML(DBCursor cursor) throws SQLException
	    {
	        
	        
	        StringBuffer xml = new StringBuffer();
	        xml.append("<Results>").append("\n");

	        while (cursor.hasNext())
	        {
	            xml.append("<Row>").append("\n");
	            DBObject record = cursor.next();
	            for (String key : record.keySet())
	            {
	                String columnName = key;
	                Object value = record.get(key);
	                xml.append("<").append(columnName).append(">");

	                if (value != null)
	                {
	                    xml.append(value.toString().trim());
	                }
	                xml.append("</").append(columnName).append(">");
	            }
	            xml.append("</Row>").append("\n");
	        }

	        xml.append("</Results>");

	        return xml.toString();
	    }


}
