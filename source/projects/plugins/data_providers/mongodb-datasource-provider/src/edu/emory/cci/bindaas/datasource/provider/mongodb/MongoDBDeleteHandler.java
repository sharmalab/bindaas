package edu.emory.cci.bindaas.datasource.provider.mongodb;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.WriteResult;
import com.mongodb.util.JSON;

import edu.emory.cci.bindaas.datasource.provider.mongodb.model.DataSourceConfiguration;
import edu.emory.cci.bindaas.framework.api.IDeleteHandler;
import edu.emory.cci.bindaas.framework.model.ProviderException;
import edu.emory.cci.bindaas.framework.model.QueryResult;
import edu.emory.cci.bindaas.framework.util.GSONUtil;
import edu.emory.cci.bindaas.framework.util.StandardMimeType;

public class MongoDBDeleteHandler implements IDeleteHandler {

	private Log log = LogFactory.getLog(getClass());
	private JsonParser parser = new JsonParser();

	@Override
	public QueryResult delete(JsonObject dataSource, String deleteQueryToExecute)
			throws ProviderException {

		try {
			DataSourceConfiguration configuration = GSONUtil.getGSONInstance()
					.fromJson(dataSource, DataSourceConfiguration.class);
			Mongo mongo = null;
			try {
				mongo = new Mongo(configuration.getHost(),configuration.getPort());
				DB db = mongo.getDB(configuration.getDb());
				DBCollection collection = db.getCollection(configuration.getCollection());
				DBObject query = (DBObject) JSON.parse(deleteQueryToExecute);
				int count = collection.find(query).count();
				collection.remove(query);	
				JsonObject res = new JsonObject();
				res.add("success", new JsonPrimitive(true));
				res.add("rowsDeleted", new JsonPrimitive(count));
				res.add("query", parser.parse(query.toString()));
				QueryResult queryResult = new QueryResult();
				queryResult.setCallback(false);
				queryResult.setError(false);
				queryResult.setData(res.toString().getBytes());
				queryResult.setMimeType(StandardMimeType.JSON.toString());
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
		} catch (Exception e) {
			log.error(e);
			throw new ProviderException(MongoDBProvider.class.getName() , MongoDBProvider.VERSION ,e);
		}

	}

}
