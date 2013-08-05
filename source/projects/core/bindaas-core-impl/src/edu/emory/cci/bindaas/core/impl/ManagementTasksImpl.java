package edu.emory.cci.bindaas.core.impl;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.JsonObject;

import edu.emory.cci.bindaas.core.api.IManagementTasks;
import edu.emory.cci.bindaas.core.api.IPersistenceDriver;
import edu.emory.cci.bindaas.core.api.IProviderRegistry;
import edu.emory.cci.bindaas.core.api.IValidator;
import edu.emory.cci.bindaas.core.exception.DuplicateException;
import edu.emory.cci.bindaas.core.exception.FrameworkEntityException.Type;
import edu.emory.cci.bindaas.core.exception.NotFoundException;
import edu.emory.cci.bindaas.core.exception.ProviderNotFoundException;
import edu.emory.cci.bindaas.core.model.DeleteEndpointRequestParameter;
import edu.emory.cci.bindaas.core.model.EntityEventType;
import edu.emory.cci.bindaas.core.model.ProfileRequestParameter;
import edu.emory.cci.bindaas.core.model.QueryEndpointRequestParameter;
import edu.emory.cci.bindaas.core.model.SubmitEndpointRequestParameter;
import edu.emory.cci.bindaas.core.model.WorkspaceRequestParameter;
import edu.emory.cci.bindaas.core.util.EventHelper;
import edu.emory.cci.bindaas.framework.api.IProvider;
import edu.emory.cci.bindaas.framework.api.IQueryHandler;
import edu.emory.cci.bindaas.framework.api.ISubmitHandler;
import edu.emory.cci.bindaas.framework.model.DeleteEndpoint;
import edu.emory.cci.bindaas.framework.model.Profile;
import edu.emory.cci.bindaas.framework.model.QueryEndpoint;
import edu.emory.cci.bindaas.framework.model.Stage;
import edu.emory.cci.bindaas.framework.model.SubmitEndpoint;
import edu.emory.cci.bindaas.framework.model.Workspace;
import edu.emory.cci.bindaas.framework.util.GSONUtil;

public class ManagementTasksImpl implements IManagementTasks {

	private IProviderRegistry providerRegistry;
	private IPersistenceDriver persistenceDriver;
	private IValidator validator;
	private Log log = LogFactory.getLog(getClass());

	public IProviderRegistry getProviderRegistry() {
		return providerRegistry;
	}

	public void setProviderRegistry(IProviderRegistry providerRegistry) {
		this.providerRegistry = providerRegistry;
	}

	public IPersistenceDriver getPersistenceDriver() {
		return persistenceDriver;
	}

	public void setPersistenceDriver(IPersistenceDriver persistenceDriver) {
		this.persistenceDriver = persistenceDriver;
	}

	public IValidator getValidator() {
		return validator;
	}

	public void setValidator(IValidator validator) {
		this.validator = validator;
	}
	
	public void init() throws Exception
	{
//		loadWorkspaces();
	}

//	/**
//	 * thread-safe. no other thread should call any method on this object when this method is executing ...
//	 * @throws IOException
//	 */
//	public synchronized void loadWorkspaces() throws IOException
//	{
//		log.debug("Loading All workspaces");
//		workspaces = new HashMap<String, Workspace>();
//		List<String> loadedContent = persistenceDriver.loadAllWorkspaces();
//		for(String content : loadedContent)
//		{
//			try {
//					Workspace workspace = GSONUtil.getGSONInstance().fromJson(content, Workspace.class);
//					workspaces.put(workspace.getName(), workspace);
//					log.trace("Workspace Loaded\n" + workspace);
//			}
//			catch(Exception e)
//			{
//				log.error(e);
//			}
//			
//			
//		}
//		
//	}
	/**
	 * Will deprecate this method as potentially dangerous. 
	 * @throws IOException
	 */
	
//	private void updateAllWorkspaces() throws IOException
//	{
//		log.debug("Updating All Workspaces");
//		for(Workspace workspace : workspaces.values())
//		{
//			persistenceDriver.createOrUpdateWorkspace(workspace.getName(), workspace.toString());
//			log.trace("Created/Updated Workspace\n" + workspace);
//		}
//	}
	
	

