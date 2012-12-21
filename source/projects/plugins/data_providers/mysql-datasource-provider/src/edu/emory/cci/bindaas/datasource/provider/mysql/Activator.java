package edu.emory.cci.bindaas.datasource.provider.mysql;



import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import edu.emory.cci.bindaas.framework.api.IProvider;

public class Activator implements BundleActivator {

	private static BundleContext bundlecontext;
	@Override
	public void start(BundleContext context) throws Exception {
		
		context.registerService(IProvider.class.getName(), new MySQLProvider(), null);
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
