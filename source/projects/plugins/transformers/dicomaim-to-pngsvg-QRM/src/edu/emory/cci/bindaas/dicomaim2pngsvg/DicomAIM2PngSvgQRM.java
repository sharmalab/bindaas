package edu.emory.cci.bindaas.dicomaim2pngsvg;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;

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
import com.pixelmed.dicom.DicomException;
import com.pixelmed.dicom.DicomInputStream;
import com.pixelmed.display.SourceImage;

import edu.emory.cci.bindaas.dicomaim2pngsvg.bundle.Activator;
import edu.emory.cci.bindaas.framework.api.IQueryResultModifier;
import edu.emory.cci.bindaas.framework.model.ModifierException;
import edu.emory.cci.bindaas.framework.model.QueryResult;
import edu.emory.cci.bindaas.framework.model.QueryResult.Callback;
import edu.emory.cci.bindaas.framework.model.RequestContext;
import edu.emory.cci.bindaas.framework.provider.exception.AbstractHttpCodeException;
import edu.emory.cci.bindaas.framework.provider.exception.ModifierExecutionFailedException;
import edu.emory.cci.bindaas.framework.provider.exception.UpstreamContentAssertionFailedException;
import edu.emory.cci.bindaas.framework.provider.exception.ValidationException;
import edu.emory.cci.bindaas.framework.util.DocumentationUtil;
import edu.emory.cci.bindaas.framework.util.GSONUtil;
import edu.emory.cci.bindaas.framework.util.StandardMimeType;

public class DicomAIM2PngSvgQRM implements IQueryResultModifier {

	private Log log = LogFactory.getLog(getClass());
	private static final String DOCUMENTATION_RESOURCES_LOCATION = "META-INF/documentation";
	private JsonObject documentation;
	private final static String sopUIDAttributeName ="sopUID";

	private Templates template;
	private Pattern inPattern;
	private Pattern outPattern;

	
	@Override
	public JsonObject getDocumentation() {

		return documentation;
	}

