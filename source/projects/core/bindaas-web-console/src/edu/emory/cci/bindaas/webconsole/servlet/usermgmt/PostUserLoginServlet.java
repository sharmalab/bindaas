package edu.emory.cci.bindaas.webconsole.servlet.usermgmt;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

import edu.emory.cci.bindaas.commons.mail.api.IMailService;
import edu.emory.cci.bindaas.core.model.hibernate.UserRequest;
import edu.emory.cci.bindaas.core.util.DynamicObject;
import edu.emory.cci.bindaas.core.util.DynamicProperties;
import edu.emory.cci.bindaas.framework.model.Stage;
import edu.emory.cci.bindaas.security.api.BindaasUser;
import edu.emory.cci.bindaas.webconsole.Activator;
import edu.emory.cci.bindaas.webconsole.ErrorView;
import edu.emory.cci.bindaas.webconsole.config.BindaasAdminConsoleConfiguration;

public class PostUserLoginServlet extends HttpServlet {
	public static final String servletLocation = "/user/postAuthenticate";
	private static String newRegistrationTemplateName = "seekReason.vt";
	private static String simpleMessageTemplateName = "simpleMessage.vt";
	private String loginPage = "/user/login";
	private String defaultLoginTarget = "/user/dashboard/queryBrowser";
	private static Template simpleMessageTemplate;
	
	private static Template newRegistrationTemplate;
	
	static {
		
		simpleMessageTemplate = Activator.getVelocityTemplateByName(simpleMessageTemplateName);
		newRegistrationTemplate = Activator.getVelocityTemplateByName(newRegistrationTemplateName);
	}
	
	private Log log = LogFactory.getLog(getClass());
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		BindaasUser bindaasUser = (BindaasUser) req.getSession().getAttribute("userLoggedIn");
		String loginTarget = (String) (req.getSession().getAttribute("loginTarget") != null ? req.getSession().getAttribute("loginTarget") : defaultLoginTarget);
		
		if(bindaasUser != null)
		{
			try {
				UserRequest userRequest = getUserRequest(bindaasUser.getProperty(BindaasUser.EMAIL_ADDRESS).toString() , "pending");
				if(userRequest!=null)
				{
					// pending approval
					VelocityContext context = new VelocityContext();
					context.put("message", "Your application is already under review. You shall hear from us shortly.");
					simpleMessageTemplate.merge(context, resp.getWriter());
				}
				else
				{
					userRequest = getUserRequest(bindaasUser.getProperty(BindaasUser.EMAIL_ADDRESS).toString() , "accepted");
					if(userRequest!=null)
					{
						// login
						bindaasUser.addProperty("apiKey", userRequest.getApiKey());
						resp.sendRedirect(loginTarget);
					}
					else
					{
						// seek reason
						VelocityContext context = new VelocityContext();
						context.put("firstName" , bindaasUser.getProperty(BindaasUser.FIRST_NAME).toString() );
						context.put("lastName" , bindaasUser.getProperty(BindaasUser.LAST_NAME).toString() );
						context.put("emailAddress" , bindaasUser.getProperty(BindaasUser.EMAIL_ADDRESS).toString() );
						newRegistrationTemplate.merge(context, resp.getWriter());
					}
				}
			
			
			}
			catch(Exception e)
			{
				ErrorView.handleError(resp, e);
			}
			//template.merge(context, resp.getWriter());
			
		}
		else
		{
			resp.sendRedirect(loginPage);
		}
	}

	private UserRequest getUserRequest(String emailAddress,String stage) throws Exception {
		SessionFactory sessionFactory = Activator
				.getService(SessionFactory.class);
		if (sessionFactory != null) {
			Session session = sessionFactory.openSession();
			Transaction transaction = null;
			try {
				transaction = session.beginTransaction();
				// check to see if already one exists
				List<UserRequest> list = session
						.createCriteria(UserRequest.class)
						.add(Restrictions.eq("emailAddress", emailAddress))
						.add(Restrictions.eq("stage", stage))
						.list();

				if (list != null && list.size() > 0) {
					return list.get(0);
				} else {
					return null;
				}

			} catch (Exception e) {
				if (transaction != null)
					transaction.rollback();
				log.error(e);
				throw e;

			} finally {
				session.close();
				
			}
		}
		else
		{
			throw new Exception("Hibernate Service not available");
		}

	}

	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		BindaasUser bindaasUser = (BindaasUser) req.getSession().getAttribute("userLoggedIn");
		String reason = req.getParameter("reason");
		String firstName = req.getParameter("firstName");
		String lastName = req.getParameter("lastName");
		String emailAddress = req.getParameter("emailAddress");
		
		if(bindaasUser!=null)
		{
			
			
			UserRequest userRequest;
			try {
					userRequest = getUserRequest(emailAddress, "pending");
					if(userRequest!=null)
					{
						VelocityContext context = new VelocityContext();
						context.put("message", "Your application is already under review. You shall hear from us shortly.");
						simpleMessageTemplate.merge(context, resp.getWriter());
					}
					else
					{
						userRequest = new UserRequest();
						userRequest.setFirstName(firstName);
						userRequest.setLastName(lastName);
						userRequest.setEmailAddress(emailAddress);
						
						userRequest.setReason(reason);
						userRequest.setStage("pending");
						
						SessionFactory sessionFactory = Activator.getService(SessionFactory.class);
						if (sessionFactory != null) {
							Session session = sessionFactory.openSession();
							Transaction transaction = null;
							try {
								transaction = session.beginTransaction();
				
								session.save(userRequest);
								transaction.commit();

								VelocityContext context = new VelocityContext();
								context.put("message",
										"Your application is under review. You shall hear from us shortly.");
								simpleMessageTemplate.merge(context, resp.getWriter());

								// send email notification to the admin

								DynamicObject<BindaasAdminConsoleConfiguration> dynamicAdminconsoleConfiguration = Activator.getService(DynamicObject.class , "(name=bindaas.adminconsole)");
								
								if (dynamicAdminconsoleConfiguration != null) {
									Boolean enabled = dynamicAdminconsoleConfiguration.getObject().getUserAccountManagement().getEnableUserSignupNotification();
									if (enabled != null && enabled.booleanValue()) {
										try {
											
											IMailService mailService = Activator
													.getService(IMailService.class);
											mailService
													.sendMail(dynamicAdminconsoleConfiguration.getObject().getUserAccountManagement().getNotificationRecepients()
															,
															"New User Signup Notification",
															String.format(
																	"A new user is requesting access to Bindaas API.\nFirst Name : %s\nLast Name : %s\nEmail Address : %s\nReason : %s",
																	userRequest
																			.getFirstName(),
																	userRequest
																			.getLastName(),
																	userRequest
																			.getEmailAddress(),
																	userRequest
																			.getReason()));
										} catch (Exception e) {
											log.error("Email notification to the admin was not sent");
										}
									}
								} else {
									log.error("Unable to retrieve Bindaas Properties");
								}
							
						} catch (Exception e) {
							if (transaction != null)
								transaction.rollback();
							log.error(e);
							ErrorView.handleError(resp, e);

						} finally {
							session.close();
							req.getSession().invalidate();
						}
						} else {
							ErrorView.handleError(resp, new Exception(
									"Session Factory not available"));
						}

					} 
					
			} catch (Exception e1) {
				ErrorView.handleError(resp, e1);
			}
		}
		else
		{
			// send back to login page
			resp.sendRedirect(loginPage);
		}
			
			
			

	}

}
