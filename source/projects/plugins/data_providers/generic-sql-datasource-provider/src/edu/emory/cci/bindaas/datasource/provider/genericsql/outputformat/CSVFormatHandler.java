package edu.emory.cci.bindaas.datasource.provider.genericsql.outputformat;

import java.io.IOException;
import java.sql.ResultSet;

import edu.emory.cci.bindaas.datasource.provider.genericsql.model.OutputFormat;
import edu.emory.cci.bindaas.datasource.provider.genericsql.model.OutputFormatProps;
import edu.emory.cci.bindaas.datasource.provider.genericsql.outputformat.util.CSVResultSetInputStream;
import edu.emory.cci.bindaas.datasource.provider.genericsql.outputformat.util.CSVResultSetInputStream.OnCloseHandler;
import edu.emory.cci.bindaas.framework.model.QueryResult;
import edu.emory.cci.bindaas.framework.util.StandardMimeType;

public class CSVFormatHandler extends AbstractFormatHandler {

	@Override
	public QueryResult format(OutputFormatProps outputFormatProps,
			ResultSet queryResult, final OnFinishHandler finishHandler)
			throws Exception {
		try {
//			StringWriter sw = new StringWriter();
//			CSVWriter csvWriter = new CSVWriter(sw);
//			csvWriter.writeAll(queryResult, true);
//			csvWriter.close();

			
			QueryResult qr = new QueryResult();
			//qr.setData(new ByteArrayInputStream(sw.toString().getBytes()));
			
			qr.setData(new CSVResultSetInputStream(queryResult, new OnCloseHandler() {
				
				@Override
				public void close() throws IOException {
					try {
						finishHandler.finish();
					} catch (Exception e) {
						throw new IOException(e);
					}
					
				}
			}));
			
			qr.setMimeType(StandardMimeType.CSV.toString());
			return qr;
		} catch (Exception e) {
			throw e;
		}

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
