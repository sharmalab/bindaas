package edu.emory.cci.bindaas.datasource.provider.mongodb;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;

import edu.emory.cci.bindaas.datasource.provider.mongodb.model.DataSourceConfiguration;
import edu.emory.cci.bindaas.datasource.provider.mongodb.operation.DeleteOperationHandler.DeleteOperationDescriptor;
import edu.emory.cci.bindaas.datasource.provider.mongodb.operation.IOperationHandler;
import edu.emory.cci.bindaas.datasource.provider.mongodb.operation.MongoDBModifyOperationDescriptor;
import edu.emory.cci.bindaas.datasource.provider.mongodb.operation.MongoDBModifyOperationType;
import edu.emory.cci.bindaas.framework.api.IDeleteHandler;
import edu.emory.cci.bindaas.framework.model.QueryResult;
import edu.emory.cci.bindaas.framework.model.RequestContext;
import edu.emory.cci.bindaas.framework.provider.exception.AbstractHttpCodeException;
import edu.emory.cci.bindaas.framework.provider.exception.DeleteExecutionFailedException;
import edu.emory.cci.bindaas.framework.provider.exception.QueryExecutionFailedException;
import edu.emory.cci.bindaas.framework.util.GSONUtil;

public class MongoDBDeleteHandler implements IDeleteHandler {

	private Log log = LogFactory.getLog(getClass());
	private JsonParser parser = new JsonParser();

	@Override
	public QueryResult delete(JsonObject dataSource, String deleteQueryToExecute , Map<String,String> runtimeParamters , RequestContext requestContext)
			throws AbstractHttpCodeException {
		
		try{
			MongoDBModifyOperationDescriptor operationDescriptor = null;
			try{
				 operationDescriptor = GSONUtil.getGSONInstance().fromJson(deleteQueryToExecute,MongoDBModifyOperationDescriptor.class);
				 if(operationDescriptor == null || operationDescriptor.get_operation() == null || operationDescriptor.get_operation_args()==null)
				 {
					 throw new Exception("Not a valid query object"); // the query is not annotated properly
				 }
			}
			catch(Exception e)
			{
				log.trace(e.getMessage());
				// default to find query
				operationDescriptor = new MongoDBModifyOperationDescriptor();
				operationDescriptor.set_operation(MongoDBModifyOperationType.delete);
				DeleteOperationDescriptor delArguments = new DeleteOperationDescriptor();
				delArguments.setQuery(parser.parse(deleteQueryToExecute).getAsJsonObject());
				operationDescriptor.set_operation_args(GSONUtil.getGSONInstance().toJsonTree(delArguments).getAsJsonObject());
			}
			
			// get DB collection
			DataSourceConfiguration configuration = GSONUtil.getGSONInstance().fromJson(dataSource, DataSourceConfiguration.class);
			Mongo mongo = null;
			try {
				mongo = new Mongo(configuration.getHost(),configuration.getPort());
				DB db = mongo.getDB(configuration.getDb());
				DBCollection collection = db.getCollection(configuration.getCollection());

				// use operationDescriptor to route to correct handler
				
				IOperationHandler operationHandler = operationDescriptor.get_operation().getHandler();
				QueryResult result = operationHandler.handleOperation(collection, null , operationDescriptor.get_operation_args(), null);
				return result;
				
			} catch (Exception e) {
				log.error(e);
				throw new DeleteExecutionFailedException(MongoDBProvider.class.getName(), MongoDBProvider.VERSION, e);
			}
			finally{
				if(mongo!=null)
				{
					mongo.close();
				}
			}

		}
	catch(AbstractHttpCodeException e)
	{
		log.error(e);
		throw e;
	}
	catch(Exception e)
	{
		log.error(e);
		throw new QueryExecutionFailedException(MongoDBProvider.class.getName() , MongoDBProvider.VERSION ,e);
	}

	}

}
