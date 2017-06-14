package edu.emory.cci.bindaas.datasource.provider.genericsql.outputformat;

import java.io.ByteArrayInputStream;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import edu.emory.cci.bindaas.datasource.provider.genericsql.model.OutputFormat;
import edu.emory.cci.bindaas.datasource.provider.genericsql.model.OutputFormatProps;
import edu.emory.cci.bindaas.framework.model.QueryResult;
import edu.emory.cci.bindaas.framework.util.StandardMimeType;

public class XMLFormatHandler extends AbstractFormatHandler {

	@Override
	public QueryResult format(OutputFormatProps outputFormatProps,
			ResultSet queryResult, OnFinishHandler finishHandler)
			throws Exception {
		try {
			QueryResult qr = new QueryResult();
			qr.setData(new ByteArrayInputStream(toXML(queryResult).getBytes()));
			qr.setMimeType(StandardMimeType.XML.toString());
			return qr;
		} catch (Exception e) {
			throw e;
		} finally {
			finishHandler.finish();
		}

	}

	@Override
	public OutputFormat getOutputFormat() {

		return OutputFormat.XML;
	}

	@Override
	public void validate(OutputFormatProps outputFormatProps) throws Exception {
		// nothing to validate

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
}
