package edu.emory.cci.bindaas.core.system;

import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import edu.emory.cci.bindaas.core.bundle.Activator;
import edu.emory.cci.bindaas.framework.event.BindaasEvent;
import edu.emory.cci.bindaas.framework.event.BindaasEventConstants;

public class StartupEventPublisher implements Runnable{
private long threshold = 1000;
	public Map<String, String> getCriticalServices() {
		return criticalServices;
	}

	public void setCriticalServices(Map<String, String> criticalServices) {
		this.criticalServices = criticalServices;
	}

	private Map<String,String> criticalServices;
	private Log log = LogFactory.getLog(getClass());
	
	public void init()
	{
		criticalServices = new HashMap<String, String>(criticalServices); // work with a copy
		Thread t = new Thread(this);
		t.start();
	}

	@Override
	public void run() {
		BundleContext context = Activator.getContext();
		
		
		while(true)
		{
			Iterator<Entry<String,String>> iterator = criticalServices.entrySet().iterator();
			while(iterator.hasNext())
			{
				Entry<String,String> entry = iterator.next();
				String filter = entry.getValue();
				try {
					ServiceReference<?> srf[] = context.getServiceReferences((String) null, filter);
					if(srf!=null)
					{
						iterator.remove();
					}
				} catch (InvalidSyntaxException e) {
					log.error("Cannot get service status",e);
				}
			}
			
			if(criticalServices.size() > 0)
			{
				-- threshold;
				if(threshold > 0)
				{
					log.trace("Sleeping off for 0.1 seconds");
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						log.error("StartupEventPublisher interrupted when asleep");
					}
				}
				else
				{
					log.fatal("Bindaas Server failed to start");
					for(String service : criticalServices.keySet())
					{
						log.fatal("Critical Service Not started :[" + service + "]");
					}
					System.exit(-1);
				}
				
			}
			else
			{
				break;
			}
			
		}
		
		BindaasEvent.addTopic(BindaasEventConstants.BINDAAS_START);
		new BindaasEvent(BindaasEventConstants.BINDAAS_START, null).emitAsynchronously();
		double jvmUpTime =  ( (double) ManagementFactory.getRuntimeMXBean().getUptime() / 1000 ) ;
		
		log.info("Bindaas Server started in [" + jvmUpTime + "] seconds ");
	}
}
