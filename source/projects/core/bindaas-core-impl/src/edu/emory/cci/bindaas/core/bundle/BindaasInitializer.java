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
import edu.emory.cci.bindaas.core.rest.security.AuditInLogger;
import edu.emory.cci.bindaas.core.rest.security.SecurityHandler;
import edu.emory.cci.bindaas.core.rest.service.api.IBindaasAdminService;
import edu.emory.cci.bindaas.core.rest.service.api.IExecutionService;
import edu.emory.cci.bindaas.core.rest.service.api.IInformationService;
import edu.emory.cci.bindaas.core.rest.service.api.IManagementService;
import edu.emory.cci.bindaas.core.rest.service.impl.ExecutionServiceImpl;
import edu.emory.cci.bindaas.core.rest.service.impl.InformationServiceImpl;
import edu.emory.cci.bindaas.core.rest.service.impl.ManagementServiceImpl;
import edu.emory.cci.bindaas.core.util.DynamicProperties;

/**
 *  Entry point for the application. Register services , set properties . 
 * @author nadir
 *
 */
public class BindaasInitializer implements IBindaasAdminService{

	
	private Properties defaultBindaasProperties;
	
	private ManagementServiceImpl managementService;
	private InformationServiceImpl informationService;
	private ExecutionServiceImpl executionService;
	
	
	
	private SecurityHandler securityModule;
	private AuditInLogger auditModule;
	
	private Log log = LogFactory.getLog(getClass());
	
	private DynamicProperties bindaasProperties;
	
	
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
		bindaasProperties = new DynamicProperties("bindaas", defaultBindaasProperties , context);
		
		
		// load authentication & authorization props

		if(bindaasProperties.get(BindaasConstants.AUTHENTICATION_STATUS).equals(Boolean.TRUE.toString()))
		{
			securityModule.setEnableAuthentication(true);
			securityModule.setAuthenticationProviderClass((String) bindaasProperties.get(BindaasConstants.AUTHENTICATION_PROVIDER));
			
			// authorization 
			
			if(bindaasProperties.get(BindaasConstants.AUTHORIZATION_STATUS).equals(Boolean.TRUE.toString()))
			{
				securityModule.setEnableAuthorization(true);
				securityModule.setAuthorizationProviderClass((String) bindaasProperties.get(BindaasConstants.AUTHORIZATION_PROVIDER));
				
			}
			
		}
		
		// configure audit
		if(bindaasProperties.get(BindaasConstants.AUDIT_STATUS).equals(Boolean.TRUE.toString()))
		{
			auditModule.setEnableAudit(true);
			auditModule.setAuditProviderClass((String) bindaasProperties.get(BindaasConstants.AUDIT_PROVIDER));
		}
		
		// start server
		start();
		
		
		
	}
	
	
	public void start() throws Exception
	{
		String protocol = (String) bindaasProperties.get(BindaasConstants.PROTOCOL);
		String host = (String) bindaasProperties.get(BindaasConstants.HOST);
		String port = (String) bindaasProperties.get(BindaasConstants.PORT);
		String publishUrl = protocol + "://" + host + ":" + port  ; // construct it !
		
		Object serviceUrl = bindaasProperties.get(BindaasConstants.SERVICE_URL);
		
		if(serviceUrl == null)
		{
			bindaasProperties.put(BindaasConstants.SERVICE_URL, publishUrl);
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
		       
	}
	
	
	@Override
	public void enableAuthentication() throws Exception {
	
		this.bindaasProperties.put(BindaasConstants.AUTHENTICATION_STATUS, Boolean.TRUE.toString());
		
		
	}


	@Override
	public void disableAuthentication() throws Exception {
		
		this.bindaasProperties.put(BindaasConstants.AUTHENTICATION_STATUS, Boolean.FALSE.toString());
		
		
	}

	@Override
	public void enableMethodLevelAuthorization() throws Exception {
		this.bindaasProperties.put(BindaasConstants.AUTHORIZATION_STATUS, Boolean.TRUE.toString());
		
		
		
	}

	@Override
	public void disableMethodLevelAuthorization() throws Exception {
		this.bindaasProperties.put(BindaasConstants.AUTHORIZATION_STATUS, Boolean.FALSE.toString());
		
		
		
	}

	@Override
	public void setPort(int port) throws Exception {
		this.bindaasProperties.put(BindaasConstants.PORT, port + "");
		
		
	}

	@Override
	public void setHost(String host) throws Exception {
		this.bindaasProperties.put(BindaasConstants.HOST, host);
		
		
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
	public void addProperty(String key, String value) throws IOException {
		this.bindaasProperties.put(key, value);
		
		
	}

	@Override
	public String displayProperties() {
		StringBuffer str = new StringBuffer();
		str.append("Bindaas Service Properties\n");
		for(Object key : this.bindaasProperties.keySet())
		{
			str.append(key + "\t=\t" + this.bindaasProperties.get(key.toString()));
		}
		return str.toString();
	}

	@Override
	public void enableHttps() throws Exception {
		this.bindaasProperties.put(BindaasConstants.PROTOCOL, "https");
		
		
	}


	@Override
	public void disableHttps() throws Exception {
		this.bindaasProperties.put(BindaasConstants.PROTOCOL, "http");
		
		
		
	}


	@Override
	public void enableAudit() throws Exception {
		
		this.bindaasProperties.put(BindaasConstants.AUDIT_STATUS, Boolean.TRUE.toString());
		
		
	}


	@Override
	public void disableAudit() throws Exception {
		this.bindaasProperties.put(BindaasConstants.AUDIT_STATUS, Boolean.FALSE.toString());
		
		
		
	}
	
	public String getVersion()
	{
		return Activator.getContext().getBundle().getVersion().toString();
	}

	@Override
	public String showStatus() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("Bindaas Version \t" + getVersion()).append("\n");
		buffer.append(bindaasProperties).append("\n");
		buffer.append("Registered Data Providers " + informationService.listProviders().getEntity()).append("\n");
		buffer.append("Registered Query Modifiers " + informationService.listQueryModifiers().getEntity()).append("\n");
		buffer.append("Registered QueryResult Modifiers " + informationService.listQueryResultModifiers().getEntity());
		buffer.append("Registered Submit Payload Modifiers " + informationService.listSubmitPayloadModifiers().getEntity()).append("\n");
		return buffer.toString();
	}

	public Properties getDefaultBindaasProperties() {
		return defaultBindaasProperties;
	}


	public void setDefaultBindaasProperties(Properties defaultBindaasProperties) {
		this.defaultBindaasProperties = defaultBindaasProperties;
	}


	@Override
	public String getProperty(String key) throws Exception {
		return (String) bindaasProperties.get(key);
		
	}
	
 // set properties
	
	

}
