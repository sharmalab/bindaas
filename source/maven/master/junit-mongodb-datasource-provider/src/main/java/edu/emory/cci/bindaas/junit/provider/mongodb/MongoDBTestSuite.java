package edu.emory.cci.bindaas.junit.provider.mongodb;

import junit.framework.TestSuite;

public class MongoDBTestSuite extends TestSuite{

	public MongoDBTestSuite()
	{
		addTestSuite(ProviderTestCase.class);
		addTestSuite(SubmitHandlerTestCase.class);
		addTestSuite(QueryHandlerTestCase.class);
		addTestSuite(DeleteHandlerTestCase.class);
		
	}
}
