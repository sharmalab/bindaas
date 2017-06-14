package edu.emory.cci.bindaas.framework.model;

import java.io.IOException;
import java.util.Iterator;

import com.google.gson.JsonObject;

public abstract class ResultSetIterator implements Iterator<JsonObject>{

	public abstract Integer size();
	public abstract void close() throws IOException;
}
