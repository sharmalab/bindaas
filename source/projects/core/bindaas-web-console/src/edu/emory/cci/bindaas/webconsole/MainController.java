package edu.emory.cci.bindaas.webconsole;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.emory.cci.bindaas.webconsole.util.UriTemplate;

public class MainController extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private Log log = LogFactory.getLog(getClass());
	private List<IRequestHandler> requestHandlers;
	
	public List<IRequestHandler> getRequestHandlers() {
		return requestHandlers;
	}

	public void setRequestHandlers(List<IRequestHandler> requestHandlers) {
		this.requestHandlers = requestHandlers;
	}

	

	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		handleRequest(req , resp);
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		handleRequest(req , resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		handleRequest(req , resp);
	}

	public void handleRequest(HttpServletRequest request,
			HttpServletResponse response) {
		try {
			IRequestHandler handler = null;
			Map<String,String> parameters = null;
			String[] pathElements = request.getPathInfo().split("/");
	
			for(IRequestHandler requestHandler : requestHandlers)
			{
				UriTemplate template = new UriTemplate(requestHandler.getUriTemplateSegments()	, pathElements);
				if(template.isMatch())
				{
					parameters = template.getParameters();
					handler = requestHandler;
					break;
				}
			}
			
			if (handler != null) {
				
				handler.handleRequest(request, response, parameters);
				
			} else {
				throw new Exception("No handler found for request path ["
						+ request.getPathInfo() + "]");
			}
		} catch (Exception e) {
			log.error(e);
			ErrorView.handleError(response, e);
		}
	}
	
	public void init()
	{
		log.trace("Main Controller Initialized");
	}

}
