package edu.emory.cci.bindaas.datasource.provider.genericsql.outputformat.util;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.JsonObject;

import edu.emory.cci.bindaas.datasource.provider.genericsql.outputformat.IFormatHandler.OnFinishHandler;
import edu.emory.cci.bindaas.framework.model.ResultSetIterator;

public class JDBCResultSetIterator extends ResultSetIterator {

	private ResultSet resultSet;
	private ResultSetMetaData rsmd;
	private OnFinishHandler finishHandler;
	private boolean cursorAdvanced;
	private boolean hasNext;
	private Integer size;
	

	private final static Log log = LogFactory
			.getLog(JDBCResultSetIterator.class);

	public JDBCResultSetIterator(ResultSet resultSet,
			OnFinishHandler finishHandler) throws SQLException {
		this.resultSet = resultSet;
		this.finishHandler = finishHandler;
		cursorAdvanced = false;
		hasNext = false;
		rsmd = resultSet.getMetaData();
	}

	@Override
	public synchronized boolean hasNext() {

		if (!cursorAdvanced) {
			try {
				hasNext = resultSet.next();
				cursorAdvanced = true;
			} catch (SQLException e) {
				log.fatal(e);
				throw new RuntimeException(e);
			}
		}

		return hasNext;
	}

	@Override
	public synchronized JsonObject next() {
		try {
			hasNext();
			JsonObject obj = JDBCUtils.fromResultSetAsJson(this.resultSet, rsmd);
			cursorAdvanced = false;
			return obj;
		} catch (SQLException e) {
			log.fatal(e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public void remove() {
		throw new RuntimeException("Method Not Implemented");
	}

	@Override
	public synchronized Integer size() {
		if (size == null) {
			try {
				if (this.resultSet.last()) {
					size = this.resultSet.getRow();
					this.resultSet.beforeFirst(); // not rs.first() because the
													// rs.next() below will move
													// on, missing the first
													// element
				}
			} catch (Exception e) {
				log.fatal(e);
				throw new RuntimeException(e);
			}
		}

		return size;
	}

	@Override
	public synchronized void close() throws IOException {
		try {
			this.finishHandler.finish();
		} catch (Exception e) {
			throw new IOException(e);
		}

	}

}
