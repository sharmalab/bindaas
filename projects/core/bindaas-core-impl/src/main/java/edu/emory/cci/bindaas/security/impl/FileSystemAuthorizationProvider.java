package edu.emory.cci.bindaas.security.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.annotations.Expose;

import edu.emory.cci.bindaas.core.bundle.Activator;
import edu.emory.cci.bindaas.core.util.DynamicObject;
import edu.emory.cci.bindaas.core.util.ThreadSafe;
import edu.emory.cci.bindaas.framework.util.GSONUtil;
import edu.emory.cci.bindaas.security.api.IAuthorizationProvider;

public class FileSystemAuthorizationProvider implements IAuthorizationProvider{

	private Policies defaultPolicies;
	public Policies getDefaultPolicies() {
		return defaultPolicies;
	}



	public void setDefaultPolicies(Policies defaultPolicies) {
		this.defaultPolicies = defaultPolicies;
	}
	private DynamicObject<Policies> policies;
	
	public void init() throws Exception
	{
		policies = new DynamicObject<FileSystemAuthorizationProvider.Policies>("bindaas.authorization", defaultPolicies, Activator.getContext());
	}
	
	
	
	@Override
	public boolean isAuthorized(Map<String, String> userAttributes,
			final String username, final String resourceId, String actionId) throws Exception {
		
		final Policies policies = this.policies.getObject() ; // get reference to policies
		boolean authDecision = policies.allow(username, resourceId);
		return authDecision; 
	}
	
	public static class AuthEntry
	{
		private String username;
		private String resource;
		
		public AuthEntry(String username , String resource)
		{
			this.username = username;
			this.resource = resource;
		}
		
		@Override
		public int hashCode() {
			return username.hashCode() + resource.hashCode();
		}


		public boolean equals(Object o)
		{
			if(o instanceof AuthEntry)
			{
				AuthEntry r = (AuthEntry) o;
				if(r.username.equals(username)  && r.resource.equals(resource)) return true;
			}
			
			return false;
		}
	}
	
	public static class Policies implements ThreadSafe{
		 
		public List<RoleRule> getRoleRules() {
			return roleRules;
		}

		public void setRoleRules(List<RoleRule> roleRules) {
			this.roleRules = roleRules;
		}

		public List<ResourceRule> getResourceRules() {
			return resourceRules;
		}

		public void setResourceRules(List<ResourceRule> resourceRules) {
			this.resourceRules = resourceRules;
		}

		@Expose private List<RoleRule> roleRules;
		@Expose private List<ResourceRule> resourceRules;
		private Log log = LogFactory.getLog(getClass());
		
		public Object clone()
		{
			return GSONUtil.getGSONInstance().fromJson(GSONUtil.getGSONInstance().toJson(this), Policies.class);
		}
		
		public boolean allow(String username , String resourceRequested)
		{
			boolean authDecision = false;
			Set<String> userRoles = getUserRoles(username);
			List<ResourceRule> applicableRules = getApplicableRules(resourceRequested);
			
			for(ResourceRule resourceRule : applicableRules)
			{
				Set<String> allowedRoles = new HashSet<String>(resourceRule.allowedRolesOrUsers);
				allowedRoles.retainAll(userRoles);
				if(allowedRoles.isEmpty()) continue;
				authDecision = true;
				log.trace("Applied Authorization Rule\t" + resourceRule);
				break;
			}
			
			return authDecision;
		}
		
		public Set<String> getUserRoles(String username)
		{
			Set<String> roles = new HashSet<String>();
			if(username!=null)
			{
				for(RoleRule roleRule : roleRules)
				{
					if(roleRule.users.contains(username))
					{
						roles.addAll(roleRule.roles);
					}
				}
				roles.add(username);
			}
			
			return roles;
		}
		
		public List<ResourceRule> getApplicableRules(String resourceRequested)
		{
			List<ResourceRule> appResourceRules = new ArrayList<ResourceRule>();
			for(ResourceRule resourceRule : this.resourceRules)
			{
				Operator logic = resourceRule.logic; 
				
				if(logic == null) logic = Operator.EXACT;
				boolean add = false;
				switch (logic)
				{
				
				case STARTS_WITH:  if(resourceRequested !=null && resourceRequested.startsWith(resourceRule.resource)) add = true; break;
				case CONTAINS: if(resourceRequested !=null && resourceRequested.contains(resourceRule.resource)) add = true; break;
				case EXACT : if(resourceRequested !=null && resourceRequested.equals(resourceRule.resource)) add = true;
					
				}
				
				if(add) appResourceRules.add(resourceRule);
			}
			return appResourceRules;
		}

		@Override
		public void init() throws Exception {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	public static class RoleRule {
		@Expose private Set<String> users;
		@Expose private Set<String> roles;
		
		
		public Set<String> getUsers() {
			return users;
		}
		public void setUsers(Set<String> users) {
			this.users = users;
		}
		public Set<String> getRoles() {
			return roles;
		}
		public void setRoles(Set<String> roles) {
			this.roles = roles;
		}
	}
	
	
	public static class ResourceRule {
		@Expose private String resource ;
		@Expose private Operator logic ;
		@Expose private Set<String> allowedRolesOrUsers;
		
		public String getResource() {
			return resource;
		}

		public void setResource(String resource) {
			this.resource = resource;
		}

		public Operator getLogic() {
			return logic;
		}

		public void setLogic(Operator logic) {
			this.logic = logic;
		}

		public Set<String> getAllowedRolesOrUsers() {
			return allowedRolesOrUsers;
		}

		public void setAllowedRolesOrUsers(Set<String> allowedRolesOrUsers) {
			this.allowedRolesOrUsers = allowedRolesOrUsers;
		}

		public String toString()
		{
			return String.format("Resource [%s] Operator [%s] Roles %s", resource , logic , allowedRolesOrUsers);
		}
	}
	public static enum Operator {
		STARTS_WITH,CONTAINS,EXACT
	}
	

}
