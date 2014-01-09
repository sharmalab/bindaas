package edu.emory.cci.bindaas.outputchainer;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.osgi.framework.BundleContext;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;

import edu.emory.cci.bindaas.framework.api.IQueryResultModifier;
import edu.emory.cci.bindaas.framework.model.ModifierException;
import edu.emory.cci.bindaas.framework.model.QueryResult;
import edu.emory.cci.bindaas.framework.model.QueryResult.Callback;
import edu.emory.cci.bindaas.framework.model.RequestContext;
import edu.emory.cci.bindaas.framework.model.ResultSetIterator;
import edu.emory.cci.bindaas.framework.provider.exception.AbstractHttpCodeException;
import edu.emory.cci.bindaas.framework.provider.exception.ModifierExecutionFailedException;
import edu.emory.cci.bindaas.framework.provider.exception.ValidationException;
import edu.emory.cci.bindaas.framework.util.DocumentationUtil;
import edu.emory.cci.bindaas.framework.util.GSONUtil;
import edu.emory.cci.bindaas.outputchainer.bundle.Activator;

public class OutputChainerQRM implements IQueryResultModifier {

	private Log log = LogFactory.getLog(getClass());
	private static final String DOCUMENTATION_RESOURCES_LOCATION = "META-INF/documentation";
	private JsonObject documentation;
	

	@Override
	public JsonObject getDocumentation() {

		return documentation;
	}

	public void init() {
		BundleContext context = Activator.getContext();
		documentation = DocumentationUtil.getProviderDocumentation(context,
				DOCUMENTATION_RESOURCES_LOCATION);
		Dictionary<String, String> props = new Hashtable<String, String>();
		props.put("class", getClass().getName());
		context.registerService(IQueryResultModifier.class.getName(), this,
				props);
	}

	@Override
	public void validate() throws ModifierException {


	}

	@Override
	public QueryResult modifyQueryResult(final QueryResult queryResult,
			JsonObject dataSource, RequestContext requestContext, JsonObject modifierProperties , final Map<String,String> runtimeParameters)
			throws AbstractHttpCodeException {
		
		
		final QRMProperties props = GSONUtil.getGSONInstance()
				.fromJson(modifierProperties, QRMProperties.class);
		if (props != null) {
			props.validate();
			final ResultSetIterator iterator = queryResult.getIntermediateResult();
			queryResult.setMimeType(props.mimeType);
			queryResult.setCallback(new Callback() {

				@Override
				public void callback(OutputStream servletOutputStream,
						Properties context) throws AbstractHttpCodeException {
					
					try {
						if(queryResult.getIntermediateResult() != null)
						{
							
							Set<String> setOfUniqueKeyAttributes = new HashSet<String>();
							
							while(iterator.hasNext()) {
							
								JsonObject jsonObj = iterator.next();
								if(jsonObj.get(props.keyAttribute)!=null)
								{
									String keyAttributeVal = jsonObj.get(props.keyAttribute)
											.getAsString();
									setOfUniqueKeyAttributes.add(keyAttributeVal);	
								}
								else
								{
									log.warn("Missing value for key Attribute [" + props.keyAttribute + "]");
								}
								
							}

							// create json array
							
							JsonArray arrayOfKeyAttributes = GSONUtil.getGSONInstance().toJsonTree(setOfUniqueKeyAttributes,HashSet.class).getAsJsonArray();

							// download and stream image
							
							String keyQueryParamVal = arrayOfKeyAttributes.toString().trim();
							keyQueryParamVal = keyQueryParamVal.substring(keyQueryParamVal.length() - 1);
							
							chainRemoteService(props.remoteURL, props.apiKey, props.keyQueryParameter , keyQueryParamVal , runtimeParameters , servletOutputStream);

						}
						else
						{
							throw new Exception("OutputFormat not compatible with the Query Result Modifier Plugin. Expected type is JSON");
						}
												
					} catch (Exception e) {
						log.error(e);
						throw new ModifierExecutionFailedException(getClass().getName(), 1 , e);
					}finally{
						try {
							iterator.close();
						} catch (IOException e) {
							log.fatal("Unable to close ResultSetIterator" , e);
						}
					}
					
				}

				private void chainRemoteService(String remoteURL,
						String apiKey , String keyQueryParam, String keyQueryParamVal, Map<String,String> runtimeParameters , OutputStream sos) throws ClientProtocolException,
						IOException {
					HttpPost post = new HttpPost(remoteURL);
					post.addHeader("api_key", apiKey);
					
					List <NameValuePair> nvps = new ArrayList <NameValuePair>();
			        nvps.add(new BasicNameValuePair(keyQueryParam, keyQueryParamVal)) ;
			        
			        for(String k : runtimeParameters.keySet())
			        {
			        	nvps.add(new BasicNameValuePair(k, runtimeParameters.get(k)));
			        }
			        
			        post.setEntity(new UrlEncodedFormEntity(nvps));
			        
					HttpClient httpClient = new DefaultHttpClient();
					HttpResponse response = httpClient.execute(post);
					if (response != null && response.getStatusLine().getStatusCode() == 200 && response.getEntity() != null) {
						response.getEntity().writeTo(sos);
					}
					else
					{
						if(response!=null)
							log.warn("Request to url [" + remoteURL + "] failed. Reason=[" + response.getStatusLine().toString() + "]");
						else
							log.warn("Request to url [" + remoteURL + "] failed");
					}
				}
			
			});

		} else {
			String error = "QRM properties missing attribute imageUrl";
			log.error(error);
			throw new ValidationException(getClass().getName() ,1, error);
		}
		return queryResult;
	}

	public static class QRMProperties {
		@Expose
		private String remoteURL;
		@Expose
		private String keyAttribute;
		@Expose
		private String keyQueryParameter;
		@Expose
		private String mimeType;
		@Expose
		private String apiKey;
		
		public void validate() throws ValidationException
		{
			if(remoteURL == null || keyAttribute == null  || keyQueryParameter == null || apiKey == null) throw new ValidationException( getClass().getName() ,1,"Modifier Properties not valid");
			if(mimeType == null ) mimeType = "application/octet-stream";
		}

	}

	@Override
	public String getDescriptiveName() {

		return "Chain Output from Remote Service";
	}

}
