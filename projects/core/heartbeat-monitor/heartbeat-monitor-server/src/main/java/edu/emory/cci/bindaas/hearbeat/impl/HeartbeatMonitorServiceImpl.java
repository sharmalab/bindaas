package edu.emory.cci.bindaas.hearbeat.impl;

import java.util.Dictionary;
import java.util.Hashtable;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import edu.emory.cci.bindaas.core.config.BindaasConfiguration;
import edu.emory.cci.bindaas.core.util.DynamicObject;
import edu.emory.cci.bindaas.framework.util.GSONUtil;
import edu.emory.cci.bindaas.hearbeat.api.IHearbeatMonitorService;
import edu.emory.cci.bindaas.hearbeat.api.IHeartbeatLogger;
import edu.emory.cci.bindaas.hearbeat.bundle.Activator;
import edu.emory.cci.bindaas.hearbeat.impl.model.Heartbeat;

public class HeartbeatMonitorServiceImpl implements IHearbeatMonitorService {

	private Log log = LogFactory.getLog(getClass());
	private IHeartbeatLogger heartbeatLogger;
	private String serviceName;
	
	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public IHeartbeatLogger getHeartbeatLogger() {
		return heartbeatLogger;
	}

	public void setHeartbeatLogger(IHeartbeatLogger heartbeatLogger) {
		this.heartbeatLogger = heartbeatLogger;
	}
	
	public void init() throws Exception
	{
		
		final BundleContext context = Activator.getContext();
		String filterExpression = "(&(objectclass=edu.emory.cci.bindaas.core.util.DynamicObject)(name=bindaas))";
		Filter filter = FrameworkUtil.createFilter(filterExpression);
		final IHearbeatMonitorService ref = this;
		@SuppressWarnings({ "rawtypes", "unchecked" })
		ServiceTracker<DynamicObject<BindaasConfiguration>,DynamicObject<BindaasConfiguration>> serviceTracker = new ServiceTracker<DynamicObject<BindaasConfiguration>,DynamicObject<BindaasConfiguration>>(context, filter,
				new ServiceTrackerCustomizer() {

					@Override
					public Object addingService(ServiceReference srf) {
						@SuppressWarnings("unchecked")
						DynamicObject<BindaasConfiguration> dynamicConfiguration = (DynamicObject<BindaasConfiguration>) context
								.getService(srf);
						Dictionary<String, Object> testProps = new Hashtable<String, Object>();
						testProps.put("edu.emory.cci.bindaas.commons.cxf.service.name", serviceName);
						

						if (dynamicConfiguration != null
								&& dynamicConfiguration.getObject() != null) {
							BindaasConfiguration configuration = dynamicConfiguration
									.getObject();
							String publishUrl = "http://"
									+ configuration.getHost() + ":"
									+ configuration.getPort();
							testProps.put("edu.emory.cci.bindaas.commons.cxf.service.address", publishUrl + "/heartbeatMonitorService");
							context.registerService(IHearbeatMonitorService.class, ref,testProps);
						}

						return null;
					}

					@Override
					public void modifiedService(ServiceReference arg0,
							Object arg1) {
					}

					@Override
					public void removedService(ServiceReference arg0,
							Object arg1) {		
						
					}

				});

		serviceTracker.open();
		
	}

	@Override
	@Path("/send")
	@POST
	public Response sendHeartBeat(String body) {
		try {
			Heartbeat heartbeat = GSONUtil.getGSONInstance().fromJson(body, Heartbeat.class);
			log.trace("Received Heartbeat:\n" + heartbeat);
			heartbeatLogger.logHeartbeat(heartbeat);
		}catch(Exception e)
		{
			log.error("Error in logging heartbeat" , e);
			return Response.serverError().build();
		}
		return Response.ok().build();
	}

}
