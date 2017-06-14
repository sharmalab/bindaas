package edu.emory.cci.bindaas.blobdownload.bundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * Created by sagrava on 3/25/15.
 */
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

        Activator.context = bundleContext;
        log.trace(String.format("Starting Bundle [%s]", context.getBundle().getSymbolicName()));
    }

    /*
     * (non-Javadoc)
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext bundleContext) throws Exception {
        Activator.context = null;
    }

}
