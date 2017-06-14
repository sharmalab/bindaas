package edu.emory.cci.bindaas.aime.dataprovider.utils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Hashtable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.JsonObject;

import edu.emory.cci.bindaas.aime.dataprovider.AIMEProvider;
import edu.emory.cci.bindaas.aime.dataprovider.bundle.Activator;
import edu.emory.cci.bindaas.aime.dataprovider.model.DataSourceConfiguration;
import edu.emory.cci.bindaas.aime.dataprovider.model.SubmitEndpointProperties;
import edu.emory.cci.bindaas.aime.dataprovider.model.SubmitEndpointProperties.InputType;
import edu.emory.cci.bindaas.aime.dataprovider.utils.MigrationSummary.MigrationEntry;
import edu.emory.cci.bindaas.core.api.IManagementTasks;
import edu.emory.cci.bindaas.core.model.DeleteEndpointRequestParameter;
import edu.emory.cci.bindaas.core.model.ProfileRequestParameter;
import edu.emory.cci.bindaas.core.model.QueryEndpointRequestParameter;
import edu.emory.cci.bindaas.core.model.SubmitEndpointRequestParameter;
import edu.emory.cci.bindaas.framework.api.ISubmitHandler;
import edu.emory.cci.bindaas.framework.model.Profile;
import edu.emory.cci.bindaas.framework.model.RequestContext;
import edu.emory.cci.bindaas.framework.model.Workspace;
import edu.emory.cci.bindaas.framework.provider.exception.AbstractHttpCodeException;
import edu.emory.cci.bindaas.framework.util.GSONUtil;

/**
 * MigrationUtil ports older version of software to newer version
 * @author nadir
 *
 */
public class MigrationUtil {

	public static final String OLD_AIME_PROVIDER = "edu.emory.cci.bindaas.datasource.provider.aime.AIMEProvider";
	
	private IManagementTasks managementTask;
	private Log log = LogFactory.getLog(getClass());
	private AIMEProvider aimeProvider;
	private RequestContext requestContext;
	
	public IManagementTasks getManagementTask() {
		return managementTask;
	}

	public void setManagementTask(IManagementTasks managementTask) {
		this.managementTask = managementTask;
	}

	public AIMEProvider getAimeProvider() {
		return aimeProvider;
	}

	public void setAimeProvider(AIMEProvider aimeProvider) {
		this.aimeProvider = aimeProvider;
	}

	public void init(){
		this.requestContext = new RequestContext();
		this.requestContext.setUser(getClass().getName());
		
		Hashtable<String, Object> props = new Hashtable<String, Object>();
		props.put("osgi.command.scope", "aime");
		props.put("osgi.command.function", new String[] {"migrate" , "selfUpdate"});
		Activator.getContext().registerService(MigrationUtil.class, this, props);
		
	}

	
	public void selfUpdate(String scope) throws Exception
	{
		if(scope == null)
		{
			// migrate all projects
			selfUpdate();
		}
		else{
			
			if(scope.contains(":"))
			{
				String tokens[] = scope.split(":");
				String projectName = tokens[0];
				String dataProviderName = tokens[1];
				Profile dataProvider = managementTask.getProfile(projectName, dataProviderName);
				if(dataProvider!=null)
				{
					System.out.println(selfUpdate(projectName , dataProvider)); 
				}
				else
					throw new Exception("Project/DataProvider not found");
				
			}
			else
			{
				String projectName = scope;
				Workspace project = managementTask.getWorkspace(projectName);
				if(project!=null)
				{
					System.out.println(selfUpdate(project)); 
				}
				else
					throw new Exception("Project not found");
			}
		}
	}
	
	
	
	
	 private MigrationSummary selfUpdate(Workspace project) throws SQLException {
		 MigrationSummary migrationSummary = new MigrationSummary();
			Collection<Profile> dataProviders = project.getProfiles().values();
			for(Profile dataProvider : dataProviders)
			{
				migrationSummary.getMigrationEntries().addAll(selfUpdate(project.getName() , dataProvider).getMigrationEntries());
			}
			
			return migrationSummary;
	}

