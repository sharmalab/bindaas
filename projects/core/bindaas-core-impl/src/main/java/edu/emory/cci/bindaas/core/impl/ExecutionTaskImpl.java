package edu.emory.cci.bindaas.core.impl;

import java.io.InputStream;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.JsonObject;

import edu.emory.cci.bindaas.core.api.IExecutionTasks;
import edu.emory.cci.bindaas.core.api.IModifierRegistry;
import edu.emory.cci.bindaas.core.api.IProviderRegistry;
import edu.emory.cci.bindaas.core.api.IValidator;
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
import edu.emory.cci.bindaas.framework.model.RequestContext;
import edu.emory.cci.bindaas.framework.model.SubmitEndpoint;
import edu.emory.cci.bindaas.framework.provider.exception.AbstractHttpCodeException;
import edu.emory.cci.bindaas.framework.provider.exception.MandatoryQueryAttributeMissingException;

public class ExecutionTaskImpl implements IExecutionTasks {

	private IProviderRegistry providerRegistry;
	private IModifierRegistry modifierRegistry;
	private IValidator validator;
	private Log log = LogFactory.getLog(getClass());

	@Override
	public QueryResult executeQueryEndpoint(String user,
			Map<String, String> runtimeParameters, Profile profile,
			QueryEndpoint queryEndpoint) throws ExecutionTaskException, AbstractHttpCodeException {
		
		RequestContext requestContext = new RequestContext();
		requestContext.setUser(user);
		// construct real query
		
		Map<String, BindVariable> bindVariables = queryEndpoint
				.getBindVariables();
		String template = queryEndpoint.getQueryTemplate();

		try {
			// modify runtimeParams

			runtimeParameters = executeQueryParameterModifierChain(requestContext,
					runtimeParameters, queryEndpoint.getQueryModifiers(),
					profile.getDataSource());
			for (BindVariable bindVariable : bindVariables.values()) {
				String attrName = bindVariable.getName();
				String attrValue = runtimeParameters.get(attrName);

				if (!bindVariable.isRequired()) {
					if (attrValue == null) {
						attrValue = bindVariable.getDefaultValue();
					}

				} else if (attrValue == null) {
					throw new MandatoryQueryAttributeMissingException(attrName);
				}

				log.trace("Substituting [" + "$" + attrName + "$" + "] for ["
						+ attrValue + "] in the Query Template");

				template = template.replace("$" + attrName + "$", attrValue);
			}

			// execute queryModifier chain
			
			String finalQuery = executeQueryModifierChain(requestContext, template,
					queryEndpoint.getQueryModifiers(), profile.getDataSource());

			// execute handler

			IProvider provider = providerRegistry.lookupProvider(
					profile.getProviderId(), profile.getProviderVersion());
			IQueryHandler queryHandler = provider.getQueryHandler();

			

			QueryResult queryResult = queryHandler.query(
					profile.getDataSource(), queryEndpoint.getOutputFormat(),
					finalQuery , runtimeParameters, requestContext);

			// execute query result chain


			queryResult = executeQueryResultModifierChain(requestContext, queryResult,
					queryEndpoint.getQueryResultModifiers(),
					profile.getDataSource() , runtimeParameters);

			// render result
			return queryResult;

		} 
		catch(AbstractHttpCodeException e)
		{
			throw e;
		}
		catch (Exception e) {
			log.error("Execution Task failed", e);
			throw new ExecutionTaskException(e);
		} 
		
	}

	protected Map<String, String> executeQueryParameterModifierChain(
			RequestContext requestContext, Map<String, String> runtimeParameters,
			ModifierEntry queryModifiers, JsonObject dataSource)
			throws Exception {
		Map<String, String> modifiedParams = runtimeParameters;
		ModifierEntry next = queryModifiers;
		while (next != null) {
			IQueryModifier queryModifier = modifierRegistry
					.findQueryModifier(next.getName());
			if (queryModifier != null) {
				modifiedParams = queryModifier.modiftQueryParameters(
						modifiedParams, dataSource, requestContext, next.getProperties());
				next = next.getAttachment();
			} else {
				throw new ExecutionTaskException("[IQueryModifier] by id=["
						+ next.getName() + "] not found");
			}

		}

		return modifiedParams;
	}

	@Override
	public QueryResult executeDeleteEndpoint(String user,
			Map<String, String> runtimeParameters, Profile profile,
			DeleteEndpoint deleteEndpoint) throws ExecutionTaskException, AbstractHttpCodeException {
		
		RequestContext requestContext = new RequestContext();
		requestContext.setUser(user);
		
		// construct real query
		Map<String, BindVariable> bindVariables = deleteEndpoint
				.getBindVariables();
		String template = deleteEndpoint.getQueryTemplate();
		for (BindVariable bindVariable : bindVariables.values()) {
			String attrName = bindVariable.getName();
			String attrValue = runtimeParameters.get(attrName);

			if (!bindVariable.isRequired()) {
				if (attrValue == null) {
					attrValue = bindVariable.getDefaultValue();
				}

			} else if (attrValue == null) {
				throw new ExecutionTaskException("Mandatory attribute ["
						+ attrName + "] not provided");
			}

			log.trace("Substituting [" + "$" + attrName + "$" + "] for ["
					+ attrValue + "] in the Query Template");

			template = template.replace("$" + attrName + "$", attrValue);
		}

		// execute handler

		try {
			IProvider provider = providerRegistry.lookupProvider(
					profile.getProviderId(), profile.getProviderVersion());
			IDeleteHandler deleteHandler = provider.getDeleteHandler();

			QueryResult queryResult = deleteHandler.delete(
					profile.getDataSource(), template ,  runtimeParameters , requestContext);
			// render result
			return queryResult;

		} 
		catch(AbstractHttpCodeException e)
		{
			throw e;
		}
		catch (Exception e) {
			log.error("Execution Task failed", e);
			throw new ExecutionTaskException(e);
		} 

	}

