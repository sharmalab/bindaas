package edu.emory.cci.bindaas.security_dashboard.service;

import java.io.IOException;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.annotations.Expose;

import edu.emory.cci.bindaas.framework.util.GSONUtil;
import edu.emory.cci.bindaas.framework.util.IOUtils;
import edu.emory.cci.bindaas.security_dashboard.RegistrableServlet;
import edu.emory.cci.bindaas.security_dashboard.api.IPolicyManager;

/**
 *  modifyPolicy(project,data-provider,type,apiname,requestType = add | remove , listOfGroups )
 * @author nadir
 *
 */
public class ModifyPolicy extends RegistrableServlet {

private static final long serialVersionUID = 1L;
	
	private IPolicyManager policyManager;

	public IPolicyManager getPolicyManager() {
		return policyManager;
	}

	public void setPolicyManager(IPolicyManager policyManager) {
		this.policyManager = policyManager;
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String body = IOUtils.toString(req.getInputStream());
		ModifyPolicyRequest modifyPolicyRequest = GSONUtil.getGSONInstance().fromJson(body, ModifyPolicyRequest.class);
		String resource = String.format("%s/%s/%s/%s", modifyPolicyRequest.project , modifyPolicyRequest.dataProvider , modifyPolicyRequest.type , modifyPolicyRequest.apiName);
		
		if(modifyPolicyRequest.requestType.equalsIgnoreCase("add"))
		{
			policyManager.addAuthorizedMember(resource, modifyPolicyRequest.listOfGroups);
		}
		else if(modifyPolicyRequest.requestType.equalsIgnoreCase("remove"))
		{
			policyManager.removeAuthorizedMember(resource, modifyPolicyRequest.listOfGroups);
		}
		else
			throw new ServletException("Unknow requestType specified [" + modifyPolicyRequest.requestType + "]");
	}
	
	
	public static class ModifyPolicyRequest {
		@Expose private String project;
		@Expose private String dataProvider;
		@Expose private String type;
		@Expose private String apiName;
		@Expose private String requestType;
		@Expose private Set<String> listOfGroups;
	}
	
}
