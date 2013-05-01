package edu.emory.cci.bindaas.installer.command;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.emory.cci.bindaas.core.system.SystemInfo;
import edu.emory.cci.bindaas.installer.bundle.Activator;


//import org.apache.felix.service.command.*;
public class VersionCommand {

	private Properties properties;
	private Properties defaultProperties;
	private String filename ; // bindaas-framework-info.properties
	private SystemInfo systemInfo;
	
	public SystemInfo getSystemInfo() {
		return systemInfo;
	}

	public void setSystemInfo(SystemInfo systemInfo) {
		this.systemInfo = systemInfo;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}


	private Log log = LogFactory.getLog(getClass());
	
	public Properties getDefaultProperties() {
		return defaultProperties;
	}

	public void setDefaultProperties(Properties defaultProperties) {
		this.defaultProperties = defaultProperties;
	}

	public Properties getProperties() {
		return properties;
	}

	public void setProperties(Properties properties) {
		this.properties = properties;
	}

	
	public void init()
	{
		File file = new File(filename);
		if(file.exists() && file.isFile() && file.canRead())
		{
			
			try {
				this.properties = new Properties();
				this.properties.load(new FileInputStream(file));
			} catch (IOException e) {
				log.trace(e);
			}
	
		}
		else
		{
			log.warn("Properties for version info could not be read. Using default properties");
			properties = defaultProperties;
		}
		
		Dictionary<String, Object> dict = new Hashtable<String, Object>();
		dict.put("osgi.command.scope", "bindaas");
		dict.put("osgi.command.function", new String[] {"version"});
		Activator.getContext().registerService(VersionCommand.class, this, dict);
		
	}
	
	public void version()
	{
		System.out.println("System runtime version :" + systemInfo.getRuntimeVersion());
		if(properties!=null)
		{
			Enumeration<Object> keys = properties.keys();
			while(keys.hasMoreElements())
			{
				Object key = keys.nextElement();
				Object value = properties.get(key);
				System.out.println( String.format("%-40s ===> %-100s", key, value )  );
			}
		}
	}
	
}
