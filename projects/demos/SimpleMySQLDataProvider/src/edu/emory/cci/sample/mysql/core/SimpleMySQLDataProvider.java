package edu.emory.cci.sample.mysql.core;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.JsonObject;
import com.mysql.jdbc.Driver;

import edu.emory.cci.bindaas.framework.api.IDeleteHandler;
import edu.emory.cci.bindaas.framework.api.IProvider;
import edu.emory.cci.bindaas.framework.api.IQueryHandler;
import edu.emory.cci.bindaas.framework.api.ISubmitHandler;
import edu.emory.cci.bindaas.framework.model.Profile;
import edu.emory.cci.bindaas.framework.model.ProviderException;
import edu.emory.cci.bindaas.framework.provider.exception.MethodNotImplementedException;
import edu.emory.cci.bindaas.framework.util.GSONUtil;
import edu.emory.cci.sample.mysql.bundle.Activator;
import edu.emory.cci.sample.mysql.model.DataSourceConfiguration;

public class SimpleMySQLDataProvider implements IProvider{

	private IQueryHandler queryHandler;
	private IDeleteHandler deleteHandler;
	private Log log = LogFactory.getLog(getClass());
	private static Driver driver;
	
	static{
		try {
			driver = new Driver();  // Initializing the MySQL database driver
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	
	public void init() throws Exception
	{	
		Dictionary<String, String> props = new Hashtable<String, String>();
		props.put("class", getClass().getName());
		props.put("driver", driver.getClass().getName());
		props.put("driver-version(major/minor)", driver.getMajorVersion() + "/" + driver.getMinorVersion());	
		Activator.getContext().registerService(IProvider.class.getName(), this, props);
		log.info("SimpleMySQLDataProvider initialized");
	
	}

	public static Connection getConnection(DataSourceConfiguration configuration) throws Exception
	{
		String url = configuration.getUrl();
		String username = configuration.getUsername();
		String password = configuration.getPassword();

		Properties connectionProps = new Properties();
		connectionProps.put("user", username);
		connectionProps.put("password", password);
		Connection connection = driver.connect(url, connectionProps);
		if(connection!=null)
			return connection;
		else
			throw new Exception("Connection could not be established with the given parameters :\n" + connectionProps);
	}

	@Override
	public JsonObject getDataSourceSchema() {
		return new JsonObject();
	}

	@Override
	public IDeleteHandler getDeleteHandler() {
		return deleteHandler;
	}

	@Override
	public JsonObject getDocumentation() {
		JsonObject documentation = new JsonObject();
		return documentation;
	}

	@Override
	public String getId() {
		return SimpleMySQLDataProvider.class.getName();
	}

	@Override
	public IQueryHandler getQueryHandler() {
		return queryHandler;
	}

	@Override
	public ISubmitHandler getSubmitHandler() {
		throw new RuntimeException(new MethodNotImplementedException(SimpleMySQLDataProvider.class.getName(), 1));
	}

	@Override
	public int getVersion() {
		return 1;
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

	public void setQueryHandler(IQueryHandler queryHandler) {
		this.queryHandler = queryHandler;
	}

	public void setDeleteHandler(IDeleteHandler deleteHandler) {
		this.deleteHandler = deleteHandler;
	}

}
