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
import edu.emory.cci.bindaas.core.model.hibernate.VerifyEmail;
import edu.emory.cci.bindaas.core.util.DynamicProperties;
import edu.emory.cci.bindaas.security.api.BindaasUser;
import edu.emory.cci.bindaas.webconsole.Activator;
import edu.emory.cci.bindaas.webconsole.ErrorView;

/**
 * Registered at /postSignup 
 * Responsible for checking user identity and seeking reason for sign-up
 * @author nadir
 *
 */
public class PostSignupServlet extends HttpServlet {
	private static String templateName = "seekReason.vt";
	private static Template template;
	private static String attribute2check4bindaasUser = "newUserSignup";
	private static String simpleMessageTemplateName = "simpleMessage.vt";
	private static Template simpleMessageTemplate;
	
	static {
		template = Activator.getVelocityTemplateByName(templateName);
		simpleMessageTemplate = Activator.getVelocityTemplateByName(simpleMessageTemplateName);
	}
	
	private Log log = LogFactory.getLog(getClass());

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String verificationCode = req.getParameter("verificationCode");
		if(verificationCode!=null)
		{
			VerifyEmail verifiedEmail = verifyEmailAddress(verificationCode);
			if(verifiedEmail!=null)
			{
				BindaasUser bu = new BindaasUser(verifiedEmail.getEmailAddress());
				bu.addProperty(BindaasUser.EMAIL_ADDRESS, verifiedEmail.getEmailAddress());
				bu.addProperty(BindaasUser.FIRST_NAME, verifiedEmail.getFirstName());
				bu.addProperty(BindaasUser.LAST_NAME, verifiedEmail.getLastName());
				
				// set session
				
				req.getSession(true).setAttribute(attribute2check4bindaasUser, bu);
			}
			else
			{
				ErrorView.handleError(resp, new Exception("Your verification code is invalid"));
			}
		}
		
		BindaasUser bindaasUser = (BindaasUser) req.getSession().getAttribute(attribute2check4bindaasUser);
		if(bindaasUser != null)
		{
			VelocityContext context = new VelocityContext();
			context.put("firstName" , bindaasUser.getProperty(BindaasUser.FIRST_NAME).toString() );
			context.put("lastName" , bindaasUser.getProperty(BindaasUser.LAST_NAME).toString() );
			context.put("emailAddress" , bindaasUser.getProperty(BindaasUser.EMAIL_ADDRESS).toString() );
			
			template.merge(context, resp.getWriter());
			
		}
		else
		{
			resp.sendRedirect(UserRegistrationServlet.servletLocation);
		}
	}

	
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		BindaasUser bindaasUser = (BindaasUser) req.getSession().getAttribute(attribute2check4bindaasUser);
		if(bindaasUser!=null)
		{
			UserRequest userRequest = new UserRequest();
			userRequest.setFirstName(bindaasUser.getProperty(BindaasUser.FIRST_NAME).toString());
			userRequest.setLastName(bindaasUser.getProperty(BindaasUser.LAST_NAME).toString());
			userRequest.setEmailAddress(bindaasUser.getProperty(BindaasUser.EMAIL_ADDRESS).toString());
			
			String reason = req.getParameter("reason");
			userRequest.setReason(reason);
			
			userRequest.setStage("pending");
			
			SessionFactory sessionFactory = Activator.getService(SessionFactory.class);
			if(sessionFactory!=null)
			{
				Session session = sessionFactory.openSession();
				Transaction transaction = null;
				try{
					transaction = session.beginTransaction();
					// check to see if already one exists
					List list = session.createCriteria(UserRequest.class).add(Restrictions.eq("emailAddress", userRequest.getEmailAddress())).add(Restrictions.eq("stage", userRequest.getStage())).list();
					
					if(list!=null && list.size() > 0)
					{
						VelocityContext context = new VelocityContext();
						context.put("message", "Your application is already under review. You shall hear from us shortly.");
						simpleMessageTemplate.merge(context, resp.getWriter());
					}
					else
					{
						session.save(userRequest);
						transaction.commit();
						
						VelocityContext context = new VelocityContext();
						context.put("message", "Your application is under review. You shall hear from us shortly.");
						simpleMessageTemplate.merge(context, resp.getWriter());
						
						// send email notification to the admin
						
						DynamicProperties dynamicProperties = Activator.getService(DynamicProperties.class, "(name=bindaas)");
						if(dynamicProperties!=null)
						{
							Boolean enabled = dynamicProperties.get("webconsole.security.userRegistration.notification.enable")!=null ? Boolean.parseBoolean(dynamicProperties.get("webconsole.security.userRegistration.notification.enable")) : null;
							if(enabled!=null && enabled.booleanValue())
							{
								try {
								String recepient = (String) dynamicProperties.get("webconsole.security.userRegistration.notification.recepient");
								String[] recepientList = recepient.split(",");
								IMailService mailService = Activator.getService(IMailService.class);
								mailService.sendMail(Arrays.asList(recepientList) , "New User Signup Notification" , String.format("A new user is requesting access to Bindaas API.\nFirst Name : %s\nLast Name : %s\nEmail Address : %s\nReason : %s", userRequest.getFirstName() , userRequest.getLastName() , userRequest.getEmailAddress() , userRequest.getReason()));
								}catch (Exception e)
								{
									log.error("Email notification to the admin was not sent");
								}
							}
						}
						else
						{
							log.error("Unable to retrieve Bindaas Properties");
						}
					}
					
					
					
				}
				catch(Exception e)
				{
					if(transaction!=null)
						transaction.rollback();
					log.error(e);
					ErrorView.handleError(resp, e);
					
				}
				finally{
					session.close();
					req.getSession().invalidate();
				}
			}
			else
			{
				ErrorView.handleError(resp, new Exception("Session Factory not available"));	
			}

		}
		else
		{
			resp.sendRedirect(UserRegistrationServlet.servletLocation);
		}
	}
	
	/**
	 * Verify if an entry exist in the database. If it does delete it and return to the caller
	 * @param verificationCode
	 * @return
	 */
	
	private VerifyEmail verifyEmailAddress(String verificationCode)
	{
		SessionFactory sessionFactory = Activator.getService(SessionFactory.class);
		if(sessionFactory!=null)
		{
			Session session = sessionFactory.openSession();
			try{
					List<VerifyEmail> entries = session.createQuery("from VerifyEmail").list();
					if(entries!=null && entries.size() > 0)
					{
						session.beginTransaction();
						VerifyEmail emailEntry = entries.get(0); 
						session.delete( emailEntry );
						session.getTransaction().commit();
						return emailEntry ;
					}
			}
			catch (Exception e)
			{
				session.getTransaction().rollback();
				log.error(e);
			}
			finally{
				session.close();
			}
			
		}
		else
		{
			log.error("Session Factory not available");
		}
		
		return null;
	}
	
	
}
