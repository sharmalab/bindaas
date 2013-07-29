package edu.emory.cci.bindaas.core.bundle;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.Hashtable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.launch.Framework;

import edu.emory.cci.bindaas.core.config.BindaasConfiguration;
import edu.emory.cci.bindaas.core.rest.security.AuditInLogger;
import edu.emory.cci.bindaas.core.rest.security.SecurityHandler;
import edu.emory.cci.bindaas.core.rest.service.api.IBindaasAdminService;
import edu.emory.cci.bindaas.core.rest.service.api.IExecutionService;
import edu.emory.cci.bindaas.core.rest.service.api.IInformationService;
import edu.emory.cci.bindaas.core.rest.service.api.IManagementService;
import edu.emory.cci.bindaas.core.rest.service.impl.ExecutionServiceImpl;
import edu.emory.cci.bindaas.core.rest.service.impl.InformationServiceImpl;
import edu.emory.cci.bindaas.core.rest.service.impl.ManagementServiceImpl;
import edu.emory.cci.bindaas.core.util.DynamicObject;

/**
 *  Entry point for the application. Register services , set properties . 
 * @author nadir
 *
 */
public class BindaasInitializer implements IBindaasAdminService{

	
	private BindaasConfiguration defaultBindaasConfiguration;
	
	public BindaasConfiguration getDefaultBindaasConfiguration() {
		return defaultBindaasConfiguration;
	}


	public void setDefaultBindaasConfiguration(
			BindaasConfiguration defaultBindaasConfiguration) {
		this.defaultBindaasConfiguration = defaultBindaasConfiguration;
	}

	private ManagementServiceImpl managementService;
	private InformationServiceImpl informationService;
	private ExecutionServiceImpl executionService;
	
	
	
	private SecurityHandler securityModule;
	private AuditInLogger auditModule;
	
	private Log log = LogFactory.getLog(getClass());
	
	private DynamicObject<BindaasConfiguration> bindaasConfiguration;
	
	
	
	
	
	public ManagementServiceImpl getManagementService() {
		return managementService;
	}


	public void setManagementService(ManagementServiceImpl managementService) {
		this.managementService = managementService;
	}


	public InformationServiceImpl getInformationService() {
		return informationService;
	}


	public void setInformationService(InformationServiceImpl informationService) {
		this.informationService = informationService;
	}


	public ExecutionServiceImpl getExecutionService() {
		return executionService;
	}


	public void setExecutionService(ExecutionServiceImpl executionService) {
		this.executionService = executionService;
	}


	public SecurityHandler getSecurityModule() {
		return securityModule;
	}


	public void setSecurityModule(SecurityHandler securityModule) {
		this.securityModule = securityModule;
	}


	public AuditInLogger getAuditModule() {
		return auditModule;
	}


	public void setAuditModule(AuditInLogger auditModule) {
		this.auditModule = auditModule;
	}


