package edu.emory.cci.bindaas.lite.misc;

import java.io.IOException;
import java.net.URLEncoder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;

import edu.emory.cci.bindaas.lite.RegistrableServlet;
import edu.emory.cci.bindaas.lite.util.VelocityEngineWrapper;
/**
 * 	/bindaas/lite/error
 * @author nadir
 *
 */
public class GeneralServerErrorServlet extends RegistrableServlet{
	private static final long serialVersionUID = 1L;
	private VelocityEngineWrapper velocityWrapper;
	private String templateName; //edu.emory.cci.bindaas.lite.login_loginServlet.html
	private Template template;
	private Log log = LogFactory.getLog(getClass());
	

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		String message = req.getParameter("message");
		if(message == null) message = "Internal Server Error";
		
		VelocityContext context = velocityWrapper.createVelocityContext();
		context.put("message", message);
		template.merge(context, resp.getWriter());
	}
	
	public VelocityEngineWrapper getVelocityWrapper() {
		return velocityWrapper;
	}



	public void setVelocityWrapper(VelocityEngineWrapper velocityWrapper) {
		this.velocityWrapper = velocityWrapper;
	}



	public String getTemplateName() {
		return templateName;
	}



	public void setTemplateName(String templateName) {
		this.templateName = templateName;
	}


	@Override
	public void init() throws ServletException {
		template = velocityWrapper.getVelocityTemplateByName(templateName);
	}
	
	public void redirect(HttpServletResponse response , String message) throws IOException
	{
		log.error(message);
		String redirectUrl = String.format("%s?message=%s" , getServletPath() , URLEncoder.encode(message,"UTF-8"));
		response.sendRedirect(redirectUrl);
	}
	
	public void redirect(HttpServletResponse response , Exception e) throws IOException
	{
		log.error("Internal Server Error", e);
		String redirectUrl = String.format("%s?message=%s" , getServletPath() , URLEncoder.encode(e.getMessage(),"UTF-8"));
		response.sendRedirect(redirectUrl);
	}
}
