package edu.emory.cci.bindaas.datasource.provider.mongodb.outputformat;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import edu.emory.cci.bindaas.datasource.provider.mongodb.model.OutputFormat;
import edu.emory.cci.bindaas.datasource.provider.mongodb.model.OutputFormatProps;
import edu.emory.cci.bindaas.framework.model.QueryResult;
import edu.emory.cci.bindaas.framework.util.StandardMimeType;

//TODO implement this later
public class HTMLFormatHandler extends AbstractFormatHandler{

	private String headSectionContent;
	public String getHeadSectionContent() {
		return headSectionContent;
	}

	public void setHeadSectionContent(String headSectionContent) {
		this.headSectionContent = headSectionContent;
	}

	@Override
	public QueryResult format(OutputFormatProps outputFormatProps,
			DBCursor cursor, OnFinishHandler finishHandler) throws Exception {
		try{
			String htmlContent = toHTML(cursor);
			QueryResult queryResult = new QueryResult();
			queryResult.setData(new ByteArrayInputStream(htmlContent.getBytes()));
			queryResult.setMimeType(StandardMimeType.HTML.toString());
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
	
		return OutputFormat.HTML; 
	}
	
	@SuppressWarnings("restriction")
	private String toHTML(DBCursor cursor)
		    throws Exception {
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
		
		 String[] columns = uniqueColumns.toArray(new String[uniqueColumns.size()]);
		 StringBuilder builder = new StringBuilder();
		 builder.append("<HTML><BODY>").append(headSectionContent);
		 builder.append("<P ALIGN='center'><TABLE id='data' BORDER=1>").append("\n");
		 
		 // table header
		 builder.append("<thead>");
		 builder.append("<TR>").append("\n");
		 for (int i = 0; i < columns.length; i++) {
		   builder.append("<TH>" + columns[i] + "</TH>");
		   }
		 builder.append("</thead>");
		 builder.append("<tbody>");
		 
		 builder.append("</TR>").append("\n");
		 // the data
		 for(Map<String,String> row : listOfValues) {
		 
		  builder.append("<TR>").append("\n");
		  for (int i = 0; i < columns.length; i++) {
		    builder.append("<TD>" + row.get(columns[i]) + "</TD>");
		    }
		  builder.append("</TR>").append("\n");
		  }
		 builder.append("</tbody>");
		 builder.append("</TABLE></P>");
		 builder.append("</BODY></HTML");
		 return builder.toString();
		}

	@Override
	public void validate(OutputFormatProps outputFormatProps) throws Exception {
		check(outputFormatProps.getOutputFormat()!=null, "OutputFormat not specified");
		check(outputFormatProps.getOutputFormat() == OutputFormat.HTML, "OutputFormat must be HTML");
		
	}
	
	private void check(boolean condition , String message) throws Exception
	{
		if(!condition) throw new Exception(message);
	}

}
