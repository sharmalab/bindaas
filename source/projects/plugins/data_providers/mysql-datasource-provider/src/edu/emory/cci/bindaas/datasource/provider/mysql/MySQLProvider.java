package edu.emory.cci.bindaas.datasource.provider.mysql;

import java.sql.Driver;

import com.google.gson.JsonObject;


import edu.emory.cci.bindaas.datasource.provider.genericsql.AbstractSQLProvider;

public class MySQLProvider extends AbstractSQLProvider{
	private static Driver driver = null;
	@Override
	public String getId() {
		
		return getClass().getName();
	}

	@Override
	public int getVersion() {

		return 1;
	}

	@Override
	public JsonObject getDocumentation() {
		// TODO later
		return new JsonObject();
	}

	@Override
	public Driver getDatabaseDriver() throws Exception {
		if(driver == null)
		{
			driver = new com.mysql.jdbc.Driver();
		}
		return driver;
	}
}
