package edu.emory.cci.bindaas.framework;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

	private static BundleContext context;
	
	
	public static BundleContext getContext()
	{
		return context;
	}
	
	@Override
	public void start(BundleContext arg0) throws Exception {
		context = arg0;

	}

	@Override
	public void stop(BundleContext arg0) throws Exception {
		context = null;

	}

}
