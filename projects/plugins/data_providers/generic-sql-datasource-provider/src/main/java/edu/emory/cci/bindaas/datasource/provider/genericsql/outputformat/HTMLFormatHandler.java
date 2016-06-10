package edu.emory.cci.bindaas.datasource.provider.genericsql.outputformat;

import java.io.ByteArrayInputStream;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

import edu.emory.cci.bindaas.datasource.provider.genericsql.model.OutputFormat;
import edu.emory.cci.bindaas.datasource.provider.genericsql.model.OutputFormatProps;
import edu.emory.cci.bindaas.framework.model.QueryResult;
import edu.emory.cci.bindaas.framework.util.StandardMimeType;

public class HTMLFormatHandler extends AbstractFormatHandler {

	private String headSectionContent;

	public String getHeadSectionContent() {
		return headSectionContent;
	}

	public void setHeadSectionContent(String headSectionContent) {
		this.headSectionContent = headSectionContent;
	}

	@Override
	public QueryResult format(OutputFormatProps outputFormatProps,
			ResultSet queryResult, OnFinishHandler finishHandler)
			throws Exception {

		try {
			QueryResult qr = new QueryResult();
			qr.setData(new ByteArrayInputStream(toHTML(queryResult).getBytes()));
			qr.setMimeType(StandardMimeType.HTML.toString());
			return qr;
		} catch (Exception e) {
			throw e;
		} finally {
			finishHandler.finish();
		}

	}

	@Override
	public OutputFormat getOutputFormat() {

		return OutputFormat.HTML;
	}

	@Override
	public void validate(OutputFormatProps outputFormatProps) throws Exception {
		// TODO Auto-generated method stub

	}

	private String toHTML(java.sql.ResultSet rs) throws Exception {

		StringBuilder builder = new StringBuilder();
		builder.append("<HTML><BODY>").append(headSectionContent);
		builder.append("<P ALIGN='center'><TABLE id='data' BORDER=1>").append(
				"\n");
		ResultSetMetaData rsmd = rs.getMetaData();
		int columnCount = rsmd.getColumnCount();
		// table header
		builder.append("<thead>");
		builder.append("<TR>").append("\n");
		for (int i = 0; i < columnCount; i++) {
			builder.append("<TH>" + rsmd.getColumnLabel(i + 1) + "</TH>");
		}
		builder.append("</TR>").append("\n");
		builder.append("</thead>");
		builder.append("<tbody>");
		// the data
		while (rs.next()) {

			builder.append("<TR>").append("\n");
			for (int i = 0; i < columnCount; i++) {
				builder.append("<TD>" + rs.getString(i + 1) + "</TD>");
			}
			builder.append("</TR>").append("\n");
		}
		builder.append("</tbody>");
		builder.append("</TABLE></P>");
		builder.append("</BODY></HTML");
		return builder.toString();
	}
}
