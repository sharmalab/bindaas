package edu.emory.cci.bindaas.webconsole;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface IRequestHandler {
	public void handleRequest(HttpServletRequest request, HttpServletResponse response , Map<String,String> pathParameters) throws Exception;
	public String getUriTemplate();
	public String[] getUriTemplateSegments();
}
