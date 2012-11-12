package edu.emory.cci.bindaas.datasource.provider.genericsql;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import edu.emory.cci.bindaas.datasource.provider.genericsql.outputformat.CSVFormatHandler;
import edu.emory.cci.bindaas.datasource.provider.genericsql.outputformat.HTMLFormatHandler;
import edu.emory.cci.bindaas.datasource.provider.genericsql.outputformat.IFormatHandler;
import edu.emory.cci.bindaas.datasource.provider.genericsql.outputformat.JSONFormatHandler;
import edu.emory.cci.bindaas.datasource.provider.genericsql.outputformat.XMLFormatHandler;

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
		registerFormatHandlers();
	}

	private void registerFormatHandlers() {
		
		new CSVFormatHandler();
		new HTMLFormatHandler();
		new JSONFormatHandler();
		new XMLFormatHandler();
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		Activator.context = null;
	}

}
