package edu.emory.cci.bindaas.trusted_app_client.core;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import edu.emory.cci.bindaas.trusted_app_client.app.exception.ClientException;
import edu.emory.cci.bindaas.trusted_app_client.app.exception.ServerException;


public interface ITrustedAppClient {

public JsonObject getShortLivedAuthenticationToken( String protocol, String username , Integer lifetime ) throws ServerException, ClientException;

public JsonObject authorizeNewUser(String protocol, String username , Long epochTimeExpires  , String comments) throws ServerException, ClientException;
	
public JsonObject revokeAccess( String protocol, String username ,   String comments) throws ServerException, ClientException;

public JsonArray listAuthenticationTokens(String protocol)  throws ServerException, ClientException;
}
