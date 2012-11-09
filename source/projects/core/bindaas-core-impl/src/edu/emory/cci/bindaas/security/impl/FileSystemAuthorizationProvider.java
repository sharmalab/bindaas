package edu.emory.cci.bindaas.security.impl;

import java.util.Arrays;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import edu.emory.cci.bindaas.core.bundle.Activator;
import edu.emory.cci.bindaas.security.api.IAuthorizationProvider;

public class FileSystemAuthorizationProvider implements IAuthorizationProvider{

	public void init()
	{
		Dictionary<String, String> props = new Hashtable<String, String>();
		props.put("class", FileSystemAuthorizationProvider.class.getName());
		Activator.getContext().registerService(IAuthorizationProvider.class.getName(), this, props);
	}
	
	
	/**
	 * Properties :
	 * user.{user}={role1},{role2} ...
	 * 
	 * 
	 * resource.{url}={role1}/{user}
	 * 
	 * 
	 * 
	 */
	
	@Override
	public boolean isAuthorized(Map<String, String> userAttributes,
			String username, String resourceId, String actionId,
			Properties props) throws Exception {
		
		Map<String,Set<String>> rules = createRuleMap(props);
		Map<String,Set<String>> roles = createRoleMap(props);
		Set<String> assoicatedRoles = roles.get(username);
		
		Set<String> rolesAllowed = getAllowedRoles(resourceId, rules);
		
		if(rolesAllowed!=null )
		{
		
			for(String allowedRole : rolesAllowed)
			{
				if( (assoicatedRoles!=null && assoicatedRoles.contains(allowedRole)) || allowedRole.equals(username))
					return true;
			}
		}
		
		return false; // Default is deny all
	}
	
	
	// get roles assoicated with this url and all its prefixes
	private Set<String> getAllowedRoles(String resourceId , Map<String,Set<String>> rules)
	{
		Set<String> setOfAllowedRoles = new HashSet<String>();
		
		for(String url : rules.keySet())
		{
			if( resourceId.startsWith(url))
			{
				setOfAllowedRoles.addAll(rules.get(url));
			}
		}
		
		return setOfAllowedRoles;
	}
	
	private Map<String,Set<String>> createRoleMap(Properties prop)
	{
		Map<String,Set<String>> retVal = new HashMap<String, Set<String>>();
		for(Object key : prop.keySet())
		{
			if(key.toString().startsWith("user."))
			{
				String id = key.toString().replace("user.", "");
				String value = prop.getProperty(key.toString());
				String[] roles = value.split(",");
				Set<String> set = new HashSet<String>();
				set.addAll(Arrays.asList(roles));
				retVal.put(id, set);
			}
		}
		return retVal;
	}
	
	private Map<String,Set<String>> createRuleMap(Properties prop)
	{
		Map<String,Set<String>> retVal = new HashMap<String, Set<String>>();
		for(Object key : prop.keySet())
		{
			if(key.toString().startsWith("resource."))
			{
				String id = key.toString().replace("resource.", "");
				String value = prop.getProperty(key.toString());
				String[] roles = value.split(",");
				Set<String> set = new HashSet<String>();
				set.addAll(Arrays.asList(roles));
				retVal.put(id, set);
			}
		}
		return retVal;
	}
	

}
