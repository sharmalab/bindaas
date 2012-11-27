package edu.emory.cci.bindaas.core.impl;

import java.io.InputStream;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.JsonObject;

import edu.emory.cci.bindaas.core.api.IExecutionTasks;
import edu.emory.cci.bindaas.core.api.IManagementTasks;
import edu.emory.cci.bindaas.core.api.IModifierRegistry;
import edu.emory.cci.bindaas.core.api.IProviderRegistry;
import edu.emory.cci.bindaas.core.api.IValidator;
import edu.emory.cci.bindaas.core.bundle.Activator;
import edu.emory.cci.bindaas.core.exception.ExecutionTaskException;
import edu.emory.cci.bindaas.framework.api.IDeleteHandler;
import edu.emory.cci.bindaas.framework.api.IProvider;
import edu.emory.cci.bindaas.framework.api.IQueryHandler;
import edu.emory.cci.bindaas.framework.api.IQueryModifier;
import edu.emory.cci.bindaas.framework.api.IQueryResultModifier;
import edu.emory.cci.bindaas.framework.api.ISubmitHandler;
import edu.emory.cci.bindaas.framework.api.ISubmitPayloadModifier;
import edu.emory.cci.bindaas.framework.model.BindVariable;
import edu.emory.cci.bindaas.framework.model.DeleteEndpoint;
import edu.emory.cci.bindaas.framework.model.ModifierEntry;
import edu.emory.cci.bindaas.framework.model.Profile;
import edu.emory.cci.bindaas.framework.model.QueryEndpoint;
import edu.emory.cci.bindaas.framework.model.QueryResult;
import edu.emory.cci.bindaas.framework.model.SubmitEndpoint;

public class ExecutionTaskImpl implements IExecutionTasks{

	private IProviderRegistry providerRegistry;
	private IModifierRegistry modifierRegistry;
	private IValidator validator;
	private Log log = LogFactory.getLog(getClass());
	
	
	public ExecutionTaskImpl()
	{
		Dictionary<String, String> props = new Hashtable<String, String>();
		props.put("class", getClass().getName());
		Activator.getContext().registerService(IExecutionTasks.class.getName(), this, props);
	}
	
	@Override
	public QueryResult executeQueryEndpoint(String user ,Map<String,String> runtimeParameters,
			Profile profile ,QueryEndpoint queryEndpoint) throws ExecutionTaskException {

		// construct real query
		Map<String,BindVariable> bindVariables = queryEndpoint.getBindVariables();
		String template = queryEndpoint.getQueryTemplate();
		for(BindVariable bindVariable : bindVariables.values())
		{
			String attrName = bindVariable.getName();
			String attrValue = runtimeParameters.get(attrName);
			
			if(!bindVariable.isRequired())
			{
				if(attrValue == null)
				{
					attrValue = bindVariable.getDefaultValue();
				}
				
			}
			else if(attrValue == null)
			{
					throw new ExecutionTaskException("Mandatory attribute ["+ attrName + "] not provided");
			}
		
			log.trace("Substituting [" + "$" + attrName + "$" + "] for [" + attrValue + "] in the Query Template");
			
			
			template = template.replace("$" + attrName + "$", attrValue);
		}
		
		try {
		// execute queryModifier chain
		String finalQuery = executeQueryModifierChain(user, template, queryEndpoint.getQueryModifiers() , profile.getDataSource());
		
		// execute handler
		
		IProvider provider = providerRegistry.lookupProvider(profile.getProviderId(), profile.getProviderVersion());
		IQueryHandler queryHandler = provider.getQueryHandler();
		QueryResult queryResult = queryHandler.query(profile.getDataSource(),queryEndpoint.getOutputFormat() ,finalQuery);
		
		// execute query result chain
		
		queryResult = executeQueryResultModifierChain(user, queryResult, queryEndpoint.getQueryResultModifiers(), profile.getDataSource());
		
		// render result
		return queryResult;
		}
		catch (Exception e) {
			log.error("Execution Task failed" , e);
			throw new ExecutionTaskException(e);
		}
	}

	@Override
	public QueryResult executeDeleteEndpoint(String user ,Map<String,String> runtimeParameters,
			Profile profile ,DeleteEndpoint deleteEndpoint) throws ExecutionTaskException {
		// construct real query
				Map<String,BindVariable> bindVariables = deleteEndpoint.getBindVariables();
				String template = deleteEndpoint.getQueryTemplate();
				for(BindVariable bindVariable : bindVariables.values())
				{
					String attrName = bindVariable.getName();
					String attrValue = runtimeParameters.get(attrName);
					
					if(!bindVariable.isRequired())
					{
						if(attrValue == null)
						{
							attrValue = bindVariable.getDefaultValue();
						}
						
					}
					else if(attrValue == null)
					{
							throw new ExecutionTaskException("Mandatory attribute ["+ attrName + "] not provided");
					}
				
					log.trace("Substituting [" + "$" + attrName + "$" + "] for [" + attrValue + "] in the Query Template");
					
					
					template = template.replace("$" + attrName + "$", attrValue);
				}
				
				// execute handler
				
				
				try {
					IProvider provider = providerRegistry.lookupProvider(profile.getProviderId(), profile.getProviderVersion());
					IDeleteHandler deleteHandler = provider.getDeleteHandler();
					QueryResult queryResult = deleteHandler.delete(profile.getDataSource(), template); 
					// render result
					return queryResult;
					
				} catch (Exception e) {
					log.error("Execution Task failed" , e);
					throw new ExecutionTaskException(e);
				}
								
				
	}

