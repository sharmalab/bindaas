package edu.emory.cci.bindaas.datasource.provider.genericsql.outputformat;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.sql.ResultSet;

import au.com.bytecode.opencsv.CSVWriter;
import edu.emory.cci.bindaas.datasource.provider.genericsql.model.OutputFormat;
import edu.emory.cci.bindaas.datasource.provider.genericsql.model.OutputFormatProps;
import edu.emory.cci.bindaas.framework.model.QueryResult;
import edu.emory.cci.bindaas.framework.util.StandardMimeType;

public class CSVFormatHandler extends AbstractFormatHandler {

	
	@Override
	public QueryResult format(OutputFormatProps outputFormatProps,
			ResultSet queryResult) throws Exception {

		StringWriter sw = new StringWriter();
		CSVWriter csvWriter = new CSVWriter(sw);
		csvWriter.writeAll(queryResult,true);
		csvWriter.close();
		
		QueryResult qr = new QueryResult();
		qr.setData(new ByteArrayInputStream(sw.toString().getBytes()));
		qr.setMimeType(StandardMimeType.CSV.toString());
		return qr;
	}

	@Override
	public OutputFormat getOutputFormat() {
		
		return OutputFormat.CSV;
	}

	@Override
	public void validate(OutputFormatProps outputFormatProps) throws Exception {
		// do nothing as nothing to validate
		
	}
	
}
