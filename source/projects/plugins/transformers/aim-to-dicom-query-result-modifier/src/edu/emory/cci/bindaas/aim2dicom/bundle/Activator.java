package edu.emory.cci.bindaas.aim2dicom.bundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import edu.emory.cci.bindaas.core.util.ProfilerService;

public class Activator implements BundleActivator {

	private static BundleContext context;
	private Log log = LogFactory.getLog(getClass());
	public static BundleContext getContext() {
		return context;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		log.debug("Starting Bundle [aim-to-dicom-query-result-modifier]");
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
		@SuppressWarnings("rawtypes")
		ServiceReference sr = (ServiceReference) context.getServiceReference(clazz.getName());
		if(sr!=null)
		{
			
			@SuppressWarnings("unchecked")
			T serviceObj = clazz.cast(context.getService(sr) ) ;
			return serviceObj;
		}
		else
			return null;
	}

	public static ProfilerService getProfilerService()
	{
		@SuppressWarnings("rawtypes")
		ServiceReference sr = (ServiceReference) context.getServiceReference(ProfilerService.class.getName());
		if(sr!=null)
		{
			@SuppressWarnings("unchecked")
			ProfilerService serviceObj = ProfilerService.class.cast(context.getService(sr) ) ;
			return serviceObj;
		}
		else
			return null;
	}

}