	public void init() throws IOException, TransformerConfigurationException {
		BundleContext context = Activator.getContext();
		
		InputStream xslt;
		try {
			xslt = context.getBundle().getEntry("META-INF/AIMv3r11MapToSVG11.xslt").openStream();
		} catch (IOException e1) {
			log.error(e1);
			throw e1;			
		}

		TransformerFactory transFact = new net.sf.saxon.TransformerFactoryImpl();
        try {
			template = transFact.newTemplates(new javax.xml.transform.stream.StreamSource(xslt));
		} catch (TransformerConfigurationException e) {
			log.error(e);
			throw e;
		}
        inPattern = Pattern.compile("<ImageAnnotation.*?</ImageAnnotation>", Pattern.DOTALL);
        outPattern = Pattern.compile("<svg.*</svg>", Pattern.DOTALL);

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
			throws AbstractHttpCodeException {
		
		
		final DicomAIM2PngSvgQRMProperties props = GSONUtil.getGSONInstance()
				.fromJson(modifierProperties, DicomAIM2PngSvgQRMProperties.class);
		if (props == null || props.aimURL == null) {
			String error = "QRM properties missing attribute imageUrl";
			log.error(error);
			throw new ValidationException(getClass().getName() , 1 ,  error);
		}
			
		queryResult.setMimeType(StandardMimeType.ZIP.toString());
		queryResult.setCallback(new Callback() {

			@Override
			public void callback(OutputStream servletOutputStream,
					Properties context) throws AbstractHttpCodeException {

				// results returned: if no image, error.  if no annotation, return empty xml + image.  if both, return both.
				
				InputStream ais = null;
				String filename = new String();
				String sopInstanceUID = new String();
				try{

					// check to see if we get the right kind of data
					if (queryResult.getIntermediateResult() == null) {
						throw new UpstreamContentAssertionFailedException(getClass().getName(), 1 , "Upstream query result did not set a json element variable.");
					}

					JsonArray resultSet = queryResult.getIntermediateResult().getAsJsonArray();
					if (resultSet.size() > 1) {
						throw new UpstreamContentAssertionFailedException(getClass().getName(), 1 , "Number of upstream image query results is more than 1.");
					} else if (resultSet.size() == 0) {
						throw new UpstreamContentAssertionFailedException(getClass().getName(), 1 , "No image results found to transform to PNG.");
					}
					JsonObject jobj = resultSet.get(0).getAsJsonObject();

					// get the sop instance UID
					if (jobj.has("SOPInstanceUID")) {
						sopInstanceUID = jobj.get("SOPInstanceUID").getAsString();
					}
					if (sopInstanceUID.isEmpty()) {
						throw new UpstreamContentAssertionFailedException(getClass().getName(), 1 , "Query result is missing SOPInstanceUID attribute.");
					}

					// get the image filename
					if (jobj.has("filepath")) {
						filename = jobj.get("filepath").getAsString();
					}
					if (filename.isEmpty()) {
						throw new UpstreamContentAssertionFailedException(getClass().getName(), 1 , "Query result is missing filepath attribute.");
					}

				} 
				
				catch(AbstractHttpCodeException e)
				{
					log.error(e);
					throw e;
				}
				catch (Exception e) {
					log.error(e);
					throw new ModifierExecutionFailedException(getClass().getName(), 1 , e);
				}


				// open the zip output stream first
				ZipOutputStream zos = new ZipOutputStream(servletOutputStream);
				try {

					// open the zip entry
					ZipEntry imageEntry = new ZipEntry(sopInstanceUID + ".png");
					zos.putNextEntry(imageEntry);
				
					// convert the dicom image to png
					try {
						writeImage(filename, zos);
					} catch (FileNotFoundException e) {
						throw e;
					} catch (IOException e) {
						throw e;
					} catch (DicomException e) {
						throw e;
					} finally {
						zos.closeEntry();
					}


					// now get the annotations.  - put this here because if there is no annotation or annotation failed, may still want to get the image.
					ais = queryAnnotations(props.aimURL, props.apiKey, sopInstanceUID);

					// open the zip entry
					ZipEntry annotationDir = new ZipEntry(sopInstanceUID + "_svg.xml");
					zos.putNextEntry(annotationDir);

					// convert the annotations
					try {
						writeAnnotations(ais, sopInstanceUID, zos);
					} catch (ClientProtocolException e) {
						throw e;
					} catch (IOException e) {
						throw e;
					} catch (TransformerException e) {
						throw e;
					} finally {
						zos.closeEntry();
					}

				} catch (Exception e) {
					log.error(e);
					throw new ModifierExecutionFailedException(getClass().getName(), 1 , e);
					
				} finally {
					try {
						zos.close();
					} catch (IOException e) {
						log.error(e);
						throw new ModifierExecutionFailedException(getClass().getName(), 1 , e);
					}
				}

			}


			private InputStream queryAnnotations(String imageUrlToFetchAIM,
					String apiKey , String sopUID) throws ClientProtocolException, IOException {

				// construct URL and query
				HttpPost post = new HttpPost(imageUrlToFetchAIM);
				post.addHeader("api_key", apiKey);

				List <NameValuePair> nvps = new ArrayList <NameValuePair>();
				nvps.add(new BasicNameValuePair(sopUIDAttributeName, sopUID)) ;
				post.setEntity(new UrlEncodedFormEntity(nvps));

				HttpClient httpClient = new DefaultHttpClient();
				HttpResponse response = httpClient.execute(post);

				if (response != null && response.getStatusLine().getStatusCode() == 200 && response.getEntity() != null) {
					return response.getEntity().getContent();
				}
				else
				{
					if(response!=null)
						throw new IOException("Request to url [" + imageUrlToFetchAIM + "] failed. Reason=[" + response.getStatusLine().toString() + "]");

					else
						throw new IOException("Request to url [" + imageUrlToFetchAIM + "] failed");
				}
			}


			private void writeImage(String filename, OutputStream os) throws FileNotFoundException, IOException, DicomException {
				SourceImage si = new SourceImage(new DicomInputStream(new FileInputStream(filename)));
				BufferedImage bi = si.getBufferedImage(0);

				ImageIO.write(bi, "png", os);
			}

			private void writeAnnotations(InputStream is, String sopUID, OutputStream os) throws ClientProtocolException,
			IOException, TransformerException {


				javax.xml.transform.Transformer trans = template.newTransformer();

				String tag = "<results>\n";
				os.write(tag.getBytes());
				String eol = "\n";

				Scanner s = new Scanner(is);
				String nextMatch = s.findWithinHorizon(inPattern, 0);
				while (nextMatch != null) {
					//						System.out.println("query result: " + nextMatch);
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					javax.xml.transform.Result result =
							new javax.xml.transform.stream.StreamResult(baos);

					javax.xml.transform.Source xmlSource =
							new javax.xml.transform.stream.StreamSource(new StringReader(nextMatch));
					trans.transform(xmlSource, result);

					ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
					Scanner s2 = new Scanner(bais);
					String svg = s2.findWithinHorizon(outPattern, 0);

					os.write(svg.getBytes());
					os.write(eol.getBytes());

					nextMatch = s.findWithinHorizon(inPattern, 0);
				}
				tag = "</results>\n";
				os.write(tag.getBytes());

			}

		});

		return queryResult;
	}

	public static class DicomAIM2PngSvgQRMProperties {
		@Expose
		private String aimURL;
		@Expose
		private String apiKey;

		public String getAimURL(String SOPInstanceUID) {
			if (aimURL != null) {
				String aimURL = this.aimURL + "?sopUID="
						+ SOPInstanceUID;
				return aimURL;
			} else
				return null;

		}
	}

	@Override
	public String getDescriptiveName() {

		return "Fetch Image and its annotations, return as PNG and SVGs in a zip";
	}

}
