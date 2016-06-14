package edu.emory.cci.bindaas.webconsole.admin;

import java.util.Date;
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
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import com.google.gson.JsonObject;

import edu.emory.cci.bindaas.core.config.BindaasConfiguration;
import edu.emory.cci.bindaas.core.model.hibernate.UserRequest;
import edu.emory.cci.bindaas.core.util.DynamicObject;
import edu.emory.cci.bindaas.core.util.DynamicProperties;
import edu.emory.cci.bindaas.framework.util.GSONUtil;
import edu.emory.cci.bindaas.security.api.BindaasUser;
import edu.emory.cci.bindaas.version_manager.api.IVersionManager;
import edu.emory.cci.bindaas.webconsole.AbstractRequestHandler;
import edu.emory.cci.bindaas.webconsole.ErrorView;
import edu.emory.cci.bindaas.webconsole.bundle.Activator;
import edu.emory.cci.bindaas.webconsole.config.BindaasAdminConsoleConfiguration;
import edu.emory.cci.bindaas.webconsole.util.VelocityEngineWrapper;

public class AdminServlet extends AbstractRequestHandler {
	private static String templateName = "administration.vt";
	private Template template;
	private VelocityEngineWrapper velocityEngineWrapper;
	private SessionFactory sessionFactory;
	private IVersionManager versionManager;
	private static final int MAX_DISPLAY_THRESHOLD = 50;
	
	public IVersionManager getVersionManager() {
		return versionManager;
	}

	public void setVersionManager(IVersionManager versionManager) {
		this.versionManager = versionManager;
	}

	
	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public VelocityEngineWrapper getVelocityEngineWrapper() {
		return velocityEngineWrapper;
	}

	public void setVelocityEngineWrapper(
			VelocityEngineWrapper velocityEngineWrapper) {
		this.velocityEngineWrapper = velocityEngineWrapper;
	}

	public void init() throws Exception {
		template = velocityEngineWrapper
				.getVelocityTemplateByName(templateName);
	}

	private String uriTemplate;
	private Log log = LogFactory.getLog(getClass());
	private Map<String, IAdminAction> adminActionMap;

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
			HttpServletResponse response, Map<String, String> pathParameters)
			throws Exception {

		if (request.getMethod().equalsIgnoreCase("post")) {
			doAction(request, response);
		} else if (request.getMethod().equalsIgnoreCase("get")) {
			getView(request, response);
		}

		else {
			throw new Exception("Http Method [" + request.getMethod()
					+ "] not allowed here");
		}
	}

	private void getView(HttpServletRequest request,
			HttpServletResponse response) {
		
		if (sessionFactory != null) {
			Session session = sessionFactory.openSession();

			try {
				List<?> pendingRequests = session
						.createQuery(
								"from UserRequest where stage = :stage order by requestDate desc")
						.setString("stage", "pending").list();
				List<?> acceptedRequests = session.createCriteria(UserRequest.class).add(Restrictions.eq("stage", "accepted")).
						add(Restrictions.ge("dateExpires", new Date()))
						.addOrder(Order.desc("requestDate")).setMaxResults(MAX_DISPLAY_THRESHOLD).list();
				List<?> historyLog = session.createQuery(
						"from HistoryLog order by activityDate desc").setMaxResults(MAX_DISPLAY_THRESHOLD).list();

				VelocityContext velocityContext = new VelocityContext();
				/**
				 * Add version information
				 */
				String versionHeader = String.format("System built <strong>%s</strong>  Build date <strong>%s<strong>", versionManager.getSystemBuild() ,versionManager.getSystemBuildDate());;
				velocityContext.put("versionHeader", versionHeader);

				// set usermgmt props
				velocityContext.put("pendingRequests", pendingRequests);
				velocityContext.put("acceptedRequests", acceptedRequests);
				velocityContext.put("historyLog", historyLog);

				DynamicProperties mailServiceProps = Activator.getService(
						DynamicProperties.class, "(name=mailService)");
				@SuppressWarnings("unchecked")
				DynamicObject<BindaasAdminConsoleConfiguration> dynamicAdminConsoleConfiguration = Activator
						.getService(DynamicObject.class,
								"(name=bindaas.adminconsole)");
				@SuppressWarnings("unchecked")
				DynamicObject<BindaasConfiguration> dynamicConfiguration = Activator
						.getService(DynamicObject.class, "(name=bindaas)");

				// set middleware props
				velocityContext.put("middlewareConfiguration",
						dynamicConfiguration.getObject().clone());

				// set adminconsole props
				velocityContext.put("adminconsoleConfiguration",
						dynamicAdminConsoleConfiguration.getObject().clone());

				// set mail service props
				velocityContext.put("mailServiceConfig",
						EmailConfigurationPanelAction.Request
								.fromDynamicProperties(mailServiceProps));
				velocityContext.put(
						"bindaasUser",
						BindaasUser.class.cast(
								request.getSession().getAttribute(
										"loggedInUser")).getName());

				template.merge(velocityContext, response.getWriter());
			} catch (Exception e) {
				log.error(e);
				ErrorView.handleError(response, e);
			} finally {
				session.close();
			}
		} else {
			ErrorView.handleError(response, new Exception(
					"Session Factory not available"));
		}
	}

	private void doAction(HttpServletRequest req, HttpServletResponse response) {

		String jsonRequest = req.getParameter("jsonRequest");
		JsonObject jsonObject = GSONUtil.getJsonParser().parse(jsonRequest)
				.getAsJsonObject();

		String action = jsonObject.get("action").getAsString();
		JsonObject request = jsonObject.get("request").getAsJsonObject();

		try {
			IAdminAction adminActionHandler = adminActionMap.get(action);
			if (adminActionHandler != null) {
				String retVal = adminActionHandler.doAction(request, req);
				response.getWriter().write(retVal);
			} else
				throw new Exception("No handler matching action [" + action
						+ "]");

		} catch (Exception e) {
			log.error(e);
			ErrorView.handleError(response, e);
		}

	}

}