	@Override
	public  Workspace createWorkspace(String name, JsonObject parameters,
			String createdBy) throws Exception {
		
			synchronized (persistenceDriver) {
				if(persistenceDriver.doesExist(name) == false)
				{
					Workspace workspace = new Workspace();
					workspace.setName(name);
					workspace.setCreatedBy(createdBy);
					
					WorkspaceRequestParameter werp = GSONUtil.getGSONInstance().fromJson(parameters, WorkspaceRequestParameter.class);
					
					if(werp!=null)
					{
						workspace = werp.getWorkspace(workspace);
					}
					else
					{
						throw new Exception("Workspace request cannot be deserialized");
					}
					
					workspace.validate();
					persistenceDriver.saveWorkspace(workspace);
					log.debug("Workspace created\n" + workspace);
					EventHelper.createEntityEvent(workspace, EntityEventType.CREATE).emitAsynchronously();
					return workspace;
				}
				else
					throw new DuplicateException(name, Type.Workspace);	
			}
				
		}
		
		
	

	@Override
	public  Profile createProfile(String name, String workspaceName,
			JsonObject parameters, String createdBy , String description) throws Exception {
		
		// locate Workspace
			Workspace workspace = getWorkspace(workspaceName);
			synchronized (persistenceDriver) {
				if(workspace.getProfiles().containsKey(name) == false)
				{
					
					Profile profile = new Profile();
					profile.setCreatedBy(createdBy);
					profile.setName(name);
					profile.setDescription(description);
					
					// locate provider
					
					ProfileRequestParameter perp = GSONUtil.getGSONInstance().fromJson(parameters, ProfileRequestParameter.class);
					
					if(perp!=null)
					{
						try{
							profile = perp.getProfile(profile);
							
							String providerId = profile.getProviderId();
							int providerVersion = profile.getProviderVersion();
							
							log.debug("Locating Provider id=[" + providerId + "] and version=[" + providerVersion + "]");
							IProvider provider = providerRegistry.lookupProvider(providerId, providerVersion);
							if(provider!=null)
							{
								log.debug("Provider found. Performing validation and initialization");
								profile = provider.validateAndInitializeProfile(profile);
								profile.validate();
								persistenceDriver.saveProfile(workspaceName, profile);
								EventHelper.createEntityEvent(profile, EntityEventType.CREATE).emitAsynchronously();
								return profile;
								
							}
							else
							{
								throw new ProviderNotFoundException(providerId,providerVersion);
							}
							
						}
						catch(NullPointerException e)
						{
							throw new IllegalArgumentException("providerId and/or providerVersion don't have correct values" + parameters);
						}
						catch(NumberFormatException e)
						{
							throw new IllegalArgumentException("providerId and/or providerVersion don't have correct values" + parameters);
						}
												
					}
					else
					{
						throw new IllegalArgumentException("providerId and/or providerVersion not specified in  " + parameters);
					}
				}
				else
				{
						throw new DuplicateException(name, Type.Profile);
				}

			}
			
				
		}
		

