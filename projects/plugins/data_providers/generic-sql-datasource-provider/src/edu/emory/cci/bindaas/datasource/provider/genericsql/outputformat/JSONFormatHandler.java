package edu.emory.cci.bindaas.datasource.provider.genericsql.outputformat;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import edu.emory.cci.bindaas.datasource.provider.genericsql.model.OutputFormat;
import edu.emory.cci.bindaas.datasource.provider.genericsql.model.OutputFormatProps;
import edu.emory.cci.bindaas.datasource.provider.genericsql.outputformat.util.JDBCResultSetIterator;
import edu.emory.cci.bindaas.datasource.provider.genericsql.outputformat.util.JSONResultSetInputStream;
import edu.emory.cci.bindaas.datasource.provider.genericsql.outputformat.util.JSONResultSetInputStream.OnCloseHandler;
import edu.emory.cci.bindaas.framework.model.QueryResult;
import edu.emory.cci.bindaas.framework.util.StandardMimeType;

public class JSONFormatHandler extends AbstractFormatHandler {

	private Log log = LogFactory.getLog(getClass());

	@Override
	public QueryResult format(OutputFormatProps outputFormatProps,
			ResultSet queryResult , final OnFinishHandler finishHandler) throws Exception {
		QueryResult qr = new QueryResult();
		JDBCResultSetIterator resultSetIterator = new JDBCResultSetIterator(queryResult, finishHandler);
		
		OnCloseHandler closeHandler = new OnCloseHandler() {
			
			@Override
			public void close() throws IOException {
					try {
							finishHandler.finish();
					} catch (Exception e) {
						throw new IOException(e);
					}
				
			}
		};
		
		JSONResultSetInputStream dataInputStream = new JSONResultSetInputStream(queryResult, closeHandler);
		qr.setData(dataInputStream);
		qr.setIntermediateResult(resultSetIterator);
		qr.setMimeType(StandardMimeType.JSON.toString());
		return qr;
	}

	@Override
	public OutputFormat getOutputFormat() {

		return OutputFormat.JSON;
	}

	@Override
	public void validate(OutputFormatProps outputFormatProps) throws Exception {
		// nothing to validate

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
