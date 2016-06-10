package edu.emory.cci.bindaas.collectionquery;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;

import com.google.gson.JsonObject;

import edu.emory.cci.bindaas.collectionquery.bundle.Activator;
import edu.emory.cci.bindaas.framework.api.IQueryModifier;
import edu.emory.cci.bindaas.framework.model.ModifierException;
import edu.emory.cci.bindaas.framework.model.RequestContext;
import edu.emory.cci.bindaas.framework.provider.exception.AbstractHttpCodeException;
import edu.emory.cci.bindaas.provider.ICollectionAuthorizationProvider;

/**
 * Created by sagrava on 3/25/15.
 */
public class CollectionQueryModifierPlugin implements IQueryModifier {

	private Log log = LogFactory.getLog(getClass());
	private Map<String, Map<String, String>> userAuthRegistry;
	private ICollectionAuthorizationProvider collectionAuthorizationProvider;
	private boolean isValid = true;
	
	public void init() throws Exception {
		BundleContext context = Activator.getContext();
		
		Dictionary<String, String> props = new Hashtable<String, String>();
		props.put("class", getClass().getName());
		context.registerService(IQueryModifier.class.getName(), this,
				props);
		
		
		userAuthRegistry = collectionAuthorizationProvider.load(context);
		
	}

	@Override
	public String getDescriptiveName() {

		return "Collection Query Modifier Plugin";
	}

	@Override
	public JsonObject getDocumentation() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void validate() throws ModifierException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String modifyQuery(String query, JsonObject dataSource,
			RequestContext requestContext, JsonObject modifierProperties)
			throws AbstractHttpCodeException {

		if (isValid)
			return query;
		else
			return null;
	}

	@Override
	public Map<String, String> modiftQueryParameters(
			Map<String, String> queryParams, JsonObject dataSource,
			RequestContext requestContext, JsonObject modifierProperties)
			throws AbstractHttpCodeException {
		
		
		Map<String, String> userAttributeAuthMap = this.userAuthRegistry.get(requestContext.getUser());
		
		if (queryParams.containsKey("Collection")) {
			
			String value = queryParams.get("Collection");
			if (!userAttributeAuthMap.containsKey(value)) {
				isValid = false;
				
			}
			
		}
		return queryParams;
		
	}

	public ICollectionAuthorizationProvider getCollectionAuthorizationProvider() {
		return collectionAuthorizationProvider;
	}

	public void setCollectionAuthorizationProvider(
			ICollectionAuthorizationProvider collectionAuthorizationProvider) {
		this.collectionAuthorizationProvider = collectionAuthorizationProvider;
	}
	
	
}
