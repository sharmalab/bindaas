package edu.emory.cci.bindaas.datasource.provider.mongodb.operation;


import java.io.ByteArrayInputStream;
import java.util.List;

import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import com.mongodb.DBCollection;


import edu.emory.cci.bindaas.datasource.provider.mongodb.MongoDBProvider;
import edu.emory.cci.bindaas.datasource.provider.mongodb.model.OutputFormatProps;
import edu.emory.cci.bindaas.datasource.provider.mongodb.outputformat.OutputFormatRegistry;
import edu.emory.cci.bindaas.framework.model.ProviderException;
import edu.emory.cci.bindaas.framework.model.QueryResult;
import edu.emory.cci.bindaas.framework.util.GSONUtil;
import edu.emory.cci.bindaas.framework.util.StandardMimeType;


public class DistinctOperationHandler implements IOperationHandler {
	private Log log = LogFactory.getLog(getClass());


	@Override
	public QueryResult handleOperation(DBCollection collection,
									   OutputFormatProps outputFormatProps, JsonObject operationArguments , OutputFormatRegistry registry, String role, boolean authorization)
			throws ProviderException {
		DistinctOperationDescriptor operationDescriptor = GSONUtil.getGSONInstance().fromJson(operationArguments, DistinctOperationDescriptor.class);
		validateArguments(operationDescriptor);

		try{
			DBObject query = (DBObject) JSON.parse(operationDescriptor.query.toString());

			List distinct;
			if (query == null) {
				distinct = collection.distinct(operationDescriptor.field);
			} else {
				distinct = collection.distinct(operationDescriptor.field, query);
			}


			QueryResult queryResult = new QueryResult();
			queryResult.setMimeType(StandardMimeType.JSON.toString());
			queryResult.setData(new ByteArrayInputStream(distinct.toString().getBytes()));
			return queryResult;

		}
		catch(Exception e)
		{
			log.error(e);
			throw new ProviderException(MongoDBProvider.class.getName() , MongoDBProvider.VERSION ,e);
		}
	}

	private  void validateArguments(DistinctOperationDescriptor operationDescriptor) throws ProviderException
	{
		try {
			check(operationDescriptor!=null ,"Invalid query. Arguments cannot as [DistinctOperationDescriptor]");
			check(operationDescriptor.query!=null ,"Invalid query. DistinctOperationDescriptor missing parameter [query]");
		} catch (Exception e) {
			log.error(e);
			throw new ProviderException(MongoDBProvider.class.getName() , MongoDBProvider.VERSION ,e);
		}

	}

	private static void check(boolean condition , String message) throws Exception
	{
		if(!condition) throw new Exception(message);
	}

	public static class DistinctOperationDescriptor {
		@Expose public String field;
		@Expose public JsonObject query;


		public JsonObject getQuery() {
			return query;
		}
		public void setQuery(JsonObject query) {
			this.query = query;
		}

		public String getField() {
			return field;
		}

		public void setField(String field) {
			this.field = field;
		}
	}


}
