package edu.emory.cci.bindaas.datasource.provider.genericsql.outputformat.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Joiner;

public class CSVResultSetInputStream extends InputStream {

	private final ResultSet resultSet;
	private final OnCloseHandler closeHandler;
	private byte[] buffer;
	private Integer position;
	private boolean finished;
	private boolean isFirst;
	private ResultSetMetaData rsmd;
	private final static Log log = LogFactory
			.getLog(CSVResultSetInputStream.class);
	private static Joiner csvJoiner;

	static {
		csvJoiner = Joiner.on(",").useForNull("");
	}
	public static interface OnCloseHandler {
		public void close() throws IOException;
	}

	public CSVResultSetInputStream(ResultSet resultSet , final Connection connection) throws SQLException
	{	
		this(resultSet ,  new OnCloseHandler() {
			
			@Override
			public void close() throws IOException {
				try {
					connection.close();
				} catch (SQLException e) {
					throw new IOException(e);
				}
				
			}
		});
		
	}
	
	public CSVResultSetInputStream(ResultSet resultSet, OnCloseHandler closeHandler)
			throws SQLException {
		this.resultSet = resultSet;
		this.closeHandler = closeHandler;
		this.isFirst = true;
		this.rsmd = resultSet.getMetaData();
	}

	

	private byte[] readNextObject() throws SQLException {
		return ("\n" + serializeObject()).getBytes();
	}

	private String serializeObject() throws SQLException {
		String[] rowElements = JDBCUtils.fromResultSetAsCSVRow(resultSet, rsmd);
		return csvJoiner.join(rowElements);
	}
	
	private String header() throws SQLException{
		int count = rsmd.getColumnCount();
		String[] columns = new String[count];
		for(int i = 0 ; i < count ; i++)
		{
			columns[i] = rsmd.getColumnLabel(i + 1); // column indices are base 1
		}
		
		return csvJoiner.join(columns);
		
	}

	@Override
	public int read() throws IOException {
		try {
			if (buffer == null) {
				// first call of read method
				
				buffer = header().getBytes();
				position = 0;
				return buffer[position++] & (0xff);
			} else {
				// not first call of read method
				if (position < buffer.length) {
					// buffer already has some data in, which hasn't been read
					// yet - returning it
					return buffer[position++] & (0xff);
				} else {
					// all data from buffer was read - checking whether there is
					// next row and re-filling buffer
					boolean hasNext = false;
					
					if (!finished && ! (hasNext = resultSet.next())) {
						finished = true;
						return -1 ; // the buffer was
															// read to the end
															// and there is no
															// rows - end of
															// input stream
					} else if (!finished && hasNext) {
						// there is next row - converting it to byte array and
						// re-filling buffer
						if (isFirst) {
							buffer = readNextObject();
							isFirst = false;
						} else
						buffer = readNextObject();
						position = 0;
						return buffer[position++] & (0xff);
					} else {
						// finished
						return -1;
					}
				}
			}
		} catch (SQLException e) {
			log.error(e);
			close();
			throw new IOException(e);
		}
		

	}

	@Override
	public void close() throws IOException {
		closeHandler.close();
	}
}
