package edu.emory.cci.bindaas.datasource.provider.mongodb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bson.Document;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Indexes;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;

import edu.emory.cci.bindaas.core.api.BindaasConstants;
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

import static edu.emory.cci.bindaas.datasource.provider.mongodb.MongoDBProvider.addAuthRule;

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
				// default to 'delete' query
				operationDescriptor = new MongoDBModifyOperationDescriptor();
				operationDescriptor.set_operation(MongoDBModifyOperationType.delete);
				DeleteOperationDescriptor delArguments = new DeleteOperationDescriptor();
				delArguments.setQuery(parser.parse(deleteQueryToExecute).getAsJsonObject());
				operationDescriptor.set_operation_args(GSONUtil.getGSONInstance().toJsonTree(delArguments).getAsJsonObject());
			}
			
			// get DB collection
			DataSourceConfiguration configuration = GSONUtil.getGSONInstance().fromJson(dataSource, DataSourceConfiguration.class);
			MongoClient mongo = null;
			try {
				if(configuration.getUsername() == null && configuration.getPassword() == null){
					mongo = new MongoClient(new ServerAddress(configuration.getHost(),configuration.getPort()));
				}
				else if(configuration.getUsername().isEmpty() && configuration.getPassword().isEmpty()){
					mongo = new MongoClient(new ServerAddress(configuration.getHost(),configuration.getPort()));
				}
				else{
					MongoCredential credential = MongoCredential.createCredential(
							configuration.getUsername(),
							configuration.getAuthenticationDb(),
							configuration.getPassword().toCharArray()
					);
					mongo = new MongoClient(new ServerAddress(configuration.getHost(),configuration.getPort()), Arrays.asList(credential));
				}
				DB db = mongo.getDB(configuration.getDb());
				DBCollection collection = db.getCollection(configuration.getCollection());


				Object role = requestContext.getAttributes().get(BindaasConstants.ROLE);
				Boolean authorization = configuration.getAuthorizationCollection() != null && !configuration.getAuthorizationCollection().isEmpty();

				if( role != null && authorization) {
					try {
						// first check in cache
						MongoDatabase database = mongo.getDatabase(configuration.getDb());
						MongoCollection<Document> authCollection = database.getCollection(configuration.getAuthorizationCollection());
						authCollection.dropIndexes();
						authCollection.createIndex(Indexes.text("roles"));
						FindIterable<Document> docs = authCollection.find(Filters.text(role.toString()));
						List<String> projectsList = new ArrayList<String>();
						for (Document doc : docs) {
							projectsList.add(doc.getString("projectName"));
						}
						addAuthRule(role.toString(),projectsList);

					} catch (Exception e) {
						log.error(e);
						throw e;
					}
				}

				// use operationDescriptor to route to correct handler
				
				IOperationHandler operationHandler = operationDescriptor.get_operation().getHandler();
				QueryResult result = operationHandler.handleOperation(collection, null , operationDescriptor.get_operation_args(), null, role.toString(), authorization);
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
