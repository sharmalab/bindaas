package edu.emory.cci.bindaas.core.api;

import java.util.List;

import edu.emory.cci.bindaas.framework.model.DeleteEndpoint;
import edu.emory.cci.bindaas.framework.model.Profile;
import edu.emory.cci.bindaas.framework.model.QueryEndpoint;
import edu.emory.cci.bindaas.framework.model.SubmitEndpoint;
import edu.emory.cci.bindaas.framework.model.Workspace;

public interface IPersistenceDriver {
	
	/**
	 * New API to store/load/update/delete entities
	 * synchronized
	 */
	public List<Workspace> loadAllWorkspaces() throws Exception;
	public boolean doesExist(String workspaceName);
	public Workspace getWorkspace(String workspaceName) throws Exception;
	public void saveWorkspace(Workspace workspace) throws Exception;
	public void deleteWorkspace(String workspaceName) throws Exception;
	
	public void saveProfile(String workspaceName,Profile profile) throws Exception;
	public void deleteProfile(String workspaceName,String profileName) throws Exception;

	public void saveQueryEndpoint(String workspaceName,String profileName,QueryEndpoint queryEndpoint) throws Exception;
	public void deleteQueryEndpoint(String workspaceName,String profileName,String queryEndpointName) throws Exception;
	
	public void saveSubmitEndpoint(String workspaceName,String profileName,SubmitEndpoint queryEndpoint) throws Exception;
	public void deleteSubmitEndpoint(String workspaceName,String profileName,String queryEndpointName) throws Exception;
	
	public void saveDeleteEndpoint(String workspaceName,String profileName,DeleteEndpoint queryEndpoint) throws Exception;
	public void deleteDeleteEndpoint(String workspaceName,String profileName,String queryEndpointName) throws Exception;
	
	
}
