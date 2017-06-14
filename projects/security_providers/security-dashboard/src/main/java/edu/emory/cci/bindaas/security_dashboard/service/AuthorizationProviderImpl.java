package edu.emory.cci.bindaas.security_dashboard.service;

import java.util.Arrays;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import com.google.common.base.Joiner;

import edu.emory.cci.bindaas.core.util.DynamicObject;
import edu.emory.cci.bindaas.security.api.IAuthorizationProvider;
import edu.emory.cci.bindaas.security_dashboard.api.IPolicyManager;
import edu.emory.cci.bindaas.security_dashboard.bundle.Activator;
import edu.emory.cci.bindaas.security_dashboard.config.SecurityDashboardConfiguration;

public class AuthorizationProviderImpl
		implements
		IAuthorizationProvider
		 {

	private Log log = LogFactory.getLog(getClass());
	private IPolicyManager policyManager;
	private ServiceTracker<DynamicObject<SecurityDashboardConfiguration>, DynamicObject<SecurityDashboardConfiguration>> serviceTracker;
	private Customizer customizer;

	public void init() {
		BundleContext context = Activator.getContext();
		customizer = new Customizer();
		serviceTracker = new ServiceTracker<DynamicObject<SecurityDashboardConfiguration>, DynamicObject<SecurityDashboardConfiguration>>(
				context, DynamicObject.class.getName(), customizer);
		serviceTracker.open();

	}

	@Override
	public boolean isAuthorized(Map<String, String> userAttributes,
			String username, String resourceId, String actionId)
			throws Exception {
		log.debug("Performing Authorization using Security Dashboard on user ["
				+ username + "] and resource [" + resourceId + "]");

		String[] pathFragments = resourceId.split("/");

		if (pathFragments.length > 4) {
			String[] lastFourFragments = Arrays.copyOfRange(pathFragments,
					pathFragments.length - 4, pathFragments.length);
			String resource = Joiner.on("/").join(lastFourFragments);
			boolean result = policyManager.isAllowedAccess(username, resource,
					getConfiguration());
			return result;
		}

		return false;
	}

	public SecurityDashboardConfiguration getConfiguration() {
		if (customizer.dynamicConfig != null) {
			return customizer.dynamicConfig.getObject();
		} else
			return null;
	}

	public IPolicyManager getPolicyManager() {
		return policyManager;
	}

	public void setPolicyManager(IPolicyManager policyManager) {
		this.policyManager = policyManager;
	}

		
	private static class Customizer implements ServiceTrackerCustomizer<DynamicObject<SecurityDashboardConfiguration>, DynamicObject<SecurityDashboardConfiguration>>{
		private DynamicObject<SecurityDashboardConfiguration> dynamicConfig;


		@Override
		public DynamicObject<SecurityDashboardConfiguration> addingService(
				ServiceReference<DynamicObject<SecurityDashboardConfiguration>> srf) {

			if (srf.getProperty("name").equals("security-dashboard")) {
				dynamicConfig = Activator.getContext().getService(srf);
			}

			return dynamicConfig;
		}

		@Override
		public void modifiedService(
				ServiceReference<DynamicObject<SecurityDashboardConfiguration>> arg0,
				DynamicObject<SecurityDashboardConfiguration> arg1) {

		}

		@Override
		public void removedService(
				ServiceReference<DynamicObject<SecurityDashboardConfiguration>> srf,
				DynamicObject<SecurityDashboardConfiguration> obj) {
			if (srf.getProperty("name").equals("security-dashboard")) {
				dynamicConfig = null;
			}

		}

	}

}
