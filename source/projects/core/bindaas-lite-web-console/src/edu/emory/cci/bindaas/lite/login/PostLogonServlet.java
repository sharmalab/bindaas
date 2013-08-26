package edu.emory.cci.bindaas.lite.login;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;

import edu.emory.cci.bindaas.core.model.hibernate.HistoryLog;
import edu.emory.cci.bindaas.core.model.hibernate.UserRequest;
import edu.emory.cci.bindaas.lite.BindaasSession;
import edu.emory.cci.bindaas.lite.RegistrableServlet;
import edu.emory.cci.bindaas.lite.config.BindaasAdminConsoleConfiguration;
import edu.emory.cci.bindaas.security.api.BindaasUser;

/**
 * registered at /bindaas/lite/post-logon
 * 
 * @author nadir
 * 
 */
public class PostLogonServlet extends RegistrableServlet {
	private static final long serialVersionUID = 1L;
	private LoginServlet loginServlet;
	private SessionFactory sessionFactory;
	private Log log = LogFactory.getLog(getClass());

	public LoginServlet getLoginServlet() {
		return loginServlet;
	}

	public void setLoginServlet(LoginServlet loginServlet) {
		this.loginServlet = loginServlet;
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		BindaasSession session = BindaasSession.getBindaasSession(req);
		BindaasUser bindaasUser = session.getBindaasUser();
		String loginTarget = session.getLoginTarget();
		if (isAuthorized(bindaasUser)) {
			String apiKey = generateApiKey(bindaasUser);
			session.setApiKey(apiKey);
			resp.sendRedirect(loginTarget);
		} else {
			loginServlet.redirect(resp,
					"You are not authorized to access this resource",
					loginTarget);
		}

	}

	public void redirect(HttpServletResponse response) throws IOException {
		response.sendRedirect(getServletPath());
	}

	private String generateApiKey(BindaasUser principal) {
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

				@SuppressWarnings("unchecked")
				List<UserRequest> listOfValidKeys = (List<UserRequest>) session
						.createCriteria(UserRequest.class)
						.add(Restrictions.eq("stage", "accepted"))
						.add(Restrictions.eq("emailAddress", emailAddress))
						.list();

				if (listOfValidKeys != null && listOfValidKeys.size() > 0) {
					UserRequest request = listOfValidKeys.get(0);
					if (request.getDateExpires().after(new Date())) {
						principal.addProperty("apiKey", request.getApiKey());
						return request.getApiKey();
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
							.toString() , "UTF-8" ));
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
					return userRequest.getApiKey();
				}

			} catch (Exception e) {
				log.error(e);
				session.getTransaction().rollback();
				
			} finally {
				session.close();
			}
			return null;
}

	private boolean isAuthorized(BindaasUser bindaasUser) {
		BindaasAdminConsoleConfiguration configuration = getBindaasAdminConfiguration();
		try {
			Set<String> setOfAllowedAdmins = configuration.getAdminAccounts();
			if (setOfAllowedAdmins.contains(bindaasUser.getName())
					|| setOfAllowedAdmins.contains(bindaasUser.getName() + "@"
							+ bindaasUser.getDomain()))
				return true;
			else
				return false;
		} catch (Exception e) {
			log.error("Error in authorization module", e);
			return false;
		}
	}



	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
}
