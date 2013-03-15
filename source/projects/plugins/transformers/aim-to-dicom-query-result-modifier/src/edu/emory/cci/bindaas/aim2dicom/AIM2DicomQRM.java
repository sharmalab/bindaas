package edu.emory.cci.bindaas.aim2dicom;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.osgi.framework.BundleContext;

import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;

import edu.emory.cci.bindaas.aim2dicom.bundle.Activator;
import edu.emory.cci.bindaas.commons.xml2json.XML2JSON;
import edu.emory.cci.bindaas.commons.xml2json.model.Mapping;
import edu.emory.cci.bindaas.commons.xml2json.model.Type;
import edu.emory.cci.bindaas.framework.api.IQueryResultModifier;
import edu.emory.cci.bindaas.framework.model.ModifierException;
import edu.emory.cci.bindaas.framework.model.QueryResult;
import edu.emory.cci.bindaas.framework.model.QueryResult.Callback;
import edu.emory.cci.bindaas.framework.util.DocumentationUtil;
import edu.emory.cci.bindaas.framework.util.GSONUtil;
import edu.emory.cci.bindaas.framework.util.StandardMimeType;

public class AIM2DicomQRM implements IQueryResultModifier {

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
		// TODO Auto-generated method stub

	}

	@Override
	public QueryResult modifyQueryResult(final QueryResult queryResult,
			JsonObject dataSource, String user, JsonObject modifierProperties)
			throws Exception {
		final AIM2DicomQRMProperties props = GSONUtil.getGSONInstance()
				.fromJson(modifierProperties, AIM2DicomQRMProperties.class);
		if (props != null && props.imageURL != null) {
			queryResult.setCallback(true);
			queryResult.setMime(true);
			queryResult.setMimeType(StandardMimeType.ZIP.toString());
			queryResult.setCallback(new Callback() {

				@Override
				public void callback(OutputStream servletOutputStream,
						Properties context) throws Exception {

					try {
						ZipOutputStream zos = new ZipOutputStream(
								servletOutputStream);
						
						List<JsonObject> annotations = parseAnnotations(queryResult
								.getData());
						
						// Create a set of URLs from where to download images
						Map<String, String> setOfImagesToDownload = new HashMap<String, String>();
						
						for (JsonObject annotation : annotations) {
						
							if(annotation.get("seriesUID")!=null)
							{
								String seriesUID = annotation.get("seriesUID")
										.getAsString();
								String imageUrlToFetchDicom = props
										.getImageUrl(seriesUID);
								setOfImagesToDownload.put(seriesUID,
										imageUrlToFetchDicom);	
							}
							else
							{
								log.warn("Encountered annotation missing seriesUID");
							}
							

						}

						// Iterate over all imageURL , download ZIP streams and
						// put them in the final ZIP file

						for (String seriesUID : setOfImagesToDownload.keySet()) {
							String imageURL = setOfImagesToDownload
									.get(seriesUID);
							log.debug("Fetching Dicom objects from ["
									+ imageURL + "]");
							try {
								
								try {
								
								InputStream dicomImageStream = getDicomImage(
										imageURL, props.apiKey);
								ZipEntry entry = new ZipEntry(seriesUID
										+ ".zip");

								zos.putNextEntry(entry);
								byte[] buffer = new byte[1024 * 5];

								int bytesRead = -1;
								while ((bytesRead = dicomImageStream
										.read(buffer)) != -1) {
									zos.write(buffer, 0, bytesRead);
								}

								zos.closeEntry();
								zos.flush();
								dicomImageStream.close();

								}
								catch(Exception e)
								{
									String errorMessage = "Unable to fetch images from ["
											+ imageURL + "]";
									log.error( errorMessage , e);
									
									// write error file to the zip stream
									
									ZipEntry errorEntry = new ZipEntry(seriesUID + ".error.log");
									zos.putNextEntry(errorEntry);
									zos.write(errorMessage.getBytes());
									zos.closeEntry();
									
								}

								

								
							} catch (Exception e) {
								log.error(e);
							}
						}

						zos.close();
					} catch (Exception e) {
						log.error(e);
						throw e;
					}
				}

				private InputStream getDicomImage(String imageUrlToFetchDicom,
						String apiKey) throws ClientProtocolException,
						IOException {
					HttpGet get = new HttpGet(imageUrlToFetchDicom);
					get.addHeader("api_key", apiKey);
					HttpClient httpClient = new DefaultHttpClient();
					HttpResponse response = httpClient.execute(get);
					if (response != null && response.getEntity() != null) {
						return response.getEntity().getContent();
					}
					return null;
				}

				private List<JsonObject> parseAnnotations(byte[] dataBytes)
						throws Exception {
					log.debug("Parsing Annotations");
					Mapping seriesUID = new Mapping();
					seriesUID.setName("seriesUID");
					seriesUID.setType(Type.SIMPLE);
					seriesUID
							.setXpath("/ns1:ImageAnnotation/ns1:imageReferenceCollection/ns1:ImageReference/ns1:imageStudy/ns1:ImageStudy/ns1:imageSeries/ns1:ImageSeries/@instanceUID");

					XML2JSON xml2json = new XML2JSON();
					Map<String, String> prefixes = xml2json.getPrefixes();
					prefixes.put("ns1",
							"gme://caCORE.caCORE/3.2/edu.northwestern.radiology.AIM");
					xml2json.setNamespaceAware(true);
					xml2json.setMappings(Arrays
							.asList(new Mapping[] { seriesUID }));
					xml2json.setRootElementSelector("/results/ns1:ImageAnnotation");
					xml2json.init();

					InputStream is = new ByteArrayInputStream(dataBytes);

					return xml2json.parseXML(is);
				}
			});

		} else {
			String error = "QRM properties missing attribute imageUrl";
			log.error(error);
			throw new Exception(error);
		}
		return queryResult;
	}

	public static class AIM2DicomQRMProperties {
		@Expose
		private String imageURL;
		@Expose
		private String apiKey;

		public String getImageUrl(String seriesInstanceUID) {
			if (imageURL != null) {
				String imageUrl = this.imageURL + "?seriesUID="
						+ seriesInstanceUID;
				return imageUrl;
			} else
				return null;

		}
	}

	@Override
	public String getDescriptiveName() {

		return "Fetch DICOM Images from AIM";
	}

}
