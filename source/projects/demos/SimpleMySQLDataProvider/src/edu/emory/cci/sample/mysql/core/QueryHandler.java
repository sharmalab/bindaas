package edu.emory.cci.sample.mysql.core;

import java.io.ByteArrayInputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import edu.emory.cci.bindaas.framework.api.IQueryHandler;
import edu.emory.cci.bindaas.framework.model.ProviderException;
import edu.emory.cci.bindaas.framework.model.QueryEndpoint;
import edu.emory.cci.bindaas.framework.model.QueryResult;
import edu.emory.cci.bindaas.framework.model.RequestContext;
import edu.emory.cci.bindaas.framework.provider.exception.AbstractHttpCodeException;
import edu.emory.cci.bindaas.framework.provider.exception.QueryExecutionFailedException;
import edu.emory.cci.bindaas.framework.util.GSONUtil;
import edu.emory.cci.bindaas.framework.util.StandardMimeType;
import edu.emory.cci.sample.mysql.model.DataSourceConfiguration;

public class QueryHandler implements IQueryHandler{
	private Log log = LogFactory.getLog(getClass());
	@Override
	public JsonObject getOutputFormatSchema() {
		return new JsonObject();
	}
	@Override
	public QueryEndpoint validateAndInitializeQueryEndpoint(QueryEndpoint queryEndpoint)
			throws ProviderException {
		try {
				JsonObject of = queryEndpoint.getOutputFormat();
				if(of.has("format"))
				{
						String format = of.get("format").getAsString();
						if(format.equals("xml") || format.equals("json"))
						{
							return queryEndpoint;
						}
						else
						{
							throw new Exception("invalid format specified");
						}
				}
				else
				{
					throw new Exception("format not specified");
				}
			
		} catch (Exception e) {
			log.error(e);
			throw new ProviderException(SimpleMySQLDataProvider.class.getName() , 1 , 
					"Validation of Input Parameters failed", e);
		}
	}
	
	@Override
	public QueryResult query(JsonObject dataSource,
			JsonObject outputFormatProps, String queryToExecute, Map<String,String> runtimeParameters, RequestContext requestContext)
			throws AbstractHttpCodeException {
		try {
			
			DataSourceConfiguration configuration = GSONUtil
					.getGSONInstance().fromJson(dataSource,
							DataSourceConfiguration.class);
			
			final Connection connection = SimpleMySQLDataProvider.getConnection(configuration);
			Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY,
                    ResultSet.CONCUR_READ_ONLY);
			ResultSet resultSet = statement
					.executeQuery(queryToExecute);
			
			String format = outputFormatProps.get("format").getAsString();
			
			QueryResult queryResult = new QueryResult();
			if(format.equals("json"))
			{
				JsonArray arr = convert(resultSet);
				String data = arr.toString();
				queryResult.setData(new ByteArrayInputStream(data.getBytes()));
				queryResult.setMimeType(StandardMimeType.JSON.toString());
				
			}
			else if (format.equals("xml"))
			{
				String data = toXML(resultSet);
				queryResult.setData(new ByteArrayInputStream(data.getBytes()));
				queryResult.setMimeType(StandardMimeType.JSON.toString());
			}
			else
				throw new Exception("invalid format specified");
			
			
			return queryResult;
		} catch (Exception e) {
			log.error(e);
			throw new QueryExecutionFailedException(SimpleMySQLDataProvider.class.getName() , 1 , "Query Not Executed", e);
		}
	}

	
	public static String toXML(ResultSet rs) throws SQLException {
		ResultSetMetaData rsmd = rs.getMetaData();
		int colCount = rsmd.getColumnCount();
		StringBuffer xml = new StringBuffer();
		xml.append("<Results>").append("\n");

		while (rs.next()) {
			xml.append("<Row>").append("\n");

			for (int i = 1; i <= colCount; i++) {
				String columnName = rsmd.getColumnLabel(i);
				Object value = rs.getObject(i);
				xml.append("<" + columnName).append(
						" type='" + rsmd.getColumnTypeName(i) + "' >");

				if (value != null) {
					xml.append(value.toString().trim());
				}
				xml.append("</" + columnName + ">").append("\n");
			}
			xml.append("</Row>").append("\n");
		}

		xml.append("</Results>");

		return xml.toString();
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
					} else {
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



}
