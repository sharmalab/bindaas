package edu.emory.cci.bindaas.datasource.provider.genericsql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.JsonObject;

import edu.emory.cci.bindaas.datasource.provider.genericsql.bundle.Activator;
import edu.emory.cci.bindaas.datasource.provider.genericsql.model.DataSourceConfiguration;
import edu.emory.cci.bindaas.datasource.provider.genericsql.model.OutputFormat;
import edu.emory.cci.bindaas.datasource.provider.genericsql.model.OutputFormatProps;
import edu.emory.cci.bindaas.datasource.provider.genericsql.outputformat.IFormatHandler;
import edu.emory.cci.bindaas.datasource.provider.genericsql.outputformat.IFormatHandler.OnFinishHandler;
import edu.emory.cci.bindaas.datasource.provider.genericsql.outputformat.OutputFormatRegistry;
import edu.emory.cci.bindaas.framework.api.IQueryHandler;
import edu.emory.cci.bindaas.framework.model.ProviderException;
import edu.emory.cci.bindaas.framework.model.QueryEndpoint;
import edu.emory.cci.bindaas.framework.model.QueryResult;
import edu.emory.cci.bindaas.framework.model.RequestContext;
import edu.emory.cci.bindaas.framework.provider.exception.AbstractHttpCodeException;
import edu.emory.cci.bindaas.framework.provider.exception.QueryExecutionFailedException;
import edu.emory.cci.bindaas.framework.util.GSONUtil;

public class GenericSQLQueryHandler implements IQueryHandler {
	private AbstractSQLProvider provider;
	
	private Log log = LogFactory.getLog(getClass());
	
	public OutputFormatRegistry getOutputFormatRegistry() {
		return Activator.getService(OutputFormatRegistry.class);
	}
	
	public AbstractSQLProvider getProvider() {
		return provider;
	}
	public void setProvider(AbstractSQLProvider provider) {
		this.provider = provider;
	}
	
	@Override
	public QueryResult query(JsonObject dataSource,
			JsonObject outputFormatProps, String queryToExecute, Map<String,String> runtimeParameters, RequestContext requestContext)
			throws AbstractHttpCodeException {
		try {
			OutputFormatProps props = GSONUtil.getGSONInstance().fromJson(
					outputFormatProps, OutputFormatProps.class); // Get
																	// outputFormat
																	// props
			
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
									outputFormat = OutputFormat.JSON; // default to JSON
								}
							}
							else
							{
								outputFormat = OutputFormat.JSON; // default to JSON
							}
						}
			IFormatHandler formatHandler = getOutputFormatRegistry().getHandler(outputFormat);
			if (formatHandler != null) {
				
				try {
					DataSourceConfiguration configuration = GSONUtil
							.getGSONInstance().fromJson(dataSource,
									DataSourceConfiguration.class);
					final Connection connection = provider.getConnection(configuration);
					Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY,
                            ResultSet.CONCUR_READ_ONLY);
					ResultSet resultSet = statement
							.executeQuery(queryToExecute);
					
					OnFinishHandler finishHandler = new OnFinishHandler() {
						private boolean finished = false;
						@Override
						public synchronized void finish() throws Exception {
							if(!finished)
								connection.close();
							this.finished = true;
						}

						@Override
						public boolean isFinished() {

							return this.finished;
						}
					};
					
					QueryResult queryResult = formatHandler.format(props,
							resultSet , finishHandler);
					
					return queryResult;
				} catch (Exception er) {
					log.error(er);
					throw er;
				}
			} else {
				throw new Exception("No Handler for OutputFormat=[" + outputFormat + "]");
			}

		} catch (Exception e) {
			log.error(e);
			throw new QueryExecutionFailedException(AbstractSQLProvider.class.getName() , AbstractSQLProvider.VERSION , "Query Not Executed", e);
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
			
			OutputFormat outputFormat = props.getOutputFormat();

			IFormatHandler formatHandler = getOutputFormatRegistry().getHandler(outputFormat);
			if (formatHandler != null && outputFormat.equals(OutputFormat.ANY)!= true) {
				formatHandler.validate(props);
			} 
			else if(outputFormat.equals(OutputFormat.ANY) == true)
			{
				return queryEndpoint;
			}
			else {
				throw new Exception("No Handler for OutputFormat=[" + outputFormat + "]");
			}
		} catch (Exception e) {
			log.error(e);
			throw new ProviderException(AbstractSQLProvider.class.getName() , AbstractSQLProvider.VERSION , 
					"Validation of Input Parameters failed", e);
		}

		return queryEndpoint;
	}

	
	@Override
	public JsonObject getOutputFormatSchema() {
		return new JsonObject();
	}

}
