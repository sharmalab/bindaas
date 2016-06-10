package edu.emory.cci.bindaas.core.apikey.api;

import java.util.Date;
import java.util.List;

import edu.emory.cci.bindaas.core.model.hibernate.HistoryLog.ActivityType;
import edu.emory.cci.bindaas.core.model.hibernate.UserRequest;
import edu.emory.cci.bindaas.core.model.hibernate.UserRequest.Stage;
import edu.emory.cci.bindaas.security.api.BindaasUser;

public interface IAPIKeyManager {

	public APIKey generateAPIKey(BindaasUser bindaasUser , Date dateExpires,String initiatedBy , String comments , ActivityType activityType , boolean throwErrorIfAlreadyExists) throws APIKeyManagerException;
	public APIKey createAPIKeyRequest(BindaasUser bindaasUser , Date dateExpires,String initiatedBy , String comments , ActivityType activityType ) throws APIKeyManagerException;
	public APIKey modifyAPIKey(Long id , Stage stage , Date dateExpires ,String initiatedBy , String comments , ActivityType activityType ) throws APIKeyManagerException;
	public APIKey createShortLivedAPIKey(BindaasUser bindaasUser , int lifetime , String applicationId) throws APIKeyManagerException;
	public BindaasUser lookupUser(String apiKey) throws APIKeyManagerException;
	public Integer revokeAPIKey(BindaasUser bindaasUser,String initiatedBy ,String comments , ActivityType activityType) throws APIKeyManagerException;
	public Integer revokeAPIKey(String apiKey,String initiatedBy ,String comments , ActivityType activityType) throws APIKeyManagerException;
	public List<UserRequest> listAPIKeys() throws APIKeyManagerException;
	public Integer purgeExpiredKeys() throws APIKeyManagerException;
	public APIKey lookupAPIKeyByUsername(String username) throws APIKeyManagerException;
}
