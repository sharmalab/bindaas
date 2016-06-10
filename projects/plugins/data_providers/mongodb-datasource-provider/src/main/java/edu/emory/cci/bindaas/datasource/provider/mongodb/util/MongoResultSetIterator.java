package edu.emory.cci.bindaas.datasource.provider.mongodb.util;

import java.io.IOException;

import com.google.gson.JsonObject;
import com.mongodb.DBCursor;
import com.mongodb.util.JSON;

import edu.emory.cci.bindaas.datasource.provider.mongodb.outputformat.IFormatHandler.OnFinishHandler;
import edu.emory.cci.bindaas.framework.model.ResultSetIterator;
import edu.emory.cci.bindaas.framework.util.GSONUtil;

public class MongoResultSetIterator extends ResultSetIterator {

	private DBCursor dbCursor ;
	private OnFinishHandler finishHandler;
	
	public MongoResultSetIterator(DBCursor dbCursor , OnFinishHandler finishHandler)
	{
		this.dbCursor = dbCursor;
		this.finishHandler = finishHandler;
	}
	
	@Override
	public boolean hasNext() {

		return dbCursor.hasNext();
	}

	@Override
	public JsonObject next() {
		String str = JSON.serialize(dbCursor.next());
		return GSONUtil.getJsonParser().parse(str).getAsJsonObject();
	}

	@Override
	public void remove() {
		throw new RuntimeException("Method Not Implemented");
		
	}

	@Override
	public Integer size() {
		return dbCursor.size();
	}

	@Override
	public void close() throws IOException {
	try {
		finishHandler.finish();
	} catch (Exception e) {
		throw new IOException(e);
	}
		
	}

}
