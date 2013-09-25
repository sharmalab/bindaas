package edu.emory.cci.bindaas.core.rest.service.impl;

import java.util.Collection;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import edu.emory.cci.bindaas.core.api.IModifierRegistry;
import edu.emory.cci.bindaas.core.api.IProviderRegistry;
import edu.emory.cci.bindaas.core.rest.service.api.IInformationService;
import edu.emory.cci.bindaas.core.util.RestUtils;
import edu.emory.cci.bindaas.framework.api.IModifier;
import edu.emory.cci.bindaas.framework.api.IProvider;
import edu.emory.cci.bindaas.framework.api.IQueryModifier;
import edu.emory.cci.bindaas.framework.api.IQueryResultModifier;
import edu.emory.cci.bindaas.framework.api.ISubmitPayloadModifier;

public class InformationServiceImpl implements IInformationService{
	
	private Log log = LogFactory.getLog(getClass());
	private IProviderRegistry providerRegistry;
	private IModifierRegistry modifierRegistry;
	private RestUtils restUtils;
	
	public RestUtils getRestUtils() {
		return restUtils;
	}

	public void setRestUtils(RestUtils restUtils) {
		this.restUtils = restUtils;
	}

	
	public IProviderRegistry getProviderRegistry() {
		return providerRegistry;
	}

	public void setProviderRegistry(IProviderRegistry providerRegistry) {
		this.providerRegistry = providerRegistry;
	}

	public IModifierRegistry getModifierRegistry() {
		return modifierRegistry;
	}

	public void setModifierRegistry(IModifierRegistry modifierRegistry) {
		this.modifierRegistry = modifierRegistry;
	}
	
	public void init() throws Exception
	{
		// do init here
		
	}

	private JsonObject createJsonFromProvider(IProvider provider)
	{
		JsonObject providerObj = new JsonObject();
		providerObj.add("id", new JsonPrimitive(provider.getId()));
		providerObj.add("version", new JsonPrimitive(provider.getVersion()));
		providerObj.add("dataSourceSchema", provider.getDataSourceSchema());
		providerObj.add("documentation", provider.getDocumentation());
		return providerObj;
	}
	
	private JsonObject createJsonFromModifier(IModifier modifier)
	{
		JsonObject obj = new JsonObject();
		obj.add("id", new JsonPrimitive(modifier.getClass().getName()));
		obj.add("documentation", modifier.getDocumentation());
		return obj;
	}
	
	
	@Override
	@Path("providers")
	@GET
	public Response listProviders() {
		Collection<IProvider> listOfProviders = providerRegistry.findProviders();
		JsonArray array = new JsonArray();
		for(IProvider provider : listOfProviders)
		{
			array.add(createJsonFromProvider(provider));
		}
		
		return restUtils.createSuccessResponse(array.toString(), "application/json");
		
	}

	@Override
	@Path("providers/{providerId}")
	@GET
	public Response getProvider(@PathParam("providerId") String providerId) {
		Collection<IProvider> listOfProviders = providerRegistry.findProvider(providerId);
		JsonArray array = new JsonArray();
		for(IProvider provider : listOfProviders)
		{
			array.add(createJsonFromProvider(provider));
		}
		
		return restUtils.createSuccessResponse(array.toString(), "application/json");
	}

	@Override
	@Path("providers/{providerId}/{providerVersion}")
	@GET
	public Response getProvider(@PathParam("providerId") String providerId,
			@PathParam("providerVersion") String providerVersion) {
		try{
		IProvider provider = providerRegistry.lookupProvider(providerId, Integer.parseInt(providerVersion));
		JsonObject resp = createJsonFromProvider(provider);
		return restUtils.createSuccessResponse(resp.toString(), "application/json");
		}
		catch(Exception e)
		{
			log.error(e);
			return restUtils.createErrorResponse("Provider Not Found");
		}
		
	}

	@Override
	@Path("queryModifiers")
	@GET
	public Response listQueryModifiers() {
		Collection<IQueryModifier> listOfQueryMods = modifierRegistry.findAllQueryModifier();
		JsonArray array = new JsonArray();
		for(IQueryModifier mod : listOfQueryMods)
		{
			array.add(createJsonFromModifier(mod));
		}
		
		return restUtils.createSuccessResponse(array.toString(), "application/json");
	}

	@Override
	@Path("queryModifiers/{queryModifier}")
	@GET
	public Response getQueryModifier(
			@PathParam("queryModifier") String queryModifier) {
		IQueryModifier mod = modifierRegistry.findQueryModifier(queryModifier);
		if(mod!=null)
		{
			return restUtils.createSuccessResponse( createJsonFromModifier(mod).toString(), "application/json");
		}
		else
		{
			return restUtils.createErrorResponse("IQueryModifier Not Found [" + queryModifier + "]");
		}
	}

	@Override
	@Path("queryResultModifiers")
	@GET
	public Response listQueryResultModifiers() {
		Collection<IQueryResultModifier> listOfQueryResultMods = modifierRegistry.findAllQueryResultModifiers();
		JsonArray array = new JsonArray();
		for(IQueryResultModifier mod : listOfQueryResultMods)
		{
			array.add(createJsonFromModifier(mod));
		}
		
		return restUtils.createSuccessResponse(array.toString(), "application/json");
	}

	@Override
	@Path("queryResultModifiers/{queryResultModifier}")
	@GET
	public Response getQueryResultModifier(
			@PathParam("queryResultModifier") String queryResultModifier) {
		IQueryResultModifier mod = modifierRegistry.findQueryResultModifier(queryResultModifier);
		if(mod!=null)
		{
			return restUtils.createSuccessResponse( createJsonFromModifier(mod).toString(), "application/json");
		}
		else
		{
			return restUtils.createErrorResponse("IQueryResultModifier Not Found [" + queryResultModifier + "]");
		}
	}

	@Override
	@Path("submitPayloadModifiers")
	@GET
	public Response listSubmitPayloadModifiers() {
		Collection<ISubmitPayloadModifier> listOfSubmitPayloadMods = modifierRegistry.findAllSubmitPayloadModifiers();
		JsonArray array = new JsonArray();
		for(ISubmitPayloadModifier mod : listOfSubmitPayloadMods)
		{
			array.add(createJsonFromModifier(mod));
		}
		
		return restUtils.createSuccessResponse(array.toString(), "application/json");
	}

	@Override
	@Path("submitPayloadModifiers/{submitPayloadModifier}")
	@GET
	public Response getSubmitPayloadModifier(
			@PathParam("submitPayloadModifier") String submitPayloadModifier) {
		ISubmitPayloadModifier mod = modifierRegistry.findSubmitPayloadModifier(submitPayloadModifier);
		if(mod!=null)
		{
			return restUtils.createSuccessResponse( createJsonFromModifier(mod).toString(), "application/json");
		}
		else
		{
			return restUtils.createErrorResponse("ISubmitPayloadModifier Not Found [" + submitPayloadModifier + "]");
		}
	}

}
