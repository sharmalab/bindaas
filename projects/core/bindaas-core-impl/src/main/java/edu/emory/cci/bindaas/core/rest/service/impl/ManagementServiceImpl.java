package edu.emory.cci.bindaas.core.rest.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.helpers.IOUtils;
import org.apache.cxf.phase.PhaseInterceptorChain;
import org.apache.cxf.security.SecurityContext;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import edu.emory.cci.bindaas.core.api.IManagementTasks;
import edu.emory.cci.bindaas.core.rest.service.api.IManagementService;
import edu.emory.cci.bindaas.core.util.RestUtils;
import edu.emory.cci.bindaas.framework.model.DeleteEndpoint;
import edu.emory.cci.bindaas.framework.model.Profile;
import edu.emory.cci.bindaas.framework.model.QueryEndpoint;
import edu.emory.cci.bindaas.framework.model.SubmitEndpoint;
import edu.emory.cci.bindaas.framework.model.Workspace;
import edu.emory.cci.bindaas.framework.util.GSONUtil;

public class ManagementServiceImpl implements IManagementService {

	public IManagementTasks getManagementTask() {
		return managementTask;
	}

	public void setManagementTask(IManagementTasks managementTask) {
		this.managementTask = managementTask;
	}

	private Log log = LogFactory.getLog(getClass());
	private IManagementTasks managementTask;
	private JsonParser parser;
	private RestUtils restUtils;
	
	public RestUtils getRestUtils() {
		return restUtils;
	}

	public void setRestUtils(RestUtils restUtils) {
		this.restUtils = restUtils;
	}

	

	public void init() throws Exception {
		parser = GSONUtil.getJsonParser();
	}

	private String getUser() {
		SecurityContext securityContext = PhaseInterceptorChain
				.getCurrentMessage().get(SecurityContext.class);
		return securityContext.getUserPrincipal().getName();

	}

	private JsonObject parsePostRequest() throws IOException {

		InputStream content = PhaseInterceptorChain.getCurrentMessage()
				.getContent(InputStream.class);
		String rawRequest = IOUtils.readStringFromStream(content);
		log.trace("POST request:\n" + rawRequest);

		return parser.parse(rawRequest).getAsJsonObject();
	}

	@Override
	@Path("{workspace}")
	@POST
	public Response createWorkspace(@PathParam("workspace") String workspace) {
		String user = getUser();
		try {
			JsonObject params = null;
			try {
				params = parsePostRequest();
			} catch (Exception e) {
				params = new JsonObject();
			}

			Workspace works = managementTask.createWorkspace(workspace, params,
					user);
			
			
			
			return restUtils.createJsonResponse(works.toString());
		} catch (Exception e) {
			log.error(e);
			return restUtils.createErrorResponse(e.getMessage());
		}

	}

	@Override
	@Path("{workspace}/{profile}")
	@POST
	public Response createProfile(@PathParam("workspace") String workspace,
			@PathParam("profile") String profile ,  @QueryParam("description") String description) {
		String user = getUser();
		try {

			Profile prof = managementTask.createProfile(profile, workspace,
					parsePostRequest(), user , description);
		
			return restUtils.createJsonResponse(prof.toString());
		} catch (Exception e) {
			log.error(e);
			return restUtils.createErrorResponse(e.getMessage());
		}
	}

	@Override
	@Path("{workspace}/{profile}/query/{queryEndpoint}")
	@POST
	public Response createQueryEndpoint(
			@PathParam("workspace") String workspace,
			@PathParam("profile") String profile,
			@PathParam("queryEndpoint") String queryEndpoint) {

		String user = getUser();
		try {

			QueryEndpoint qe = managementTask
					.createQueryEndpoint(queryEndpoint, workspace, profile,
							parsePostRequest(), user);
			
			return restUtils.createJsonResponse(qe.toString());
		} catch (Exception e) {
			log.error(e);
			return restUtils.createErrorResponse(e.getMessage());
		}
	}

