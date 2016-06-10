package edu.emory.cci.bindaas.webconsole.admin;

import javax.servlet.http.HttpServletRequest;

import com.google.gson.JsonObject;

public interface IAdminAction {

	public String getActionName();
	public String doAction(JsonObject payload , HttpServletRequest request) throws Exception;
}
