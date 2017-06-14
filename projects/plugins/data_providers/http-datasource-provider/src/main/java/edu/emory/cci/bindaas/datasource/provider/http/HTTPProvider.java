package edu.emory.cci.bindaas.datasource.provider.http;

import java.util.Dictionary;
import java.util.Hashtable;

import com.google.gson.JsonObject;

import edu.emory.cci.bindaas.datasource.provider.http.bundle.Activator;
import edu.emory.cci.bindaas.framework.api.IProvider;
import edu.emory.cci.bindaas.framework.model.Profile;
import edu.emory.cci.bindaas.framework.model.ProviderException;
import edu.emory.cci.bindaas.framework.util.DocumentationUtil;

public class HTTPProvider implements IProvider{
	public static final Integer VERSION = 1;
	private HTTPQueryHandler queryHandler;
	private HTTPDeleteHandler deleteHandler;
	private HTTPSubmitHandler submitHandler;
	
	private static final String DOCUMENTATION_RESOURCES_LOCATION = "META-INF/documentation";
	private JsonObject documentation;
	
	
		public void init() {
		
		
		Dictionary<String, Object> props = new Hashtable<String, Object>();
		
		props.put("class", getClass().getName());
		
		Activator.getContext().registerService(IProvider.class.getName(), this, props);
		
		// initialize documentation object
		
		documentation = DocumentationUtil.getProviderDocumentation(Activator.getContext(), DOCUMENTATION_RESOURCES_LOCATION);
		
	}
	
	@Override
	public String getId() {
		
		return getClass().getName();
	}

	@Override
	public int getVersion() {
		return VERSION;
	}

	@Override
	public JsonObject getDocumentation() {
		return documentation;
	}

	public HTTPQueryHandler getQueryHandler() {
		return queryHandler;
	}

	public void setQueryHandler(HTTPQueryHandler queryHandler) {
		this.queryHandler = queryHandler;
	}

	public HTTPDeleteHandler getDeleteHandler() {
		return deleteHandler;
	}

	public void setDeleteHandler(HTTPDeleteHandler deleteHandler) {
		this.deleteHandler = deleteHandler;
	}

	public HTTPSubmitHandler getSubmitHandler() {
		return submitHandler;
	}

	public void setSubmitHandler(HTTPSubmitHandler submitHandler) {
		this.submitHandler = submitHandler;
	}

	@Override
	public Profile validateAndInitializeProfile(Profile profile)
			throws ProviderException {
		profile.setDataSource(new JsonObject());
		return profile;
	}

	@Override
	public JsonObject getDataSourceSchema() {
		// TODO Auto-generated method stub
		return null;
	} 
}
