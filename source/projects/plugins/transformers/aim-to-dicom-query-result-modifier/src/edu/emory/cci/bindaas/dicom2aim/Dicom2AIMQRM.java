package edu.emory.cci.bindaas.dicom2aim;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
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
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;

import edu.emory.cci.bindaas.aim2dicom.bundle.Activator;
import edu.emory.cci.bindaas.framework.api.IQueryResultModifier;
import edu.emory.cci.bindaas.framework.model.ModifierException;
import edu.emory.cci.bindaas.framework.model.QueryResult;
import edu.emory.cci.bindaas.framework.model.RequestContext;
import edu.emory.cci.bindaas.framework.model.QueryResult.Callback;
import edu.emory.cci.bindaas.framework.util.DocumentationUtil;
import edu.emory.cci.bindaas.framework.util.GSONUtil;
import edu.emory.cci.bindaas.framework.util.StandardMimeType;

public class Dicom2AIMQRM implements IQueryResultModifier {

	private Log log = LogFactory.getLog(getClass());
	private static final String DOCUMENTATION_RESOURCES_LOCATION = "META-INF/documentation-dicom2aim";
	private JsonObject documentation;
	private final static String seriesUIDAttributeName ="seriesUID";

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
		// TODO Auto-generated method stub

	}

	@Override
	public QueryResult modifyQueryResult(final QueryResult queryResult,
			JsonObject dataSource, RequestContext requestContext, JsonObject modifierProperties , Map<String,String> runtimeParameters)
			throws Exception {
		
		
		final Dicom2AIMQRMProperties props = GSONUtil.getGSONInstance()
				.fromJson(modifierProperties, Dicom2AIMQRMProperties.class);
		if (props != null && props.aimURL != null) {
			queryResult.setCallback(true);
			queryResult.setMime(true);
			queryResult.setMimeType(StandardMimeType.XML.toString());
			queryResult.setCallback(new Callback() {

				@Override
				public void callback(OutputStream servletOutputStream,
						Properties context) throws Exception {
					
					try{
						// get array of raw dicom objects
						
						JsonArray dicomArray = queryResult.getIntermediateResult().getAsJsonArray();
						Set<String> setOfUniqueSeries = new HashSet<String>();
						Iterator<JsonElement> dicomArrayIterator = dicomArray.iterator();
						while(dicomArrayIterator.hasNext()) {
							JsonObject dicomObj = dicomArrayIterator.next().getAsJsonObject();
							if(dicomObj.get(seriesUIDAttributeName)!=null)
							{
								String seriesUID = dicomObj.get(seriesUIDAttributeName)
										.getAsString();
								setOfUniqueSeries.add(seriesUID);	
							}
							else
							{
								log.warn("Encountered annotation missing seriesUID");
							}
							
						}

						// create json array
						
						JsonArray arrayOfSeries = GSONUtil.getGSONInstance().toJsonTree(setOfUniqueSeries,HashSet.class).getAsJsonArray();

						// download and stream objects
						
						String seriesJson = arrayOfSeries.toString().trim();
						writeDicomImage(props.aimURL, props.apiKey,seriesJson.substring(1, seriesJson.length() - 1) ,  servletOutputStream);
						
					} catch (Exception e) {
						log.error(e);
						throw e;
					}
					
				}

				private void writeDicomImage(String imageUrlToFetchAIM,
						String apiKey , String series , OutputStream zos) throws ClientProtocolException,
						IOException {
					HttpPost post = new HttpPost(imageUrlToFetchAIM);
					post.addHeader("api_key", apiKey);
					
					List <NameValuePair> nvps = new ArrayList <NameValuePair>();
			        nvps.add(new BasicNameValuePair(seriesUIDAttributeName, series)) ;
			        post.setEntity(new UrlEncodedFormEntity(nvps));
			        
					HttpClient httpClient = new DefaultHttpClient();
					HttpResponse response = httpClient.execute(post);
					if (response != null && response.getStatusLine().getStatusCode() == 200 && response.getEntity() != null) {
						response.getEntity().writeTo(zos);
					}
					else
					{
						if(response!=null)
							log.warn("Request to url [" + imageUrlToFetchAIM + "] failed. Reason=[" + response.getStatusLine().toString() + "]");
						else
							log.warn("Request to url [" + imageUrlToFetchAIM + "] failed");
					}
				}

			});

		} else {
			String error = "QRM properties missing attribute imageUrl";
			log.error(error);
			throw new Exception(error);
		}
		return queryResult;
	}

	public static class Dicom2AIMQRMProperties {
		@Expose
		private String aimURL;
		@Expose
		private String apiKey;

		public String getAimURL(String seriesInstanceUID) {
			if (aimURL != null) {
				String aimURL = this.aimURL + "?seriesUID="
						+ seriesInstanceUID;
				return aimURL;
			} else
				return null;

		}
	}

	@Override
	public String getDescriptiveName() {

		return "Fetch Annotations for Images";
	}

}