	@Override
	public QueryEndpoint createQueryEndpoint(String name, String workspaceName,
			String profileName, JsonObject parameters, String createdBy)
			throws Exception {
		
		synchronized (persistenceDriver) {
			// locate provider
			Profile profile = getProfile(workspaceName, profileName);
			if(profile.getQueryEndpoints().containsKey(name) == false)
			{
				try{
					String providerId = profile.getProviderId();
					int providerVersion = profile.getProviderVersion();
					log.debug("Locating Provider id=[" + providerId + "] and version=[" + providerVersion + "]");
					IProvider provider = providerRegistry.lookupProvider(providerId, providerVersion);
					if(provider!=null)
					{
						IQueryHandler queryHandler = provider.getQueryHandler();
						
						if(queryHandler!=null)
						{
						
							QueryEndpoint queryEndpoint = new QueryEndpoint();
							queryEndpoint.setName(name);
							queryEndpoint.setCreatedBy(createdBy);
							queryEndpoint.setStage(Stage.UNVERIFIED);
						
							queryEndpoint = constructQueryEndpointFromProps(queryEndpoint, parameters);
						
						
							log.debug("Provider found. Extracting QueryHandler");
							
							// perform framework validation
							validator.validateQueryModifierRequestChain(queryEndpoint.getQueryModifiers());
							validator.validateQueryResultModifierRequestChain(queryEndpoint.getQueryResultModifiers());
							// perform provider validation
							queryEndpoint = queryHandler.validateAndInitializeQueryEndpoint(queryEndpoint);
							queryEndpoint.validate();
							// register queryEndpoint
							persistenceDriver.saveQueryEndpoint(workspaceName, profileName, queryEndpoint);
							EventHelper.createEntityEvent(queryEndpoint, EntityEventType.CREATE).emitAsynchronously();
							return queryEndpoint;	
						}
						else
						{
							throw new Exception("Query Functionality not available from provider [" + providerId + "] version=[" + providerVersion  +"]");
						}
						
						
					}
					else
					{
						throw new ProviderNotFoundException(providerId,providerVersion);
					}
					
				}
				catch(NullPointerException e)
				{
					throw new IllegalArgumentException("providerId and/or providerVersion don't have correct values" + parameters);
				}
				catch(NumberFormatException e)
				{
					throw new IllegalArgumentException("providerId and/or providerVersion don't have correct values" + parameters);
				}
				
				
				
			}
			else
			{
				throw new DuplicateException(name, Type.QueryEndpoint);
			}

			
		}
		
	}

	
	private QueryEndpoint constructQueryEndpointFromProps(QueryEndpoint queryEndpoint , JsonObject parameters) throws Exception {
		
		QueryEndpointRequestParameter qerp = GSONUtil.getGSONInstance().fromJson(parameters, QueryEndpointRequestParameter.class);
		queryEndpoint = qerp.getQueryEndpoint(queryEndpoint);
		return queryEndpoint;
	}

	@Override
	public DeleteEndpoint createDeleteEndpoint(String name,
			String workspaceName, String profileName, JsonObject parameters,
			String createdBy) throws Exception {

		
		
		synchronized (persistenceDriver) {
			Profile profile = getProfile(workspaceName, profileName);
			// locate provider
			if(profile.getDeleteEndpoints().containsKey(name) == false)
			{
				try{
					String providerId = profile.getProviderId();
					int providerVersion = profile.getProviderVersion();
					
					log.debug("Locating Provider id=[" + providerId + "] and version=[" + providerVersion + "]");
					IProvider provider = providerRegistry.lookupProvider(providerId, providerVersion);
					if(provider!=null)
					{
						log.debug("Provider found. Extracting DeleteHandler");
						if(provider.getDeleteHandler()!=null)
						{
							DeleteEndpoint deleteEndpoint = new DeleteEndpoint();
							deleteEndpoint.setName(name);
							deleteEndpoint.setCreatedBy(createdBy);
							deleteEndpoint.setStage(Stage.UNVERIFIED);
							
							deleteEndpoint = constructDeleteEndpointFromProps(deleteEndpoint, parameters);
							deleteEndpoint.validate();
							persistenceDriver.saveDeleteEndpoint(workspaceName, profileName, deleteEndpoint);
							EventHelper.createEntityEvent(deleteEndpoint, EntityEventType.CREATE).emitAsynchronously();
							return deleteEndpoint;
						}
						else
						{
							throw new Exception("Delete Functionality not available from provider [" + providerId + "] version=[" + providerVersion  +"]");
						}
						
						
						
					}
					else
					{
						throw new ProviderNotFoundException(providerId,providerVersion);
					}
					
				}
				catch(NullPointerException e)
				{
					throw new IllegalArgumentException("providerId and/or providerVersion don't have correct values" + parameters);
				}
				catch(NumberFormatException e)
				{
					throw new IllegalArgumentException("providerId and/or providerVersion don't have correct values" + parameters);
				}
				
				
				
			}
			else
			{
				throw new DuplicateException(name, Type.DeleteEndpoint);
			}

		}
		

					
	}

