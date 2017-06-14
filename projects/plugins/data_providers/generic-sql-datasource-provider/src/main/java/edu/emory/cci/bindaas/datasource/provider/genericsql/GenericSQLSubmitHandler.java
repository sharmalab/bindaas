package edu.emory.cci.bindaas.datasource.provider.genericsql;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import au.com.bytecode.opencsv.CSVReader;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import edu.emory.cci.bindaas.datasource.provider.genericsql.model.DataSourceConfiguration;
import edu.emory.cci.bindaas.datasource.provider.genericsql.model.SubmitEndpointProperties;
import edu.emory.cci.bindaas.datasource.provider.genericsql.model.SubmitEndpointProperties.InputType;
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

public class GenericSQLSubmitHandler implements ISubmitHandler {
	private AbstractSQLProvider provider;
	private Log log = LogFactory.getLog(getClass());
	private JsonParser jsonParser = new JsonParser();

	public AbstractSQLProvider getProvider() {
		return provider;
	}

	public void setProvider(AbstractSQLProvider provider) {
		this.provider = provider;
	}

	@Override
	public QueryResult submit(JsonObject dataSource,
			JsonObject endpointProperties, InputStream is, RequestContext requestContext)
			throws AbstractHttpCodeException {
		SubmitEndpointProperties seProps = GSONUtil.getGSONInstance().fromJson(
				endpointProperties, SubmitEndpointProperties.class);
		List<Map<String, String>> records = null;

		if (seProps.getInputType() != null
				&& seProps.getInputType().equals(InputType.CSV)) {
			// process CSV file here
			try {
				String data = IOUtils.toString(is);
				records = parseCSV(data);
			} catch (Exception e) {
				log.error("Could not parse the input CSV data ", e);
				throw new SubmitExecutionFailedException(AbstractSQLProvider.class.getName() , AbstractSQLProvider.VERSION , e);
			}

		} else {
			throw new ValidationException(AbstractSQLProvider.class.getName() , AbstractSQLProvider.VERSION , 
					"Invalid InputFormat specified in the endpoint properties");
		}

		QueryResult queryResult = new QueryResult();
		
		queryResult.setMimeType(StandardMimeType.JSON.toString());
		// insert records
		if (records != null) {
			Connection connection = null;
			try {
				DataSourceConfiguration configuration = GSONUtil
						.getGSONInstance().fromJson(dataSource,
								DataSourceConfiguration.class);
				connection = provider.getConnection(configuration);
				connection.setAutoCommit(false);
				int total = insertRecords(records, connection,seProps.getTableName());
				connection.commit();
				queryResult.setData(new ByteArrayInputStream(String.format("{ 'result' : 'success' , 'rowsInserted' : '%s'  }", total).getBytes()));
			} catch (Exception e) {
				log.error(e);
				throw new SubmitExecutionFailedException(AbstractSQLProvider.class.getName() , AbstractSQLProvider.VERSION , e);
			} finally {
				if (connection != null) {
					try {
						connection.close();
						
					} catch (SQLException e) {
						log.error(e);
						throw new SubmitExecutionFailedException(AbstractSQLProvider.class.getName() , AbstractSQLProvider.VERSION , e);
					}
				}
			}
		}
		else
		{
			queryResult.setError(true);
			queryResult.setErrorMessage("No data to submit");
		}
		
		return queryResult;
	}

	@Override
	public QueryResult submit(JsonObject dataSource,
			JsonObject endpointProperties, String data, RequestContext requestContext)
			throws AbstractHttpCodeException {
		SubmitEndpointProperties seProps = GSONUtil.getGSONInstance().fromJson(
				endpointProperties, SubmitEndpointProperties.class);
		List<Map<String, String>> records = null;

		if (seProps.getInputType() != null
				&& seProps.getInputType().equals(InputType.CSV)) {
			// process CSV file here
			try {
				records = parseCSV(data);
			} catch (Exception e) {
				log.error("Could not parse the input CSV data ", e);
				throw new SubmitExecutionFailedException(AbstractSQLProvider.class.getName() , AbstractSQLProvider.VERSION , e);
			}

		} else if (seProps.getInputType() != null
				&& seProps.getInputType().equals(InputType.JSON)) {
			try {
				records = parseJson(data);
			} catch (Exception e) {
				log.error("Could not parse the input JSON data ", e);
				throw new SubmitExecutionFailedException(AbstractSQLProvider.class.getName() , AbstractSQLProvider.VERSION , e);
			}
		} else {
			throw new ValidationException(AbstractSQLProvider.class.getName() , AbstractSQLProvider.VERSION , 
					"Invalid InputFormat specified in the endpoint properties");
		}

		QueryResult queryResult = new QueryResult();
		
		queryResult.setMimeType(StandardMimeType.JSON.toString());
		// insert records
		if (records != null) {
			Connection connection = null;
			try {
				DataSourceConfiguration configuration = GSONUtil
						.getGSONInstance().fromJson(dataSource,
								DataSourceConfiguration.class);
				connection = provider.getConnection(configuration);
				connection.setAutoCommit(false);
				int total = insertRecords(records, connection,seProps.getTableName());
				connection.commit();
				queryResult.setData(new ByteArrayInputStream(String.format("{ 'result' : 'success' , 'rowsInserted' : '%s'  }", total).getBytes()));
			} catch (Exception e) {
				log.error(e);
				throw new SubmitExecutionFailedException(AbstractSQLProvider.class.getName() , AbstractSQLProvider.VERSION , e);
			} finally {
				if (connection != null) {
					try {
						connection.close();
					} catch (SQLException e) {
						log.error(e);
						throw new SubmitExecutionFailedException(AbstractSQLProvider.class.getName() , AbstractSQLProvider.VERSION , e);
					}
				}
			}
		}
		else
		{
			queryResult.setError(true);
			queryResult.setErrorMessage("No data to submit");
		}
		
		return queryResult;
	}

