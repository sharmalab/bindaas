package edu.emory.cci.bindaas.openid;

import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.openid4java.OpenIDException;
import org.openid4java.consumer.ConsumerManager;
import org.openid4java.consumer.InMemoryConsumerAssociationStore;
import org.openid4java.consumer.InMemoryNonceVerifier;
import org.openid4java.consumer.VerificationResult;
import org.openid4java.discovery.Discovery;
import org.openid4java.discovery.DiscoveryInformation;
import org.openid4java.discovery.Identifier;
import org.openid4java.discovery.html.HtmlResolver;
import org.openid4java.discovery.yadis.YadisResolver;
import org.openid4java.message.AuthRequest;
import org.openid4java.message.AuthSuccess;
import org.openid4java.message.ParameterList;
import org.openid4java.message.ax.AxMessage;
import org.openid4java.message.ax.FetchRequest;
import org.openid4java.message.ax.FetchResponse;
import org.openid4java.server.RealmVerifierFactory;
import org.openid4java.util.HttpFetcherFactory;

import edu.emory.cci.bindaas.security.api.BindaasUser;

public class OpenIDHelper {

	private ConsumerManager manager;
	private Log log = LogFactory.getLog(getClass());
	private Map<String,OpenIDProvider> listOfOpenIdProviders;

    public Map<String, OpenIDProvider> getListOfOpenIdProviders() {
		return listOfOpenIdProviders;
	}

	public void setListOfOpenIdProviders(
			Map<String, OpenIDProvider> listOfOpenIdProviders) {
		this.listOfOpenIdProviders = listOfOpenIdProviders;
	}

	public OpenIDHelper() throws Exception
    {
        // instantiate a ConsumerManager object
        manager = newConsumerManager();
        
    }

	public void init() throws Exception
	{
		//Activator.getContext().registerService(this.getClass().getName(), this, null);
	}
    // --- placing the authentication request ---
    public String authRequest(
                              HttpServletRequest httpReq,
                              HttpServletResponse httpResp , String returnToUrl)
            throws IOException
    {
        try
        {
        	
        	if(httpReq.getParameter("identifier")!=null)
        	{
        		String userSuppliedString = httpReq.getParameter("identifier");
                // perform discovery on the user-supplied identifier

        		@SuppressWarnings("unchecked")
				List<Object> discoveries = manager.discover(userSuppliedString);

                // attempt to associate with the OpenID provider
                // and retrieve one service endpoint for authentication
                DiscoveryInformation discovered = manager.associate(discoveries);

                // store the discovery information in the user's session
                httpReq.getSession().setAttribute("openid-disc", discovered);

                // obtain a AuthRequest message to be sent to the OpenID provider
                String queryString = httpReq.getQueryString();
                StringBuffer receivingURL = httpReq.getRequestURL();
                
                if (queryString != null && queryString.length() > 0)
                    receivingURL.append("?").append(httpReq.getQueryString());
                
                AuthRequest authReq = manager.authenticate(discovered, receivingURL.toString());

                // Attribute Exchange example: fetching the 'email' attribute
                
                OpenIDProvider openIdProvider = null;
                for(String provider :  listOfOpenIdProviders.keySet())
                {
                	if(userSuppliedString.startsWith(provider))
                	{
                		openIdProvider = listOfOpenIdProviders.get(provider);
                		break;
                	}
                }
                listOfOpenIdProviders.get(userSuppliedString);
                if(openIdProvider!=null)
                {
                	  FetchRequest fetch = FetchRequest.createFetchRequest();
                	  Properties attributes = openIdProvider.getAttributes();
                	  if(attributes!=null)
                	  {
                		  for(Object attr : attributes.keySet())
                		  {
                			  fetch.addAttribute(attr.toString(),attributes.getProperty(attr.toString()), true);
                		  }
                	  }

                	
                      // attach the extension to the authentication request
                      authReq.addExtension(fetch);

                }
               
                httpResp.sendRedirect(authReq.getDestinationUrl(true));

        	}
        	else
        		throw new Exception("[identifier] indicating the openId provider not provided in the request");

        }
        catch (Exception e)
        {
            log.error("Error redirecting to OpenID provider" , e);        }

        return null;
    }