	private DeleteEndpoint constructDeleteEndpointFromProps(DeleteEndpoint deleteEndpoint ,
			JsonObject parameters) throws Exception {
		
		DeleteEndpointRequestParameter derp = GSONUtil.getGSONInstance().fromJson(parameters, DeleteEndpointRequestParameter.class);
		deleteEndpoint = derp.getDeleteEndpoint(deleteEndpoint);
		return deleteEndpoint;
	}

	@Override
	public SubmitEndpoint createSubmitEndpoint(String name,
			String workspaceName, String profileName, JsonObject parameters,
			String createdBy) throws Exception {
		
		
		
		
		synchronized (persistenceDriver) {
			Profile profile = getProfile(workspaceName, profileName);
			// locate provider
			if(profile.getSubmitEndpoints().containsKey(name) == false)
			{
				try{
					String providerId = profile.getProviderId();
					int providerVersion = profile.getProviderVersion();
					
					log.debug("Locating Provider id=[" + providerId + "] and version=[" + providerVersion + "]");
					IProvider provider = providerRegistry.lookupProvider(providerId, providerVersion);
					if(provider!=null)
					{
						ISubmitHandler submitHandler = provider.getSubmitHandler();
						
						if(submitHandler!=null)
						{
							SubmitEndpoint submitEndpoint = new SubmitEndpoint();
							submitEndpoint.setName(name);
							submitEndpoint.setCreatedBy(createdBy);
							submitEndpoint = constructSubmitEndpointFromProps(submitEndpoint, parameters);
							
							// perform framework validation
							
							validator.validateSubmitPayloadModifierRequestChain(submitEndpoint.getSubmitPayloadModifiers());

							// perform provider validation
							submitEndpoint = submitHandler.validateAndInitializeSubmitEndpoint(submitEndpoint);
							submitEndpoint.validate();
							persistenceDriver.saveSubmitEndpoint(workspaceName, profileName, submitEndpoint);
							EventHelper.createEntityEvent(submitEndpoint, EntityEventType.CREATE).emitAsynchronously();
							return submitEndpoint;
						}
						else
						{
							throw new Exception("Submit Functionality not available from provider [" + providerId + "] version=[" + providerVersion  +"]");
						}
						
					}
					else
					{
						throw new ProviderNotFoundException(providerId,providerVersion);
					}
					
				}
				catch(NullPointerException e)
				{
					throw new IllegalArgumentException("providerId and/or providerVersion don't have correct values" + parameters);
				}
				catch(NumberFormatException e)
				{
					throw new IllegalArgumentException("providerId and/or providerVersion don't have correct values" + parameters);
				}				
				
			}
			else
			{
				throw new DuplicateException(name, Type.SubmitEndpoint);
			}

		}
		
					
	}

	private SubmitEndpoint constructSubmitEndpointFromProps(SubmitEndpoint submitEndpoint,
			JsonObject parameters) throws Exception {

		SubmitEndpointRequestParameter serp = GSONUtil.getGSONInstance().fromJson(parameters, SubmitEndpointRequestParameter.class);
		submitEndpoint = serp.getSubmitEndpoint(submitEndpoint);
		return submitEndpoint;
	}
	

	@Override
	public void deleteWorkspace(String workspaceName) throws Exception {
		// locate Workspace
		persistenceDriver.deleteWorkspace(workspaceName);
		EventHelper.createDeleteWorkspaceEvent(workspaceName).emitAsynchronously();
	}

	///-----
	@Override
	public void deleteProfile(String workspaceName, String profileName)
			throws Exception {
		persistenceDriver.deleteProfile(workspaceName, profileName);
		EventHelper.createDeleteProfileEvent(workspaceName, profileName).emitAsynchronously();
	}
	

