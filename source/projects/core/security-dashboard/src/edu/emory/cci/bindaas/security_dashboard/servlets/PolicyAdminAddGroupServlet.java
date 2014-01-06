package edu.emory.cci.bindaas.security_dashboard.servlets;

import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;

import edu.emory.cci.bindaas.security_dashboard.RegistrableServlet;
import edu.emory.cci.bindaas.security_dashboard.api.IPolicyManager;
import edu.emory.cci.bindaas.security_dashboard.model.Group;
import edu.emory.cci.bindaas.security_dashboard.util.RakshakUtils;

public class PolicyAdminAddGroupServlet extends RegistrableServlet {

	private static final long serialVersionUID = 1L;
	private String templateName;
	private Template template;
	private Log log = LogFactory.getLog(getClass());

	private IPolicyManager policyManager;

	public IPolicyManager getPolicyManager() {
		return policyManager;
	}

	public void setPolicyManager(IPolicyManager policyManager) {
		this.policyManager = policyManager;
	}

	public void init() {
		template = getVelocityEngineWrapper().getVelocityTemplateByName(
				templateName);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		try {
			String project = req.getParameter("project");
			String dataProvider = req.getParameter("dataProvider");
			String type = req.getParameter("type");
			String apiName = req.getParameter("apiName");

			String resource = String.format("%s/%s/%s/%s", project,
					dataProvider, type, apiName);
			Set<Group> remoteGroups = RakshakUtils
					.getAllGroups(getConfiguration());
			Set<String> alreadyAddedGroups = policyManager
					.getAuthorizedGroups(resource);
			Set<Group> finalGroups = new TreeSet<Group>();
			for (Group group : remoteGroups) {
				if (!alreadyAddedGroups.contains(group.getName())) {
					finalGroups.add(group);
				}
			}

			VelocityContext context = getVelocityEngineWrapper()
					.createVelocityContext(req);
			context.put("groups", finalGroups);

			template.merge(context, resp.getWriter());
		} catch (Exception e) {
			log.error(e);
			throw new ServletException(e);
		}

	}

}