	private MigrationSummary selfUpdate(String projectName, Profile dataProvider) throws SQLException {
		MigrationSummary migrationSummary = new MigrationSummary();
		DataSourceConfiguration configuration = GSONUtil.getGSONInstance().fromJson(dataProvider.getDataSource(), DataSourceConfiguration.class);
		if(dataProvider.getProviderId().equals(AIMEProvider.class.getName()) && configuration.isAutoInitialize())
		{
			log.debug("Self Update [" + projectName + "/" + dataProvider.getName() + "]");
			MigrationEntry entry = new MigrationEntry();
			Connection connection = null;
			try{
				
				
				configuration.validate();
				JsonObject newDataSourceConfig = GSONUtil.getGSONInstance().toJsonTree(configuration).getAsJsonObject();
				connection = AIMEProvider.getConnection(configuration);
				Statement st = connection.createStatement();
				
//				String tempTable = configuration.getTableName() +   "_TEMP";
				// create temp table 
				
				String tempTable = createTempTable(configuration.getTableName(), st);

				// copy table contents to temp
				Integer count = copyAnnotations(configuration.getTableName(), tempTable , st , newDataSourceConfig);
				entry.setRowsUpdated(count);

				// drop current table
				dropTable(configuration.getTableName() , st);
				
				// rename temp to current
				renameTable(tempTable, configuration.getTableName() , st);
								
				
				entry.setSuccess(true);
				entry.setOldDataSourceConfiguration(dataProvider.getDataSource());
				entry.setNewDataSourceConfiguration(newDataSourceConfig);
				
				connection.commit();
				
			}catch(Exception e)
			{
				log.error("Migration Error" , e);
				entry.setSuccess(false);
				entry.setErrorDescription(e.getMessage());
			}
			finally{
				entry.setProjectName(projectName);
				entry.setDataProviderName(dataProvider.getName());
				migrationSummary.getMigrationEntries().add(entry);
				if(connection!=null)
				{
					connection.close();
				}
			}
		}
		else
		{
			log.debug("Skipping  [" + projectName + "/" + dataProvider.getName() + "]");
		}
		
		
		return migrationSummary;

	}

	private void selfUpdate() throws Exception {
		MigrationSummary migrationSummary = new MigrationSummary();
		Collection<Workspace> projects = managementTask.listWorkspaces();
		for(Workspace project : projects)
		{
			migrationSummary.getMigrationEntries().addAll(selfUpdate(project).getMigrationEntries());
		}
		System.out.println(migrationSummary);
		
	}

	// scope ==> syntax {project-name}:{data-provider-name} or {project-name}
	public void migrate(String scope) throws Exception
	{
		if(scope == null)
		{
			// migrate all projects
			migrate();
		}
		else{
			
			if(scope.contains(":"))
			{
				String tokens[] = scope.split(":");
				String projectName = tokens[0];
				String dataProviderName = tokens[1];
				Profile dataProvider = managementTask.getProfile(projectName, dataProviderName);
				if(dataProvider!=null)
				{
					System.out.println(migrate(projectName , dataProvider)); 
				}
				else
					throw new Exception("Project/DataProvider not found");
				
			}
			else
			{
				String projectName = scope;
				Workspace project = managementTask.getWorkspace(projectName);
				if(project!=null)
				{
					System.out.println(migrate(project)); 
				}
				else
					throw new Exception("Project not found");
			}
		}
	}
	
	
	private void migrate() throws Exception
	{
		MigrationSummary migrationSummary = new MigrationSummary();
		Collection<Workspace> projects = managementTask.listWorkspaces();
		for(Workspace project : projects)
		{
			migrationSummary.getMigrationEntries().addAll(migrate(project).getMigrationEntries());
		}
		System.out.println(migrationSummary);
		
	}
	
	private MigrationSummary migrate(Workspace project) throws SQLException
	{
		MigrationSummary migrationSummary = new MigrationSummary();
		Collection<Profile> dataProviders = project.getProfiles().values();
		for(Profile dataProvider : dataProviders)
		{
			migrationSummary.getMigrationEntries().addAll(migrate(project.getName() , dataProvider).getMigrationEntries());
		}
		
		return migrationSummary;
	}
	
