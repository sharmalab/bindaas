package edu.emory.cci.bindaas.framework.bundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

	private static BundleContext context;
	private Log log = LogFactory.getLog(getClass());
	
	public static BundleContext getContext()
	{
		return context;
	}
	
	@Override
	public void start(BundleContext arg0) throws Exception {
		log.debug("Starting Bundle [bindaas-core-api]");
		context = arg0;

	}

	@Override
	public void stop(BundleContext arg0) throws Exception {
		context = null;

	}

}
