package edu.emory.cci.bindaas.datasource.provider.mongodb;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.util.JSON;

import edu.emory.cci.bindaas.datasource.provider.mongodb.model.DataSourceConfiguration;
import edu.emory.cci.bindaas.datasource.provider.mongodb.model.OutputFormat;
import edu.emory.cci.bindaas.datasource.provider.mongodb.model.OutputFormatProps;
import edu.emory.cci.bindaas.datasource.provider.mongodb.outputformat.IFormatHandler;
import edu.emory.cci.bindaas.datasource.provider.mongodb.outputformat.OutputFormatRegistry;
import edu.emory.cci.bindaas.framework.api.IQueryHandler;
import edu.emory.cci.bindaas.framework.model.ProviderException;
import edu.emory.cci.bindaas.framework.model.QueryEndpoint;
import edu.emory.cci.bindaas.framework.model.QueryResult;
import edu.emory.cci.bindaas.framework.util.GSONUtil;

public class MongoDBQueryHandler implements IQueryHandler {

	private Log log = LogFactory.getLog(getClass());
	private OutputFormatRegistry registry;
	private JsonParser parser = new JsonParser();
	public OutputFormatRegistry getRegistry() {
		return registry;
	}

	public void setRegistry(OutputFormatRegistry registry) {
		this.registry = registry;
	}

	@Override
	public QueryResult query(JsonObject dataSource,
			JsonObject outputFormatProps, String queryToExecute)
			throws ProviderException {
		try{
			if(outputFormatProps!=null)
			{
				OutputFormatProps props = GSONUtil.getGSONInstance().fromJson(outputFormatProps, OutputFormatProps.class);
				if(props!=null)
				{
					OutputFormat of = props.getOutputFormat();
					IFormatHandler formatHandler = registry.getHandler(of);
					if(formatHandler!=null)
					{
						JsonArray jsonArray = parser.parse(queryToExecute).getAsJsonArray();
						String queryPart = jsonArray.get(0).toString();
						String keyPart = jsonArray.size() <= 1 ? null : jsonArray.get(1).toString();
						DBObject query = (DBObject) JSON.parse(queryPart); 
						DataSourceConfiguration configuration = GSONUtil.getGSONInstance().fromJson(dataSource, DataSourceConfiguration.class);
						Mongo mongo = null;
						try {
							mongo = new Mongo(configuration.getHost(),configuration.getPort());
							DB db = mongo.getDB(configuration.getDb());
							DBCollection collection = db.getCollection(configuration.getCollection());
							DBCursor cursor = null ;
							
							if(keyPart == null)
								cursor = collection.find(query);
							else
							{
								DBObject keys = (DBObject) JSON.parse(keyPart);
								cursor = collection.find(query, keys);
							}
							
							QueryResult queryResult = formatHandler.format(props, cursor);
							return queryResult;
						} catch (Exception e) {
							log.error(e);
							throw e;
						}
						finally{
							if(mongo!=null)
							{
								mongo.close();
							}
						}
					}
					else
					{
						throw new Exception("No handler found for outputType=[" + of + "]");
					}
					
				}
				else
				{
					throw new Exception("outputFormat could not be parsed");
				}
			}
			else
			{
				throw new Exception("outputFormat not specified");
			}
			
	}
	catch(Exception e)
	{
		log.error(e);
		throw new ProviderException(e);
	}

}

	@Override
	public QueryEndpoint validateAndInitializeQueryEndpoint(
			QueryEndpoint queryEndpoint) throws ProviderException {
		try{
				if(queryEndpoint.getOutputFormat()!=null)
				{
					OutputFormatProps props = GSONUtil.getGSONInstance().fromJson(queryEndpoint.getOutputFormat(), OutputFormatProps.class);
					if(props!=null)
					{
						OutputFormat of = props.getOutputFormat();
						IFormatHandler formatHandler = registry.getHandler(of);
						if(formatHandler!=null)
						{
							formatHandler.validate(props);
							return queryEndpoint;
						}
						else
						{
							throw new Exception("No handler found for outputType=[" + of + "]");
						}
						
					}
					else
					{
						throw new Exception("outputFormat could not be parsed");
					}
				}
				else
				{
					throw new Exception("outputFormat not specified");
				}
				
		}
		catch(Exception e)
		{
			log.error(e);
			throw new ProviderException(e);
		}
	}

	@Override
	public JsonObject getOutputFormatSchema() {
		// TODO later
		return new JsonObject();
	}

}
