package edu.emory.cci.bindaas.framework.api;

import java.util.Map;

import com.google.gson.JsonObject;

import edu.emory.cci.bindaas.framework.model.QueryResult;
import edu.emory.cci.bindaas.framework.model.RequestContext;

public interface IQueryResultModifier extends IModifier {

	public QueryResult modifyQueryResult(QueryResult queryResult ,  JsonObject dataSource , RequestContext requestContext , JsonObject modifierProperties , Map<String,String> queryParams) throws Exception;
}
