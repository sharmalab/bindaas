package edu.emory.cci.bindaas.junit.mock;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import edu.emory.cci.bindaas.framework.api.IDeleteHandler;
import edu.emory.cci.bindaas.framework.api.IProvider;
import edu.emory.cci.bindaas.framework.api.IQueryHandler;
import edu.emory.cci.bindaas.framework.api.ISubmitHandler;
import edu.emory.cci.bindaas.framework.model.Profile;
import edu.emory.cci.bindaas.framework.model.ProviderException;

public class MockProvider implements IProvider{

	public static final Integer VERSION = 1;
	private  MockQueryHandler mockQueryHandler;
	private  MockDeleteHandler mockDeleteHandler;
	private  MockSubmitHandler mockSubmitHandler;
	
	
	public void init() {
		
		mockQueryHandler = new MockQueryHandler();
		mockDeleteHandler = new MockDeleteHandler();
		mockSubmitHandler = new MockSubmitHandler();
	}
	@Override
	public String getId() {
		
		return MockProvider.class.getName();
	}

	@Override
	public int getVersion() {

		return VERSION;
	}

	@Override
	public JsonObject getDocumentation() {

		return new JsonObject();
	}

	@Override
	public IQueryHandler getQueryHandler() {

		return mockQueryHandler;
	}

	@Override
	public ISubmitHandler getSubmitHandler() {

		return mockSubmitHandler;
	}

	@Override
	public IDeleteHandler getDeleteHandler() {

		return mockDeleteHandler;
	}

	@Override
	public Profile validateAndInitializeProfile(Profile profile)
			throws ProviderException {
		profile.getDataSource().add("validated", new JsonPrimitive(true));
		return profile;
	}

	@Override
	public JsonObject getDataSourceSchema() {

		return new JsonObject();
	}

}
