package edu.emory.cci.bindaas.security.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.helpers.IOUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import edu.emory.cci.bindaas.core.bundle.Activator;
import edu.emory.cci.bindaas.core.util.DynamicProperties;
import edu.emory.cci.bindaas.framework.util.GSONUtil;
import edu.emory.cci.bindaas.security.api.IAuditProvider;
import edu.emory.cci.bindaas.security.model.hibernate.AuditMessage;

/**
 * Depricated : Should not be used.
 * @author nadir
 *
 */
public class FileSystemAuditProvider implements IAuditProvider{

	private JsonParser parser = GSONUtil.getJsonParser();
	private Properties defaultProperties;
	private Log log = LogFactory.getLog(getClass());
	
	public Properties getDefaultProperties() {
		return defaultProperties;
	}

	public void setDefaultProperties(Properties defaultProperties) {
		this.defaultProperties = defaultProperties;
	}

	private DynamicProperties dynamicProperties;
	
	public void init()
	{
		dynamicProperties = new DynamicProperties("bindaas.audit", defaultProperties , Activator.getContext());
	}
	
	@Override
	public void audit(AuditMessage auditMessage)
			throws Exception {
		Properties props = (Properties) dynamicProperties.getProperties().clone();
		
		String filename = props.getProperty("audit.file");
		if(filename!=null)
		{
			File file = new File(filename); 
			JsonArray array = null;
			if(file.exists())
			{
				array = parser.parse(new FileReader(filename)).getAsJsonArray();
			}
			else
			{
				boolean error = true;
				if(file.getParentFile().exists() == false)
				{
					boolean successful = file.getParentFile().mkdirs();
					if(successful)
					{
						error = ! file.createNewFile();
					}
				}
				
				if(error)
				{
					throw new Exception("Cannot write to the audit file [" + file.getAbsolutePath() + "]");
				}
				
				array = new JsonArray();
			}
			
			String jsonStr = GSONUtil.getGSONInstance().toJson(auditMessage);
			array.add(   parser.parse(jsonStr)  );
			
			IOUtils.copyAndCloseInput(new ByteArrayInputStream(array.toString().getBytes()), new FileOutputStream(file));
		
		}
		
		
		
	}
	
	/**
	 * TODO : UNTESTED DANGEROUS CODE -- SHOULD NOT BE USED AS IS
	 */

	@Override
	public List<AuditMessage> getAuditLogs() throws Exception {
		List<AuditMessage> retVal = new ArrayList<AuditMessage>();
		
		Properties props = (Properties) dynamicProperties.getProperties().clone();
		
		String filename = props.getProperty("audit.file");
		if(filename!=null)
		{
			File file = new File(filename);
			
			if(file.exists() && file.canRead())
			{
				JsonArray array = parser.parse(new FileReader(filename)).getAsJsonArray();
				Iterator<JsonElement> iterator = array.iterator(); 
				while(iterator.hasNext())
				{
					JsonObject jsonObj = iterator.next().getAsJsonObject();
					AuditMessage auditMessage = GSONUtil.getGSONInstance().fromJson(jsonObj, AuditMessage.class);
					retVal.add(auditMessage);
				}
			
			}
			else
			{
				log.error("No file to write Audit Logs");
			}
			
		
		}
		
		return retVal;
		
	}

	@Override
	public int clean() throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void dump(Writer writer) throws Exception {
		// TODO Auto-generated method stub
		
	}

}
