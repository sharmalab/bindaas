package edu.emory.cci.bindaas.datasource.provider.mongodb;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;

import edu.emory.cci.bindaas.datasource.provider.mongodb.model.DataSourceConfiguration;
import edu.emory.cci.bindaas.datasource.provider.mongodb.model.OutputFormat;
import edu.emory.cci.bindaas.datasource.provider.mongodb.model.OutputFormatProps;
import edu.emory.cci.bindaas.datasource.provider.mongodb.operation.FindOperationHandler.FindOperationDescriptor;
import edu.emory.cci.bindaas.datasource.provider.mongodb.operation.IOperationHandler;
import edu.emory.cci.bindaas.datasource.provider.mongodb.operation.MongoDBQueryOperationDescriptor;
import edu.emory.cci.bindaas.datasource.provider.mongodb.operation.MongoDBQueryOperationType;
import edu.emory.cci.bindaas.datasource.provider.mongodb.outputformat.IFormatHandler;
import edu.emory.cci.bindaas.datasource.provider.mongodb.outputformat.OutputFormatRegistry;
import edu.emory.cci.bindaas.framework.api.IQueryHandler;
import edu.emory.cci.bindaas.framework.model.ProviderException;
import edu.emory.cci.bindaas.framework.model.QueryEndpoint;
import edu.emory.cci.bindaas.framework.model.QueryResult;
import edu.emory.cci.bindaas.framework.model.RequestContext;
import edu.emory.cci.bindaas.framework.provider.exception.AbstractHttpCodeException;
import edu.emory.cci.bindaas.framework.provider.exception.QueryExecutionFailedException;
import edu.emory.cci.bindaas.framework.provider.exception.ValidationException;
import edu.emory.cci.bindaas.framework.util.GSONUtil;

public class MongoDBQueryHandler implements IQueryHandler {

    private Log log = LogFactory.getLog(getClass());
    private OutputFormatRegistry registry;
    private JsonParser parser = new JsonParser();
    public final static String ROLE = "role";
    private Map<String, DBCollection> dbCollectionMap = new HashMap<String, DBCollection>();

    public OutputFormatRegistry getRegistry() {
        return registry;
    }

    public void setRegistry(OutputFormatRegistry registry) {
        this.registry = registry;
    }

