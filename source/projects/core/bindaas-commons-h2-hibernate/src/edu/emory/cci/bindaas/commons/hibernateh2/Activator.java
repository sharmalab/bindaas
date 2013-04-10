package edu.emory.cci.bindaas.commons.hibernateh2;



import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.internal.SessionFactoryRegistry;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;


public class Activator implements BundleActivator {
	private Log log = LogFactory.getLog(getClass());
	private static BundleContext context;
	private final static String HIBERNATE_ENTITY_NAME = "Hibernate-Entity";
	public static BundleContext getContext() {
		return context;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
		SessionFactory sessionFactory = searchAndInitializeHibernateEntities(bundleContext);
		bundleContext.registerService(SessionFactory.class.getName(), sessionFactory, null);
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		Activator.context = null;
	}

	
	private SessionFactory searchAndInitializeHibernateEntities(BundleContext context) 
	{
		 
		@SuppressWarnings("rawtypes")
		final Map<String,Class> discoveredEntitiesMap = new HashMap<String, Class>();
		
		Bundle[] bundles = context.getBundles();
		for(Bundle bundle : bundles)
		{
			Object value = bundle.getHeaders().get(HIBERNATE_ENTITY_NAME); 
			if(value!=null)
			{
				String testClassNames = value.toString();
				try{
					String[] testClasses = testClassNames.split(",");
					for(String testClass : testClasses)
					{
						Class clazz = bundle.loadClass(testClass);
						discoveredEntitiesMap.put(testClass, clazz);
						log.debug("Discovered Hibernate-Entity class [" + clazz.getName() + "] in bundle [" + bundle.getSymbolicName() + "]");
					}
				}
				catch(Exception e)
				{
					log.warn("Some Hibernate-Entity from bundle [" + bundle.getSymbolicName() + "] were not added" , e);
				}
			}
		}
		
		final ClassLoader cl = Thread.currentThread().getContextClassLoader();
		try {
		 ClassLoader proxyClassLoader = new ClassLoader() {

			@Override
			public Class<?> loadClass(String name)
					throws ClassNotFoundException {
				Class retVal = null;
				try{
					return cl.loadClass(name);
				}catch(ClassNotFoundException notFoundE)
				{
					retVal = discoveredEntitiesMap.get(name);
					if(retVal == null)
						throw new ClassNotFoundException(name);
					else return retVal;
				}
			}
			 
		};
		
		Thread.currentThread().setContextClassLoader(proxyClassLoader);
		Configuration config = new Configuration();
		
		
		for(@SuppressWarnings("rawtypes") Class clzz : discoveredEntitiesMap.values())
		{
			config.addAnnotatedClass(clzz);
		}
		
		
		config = config.configure();
		
		ServiceRegistry serviceRegistry = new ServiceRegistryBuilder().applySettings(config.getProperties()).buildServiceRegistry();        
		SessionFactory sessionFactory = config.buildSessionFactory(serviceRegistry); 
		
		return sessionFactory; 
		}
		catch(RuntimeException e)
		{
			log.error(e);
			return null;
		}
		finally {
			Thread.currentThread().setContextClassLoader(cl);  // restore the original class loader
		}
		
		
	}
}
