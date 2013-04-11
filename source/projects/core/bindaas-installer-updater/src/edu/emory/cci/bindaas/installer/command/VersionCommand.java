package edu.emory.cci.bindaas.installer.command;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;

import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;

import edu.emory.cci.bindaas.installer.bundle.Activator;


//import org.apache.felix.service.command.*;
public class VersionCommand implements ManagedService{

	private Dictionary properties;
	private String servicePid;
	
	public String getServicePid() {
		return servicePid;
	}

	public void setServicePid(String servicePid) {
		this.servicePid = servicePid;
	}

	public void init()
	{
		Dictionary<String, Object> dict = new Hashtable<String, Object>();
		dict.put("osgi.command.scope", "bindaas");
		dict.put("osgi.command.function", new String[] {"version"});
		dict.put("service.pid",servicePid);
		
		Activator.getContext().registerService(ManagedService.class, this, dict);
	}
	
	@Override
	public void updated(Dictionary properties) throws ConfigurationException {
		this.properties = properties;
		
	}
	
	public void version()
	{
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
