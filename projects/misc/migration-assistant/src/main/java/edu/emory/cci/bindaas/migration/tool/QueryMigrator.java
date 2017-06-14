package edu.emory.cci.bindaas.migration.tool;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.apache.log4j.spi.LoggerFactory;

import edu.emory.cci.bindaas.migrationasst.core.impl.FileSystemPersistenceDriverImpl;
import edu.emory.cci.bindaas.migrationasst.datasource.provider.aime.model.OutputFormatProps;
import edu.emory.cci.bindaas.migrationasst.datasource.provider.aime.model.OutputFormatProps.OutputFormat;
import edu.emory.cci.bindaas.migrationasst.datasource.provider.aime.model.OutputFormatProps.QueryType;
import edu.emory.cci.bindaas.migrationasst.framework.model.BindVariable;
import edu.emory.cci.bindaas.migrationasst.framework.model.QueryEndpoint;
import edu.emory.cci.bindaas.migrationasst.framework.model.Workspace;
import edu.emory.cci.bindaas.migrationasst.framework.util.GSONUtil;
import edu.emory.cci.bindaas.model.QueryTemplate;
import edu.emory.cci.bindaas.model.User;
import edu.emory.cci.bindaas.mongodb.MongoDBEntityPersistenceDriver;

public class QueryMigrator {

	private String targetWorkspaceName;
	private String targetWorkspaceDirectory;
	private String targetProfileName;
	
	private String mongoHost;
	private int mongoPort;
	private String dbName;
	private String collectionName;
	
	private String sourceWorkspaceName;
	private MongoDBEntityPersistenceDriver mongodbDriver;
	private FileSystemPersistenceDriverImpl fileDriver;
	
	
	
	public void init() throws Exception
	{
		
		mongodbDriver = new MongoDBEntityPersistenceDriver();
		mongodbDriver.setHost(mongoHost);
		mongodbDriver.setPort(mongoPort);
		mongodbDriver.setDb(dbName);
		mongodbDriver.setCollection(collectionName);
		mongodbDriver.init();
		
		fileDriver = new FileSystemPersistenceDriverImpl();
		fileDriver.setMetadataStore(targetWorkspaceDirectory);
		fileDriver.init();
	}
	
	public void start() throws Exception
	{
		// load metadata from mongodb
		List retVal = mongodbDriver.retrieve(User.class, "admin"); 
		User sourceWorkspace = null;
		for(Object obj : retVal)
		{
			User user = (User) obj;
			if(user.getName().equals(sourceWorkspaceName))
			{
				sourceWorkspace = user;
				break;
			}
		}
		
		if(sourceWorkspace!=null){
			
			// load osgi compliant workspace
			String content = this.fileDriver.loadWorkspaceByName(targetWorkspaceName);
			Workspace targetWorkspace = GSONUtil.getGSONInstance().fromJson(content, Workspace.class);
			edu.emory.cci.bindaas.migrationasst.framework.model.Profile profile = targetWorkspace.getProfiles().get(targetProfileName);
			
			Map<String,QueryTemplate> queryTemplates = sourceWorkspace.getListOfQueryTemplates();
			for(QueryTemplate qt : queryTemplates.values())
			{
				QueryEndpoint qe = new QueryEndpoint();
				qe.setCreatedBy(getClass().getName());
				qe.setName(qt.getName());
				qe.setQueryTemplate(qt.getQueryInfo().getTemplate());
				qe.setBindVariables(new HashMap<String, BindVariable>());
				
				for(Map<String,String> bindVar : qt.getQueryInfo().getBindVariables())
				{
					BindVariable bindVariable = new BindVariable();
					bindVariable.setDefaultValue(bindVar.get("defaultValue"));
					bindVariable.setDescription(bindVar.get("description"));
					bindVariable.setName(bindVar.get("name"));
					bindVariable.setRequired(Boolean.parseBoolean(bindVar.get("required") == null ? "false" :   bindVar.get("required") ));
					qe.getBindVariables().put(bindVariable.getName(), bindVariable);
				}
				
				OutputFormatProps props = new OutputFormatProps();
				props.setQueryType(QueryType.XQUERY);
				
				if(qt.getOutputFormat().equalsIgnoreCase("db2csv"))
				{
					props.setOutputFormat(OutputFormat.CSV);
				} else if (qt.getOutputFormat().equalsIgnoreCase("db2xml"))
				{
					props.setOutputFormat(OutputFormat.XML);
				} else if (qt.getOutputFormat().equalsIgnoreCase("db2html"))
				{
					props.setOutputFormat(OutputFormat.HTML);
				}
				
				qe.setOutputFormat(GSONUtil.getGSONInstance().toJsonTree(props).getAsJsonObject());
				qe.setDescription(qt.getQueryInfo().getDescription());
				profile.getQueryEndpoints().put(qe.getName(), qe);
			}
			
			fileDriver.createOrUpdateWorkspace(targetWorkspaceName, targetWorkspace.toString());
			
			
		}else throw new Exception("Source Workspace [" + sourceWorkspaceName + "] not found");
		
	}
	
	
	public String getTargetWorkspaceName() {
		return targetWorkspaceName;
	}

	public void setTargetWorkspaceName(String targetWorkspaceName) {
		this.targetWorkspaceName = targetWorkspaceName;
	}

	public String getTargetWorkspaceDirectory() {
		return targetWorkspaceDirectory;
	}

	public void setTargetWorkspaceDirectory(String targetWorkspaceDirectory) {
		this.targetWorkspaceDirectory = targetWorkspaceDirectory;
	}

	public String getTargetProfileName() {
		return targetProfileName;
	}

	public void setTargetProfileName(String targetProfileName) {
		this.targetProfileName = targetProfileName;
	}

	public String getMongoHost() {
		return mongoHost;
	}

	public void setMongoHost(String mongoHost) {
		this.mongoHost = mongoHost;
	}

	public int getMongoPort() {
		return mongoPort;
	}

	public void setMongoPort(int mongoPort) {
		this.mongoPort = mongoPort;
	}

	public String getDbName() {
		return dbName;
	}

	public void setDbName(String dbName) {
		this.dbName = dbName;
	}

	public String getCollectionName() {
		return collectionName;
	}

	public void setCollectionName(String collectionName) {
		this.collectionName = collectionName;
	}

	public String getSourceWorkspaceName() {
		return sourceWorkspaceName;
	}

	public void setSourceWorkspaceName(String sourceWorkspaceName) {
		this.sourceWorkspaceName = sourceWorkspaceName;
	}

	public static void main(String[] args) throws Exception {
		QueryMigrator migrator = new QueryMigrator();
		migrator.mongoHost = System.getProperty("mongoHost");
		migrator.mongoPort = Integer.parseInt(System.getProperty("mongoPort"));
		migrator.dbName = System.getProperty("dbName");
		migrator.collectionName = System.getProperty("collectionName");
		migrator.sourceWorkspaceName = System.getProperty("sourceWorkspaceName");
		migrator.targetProfileName = System.getProperty("targetProfileName");
		migrator.targetWorkspaceDirectory = System.getProperty("targetWorkspaceDirectory");
		migrator.targetWorkspaceName = System.getProperty("targetWorkspaceName");
		
		migrator.init();
		migrator.start();
		
		
		
		
				
	}
}
