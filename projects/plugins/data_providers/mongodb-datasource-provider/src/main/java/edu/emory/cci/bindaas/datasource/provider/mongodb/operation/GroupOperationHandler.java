package edu.emory.cci.bindaas.datasource.provider.mongodb.operation;

import java.io.ByteArrayInputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;

import edu.emory.cci.bindaas.datasource.provider.mongodb.MongoDBProvider;
import edu.emory.cci.bindaas.datasource.provider.mongodb.model.OutputFormatProps;
import edu.emory.cci.bindaas.datasource.provider.mongodb.outputformat.OutputFormatRegistry;
import edu.emory.cci.bindaas.framework.model.ProviderException;
import edu.emory.cci.bindaas.framework.model.QueryResult;
import edu.emory.cci.bindaas.framework.util.GSONUtil;
import edu.emory.cci.bindaas.framework.util.StandardMimeType;

public class GroupOperationHandler implements IOperationHandler {
	private Log log = LogFactory.getLog(getClass());
	@Override
	public QueryResult handleOperation(DBCollection collection,
			OutputFormatProps outputFormatProps, JsonObject operationArguments , OutputFormatRegistry registry)
			throws ProviderException {
		GroupOperationDescriptor operationDescriptor = GSONUtil.getGSONInstance().fromJson(operationArguments, GroupOperationDescriptor.class);
		validateArguments(operationDescriptor);
		
		try{
			
			DBObject key = (DBObject) JSON.parse(operationDescriptor.key.toString());
			DBObject condition = (DBObject) JSON.parse(operationDescriptor.condition.toString());
			DBObject initial = (DBObject) JSON.parse(operationDescriptor.initial.toString());
			String reduce = operationDescriptor.reduce;
			String finalize = operationDescriptor.finalize;
			
			DBObject result = collection.group(key, condition, initial, reduce, finalize);
			
			QueryResult queryResult = new QueryResult();
			queryResult.setMimeType(StandardMimeType.JSON.toString());
			queryResult.setData(new ByteArrayInputStream(result.toString().getBytes()));
			return queryResult;
			
			
		}catch(Exception e)
		{
			log.error(e);
			throw new ProviderException(MongoDBProvider.class.getName() , MongoDBProvider.VERSION ,e);
		}
		
	}
	
	private  void validateArguments(GroupOperationDescriptor operationDescriptor) throws ProviderException
	{
		try {
			check(operationDescriptor!=null ,"Invalid query. Arguments cannot as [GroupOperationDescriptor]");
			check(operationDescriptor.key!=null ,"Invalid query. GroupOperationDescriptor missing parameter [key]");
			check(operationDescriptor.condition!=null ,"Invalid query. GroupOperationDescriptor missing parameter [condition]");
			check(operationDescriptor.initial!=null ,"Invalid query. GroupOperationDescriptor missing parameter [initial]");
			check(operationDescriptor.reduce!=null ,"Invalid query. GroupOperationDescriptor missing parameter [reduce]");
			check(operationDescriptor.finalize!=null ,"Invalid query. GroupOperationDescriptor missing parameter [finalize]");
		} catch (Exception e) {
			log.error(e);
			throw new ProviderException(MongoDBProvider.class.getName() , MongoDBProvider.VERSION ,e);
		}
		
	}
	
	private static void check(boolean condition , String message) throws Exception
	{
		if(!condition) throw new Exception(message);
	}
	
	public static class GroupOperationDescriptor{
		@Expose private JsonObject key;
		@Expose private JsonObject condition;
		@Expose private JsonObject initial;
		@Expose private String reduce;
		@Expose private String finalize;
		public JsonObject getKey() {
			return key;
		}
		public void setKey(JsonObject key) {
			this.key = key;
		}
		public JsonObject getCondition() {
			return condition;
		}
		public void setCondition(JsonObject condition) {
			this.condition = condition;
		}
		public JsonObject getInitial() {
			return initial;
		}
		public void setInitial(JsonObject initial) {
			this.initial = initial;
		}
		public String getReduce() {
			return reduce;
		}
		public void setReduce(String reduce) {
			this.reduce = reduce;
		}
		public String getFinalize() {
			return finalize;
		}
		public void setFinalize(String finalize) {
			this.finalize = finalize;
		}
		
		
	}

}
