package edu.emory.cci.bindaas.webconsole.servlet.usermgmt;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URLEncoder;
import java.util.UUID;

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

import edu.emory.cci.bindaas.commons.mail.api.IMailService;
import edu.emory.cci.bindaas.core.api.BindaasConstants;
import edu.emory.cci.bindaas.core.config.BindaasConfiguration;
import edu.emory.cci.bindaas.core.model.hibernate.VerifyEmail;
import edu.emory.cci.bindaas.core.rest.service.api.IBindaasAdminService;
import edu.emory.cci.bindaas.core.util.DynamicObject;
import edu.emory.cci.bindaas.webconsole.Activator;
import edu.emory.cci.bindaas.webconsole.ErrorView;
import edu.emory.cci.bindaas.webconsole.config.BindaasAdminConsoleConfiguration;

/**
 * Registered at /userRegistration
 * @author nadir
 *
 */
public class UserRegistrationServlet extends HttpServlet {
	public static final String servletLocation2VerifyEmailAccount = "/postSignup";
	private static String userRegistrationTemplateName = "userRegistration.vt";
	private static String simpleMessageTemplateName = "simpleMessage.vt";
	private static String userRegEmailTemplateName = "userRegEmail.vt";
	private static Template userRegistrationTemplate;
	private static Template simpleMessageTemplate;
	private static Template userRegEmailTemplate;
	public static final String servletLocation = "/userRegistration";
	
	static {
		userRegistrationTemplate = Activator.getVelocityTemplateByName(userRegistrationTemplateName);
		simpleMessageTemplate = Activator.getVelocityTemplateByName(simpleMessageTemplateName);
		userRegEmailTemplate = Activator.getVelocityTemplateByName(userRegEmailTemplateName);
	}
	
	private Log log = LogFactory.getLog(getClass());
	 

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		VelocityContext context = new VelocityContext();
		userRegistrationTemplate.merge(context, resp.getWriter());
	}

	
	// verify email 
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String emailAddress = req.getParameter("emailAddress");
		String firstName = req.getParameter("firstName");
		String lastName = req.getParameter("lastName");
		
		VerifyEmail verifyEmail = new VerifyEmail();
		verifyEmail.setEmailAddress(emailAddress);
		verifyEmail.setFirstName(firstName);
		verifyEmail.setLastName(lastName);
		verifyEmail.setVerificationCode(UUID.randomUUID().toString());
		
		SessionFactory sessionFactory = Activator.getService(SessionFactory.class);
		if(sessionFactory!=null)
		{
			Session session = sessionFactory.openSession();
			Transaction transaction = null;
			try{
				transaction = session.beginTransaction();
				session.save(verifyEmail);
				
				IMailService mailService = Activator.getService(IMailService.class);
				DynamicObject<BindaasAdminConsoleConfiguration> bindaasAdminConsoleConfiguration = Activator.getService(DynamicObject.class , "(name=bindaas.adminconsole)");
				if(mailService!=null && bindaasAdminConsoleConfiguration!=null)
				{
					VelocityContext context = new VelocityContext();
					context.put("firstName", firstName);
					context.put("lastName", lastName);
					
					String serviceUIUrl = bindaasAdminConsoleConfiguration.getObject().getProxyUrl(); 
					String verificationUrl = String.format("%s%s?verificationCode=%s", serviceUIUrl , servletLocation2VerifyEmailAccount , URLEncoder.encode(verifyEmail.getVerificationCode()));
					
					context.put("verificationUrl" , verificationUrl);
					
					StringWriter sw = new StringWriter();
					userRegEmailTemplate.merge(context, sw);
					mailService.sendMail(emailAddress,  "Please verify your email address" , sw.toString());
					
					
				}
				else
					throw new Exception("Mail/Admin Service not available");
				
				transaction.commit();
				VelocityContext context = new VelocityContext();
				context.put("message" , "Please check your email and follow the instructions");
				simpleMessageTemplate.merge(context, resp.getWriter());
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
			}
		}
		else
		{
			ErrorView.handleError(resp, new Exception("Session Factory not available"));
		}
		
	}
	
	
	
}
