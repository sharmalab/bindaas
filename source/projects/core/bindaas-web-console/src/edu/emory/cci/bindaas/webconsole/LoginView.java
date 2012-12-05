package edu.emory.cci.bindaas.webconsole;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;

public class LoginView {
	
	private static String templateName = "login.vt";
	private static Template template;
	
	static {
		template = Activator.getVelocityTemplateByName(templateName);
	}
	
	public static void generateLoginView(HttpServletRequest request , HttpServletResponse response , String loginTarget , String errorMessage) throws Exception
	{
		VelocityContext context = new VelocityContext();
		context.put("loginTarget", loginTarget);
		context.put("errorMessage", errorMessage);
		template.merge(context, response.getWriter());
		
	}
}
