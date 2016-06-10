package edu.emory.cci.bindaas.core.util;

import java.util.HashMap;
import java.util.Map;

import edu.emory.cci.bindaas.core.model.EntityEventType;
import edu.emory.cci.bindaas.framework.event.BindaasEvent;
import edu.emory.cci.bindaas.framework.event.BindaasEventConstants;
import edu.emory.cci.bindaas.framework.model.DeleteEndpoint;
import edu.emory.cci.bindaas.framework.model.Entity;
import edu.emory.cci.bindaas.framework.model.Profile;
import edu.emory.cci.bindaas.framework.model.QueryEndpoint;
import edu.emory.cci.bindaas.framework.model.SubmitEndpoint;
import edu.emory.cci.bindaas.framework.model.Workspace;

public class EventHelper {

	
	
	public static BindaasEvent createEntityEvent(Entity entity , EntityEventType entityEventType)
	{
				
		if(entity instanceof Workspace)
		{
			Map<String,Object> properties = new HashMap<String, Object>();
			properties.put("workspace", entity);
			switch(entityEventType)
			{
				case CREATE : return new BindaasEvent(BindaasEventConstants.CREATE_WORKSPACE_TOPIC  , properties) ;
				default:  throw new RuntimeException("Not a valid entityEventType");
					
			}
		}
		else if(entity instanceof Profile)
		{
			Map<String,Object> properties = new HashMap<String, Object>();
			properties.put("profile", entity);
			switch(entityEventType)
			{
				case CREATE : return new BindaasEvent(BindaasEventConstants.CREATE_PROFILE_TOPIC  , properties) ;
				case UPDATE : return new BindaasEvent(BindaasEventConstants.UPDATE_PROFILE_TOPIC  , properties) ;

				default:  throw new RuntimeException("Not a valid entityEventType");
					
			}
		}
		else if(entity instanceof QueryEndpoint)
		{
			Map<String,Object> properties = new HashMap<String, Object>();
			properties.put("queryEndpoint", entity);
			switch(entityEventType)
			{
				case CREATE : return new BindaasEvent(BindaasEventConstants.CREATE_QUERY_TOPIC  , properties) ;
				case UPDATE : return new BindaasEvent(BindaasEventConstants.UPDATE_QUERY_TOPIC  , properties) ;

				default:  throw new RuntimeException("Not a valid entityEventType");
					
			}
		}
		else if(entity instanceof DeleteEndpoint)
		{
			Map<String,Object> properties = new HashMap<String, Object>();
			properties.put("deleteEndpoint", entity);
			switch(entityEventType)
			{
				case CREATE : return new BindaasEvent(BindaasEventConstants.CREATE_DELETE_TOPIC  , properties) ;
				case UPDATE : return new BindaasEvent(BindaasEventConstants.UPDATE_DELETE_TOPIC  , properties) ;

				default:  throw new RuntimeException("Not a valid entityEventType");
					
			}
		}
		else if(entity instanceof SubmitEndpoint)
		{
			Map<String,Object> properties = new HashMap<String, Object>();
			properties.put("submitEndpoint", entity);
			switch(entityEventType)
			{
				case CREATE : return new BindaasEvent(BindaasEventConstants.CREATE_SUBMIT_TOPIC  , properties) ;
				case UPDATE : return new BindaasEvent(BindaasEventConstants.UPDATE_SUBMIT_TOPIC  , properties) ;

				default:  throw new RuntimeException("Not a valid entityEventType");
					
			}
		}
		else throw new RuntimeException("Not a valid entity");
	}


public static BindaasEvent createDeleteWorkspaceEvent(String workspace)
{
	
	Map<String,Object> properties = new HashMap<String, Object>();
	properties.put("workspaceName", workspace);
	
	return new BindaasEvent(BindaasEventConstants.DELETE_WORKSPACE_TOPIC  , properties);
}

public static BindaasEvent createDeleteProfileEvent(String workspace , String profile )
{
	
		
	Map<String,Object> properties = new HashMap<String, Object>();
	properties.put("workspaceName", workspace);
	properties.put("profileName", profile);
	return new BindaasEvent(BindaasEventConstants.DELETE_PROFILE_TOPIC  , properties);
}

public static BindaasEvent createDeleteQueryEndpointEvent(String workspace , String profile , String queryEndpoint )
{
	
		
	Map<String,Object> properties = new HashMap<String, Object>();
	properties.put("workspaceName", workspace);
	properties.put("profileName", profile);
	properties.put("queryEndpointName", queryEndpoint);
	
	return new BindaasEvent(BindaasEventConstants.DELETE_QUERY_TOPIC  , properties);
}

public static BindaasEvent createDeleteSubmitEndpointEvent(String workspace , String profile , String submitEndpoint )
{
	
		
	Map<String,Object> properties = new HashMap<String, Object>();
	properties.put("workspaceName", workspace);
	properties.put("profileName", profile);
	properties.put("submitEndpointName", submitEndpoint);
	
	return new BindaasEvent(BindaasEventConstants.DELETE_SUBMIT_TOPIC  , properties);
}

public static BindaasEvent createDeleteDeleteEndpointEvent(String workspace , String profile , String deleteEndpoint )
{
	
		
	Map<String,Object> properties = new HashMap<String, Object>();
	properties.put("workspaceName", workspace);
	properties.put("profileName", profile);
	properties.put("deleteEndpointName", deleteEndpoint);
	
	return new BindaasEvent(BindaasEventConstants.DELETE_DELETE_TOPIC  , properties);
}


}
