package edu.emory.cci.bindaas.webconsole;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;

import edu.emory.cci.bindaas.core.model.hibernate.HistoryLog;
import edu.emory.cci.bindaas.core.model.hibernate.UserRequest;
import edu.emory.cci.bindaas.core.util.DynamicObject;
import edu.emory.cci.bindaas.core.util.DynamicProperties;
import edu.emory.cci.bindaas.security.api.AuthenticationException;
import edu.emory.cci.bindaas.security.api.BindaasUser;
import edu.emory.cci.bindaas.webconsole.config.BindaasAdminConsoleConfiguration;

public class PostLoginAction extends HttpServlet {
	private String defaultLoginTarget;
	private Log log = LogFactory.getLog(getClass());

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

			DynamicObject<BindaasAdminConsoleConfiguration> dynamicAdminConsoleConfiguration = Activator
					.getService(DynamicObject.class,
							"(name=bindaas.adminconsole)");
			Set<String> setOfAllowedAdmins = dynamicAdminConsoleConfiguration
					.getObject().getAdminAccounts();
			if (setOfAllowedAdmins.contains(principal.getName())) {

				// generate a api_key for this user if doesnt exist

				generateApiKey(principal);
				response.sendRedirect(loginTarget);

			} else {

				try {
					request.getSession().setAttribute("loggedInUser", null);
					LoginView.generateLoginView(request, response, loginTarget,
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

				LoginView.generateLoginView(request, response, loginTarget,
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

	private void generateApiKey(BindaasUser principal) {

		SessionFactory sessionFactory = Activator
				.getService(SessionFactory.class);
		if (sessionFactory != null) {
			Session session = sessionFactory.openSession();
			try {
				session.beginTransaction();

				String emailAddress = principal
						.getProperty(BindaasUser.EMAIL_ADDRESS) != null ? principal
						.getProperty(BindaasUser.EMAIL_ADDRESS).toString()
						: principal.getName() + "@" + principal.getDomain();
				String firstName = principal
						.getProperty(BindaasUser.FIRST_NAME) != null ? principal
						.getProperty(BindaasUser.FIRST_NAME).toString()
						: principal.getName();
				String lastName = principal.getProperty(BindaasUser.LAST_NAME) != null ? principal
						.getProperty(BindaasUser.LAST_NAME).toString()
						: principal.getName();

				List<UserRequest> listOfValidKeys = (List<UserRequest>) session
						.createCriteria(UserRequest.class)
						.add(Restrictions.eq("stage", "accepted"))
						.add(Restrictions.eq("emailAddress", emailAddress))
						.list();

				if (listOfValidKeys != null && listOfValidKeys.size() > 0) {
					UserRequest request = listOfValidKeys.get(0);
					if (request.getDateExpires().after(new Date())) {
						principal.addProperty("apiKey", request.getApiKey());
						return;
					} else {
						log.warn("The API Key of user [" + principal
								+ "] has expired");
					}
				} else {
					// generate api key for first time user

					GregorianCalendar calendar = new GregorianCalendar();
					calendar.add(Calendar.YEAR, 40);
					UserRequest userRequest = new UserRequest();
					userRequest.setStage("accepted");
					userRequest.setApiKey(URLEncoder.encode(UUID.randomUUID()
							.toString()));
					userRequest.setDateExpires(calendar.getTime());

					userRequest.setEmailAddress(emailAddress);
					userRequest.setFirstName(firstName);
					userRequest.setLastName(lastName);

					session.save(userRequest);

					HistoryLog historyLog = new HistoryLog();
					historyLog.setActivityType("system-approve");
					historyLog
							.setComments("System generated API Key for the user");
					historyLog.setInitiatedBy("system");
					historyLog.setUserRequest(userRequest);

					session.save(historyLog);
					session.getTransaction().commit();

					principal.addProperty("apiKey", userRequest.getApiKey());
				}

			} catch (Exception e) {
				log.error(e);
				session.getTransaction().rollback();
			} finally {
				session.close();
			}

		}

	}

}
