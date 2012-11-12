package edu.emory.cci.bindaas.junit.core;

import org.eclipse.osgi.framework.console.CommandProvider;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import edu.emory.cci.bindaas.framework.api.IProvider;
import edu.emory.cci.bindaas.junit.mock.MockProvider;
import edu.emory.cci.bindaas.junit.runner.JunitRunner;

public class Activator implements BundleActivator {

	private static BundleContext context;

	public static BundleContext getContext() {
		return context;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
		context.registerService(CommandProvider.class.getName(), new JunitRunner(), null);
		MockProvider provider = new MockProvider();
		provider.init();
		context.registerService(IProvider.class.getName(), provider, null);
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		Activator.context = null;
	}
	

}