	@Override
	public QueryEndpoint deleteQueryEndpoint(String workspaceName,
			String profileName, String queryEndpointName) throws Exception {
		
		synchronized (persistenceDriver) {
			Profile profile = getProfile(workspaceName, profileName);	
			
			if(profile.getQueryEndpoints().containsKey(queryEndpointName))
			{
				QueryEndpoint queryEndpoint = profile.getQueryEndpoints().get(queryEndpointName);
				persistenceDriver.deleteQueryEndpoint(workspaceName, profileName, queryEndpointName);
				EventHelper.createDeleteQueryEndpointEvent(workspaceName, profileName, queryEndpointName).emitAsynchronously();
				return queryEndpoint;
			}
			else
				return null;
//				throw new NotFoundException(queryEndpointName, Type.QueryEndpoint); // TODO : Need to decide on the behaviour. To throw exception when query not found or not	
		}
		
	}

	
	@Override
	public DeleteEndpoint deleteDeleteEndpoint(String workspaceName,
			String profileName, String deleteEndpointName) throws Exception {
		
		synchronized (persistenceDriver) {
			Profile profile = getProfile(workspaceName, profileName);	
			
			if(profile.getDeleteEndpoints().containsKey(deleteEndpointName))
			{
				DeleteEndpoint deleteEndpoint = profile.getDeleteEndpoints().get(deleteEndpointName);
				persistenceDriver.deleteDeleteEndpoint(workspaceName, profileName, deleteEndpointName);
				EventHelper.createDeleteDeleteEndpointEvent(workspaceName, profileName, deleteEndpointName).emitAsynchronously();
				return deleteEndpoint;
			}
			else
				return null;
//			else
//				throw new NotFoundException(deleteEndpointName, Type.DeleteEndpoint);	
		}	
	}
	

	@Override
	public SubmitEndpoint deleteSubmitEndpoint(String workspaceName,
			String profileName, String submitEndpointName) throws Exception {
		synchronized (persistenceDriver) {
			Profile profile = getProfile(workspaceName, profileName);	
			
			if(profile.getSubmitEndpoints().containsKey(submitEndpointName))
			{
				SubmitEndpoint submitEndpoint = profile.getSubmitEndpoints().get(submitEndpointName);
				persistenceDriver.deleteSubmitEndpoint(workspaceName, profileName, submitEndpointName);
				EventHelper.createDeleteSubmitEndpointEvent(workspaceName, profileName, submitEndpointName).emitAsynchronously();
				return submitEndpoint;
			}
			else
				return null;
//			else
//				throw new NotFoundException(submitEndpointName, Type.SubmitEndpoint);	
		}
	}

	@Override
	public Profile updateProfile(String profileName, String workspaceName,
			JsonObject parameters, String updatedBy , String description) throws Exception {
		
		synchronized (persistenceDriver) {
			Profile oldProfile = getProfile(workspaceName, profileName);
			deleteProfile(workspaceName, profileName);
			try{
			Profile profile = this.createProfile(profileName, workspaceName, parameters, updatedBy , description);
			profile.setQueryEndpoints(oldProfile.getQueryEndpoints());
			profile.setDeleteEndpoints(oldProfile.getDeleteEndpoints());
			profile.setSubmitEndpoints(oldProfile.getSubmitEndpoints());
			persistenceDriver.saveProfile(workspaceName, profile);
			EventHelper.createEntityEvent(profile, EntityEventType.UPDATE).emitAsynchronously();
			return profile;
			}
			catch(Exception e)
			{
				log.error(e);
				log.debug("Restoring old profile");
				if(oldProfile!=null)
					persistenceDriver.saveProfile(workspaceName, oldProfile);
				throw e;
			}
			
				
		}
		
	}

	@Override
	public QueryEndpoint updateQueryEndpoint(String queryEndpointName, String workspaceName,
			String profileName, JsonObject parameters, String updatedBy)
			throws Exception {
		
		synchronized (persistenceDriver) {
			QueryEndpoint oldQueryEndpoint = deleteQueryEndpoint(workspaceName, profileName, queryEndpointName);
			try{
				QueryEndpoint queryEndpoint = this.createQueryEndpoint(queryEndpointName, workspaceName, profileName, parameters, updatedBy);
				EventHelper.createEntityEvent(queryEndpoint, EntityEventType.UPDATE).emitAsynchronously();
				return queryEndpoint;
			}
			catch(Exception e)
			{
				log.error(e);
				log.debug("Restoring old QueryEndpoint");
				if(oldQueryEndpoint!=null)
					persistenceDriver.saveQueryEndpoint(workspaceName, profileName, oldQueryEndpoint);
				throw e;
			}
			
			
		}
		
	}

