package edu.emory.cci.bindaas.cilogon;

import java.io.IOException;
import java.net.URI;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.emory.cci.bindaas.security.api.BindaasUser;
import edu.uiuc.ncsa.myproxy.oa4mp.client.AssetResponse;
import edu.uiuc.ncsa.myproxy.oa4mp.client.ClientEnvironment;
import edu.uiuc.ncsa.myproxy.oa4mp.client.OA4MPService;

public class CILogonSession {
	
	private OA4MPService service;

	
	public CILogonSession(URI ciLogonURL, URI redirectURL, long lifetime,
			String clientId, PrivateKey privateKey, PublicKey publicKey) {
		
		ClientEnvironment clientEnv = new ClientEnvironment(ciLogonURL, redirectURL, lifetime , clientId, privateKey, publicKey);
		this.service = new OA4MPService(clientEnv);
		
	}

	public void authRequest(
            HttpServletRequest httpReq,
            HttpServletResponse httpResp) throws IOException 
            {
				String serverUri = service.requestCert().getRedirect().toString();
				httpResp.sendRedirect(serverUri);
            }
	
	public BindaasUser verifyResponse(HttpServletRequest httpReq)
    {
		String tempToken = httpReq.getParameter("tempToken");
		String verifier = httpReq.getParameter("verifier");
    	AssetResponse assetResponse = service.getCert(tempToken, verifier);
    	if(assetResponse!=null)
    	{
    		String username = assetResponse.getUsername();
            BindaasUser bindaasUser = new BindaasUser(username);
            bindaasUser.addProperty("certificate", assetResponse.getX509Certificate());
            return bindaasUser;
    	}
    	else
    	{
    		return null;
    	}
    	
    	
    }
	
	
}
