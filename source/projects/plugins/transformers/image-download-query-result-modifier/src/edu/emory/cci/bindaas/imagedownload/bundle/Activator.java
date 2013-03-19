package edu.emory.cci.bindaas.imagedownload.bundle;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import edu.emory.cci.bindaas.core.util.ProfilerService;

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
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		Activator.context = null;
	}

	public static ProfilerService getProfilerService()
	{
		ServiceReference sr = (ServiceReference) context.getServiceReference(ProfilerService.class.getName());
		if(sr!=null)
		{
			ProfilerService serviceObj = ProfilerService.class.cast(context.getService(sr) ) ;
			return serviceObj;
		}
		else
			return null;
	}


}
