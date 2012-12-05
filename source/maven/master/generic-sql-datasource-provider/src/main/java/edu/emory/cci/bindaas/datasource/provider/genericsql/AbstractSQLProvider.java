package edu.emory.cci.bindaas.datasource.provider.genericsql;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.JsonObject;

import edu.emory.cci.bindaas.datasource.provider.genericsql.model.DataSourceConfiguration;
import edu.emory.cci.bindaas.framework.api.IDeleteHandler;
import edu.emory.cci.bindaas.framework.api.IProvider;
import edu.emory.cci.bindaas.framework.api.IQueryHandler;
import edu.emory.cci.bindaas.framework.api.ISubmitHandler;
import edu.emory.cci.bindaas.framework.model.Profile;
import edu.emory.cci.bindaas.framework.model.ProviderException;
import edu.emory.cci.bindaas.framework.util.GSONUtil;

public abstract class AbstractSQLProvider implements IProvider {

	public abstract Driver getDatabaseDriver() throws Exception;
	public final static int VERSION = 1;
	private Driver driver;
	
	
	public void init() throws Exception
	{
		driver = getDatabaseDriver();
		Activator.getContext().registerService(IProvider.class.getName(), this, null);
		queryHandler.setProvider(this);
		deleteHandler.setProvider(this);
		submitHandler.setProvider(this);
	}
	
	public void setQueryHandler(GenericSQLQueryHandler queryHandler) {
		this.queryHandler = queryHandler;
	}


	public void setDeleteHandler(GenericSQLDeleteHandler deleteHandler) {
		this.deleteHandler = deleteHandler;
	}


	public void setSubmitHandler(GenericSQLSubmitHandler submitHandler) {
		this.submitHandler = submitHandler;
	}
	private  GenericSQLQueryHandler queryHandler;
	private  GenericSQLDeleteHandler deleteHandler;
	private  GenericSQLSubmitHandler submitHandler;
	private Log log = LogFactory.getLog(getClass());
	
	
	


	@Override
	public GenericSQLQueryHandler getQueryHandler() {

		return queryHandler;
	}


	@Override
	public GenericSQLSubmitHandler getSubmitHandler() {

		return submitHandler;
	}


	@Override
	public GenericSQLDeleteHandler getDeleteHandler() {

		return deleteHandler;
	}


	@Override
	public Profile validateAndInitializeProfile(Profile profile)
			throws ProviderException {
		JsonObject dataSourceProps = profile.getDataSource();
		DataSourceConfiguration configuration = GSONUtil.getGSONInstance().fromJson(dataSourceProps, DataSourceConfiguration.class);
		Connection connection = null;
		try {
			configuration.validate();
			connection = getConnection(configuration);
			
		} catch (Exception e) {
			log.error(e);
			throw new ProviderException(this.getClass().getName() , this.getVersion() , e);
		}
		finally{
			if(connection!=null)
			{
				try {
					connection.close();
				} catch (SQLException e) {
					log.error(e);
					throw new ProviderException(this.getClass().getName() , this.getVersion() ,e);
				}
			}
		}
		
		return profile;
	}

	public Connection getConnection(DataSourceConfiguration configuration) throws Exception
	{
		String url = configuration.getUrl();
		String username = configuration.getUsername();
		String password = configuration.getPassword();

		Properties connectionProps = new Properties();
		connectionProps.put("user", username);
		connectionProps.put("password", password);
		Connection connection = this.driver.connect(url, connectionProps);
		return connection;
	}
	
	@Override
	public JsonObject getDataSourceSchema() {
		// TODO later
		return new JsonObject();
	}
	
	
	
}