    // --- processing the authentication response ---
    public BindaasUser verifyResponse(HttpServletRequest httpReq)
    {
        try
        {
            // extract the parameters from the authentication response
            // (which comes in as a HTTP request from the OpenID provider)
            ParameterList response =
                    new ParameterList(httpReq.getParameterMap());

            // retrieve the previously stored discovery information
            DiscoveryInformation discovered = (DiscoveryInformation)
                    httpReq.getSession().getAttribute("openid-disc");

            // extract the receiving URL from the HTTP request
            StringBuffer receivingURL = httpReq.getRequestURL();
            String queryString = httpReq.getQueryString();
            if (queryString != null && queryString.length() > 0)
                receivingURL.append("?").append(httpReq.getQueryString());

            // verify the response; ConsumerManager needs to be the same
            // (static) instance used to place the authentication request
            VerificationResult verification = manager.verify(
                    receivingURL.toString(),
                    response, discovered);

            // examine the verification result and extract the verified identifier
            Identifier verified = verification.getVerifiedId();
            if (verified != null)
            {
            	BindaasUser principal = null;
                AuthSuccess authSuccess =
                        (AuthSuccess) verification.getAuthResponse();

                if (authSuccess.hasExtension(AxMessage.OPENID_NS_AX))
                {
                	if(httpReq.getParameter("identifier")!=null)
                	{
                		String openIdIdentifier = httpReq.getParameter("identifier");
                		
                		OpenIDProvider openIdProvider = null;
                        for(String provider :  listOfOpenIdProviders.keySet())
                        {
                        	if(openIdIdentifier.startsWith(provider))
                        	{
                        		openIdProvider = listOfOpenIdProviders.get(provider);
                        		break;
                        	}
                        }
                		
                		if(openIdProvider!=null)
                		{
                			  FetchResponse fetchResp = (FetchResponse) authSuccess
                                      .getExtension(AxMessage.OPENID_NS_AX);
                              principal = new BindaasUser(fetchResp.getAttributeValue(BindaasUser.EMAIL_ADDRESS) );
                              principal.addProperty(BindaasUser.FIRST_NAME, "");
                              principal.addProperty(BindaasUser.LAST_NAME, "");
                              Properties attributes = openIdProvider.getAttributes();
                        	  if(attributes!=null)
                        	  {
                        		  for(Object attr : attributes.keySet())
                        		  {
                        			  String openIdAttr = fetchResp.getAttributeValue(attr.toString());
                        			  if(openIdAttr!=null)
                        			  principal.addProperty(attr.toString(), openIdAttr );
                        		  }
                        	  }
                              
                		}
                	}
                	
                  
                    
                }
                else
                {
                	principal = new BindaasUser(verified.getIdentifier());
                }
                
                return principal;  // success
            }
            
            
        }
        catch (OpenIDException e)
        {
        	log.error(e);
        }

        return null;
    }
    
    /**
     * Java proides a standard "trust manager" interface.  This trust manager
     * essentially disables the rejection of certificates by trusting anyone and everyone.
     */
     public static X509TrustManager getDummyTrustManager() {
         return new X509TrustManager() {
             public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                 return null;
             }
             public void checkClientTrusted(X509Certificate[] certs, String authType) {
             }
             public void checkServerTrusted(X509Certificate[] certs, String authType) {
             }
         };
     }

     /**
     * Returns a hostname verifiers that always returns true, always positively verifies a host.
     */
     public static HostnameVerifier getAllHostVerifier() {
         return new HostnameVerifier() {
             public boolean verify(String hostname, SSLSession session) {
                
            	 return true;
             }
         };
     }
     
    public static ConsumerManager newConsumerManager() throws Exception {
        // Install the all-trusting trust manager SSL Context
        
    	 // Create a trust manager that does not validate certificate chains
        TrustManager[] tma = new TrustManager[] {getDummyTrustManager()};

        // Install the all-trusting trust manager
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, tma, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

        // Install the all-trusting host verifier
        HttpsURLConnection.setDefaultHostnameVerifier(getAllHostVerifier());
        
    	

        HttpFetcherFactory hff = new HttpFetcherFactory(sc,
              SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        YadisResolver yr = new YadisResolver(hff);
        RealmVerifierFactory rvf = new RealmVerifierFactory(yr);
        Discovery d = new Discovery(new HtmlResolver(hff),yr,
              Discovery.getXriResolver());

        ConsumerManager manager = new ConsumerManager(rvf, d, hff);
        manager.setAssociations(new InMemoryConsumerAssociationStore());
        manager.setNonceVerifier(new InMemoryNonceVerifier(5000));
        return manager;
    }
}
