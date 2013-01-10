package edu.emory.cci.bindaas.security.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

import org.apache.cxf.helpers.IOUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import edu.emory.cci.bindaas.core.bundle.Activator;
import edu.emory.cci.bindaas.core.util.DynamicProperties;
import edu.emory.cci.bindaas.framework.util.GSONUtil;
import edu.emory.cci.bindaas.security.api.IAuditProvider;

public class FileSystemAuditProvider implements IAuditProvider{

	private JsonParser parser = GSONUtil.getJsonParser();
	private Properties defaultProperties;
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

}
