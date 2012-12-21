package edu.emory.cci.bindaas.webconsole;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.velocity.Template;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.tools.generic.EscapeTool;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

import edu.emory.cci.bindaas.core.api.IManagementTasks;
import edu.emory.cci.bindaas.core.api.IModifierRegistry;
import edu.emory.cci.bindaas.core.api.IProviderRegistry;
import edu.emory.cci.bindaas.core.rest.service.api.IBindaasAdminService;

public class Activator implements BundleActivator {

	private static BundleContext context;
	private static VelocityEngine velocityEngine;
	private final static String TEMPLATE_DIRECTORY_PATH = "META-INF/templates";
	private static List<ServiceRegistration> registrations;
	private static EscapeTool escapeTool;

	public static BundleContext getContext() {
		return context;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
		registrations = new ArrayList<ServiceRegistration>();
		velocityEngine = new VelocityEngine();
		Properties props = new Properties();
		props.put("resource.loader", "class");
		props.put("class.resource.loader.class",
				"org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
		velocityEngine.init(props);
		escapeTool = new EscapeTool();
	}
	
	public static void addServiceRegistration(ServiceRegistration sreg)
	{
		if(registrations!=null)
		{
			registrations.add(sreg);
		}
	}
	

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		Activator.context = null;
		if(registrations!=null)
		{
			for(ServiceRegistration sreg : registrations)
			{
				sreg.unregister();
			}
		}
	}
	
	public static VelocityEngine getVelocityEngine()
	{
		return velocityEngine;
	}
	
	public static EscapeTool getEscapeTool()
	{
		return escapeTool;
	}
	public static Template getVelocityTemplateByName(String templateName)
	{
		String templateLoc = TEMPLATE_DIRECTORY_PATH + "/" + templateName;
		if(velocityEngine!=null)
		{
			return velocityEngine.getTemplate(templateLoc);
		}
		else
			return null;
	}
	
	public static IManagementTasks getManagementTasksBean()
	{
		ServiceReference sr = (ServiceReference) context.getServiceReference(IManagementTasks.class.getName());
		if(sr!=null)
		{
			IManagementTasks managementTasks = (IManagementTasks) context.getService(sr);
			return managementTasks;
		}
		else
			return null;
	}
	
	public static IProviderRegistry getProviderRegistry()
	{
		ServiceReference sr = (ServiceReference) context.getServiceReference(IProviderRegistry.class.getName());
		if(sr!=null)
		{
			IProviderRegistry providerReg = (IProviderRegistry) context.getService(sr);
			return providerReg;
		}
		else
			return null;
	}
	
	public static IModifierRegistry getModifierRegistry()
	{
		ServiceReference sr = (ServiceReference) context.getServiceReference(IModifierRegistry.class.getName());
		if(sr!=null)
		{
			IModifierRegistry modifierReg = (IModifierRegistry) context.getService(sr);
			return modifierReg;
		}
		else
			return null;
	}
	
	public static IBindaasAdminService getBindaasAdminService()
	{
		ServiceReference sr = (ServiceReference) context.getServiceReference(IBindaasAdminService.class.getName());
		if(sr!=null)
		{
			IBindaasAdminService adminServ = (IBindaasAdminService) context.getService(sr);
			return adminServ;
		}
		else
			return null;
	}

}