	@Override
	@Path("{workspace}/{profile}/delete/{deleteEndpoint}")
	@POST
	public Response createDeleteEndpoint(
			@PathParam("workspace") String workspace,
			@PathParam("profile") String profile,
			@PathParam("deleteEndpoint") String deleteEndpoint) {
		String user = getUser();
		try {

			DeleteEndpoint de = managementTask.createDeleteEndpoint(
					deleteEndpoint, workspace, profile, parsePostRequest(),
					user);
			
			return restUtils.createJsonResponse(de.toString());
		} catch (Exception e) {
			log.error(e);
			return restUtils.createErrorResponse(e.getMessage());
		}
	}

	@Override
	@Path("{workspace}/{profile}/submit/{submitEndpoint}")
	@POST
	public Response createSubmitEndpoint(
			@PathParam("workspace") String workspace,
			@PathParam("profile") String profile,
			@PathParam("submitEndpoint") String submitEndpoint) {
		String user = getUser();
		try {

			SubmitEndpoint se = managementTask.createSubmitEndpoint(
					submitEndpoint, workspace, profile, parsePostRequest(),
					user);
			
			return restUtils.createJsonResponse(se.toString());
		} catch (Exception e) {
			log.error(e);
			return restUtils.createErrorResponse(e.getMessage());
		}
	}

	@Override
	@Path("{workspace}")
	@DELETE
	public Response deleteWorkspace(@PathParam("workspace") String workspace) {

		try {

			managementTask.deleteWorkspace(workspace);
			
			return restUtils.createSuccessResponse("Workspace [" + workspace
					+ "] deleted");
		} catch (Exception e) {
			log.error(e);
			return restUtils.createErrorResponse(e.getMessage());
		}
	}

	@Override
	@Path("{workspace}/{profile}")
	@DELETE
	public Response deleteProfile(@PathParam("workspace") String workspace,
			@PathParam("profile") String profile) {
		try {

			managementTask.deleteProfile(workspace, profile);
			
			return restUtils.createSuccessResponse("Profile [" + profile
					+ "] deleted");
		} catch (Exception e) {
			log.error(e);
			return restUtils.createErrorResponse(e.getMessage());
		}
	}

	@Override
	@Path("{workspace}/{profile}/query/{queryEndpoint}")
	@DELETE
	public Response deleteQueryEndpoint(
			@PathParam("workspace") String workspace,
			@PathParam("profile") String profile,
			@PathParam("queryEndpoint") String queryEndpoint) {
		try {
			managementTask.deleteQueryEndpoint(workspace, profile,
					queryEndpoint);
			
			return restUtils.createSuccessResponse("QueryEndpoint ["
					+ queryEndpoint + "] deleted");
		} catch (Exception e) {
			log.error(e);
			return restUtils.createErrorResponse(e.getMessage());
		}
	}

	@Override
	@Path("{workspace}/{profile}/delete/{deleteEndpoint}")
	@DELETE
	public Response deleteDeleteEndpoint(
			@PathParam("workspace") String workspace,
			@PathParam("profile") String profile,
			@PathParam("deleteEndpoint") String deleteEndpoint) {
		try {
			managementTask.deleteDeleteEndpoint(workspace, profile,
					deleteEndpoint);
			
			return restUtils.createSuccessResponse("DeleteEndpoint ["
					+ deleteEndpoint + "] deleted");
		} catch (Exception e) {
			log.error(e);
			return restUtils.createErrorResponse(e.getMessage());
		}
	}

	@Override
	@Path("{workspace}/{profile}/submit/{submitEndpoint}")
	@DELETE
	public Response deleteSubmitEndpoint(
			@PathParam("workspace") String workspace,
			@PathParam("profile") String profile,
			@PathParam("submitEndpoint") String submitEndpoint) {
		try {
			managementTask.deleteSubmitEndpoint(workspace, profile,
					submitEndpoint);
			
			return restUtils.createSuccessResponse("SubmitEndpoint ["
					+ submitEndpoint + "] deleted");
		} catch (Exception e) {
			log.error(e);
			return restUtils.createErrorResponse(e.getMessage());
		}
	}

