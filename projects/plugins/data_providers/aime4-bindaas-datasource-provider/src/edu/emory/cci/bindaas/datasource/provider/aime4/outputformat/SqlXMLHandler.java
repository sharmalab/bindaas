package edu.emory.cci.bindaas.datasource.provider.aime4.outputformat;

import java.io.ByteArrayInputStream;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import edu.emory.cci.bindaas.framework.model.QueryResult;
import edu.emory.cci.bindaas.framework.util.StandardMimeType;
import edu.emory.cci.bindaas.datasource.provider.aime4.bundle.Activator;
import edu.emory.cci.bindaas.datasource.provider.aime4.model.OutputFormatProps;
import edu.emory.cci.bindaas.datasource.provider.aime4.model.OutputFormatProps.OutputFormat;
import edu.emory.cci.bindaas.datasource.provider.aime4.model.OutputFormatProps.QueryType;

public class SqlXMLHandler implements IFormatHandler {

	
	public SqlXMLHandler()
	{
		Activator.getContext().registerService(IFormatHandler.class.getName(), this, null);
	}
	
	@Override
	public QueryResult format(OutputFormatProps outputFormatProps,
			ResultSet queryResult) throws Exception {
		QueryResult qr = new QueryResult();
		qr.setData(new ByteArrayInputStream(toXML(queryResult).getBytes()));
		qr.setMimeType(StandardMimeType.XML.toString());
		return qr;
	
	}

	@Override
	public QueryType getQueryType() {
		
		return QueryType.SQL;
	}

	@Override
	public OutputFormat getOutputFormat() {

		return OutputFormat.XML;
	}
	
	public static String toXML(ResultSet rs) throws SQLException
    {
        ResultSetMetaData rsmd = rs.getMetaData();
        int colCount = rsmd.getColumnCount();
        StringBuffer xml = new StringBuffer();
        xml.append("<Results>").append("\n");

        while (rs.next())
        {
            xml.append("<Row>").append("\n");

            for (int i = 1; i <= colCount; i++)
            {
                String columnName = rsmd.getColumnLabel(i);
                String value = rs.getString(i);
                xml.append("<" + columnName).append(" type='" + rsmd.getColumnTypeName(i) + "' >");

                if (value != null)
                {
                    xml.append(value);
                }
                xml.append("</" + columnName + ">").append("\n");
            }
            xml.append("</Row>").append("\n");
        }

        xml.append("</Results>");

        return xml.toString();
    }

	

	@Override
	public void validate(OutputFormatProps outputFormatProps) throws Exception {
		if(outputFormatProps.getOutputFormat()!=OutputFormat.XML || outputFormatProps.getQueryType() != QueryType.SQL)
			throw new Exception("Incompatible OutputFormat and/or QueryType specified. Expected QueryType =[" + QueryType.SQL + "] and OutputFormat=["+ OutputFormat.XML + "]");
		if(outputFormatProps.getRootElement() == null) throw new Exception("Root XML Element not provided");
	}

}