	private MigrationSummary migrate(String projectName , Profile dataProvider) throws SQLException
	{
		MigrationSummary migrationSummary = new MigrationSummary();
		DataSourceConfiguration configuration = GSONUtil.getGSONInstance().fromJson(dataProvider.getDataSource(), DataSourceConfiguration.class);
		if(dataProvider.getProviderId().equals(OLD_AIME_PROVIDER) && configuration.isAutoInitialize())
		{
			log.debug("Upgrading [" + projectName + "/" + dataProvider.getName() + "]");
			MigrationEntry entry = new MigrationEntry();
			Connection connection = null;
			try{
				
				
				configuration.validate();
				JsonObject newDataSourceConfig = GSONUtil.getGSONInstance().toJsonTree(configuration).getAsJsonObject();
				connection = AIMEProvider.getConnection(configuration);
				Statement st = connection.createStatement();
				
//				String tempTable = configuration.getTableName() +   "_TEMP";
				// create temp table 
				
				String tempTable = createTempTable(configuration.getTableName(), st);
				
				// copy table contents to temp
				Integer count = copyAnnotations(configuration.getTableName(), tempTable , st , newDataSourceConfig);
				entry.setRowsUpdated(count);

				// drop current table
				dropTable(configuration.getTableName() , st);
				
				// rename temp to current
				renameTable(tempTable, configuration.getTableName() , st);
				
				// upgrade to new DataSource provider
				
				
				ProfileRequestParameter perp = new ProfileRequestParameter();
				perp.setDataSource(newDataSourceConfig);
				perp.setProviderId(AIMEProvider.class.getName());
				perp.setProviderVersion(AIMEProvider.VERSION);
				perp.setDescription(dataProvider.getDescription());
				JsonObject dataProviderParameters = GSONUtil.getGSONInstance().toJsonTree(perp).getAsJsonObject();
				
				entry.setSuccess(true);
				entry.setOldDataSourceConfiguration(dataProvider.getDataSource());
				entry.setNewDataSourceConfiguration(newDataSourceConfig);
				
				// delete old data provider
				managementTask.deleteProfile(projectName, dataProvider.getName());
				
				// create new data provider
				Profile newDataProvider = managementTask.createProfile(dataProvider.getName(), projectName, dataProviderParameters , "aime-updater", dataProvider.getDescription());
				importAPIDefinitions ( projectName , dataProvider, newDataProvider);
				connection.commit();
				
			}catch(Exception e)
			{
				log.error("Migration Error" , e);
				entry.setSuccess(false);
				entry.setErrorDescription(e.getMessage());
			}
			finally{
				entry.setProjectName(projectName);
				entry.setDataProviderName(dataProvider.getName());
				migrationSummary.getMigrationEntries().add(entry);
				if(connection!=null)
				{
					connection.close();
				}
			}
		}
		else
		{
			log.debug("Skipping  [" + projectName + "/" + dataProvider.getName() + "]");
		}
		
		
		return migrationSummary;
	}

private void importAPIDefinitions(String projectName , Profile oldProvider , Profile newProvider) throws Exception
{
	// QueryEndpoints
	
	for(String name : oldProvider.getQueryEndpoints().keySet())
	{
		String finalName = name;
		if(newProvider.getQueryEndpoints().containsKey(name))
		{
			finalName = name + "_OLD";
		}
		
		QueryEndpointRequestParameter qerp = QueryEndpointRequestParameter.convert(oldProvider.getQueryEndpoints().get(name)) ; 
		JsonObject parameters = GSONUtil.getGSONInstance().toJsonTree(qerp).getAsJsonObject();
		this.managementTask.createQueryEndpoint(finalName, projectName, newProvider.getName(), parameters, newProvider.getCreatedBy());
	}
	
	// DeleteEndpoint
	for(String name : oldProvider.getDeleteEndpoints().keySet())
	{
		String finalName = name;
		if(newProvider.getDeleteEndpoints().containsKey(name))
		{
			finalName = name + "_OLD";
		}
		
		DeleteEndpointRequestParameter derp = DeleteEndpointRequestParameter.convert(oldProvider.getDeleteEndpoints().get(name)) ; 
		JsonObject parameters = GSONUtil.getGSONInstance().toJsonTree(derp).getAsJsonObject();
		this.managementTask.createDeleteEndpoint(finalName, projectName, newProvider.getName(), parameters, newProvider.getCreatedBy());
	}
	
	// SubmitEndpoint
		for(String name : oldProvider.getSubmitEndpoints().keySet())
		{
			String finalName = name;
			if(newProvider.getSubmitEndpoints().containsKey(name))
			{
				finalName = name + "_OLD";
			}
			
			SubmitEndpointRequestParameter serp = SubmitEndpointRequestParameter.convert(oldProvider.getSubmitEndpoints().get(name)) ; 
			JsonObject parameters = GSONUtil.getGSONInstance().toJsonTree(serp).getAsJsonObject();
			this.managementTask.createSubmitEndpoint(finalName, projectName, newProvider.getName(), parameters, newProvider.getCreatedBy());
		}
	
	
	
}
private String createTempTable(String tableName , Statement st) throws SQLException
{
	
	String tempTable = tableName + "_TEMP";
	log.debug("Creating temporary table [" +  tempTable + "]");
	String createTempTable = aimeProvider.getCreateAIMETableQuery().replaceAll("%s", tempTable);
	try{
		st.execute(createTempTable);
	}catch(SQLException e)
	{
		// if table exist then truncate it
		log.debug(tempTable +  " already exist. Truncating table");
		st.execute("delete from xmlds." + tempTable);
	}
	return tempTable;
}

private Integer copyAnnotations(String oldTable , String newTable , Statement st , JsonObject dataSource) throws SQLException, AbstractHttpCodeException
{
	log.debug("Copying annotations from  [" +  oldTable + "] to [" + newTable + "]");
	ISubmitHandler submitHandler = aimeProvider.getSubmitHandler();
	SubmitEndpointProperties sep = new SubmitEndpointProperties();
	sep.setTableName(newTable);
	sep.setInputType(InputType.XML);
	
	JsonObject endpointProperties = GSONUtil.getGSONInstance().toJsonTree(sep).getAsJsonObject();
	ResultSet rs = st.executeQuery("select xmlcolumn from xmlds." + oldTable);
	Integer count = 0;
	while(rs.next())
	{
		String xmlData = rs.getString(1);
		submitHandler.submit(dataSource, endpointProperties, xmlData, requestContext);
		log.debug("Updated row #" + count++);
	}
	return count;
	
}

private void renameTable(String oldName , String newName , Statement st) throws SQLException
{
	log.debug("Renaming table [" + oldName + "] to [" + newName + "]");
	st.executeUpdate("rename table xmlds." + oldName + " to " + newName);
}

private void dropTable(String tableName , Statement st) throws SQLException{
	log.debug("Dropping table [" + tableName + "]");
	st.executeUpdate("drop table xmlds." + tableName);
}


	
}
