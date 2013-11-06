package edu.emory.cci.bindaas.aime.dataprovider.outputformat;

import java.io.ByteArrayInputStream;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import edu.emory.cci.bindaas.framework.model.QueryResult;
import edu.emory.cci.bindaas.framework.util.StandardMimeType;
import edu.emory.cci.bindaas.aime.dataprovider.bundle.Activator;
import edu.emory.cci.bindaas.aime.dataprovider.model.OutputFormatProps;
import edu.emory.cci.bindaas.aime.dataprovider.model.OutputFormatProps.OutputFormat;
import edu.emory.cci.bindaas.aime.dataprovider.model.OutputFormatProps.QueryType;

public class SqlJSONHandler implements IFormatHandler {

	private Log log = LogFactory.getLog(getClass());
	
	public SqlJSONHandler()
	{
		Activator.getContext().registerService(IFormatHandler.class.getName(), this, null);
	}
	
	@Override
	public QueryResult format(OutputFormatProps outputFormatProps,
			ResultSet queryResult) throws Exception {
		QueryResult qr = new QueryResult();
		qr.setData(new ByteArrayInputStream(convert(queryResult).toString().getBytes()));
		qr.setMimeType(StandardMimeType.JSON.toString());
		return qr;
	
	}

	@Override
	public QueryType getQueryType() {
		
		return QueryType.SQL;
	}

	@Override
	public OutputFormat getOutputFormat() {

		return OutputFormat.JSON;
	}
	
	public JsonArray convert(ResultSet rs) throws SQLException {
		JsonArray json = new JsonArray();
		ResultSetMetaData rsmd = rs.getMetaData();

		while (rs.next()) {
			int numColumns = rsmd.getColumnCount();
			JsonObject obj = new JsonObject();

			for (int i = 1; i < numColumns + 1; i++) {
				String column_name = rsmd.getColumnLabel(i);
				try {

					if (rsmd.getColumnType(i) == java.sql.Types.ARRAY) {
						obj.add(column_name,
								new JsonPrimitive(rs.getArray(column_name)
										.toString()));
					} else if (rsmd.getColumnType(i) == java.sql.Types.BIGINT) {
						obj.add(column_name,
								new JsonPrimitive(rs.getInt(column_name)));
					} else if (rsmd.getColumnType(i) == java.sql.Types.BOOLEAN) {
						obj.add(column_name,
								new JsonPrimitive(rs.getBoolean(column_name)));
					} else if (rsmd.getColumnType(i) == java.sql.Types.BLOB) {
						obj.add(column_name,
								new JsonPrimitive(rs.getBlob(column_name)
										.toString()));
					} else if (rsmd.getColumnType(i) == java.sql.Types.DOUBLE) {
						obj.add(column_name,
								new JsonPrimitive(rs.getDouble(column_name)));
					} else if (rsmd.getColumnType(i) == java.sql.Types.FLOAT) {
						obj.add(column_name,
								new JsonPrimitive(rs.getFloat(column_name)));
					} else if (rsmd.getColumnType(i) == java.sql.Types.INTEGER) {
						obj.add(column_name,
								new JsonPrimitive(rs.getInt(column_name)));
					} else if (rsmd.getColumnType(i) == java.sql.Types.NVARCHAR) {
						obj.add(column_name,
								new JsonPrimitive(rs.getNString(column_name)));
					} else if (rsmd.getColumnType(i) == java.sql.Types.VARCHAR) {
						obj.add(column_name,
								new JsonPrimitive(rs.getString(column_name)));
					} else if (rsmd.getColumnType(i) == java.sql.Types.TINYINT) {
						obj.add(column_name,
								new JsonPrimitive(rs.getInt(column_name)));
					} else if (rsmd.getColumnType(i) == java.sql.Types.SMALLINT) {
						obj.add(column_name,
								new JsonPrimitive(rs.getInt(column_name)));
					} else if (rsmd.getColumnType(i) == java.sql.Types.DATE) {
						obj.add(column_name,
								new JsonPrimitive(rs.getDate(column_name)
										.toString()));
					} else if (rsmd.getColumnType(i) == java.sql.Types.TIMESTAMP) {
						obj.add(column_name,
								new JsonPrimitive(rs.getTimestamp(column_name)
										.toString()));
					}
					else if (rsmd.getColumnType(i) == java.sql.Types.SQLXML) {
						obj.add(column_name,
								new JsonPrimitive(rs.getString(column_name)
										.toString()));
					}
					
					else {
						obj.add(column_name,
								new JsonPrimitive(rs.getObject(column_name)
										.toString()));
					}
				} catch (NullPointerException e) {
					log.debug("Attribute [" + column_name + "] is null");
				}
			}

			json.add(obj);
		}

		return json;
	}
	

	@Override
	public void validate(OutputFormatProps outputFormatProps) throws Exception {
		if(outputFormatProps.getOutputFormat()!=OutputFormat.JSON || outputFormatProps.getQueryType() != QueryType.SQL)
			throw new Exception("Incompatible OutputFormat and/or QueryType specified. Expected QueryType =[" + QueryType.SQL + "] and OutputFormat=["+ OutputFormat.JSON + "]");
		if(outputFormatProps.getRootElement() == null) throw new Exception("Root XML Element not provided");
	}

}
