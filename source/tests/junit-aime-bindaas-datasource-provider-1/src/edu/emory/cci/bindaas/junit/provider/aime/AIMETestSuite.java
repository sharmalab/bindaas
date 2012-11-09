package edu.emory.cci.bindaas.junit.provider.aime;

import junit.framework.TestSuite;

public class AIMETestSuite extends TestSuite {

	public AIMETestSuite()
	{
		addTestSuite(AIMEProviderTestCase.class);
		addTestSuite(AIMESubmitHandlerTestCase.class);
		addTestSuite(AIMEQueryHandlerTestCase.class);
//		addTestSuite(AIMEDeleteHandlerTestCase.class);
	}
}
