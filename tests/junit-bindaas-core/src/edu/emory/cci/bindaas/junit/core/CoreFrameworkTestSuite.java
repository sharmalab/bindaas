package edu.emory.cci.bindaas.junit.core;

import junit.framework.TestSuite;

public class CoreFrameworkTestSuite extends TestSuite {

	public CoreFrameworkTestSuite() {
		addTestSuite(ManagementTasksTest.class);
		addTestSuite(ExecutionTasksTest.class);
	}

}
