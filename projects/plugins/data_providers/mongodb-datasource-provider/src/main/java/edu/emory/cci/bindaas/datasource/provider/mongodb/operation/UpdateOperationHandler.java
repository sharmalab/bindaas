package edu.emory.cci.bindaas.datasource.provider.mongodb.operation;

import java.io.ByteArrayInputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.WriteResult;
import com.mongodb.util.JSON;

import edu.emory.cci.bindaas.datasource.provider.mongodb.MongoDBProvider;
import edu.emory.cci.bindaas.datasource.provider.mongodb.model.OutputFormatProps;
import edu.emory.cci.bindaas.datasource.provider.mongodb.outputformat.OutputFormatRegistry;
import edu.emory.cci.bindaas.framework.model.ProviderException;
import edu.emory.cci.bindaas.framework.model.QueryResult;
import edu.emory.cci.bindaas.framework.util.GSONUtil;
import edu.emory.cci.bindaas.framework.util.StandardMimeType;

import static edu.emory.cci.bindaas.datasource.provider.mongodb.MongoDBProvider.getAuthorizationRulesCache;

public class UpdateOperationHandler implements IOperationHandler{

	private Log log = LogFactory.getLog(getClass());
	@Override
	public QueryResult handleOperation(DBCollection collection,
			OutputFormatProps outputFormatProps, JsonObject operationArguments , OutputFormatRegistry registry, Object role, boolean authorization )
			throws ProviderException {
	
		UpdateOperationDescriptor operationDescriptor = GSONUtil.getGSONInstance().fromJson(operationArguments, UpdateOperationDescriptor.class);
		validateArguments(operationDescriptor);

		DBObject query = (DBObject) JSON.parse(operationDescriptor.query.toString());
		DBCursor cursor = collection.find(query);
		if(role != null & authorization) {
			for(DBObject o : cursor) {
				if(!getAuthorizationRulesCache().getIfPresent(role.toString()).
						contains(o.get("project").toString())){
					throw new ProviderException(MongoDBProvider.class.getName() , MongoDBProvider.VERSION, "Not authorized to execute this query.");
				}
			}
		}
		WriteResult writeResult = collection.update(query, DBObject.class.cast(JSON.parse(operationDescriptor.update.toString())),operationDescriptor.upsert,operationDescriptor.multi);
		QueryResult queryResult = new QueryResult();
		queryResult.setData(new ByteArrayInputStream( String.format("{ \"rowsAffected\" : %s ,  \"operation\" : \"update\" , " +
				"\"query\" : %s , \"upsert\": %b , \"multi\": %b }", writeResult.getN() + "" ,
				operationDescriptor.query.toString(), operationDescriptor.upsert, operationDescriptor.multi).getBytes()));
		queryResult.setMimeType(StandardMimeType.JSON.toString());
		return queryResult;
	}
	
	
	private  void validateArguments(UpdateOperationDescriptor operationDescriptor) throws ProviderException
	{
		try {
			check(operationDescriptor!=null ,"Invalid query. Arguments incompatible with [UpdateOperationDescriptor]");
			check(operationDescriptor.query!=null ,"Invalid query. UpdateOperationDescriptor missing parameter [query]");
			check(operationDescriptor.update!=null ,"Invalid update object. UpdateOperationDescriptor missing parameter [update]");
			
		} catch (Exception e) {
			log.error(e);
			throw new ProviderException(MongoDBProvider.class.getName() , MongoDBProvider.VERSION ,e);
		}
		
	}
	
	private static void check(boolean condition , String message) throws Exception
	{
		if(!condition) throw new Exception(message);
	}

	
	
	public static class UpdateOperationDescriptor {
		@Expose public JsonObject query;
		@Expose public boolean upsert;
		@Expose public boolean multi;
		@Expose public JsonObject update;
		
		public JsonObject getQuery() {
			return query;
		}
		public void setQuery(JsonObject query) {
			this.query = query;
		}
		public JsonObject getUpdate() {
			return update;
		}
		public void setUpdate(JsonObject update) {
			this.update = update;
		}

		public void setUpsert(boolean upsert) {
			this.upsert = upsert;
		}

		public boolean isUpsert() {
			return upsert;
		}

		public boolean isMulti() {
			return multi;
		}

		public void setMulti(boolean multi) {
			this.multi = multi;
		}
	}

}
