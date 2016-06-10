package edu.emory.cci.bindaas.datasource.provider.aime;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.JsonObject;

import edu.emory.cci.bindaas.framework.api.IQueryHandler;
import edu.emory.cci.bindaas.framework.model.ProviderException;
import edu.emory.cci.bindaas.framework.model.QueryEndpoint;
import edu.emory.cci.bindaas.framework.model.QueryResult;
import edu.emory.cci.bindaas.framework.model.RequestContext;
import edu.emory.cci.bindaas.framework.provider.exception.AbstractHttpCodeException;
import edu.emory.cci.bindaas.framework.provider.exception.QueryExecutionFailedException;
import edu.emory.cci.bindaas.framework.util.GSONUtil;
import edu.emory.cci.bindaas.datasource.provider.aime.model.DataSourceConfiguration;
import edu.emory.cci.bindaas.datasource.provider.aime.model.OutputFormatProps;
import edu.emory.cci.bindaas.datasource.provider.aime.model.OutputFormatProps.OutputFormat;
import edu.emory.cci.bindaas.datasource.provider.aime.model.OutputFormatProps.QueryType;
import edu.emory.cci.bindaas.datasource.provider.aime.outputformat.IFormatHandler;
import edu.emory.cci.bindaas.datasource.provider.aime.outputformat.OutputFormatRegistry;

public class AIMEQueryHandler implements IQueryHandler {

	private OutputFormatRegistry outputFormatRegistry;
	private Log log = LogFactory.getLog(getClass());

	public OutputFormatRegistry getOutputFormatRegistry() {
		return outputFormatRegistry;
	}

	public void setOutputFormatRegistry(
			OutputFormatRegistry outputFormatRegistry) {
		this.outputFormatRegistry = outputFormatRegistry;
	}

	@Override
	public QueryResult query(JsonObject dataSource,
			JsonObject outputFormatProps, String queryToExecute , Map<String,String> runtimeParameters , RequestContext requestContext)
			throws AbstractHttpCodeException {

		try {
			OutputFormatProps props = GSONUtil.getGSONInstance().fromJson(
					outputFormatProps, OutputFormatProps.class); // Get
																	// outputFormat
																	// props
			QueryType queryType = props.getQueryType();
			OutputFormat outputFormat = props.getOutputFormat();

			// override output format
			if(outputFormat.equals(OutputFormat.ANY))
			{
				if(runtimeParameters.containsKey("format") && runtimeParameters.get("format")!=null )
				{
					try{
						
						outputFormat = OutputFormat.valueOf(runtimeParameters.get("format").toUpperCase());
					     
					}catch(IllegalArgumentException ei)
					{
						outputFormat = OutputFormat.XML; // default to XML
					}
				}
				else
				{
					outputFormat = OutputFormat.XML; // default to XML
				}
			}
			
			IFormatHandler formatHandler = outputFormatRegistry.getHandler(
					queryType, outputFormat);
			if (formatHandler != null) {
				Connection connection = null;
				try {
					DataSourceConfiguration configuration = GSONUtil
							.getGSONInstance().fromJson(dataSource,
									DataSourceConfiguration.class);
					connection = AIMEProvider.getConnection(configuration);
					Statement statement = connection.createStatement();
					ResultSet resultSet = statement
							.executeQuery(queryToExecute);
					QueryResult queryResult = formatHandler.format(props,
							resultSet);
					return queryResult;
				} catch (Exception er) {
					log.error(er);
					throw er;
				} finally {
					if (connection != null)
						connection.close();
				}
			} else {
				throw new Exception("No Handler for QueryType=[" + queryType
						+ "] and OutputFormat=[" + outputFormat + "]");
			}

		} catch (Exception e) {
			log.error(e);
			throw new QueryExecutionFailedException(AIMEProvider.class.getName(),AIMEProvider.VERSION,"Query Not Executed", e);
		}

	}

	@Override
	public QueryEndpoint validateAndInitializeQueryEndpoint(
			QueryEndpoint queryEndpoint) throws ProviderException {
		try {
			OutputFormatProps props = GSONUtil.getGSONInstance().fromJson(
					queryEndpoint.getOutputFormat(), OutputFormatProps.class); // Get
																				// outputFormat
																				// props
			QueryType queryType = props.getQueryType();
			OutputFormat outputFormat = props.getOutputFormat();

			IFormatHandler formatHandler = outputFormatRegistry.getHandler(
					queryType, outputFormat);
			if (formatHandler != null && outputFormat.equals(OutputFormat.ANY)!= true) {
				formatHandler.validate(props);
			}
			else if(outputFormat.equals(OutputFormat.ANY) == true)
			{
				return queryEndpoint;
			}
			else {
				throw new Exception("No Handler for QueryType=[" + queryType
						+ "] and OutputFormat=[" + outputFormat + "]");
			}
		} catch (Exception e) {
			log.error(e);
			throw new ProviderException(AIMEProvider.class.getName(),AIMEProvider.VERSION,
					"Validation of Input Parameters failed", e);
		}

		return queryEndpoint;
	}

	@Override
	public JsonObject getOutputFormatSchema() {
		return new JsonObject();
	}

}
