package edu.emory.cci.bindaas.datasource.provider.mongodb;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import au.com.bytecode.opencsv.CSVReader;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.util.JSON;

import edu.emory.cci.bindaas.datasource.provider.mongodb.model.DataSourceConfiguration;
import edu.emory.cci.bindaas.datasource.provider.mongodb.model.SubmitEndpointProperties;
import edu.emory.cci.bindaas.datasource.provider.mongodb.model.SubmitEndpointProperties.InputType;
import edu.emory.cci.bindaas.framework.api.ISubmitHandler;
import edu.emory.cci.bindaas.framework.model.ProviderException;
import edu.emory.cci.bindaas.framework.model.QueryResult;
import edu.emory.cci.bindaas.framework.model.RequestContext;
import edu.emory.cci.bindaas.framework.model.SubmitEndpoint;
import edu.emory.cci.bindaas.framework.model.SubmitEndpoint.Type;
import edu.emory.cci.bindaas.framework.provider.exception.AbstractHttpCodeException;
import edu.emory.cci.bindaas.framework.provider.exception.SubmitExecutionFailedException;
import edu.emory.cci.bindaas.framework.provider.exception.ValidationException;
import edu.emory.cci.bindaas.framework.util.GSONUtil;
import edu.emory.cci.bindaas.framework.util.IOUtils;
import edu.emory.cci.bindaas.framework.util.StandardMimeType;

public class MongoDBSubmitHandler implements ISubmitHandler {

	private Log log = LogFactory.getLog(getClass());

	@Override
	public QueryResult submit(JsonObject dataSource,
			JsonObject endpointProperties, InputStream is, RequestContext requestContext)
			throws AbstractHttpCodeException {
		
		try {
			String data = IOUtils.toString(is);
			QueryResult queryResult = submit(dataSource, endpointProperties, data , requestContext);
			return queryResult;
		} catch (IOException e) {
			log.error(e);
			throw new SubmitExecutionFailedException(MongoDBProvider.class.getName() , MongoDBProvider.VERSION ,e);
		}

	}

	@Override
	public QueryResult submit(JsonObject dataSource,
			JsonObject endpointProperties, String data, RequestContext requestContext)
			throws AbstractHttpCodeException {
		Mongo mongo = null;
		try {
			DataSourceConfiguration configuration = GSONUtil.getGSONInstance()
					.fromJson(dataSource, DataSourceConfiguration.class);
			SubmitEndpointProperties submitEndpointProperties = GSONUtil
					.getGSONInstance().fromJson(endpointProperties,
							SubmitEndpointProperties.class);

			if (submitEndpointProperties.getInputType()!=null && submitEndpointProperties.getInputType().toString().startsWith("JSON")) {
				
				mongo = new Mongo(configuration.getHost(),configuration.getPort());
				DB db = mongo.getDB(configuration.getDb());
				DBCollection collection = db.getCollection(configuration.getCollection());
				
				DBObject object = (DBObject) JSON.parse(data);
				collection.insert(object);
				
				QueryResult queryResult = new QueryResult();
				
				queryResult.setData(new ByteArrayInputStream("{ 'count':'1'}".getBytes()));
				queryResult.setMimeType(StandardMimeType.JSON.toString());
				return queryResult;
			}
			else if(submitEndpointProperties.getInputType()!=null && submitEndpointProperties.getInputType().toString().startsWith("CSV"))
			{
				mongo = new Mongo(configuration.getHost(),configuration.getPort());
				DB db = mongo.getDB(configuration.getDb());
				DBCollection collection = db.getCollection(configuration.getCollection());
				DBObject[] object = toJSON(data , submitEndpointProperties.getCsvHeader());
				collection.insert(object);
				QueryResult queryResult = new QueryResult();
				StringBuffer buffer = new StringBuffer();
				buffer.append("{ 'count':'" + object.length + "'}");
				queryResult.setData(new ByteArrayInputStream(buffer.toString().getBytes()));
				queryResult.setMimeType(StandardMimeType.JSON.toString());
				return queryResult;
			}
			else
				throw new ValidationException(getClass().getName() , MongoDBProvider.VERSION ,"Unsupported Input Type");
		} 
		catch(AbstractHttpCodeException e)
		{
			log.error(e);
			throw e;
		}
		catch (Exception e) {
			log.error(e);
			throw new SubmitExecutionFailedException(MongoDBProvider.class.getName() , MongoDBProvider.VERSION ,e);
		}
		finally{
			if(mongo!=null)
				mongo.close();
		}

		
	}
	
	public DBObject[] toJSON(String csvData , String[] csvHeader) throws IOException
	{
		List<DBObject> listOfDBObjects = new ArrayList<DBObject>();
		CSVReader csvReader = new CSVReader(new StringReader(csvData));
		List<String[]> rowsRead = csvReader.readAll();
		if(csvHeader == null)
		{
			csvHeader = rowsRead.remove(0);
		}
		
		for(String[] values : rowsRead)
		{
			JsonObject jsonRow = new JsonObject();
			for(int i = 0;i<values.length;i++)
			{
				jsonRow.add(csvHeader[i], new JsonPrimitive(values[i]));
			}
			DBObject dbObject = (DBObject) JSON.parse(jsonRow.toString());
			listOfDBObjects.add(dbObject);
			
		}
		
		
		csvReader.close();
		
		return listOfDBObjects.toArray(new DBObject[listOfDBObjects.size()]);
	}

	@Override
	public SubmitEndpoint validateAndInitializeSubmitEndpoint(
			SubmitEndpoint submitEndpoint) throws ProviderException {
		JsonObject endpointProperties = submitEndpoint.getProperties();
		try{
			if(endpointProperties!=null)
			{
				SubmitEndpointProperties submitEndpointProperties = GSONUtil
						.getGSONInstance().fromJson(endpointProperties,
								SubmitEndpointProperties.class);
				if( submitEndpointProperties==null || submitEndpointProperties.getInputType()==null)
				{
					throw new Exception("Submit Endpoint properties could not be parsed");
				}
				else
				{
					InputType inputType = submitEndpointProperties.getInputType();
					switch (inputType) {
					case JSON:
					case CSV:
						submitEndpoint.setType(Type.FORM_DATA);
						break;
					case JSON_FILE:
					case CSV_FILE:
						submitEndpoint.setType(Type.MULTIPART);
						break;
					default:
						throw new Exception("Invalid InputType specified. Must one of the following [CSV,JSON]");
					}
					
					return submitEndpoint;
				}
					
			}
			else throw new Exception("Submit Endpoint properties not specified");
			
		}
		catch(Exception e)
		{
			log.error(e);
			throw new ProviderException(MongoDBProvider.class.getName() , MongoDBProvider.VERSION ,e);
		}
		
	}

	@Override
	public JsonObject getSubmitPropertiesSchema() {
		// TODO later
		return new JsonObject();
	}

}
