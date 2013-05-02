package edu.emory.cci.bindaas.core.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.helpers.IOUtils;

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
		
	}
	
	public synchronized List<Workspace> loadAllWorkspaces() throws Exception {
		List<Workspace> workspaceContents = new ArrayList<Workspace>();
		
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
			String content = readContent(workspaceFile);
			Workspace workspace = GSONUtil.getGSONInstance().fromJson(content, Workspace.class);
			workspaceContents.add(workspace);
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
		File workspaceFile = new File(metadataStoreDirectory.getAbsolutePath() +"/"+ workspaceName + fileExtension);
		if(workspaceFile.isFile() && workspaceFile.canRead())
		{
			return true;
		}
		else
		return false;
	}

	
	@Override
	public Workspace getWorkspace(String workspaceName) throws Exception {
		try{
		String content = loadWorkspaceByName(workspaceName);
		return GSONUtil.getGSONInstance().fromJson(content, Workspace.class);
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
	}
	
	@Override
	public synchronized void deleteWorkspace(String workspaceName) throws IOException {
		File workspaceFile = new File(metadataStoreDirectory.getAbsolutePath() +"/"+ workspaceName + fileExtension);
		workspaceFile.delete();
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
