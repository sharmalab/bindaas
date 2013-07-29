package edu.emory.cci.bindaas.hearbeat.api;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

public interface IHearbeatMonitorService {

	@Path("/send")
	@POST
	public Response sendHeartBeat( String body) ;
}
