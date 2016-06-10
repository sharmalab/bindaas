package edu.emory.cci.bindaas.datasource.provider.genericsql.outputformat.util;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.apache.commons.lang3.StringEscapeUtils;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class JDBCUtils {

	public static JsonObject fromResultSetAsJson(ResultSet rs , ResultSetMetaData rsmd) throws SQLException
	{
		// convert resultset to json
				
				int numColumns = rsmd.getColumnCount();
				JsonObject obj = new JsonObject();

				for (int i = 1; i < numColumns + 1; i++) {
					String column_name = rsmd.getColumnLabel(i);
					try {

						if (rsmd.getColumnType(i) == java.sql.Types.ARRAY) {
							obj.add(column_name, new JsonPrimitive(rs
									.getArray(column_name).toString()));
						} else if (rsmd.getColumnType(i) == java.sql.Types.BIGINT) {
							obj.add(column_name,
									new JsonPrimitive(rs
											.getInt(column_name)));
						} else if (rsmd.getColumnType(i) == java.sql.Types.BOOLEAN) {
							obj.add(column_name,
									new JsonPrimitive(rs
											.getBoolean(column_name)));
						} else if (rsmd.getColumnType(i) == java.sql.Types.BLOB) {
							obj.add(column_name, new JsonPrimitive(rs
									.getBlob(column_name).toString()));
						} else if (rsmd.getColumnType(i) == java.sql.Types.DOUBLE) {
							obj.add(column_name,
									new JsonPrimitive(rs
											.getDouble(column_name)));
						} else if (rsmd.getColumnType(i) == java.sql.Types.FLOAT) {
							obj.add(column_name,
									new JsonPrimitive(rs
											.getFloat(column_name)));
						} else if (rsmd.getColumnType(i) == java.sql.Types.INTEGER) {
							obj.add(column_name,
									new JsonPrimitive(rs
											.getInt(column_name)));
						} else if (rsmd.getColumnType(i) == java.sql.Types.NVARCHAR) {
							obj.add(column_name,
									new JsonPrimitive(rs
											.getNString(column_name)));
						} else if (rsmd.getColumnType(i) == java.sql.Types.VARCHAR) {
							obj.add(column_name,
									new JsonPrimitive(rs
											.getString(column_name)));
						} else if (rsmd.getColumnType(i) == java.sql.Types.TINYINT) {
							obj.add(column_name,
									new JsonPrimitive(rs
											.getInt(column_name)));
						} else if (rsmd.getColumnType(i) == java.sql.Types.SMALLINT) {
							obj.add(column_name,
									new JsonPrimitive(rs
											.getInt(column_name)));
						} else if (rsmd.getColumnType(i) == java.sql.Types.DATE) {
							obj.add(column_name, new JsonPrimitive(rs
									.getDate(column_name).toString()));
						} else if (rsmd.getColumnType(i) == java.sql.Types.TIMESTAMP) {
							obj.add(column_name, new JsonPrimitive(rs
									.getTimestamp(column_name).toString()));
						}
						else if (rsmd.getColumnType(i) == java.sql.Types.SQLXML) {
							obj.add(column_name, new JsonPrimitive(rs
									.getString(column_name)));
						}
						else if (rsmd.getColumnType(i) == java.sql.Types.CLOB) {
							obj.add(column_name, new JsonPrimitive(rs
									.getString(column_name)));
						}
						else {
							obj.add(column_name, new JsonPrimitive(rs
									.getObject(column_name).toString()));
						}
					} catch (NullPointerException e) {
						
					}
				}
				return obj;

	}
	
	public static String[] fromResultSetAsCSVRow(ResultSet rs , ResultSetMetaData rsmd) throws SQLException
	{
		// convert resultset to json
				
				int numColumns = rsmd.getColumnCount();
				String[] retVal = new String[numColumns];

				for (int i = 1; i < numColumns + 1; i++) {
					int arrayIndex = i - 1;
					String column_name = rsmd.getColumnLabel(i);
					try {

						if (rsmd.getColumnType(i) == java.sql.Types.ARRAY) {
							retVal[arrayIndex] = rs
									.getArray(column_name).toString(); 
							
						} else if (rsmd.getColumnType(i) == java.sql.Types.BIGINT) {
							retVal[arrayIndex] = new Integer(rs
									.getInt(column_name)).toString();
							
						} else if (rsmd.getColumnType(i) == java.sql.Types.BOOLEAN) {
							
							retVal[arrayIndex] = new Boolean(rs
									.getBoolean(column_name)).toString();
							
						} else if (rsmd.getColumnType(i) == java.sql.Types.BLOB) {
							retVal[arrayIndex] = rs
									.getBlob(column_name).toString();
							
						} else if (rsmd.getColumnType(i) == java.sql.Types.DOUBLE) {
							retVal[arrayIndex] = new Double(rs
									.getDouble(column_name)).toString();
							
							
						} else if (rsmd.getColumnType(i) == java.sql.Types.FLOAT) {
							retVal[arrayIndex] = new Float(rs
									.getFloat(column_name)).toString();
							
						} else if (rsmd.getColumnType(i) == java.sql.Types.INTEGER) {
							retVal[arrayIndex] = new Integer(rs
									.getInt(column_name)).toString();
							
							
						} else if (rsmd.getColumnType(i) == java.sql.Types.NVARCHAR) {
							retVal[arrayIndex] = rs
									.getNString(column_name).toString();
							
							
						} else if (rsmd.getColumnType(i) == java.sql.Types.VARCHAR) {
							retVal[arrayIndex] = rs
									.getString(column_name).toString();
							
							
						} else if (rsmd.getColumnType(i) == java.sql.Types.TINYINT) {
							retVal[arrayIndex] = new Integer(rs
									.getInt(column_name)).toString();
							
							
						} else if (rsmd.getColumnType(i) == java.sql.Types.SMALLINT) {
							retVal[arrayIndex] = new Integer(rs
									.getInt(column_name)).toString();
							
							
						} else if (rsmd.getColumnType(i) == java.sql.Types.DATE) {
							retVal[arrayIndex] = rs
									.getDate(column_name).toString();
							
						} else if (rsmd.getColumnType(i) == java.sql.Types.TIMESTAMP) {
							retVal[arrayIndex] = rs
									.getTimestamp(column_name).toString();
							
						}
						else if (rsmd.getColumnType(i) == java.sql.Types.SQLXML) {
							retVal[arrayIndex] = rs
									.getString(column_name).toString();
						}
						else if (rsmd.getColumnType(i) == java.sql.Types.CLOB) {
							retVal[arrayIndex] = rs
									.getString(column_name).toString();
						}
						else {
							retVal[arrayIndex] = rs
									.getObject(column_name).toString();
						}
					
						retVal[arrayIndex] = String.format("\"%s\"", StringEscapeUtils.escapeEcmaScript(retVal[arrayIndex]));
					
					} catch (NullPointerException e) {
						retVal[arrayIndex] = null;
					}
				}
				return retVal;

	}

}
