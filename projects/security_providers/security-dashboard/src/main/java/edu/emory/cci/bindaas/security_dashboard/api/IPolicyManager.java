package edu.emory.cci.bindaas.security_dashboard.api;

import java.util.Set;

import edu.emory.cci.bindaas.security_dashboard.config.SecurityDashboardConfiguration;

public interface IPolicyManager {

	public void addAuthorizedMember(String resource , Set<String> groups) ;
	public Set<String> getAuthorizedGroups(String resource) throws Exception ;
	public void removeAuthorizedMember(String resource, Set<String> grous);
	
	public boolean isAllowedAccess(String user , String resource , SecurityDashboardConfiguration configuration) throws Exception;
	
	
}
