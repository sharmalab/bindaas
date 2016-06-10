package edu.emory.cci.bindaas.zipstream;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.util.zip.GZIPOutputStream;
import java.io.ObjectOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;

import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;

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
import edu.emory.cci.bindaas.zipstream.bundle.Activator;

/**
 * Created by sagrava on 3/25/15.
 */
public class ZipStreamPlugin implements IQueryResultModifier {

	
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
		
		
		final FileDownloadQRMProperties props = GSONUtil.getGSONInstance()
				.fromJson(modifierProperties, FileDownloadQRMProperties.class);
		
		
		final ResultSetIterator iterator = queryResult.getIntermediateResult();
		final Map<String,Object> responseHeaders = new HashMap<String, Object>();
		final List<String> jsonResult = new ArrayList<String>();

		while(iterator.hasNext()) {
			
			JsonObject currentRecord = iterator.next();
			
			String jsonData = currentRecord.has(props.fileLinkAttribute) ? 
					currentRecord.get(props.fileLinkAttribute).getAsString() : null;
			if(jsonData!=null)
			{
				jsonResult.add(jsonData);
				
			}		
			
			
		}
		queryResult.setMimeType(StandardMimeType.ZIP.toString());
		//responseHeaders.put("Content-Disposition","attachment;filename=\"result-zip-download.zip\"");
		responseHeaders.put("Content-Encoding", "gzip");
		queryResult.setResponseHeaders(responseHeaders);
		
		queryResult.setCallback(new QueryResult.Callback() {
			@Override
			public void callback(OutputStream servletOutputStream,
					Properties context) throws AbstractHttpCodeException {
				BufferedWriter writer = null;
				try {
						
					
				    GZIPOutputStream gzos=new GZIPOutputStream(servletOutputStream);
				    writer = new BufferedWriter(new OutputStreamWriter(gzos, "UTF-8"));
				    writer.write(jsonResult.get(0));
				    log.info("output:[" + jsonResult.get(0) + "]");
				    
					
				} 
				catch (Exception e) {
					log.error(e);
					throw new ModifierExecutionFailedException(getClass()
							.getName(), 1, e);
				} 
				finally{
					try {
						iterator.close();
						if(writer != null) writer.close();
					
					} 
					catch (IOException e) {
						log.fatal("Unable to close ResultSetIterator" , e);
					}
				}
			}
			
			

		});
		

		return queryResult;
	}


	
	public static class FileDownloadQRMProperties {

		@Expose
		private String fileLinkAttribute;
		
	
	}

	

	@Override
	public String getDescriptiveName() {

		return "stream zip file from ResultSet";
	}
}
