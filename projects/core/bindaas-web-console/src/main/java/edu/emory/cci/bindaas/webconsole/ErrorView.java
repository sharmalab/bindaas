package edu.emory.cci.bindaas.webconsole;


import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class ErrorView  {

	
	private static Log log = LogFactory.getLog(ErrorView.class);
	
	public static void handleError(HttpServletResponse response , Exception e)
	{
		try {
			log.error("Server Error - Request could not be served" , e);
			response.sendError(500, e.getMessage());
		} catch (IOException e1) {
			log.error(e1);
		}
	}
}
