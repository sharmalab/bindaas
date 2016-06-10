package edu.emory.cci.bindaas.lite.dashboard;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;

import edu.emory.cci.bindaas.lite.RegistrableServlet;
import edu.emory.cci.bindaas.lite.misc.GeneralServerErrorServlet;
import edu.emory.cci.bindaas.lite.projects.ProjectExplorerServlet;
import edu.emory.cci.bindaas.lite.util.VelocityEngineWrapper;

public class DashboardServlet extends RegistrableServlet {

	private static final long serialVersionUID = 1L;
	private Log log = LogFactory.getLog(getClass());
	private VelocityEngineWrapper velocityWrapper;
	private String templateName; // edu.emory.cci.bindaas.lite.dashboard_dashboard.html
	private Template template;
	private GeneralServerErrorServlet errorServlet;
	private ProjectExplorerServlet projectExplorerServlet; 

	public ProjectExplorerServlet getProjectExplorerServlet() {
		return projectExplorerServlet;
	}

	public void setProjectExplorerServlet(
			ProjectExplorerServlet projectExplorerServlet) {
		this.projectExplorerServlet = projectExplorerServlet;
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

	public GeneralServerErrorServlet getErrorServlet() {
		return errorServlet;
	}

	public void setErrorServlet(GeneralServerErrorServlet errorServlet) {
		this.errorServlet = errorServlet;
	}

	@Override
	public void init() throws ServletException {
		template = velocityWrapper.getVelocityTemplateByName(templateName);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		try {
			VelocityContext context = velocityWrapper.createVelocityContext();
			context.put("projectExplorerUrl", projectExplorerServlet.getServletPath());
			template.merge(context, resp.getWriter()); 

		}

		catch (Exception e) {
			log.error(e);
			errorServlet.redirect(resp, e);
		}

	}

}
