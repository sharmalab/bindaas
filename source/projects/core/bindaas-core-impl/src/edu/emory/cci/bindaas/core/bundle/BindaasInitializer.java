package edu.emory.cci.bindaas.core.bundle;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.launch.Framework;

import edu.emory.cci.bindaas.core.api.BindaasConstants;
import edu.emory.cci.bindaas.core.api.ISecurityHandler;
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
import edu.emory.cci.bindaas.core.util.DynamicProperties;

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
	
	
	public BindaasInitializer()
	{
		
	}
	
	
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


	
	
	
	public void init() throws Exception
	{
		BundleContext context = Activator.getContext();
		context.registerService(CommandProvider.class.getName(), new BindaasOSGIConsole(this), null);
		context.registerService(IBindaasAdminService.class.getName(), this , null);
		bindaasConfiguration = new DynamicObject<BindaasConfiguration>("bindaas", defaultBindaasConfiguration , context);
		
		
		// load authentication & authorization props

		if(bindaasConfiguration.getObject().getEnableAuthentication())
		{
			securityModule.setEnableAuthentication(true);
			securityModule.setAuthenticationProviderClass(bindaasConfiguration.getObject().getAuthenticationProviderClass());
			
			// authorization 
			
			if(bindaasConfiguration.getObject().getEnableAuthorization())
			{
				securityModule.setEnableAuthorization(true);
				securityModule.setAuthorizationProviderClass(bindaasConfiguration.getObject().getAuthorizationProviderClass());
				
			}
			
		}
		
		// configure audit
		if(bindaasConfiguration.getObject().getEnableAudit())
		{
			auditModule.setEnableAudit(true);
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
		
		
		Dictionary<String, Object> cxfServiceProps = new Hashtable<String, Object>();
		cxfServiceProps.put("service.exported.interfaces", "*");
		cxfServiceProps.put("service.exported.intents", "HTTP");
		cxfServiceProps.put("service.exported.configs", "org.apache.cxf.rs");
//		cxfServiceProps.put("org.apache.cxf.rs.address", publishUrl);
		cxfServiceProps.put("org.apache.cxf.rs.provider", securityModule );
		cxfServiceProps.put("org.apache.cxf.rs.in.interceptors",  auditModule );
		// configure securityModule : read props authentication, authorization
		
		BundleContext context = Activator.getContext();
		
		// set Mgmt Bean
		cxfServiceProps.put("org.apache.cxf.rs.address", publishUrl + "/administration"); // TODO use String constants here
		context.registerService(IManagementService.class.getName(), managementService, cxfServiceProps);
		
		cxfServiceProps.put("org.apache.cxf.rs.address", publishUrl + "/info");
		context.registerService(IInformationService.class.getName(), informationService, cxfServiceProps);
		
		cxfServiceProps.put("org.apache.cxf.rs.address", publishUrl + "/services");
		context.registerService(IExecutionService.class.getName(), executionService, cxfServiceProps);
		
		// configure auditModule : read props enable audit
		
		context.registerService(ISecurityHandler.class.getName(), securityModule, null);
		
		log.info("Bindaas Middleware Started");
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
		StringBuffer buffer = new StringBuffer();
		buffer.append("Bindaas Version \t" + getVersion()).append("\n");
		buffer.append(bindaasConfiguration.getObject().toString()).append("\n");
		buffer.append("Registered Data Providers " + informationService.listProviders().getEntity()).append("\n");
		buffer.append("Registered Query Modifiers " + informationService.listQueryModifiers().getEntity()).append("\n");
		buffer.append("Registered QueryResult Modifiers " + informationService.listQueryResultModifiers().getEntity());
		buffer.append("Registered Submit Payload Modifiers " + informationService.listSubmitPayloadModifiers().getEntity()).append("\n");
		return buffer.toString();
	}

	
	

}
