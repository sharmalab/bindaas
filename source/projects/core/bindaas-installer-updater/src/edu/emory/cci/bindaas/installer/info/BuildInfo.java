package edu.emory.cci.bindaas.installer.info;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

import edu.emory.cci.bindaas.installer.bundle.Activator;

public class BuildInfo {

	private Properties defaultProperties;
	public Properties getDefaultProperties() {
		return defaultProperties;
	}
	public void setDefaultProperties(Properties defaultProperties) {
		this.defaultProperties = defaultProperties;
	}
	public String getFilename() {
		return filename;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}
	public String getServicePid() {
		return servicePid;
	}
	public void setServicePid(String servicePid) {
		this.servicePid = servicePid;
	}
	private Log log = LogFactory.getLog(getClass());
	private String filename ; // bindaas-framework-info.properties
	private String servicePid; // bindaas-framework-info
	public void init() throws FileNotFoundException, IOException
	{
		BundleContext context = Activator.getContext();
		ServiceReference<ConfigurationAdmin> srf = context.getServiceReference(ConfigurationAdmin.class);
		ConfigurationAdmin configurationAdmin = context.getService(srf);
		
		if(configurationAdmin!=null)
		{
			Configuration configuration = configurationAdmin.getConfiguration(servicePid);
			configuration.setBundleLocation(context.getBundle().getLocation());
			
			File file = new File(filename);
			if(file.exists() && file.isFile() && file.canRead())
			{
				Properties props = new Properties();
				props.load(new FileInputStream(file));
				configuration.update(props);
			}
			else
			{
				log.warn("Properties for service.pid [" + servicePid + "] could not be read. Using default properties");
				configuration.update(defaultProperties);
			}
		
		}
		else
		{
			log.warn("ConfigurationAdmin service not available. Unable to register service.pid [" + servicePid + "]");
		}
		
		
	}
}
