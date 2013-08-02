package edu.emory.cci.bindaas.webconsole.admin;

import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;

import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;

import edu.emory.cci.bindaas.commons.mail.api.IMailService;
import edu.emory.cci.bindaas.core.model.hibernate.HistoryLog;
import edu.emory.cci.bindaas.core.model.hibernate.UserRequest;
import edu.emory.cci.bindaas.framework.util.GSONUtil;
import edu.emory.cci.bindaas.security.api.BindaasUser;

public class UserManagementPanelAction implements IAdminAction {
	
	private String actionName;
	private Log log = LogFactory.getLog(getClass());
	private IMailService mailService;
	
	public IMailService getMailService() {
		return mailService;
	}
	public void setMailService(IMailService mailService) {
		this.mailService = mailService;
	}

	private SessionFactory sessionFactory;

	
	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	
	public void setActionName(String actionName) {
		this.actionName = actionName;
	}

	@Override
	public String getActionName() {
		return actionName;
	}

	@SuppressWarnings("deprecation")
	@Override
	public String doAction(JsonObject payload , HttpServletRequest request) throws Exception {
		BindaasUser admin = (BindaasUser) request.getSession().getAttribute("loggedInUser");
		Request requestObject = GSONUtil.getGSONInstance().fromJson(payload, Request.class);
	
		if(sessionFactory!=null)
		{
			Session session  = sessionFactory.openSession();
			
			try {
					session.beginTransaction();
					@SuppressWarnings("unchecked")
					List<UserRequest> list = session.createCriteria(UserRequest.class).add(Restrictions.eq("id", requestObject.entityId)). list();
					String emailMessage = null;
					if(list!=null && list.size() > 0)
					{
						UserRequest userRequest = list.get(0);
						HistoryLog historyLog = new HistoryLog();
						historyLog.setComments(requestObject.entityComments);
						historyLog.setInitiatedBy(admin.getName());
						historyLog.setUserRequest(userRequest);
						
						
						
						if(requestObject.entityAction!=null && (requestObject.entityAction.equals("approve") || requestObject.entityAction.equals("refresh")))
						{
							userRequest.setStage("accepted");
							userRequest.setApiKey( URLEncoder.encode( UUID.randomUUID().toString()  ));
							userRequest.setDateExpires(requestObject.getExpiration());
							
							emailMessage = String.format("Congratulations!\nYour application has been accepted." +
									"\nYour new API-Key : %s \nExpires On : %s ", userRequest.getApiKey() , userRequest.getDateExpires().toString());
							
						}
						else if(requestObject.entityAction!=null && requestObject.entityAction.equals("revoke") )
						{
							userRequest.setStage("revoked");
							emailMessage = "Your access has been revoked by the administrator";
							
							
						}
						else if(requestObject.entityAction!=null && requestObject.entityAction.equals("deny") )
						{
							userRequest.setStage("denied");
							emailMessage = "Your application has been denied by the administrator";
						}
						else
						{
							throw new Exception("Action not defined");
						}
						
						historyLog.setActivityType(requestObject.entityAction);
						
						session.save(userRequest);
						session.save(historyLog);
						
//						IMailService mailService = Activator.getService(IMailService.class);
					
						if(mailService != null) 
						{
							try{
							mailService.sendMail(userRequest.getEmailAddress() , "Your Bindaas API Key status" , emailMessage);
							}catch(Exception e)
							{
								log.error(String.format("Unable send mail notification. Message [%s] not sent to [%s]" , emailMessage ,userRequest.getEmailAddress() ));
							}
						}
						else
							log.error(String.format("Mail Service not available. Message [%s] not sent to [%s]" , emailMessage ,userRequest.getEmailAddress() ));
						
						
					}
					else
					{
						throw new Exception("No results found matching id = [" + requestObject.entityId + "]");
					}
					
					session.getTransaction().commit();
					return "success";
					
			}
			catch(Exception e)
			{
				session.getTransaction().rollback();
				log.error(e);
				throw e;
			}
			finally{
				session.close();
			}
		}
		else
		{
			throw new Exception("Session Factory not available");
			
		}

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