	@Override
	@Path("{workspace}/{profile}")
	@PUT
	public Response updateProfile(@PathParam("workspace") String workspace,
			@PathParam("profile") String profileName , @QueryParam("description") String description) {
		try {
			Profile profile = managementTask.updateProfile(profileName,
					workspace, parsePostRequest(), getUser() , description);
			
			return restUtils.createJsonResponse(profile.toString());
		} catch (Exception e) {
			log.error(e);
			return restUtils.createErrorResponse(e.getMessage());
		}

	}

	@Override
	@Path("{workspace}/{profile}/query/{queryEndpoint}")
	@PUT
	public Response updateQueryEndpoint(
			@PathParam("workspace") String workspace,
			@PathParam("profile") String profile,
			@PathParam("queryEndpoint") String queryEndpoint) {
		try {
			QueryEndpoint qe = managementTask.updateQueryEndpoint(
					queryEndpoint, workspace, profile, parsePostRequest(),
					getUser());
			
			return restUtils.createJsonResponse(qe.toString());
		} catch (Exception e) {
			log.error(e);
			return restUtils.createErrorResponse(e.getMessage());
		}

	}

	@Override
	@Path("{workspace}/{profile}/delete/{deleteEndpoint}")
	@PUT
	public Response updateDeleteEndpoint(
			@PathParam("workspace") String workspace,
			@PathParam("profile") String profile,
			@PathParam("deleteEndpoint") String deleteEndpoint) {
		try {
			DeleteEndpoint de = managementTask.updateDeleteEndpoint(
					deleteEndpoint, workspace, profile, parsePostRequest(),
					getUser());
			
			return restUtils.createJsonResponse(de.toString());
		} catch (Exception e) {
			log.error(e);
			return restUtils.createErrorResponse(e.getMessage());
		}
	}

	@Override
	@Path("{workspace}/{profile}/submit/{submitEndpoint}")
	@PUT
	public Response updateSubmitEndpoint(
			@PathParam("workspace") String workspace,
			@PathParam("profile") String profile,
			@PathParam("submitEndpoint") String submitEndpoint) {
		try {
			SubmitEndpoint se = managementTask.updateSubmitEndpoint(
					submitEndpoint, workspace, profile, parsePostRequest(),
					getUser());
			
			return restUtils.createJsonResponse(se.toString());
		} catch (Exception e) {
			log.error(e);
			return restUtils.createErrorResponse(e.getMessage());
		}
	}

	@Override
	@Path("{workspace}")
	@GET
	public Response getWorkspace(@PathParam("workspace") String workspace) {

		try {
			Workspace works = managementTask.getWorkspace(workspace);
			return restUtils.createJsonResponse(works.toString());
		} catch (NullPointerException ne) {
			log.error(ne);
			return restUtils.createResponse("Resource not found", 404);
		} catch (Exception e) {
			log.error(e);
			return restUtils.createErrorResponse(e.getMessage());
		}
	}

	@Override
	@Path("{workspace}/{profile}")
	@GET
	public Response getProfile(@PathParam("workspace") String workspace,
			@PathParam("profile") String profile) {
		try {
			Profile prof = managementTask.getProfile(workspace, profile);
			return restUtils.createJsonResponse(prof.toString());
		} catch (NullPointerException ne) {
			log.error(ne);
			return restUtils.createResponse("Resource not found", 404);
		} catch (Exception e) {
			log.error(e);
			return restUtils.createErrorResponse(e.getMessage());
		}
	}

