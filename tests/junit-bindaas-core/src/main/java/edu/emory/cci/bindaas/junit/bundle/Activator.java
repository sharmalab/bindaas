package edu.emory.cci.bindaas.junit.bundle;

import org.eclipse.osgi.framework.console.CommandProvider;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import edu.emory.cci.bindaas.framework.api.IProvider;
import edu.emory.cci.bindaas.junit.mock.MockProvider;
import edu.emory.cci.bindaas.junit.runner.JunitRunner;

public class Activator implements BundleActivator {

	private static BundleContext context;
	private static final String RUN_JUNIT_AT_START_COMMAND = "runJunit";

	public static BundleContext getContext() {
		return context;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
		JunitRunner junitRunner = new JunitRunner();
		context.registerService(CommandProvider.class.getName(),junitRunner , null);
		MockProvider provider = new MockProvider();
		provider.init();
		context.registerService(IProvider.class.getName(), provider, null);
		
		if(System.getProperties().containsKey(RUN_JUNIT_AT_START_COMMAND))
		{
			junitRunner.runTestSuite(System.getProperties().getProperty(RUN_JUNIT_AT_START_COMMAND));
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		Activator.context = null;
	}
	

}