	@Override
	public SubmitEndpoint validateAndInitializeSubmitEndpoint(
			SubmitEndpoint submitEndpoint) throws ProviderException {
		JsonObject props = submitEndpoint.getProperties();
		SubmitEndpointProperties seProps = GSONUtil.getGSONInstance().fromJson(
				props, SubmitEndpointProperties.class);

		if (seProps.getInputType().equals(InputType.JSON)) {
			submitEndpoint.setType(Type.FORM_DATA);
		} else if (seProps.getInputType().equals(InputType.CSV)) {
			submitEndpoint.setType(Type.MULTIPART);
		}

		return submitEndpoint;
	}

	@Override
	public JsonObject getSubmitPropertiesSchema() {
		// TODO later
		return new JsonObject();
	}

	private List<Map<String, String>> parseJson(String data) throws Exception {
		JsonArray array = jsonParser.parse(data).getAsJsonArray();
		List<Map<String,String>> retVal = new ArrayList<Map<String,String>>();
		for(int index = 0;index<array.size(); index++){
			Map<String,String> map = new HashMap<String, String>();
			JsonObject jsonObj = array.get(index).getAsJsonObject();
			for(Object key : jsonObj.entrySet())
			{
				map.put(key.toString(), jsonObj.get(key.toString()).toString());
			}
			retVal.add(map);
		}
		return retVal;
	}

	private List<Map<String, String>> parseCSV(String data) throws Exception {
		@SuppressWarnings("resource")
		CSVReader csvReader = new CSVReader(new StringReader(data));
		List<String[]> listOfVal = csvReader.readAll();
		List<Map<String, String>> retVal = new ArrayList<Map<String,String>>();
		String[] columnNames = listOfVal.get(0);
		for(int index = 1; index < listOfVal.size() ; index ++)
		{
			Map<String,String> record = new HashMap<String, String>();
			int i = 0;
			String[] values = listOfVal.get(index);
			for(String column : columnNames){
				record.put(column, values[i++] );
			}
			
			retVal.add(record);
		}
		
		return retVal;
	}

	private int insertRecords(List<Map<String, String>> records,
			Connection conn , String tableName) throws Exception {
		int total = 0;
		conn.setAutoCommit(false);
		
		for(Map<String,String> record : records)
		{
			
			String columnNames = IOUtils.join(record.keySet(),",");
			String columns[] =	columnNames.split(",");
			StringBuilder preparedStatement = new StringBuilder();
			preparedStatement.append("insert into ").append(tableName).append(" (").append(columnNames).append(" ) values (");
			for(@SuppressWarnings("unused") String column : columns)
			{
				preparedStatement.append("?,");
			}
			
			preparedStatement.replace(preparedStatement.lastIndexOf(","), preparedStatement.length(), ")");
		
			PreparedStatement ps = null;
			try{
				ps = conn.prepareStatement(preparedStatement.toString());
				
				for(int index = 0 ; index < columns.length ; index++)
				{
					ps.setObject(index + 1, record.get(columns[index]));
				}
				
				total += ps.executeUpdate(); // TODO bug. If exception is thrown here , the connection.close() fails leaving the connection open.

			}catch(Exception e)
			{
				log.error(e);
				throw e;
			}
			finally{
				if(ps !=null)
				{
					ps.close();
				}
			}
 			
			
		}
		
		conn.commit();
		
		
		
		
		return total;
	}

}
