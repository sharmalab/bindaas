package edu.emory.cci.bindaas.trusted_app.bundle;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

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

//	public static <T> T  getService(Class<T> clazz) {
//		ServiceReference<T> srf = context.getServiceReference(clazz);
//		if(srf!=null)
//			return context.getService(srf);
//
//		return null;
//	}

	public static <T> T  getService(Class<T> clazz , String filter)
	{
		@SuppressWarnings("rawtypes")
		ServiceReference[] sr;
		try {
			sr = (ServiceReference[]) context.getServiceReferences(clazz.getName() , filter);
			if(sr!=null && sr.length > 0)
			{
				@SuppressWarnings("unchecked")
				T serviceObj = clazz.cast(context.getService(sr[0]) ) ;
				return serviceObj;
			}
			else
				return null;
		} catch (InvalidSyntaxException e) {
			return null;
		}

	}
}