	@Override
	public QueryResult executeSubmitEndpoint(String user ,InputStream is,
			Profile profile ,SubmitEndpoint submitEndpoint) throws ExecutionTaskException {
		try{
		InputStream finalStream = executeSubmitPayloadModifierChain(user, is, submitEndpoint.getSubmitPayloadModifiers(), submitEndpoint);
		// execute handler
		
		IProvider provider = providerRegistry.lookupProvider(profile.getProviderId(), profile.getProviderVersion());
		ISubmitHandler submitHandler = provider.getSubmitHandler();
		QueryResult queryResult = submitHandler.submit(profile.getDataSource(), submitEndpoint.getProperties(), finalStream);
		return queryResult;
		}
		catch (Exception e) {
			log.error("Execution Task failed" , e);
			throw new ExecutionTaskException(e);
		}
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


	public IValidator getValidator() {
		return validator;
	}

	public void setValidator(IValidator validator) {
		this.validator = validator;
	}

	protected String executeQueryModifierChain(String user ,String query , ModifierEntry modifierChain , JsonObject dataSource) throws Exception
	{
		String modifiedQuery = query;
		ModifierEntry next = modifierChain;
		while(next!=null)
		{
			IQueryModifier queryModifier = modifierRegistry.findQueryModifier(next.getName()) ;
			if(queryModifier!=null)
			{
				modifiedQuery = queryModifier.modifyQuery(modifiedQuery, dataSource, user, next.getProperties());
				next = next.getAttachment();
			}
			else
			{
				throw new ExecutionTaskException("[IQueryModifier] by id=[" + next.getName()  +"] not found");
			}
			
		}
		
		return modifiedQuery;
	}
	
	protected QueryResult executeQueryResultModifierChain(String user , QueryResult queryResult  ,ModifierEntry modifierChain , JsonObject dataSource) throws Exception
	{
		QueryResult modifiedQueryResult = queryResult;
		ModifierEntry next = modifierChain;
		while(next!=null)
		{
			IQueryResultModifier queryResultModifier = modifierRegistry.findQueryResultModifier(next.getName()) ;
			if(queryResultModifier!=null)
			{
				modifiedQueryResult = queryResultModifier.modifyQueryResult(modifiedQueryResult, dataSource, user, next.getProperties());
				next = next.getAttachment();
			}
			else
			{
				throw new ExecutionTaskException("[IQueryResultModifier] by id=[" + next.getName()  +"] not found");
			}
			
		}
		
		
		return modifiedQueryResult; 
	}
	
	protected InputStream executeSubmitPayloadModifierChain(String user, InputStream input , ModifierEntry modifierChain , SubmitEndpoint submitEndpoint) throws Exception
	{
		InputStream modifiedInput = input;
		ModifierEntry next = modifierChain;
		while(next!=null)
		{
			ISubmitPayloadModifier submitPayloadModifier = modifierRegistry.findSubmitPayloadModifier(next.getName()) ;
			if(submitPayloadModifier!=null)
			{
				modifiedInput = submitPayloadModifier.transformPayload(modifiedInput, submitEndpoint, next.getProperties());
				next = next.getAttachment();
			}
			else
			{
				throw new ExecutionTaskException("[ISubmitPayloadModifier] by id=[" + next.getName()  +"] not found");
			}
			
		}
		
		return modifiedInput; 
	}

	@Override
	public QueryResult executeSubmitEndpoint(String user, String data,
			Profile profile, SubmitEndpoint submitEndpoint)
			throws ExecutionTaskException {
		try{
			String finalData = executeSubmitPayloadModifierChain(user, data, submitEndpoint.getSubmitPayloadModifiers(), submitEndpoint);
			// execute handler
			
			IProvider provider = providerRegistry.lookupProvider(profile.getProviderId(), profile.getProviderVersion());
			ISubmitHandler submitHandler = provider.getSubmitHandler();
			QueryResult queryResult = submitHandler.submit(profile.getDataSource(), submitEndpoint.getProperties(), finalData);
			return queryResult;
			}
			catch (Exception e) {
				log.error("Execution Task failed" , e);
				throw new ExecutionTaskException(e);
			}
	}

	private String executeSubmitPayloadModifierChain(String user, String data,
			ModifierEntry submitPayloadModifiers , SubmitEndpoint submitEndpoint) throws Exception {

		String modifiedInput = data;
		ModifierEntry next = submitPayloadModifiers;
		while(next!=null)
		{
			ISubmitPayloadModifier submitPayloadModifier = modifierRegistry.findSubmitPayloadModifier(next.getName()) ;
			if(submitPayloadModifier!=null)
			{
				modifiedInput = submitPayloadModifier.transformPayload(modifiedInput, submitEndpoint, next.getProperties());
				next = next.getAttachment();
			}
			else
			{
				throw new ExecutionTaskException("[ISubmitPayloadModifier] by id=[" + next.getName()  +"] not found");
			}
			
		}
		
		return modifiedInput; 
	}
}
