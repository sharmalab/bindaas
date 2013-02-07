package edu.emory.cci.bindaas.datasource.provider.aime;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.ibm.db2.jcc.DB2Driver;

import edu.emory.cci.bindaas.framework.api.IDeleteHandler;
import edu.emory.cci.bindaas.framework.api.IProvider;
import edu.emory.cci.bindaas.framework.api.IQueryHandler;
import edu.emory.cci.bindaas.framework.api.ISubmitHandler;
import edu.emory.cci.bindaas.framework.model.Profile;
import edu.emory.cci.bindaas.framework.model.ProviderException;
import edu.emory.cci.bindaas.framework.model.SubmitEndpoint;
import edu.emory.cci.bindaas.framework.model.SubmitEndpoint.Type;
import edu.emory.cci.bindaas.framework.util.DocumentationUtil;
import edu.emory.cci.bindaas.framework.util.GSONUtil;
import edu.emory.cci.bindaas.framework.util.IOUtils;
import edu.emory.cci.bindaas.datasource.provider.aime.model.DataSourceConfiguration;
import edu.emory.cci.bindaas.datasource.provider.aime.model.SubmitEndpointProperties;
import edu.emory.cci.bindaas.datasource.provider.aime.model.SubmitEndpointProperties.InputType;

public class AIMEProvider implements IProvider{

	
	private  AIMEQueryHandler aimeQueryHandler;
	private  AIMEDeleteHandler aimeDeleteHandler;
	private  AIMESubmitHandler aimeSubmitHandler;
	private  String createAIMETableQuery ; 
	private  String dropAIMETableQuery ;
	private  String submitEndpointDefaultName = "xml";
	public final static int VERSION = 1;
	private Log log = LogFactory.getLog(getClass());
	private static DB2Driver dbDriver;
	private static final String DOCUMENTATION_RESOURCES_LOCATION = "META-INF/documentation";
	private JsonObject documentation;
	
	
	public void init() {
		
		dbDriver = new DB2Driver();
		Dictionary<String, Object> props = new Hashtable<String, Object>();
		
		props.put("class", getClass().getName());
		props.put("driver", dbDriver.getClass().getName());
		props.put("driver-version(major/minor)", dbDriver.getMajorVersion() + "/" + dbDriver.getMinorVersion());
		Activator.getContext().registerService(IProvider.class.getName(), this, props);
		
		// initialize documentation object
		
		documentation = DocumentationUtil.getProviderDocumentation(Activator.getContext(), DOCUMENTATION_RESOURCES_LOCATION);
		
	}
	
	public String getDropAIMETableQuery() {
		return dropAIMETableQuery;
	}

	public void setDropAIMETableQuery(String dropAIMETableQuery) {
		this.dropAIMETableQuery = dropAIMETableQuery;
	}

	
	public AIMEQueryHandler getAimeQueryHandler() {
		return aimeQueryHandler;
	}


	public void setAimeQueryHandler(AIMEQueryHandler aimeQueryHandler) {
		this.aimeQueryHandler = aimeQueryHandler;
	}


	public AIMEDeleteHandler getAimeDeleteHandler() {
		return aimeDeleteHandler;
	}


	public void setAimeDeleteHandler(AIMEDeleteHandler aimeDeleteHandler) {
		this.aimeDeleteHandler = aimeDeleteHandler;
	}


	public AIMESubmitHandler getAimeSubmitHandler() {
		return aimeSubmitHandler;
	}


	public void setAimeSubmitHandler(AIMESubmitHandler aimeSubmitHandler) {
		this.aimeSubmitHandler = aimeSubmitHandler;
	}


	public String getCreateAIMETableQuery() {
		return createAIMETableQuery;
	}


	public void setCreateAIMETableQuery(String createAIMETableQuery) {
		this.createAIMETableQuery = createAIMETableQuery;
	}


	public String getSubmitEndpointDefaultName() {
		return submitEndpointDefaultName;
	}


	public void setSubmitEndpointDefaultName(String submitEndpointDefaultName) {
		this.submitEndpointDefaultName = submitEndpointDefaultName;
	}
	@Override
	public String getId() {
		
		return AIMEProvider.class.getName();
	}

	@Override
	public int getVersion() {

		return VERSION;
	}

	@Override
	public JsonObject getDocumentation() {

		
		return documentation;
	}

	@Override
	public IQueryHandler getQueryHandler() {

		return aimeQueryHandler;
	}

	@Override
	public ISubmitHandler getSubmitHandler() {

		return aimeSubmitHandler;
	}

	@Override
	public IDeleteHandler getDeleteHandler() {

		return aimeDeleteHandler;
	}

	@Override
	public Profile validateAndInitializeProfile(Profile profile)
			throws ProviderException {
		
		JsonObject dataSourceProps = profile.getDataSource();
		DataSourceConfiguration configuration = GSONUtil.getGSONInstance().fromJson(dataSourceProps, DataSourceConfiguration.class);
		try {
			configuration.validate();
			testConnection(configuration);
			
			if(configuration.isAutoInitialize() == true)
			{
				autoInitializeProvider(configuration);
				SubmitEndpoint submitEndpoint = new SubmitEndpoint();
				submitEndpoint.setCreatedBy(profile.getCreatedBy());
				submitEndpoint.setName(submitEndpointDefaultName);
				submitEndpoint.setType(Type.FORM_DATA);
				
				SubmitEndpointProperties seProps = new SubmitEndpointProperties();
				seProps.setTableName(configuration.getTableName());
				seProps.setInputType(InputType.XML);
				
				JsonObject properties  = GSONUtil.getGSONInstance().toJsonTree(seProps).getAsJsonObject(); // TODO : add a breakpoint here for testing
				submitEndpoint.setProperties(properties);
				profile.getSubmitEndpoints().put(submitEndpoint.getName(), submitEndpoint);
			}
			
		} catch (Exception e) {
			log.error(e);
			throw new ProviderException(AIMEProvider.class.getName(),AIMEProvider.VERSION,e);
		}
		
		return profile;
	}

	
	public static Connection getConnection(DataSourceConfiguration configuration) throws Exception
	{
		String url = configuration.getUrl();
		String username = configuration.getUsername();
		String password = configuration.getPassword();

		Properties connectionProps = new Properties();
		connectionProps.put("user", username);
		connectionProps.put("password", password);
		Connection connection = dbDriver.connect(url, connectionProps);
		return connection;
	}
	
	private void autoInitializeProvider(DataSourceConfiguration configuration) throws Exception {
		Connection connection = getConnection(configuration);
		try {
			
			String createTableQuery = String.format(createAIMETableQuery,configuration.getTableName());
			String dropTableQuery = String.format(dropAIMETableQuery, configuration.getTableName());
			Statement statement = connection.createStatement();
			try {
				statement.execute(dropTableQuery);
			}
			catch(Exception e)
			{
				log.warn("Failed to drop table " +configuration.getTableName());
			}
			
			statement.executeUpdate(createTableQuery);
			
			
		}catch(Exception e)
		{
			log.error(e);
			throw e;
		}
		finally{
			connection.close();
		}
		
	}
	private void testConnection(DataSourceConfiguration configuration) throws Exception{
		Connection connection = getConnection(configuration);
		if(connection!=null)
			connection.close();
		else
			throw new ProviderException(AIMEProvider.class.getName(), AIMEProvider.VERSION , "Cannot establish a database connection with the parameters provided");
		
		
	}
	@Override
	public JsonObject getDataSourceSchema() {

		return new JsonObject();
	}

}
