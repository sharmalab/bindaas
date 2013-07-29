package edu.emory.cci.bindaas.core.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.helpers.IOUtils;

import com.google.common.annotations.GwtIncompatible;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;

import edu.emory.cci.bindaas.core.api.IPersistenceDriver;
import edu.emory.cci.bindaas.core.exception.FrameworkEntityException.Type;
import edu.emory.cci.bindaas.core.exception.NotFoundException;
import edu.emory.cci.bindaas.framework.model.DeleteEndpoint;
import edu.emory.cci.bindaas.framework.model.Profile;
import edu.emory.cci.bindaas.framework.model.QueryEndpoint;
import edu.emory.cci.bindaas.framework.model.SubmitEndpoint;
import edu.emory.cci.bindaas.framework.model.Workspace;
import edu.emory.cci.bindaas.framework.util.GSONUtil;

public class FileSystemPersistenceDriverImpl implements IPersistenceDriver {

	private String metadataStore;
	private File metadataStoreDirectory;
	private String fileExtension;
	
	private LoadingCache<String, Workspace> listOfWorkspaces;
	private Set<String> setOfDiscoveredWorkspaces;
	private final static Integer POLL_FREQUENCY = 30 * 1000 ; // 10 seconds
	
	public String getFileExtension() {
		return fileExtension;
	}

	public void setFileExtension(String fileExtension) {
		this.fileExtension = fileExtension;
	}
	
	private Log log = LogFactory.getLog(getClass());
	
	public void init() throws Exception
	{
		if(metadataStore!=null)
		{
			metadataStoreDirectory = new File(metadataStore);
			if(!metadataStoreDirectory.isDirectory())
			{
				log.info("Metadata Store does not exist. Creating a new one");
				metadataStoreDirectory.mkdirs();
			}
		}
		else
		{
			throw new Exception("Metadata Store Directory not set");
		}
		
		
		initCache();
	}
	
	public void initCache() throws Exception
	{
		setOfDiscoveredWorkspaces = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
		
		Runnable updateDiscoveredWorkspacesTask = new Runnable()
		{
			private Log log = LogFactory.getLog(FileSystemPersistenceDriverImpl.class);
			@Override
			public void run() {
				
				while(true)
				{
					File[] listOfWorkspaces = metadataStoreDirectory.listFiles(
							
							new FilenameFilter() {
								
								@Override
								public boolean accept(File arg0, String filename) {
									if(filename.endsWith(fileExtension))
										return true;
									else
										return false;
								}
							}
							);
					
					for(File workspaceFile : listOfWorkspaces)
					{
						setOfDiscoveredWorkspaces.add(workspaceFile.getName().replace(fileExtension, ""));
					}
					
					try {
						Thread.sleep(POLL_FREQUENCY);
					} catch (InterruptedException e) {
						log.error("Project-Scanner-Thread was interrupted while sleeping");
					}
				}
			}
			
		};
		
		Thread t = new Thread(updateDiscoveredWorkspacesTask,"Project-Scanner-Thread");
		t.start();
		
		
		
		listOfWorkspaces = CacheBuilder.newBuilder().maximumSize(1000).refreshAfterWrite(10, TimeUnit.SECONDS).build(
				new CacheLoader<String, Workspace>() {

					@Override
					public Workspace load(String workspace) throws Exception {
						
						String content = loadWorkspaceByName(workspace);
						return GSONUtil.getGSONInstance().fromJson(content, Workspace.class);
					
					}

					@Override
					@GwtIncompatible("Futures")
					public ListenableFuture<Workspace> reload(final String workspaceName,
							Workspace oldValue) throws Exception {
						
						ListenableFutureTask<Workspace> task = ListenableFutureTask.create(new Callable<Workspace>() {
			                   public Workspace call() throws IOException {
			                	   	String content = loadWorkspaceByName(workspaceName);
									Workspace workspace =  GSONUtil.getGSONInstance().fromJson(content, Workspace.class);
									return workspace;
			                   }
			                 });
						
						return task;
					}
					
				}
				
				);
	}
	
	
	
	public synchronized List<Workspace> loadAllWorkspaces() throws Exception {
		List<Workspace> workspaceContents = new ArrayList<Workspace>();
		
		
		for(String workspaceName : setOfDiscoveredWorkspaces)
		{
			try{
					Workspace workspace = listOfWorkspaces.get(workspaceName);
					workspaceContents.add(workspace);
			}
			catch(Exception e)
			{
				log.error("Failed to load project [" + workspaceName + "]" , e);
			}
		}
		
		return workspaceContents;
	}


	public  String loadWorkspaceByName(String name) throws IOException {
		File workspaceFile = new File(metadataStoreDirectory.getAbsolutePath() +"/"+ name + fileExtension);
		if(workspaceFile.isFile() && workspaceFile.canRead())
		{
			return readContent(workspaceFile);
		}
		else throw new IOException("Workspace metadata not found. name=[" + name + "]");
		
	}

	
	public String getMetadataStore() {
		return metadataStore;
	}
	public void setMetadataStore(String metadataStore) {
		this.metadataStore = metadataStore;
	}
	private String readContent(File file) throws IOException
	{
		StringWriter sw = new StringWriter();
		IOUtils.copy(new FileReader(file), sw , 2048);
		return sw.toString();
		
	}
	private  void writeToFile(File file,String content) throws IOException
	{
		FileOutputStream fos = new FileOutputStream(file);
		ByteArrayInputStream bis = new ByteArrayInputStream(content.getBytes());
		IOUtils.copyAndCloseInput(bis, fos);
	}
	

