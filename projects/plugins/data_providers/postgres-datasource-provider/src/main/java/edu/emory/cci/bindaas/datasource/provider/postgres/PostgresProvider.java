package edu.emory.cci.bindaas.datasource.provider.postgres;

import java.sql.Driver;

import org.osgi.framework.BundleContext;

import com.google.gson.JsonObject;

import edu.emory.cci.bindaas.datasource.provider.genericsql.AbstractSQLProvider;
import edu.emory.cci.bindaas.datasource.provider.postgres.bundle.Activator;
import edu.emory.cci.bindaas.framework.util.DocumentationUtil;

public class PostgresProvider extends AbstractSQLProvider{
	private static Driver driver = null;
	private static final String DOCUMENTATION_RESOURCES_LOCATION = "META-INF/documentation";
	private JsonObject documentation;
	
	
	public void init() throws Exception
	{
		super.init();
		
		// initialize documentation object
		
		documentation = DocumentationUtil.getProviderDocumentation(Activator.getContext(), DOCUMENTATION_RESOURCES_LOCATION);
	}
	
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
		
		return documentation;
	}

	@Override
	public Driver getDatabaseDriver() throws Exception {
		if(driver == null)
		{
			driver = new org.postgresql.Driver();
		}
		return driver;
	}

	@Override
	public BundleContext getBundleContext() {
		return Activator.getContext();
	}
}
