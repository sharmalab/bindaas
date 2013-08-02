package edu.emory.cci.bindaas.dicom2png;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.pixelmed.dicom.Attribute;
import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.DicomException;
import com.pixelmed.dicom.DicomInputStream;
import com.pixelmed.dicom.TagFromName;

import edu.emory.cci.bindaas.dicom2png.bundle.Activator;
import edu.emory.cci.bindaas.framework.api.IQueryResultModifier;
import edu.emory.cci.bindaas.framework.model.ModifierException;
import edu.emory.cci.bindaas.framework.model.QueryResult;
import edu.emory.cci.bindaas.framework.model.QueryResult.Callback;
import edu.emory.cci.bindaas.framework.model.RequestContext;
import edu.emory.cci.bindaas.framework.provider.exception.AbstractHttpCodeException;
import edu.emory.cci.bindaas.framework.provider.exception.ModifierExecutionFailedException;
import edu.emory.cci.bindaas.framework.provider.exception.UpstreamContentAssertionFailedException;
import edu.emory.cci.image.convert.FromDICOM;

public class Dicom2PNGQRM implements IQueryResultModifier {

	private Log log = LogFactory.getLog(getClass());
	private static final String DOCUMENTATION_RESOURCES_LOCATION = "META-INF/documentation";
	private JsonObject documentation;
	public static final String FORMAT = "format";
	public static final String SCALE = "scale";
	public static final String WIDTH = "width";
	public static final String WINDOW = "window";
	public static final String LEVEL = "level";
	public static final String FRAMEID = "frame";
	

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
			throws AbstractHttpCodeException {
		// TODO: jpeg and bmp write out are not correct.  possibly due to image format issue.
		
		String format = "png";
		if (runtimeParameters.containsKey(FORMAT)) format = runtimeParameters.get(FORMAT);

		final Map<String, String> fRuntimeParameters = runtimeParameters;
		final String fformat = format;
		queryResult.setMimeType("image/" + format);
		
		
		queryResult.setCallback(new Callback() {
			
			@Override
			public void callback(OutputStream servletOutputStream,
					Properties context) throws AbstractHttpCodeException {
				try {
					if (queryResult.getIntermediateResult() == null) {
						throw new UpstreamContentAssertionFailedException(getClass().getName(), 1 , "Upstream query result did not set a json element variable.");
					}
					 JsonArray resultSet = queryResult.getIntermediateResult().getAsJsonArray();
					 if (resultSet.size() > 1) {
						throw new UpstreamContentAssertionFailedException(getClass().getName(), 1 , "Number of upstream image query results is more than 1.");
					 } else if (resultSet.size() == 0) {
						throw new UpstreamContentAssertionFailedException(getClass().getName(), 1 , "No image results found to transform to PNG.");
					 }
					 String filename = new String();
					 JsonObject jobj = resultSet.get(0).getAsJsonObject();
					 if (jobj.has("filepath")) {
						 filename = jobj.get("filepath").getAsString();
					 }
					 
					 if (filename.isEmpty()) {
						throw new UpstreamContentAssertionFailedException(getClass().getName(), 1 , "Query result is missing filepath attribute.");
					 }
					 
					try {
						DicomInputStream dis = new DicomInputStream(new FileInputStream(filename));
						convert(dis, servletOutputStream, fformat, fRuntimeParameters);
						dis.close();

					} catch (FileNotFoundException e) {
						throw e;
					} catch (IOException e) {
						throw e;
					} catch (DicomException e) {
						throw e;
					}

					
				} catch (Exception e) {
					log.error(e);
					throw new ModifierExecutionFailedException(getClass().getName() , 1 , e);
				}
				
			}
			private void convert(DicomInputStream dis, OutputStream os, String format, Map<String,String> runtimeParameters) throws IOException, DicomException {
//					throws FileNotFoundException, IOException, DicomException, ValidationException, NoContentException, BadContentException, ModifierExecutionFailedException {
				
				
				if ("dicom".equalsIgnoreCase(format)) {
					// just want dicom.  so send it back.
				    byte[] buffer = new byte[1024]; // Adjust if you want
				    int bytesRead;
				    while ((bytesRead = dis.read(buffer)) != -1) {
				        os.write(buffer, 0, bytesRead);
				    }
				    
				    System.out.println("dicom requested.  directly read from file system.");
				    
					return;
				}
					
				// get the parameters from user.  constrain to: 1 or 3 channels, byte, (u)short, float, double.  (bitmap on source side becomes byte).

				// only applies to gray data.
				double window = Double.NaN, level = Double.NaN;
				if (runtimeParameters.containsKey(WINDOW) != runtimeParameters.containsKey(LEVEL)) throw new DicomException("Both Window and Level need to be specified at the same time");
				if (runtimeParameters.containsKey(WINDOW) && !runtimeParameters.get(WINDOW).isEmpty()) window = Double.parseDouble(runtimeParameters.get(WINDOW));
				if (runtimeParameters.containsKey(LEVEL) && !runtimeParameters.get(LEVEL).isEmpty()) level = Double.parseDouble(runtimeParameters.get(LEVEL));
				
				
				int width = -1, height=-1;
				if (runtimeParameters.containsKey(WIDTH) && !runtimeParameters.get(WIDTH).isEmpty()) width = Integer.parseInt(runtimeParameters.get(WIDTH));
				//if (runtimeParameters.containsKey(HEIGHT) && !runtimeParameters.get(HEIGHT).isEmpty()) height = Integer.parseInt(runtimeParameters.get(HEIGHT));
				double scale = 1.0;
				if (runtimeParameters.containsKey(SCALE) && !runtimeParameters.get(SCALE).isEmpty()) scale = Double.parseDouble(runtimeParameters.get(SCALE));
				
				AttributeList list = new AttributeList();
				list.read(dis);
				int s_width, s_height;
				s_width		= Attribute.getSingleIntegerValueOrDefault(list,TagFromName.Columns,0);
				s_height		= Attribute.getSingleIntegerValueOrDefault(list,TagFromName.Rows,0);

				if (width > 0) {
					scale = (double)width / (double)s_width;
					height = (int)(Math.round(scale * (double)s_height)); 
				} else if (scale != 1.0) {
					width = (int)(Math.round(scale * (double)s_width));
					height = (int)(Math.round(scale * (double)s_height));
				} else {
					width = -1;
					height = -1;
				}
				
				
				// which frame?
				int frameid = 0;
				if (runtimeParameters.containsKey(FRAMEID) && !runtimeParameters.get(FRAMEID).isEmpty()) frameid = Integer.parseInt(runtimeParameters.get(FRAMEID));
				
				
				FromDICOM.convertToEightBitImage(list, os, "result", format, level, window, width, height, 0, 0, 0, 0, frameid, frameid, 100, null, 0);
				
			}

			


		});

		return queryResult;
	}


	@Override
	public String getDescriptiveName() {

		return "Transform DICOM image to PNG format, preserving pixel format.";
	}


	
}
