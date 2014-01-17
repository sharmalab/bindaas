package edu.emory.cci.sample.mysql.core;

import java.io.ByteArrayInputStream;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.JsonObject;

import edu.emory.cci.bindaas.framework.api.IDeleteHandler;
import edu.emory.cci.bindaas.framework.model.QueryResult;
import edu.emory.cci.bindaas.framework.model.RequestContext;
import edu.emory.cci.bindaas.framework.provider.exception.AbstractHttpCodeException;
import edu.emory.cci.bindaas.framework.provider.exception.DeleteExecutionFailedException;
import edu.emory.cci.bindaas.framework.util.GSONUtil;
import edu.emory.cci.bindaas.framework.util.StandardMimeType;
import edu.emory.cci.sample.mysql.model.DataSourceConfiguration;

public class DeleteHandler implements IDeleteHandler {
	private Log log = LogFactory.getLog(getClass());
	
	
	
	@Override
	public QueryResult delete(JsonObject dataSource, String deleteQueryToExecute, Map<String,String> runtimeParamters , RequestContext requestContext)
			throws AbstractHttpCodeException {
		try {
			Connection connection = null;
			try {
				DataSourceConfiguration configuration = GSONUtil
						.getGSONInstance().fromJson(dataSource,
								DataSourceConfiguration.class);
				connection = SimpleMySQLDataProvider.getConnection(configuration);
				Statement statement = connection.createStatement();
				int status = statement.executeUpdate(deleteQueryToExecute);
				QueryResult queryResult = new QueryResult();
				queryResult.setMimeType(StandardMimeType.JSON.toString());
				queryResult.setData( new ByteArrayInputStream(String.format(
						"{ 'result' : 'success' , 'rowsDeleted' : '%s' }",
						status + "").getBytes()));
				return queryResult;
			} catch (Exception er) {
				log.error(er);
				throw er;
			} finally {
				if (connection != null)
					connection.close();
			}

		} catch (Exception e) {
			log.error(e);
			throw new DeleteExecutionFailedException(SimpleMySQLDataProvider.class.getName() , 1 , "Delete operation failed", e);
		}
		
	}

}
