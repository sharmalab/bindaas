package edu.emory.cci.bindaas.webconsole.admin;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import com.google.gson.JsonObject;

import edu.emory.cci.bindaas.core.util.DynamicProperties;
import edu.emory.cci.bindaas.framework.util.GSONUtil;
import edu.emory.cci.bindaas.security.api.BindaasUser;
import edu.emory.cci.bindaas.webconsole.AbstractRequestHandler;
import edu.emory.cci.bindaas.webconsole.Activator;
import edu.emory.cci.bindaas.webconsole.ErrorView;


public class AdminServlet extends AbstractRequestHandler{
	private static String templateName = "administration.vt";
	private static Template template;
	
	static {
		template = Activator.getVelocityTemplateByName(templateName);
	}
	
	private String uriTemplate;
	private Log log = LogFactory.getLog(getClass());
	private Map<String,IAdminAction> adminActionMap;
	
	public Map<String, IAdminAction> getAdminActionMap() {
		return adminActionMap;
	}

	public void setAdminActionMap(Map<String, IAdminAction> adminActionMap) {
		this.adminActionMap = adminActionMap;
	}

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
		SessionFactory sessionFactory  = Activator.getService(SessionFactory.class);
		if(sessionFactory!=null)
		{
			Session session  = sessionFactory.openSession();
			
			try {
					List pendingRequests = session.createQuery("from UserRequest where stage = :stage order by requestDate desc").setString("stage", "pending").list();
					List acceptedRequests = session.createQuery("from UserRequest where stage = :stage order by requestDate desc").setString("stage", "accepted").list();
					List historyLog = session.createQuery("from HistoryLog order by activityDate desc").list();
					
					VelocityContext velocityContext = new VelocityContext();
					
					
					// set usermgmt props
					velocityContext.put("pendingRequests", pendingRequests);
					velocityContext.put("acceptedRequests", acceptedRequests);
					velocityContext.put("historyLog", historyLog);
					
					DynamicProperties bindaasProps = Activator.getService(DynamicProperties.class, "(name=bindaas)");
					DynamicProperties ldapProps = Activator.getService(DynamicProperties.class, "(name=bindaas.authentication.ldap)");
					DynamicProperties mailServiceProps = Activator.getService(DynamicProperties.class, "(name=mailService)");

					// set server admin props
					velocityContext.put("serverConfig", ServerAdminAction.Request.fromDynamicProperties(bindaasProps));
					
					// set security props
					velocityContext.put("securityConfig", SecurityAction.Request.fromDynamicProperties(bindaasProps , ldapProps));
					
					// set mail service props
					velocityContext.put("mailServiceConfig", EmailAction.Request.fromDynamicProperties(mailServiceProps));
					velocityContext.put("bindaasUser" , BindaasUser.class.cast(request.getSession().getAttribute("loggedInUser")).getName());
					
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
	
	private void doAction(HttpServletRequest req,
			HttpServletResponse response)
	{

		String jsonRequest = req.getParameter("jsonRequest");
		JsonObject jsonObject = GSONUtil.getJsonParser().parse(jsonRequest).getAsJsonObject();
		
		String action = jsonObject.get("action").getAsString();
		JsonObject request = jsonObject.get("request").getAsJsonObject();
		
		try {
		IAdminAction adminActionHandler = adminActionMap.get(action);
		if(adminActionHandler!=null)
		{
			String retVal = adminActionHandler.doAction(request, req);
			response.getWriter().write(retVal);
		}
		else
			throw new Exception("No handler matching action [" + action + "]");
		
		}
		catch(Exception e)
		{
			log.error(e);
			ErrorView.handleError(response, e);
		}
		
	}
	
		

}
