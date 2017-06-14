package edu.emory.cci.bindaas.datasource.provider.postgres.bundle;



import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import edu.emory.cci.bindaas.datasource.provider.postgres.PostgresProvider;
import edu.emory.cci.bindaas.framework.api.IProvider;

public class Activator implements BundleActivator {

	private static BundleContext bundlecontext;
	private Log log = LogFactory.getLog(getClass());
	@Override
	public void start(BundleContext context) throws Exception {
		log.trace(String.format("Starting Bundle [%s]", context.getBundle().getSymbolicName()));
		context.registerService(IProvider.class.getName(), new PostgresProvider(), null);
		bundlecontext = context;
	}

	@Override
	public void stop(BundleContext arg0) throws Exception {
		// TODO Auto-generated method stub

	}

	public static BundleContext getContext() {
		return bundlecontext;
	}

}