	@Override
	public DeleteEndpoint updateDeleteEndpoint(String deleteEndpointName,
			String workspaceName, String profileName, JsonObject parameters,
			String updatedBy) throws Exception {
		
		synchronized (persistenceDriver) {
			DeleteEndpoint oldDeleteEndpoint = deleteDeleteEndpoint(workspaceName, profileName, deleteEndpointName);
			try{
				DeleteEndpoint deleteEndpoint = this.createDeleteEndpoint(deleteEndpointName, workspaceName, profileName, parameters, updatedBy);
				EventHelper.createEntityEvent(deleteEndpoint, EntityEventType.UPDATE).emitAsynchronously();
				return deleteEndpoint;
			}
			catch(Exception e)
			{
				log.error(e);
				log.debug("Restoring old DeleteEndpoint");
				if(oldDeleteEndpoint!=null)
					persistenceDriver.saveDeleteEndpoint(workspaceName, profileName, oldDeleteEndpoint);
				throw e;
			}
	
		}
		
	}

	@Override
	public SubmitEndpoint updateSubmitEndpoint(String submitEndpointName,
			String workspaceName, String profileName, JsonObject parameters,
			String updatedBy) throws Exception {
		
		synchronized (persistenceDriver) {
			SubmitEndpoint oldSubmitEndpoint = deleteSubmitEndpoint(workspaceName, profileName, submitEndpointName);
			try{
				SubmitEndpoint submitEndpoint = this.createSubmitEndpoint(submitEndpointName, workspaceName, profileName, parameters, updatedBy);
				EventHelper.createEntityEvent(submitEndpoint, EntityEventType.UPDATE).emitAsynchronously();
				return submitEndpoint;
			}
			catch(Exception e)
			{
				log.error(e);
				log.debug("Restoring old SubmitEndpoint");
				if(oldSubmitEndpoint!=null)
					persistenceDriver.saveSubmitEndpoint(workspaceName, profileName, oldSubmitEndpoint);
				throw e;
			}
				
		}
		
	}

	@Override
	public Workspace getWorkspace(String workspaceName) throws Exception {
			Workspace workspace = persistenceDriver.getWorkspace(workspaceName);
			workspace.validate();
			return workspace;	
		}
		
		
	

	@Override
	public Profile getProfile(String workspaceName, String profileName)
			throws Exception {
			Workspace workspace = getWorkspace(workspaceName);
			Profile profile = workspace.getProfiles().get(profileName);
			if(profile!=null)
			{
				validateProfile(profile);
				return profile;
			}
			else
			{
				throw new NotFoundException(profileName, Type.Profile);
			}
		}
		
		

	@Override
	public QueryEndpoint getQueryEndpoint(String workspaceName,
		String profileName, String queryEndpointName) throws Exception {
		Profile profile = getProfile(workspaceName, profileName);
		
		
			QueryEndpoint queryEndpoint = profile.getQueryEndpoints().get(queryEndpointName);
			if(queryEndpoint!=null)
			{
				queryEndpoint.validate();
				return queryEndpoint;
			}
				
			else
				throw new NotFoundException(queryEndpointName, Type.QueryEndpoint);	
		}
		
		
	

	@Override
	public DeleteEndpoint getDeleteEndpoint(String workspaceName,
			String profileName, String deleteEndpointName) throws Exception {
		Profile profile = getProfile(workspaceName, profileName);
	
			DeleteEndpoint deleteEndpoint = profile.getDeleteEndpoints().get(deleteEndpointName);
			if(deleteEndpoint!=null)
			{
				deleteEndpoint.validate();
				return deleteEndpoint;
			}
				
			else
				throw new NotFoundException(deleteEndpointName, Type.DeleteEndpoint);
		}
	

