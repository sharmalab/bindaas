package edu.emory.cci.bindaas.version_manager.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.emory.cci.bindaas.version_manager.api.IVersionManager;

public class VersionManagerImpl implements IVersionManager{

	private Properties properties;
	private Properties defaultProperties;
	private String filename ; // bindaas-framework-info.properties
	private String systemBuild;
	
	private static final String MAJOR = "bindaas.framework.version.major";
	private static final String MINOR = "bindaas.framework.version.minor";
	private static final String REVISION = "bindaas.framework.version.revision";
	private static final String BUILD_DATE = "bindaas.build.date";
	
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
			this.properties = defaultProperties;
		}
		
		this.systemBuild = String.format("%s.%s.%s", this.properties.get(MAJOR) , this.properties.get(MINOR) , this.properties.get(REVISION) );
	}

	@Override
	public String getSystemBuild() {

		return this.systemBuild;
	}

	@Override
	public String getSystemMajor() {

		return this.properties.getProperty(MAJOR);
	}

	@Override
	public String getSystemMinor() {
		return this.properties.getProperty(MINOR);
	}

	@Override
	public String getSystemRevision() {
		return this.properties.getProperty(REVISION);
	}

	@Override
	public String getSystemBuildDate() {
		return this.properties.getProperty(BUILD_DATE);	
	}
	
}
