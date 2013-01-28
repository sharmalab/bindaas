package edu.emory.cci.bindaas.webconsole.servlet.views;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;

import edu.emory.cci.bindaas.security.api.BindaasUser;
import edu.emory.cci.bindaas.security.api.IAuditProvider;
import edu.emory.cci.bindaas.webconsole.AbstractRequestHandler;
import edu.emory.cci.bindaas.webconsole.Activator;

public class AuditView extends AbstractRequestHandler {

	private static String templateName = "audit.vt";
	private static Template template;
	private String uriTemplate;
	private Log log = LogFactory.getLog(getClass());

	public String getUriTemplate() {
		return uriTemplate;
	}

	public void setUriTemplate(String uriTemplate) {
		this.uriTemplate = uriTemplate;
	}

	static {
		template = Activator.getVelocityTemplateByName(templateName);
	}

	@Override
	public void handleRequest(HttpServletRequest request,
			HttpServletResponse response, Map<String, String> pathParameters)
			throws Exception {

		if (request.getMethod().equalsIgnoreCase("get")) {
			generateView(request, response, pathParameters);
		}
		else {
			throw new Exception("Http Method [" + request.getMethod()
					+ "] not allowed here");
		}

	}

	public void generateView(HttpServletRequest request,
			HttpServletResponse response, Map<String, String> pathParameters)
			throws Exception {
		IAuditProvider auditProvider = Activator
				.getService(IAuditProvider.class);
		List<Map<String,String>> messages = null;
		VelocityContext context = new VelocityContext();
		
		context.put(
				"bindaasUser",
				BindaasUser.class.cast(
						request.getSession().getAttribute("loggedInUser"))
						.getName());
		
		if (auditProvider != null) {
			messages = auditProvider.getAuditLogs();
			context.put("auditMessages", messages );
			
		} else {
			
			log.warn("No audit logs found");
			messages = new ArrayList<Map<String,String>>();
			
		}
		template.merge(context, response.getWriter());
	}

}
