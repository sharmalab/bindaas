package edu.emory.cci.bindaas.core.jwt;

import java.util.Date;
import java.util.List;

import edu.emory.cci.bindaas.core.model.hibernate.HistoryLog.ActivityType;
import edu.emory.cci.bindaas.core.model.hibernate.UserRequest;
import edu.emory.cci.bindaas.core.model.hibernate.UserRequest.Stage;
import edu.emory.cci.bindaas.security.api.BindaasUser;

public interface IJWTManager {

	public Boolean verifyToken(String securityToken) throws JWTManagerException;
	public BindaasUser createUser(String securityToken, String initiatedBy, String comments, ActivityType activityType) throws JWTManagerException;
	public void modifyJWT(Long id , Stage stage , Date dateExpires ,String initiatedBy , String comments , ActivityType activityType ) throws JWTManagerException;
	public BindaasUser lookupUser(String jwt) throws JWTManagerException;
	public List<UserRequest> listJWT() throws JWTManagerException;
	public Integer revokeJWT(BindaasUser bindaasUser, String initiatedBy ,String comments , ActivityType activityType) throws JWTManagerException;
	public Date getExpires(String jwt);
	public String getEmailAddress(Long id);

}
