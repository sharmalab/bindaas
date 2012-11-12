package edu.emory.cci.bindaas.datasource.provider.db2;

import java.sql.Driver;

import com.google.gson.JsonObject;
import com.ibm.db2.jcc.DB2Driver;

import edu.emory.cci.bindaas.datasource.provider.genericsql.AbstractSQLProvider;
import edu.emory.cci.bindaas.framework.api.IProvider;

public class DB2Provider extends AbstractSQLProvider{

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
			driver = new DB2Driver();
		}
		return driver;
	}

}
