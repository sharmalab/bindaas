package edu.emory.cci.bindaas.provider;


import java.util.Map;

import org.osgi.framework.BundleContext;

public interface ICollectionAuthorizationProvider {

	public Map<String,Map<String, String>> load(BundleContext context);
	
}