    @Override
    public QueryResult query(JsonObject dataSource,
            JsonObject outputFormatProps, String queryToExecute, Map<String,String> runtimeParameters, RequestContext requestContext)
            throws AbstractHttpCodeException {

        // can get role from requestContext as
        // requestContext.getAttributes().get(ROLE)

        try{
            if(outputFormatProps!=null)
            {
                OutputFormatProps props = GSONUtil.getGSONInstance().fromJson(outputFormatProps, OutputFormatProps.class);
                if(props!=null)
                {
                    // override output format
                    OutputFormat outputFormat = props.getOutputFormat();
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
                    props.setOutputFormat(outputFormat);

                    MongoDBQueryOperationDescriptor operationDescriptor = null;
                    try{
                         operationDescriptor = GSONUtil.getGSONInstance().fromJson(queryToExecute,MongoDBQueryOperationDescriptor.class);
                         if(operationDescriptor == null || operationDescriptor.get_operation() == null || operationDescriptor.get_operation_args()==null)
                         {
                             throw new Exception("Not a valid query object"); // the query is not annotated properly
                         }
                    }
                    catch(Exception e)
                    {
                        log.trace(e.getMessage());
                        // default to 'find' query
                        operationDescriptor = new MongoDBQueryOperationDescriptor();
                        operationDescriptor.set_operation(MongoDBQueryOperationType.find);
                        FindOperationDescriptor findArguments = new FindOperationDescriptor();
                        findArguments.setQuery(parser.parse(queryToExecute).getAsJsonObject());
                        operationDescriptor.set_operation_args(GSONUtil.getGSONInstance().toJsonTree(findArguments).getAsJsonObject());
                    }
                    
                        
                    // get DB collection
                    DataSourceConfiguration configuration = GSONUtil.getGSONInstance().fromJson(dataSource, DataSourceConfiguration.class);
                    String dbCollectionKey = configuration.getDb() + "-" + configuration.getCollection();
                    if (! dbCollectionMap.containsKey(dbCollectionKey) ) {
                        MongoClient mongo = null;
                        try {

                            MongoClientOptions.Builder optionsBuilder = new MongoClientOptions.Builder();
                            optionsBuilder.connectionsPerHost(50);
                            MongoClientOptions options = optionsBuilder.build();

                            if(configuration.getUsername() == null && configuration.getPassword() == null){
                                mongo = new MongoClient(new ServerAddress(configuration.getHost(),configuration.getPort()), options);
                            }
                            else if(configuration.getUsername().isEmpty() && configuration.getPassword().isEmpty()){
                                mongo = new MongoClient(new ServerAddress(configuration.getHost(),configuration.getPort()), options);
                            }
                            else{
                                MongoCredential credential = MongoCredential.createCredential(
                                        configuration.getUsername(),
                                        configuration.getAuthenticationDb(),
                                        configuration.getPassword().toCharArray()
                                );
                                mongo = new MongoClient(new ServerAddress(configuration.getHost(),configuration.getPort()), Arrays.asList(credential),options);
                            }

                            DB db = mongo.getDB(configuration.getDb());
                            DBCollection mongoDbCollection = db.getCollection(configuration.getCollection());
                            dbCollectionMap.put(dbCollectionKey, mongoDbCollection);

                            
                        } catch (Exception e) {
                            log.error("",e);
                            throw e;
                        }
                    }
                    // use operationDescriptor to route to correct handler
                    
                    IOperationHandler operationHandler = operationDescriptor.get_operation().getHandler();
                    QueryResult result = operationHandler.handleOperation(dbCollectionMap.get(dbCollectionKey), props , operationDescriptor.get_operation_args(), registry);
                    return result;
                    
                    
                }
                else
                {
                    throw new ValidationException(MongoDBProvider.class.getName() , MongoDBProvider.VERSION ,"outputFormat could not be parsed");
                }
            }
            else
            {
                throw new ValidationException(MongoDBProvider.class.getName() , MongoDBProvider.VERSION ,"outputFormat not specified");
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

    @Override
    public QueryEndpoint validateAndInitializeQueryEndpoint(
            QueryEndpoint queryEndpoint) throws ProviderException {
        try{
                if(queryEndpoint.getOutputFormat()!=null)
                {
                    OutputFormatProps props = GSONUtil.getGSONInstance().fromJson(queryEndpoint.getOutputFormat(), OutputFormatProps.class);
                    if(props!=null)
                    {
                        OutputFormat of = props.getOutputFormat();
                        IFormatHandler formatHandler = registry.getHandler(of);
                        if(formatHandler!=null && of.equals(OutputFormat.ANY)!= true)
                        {
                            formatHandler.validate(props);
                            return queryEndpoint;
                        }
                        else if(of.equals(OutputFormat.ANY) == true)
                        {
                            return queryEndpoint;
                        }
                        else
                        {
                            throw new ValidationException(MongoDBProvider.class.getName() , MongoDBProvider.VERSION  ,"No handler found for outputType=[" + of + "]");
                        }
                        
                    }
                    else
                    {
                        throw new Exception("outputFormat could not be parsed");
                    }
                }
                else
                {
                    throw new Exception("outputFormat not specified");
                }
                
        }
        catch(Exception e)
        {
            log.error(e);
            throw new ProviderException(MongoDBProvider.class.getName() , MongoDBProvider.VERSION ,e);
        }
    }

    @Override
    public JsonObject getOutputFormatSchema() {
        // TODO later
        return new JsonObject();
    }

}


