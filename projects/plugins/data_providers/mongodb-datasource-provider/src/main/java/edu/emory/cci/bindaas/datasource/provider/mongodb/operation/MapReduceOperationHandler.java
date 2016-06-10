package edu.emory.cci.bindaas.datasource.provider.mongodb.operation;

import java.io.ByteArrayInputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MapReduceCommand;
import com.mongodb.MapReduceOutput;
import com.mongodb.util.JSON;

import edu.emory.cci.bindaas.datasource.provider.mongodb.MongoDBProvider;
import edu.emory.cci.bindaas.datasource.provider.mongodb.model.OutputFormatProps;
import edu.emory.cci.bindaas.datasource.provider.mongodb.outputformat.OutputFormatRegistry;
import edu.emory.cci.bindaas.framework.model.ProviderException;
import edu.emory.cci.bindaas.framework.model.QueryResult;
import edu.emory.cci.bindaas.framework.util.GSONUtil;
import edu.emory.cci.bindaas.framework.util.StandardMimeType;

public class MapReduceOperationHandler implements IOperationHandler {
	private Log log = LogFactory.getLog(getClass());
	@Override
	public QueryResult handleOperation(DBCollection collection,
			OutputFormatProps outputFormatProps, JsonObject operationArguments , OutputFormatRegistry registry)
			throws ProviderException {
		MapReduceOperationDescriptor operationDescriptor = GSONUtil.getGSONInstance().fromJson(operationArguments, MapReduceOperationDescriptor.class);
		validateArguments(operationDescriptor);
		
		try{
			
			DBObject query = (DBObject) JSON.parse(operationDescriptor.query.toString());
			String outputCollection = operationDescriptor.outputCollection;
			String reduce = operationDescriptor.reduce;
			String map = operationDescriptor.map;
			MapReduceCommand.OutputType outputType = operationDescriptor.outputType;
			
			MapReduceOutput mapReduceOutput = collection.mapReduce(map, reduce, outputCollection, outputType, query);
			
			
			QueryResult queryResult = new QueryResult();
			queryResult.setMimeType(StandardMimeType.JSON.toString());
			queryResult.setData(new ByteArrayInputStream(mapReduceOutput.toString().getBytes()));
			return queryResult;
			
			
		}catch(Exception e)
		{
			log.error(e);
			throw new ProviderException(MongoDBProvider.class.getName() , MongoDBProvider.VERSION ,e);
		}
		
	}
	
	private  void validateArguments(MapReduceOperationDescriptor operationDescriptor) throws ProviderException
	{
		try {
			check(operationDescriptor!=null ,"Invalid query. Arguments cannot as [MapReduceOperationDescriptor]");
			check(operationDescriptor.query!=null ,"Invalid query. MapReduceOperationDescriptor missing parameter [query]");
			check(operationDescriptor.map!=null ,"Invalid query. MapReduceOperationDescriptor missing parameter [map]");
			check(operationDescriptor.reduce!=null ,"Invalid query. MapReduceOperationDescriptor missing parameter [reduce]");
			check(operationDescriptor.outputCollection!=null ,"Invalid query. MapReduceOperationDescriptor missing parameter [outputCollection]");
		} catch (Exception e) {
			log.error(e);
			throw new ProviderException(MongoDBProvider.class.getName() , MongoDBProvider.VERSION ,e);
		}
		
	}
	
	private static void check(boolean condition , String message) throws Exception
	{
		if(!condition) throw new Exception(message);
	}
	
	public static class MapReduceOperationDescriptor{
		@Expose private JsonObject query;
		@Expose private String map;
		@Expose private String reduce;
		@Expose private String outputCollection;
		@Expose private MapReduceCommand.OutputType outputType = MapReduceCommand.OutputType.REDUCE; // default
		
		public JsonObject getQuery() {
			return query;
		}
		public void setQuery(JsonObject query) {
			this.query = query;
		}
		public String getMap() {
			return map;
		}
		public void setMap(String map) {
			this.map = map;
		}
		public String getReduce() {
			return reduce;
		}
		public void setReduce(String reduce) {
			this.reduce = reduce;
		}
		public String getOutputCollection() {
			return outputCollection;
		}
		public void setOutputCollection(String outputCollection) {
			this.outputCollection = outputCollection;
		}
		public MapReduceCommand.OutputType getOutputType() {
			return outputType;
		}
		public void setOutputType(MapReduceCommand.OutputType outputType) {
			this.outputType = outputType;
		}
		
		
	}

}
