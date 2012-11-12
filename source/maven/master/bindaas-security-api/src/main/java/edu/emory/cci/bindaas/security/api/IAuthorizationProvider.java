package edu.emory.cci.bindaas.security.api;

import java.util.Map;
import java.util.Properties;

public interface IAuthorizationProvider {

	public boolean isAuthorized(Map<String,String> userAttributes , String username , String resourceId , String actionId , Properties props) throws Exception;
	public static final String IP_ADDRESS = "ipAddress";
	public static final String TIME_OF_AUTHENTICATION = "authInstant";
	
	
}
