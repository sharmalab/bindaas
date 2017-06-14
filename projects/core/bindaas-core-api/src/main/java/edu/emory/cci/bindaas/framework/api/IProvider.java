package edu.emory.cci.bindaas.framework.api;

import com.google.gson.JsonObject;

import edu.emory.cci.bindaas.framework.model.Profile;
import edu.emory.cci.bindaas.framework.model.ProviderException;

public interface IProvider {

	public String getId();
	public int getVersion();
	public JsonObject  getDocumentation();
	public IQueryHandler getQueryHandler();
	public ISubmitHandler getSubmitHandler();
	public IDeleteHandler getDeleteHandler();
	public Profile validateAndInitializeProfile(Profile profile) throws ProviderException;
	public JsonObject getDataSourceSchema();
}