	@Override
	@Path("{workspace}/{profile}/query/{queryEndpoint}")
	@GET
	public Response getQueryEndpoint(@PathParam("workspace") String workspace,
			@PathParam("profile") String profile,
			@PathParam("queryEndpoint") String queryEndpoint) {
		try {
			QueryEndpoint qe = managementTask.getQueryEndpoint(workspace,
					profile, queryEndpoint);
			return restUtils.createJsonResponse(qe.toString());
		} catch (NullPointerException ne) {
			log.error(ne);
			return restUtils.createResponse("Resource not found", 404);
		} catch (Exception e) {
			log.error(e);
			return restUtils.createErrorResponse(e.getMessage());
		}
	}

	@Override
	@Path("{workspace}/{profile}/delete/{deleteEndpoint}")
	@GET
	public Response getDeleteEndpoint(@PathParam("workspace") String workspace,
			@PathParam("profile") String profile,
			@PathParam("deleteEndpoint") String deleteEndpoint) {
		try {
			DeleteEndpoint de = managementTask.getDeleteEndpoint(workspace,
					profile, deleteEndpoint);
			return restUtils.createJsonResponse(de.toString());
		} catch (NullPointerException ne) {
			log.error(ne);
			return restUtils.createResponse("Resource not found", 404);
		} catch (Exception e) {
			log.error(e);
			return restUtils.createErrorResponse(e.getMessage());
		}
	}

	@Override
	@Path("{workspace}/{profile}/submit/{submitEndpoint}")
	@GET
	public Response getSubmitEndpoint(@PathParam("workspace") String workspace,
			@PathParam("profile") String profile,
			@PathParam("submitEndpoint") String submitEndpoint) {
		try {
			SubmitEndpoint se = managementTask.getSubmitEndpoint(workspace,
					profile, submitEndpoint);
			return restUtils.createSuccessResponse(se.toString());
		} catch (NullPointerException ne) {
			log.error(ne);
			return restUtils.createResponse("Resource not found", 404);
		} catch (Exception e) {
			log.error(e);
			return restUtils.createErrorResponse(e.getMessage());
		}
	}

	@Override
	@Path("{workspace}/{profile}/query/{queryEndpoint}/publish")
	@POST
	public Response publishQueryEndpoint(
			@PathParam("workspace") String workspace,
			@PathParam("profile") String profile,
			@PathParam("queryEndpoint") String queryEndpoint) throws Exception {
		try {
			managementTask.publishQueryEndpoint(workspace, profile,
					queryEndpoint);
			return restUtils.createSuccessResponse("QueryEndpoint ["
					+ queryEndpoint + "] published");
		} catch (Exception e) {
			log.error(e);
			return restUtils.createErrorResponse(e.getMessage());
		}

	}

	@Override
	@Path("{workspace}/{profile}/delete/{deleteEndpoint}/publish")
	@POST
	public Response publishDeleteEndpoint(
			@PathParam("workspace") String workspace,
			@PathParam("profile") String profile,
			@PathParam("deleteEndpoint") String deleteEndpoint) {
		try {
			managementTask.publishDeleteEndpoint(workspace, profile,
					deleteEndpoint);
			return restUtils.createSuccessResponse("DeleteEndpoint ["
					+ deleteEndpoint + "] published");
		} catch (Exception e) {
			log.error(e);
			return restUtils.createErrorResponse(e.getMessage());
		}

	}

	@Override
	@Path("workspaces")
	@GET
	public Response listWorkspaces() {
		try {
			Collection<Workspace> listOfWorkspaces = managementTask
					.listWorkspaces();
			if(listOfWorkspaces!=null)
			{
				String jsonStr = GSONUtil.getGSONInstance().toJson(listOfWorkspaces);
				return restUtils.createJsonResponse(jsonStr);
			}
			else
			{
				return restUtils.createJsonResponse("[]");
			}
		} catch (Exception e) {
			log.error(e);
			return restUtils.createErrorResponse(e.getMessage());
		}
		
	}

}
