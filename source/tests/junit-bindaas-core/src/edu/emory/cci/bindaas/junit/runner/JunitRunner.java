package edu.emory.cci.bindaas.junit.runner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestFailure;
import junit.framework.TestResult;
import junit.textui.TestRunner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import edu.emory.cci.bindaas.junit.core.Activator;

public class JunitRunner implements CommandProvider {

	private final static String TEST_MANIFEST_HEADER_NAME = "Test-Suite";
	private Log log = LogFactory.getLog(getClass());
	private String junitLogFilePattern = "junit-bindaas-%s.log";
	private String junitErrorLogPattern = "junit-bindaas-%s.error";
	
	private List<Test> discoverTests()
	{
		BundleContext context = Activator.getContext();
		Bundle[] bundles = context.getBundles();
		List<Test> listOfTests = new ArrayList<Test>();
		
		for(Bundle bundle : bundles)
		{
			Object value = bundle.getHeaders().get(TEST_MANIFEST_HEADER_NAME);
			
			if(value!=null)
			{
				String testClassNames = value.toString();
				try{
					String[] testClasses = testClassNames.split(",");
					for(String testClass : testClasses)
					{
						Class clazz = bundle.loadClass(testClass);
						Object obj = clazz.newInstance();
						if(obj instanceof Test)
						{
							log.info("Discovered TestCase [" + testClass + "] in bundle [" + bundle.getSymbolicName() + "]");
							listOfTests.add((Test) obj);
						}
						
						
					}
				}
				catch(Exception e)
				{
					log.warn("Some TestCases from bundle [" + bundle.getSymbolicName() + "] were not added" , e);
				}
			}
		}
		
		return listOfTests;
	}
	public void runTestSuite(String id)
	{
		log.info("Running All Junit TestCases");
		PrintStream console;
		if(id == null)
		{
			id = ((new Date()).toString()).replace(" ", "-")   ;
		}
		try {
			console = new JunitLogger(new File( String.format(junitLogFilePattern, id )));
			TestRunner testRunner = new TestRunner(console);
			PrintWriter errorWriter = null;
			for(Test test : discoverTests())
			{
				console.println("\n------------------- Testing [" + test.getClass().getName() + "]  -------------------\n");
				TestResult testResult = testRunner.doRun(test);
				if(testResult.errorCount() > 0)
				{
					if(errorWriter == null)
					{
						errorWriter = new  PrintWriter(new File( String.format(junitErrorLogPattern, id )));
					}
					
					
					Enumeration<TestFailure> failures = testResult.errors(); 
					while(failures.hasMoreElements())
					{
						TestFailure failure = failures.nextElement();
						errorWriter.println(failure);
					}
				}
				
			}
			
			if(errorWriter!=null)
			{
				errorWriter.close();
			}
			
			log.info("Finished All Junit TestCases");
		} catch (FileNotFoundException e) {
			
			log.error("Cannot Start junit testing",e);
		}
		
		
	}

	@Override
	public String getHelp() {
		
		return "\njunit - Execute Test Cases defined in the Bundle Manifest header 'Test-Suite'\nOptions:\n[-test <TestSuite/TestClass>]\n[-report [console|file:<path>]]\n[-testDataDirectory <dataDirectory>]";
	}
	
	public void _junit(CommandInterpreter ci) {
		
		runTestSuite(null);
	}

}
