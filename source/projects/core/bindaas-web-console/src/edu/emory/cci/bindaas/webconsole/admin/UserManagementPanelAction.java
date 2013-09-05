package edu.emory.cci.bindaas.webconsole.admin;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;

import edu.emory.cci.bindaas.commons.mail.api.IMailService;
import edu.emory.cci.bindaas.core.apikey.api.APIKey;
import edu.emory.cci.bindaas.core.apikey.api.IAPIKeyManager;
import edu.emory.cci.bindaas.core.model.hibernate.HistoryLog.ActivityType;
import edu.emory.cci.bindaas.core.model.hibernate.UserRequest.Stage;
import edu.emory.cci.bindaas.framework.util.GSONUtil;
import edu.emory.cci.bindaas.security.api.BindaasUser;

public class UserManagementPanelAction implements IAdminAction {
	
	private String actionName;
	private Log log = LogFactory.getLog(getClass());
	private IMailService mailService;
	private IAPIKeyManager apiKeyManager;
	
	public IAPIKeyManager getApiKeyManager() {
		return apiKeyManager;
	}
	public void setApiKeyManager(IAPIKeyManager apiKeyManager) {
		this.apiKeyManager = apiKeyManager;
	}
	public IMailService getMailService() {
		return mailService;
	}
	public void setMailService(IMailService mailService) {
		this.mailService = mailService;
	}

	
	public void setActionName(String actionName) {
		this.actionName = actionName;
	}

	@Override
	public String getActionName() {
		return actionName;
	}

	@Override
	public String doAction(JsonObject payload , HttpServletRequest request) throws Exception {
		BindaasUser admin = (BindaasUser) request.getSession().getAttribute("loggedInUser");
		Request requestObject = GSONUtil.getGSONInstance().fromJson(payload, Request.class);
	
		
		String emailMessage =  null;
		String emailAddress = null;
		if(requestObject.entityAction!=null && (requestObject.entityAction.equals(ActivityType.APPROVE.toString()) || requestObject.entityAction.equals(ActivityType.REFRESH.toString())))
		{
			
			APIKey apiKey = this.apiKeyManager.modifyAPIKey(requestObject.entityId, Stage.accepted, requestObject.getExpiration(), admin.getName(), requestObject.entityComments, ActivityType.valueOf(requestObject.entityAction.toUpperCase()) );
			emailMessage = String.format("Congratulations!\nYour application has been accepted." +
					"\nYour new API-Key : %s \nExpires On : %s ", apiKey.getValue() , apiKey.getExpires().toString());
			emailAddress = apiKey.getEmailAddress();
			
		}
		else if(requestObject.entityAction!=null && requestObject.entityAction.equals(ActivityType.REVOKE.toString()) )
		{
			APIKey apiKey = this.apiKeyManager.modifyAPIKey(requestObject.entityId, Stage.revoked, requestObject.getExpiration(), admin.getName(), requestObject.entityComments, ActivityType.REVOKE );
			emailMessage = "Your access has been revoked by the administrator";
			emailAddress = apiKey.getEmailAddress();
			
		}
		else if(requestObject.entityAction!=null && requestObject.entityAction.equals(ActivityType.DENY.toString()) )
		{
			APIKey apiKey = this.apiKeyManager.modifyAPIKey(requestObject.entityId, Stage.denied, requestObject.getExpiration(), admin.getName(), requestObject.entityComments, ActivityType.DENY );
			emailMessage = "Your application has been denied by the administrator";
			emailAddress = apiKey.getEmailAddress();
		}
		else
		{
			throw new Exception("Action not defined");
		}
		
		if(mailService != null) 
		{
			try{
				mailService.sendMail(emailAddress , "Your Bindaas API Key status" , emailMessage);
			}catch(Exception e)
			{
				log.error(String.format("Unable send mail notification. Message [%s] not sent to [%s]" , emailMessage , emailAddress ));
			}
		}
		else
			log.error(String.format("Mail Service not available. Message [%s] not sent to [%s]" , emailMessage , emailAddress ));
		
		return "success";
	}
	
	public static class Request {
		@Expose private String entityComments;
		@Expose private String entityAction;
		@Expose private Long entityId;
		@Expose private String entityExpiration;
		
		private Date getExpiration()
		{
			GregorianCalendar calendar = new GregorianCalendar();
			if(entityExpiration!=null && entityExpiration.equals("1d"))
			{
				calendar.add(Calendar.DAY_OF_MONTH, 1);
			}
			else if(entityExpiration!=null && entityExpiration.equals("30d"))
			{
				calendar.add(Calendar.DAY_OF_MONTH, 30);
			}
			else if(entityExpiration!=null && entityExpiration.equals("1y"))
			{
				calendar.add(Calendar.YEAR, 1);
			}
			else if(entityExpiration!=null && entityExpiration.equals("-1"))
			{
				calendar.add(Calendar.YEAR, 30);
			}
			
			return calendar.getTime();
		}
	}


}
