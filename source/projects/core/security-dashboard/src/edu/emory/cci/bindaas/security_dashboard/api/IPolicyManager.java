package edu.emory.cci.bindaas.security_dashboard.api;

import java.util.Set;

import edu.emory.cci.bindaas.security_dashboard.config.SecurityDashboardConfiguration;
import edu.emory.cci.bindaas.security_dashboard.model.Group;

public interface IPolicyManager {

	public void addAuthorizedMember(String resource , Set<String> groups) ;
	public Set<Group> getAuthorizedGroups(String resource) ;
	public void removeAuthorizedMember(String resource, Set<String> grous);
	
	public boolean isAllowedAccess(String user , String resource , SecurityDashboardConfiguration configuration);
	
	
}
