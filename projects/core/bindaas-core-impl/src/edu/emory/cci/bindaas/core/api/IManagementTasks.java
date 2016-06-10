package edu.emory.cci.bindaas.core.api;

import java.util.Collection;

import com.google.gson.JsonObject;

import edu.emory.cci.bindaas.framework.model.DeleteEndpoint;
import edu.emory.cci.bindaas.framework.model.Profile;
import edu.emory.cci.bindaas.framework.model.QueryEndpoint;
import edu.emory.cci.bindaas.framework.model.SubmitEndpoint;
import edu.emory.cci.bindaas.framework.model.Workspace;

public interface IManagementTasks {

	/**
	 * Create operations
	 */
	
	public Workspace createWorkspace(String name , JsonObject parameters , String createdBy) throws Exception;
	public Profile createProfile(String name , String workspaceName,JsonObject parameters , String createdBy , String description) throws Exception;
	public QueryEndpoint createQueryEndpoint(String name , String workspaceName,String profileName ,JsonObject parameters, String createdBy) throws Exception;
	public DeleteEndpoint createDeleteEndpoint(String name , String workspaceName,String profileName ,JsonObject parameters, String createdBy) throws Exception;
	public SubmitEndpoint createSubmitEndpoint(String name , String workspaceName,String profileName ,JsonObject parameters, String createdBy) throws Exception;
	
	
	/**
	 *  Delete operations
	 */
	
	public void deleteWorkspace(String workspaceName) throws Exception;
	public void deleteProfile(String workspaceName,String profileName) throws Exception;
	public QueryEndpoint deleteQueryEndpoint(String workspaceName,String profileName , String queryEndpointName) throws Exception;
	public DeleteEndpoint deleteDeleteEndpoint(String workspaceName,String profileName , String deleteEndpointName) throws Exception;
	public SubmitEndpoint deleteSubmitEndpoint(String workspaceName,String profileName , String submitEndpointName) throws Exception;
	
	
	/**
	 * Update operations
	 */
	
	
	public Profile updateProfile(String name , String workspaceName,JsonObject parameters, String updatedBy , String description) throws Exception;
	public QueryEndpoint updateQueryEndpoint(String name , String workspaceName,String profileName ,JsonObject parameters, String updatedBy) throws Exception;
	public DeleteEndpoint updateDeleteEndpoint(String name , String workspaceName,String profileName ,JsonObject parameters, String updatedBy) throws Exception;
	public SubmitEndpoint updateSubmitEndpoint(String name , String workspaceName,String profileName ,JsonObject parameters, String updatedBy) throws Exception;
	
	/**
	 *  Other operations
	 */
	
	public Collection<Workspace> listWorkspaces() throws Exception;
	public Workspace getWorkspace(String name) throws Exception;
	public Profile getProfile(String workspaceName , String profileName) throws Exception;
	public QueryEndpoint getQueryEndpoint(String workspaceName,String profileName, String queryEndpointName) throws Exception;
	public DeleteEndpoint getDeleteEndpoint(String workspaceName,String profileName,String deleteEndpointName) throws Exception;
	public SubmitEndpoint getSubmitEndpoint(String workspaceName,String profileName,String submitEndpointName) throws Exception;
	
	/**
	 * publish operations
	 * 
	 */
	
	public void publishQueryEndpoint(String workspaceName,String profileName,String queryEndpointName) throws Exception;
	public void publishDeleteEndpoint(String workspaceName,String profileName,String deleteEndpointName) throws Exception;
	
	
}
