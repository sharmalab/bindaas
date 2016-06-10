package edu.emory.cci.bindaas.datasource.provider.mongodb.operation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;

import edu.emory.cci.bindaas.datasource.provider.mongodb.MongoDBProvider;
import edu.emory.cci.bindaas.datasource.provider.mongodb.model.OutputFormat;
import edu.emory.cci.bindaas.datasource.provider.mongodb.model.OutputFormatProps;
import edu.emory.cci.bindaas.datasource.provider.mongodb.outputformat.IFormatHandler;
import edu.emory.cci.bindaas.datasource.provider.mongodb.outputformat.IFormatHandler.OnFinishHandler;
import edu.emory.cci.bindaas.datasource.provider.mongodb.outputformat.OutputFormatRegistry;
import edu.emory.cci.bindaas.framework.model.ProviderException;
import edu.emory.cci.bindaas.framework.model.QueryResult;
import edu.emory.cci.bindaas.framework.util.GSONUtil;

public class FindOperationHandler implements IOperationHandler {
	
	private Log log = LogFactory.getLog(getClass());
	@Override
	public QueryResult handleOperation(DBCollection collection,
			OutputFormatProps outputFormatProps, JsonObject operationArguments , OutputFormatRegistry registry )
			throws ProviderException {
	
		FindOperationDescriptor operationDescriptor = GSONUtil.getGSONInstance().fromJson(operationArguments, FindOperationDescriptor.class);
		validateArguments(operationDescriptor);
		
		OutputFormat of = outputFormatProps.getOutputFormat();
		IFormatHandler formatHandler = registry.getHandler(of);
		if(formatHandler!=null)
		{
			try{
				DBObject query = (DBObject) JSON.parse(operationDescriptor.query.toString());
				DBObject fields = operationDescriptor.fields == null ? null : (DBObject) JSON.parse(operationDescriptor.fields.toString());
				DBCursor cursor = null;
				if(fields!=null)
				{
					cursor = collection.find(query , fields);
				}
				else
				{
					cursor = collection.find(query);
				}
				
				if(operationDescriptor.skip !=null)
				{
					cursor = cursor.skip(operationDescriptor.skip);
				}
				
				if(operationDescriptor.limit !=null)
				{
					cursor = cursor.limit(operationDescriptor.limit);
				}
				
				OnFinishHandler finishHandler = new OnFinishHandler() {
					private boolean finished = false;
					@Override
					public boolean isFinished() {

						return finished;
					}
					
					@Override
					public void finish() throws Exception {
						finished = true;
					}
				};
				
				QueryResult queryResult = formatHandler.format(outputFormatProps, cursor , finishHandler);
				return queryResult;
				
			}
			catch(Exception e)
			{
				log.error(e);
				throw new ProviderException(MongoDBProvider.class.getName() , MongoDBProvider.VERSION ,e);
			}
		}
		else
		{
			throw new ProviderException(MongoDBProvider.class.getName() , MongoDBProvider.VERSION ,"No handler found for outputType=[" + of + "]");
		}
		
	}
	
	
	private  void validateArguments(FindOperationDescriptor operationDescriptor) throws ProviderException
	{
		try {
			check(operationDescriptor!=null ,"Invalid query. Arguments cannot as [FindOperationDescriptor]");
			check(operationDescriptor.query!=null ,"Invalid query. FindOperationDescriptor missing parameter [query]");
		} catch (Exception e) {
			log.error(e);
			throw new ProviderException(MongoDBProvider.class.getName() , MongoDBProvider.VERSION ,e);
		}
		
	}
	
	private static void check(boolean condition , String message) throws Exception
	{
		if(!condition) throw new Exception(message);
	}
	
	public static class FindOperationDescriptor {

		@Expose public JsonObject query;
		@Expose public JsonObject fields;
		@Expose public Integer skip;
		@Expose public Integer limit;
		
		
		public Integer getSkip() {
			return skip;
		}
		public void setSkip(Integer skip) {
			this.skip = skip;
		}
		public Integer getLimit() {
			return limit;
		}
		public void setLimit(Integer limit) {
			this.limit = limit;
		}
		public JsonObject getQuery() {
			return query;
		}
		public void setQuery(JsonObject query) {
			this.query = query;
		}
		public JsonObject getFields() {
			return fields;
		}
		public void setFields(JsonObject fields) {
			this.fields = fields;
		}
		
		
	}


}