	private void initializeBindaasConfiguration() throws Exception
	{
		String instanceName = this.bindaasConfiguration.getObject().getInstanceName();
		if(instanceName == null)
		{
			try {
				instanceName = Inet4Address.getLocalHost().getHostName();
			} catch (UnknownHostException e) {
				log.error("Unable to discern hostname. Assigning default value" , e);
				instanceName = "localhost";
			}
			
			this.bindaasConfiguration.getObject().setInstanceName(instanceName);
			this.bindaasConfiguration.saveObject();
		}
	}
	
	
	public void init() throws Exception
	{
		BundleContext context = Activator.getContext();
		context.registerService(IBindaasAdminService.class.getName(), this , null);
		bindaasConfiguration = new DynamicObject<BindaasConfiguration>("bindaas", defaultBindaasConfiguration , context);
		initializeBindaasConfiguration();
		
		// load authentication & authorization props

		if(bindaasConfiguration.getObject().getEnableAuthentication())
		{
			
			securityModule.setAuthenticationProviderClass(bindaasConfiguration.getObject().getAuthenticationProviderClass());
			
			// authorization 
			
			if(bindaasConfiguration.getObject().getEnableAuthorization())
			{
				securityModule.setAuthorizationProviderClass(bindaasConfiguration.getObject().getAuthorizationProviderClass());	
			}
			
		}
		
		// configure audit
		if(bindaasConfiguration.getObject().getEnableAudit())
		{
			
			auditModule.setAuditProviderClass(bindaasConfiguration.getObject().getAuditProviderClass());
		}
		
		// start server
		start();
		
		
		
	}
	
	
	public void start() throws Exception
	{
		String protocol = bindaasConfiguration.getObject().getProtocol();
		String host = bindaasConfiguration.getObject().getHost();
		String port = bindaasConfiguration.getObject().getPort().toString();
		String publishUrl = protocol + "://" + host + ":" + port  ; // construct it !
		
		Object serviceUrl = bindaasConfiguration.getObject().getProxyUrl();
		
		if(serviceUrl == null)
		{
			synchronized (bindaasConfiguration.getObject()) {
				bindaasConfiguration.getObject().setProxyUrl(publishUrl);
				bindaasConfiguration.saveObject();
			}
			
		}

		BundleContext context = Activator.getContext();
		
		Dictionary<String, Object> adminServiceProps = new Hashtable<String, Object>();
		adminServiceProps.put("edu.emory.cci.bindaas.commons.cxf.service.name", "Bindaas Administration Service");
		adminServiceProps.put("edu.emory.cci.bindaas.commons.cxf.service.address",  publishUrl + "/administration");
		adminServiceProps.put("edu.emory.cci.bindaas.commons.cxf.provider", Arrays.asList(new Object[]{ securityModule}));
		adminServiceProps.put("edu.emory.cci.bindaas.commons.cxf.in.interceptor", Arrays.asList(new Object[]{ auditModule}));
		context.registerService(IManagementService.class.getName(), managementService, adminServiceProps);
		
		Dictionary<String, Object> infoServiceProps = new Hashtable<String, Object>();
		infoServiceProps.put("edu.emory.cci.bindaas.commons.cxf.service.name", "Bindaas Information Service");
		infoServiceProps.put("edu.emory.cci.bindaas.commons.cxf.service.address",  publishUrl + "/info");
		infoServiceProps.put("edu.emory.cci.bindaas.commons.cxf.provider", Arrays.asList(new Object[]{ securityModule}));
		infoServiceProps.put("edu.emory.cci.bindaas.commons.cxf.in.interceptor", Arrays.asList(new Object[]{ auditModule}));
		context.registerService(IInformationService.class.getName(), informationService, infoServiceProps);
		
		Dictionary<String, Object> execServiceProps = new Hashtable<String, Object>();
		execServiceProps.put("edu.emory.cci.bindaas.commons.cxf.service.name", "Bindaas Execution Service");
		execServiceProps.put("edu.emory.cci.bindaas.commons.cxf.service.address",  publishUrl + "/services");
		execServiceProps.put("edu.emory.cci.bindaas.commons.cxf.provider", Arrays.asList(new Object[]{ securityModule}));
		execServiceProps.put("edu.emory.cci.bindaas.commons.cxf.in.interceptor", Arrays.asList(new Object[]{ auditModule}));
		context.registerService(IExecutionService.class.getName(), executionService, execServiceProps);
	
		log.info("Middleware Started");
	}
	
	
	@Override
	public void enableAuthentication() throws Exception {
		// TODO : deprecate 
	}


	@Override
	public void disableAuthentication() throws Exception {
		// TODO : deprecate 
	}

	@Override
	public void enableMethodLevelAuthorization() throws Exception {
		// TODO : deprecate 
		
		
		
	}

	@Override
	public void disableMethodLevelAuthorization() throws Exception {
		// TODO : deprecate 
		
		
		
	}

	@Override
	public void setPort(int port) throws Exception {
		// TODO : deprecate 
		
		
	}

	@Override
	public void setHost(String host) throws Exception {
		// TODO : deprecate 
		
		
	}

	@Override
	public void restart() throws Exception {
		BundleContext context = Activator.getContext();
		final Framework systemBundle = (Framework) context.getBundle(0);
		
		new Thread() {
			  public void run() {
				  try {
					systemBundle.update();
				} catch (BundleException e) {
						e.printStackTrace();
				}
			  }
			}.start();
		
		
	}

	@Override
	public String displayProperties() {
		StringBuffer str = new StringBuffer();
		str.append("Bindaas Service Properties\n");
		str.append(bindaasConfiguration.getObject().toString());
		return str.toString();
	}

	@Override
	public void enableHttps() throws Exception {
		// TODO : depricate
		
		
	}


	@Override
	public void disableHttps() throws Exception {
		// TODO : depricate
		
		
		
	}


	@Override
	public void enableAudit() throws Exception {
		
		// TODO : depricate
		
		
	}


	@Override
	public void disableAudit() throws Exception {
		// TODO : depricate
		
		
		
	}
	
	public String getVersion()
	{
		return Activator.getContext().getBundle().getVersion().toString();
	}

	@Override
	public String showStatus() {
		return null; // TODO : deprecate
	}

	
	

}
