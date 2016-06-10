package edu.emory.cci.bindaas.datasource.provider.genericsql.outputformat.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class JSONResultSetInputStream extends InputStream {

	private final ResultSet resultSet;
	private final OnCloseHandler closeHandler;
	private byte[] buffer;
	private Integer position;
	private boolean finished;
	private boolean isFirst;
	private ResultSetMetaData rsmd;
	private final static Log log = LogFactory
			.getLog(JSONResultSetInputStream.class);


	public static interface OnCloseHandler {
		public void close() throws IOException;
	}

	public JSONResultSetInputStream(ResultSet resultSet , final Connection connection) throws SQLException
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
	
	public JSONResultSetInputStream(ResultSet resultSet, OnCloseHandler closeHandler)
			throws SQLException {
		this.resultSet = resultSet;
		this.closeHandler = closeHandler;
		this.isFirst = true;
		this.rsmd = resultSet.getMetaData();
	}

	private byte[] readFirstObject() throws SQLException {
		return serializeObject().getBytes();
	}

	private byte[] readNextObject() throws SQLException {
		return ("," + serializeObject()).getBytes();
	}

	private String serializeObject() throws SQLException {
	
		return JDBCUtils.fromResultSetAsJson(this.resultSet, this.rsmd).toString();
	}

	@Override
	public int read() throws IOException {
		try {
			if (buffer == null) {
				// first call of read method
				buffer = "[".getBytes();
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
						buffer = "]".getBytes();
						position = 0;
						finished = true;
						return buffer[position++] & (0xff); // the buffer was
															// read to the end
															// and there is no
															// rows - end of
															// input stream
					} else if (!finished && hasNext) {
						// there is next row - converting it to byte array and
						// re-filling buffer
						if (isFirst) {
							buffer = readFirstObject();
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
