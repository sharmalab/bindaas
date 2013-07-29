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
import edu.emory.cci.bindaas.framework.provider.exception.AbstractHttpCodeException;
import edu.emory.cci.bindaas.framework.provider.exception.ModifierExecutionFailedException;

public class Dicom2PNGQRM implements IQueryResultModifier {

	private Log log = LogFactory.getLog(getClass());
	private JsonObject documentation;
	
	

	@Override
	public JsonObject getDocumentation() {

		return documentation;
	}

	public void init() {
		BundleContext context = Activator.getContext();
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
		
		String oext = "png";
		final String foext = oext;
		queryResult.setMimeType("image/" + oext);
		
		queryResult.setCallback(new Callback() {
			
			@Override
			public void callback(OutputStream servletOutputStream,
					Properties context) throws AbstractHttpCodeException {
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
					throw new  ModifierExecutionFailedException(getClass().getName() , 1 , e);
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
