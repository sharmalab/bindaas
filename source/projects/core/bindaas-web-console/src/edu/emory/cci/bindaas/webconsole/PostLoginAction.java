package edu.emory.cci.bindaas.webconsole;

import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.emory.cci.bindaas.core.apikey.api.APIKey;
import edu.emory.cci.bindaas.core.apikey.api.APIKeyManagerException;
import edu.emory.cci.bindaas.core.apikey.api.IAPIKeyManager;
import edu.emory.cci.bindaas.core.model.hibernate.HistoryLog.ActivityType;
import edu.emory.cci.bindaas.core.util.DynamicObject;
import edu.emory.cci.bindaas.security.api.BindaasUser;
import edu.emory.cci.bindaas.webconsole.bundle.Activator;
import edu.emory.cci.bindaas.webconsole.config.BindaasAdminConsoleConfiguration;

public class PostLoginAction extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private String defaultLoginTarget;
	private Log log = LogFactory.getLog(getClass());
	private LoginView loginView;
	private IAPIKeyManager apiKeyManager;
	
	public IAPIKeyManager getApiKeyManager() {
		return apiKeyManager;
	}
	public void setApiKeyManager(IAPIKeyManager apiKeyManager) {
		this.apiKeyManager = apiKeyManager;
	}
	public LoginView getLoginView() {
		return loginView;
	}
	public void setLoginView(LoginView loginView) {
		this.loginView = loginView;
	}
	public String getDefaultLoginTarget() {
		return defaultLoginTarget;
	}

	public void setDefaultLoginTarget(String defaultLoginTarget) {
		this.defaultLoginTarget = defaultLoginTarget;
	}

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		String loginTarget = request.getSession().getAttribute("loginTarget") != null ? (String) request
				.getSession().getAttribute("loginTarget") : defaultLoginTarget;
		try {
			BindaasUser principal = (BindaasUser) request.getSession()
					.getAttribute("loggedInUser");

			@SuppressWarnings("unchecked")
			DynamicObject<BindaasAdminConsoleConfiguration> dynamicAdminConsoleConfiguration = Activator
					.getService(DynamicObject.class,
							"(name=bindaas.adminconsole)");
			Set<String> setOfAllowedAdmins = dynamicAdminConsoleConfiguration
					.getObject().getAdminAccounts();
			if (setOfAllowedAdmins.contains(principal.getName()) || setOfAllowedAdmins.contains(principal.getName() + "@" + principal.getDomain())) {

				// generate a api_key for this user if doesnt exist

				principal = generateApiKey(principal);
				response.sendRedirect(loginTarget);

			} else {

				try {
					request.getSession().setAttribute("loggedInUser", null);
					loginView.generateLoginView(request, response, loginTarget,
							"You are not authorized to access this resource");

				} catch (Exception e1) {
					log.error(e1);
					ErrorView.handleError(response, new Exception(
							"Authentication System unavailable"));
				}

			}

		} catch (Exception e) {
			log.error(e);

			try {

				loginView.generateLoginView(request, response, loginTarget,
						"You are not authorized to access this resource");

			} catch (Exception e1) {
				log.error(e1);
				ErrorView.handleError(response, new Exception(
						"Authentication System unavailable"));
			}

		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		super.doPost(req, resp);
	}

	
	private BindaasUser generateApiKey(BindaasUser principal) throws APIKeyManagerException  {
		
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.add(Calendar.YEAR, 40);
		APIKey apiKey = apiKeyManager.generateAPIKey(principal, calendar.getTime(), "system", "System generated API Key for the user", ActivityType.SYSTEM_APPROVE, false);
		principal.addProperty("apiKey", apiKey.getValue());
		return principal;
	}

}
