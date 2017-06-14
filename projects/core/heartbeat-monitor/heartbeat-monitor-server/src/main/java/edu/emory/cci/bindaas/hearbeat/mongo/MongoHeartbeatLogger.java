package edu.emory.cci.bindaas.hearbeat.mongo;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.emory.cci.bindaas.core.util.DynamicObject;
import edu.emory.cci.bindaas.hearbeat.api.IHeartbeatLogger;
import edu.emory.cci.bindaas.hearbeat.bundle.Activator;
import edu.emory.cci.bindaas.hearbeat.impl.model.Heartbeat;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;

public class MongoHeartbeatLogger implements IHeartbeatLogger {

	private MongoHeartbeatLoggerConfiguration defaultConfiguration;
	public MongoHeartbeatLoggerConfiguration getDefaultConfiguration() {
		return defaultConfiguration;
	}

	public void setDefaultConfiguration(
			MongoHeartbeatLoggerConfiguration defaultConfiguration) {
		this.defaultConfiguration = defaultConfiguration;
	}

	private DynamicObject<MongoHeartbeatLoggerConfiguration> configuration;
	private Log log = LogFactory.getLog(getClass());
	private DBCollection dbCollection;
	
	public void init() throws Exception
	{
		if(defaultConfiguration!=null)
		{
			configuration = new DynamicObject<MongoHeartbeatLoggerConfiguration>("heartbeat-monitor",defaultConfiguration , Activator.getContext());
			MongoHeartbeatLoggerConfiguration effectiveConfig = configuration.getObject();
			Mongo mongo = new Mongo(effectiveConfig.getHost() , effectiveConfig.getPort());
			DB db = mongo.getDB(effectiveConfig.getDb());
			this.dbCollection = db.getCollection(effectiveConfig.getCollection());
		}
		else throw new Exception("Default Configuration not specified");
	}
	
	@Override
	public void logHeartbeat(Heartbeat heartbeat) throws IOException {
		
		if(configuration.getObject().isEnable())
		{
			DBObject dbObject = (DBObject)  com.mongodb.util.JSON.parse(heartbeat.toString());
			DBObject query = new BasicDBObject();
			query.put("uniqueId", heartbeat.getUniqueId());
			dbCollection.update(query, dbObject	, true, false );
			log.trace("Logged heartbeat :\n" + heartbeat);
	
		}
	}

}