	@Override
	public SubmitEndpoint getSubmitEndpoint(String workspaceName,
			String profileName, String submitEndpointName) throws Exception {
		Profile profile = getProfile(workspaceName, profileName);
	
			SubmitEndpoint submitEndpoint = profile.getSubmitEndpoints().get(submitEndpointName);
			if(submitEndpoint!=null)
			{
				submitEndpoint.validate();
				return submitEndpoint;
			}
				
			else
				throw new NotFoundException(submitEndpointName, Type.SubmitEndpoint);

		}
	

	@Override
	public void publishQueryEndpoint(String workspaceName, String profileName,
			String queryEndpointName) throws Exception {
		
		synchronized (persistenceDriver) {
			QueryEndpoint queryEndpoint = getQueryEndpoint(workspaceName, profileName, queryEndpointName);
			queryEndpoint.setStage(Stage.VERIFIED);
			persistenceDriver.saveQueryEndpoint(workspaceName, profileName, queryEndpoint);
		}
		
		
				
		
	}

	@Override
	public void publishDeleteEndpoint(String workspaceName, String profileName,
			String deleteEndpointName) throws Exception {
		synchronized (persistenceDriver) {
			DeleteEndpoint deleteEndpoint = getDeleteEndpoint(workspaceName, profileName, deleteEndpointName);
			deleteEndpoint.setStage(Stage.VERIFIED);
			persistenceDriver.saveDeleteEndpoint(workspaceName, profileName, deleteEndpoint);
		}
		
	}

	@Override
	public Collection<Workspace> listWorkspaces() throws Exception {
		List<Workspace> listOfWorkspaces = persistenceDriver.loadAllWorkspaces();
		
		Iterator<Workspace> iterator = listOfWorkspaces.iterator();
		while(iterator.hasNext())
		{
			Workspace workspace = iterator.next();
			
			try{
				validateWorkspace(workspace);
			}
			catch(Exception e)
			{
				log.error("Failed to load workspace [" + workspace + "]", e);
				iterator.remove();
			}	
			
		}
		
		return listOfWorkspaces;
	}
	
	private void validateWorkspace(Workspace workspace) throws Exception
	{
		workspace.validate();
		Iterator<Entry<String,Profile>> iterator = workspace.getProfiles().entrySet().iterator();
		while(iterator.hasNext())
		{
			Profile profile = iterator.next().getValue();
			
			try{
				validateProfile(profile);
			}
			catch(Exception e)
			{
				log.error("Failed to profile [" + profile + "] from workspace [" + workspace.getName() + "]", e);
				iterator.remove();
			}	
			
		}
		
	}
	
	private void validateProfile(Profile profile) throws Exception
	{
		profile.validate();
		
		Iterator<Entry<String,QueryEndpoint>> qeIterator = profile.getQueryEndpoints().entrySet().iterator();
		while(qeIterator.hasNext())
		{
			QueryEndpoint qe = qeIterator.next().getValue();
			try {
				qe.validate();
			}
			catch(Exception e)
			{
				log.error("Failed to QueryEndpoint [" + qe + "] from profile [" + profile.getName() + "]", e);
				qeIterator.remove();
			}
		}
		
		Iterator<Entry<String,SubmitEndpoint>> seIterator = profile.getSubmitEndpoints().entrySet().iterator();
		while(seIterator.hasNext())
		{
			SubmitEndpoint se = seIterator.next().getValue();
			try {
				se.validate();
			}
			catch(Exception e)
			{
				log.error("Failed to SubmitEndpoint [" + se + "] from profile [" + profile.getName() + "]", e);
				qeIterator.remove();
			}
		}
		
		Iterator<Entry<String,DeleteEndpoint>> deIterator = profile.getDeleteEndpoints().entrySet().iterator();
		while(deIterator.hasNext())
		{
			DeleteEndpoint de = deIterator.next().getValue();
			try {
				de.validate();
			}
			catch(Exception e)
			{
				log.error("Failed to DeleteEndpoint [" + de + "] from profile [" + profile.getName() + "]", e);
				qeIterator.remove();
			}
		}
		
		
	}
	
	

}
