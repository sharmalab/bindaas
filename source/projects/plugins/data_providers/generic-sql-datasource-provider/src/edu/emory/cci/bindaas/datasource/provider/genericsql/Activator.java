package edu.emory.cci.bindaas.datasource.provider.genericsql;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import edu.emory.cci.bindaas.datasource.provider.genericsql.outputformat.CSVFormatHandler;
import edu.emory.cci.bindaas.datasource.provider.genericsql.outputformat.HTMLFormatHandler;
import edu.emory.cci.bindaas.datasource.provider.genericsql.outputformat.JSONFormatHandler;
import edu.emory.cci.bindaas.datasource.provider.genericsql.outputformat.XMLFormatHandler;

public class Activator implements BundleActivator {

	private static BundleContext context;
	private static Log log = LogFactory.getLog(Activator.class);

	public static BundleContext getContext() {
		return context;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
	}

	
	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		Activator.context = null;
	}
	
	public static <T> T  getService(Class<T> clazz)
	{
		ServiceReference sr = (ServiceReference) context.getServiceReference(clazz.getName());
		if(sr!=null)
		{
			T serviceObj = clazz.cast(context.getService(sr) ) ;
			return serviceObj;
		}
		else
			return null;
	}
	
	public static <T> T  getService(Class<T> clazz , String filter)
	{
		ServiceReference[] sr;
		try {
			sr = (ServiceReference[]) context.getServiceReferences(clazz.getName() , filter);
			if(sr!=null && sr.length > 0)
			{
				T serviceObj = clazz.cast(context.getService(sr[0]) ) ;
				return serviceObj;
			}
			else
				return null;
		} catch (InvalidSyntaxException e) {
			log.error(e);
			return null;
		}
		
	}
	

}
