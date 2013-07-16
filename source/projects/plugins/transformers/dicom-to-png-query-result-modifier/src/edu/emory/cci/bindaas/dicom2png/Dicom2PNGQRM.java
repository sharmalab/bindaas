package edu.emory.cci.bindaas.dicom2png;

import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

import javax.imageio.ImageIO;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.pixelmed.dicom.DicomException;
import com.pixelmed.dicom.DicomInputStream;
import com.pixelmed.display.SourceImage;

import edu.emory.cci.bindaas.dicom2png.bundle.Activator;
import edu.emory.cci.bindaas.framework.api.IQueryResultModifier;
import edu.emory.cci.bindaas.framework.model.ModifierException;
import edu.emory.cci.bindaas.framework.model.QueryResult;
import edu.emory.cci.bindaas.framework.model.QueryResult.Callback;
import edu.emory.cci.bindaas.framework.model.RequestContext;

public class Dicom2PNGQRM implements IQueryResultModifier {

	private Log log = LogFactory.getLog(getClass());
	private static final String DOCUMENTATION_RESOURCES_LOCATION = "META-INF/documentation";
	private JsonObject documentation;
	
	

	@Override
	public JsonObject getDocumentation() {

		return documentation;
	}

	public void init() {
		BundleContext context = Activator.getContext();
//		documentation = DocumentationUtil.getProviderDocumentation(context,
//				DOCUMENTATION_RESOURCES_LOCATION);
		documentation = new JsonObject();
		documentation.add("view", new JsonPrimitive(""));
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
		// TODO: jpeg and bmp write out are not correct.  possibly due to image format issue.
		
		String oext = "png";
//		if (runtimeParameters.containsKey("imgFormat")) {
//			String oe = runtimeParameters.get("imgFormat");
//			if (oe == null || oe.equals("")) {
//				log.warn("format conversion requested but not specified. default to PNG");
//			} else {
//				oe = oe.toLowerCase();
//				//if (oe.equals("tif")) oe = "tiff";
//				if (oe.equals("jpg")) oe = "jpeg";
//				if (!(oe.equals("png") || oe.equals("jpeg") || oe.equals("bmp") )) {
//					log.error("unsupported file type requested: " + oe + ".  default to PNG");
//				} else {
//					oext = oe;
//				}
//			}
//		}
//
//		int width = -1;
//		int height = -1;
//		if (runtimeParameters.containsKey("width")) {
//			String str = runtimeParameters.get("width");
//			if (str == null || str.equals("")) {
//				log.warn("resizing requested but not specified. default to original size");
//			} else {
//				width = Integer.parseInt(str);
//			}
//		}
//		if (runtimeParameters.containsKey("height")) {
//			String str = runtimeParameters.get("height");
//			if (str == null || str.equals("")) {
//				log.warn("resizing requested but not specified. default to original size");
//			} else {
//				height = Integer.parseInt(str);
//			}
//		}
//		
//		int channels = -1;
//		if (runtimeParameters.containsKey("channels")) {
//			String str = runtimeParameters.get("channels");
//			if (str == null || str.equals("")) {
//				log.warn("number of channels requested but not requested. default to original number of channels");
//			} else {
//				int c = Integer.parseInt(str);
//				if (c != 8 || c != 16) {
//					log.warn("converting to " + c + " number of channels is unsupported. default to original channels");
//				} else {
//					channels = c;
//				}
//			}
//		}
//
//		int bits = -1;
//		if (runtimeParameters.containsKey("bits")) {
//			String str = runtimeParameters.get("bits");
//			if (str == null || str.equals("")) {
//				log.warn("rescaling to new bit rate requested but not specified. default to original bit rate");
//			} else {
//				int b = Integer.parseInt(str);
//				if (b != 8 || b != 16) {
//					log.warn("rescaling to " + b + " bit rate is unsupported. default to original bit rate");
//				} else {
//					bits = b;
//				}
//			}
//		}
//		
//		
//		
		final String foext = oext;
//		final int fw = width;
//		final int fh = height;
//		final int fc = channels;
//		final int fb = bits;
//		
//		
//		if (oext.equals("wbmp")) queryResult.setMimeType("image/vnd.wap.wbmp");
		queryResult.setMimeType("image/" + oext);
		
		queryResult.setCallback(new Callback() {
			
			@Override
			public void callback(OutputStream servletOutputStream,
					Properties context) throws Exception {
				try {
					if (queryResult.getIntermediateResult() == null) {
						throw new Exception("Upstream query result did not set a json element variable.");
					}
					 JsonArray resultSet = queryResult.getIntermediateResult().getAsJsonArray();
					 if (resultSet.size() > 1) {
						 Exception e2 = new Exception("Number of upstream image query results is more than 1.");
						 throw e2;
					 } else if (resultSet.size() == 0) {
						 Exception e2 = new Exception("No query results found to transform to PNG.");
						 throw e2; // for now, if no upstream, then throw an exception.
					 }
					 String filename = new String();
					 JsonObject jobj = resultSet.get(0).getAsJsonObject();
					 if (jobj.has("filepath")) {
						 filename = jobj.get("filepath").getAsString();
					 }
					 
					 if (filename.isEmpty()) {
						 Exception e2 = new Exception("Query result is missing filepath attribute.");
						 throw e2; // for now, if no upstream, then throw an exception.
					 }
					 
					SourceImage si;
					BufferedImage bi;
					try {
						si = new SourceImage(new DicomInputStream(new FileInputStream(filename)));
						bi = si.getBufferedImage(0);
					} catch (FileNotFoundException e) {
						throw e;
					} catch (IOException e) {
						throw e;
					} catch (DicomException e) {
						throw e;
					}

					
					try {
						ImageIO.write(bi, foext, servletOutputStream);
					} catch (IOException e) {
						throw e;
					}

				} catch (Exception e) {
					log.error(e);
					throw e;
				}
				
			}

		});

		return queryResult;
	}


	@Override
	public String getDescriptiveName() {

		return "Transform DICOM image to PNG format, preserving pixel format.";
	}

}
