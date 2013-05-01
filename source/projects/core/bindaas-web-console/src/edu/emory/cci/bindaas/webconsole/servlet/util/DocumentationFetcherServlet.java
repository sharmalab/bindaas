package edu.emory.cci.bindaas.webconsole.servlet.util;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.JsonObject;

import edu.emory.cci.bindaas.core.api.IProviderRegistry;
import edu.emory.cci.bindaas.framework.api.IProvider;
import edu.emory.cci.bindaas.framework.api.IQueryModifier;
import edu.emory.cci.bindaas.framework.api.IQueryResultModifier;
import edu.emory.cci.bindaas.framework.api.ISubmitPayloadModifier;
import edu.emory.cci.bindaas.framework.util.StandardMimeType;
import edu.emory.cci.bindaas.webconsole.ErrorView;
import edu.emory.cci.bindaas.webconsole.bundle.Activator;

public class DocumentationFetcherServlet extends HttpServlet {

	
	private static final long serialVersionUID = 1L;
	private IProviderRegistry providerReg;

	public IProviderRegistry getProviderReg() {
		return providerReg;
	}

	public void setProviderReg(IProviderRegistry providerReg) {
		this.providerReg = providerReg;
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		String type = request.getParameter("type");
		String className = request.getParameter("class");
		
		if(type!=null && className!=null)
		{
			if(type.equals("IProvider"))
			{
				
				
				if(providerReg!=null && providerReg.findProvider(className)!=null && providerReg.findProvider(className).size() > 0)
				{
					IProvider provider = providerReg.findProvider(className).iterator().next();
					JsonObject documentation = provider.getDocumentation();
					response.setContentType(StandardMimeType.JSON.toString());
					response.getWriter().print(documentation.toString());
				}
				else
				{
					ErrorView.handleError(response, new Exception("No provider matching class [" + className + "]"));
				}
			} else if(type.equals("IQueryModifier"))
			{
				IQueryModifier queryModifier = Activator.getService(IQueryModifier.class, String.format("(class=%s)", className));
				if(queryModifier!=null)
				{
					JsonObject documentation = queryModifier.getDocumentation();
					response.setContentType(StandardMimeType.JSON.toString());
					response.getWriter().print(documentation.toString());
				}
				else
				{
					ErrorView.handleError(response, new Exception("No queryModifier matching class [" + className + "]"));
				}
			}else if(type.equals("IQueryResultModifier"))
			{
				IQueryResultModifier queryResultModifier = Activator.getService(IQueryResultModifier.class, String.format("(class=%s)", className));
				if(queryResultModifier!=null)
				{
					JsonObject documentation = queryResultModifier.getDocumentation();
					response.setContentType(StandardMimeType.JSON.toString());
					response.getWriter().print(documentation.toString());
				}
				else
				{
					ErrorView.handleError(response, new Exception("No queryResultModifier matching class [" + className + "]"));
				}
			}
			else if(type.equals("ISubmitPayloadModifier"))
			{
				ISubmitPayloadModifier submitPayloadModifier = Activator.getService(ISubmitPayloadModifier.class, String.format("(class=%s)", className));
				if(submitPayloadModifier!=null)
				{
					JsonObject documentation = submitPayloadModifier.getDocumentation();
					response.setContentType(StandardMimeType.JSON.toString());
					response.getWriter().print(documentation.toString());
				}
				else
				{
					ErrorView.handleError(response, new Exception("No submitPayloadModifier matching class [" + className + "]"));
				}
			}
			else
			{
				ErrorView.handleError(response, new Exception("Unrecognized type [" + type + "]"));
			}
		}
		else
		{
			ErrorView.handleError(response, new Exception("Mandatory fields type or class not specified"));
		}
		
		
	}

}
