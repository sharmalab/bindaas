package edu.emory.cci.bindaas.datasource.provider.mongodb.operation;

import com.google.gson.JsonObject;
import com.mongodb.DBCollection;

import edu.emory.cci.bindaas.datasource.provider.mongodb.model.OutputFormatProps;
import edu.emory.cci.bindaas.datasource.provider.mongodb.outputformat.OutputFormatRegistry;
import edu.emory.cci.bindaas.framework.model.ProviderException;
import edu.emory.cci.bindaas.framework.model.QueryResult;

public interface IOperationHandler {

	public QueryResult handleOperation(DBCollection collection, OutputFormatProps outputFormatProps , JsonObject operationArguments, OutputFormatRegistry registry) throws ProviderException;
	
}
