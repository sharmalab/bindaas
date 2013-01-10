package edu.emory.cci.bindaas.webconsole;

import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.tools.generic.EscapeTool;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

public class Activator implements BundleActivator {

	private static BundleContext context;
	
	private final static String TEMPLATE_DIRECTORY_PATH = "META-INF/templates";
	private final static EscapeTool escapeTool;
	private static Log log = LogFactory.getLog(Activator.class);
	
	static {
		
		escapeTool = new EscapeTool();
	}

	public static BundleContext getContext() {
		return context;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
		VelocityEngine velocityEngine = new VelocityEngine();
		Properties props = new Properties();
		props.put("resource.loader", "class");
		props.put("class.resource.loader.class",
				"org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
		props.put( RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS, edu.emory.cci.bindaas.webconsole.util.VelocityLogger.class.getName());
		velocityEngine.init(props);	
		context.registerService(VelocityEngine.class.getName(), velocityEngine, null);
		
	}
//	
//	public static void addServiceRegistration(ServiceRegistration sreg)
//	{
//		if(registrations!=null)
//		{
//			registrations.add(sreg);
//		}
//	}
	

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		Activator.context = null;	
	}
	
	public static VelocityEngine getVelocityEngine()
	{
		return getService(VelocityEngine.class);
	}
	
	public static EscapeTool getEscapeTool()
	{
		return escapeTool;
	}
	
	public static Template getVelocityTemplateByName(String templateName)
	{
		String templateLoc = TEMPLATE_DIRECTORY_PATH + "/" + templateName;
		VelocityEngine velocityEngine = getService(VelocityEngine.class);
		if(velocityEngine!=null)
		{
			return velocityEngine.getTemplate(templateLoc);
		}
		else
			return null;
	}
		
	public static <T> T  getService(Class<T> clazz)
	{
		ServiceReference sr = (ServiceReference) context.getServiceReference(clazz.getName());
		if(sr!=null)
		{
			T serviceObj = clazz.cast(context.getService(sr) ) ;
			return serviceObj;
		}
		else
			return null;
	}
	
	public static <T> T  getService(Class<T> clazz , String filter)
	{
		ServiceReference[] sr;
		try {
			sr = (ServiceReference[]) context.getServiceReferences(clazz.getName() , filter);
			if(sr!=null && sr.length > 0)
			{
				T serviceObj = clazz.cast(context.getService(sr[0]) ) ;
				return serviceObj;
			}
			else
				return null;
		} catch (InvalidSyntaxException e) {
			log.error(e);
			return null;
		}
		
	}

}
