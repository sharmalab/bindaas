package edu.emory.cci.bindaas.core.system.command;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import edu.emory.cci.bindaas.core.bundle.Activator;

public class StatusCommand {

	public Map<String, String> getCriticalServices() {
		return criticalServices;
	}


	public void setCriticalServices(Map<String, String> criticalServices) {
		this.criticalServices = criticalServices;
	}


	/**
	 * Display the status of Bindaas on console
	 */
	private Map<String,String> criticalServices;
	private Log log = LogFactory.getLog(getClass());
	
	public void init()
	{
		Hashtable<String, Object> props = new Hashtable<String, Object>();
		props.put("osgi.command.scope", "bindaas");
		props.put("osgi.command.function", new String[] {"status"});
		Activator.getContext().registerService(StatusCommand.class, this, props);
	}
	
	
	public void status()
	{
		BundleContext context = Activator.getContext();
		StringBuffer strBuff = new StringBuffer();
		Iterator<Entry<String,String>> iterator = criticalServices.entrySet().iterator();
		while(iterator.hasNext())
		{
			Entry<String,String> entry = iterator.next();
			String filter = entry.getValue();
			try {
				ServiceReference<?> srf[] = context.getServiceReferences((String) null, filter);
				if(srf!=null)
				{
					strBuff.append(String.format("\n%-80s ===> %-100s", entry.getKey(), "running" ));
				}
				else
				{
					strBuff.append(String.format("\n%-80s ===> %-100s\n", entry.getKey(), "unavailable" ));
				}
			} catch (InvalidSyntaxException e) {
				log.error("Cannot get service status",e);
			}
		}
		System.out.println(strBuff);
		
	}
}