	@Override
	public synchronized boolean doesExist(String workspaceName) {
		try {
			
			Workspace workspace = listOfWorkspaces.get(workspaceName);
			if(workspace!=null) 
				return true ;
			else
				return false;
		}catch(Exception e)
		{
			return false;
		}
	}

	
	@Override
	public Workspace getWorkspace(String workspaceName) throws Exception {
		try{
			 return listOfWorkspaces.get(workspaceName);
		}
		catch(Exception e)
		{
			log.error(e);
			throw new NotFoundException(workspaceName, Type.Workspace);
		}
		
	}
	
	@Override
	public synchronized void saveWorkspace(Workspace workspace) throws IOException {
		File workspaceFile = new File(metadataStoreDirectory.getAbsolutePath() +"/"+ workspace.getName() + fileExtension);
		writeToFile(workspaceFile, workspace.toString());
		listOfWorkspaces.invalidateAll();
		setOfDiscoveredWorkspaces.add(workspace.getName());
	}
	
	@Override
	public synchronized void deleteWorkspace(String workspaceName) throws IOException {
		File workspaceFile = new File(metadataStoreDirectory.getAbsolutePath() +"/"+ workspaceName + fileExtension);
		workspaceFile.delete();
		setOfDiscoveredWorkspaces.remove(workspaceName);
	}
	@Override
	public synchronized void saveProfile(String workspaceName, Profile profile)
			throws Exception {
		Workspace workspace = getWorkspace(workspaceName);
		workspace.getProfiles().put(profile.getName(), profile);
		saveWorkspace(workspace);
		
	}
	@Override
	public synchronized void deleteProfile(String workspaceName, String profileName)
			throws Exception {
		Workspace workspace = getWorkspace(workspaceName);
		workspace.getProfiles().remove(profileName);
		saveWorkspace(workspace);
	}
	@Override
	public synchronized void saveQueryEndpoint(String workspaceName, String profileName,
			QueryEndpoint queryEndpoint) throws Exception {
		Workspace workspace = getWorkspace(workspaceName);
		if(workspace.getProfiles().containsKey(profileName))
		{
			workspace.getProfiles().get(profileName).getQueryEndpoints().put(queryEndpoint.getName(), queryEndpoint);
			saveWorkspace(workspace);
		}
		else
			throw new NotFoundException(profileName, Type.Profile);
	}
	
	@Override
	public synchronized void deleteQueryEndpoint(String workspaceName,
			String profileName, String queryEndpointName) throws Exception {
		Workspace workspace = getWorkspace(workspaceName);
		if(workspace.getProfiles().containsKey(profileName))
		{
			workspace.getProfiles().get(profileName).getQueryEndpoints().remove(queryEndpointName);
			saveWorkspace(workspace);
		}
		else
			throw new NotFoundException(profileName, Type.Profile);
	}
	@Override
	public synchronized void saveSubmitEndpoint(String workspaceName, String profileName,
			SubmitEndpoint submitEndpoint) throws Exception {
		Workspace workspace = getWorkspace(workspaceName);
		if(workspace.getProfiles().containsKey(profileName))
		{
			workspace.getProfiles().get(profileName).getSubmitEndpoints().put(submitEndpoint.getName()	,  submitEndpoint);
			saveWorkspace(workspace);
		}
		else
			throw new NotFoundException(profileName, Type.Profile);
	
		
	}
	@Override
	public synchronized void deleteSubmitEndpoint(String workspaceName,
			String profileName, String submitEndpointName) throws Exception {
		Workspace workspace = getWorkspace(workspaceName);
		if(workspace.getProfiles().containsKey(profileName))
		{
			workspace.getProfiles().get(profileName).getSubmitEndpoints().remove(submitEndpointName);
			saveWorkspace(workspace);
		}
		else
			throw new NotFoundException(profileName, Type.Profile);
	
	
	}
	@Override
	public synchronized void saveDeleteEndpoint(String workspaceName, String profileName,
			DeleteEndpoint deleteEndpoint) throws Exception {
		Workspace workspace = getWorkspace(workspaceName);
		if(workspace.getProfiles().containsKey(profileName))
		{
			workspace.getProfiles().get(profileName).getDeleteEndpoints().put(deleteEndpoint.getName(),deleteEndpoint);
			saveWorkspace(workspace);
		}
		else
			throw new NotFoundException(profileName, Type.Profile);
	
		
	}
	@Override
	public synchronized void deleteDeleteEndpoint(String workspaceName,
			String profileName, String deleteEndpointName) throws Exception {
		Workspace workspace = getWorkspace(workspaceName);
		if(workspace.getProfiles().containsKey(profileName))
		{
			workspace.getProfiles().get(profileName).getDeleteEndpoints().remove(deleteEndpointName);
			saveWorkspace(workspace);
		}
		else
			throw new NotFoundException(profileName, Type.Profile);
	
	}


}
