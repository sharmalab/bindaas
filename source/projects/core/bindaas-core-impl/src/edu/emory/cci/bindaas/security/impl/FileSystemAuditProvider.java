package edu.emory.cci.bindaas.security.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
		Dictionary<String, String> props = new Hashtable<String, String>();
		props.put("class", FileSystemAuditProvider.class.getName());
		Activator.getContext().registerService(IAuditProvider.class.getName(), this, props);
		dynamicProperties = new DynamicProperties("bindaas.audit", defaultProperties , Activator.getContext());
	}
	
	@Override
	public void audit(Map<String, String> auditMessage)
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
				array = new JsonArray();
			}
			
			String jsonStr = GSONUtil.getGSONInstance().toJson(auditMessage);
			array.add(   parser.parse(jsonStr)  );
			
			IOUtils.copyAndCloseInput(new ByteArrayInputStream(array.toString().getBytes()), new FileOutputStream(file));
		
		}
		
		
		
	}

	@Override
	public List<Map<String, String>> getAuditLogs() throws Exception {
		List<Map<String, String>> retVal = new ArrayList<Map<String,String>>();
		
		Properties props = (Properties) dynamicProperties.getProperties().clone();
		
		String filename = props.getProperty("audit.file");
		if(filename!=null)
		{
			File file = new File(filename);
			
			if(file.exists() && file.canRead())
			{
				JsonArray array = parser.parse(new FileReader(filename)).getAsJsonArray();
				while(array.iterator().hasNext())
				{
					Map<String,String> auditMessage = new HashMap<String, String>();
					JsonObject jsonObj = array.iterator().next().getAsJsonObject();
					
					for(Entry<String,JsonElement> entry : jsonObj.entrySet())
					{
						auditMessage.put(entry.getKey(), entry.getValue().toString());
					}
					
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

}
