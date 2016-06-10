package edu.emory.cci.bindaas.lite.projects;

import java.io.BufferedReader;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.emory.cci.bindaas.core.api.IManagementTasks;
import edu.emory.cci.bindaas.lite.RegistrableServlet;
import edu.emory.cci.bindaas.lite.misc.GeneralServerErrorServlet;

// registered /bindaas/lite/admin/projects/dataProvider/edit
public class EditDataProviderServlet extends RegistrableServlet{

	private static final long serialVersionUID = 1L;
	private Log log = LogFactory.getLog(getClass());
	private IManagementTasks managementTask;
	private GeneralServerErrorServlet errorServlet;
	
	public IManagementTasks getManagementTask() {
		return managementTask;
	}
	public void setManagementTask(IManagementTasks managementTask) {
		this.managementTask = managementTask;
	}
	public GeneralServerErrorServlet getErrorServlet() {
		return errorServlet;
	}
	public void setErrorServlet(GeneralServerErrorServlet errorServlet) {
		this.errorServlet = errorServlet;
	}
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		BufferedReader reader = req.getReader();
		StringBuilder builder = new StringBuilder();
		String buff;
		while((buff = reader.readLine())!=null)
		{
			builder.append(buff);
		}
		
		log.debug(builder.toString());
		resp.setStatus(200);
		resp.getWriter().append("random error");
	}
	@Override
	public void init() throws ServletException {
	
		super.init();
	}

	
}
