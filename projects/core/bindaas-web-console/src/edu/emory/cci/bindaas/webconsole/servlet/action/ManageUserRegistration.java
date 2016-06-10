package edu.emory.cci.bindaas.webconsole.servlet.action;

import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;

import com.google.gson.annotations.Expose;

import edu.emory.cci.bindaas.commons.mail.api.IMailService;
import edu.emory.cci.bindaas.core.model.hibernate.HistoryLog;
import edu.emory.cci.bindaas.core.model.hibernate.UserRequest;
import edu.emory.cci.bindaas.framework.util.GSONUtil;
import edu.emory.cci.bindaas.security.api.BindaasUser;
import edu.emory.cci.bindaas.version_manager.api.IVersionManager;
import edu.emory.cci.bindaas.webconsole.AbstractRequestHandler;
import edu.emory.cci.bindaas.webconsole.ErrorView;
import edu.emory.cci.bindaas.webconsole.util.VelocityEngineWrapper;


public class ManageUserRegistration extends AbstractRequestHandler {

	private static String templateName = "manageUserRegistration.vt";
	private  Template template;
	private VelocityEngineWrapper velocityEngineWrapper;
	private IMailService mailService;
	private SessionFactory sessionFactory;
	private IVersionManager versionManager;
	
	public IMailService getMailService() {
		return mailService;
	}

	public void setMailService(IMailService mailService) {
		this.mailService = mailService;
	}

	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public IVersionManager getVersionManager() {
		return versionManager;
	}

	public void setVersionManager(IVersionManager versionManager) {
		this.versionManager = versionManager;
	}

	public VelocityEngineWrapper getVelocityEngineWrapper() {
		return velocityEngineWrapper;
	}

	public void setVelocityEngineWrapper(VelocityEngineWrapper velocityEngineWrapper) {
		this.velocityEngineWrapper = velocityEngineWrapper;
	}

	public void init() throws Exception
	{
		template = velocityEngineWrapper.getVelocityTemplateByName(templateName);
	}
	
	private String uriTemplate;
	private Log log = LogFactory.getLog(getClass());
	
	public String getUriTemplate() {
		return uriTemplate;
	}

	public void setUriTemplate(String uriTemplate) {
		this.uriTemplate = uriTemplate;
	}
	@Override
	public void handleRequest(HttpServletRequest request,
			HttpServletResponse response, Map<String,String> pathParameters) throws Exception {

		if(request.getMethod().equalsIgnoreCase("post"))
		{
			doAction(request, response);
		}
		else if (request.getMethod().equalsIgnoreCase("get"))
		{
			getView(request,response);
		}
		
		else
		{
			throw new Exception("Http Method [" + request.getMethod() + "] not allowed here");
		}
	}
	
	private void getView(HttpServletRequest request,
			HttpServletResponse response)
	{
		
		if(sessionFactory!=null)
		{
			Session session  = sessionFactory.openSession();
			
			try {
					List<?> pendingRequests = session.createQuery("from UserRequest where stage = :stage order by requestDate desc").setString("stage", "pending").list();
					List<?> acceptedRequests = session.createQuery("from UserRequest where stage = :stage order by requestDate desc").setString("stage", "accepted").list();
					List<?> historyLog = session.createQuery("from HistoryLog order by activityDate desc").list();
					
					VelocityContext velocityContext = new VelocityContext();
					velocityContext.put("pendingRequests", pendingRequests);
					velocityContext.put("acceptedRequests", acceptedRequests);
					velocityContext.put("historyLog", historyLog);
					velocityContext.put("bindaasUser" , BindaasUser.class.cast(request.getSession().getAttribute("loggedInUser")).getName());
					/**
					 * Add version information
					 */
					String versionHeader = String.format("System built <strong>%s</strong>  Build date <strong>%s<strong>", versionManager.getSystemBuild() ,versionManager.getSystemBuildDate());;
					velocityContext.put("versionHeader", versionHeader);
					template.merge(velocityContext, response.getWriter());
			}
			catch(Exception e)
			{
				log.error(e);
				ErrorView.handleError(response, e);
			}
			finally{
				session.close();
			}
		}
		else
		{
			ErrorView.handleError(response, new Exception("Session Factory not available"));
		}
	}
	
	@SuppressWarnings("deprecation")
	private void doAction(HttpServletRequest request,
			HttpServletResponse response)
	{

		String jsonRequest = request.getParameter("jsonRequest");
		Request requestObject = GSONUtil.getGSONInstance().fromJson(jsonRequest, Request.class);
		BindaasUser admin = (BindaasUser) request.getSession().getAttribute("loggedInUser");
		
		
		//
		
		if(sessionFactory!=null)
		{
			Session session  = sessionFactory.openSession();
			
			try {
					session.beginTransaction();
					
					@SuppressWarnings("unchecked")
					List<UserRequest> list = session.createCriteria(UserRequest.class).add(Restrictions.eq("id", requestObject.entityId)).list();
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
						
						if(mailService == null) throw new Exception("Mail Service not available");
						else
						mailService.sendMail(userRequest.getEmailAddress() , "Your Bindaas API Key status" , emailMessage);
						
					}
					else
					{
						throw new Exception("No results found matching id = [" + requestObject.entityId + "]");
					}
					
					session.getTransaction().commit();
					
					
			}
			catch(Exception e)
			{
				session.getTransaction().rollback();
				log.error(e);
				ErrorView.handleError(response, e);
			}
			finally{
				session.close();
			}
		}
		else
		{
			ErrorView.handleError(response, new Exception("Session Factory not available"));
		}
	}
	
	private static class Request {
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
