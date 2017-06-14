package edu.emory.cci.bindaas.blobdownload;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.activation.MimetypesFileTypeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.annotations.Expose;

import edu.emory.cci.bindaas.blobdownload.bundle.Activator;
import edu.emory.cci.bindaas.framework.api.IQueryResultModifier;
import edu.emory.cci.bindaas.framework.model.ModifierException;
import edu.emory.cci.bindaas.framework.model.QueryResult;
import edu.emory.cci.bindaas.framework.model.RequestContext;
import edu.emory.cci.bindaas.framework.model.ResultSetIterator;
import edu.emory.cci.bindaas.framework.provider.exception.AbstractHttpCodeException;
import edu.emory.cci.bindaas.framework.provider.exception.ModifierExecutionFailedException;
import edu.emory.cci.bindaas.framework.util.DocumentationUtil;
import edu.emory.cci.bindaas.framework.util.GSONUtil;
import edu.emory.cci.bindaas.framework.util.StandardMimeType;

/**
 * Created by sagrava on 3/25/15.
 */
public class BlobDownloadPlugin implements IQueryResultModifier {

	private Log log = LogFactory.getLog(getClass());
	private static final String DOCUMENTATION_RESOURCES_LOCATION = "META-INF/documentation";
	private JsonObject documentation;
		
	@Override
	public JsonObject getDocumentation() {

		return documentation;
	}

	@Override
	public void validate() throws ModifierException {

	}

	public void init() throws Exception {
		BundleContext context = Activator.getContext();
		documentation = DocumentationUtil.getProviderDocumentation(context,
				DOCUMENTATION_RESOURCES_LOCATION);
		Dictionary<String, String> props = new Hashtable<String, String>();
		props.put("class", getClass().getName());
		context.registerService(IQueryResultModifier.class.getName(), this,
				props);
	}

	@Override
	public QueryResult modifyQueryResult(final QueryResult queryResult,
			JsonObject dataSource, RequestContext requestContext,
			final JsonObject modifierProperties, Map<String, String> queryParams)
			throws AbstractHttpCodeException {
		
		final BlobDownloadQRMProperties props = GSONUtil.getGSONInstance()
				.fromJson(modifierProperties, BlobDownloadQRMProperties.class);
		
		
		final ResultSetIterator iterator = queryResult.getIntermediateResult();
		final Map<String,Object> responseHeaders = new HashMap<String, Object>();
		final List<File> files = new ArrayList<File>();

		while(iterator.hasNext()) {
			JsonObject currentRecord = iterator.next();
			
			String fileLink = currentRecord.has(props.fileLinkAttribute) ? 
								currentRecord.get(props.fileLinkAttribute).getAsString() : null;
			if(fileLink!=null)
			{
				File file = new File(fileLink);
				if(file.isFile() && file.canRead())
				{
					files.add(file);
								        							
				}
				
			}
		}
		// if there is more than 1 file set the mime type to zip file
		if (props.zipDownload != null && props.zipDownload.equals("true")) {
			
			queryResult.setMimeType(StandardMimeType.ZIP.toString());
			responseHeaders.put("Content-Disposition","attachment;filename=\"bindaas-blob-download.zip\"");
			queryResult.setResponseHeaders(responseHeaders);
			
		} 
		else {
			// otherwise set the mime type to whatever the file is
			File file = files.get(0);
			MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
			String mimeType = mimeTypesMap.getContentType(file);			
			queryResult.setMimeType(mimeType);	
			responseHeaders.put("Content-Disposition","attachment;filename=\"" + file.getName() + "\"");
			queryResult.setResponseHeaders(responseHeaders);
			
		} 
		queryResult.setCallback(new QueryResult.Callback() {
			@Override
			public void callback(OutputStream servletOutputStream,
					Properties context) throws AbstractHttpCodeException {
				
				try {
					
					if (props.zipDownload.equals("true")) {
						
						writeZipOutputStream(files, servletOutputStream);
					} 
					else if (files.size() == 1) {
						File file = files.get(0);
						writeFileOutputStream(file, servletOutputStream);				        					        							
						
					} 
					

					
				} 
				catch (Exception e) {
					log.error(e);
					throw new ModifierExecutionFailedException(getClass()
							.getName(), 1, e);
				} 
				finally{
					try {
						iterator.close();
					} 
					catch (IOException e) {
						log.fatal("Unable to close ResultSetIterator" , e);
					}
				}
			}
			
			private void writeFileOutputStream(File file, OutputStream servletOutputStream) throws IOException {
				log.info("writeFileOuputStream for " + file.getName());			
				InputStream in = new FileInputStream(file);
		        byte[] buffer = new byte[1024];
		        int len = in.read(buffer);
		        while (len != -1) {
		        	servletOutputStream.write(buffer, 0, len);
		            len = in.read(buffer);
		        }
		        in.close();
		        servletOutputStream.close();
		        log.info("completed writeFileOuputStream for " + file.getName());	
		        
			}
			private void writeZipOutputStream(List<File> files, OutputStream servletOutputStream) throws IOException {
				log.info("starting writeZipOutputStream for " + files.size() + " files");
				ZipOutputStream zos = new ZipOutputStream(servletOutputStream);
				ZipEntry fileDirectory = new ZipEntry("./");
				zos.putNextEntry(fileDirectory);			
				int counter = 0;
				for (File file : files) {
					
					String locationToSave = "./"  + (++counter) + "-" + file.getName();
					// do your magic here
					packImage(file, zos, locationToSave);

					
				}
				zos.closeEntry();												
				zos.close();	
				log.info("completed writeZipOutputStream for " + (counter-1) + " files");
			}

		});
		

		return queryResult;
	}


	
	private void packImage(File image , ZipOutputStream zos , String locationToSave) {
		
		try {
			log.info("packing Image... location to save: " + locationToSave);
			ZipEntry entry = new ZipEntry(locationToSave);
			zos.putNextEntry(entry);
			byte[] buffer = new byte[1024 * 5];

			FileInputStream fis = new FileInputStream(image);
			int bytesRead = -1;
			while ((bytesRead = fis.read(buffer)) != -1) {
				zos.write(buffer, 0, bytesRead);
			}

			zos.closeEntry();
			fis.close();

		} 
		catch (Exception e) {
				log.error(e);
		}

		
	}
	
	public static class BlobDownloadQRMProperties {

		@Expose
		private String fileLinkAttribute;
		
		@Expose
		private String zipDownload;
	}

	@Override
	public String getDescriptiveName() {

		return "Download blobs identified by attribute in query result";
	}
}
