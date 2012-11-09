package edu.emory.cci.bindaas.framework.api;

import com.google.gson.JsonObject;

import edu.emory.cci.bindaas.framework.model.QueryResult;

public interface IQueryResultModifier extends IModifier {

	public QueryResult modifyQueryResult(QueryResult queryResult ,  JsonObject dataSource , String user , JsonObject modifierProperties) throws Exception;
}