	@Override
	public QueryResult executeSubmitEndpoint(String user, InputStream is,
			Profile profile, SubmitEndpoint submitEndpoint)
			throws ExecutionTaskException, AbstractHttpCodeException {
		
		
		try {
			RequestContext requestContext = new RequestContext();
			requestContext.setUser(user);
			
			InputStream finalStream = executeSubmitPayloadModifierChain(requestContext,
					is, submitEndpoint.getSubmitPayloadModifiers(),
					submitEndpoint);
			// execute handler

			IProvider provider = providerRegistry.lookupProvider(
					profile.getProviderId(), profile.getProviderVersion());
			ISubmitHandler submitHandler = provider.getSubmitHandler();

			QueryResult queryResult = submitHandler.submit(
					profile.getDataSource(), submitEndpoint.getProperties(),
					finalStream , requestContext );
			return queryResult;
		} 
		
		catch(AbstractHttpCodeException e)
		{
			throw e;
		}
		catch (Exception e) {
			log.error("Execution Task failed", e);
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

	protected String executeQueryModifierChain(RequestContext requestContext, String query,
			ModifierEntry modifierChain, JsonObject dataSource)
			throws Exception {
		String modifiedQuery = query;
		ModifierEntry next = modifierChain;
		while (next != null) {
			IQueryModifier queryModifier = modifierRegistry
					.findQueryModifier(next.getName());
			if (queryModifier != null) {
				modifiedQuery = queryModifier.modifyQuery(modifiedQuery,
						dataSource, requestContext, next.getProperties());
				next = next.getAttachment();
			} else {
				throw new ExecutionTaskException("[IQueryModifier] by id=["
						+ next.getName() + "] not found");
			}

		}

		return modifiedQuery;
	}

	protected QueryResult executeQueryResultModifierChain(RequestContext requestContext,
			QueryResult queryResult, ModifierEntry modifierChain,
			JsonObject dataSource , Map<String,String> queryParams) throws Exception {
		QueryResult modifiedQueryResult = queryResult;
		ModifierEntry next = modifierChain;
		while (next != null) {
			IQueryResultModifier queryResultModifier = modifierRegistry
					.findQueryResultModifier(next.getName());
			if (queryResultModifier != null) {
				modifiedQueryResult = queryResultModifier.modifyQueryResult(
						modifiedQueryResult, dataSource, requestContext,
						next.getProperties(), queryParams );
				next = next.getAttachment();
			} else {
				throw new ExecutionTaskException(
						"[IQueryResultModifier] by id=[" + next.getName()
								+ "] not found");
			}

		}

		return modifiedQueryResult;
	}

	protected InputStream executeSubmitPayloadModifierChain(RequestContext requestContext,
			InputStream input, ModifierEntry modifierChain,
			SubmitEndpoint submitEndpoint) throws Exception {
		InputStream modifiedInput = input;
		ModifierEntry next = modifierChain;
		while (next != null) {
			ISubmitPayloadModifier submitPayloadModifier = modifierRegistry
					.findSubmitPayloadModifier(next.getName());
			if (submitPayloadModifier != null) {
				modifiedInput = submitPayloadModifier.transformPayload(
						modifiedInput, submitEndpoint, next.getProperties() , requestContext);
				next = next.getAttachment();
			} else {
				throw new ExecutionTaskException(
						"[ISubmitPayloadModifier] by id=[" + next.getName()
								+ "] not found");
			}

		}

		return modifiedInput;
	}

	@Override
	public QueryResult executeSubmitEndpoint(String user, String data,
			Profile profile, SubmitEndpoint submitEndpoint)
			throws ExecutionTaskException, AbstractHttpCodeException {
		try {
			
			RequestContext requestContext = new RequestContext();
			requestContext.setUser(user);
			log.trace(data);
			
			String finalData = executeSubmitPayloadModifierChain(requestContext, data,
					submitEndpoint.getSubmitPayloadModifiers(), submitEndpoint);
			// execute handler

			IProvider provider = providerRegistry.lookupProvider(
					profile.getProviderId(), profile.getProviderVersion());
			ISubmitHandler submitHandler = provider.getSubmitHandler();
			QueryResult queryResult = submitHandler.submit(
					profile.getDataSource(), submitEndpoint.getProperties(),
					finalData , requestContext);
			return queryResult;
		} 
		catch(AbstractHttpCodeException e)
		{
			throw e;
		}
		catch (Exception e) {
			log.error("Execution Task failed", e);
			throw new ExecutionTaskException(e);
		}
	}

	private String executeSubmitPayloadModifierChain(RequestContext requestContext, String data,
			ModifierEntry submitPayloadModifiers, SubmitEndpoint submitEndpoint)
			throws Exception {

		String modifiedInput = data;
		ModifierEntry next = submitPayloadModifiers;
		while (next != null) {
			ISubmitPayloadModifier submitPayloadModifier = modifierRegistry
					.findSubmitPayloadModifier(next.getName());
			if (submitPayloadModifier != null) {
				modifiedInput = submitPayloadModifier.transformPayload(
						modifiedInput, submitEndpoint, next.getProperties() , requestContext);
				next = next.getAttachment();
			} else {
				throw new ExecutionTaskException(
						"[ISubmitPayloadModifier] by id=[" + next.getName()
								+ "] not found");
			}

		}

		return modifiedInput;
	}
}
