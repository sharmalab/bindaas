package edu.emory.cci.bindaas.trusted_app_client.core;

import java.util.List;

import com.google.gson.JsonObject;
import edu.emory.cci.bindaas.trusted_app_client.app.exception.ClientException;
import edu.emory.cci.bindaas.trusted_app_client.app.exception.ServerException;


public interface ITrustedAppClient {

public APIKey getShortLivedAPIKey( String username , Integer lifetime ) throws ServerException, ClientException;

public JsonObject authorizeNewUser(String protocol, String username , Long epochTimeExpires  , String comments) throws ServerException, ClientException;
	
public String revokeAccess( String username ,   String comments) throws ServerException, ClientException;

public List<APIKey> listAPIKeys()  throws ServerException, ClientException;
}
