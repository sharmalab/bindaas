package edu.emory.cci.bindaas.core.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;

public class DynamicProperties {
	
	private Properties defaultProperties;
	private Properties properties;
	private boolean loadedFromDefault;
	private String name;
	private String filename;
	private Log log = LogFactory.getLog(getClass());
	
	public DynamicProperties(String name , Properties defaultProperties)
	{
		this.name = name;
		this.defaultProperties = defaultProperties;
		init();
	}
	
	public DynamicProperties(String name , Properties defaultProperties, BundleContext context)
	{
		this.name = name;
		this.defaultProperties = defaultProperties;
		Dictionary<String, String> prop = new Hashtable<String, String>();
		prop.put("name", name);
		context.registerService(DynamicProperties.class.getName(), this, prop);
		init();
	}
	
	private synchronized void save()
	{
		try {
			properties.store(new FileOutputStream(filename), "Last updated : [" + (new Date()).toString() + "]" );
		} catch (Exception e) {
			log.error(e);
		}
		
	}
	
	private void init()
	{
		filename = name + ".properties";
		properties = new Properties();
		try {
			properties.load(new FileInputStream(filename));
		} catch (Exception e) {
			loadedFromDefault = true;
			properties = (Properties) defaultProperties.clone();
			log.debug("Loaded default properties");
			save();
		}
	}
	
	public void put(String key , String value)
	{
		properties.put(key, value);
		save();
	}
	
	public String get(String key)
	{
		return (String) properties.get(key);
	}
	
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

	public boolean isLoadedFromDefault() {
		return loadedFromDefault;
	}

	public void setLoadedFromDefault(boolean loadedFromDefault) {
		this.loadedFromDefault = loadedFromDefault;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public void resetToDefault()
	{
		properties = (Properties) defaultProperties.clone();
		save();
	}

	public Set<Object> keySet() {
		return properties.keySet();
	}
	
	public String toString()
	{
		return properties.toString();
	}

}
