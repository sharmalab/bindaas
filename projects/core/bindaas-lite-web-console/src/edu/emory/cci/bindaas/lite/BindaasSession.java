package edu.emory.cci.bindaas.lite;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import edu.emory.cci.bindaas.security.api.BindaasUser;

public class BindaasSession {
	
	private BindaasUser bindaasUser;
	private final static String SESSION_ATTRIBUTE_NAME = "bindaas-session";
	private String loginTarget;
	private String apiKey;
	
	
	
	public String getApiKey() {
		return apiKey;
	}
	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}
	public String getLoginTarget() {
		return loginTarget;
	}
	public void setLoginTarget(String loginTarget) {
		this.loginTarget = loginTarget;
	}
	public BindaasUser getBindaasUser() {
		return bindaasUser;
	}
	public void setBindaasUser(BindaasUser bindaasUser) {
		this.bindaasUser = bindaasUser;
	}
	
	public static BindaasSession getBindaasSession(HttpServletRequest servletRequest)
	{
		if(servletRequest.getSession()!=null && servletRequest.getSession().getAttribute(SESSION_ATTRIBUTE_NAME)!=null && servletRequest.getSession().getAttribute(SESSION_ATTRIBUTE_NAME) instanceof BindaasSession)
		{
			return (BindaasSession) servletRequest.getSession().getAttribute(SESSION_ATTRIBUTE_NAME);
		}
		else
			return null;
	}
	
	public static void setBindaasSession(HttpSession httpSession , BindaasSession bindaasSession)
	{
		httpSession.setAttribute(SESSION_ATTRIBUTE_NAME, bindaasSession);
	}
	
}
