package edu.emory.cci.bindaas.core.bundle;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Date;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.RequestHandler;
import org.apache.cxf.jaxrs.interceptor.JAXRSInInterceptor;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

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
import edu.emory.cci.bindaas.framework.event.BindaasEvent;
import edu.emory.cci.bindaas.framework.event.BindaasEventConstants;
import edu.emory.cci.bindaas.framework.util.PrettyPrintProperties;
import edu.emory.cci.bindaas.security.impl.TestEventSubscriber;

/**
 *  Entry point for the application. Register services , set properties . 
 * @author nadir
 *
 */
public class BindaasInitializer implements IBindaasAdminService{

	
	private Properties bindaasProperties;
	private String bindaasPropertyFileName;
	private String bindaasAuthenticationPropsFileName;
	private String bindaasAuthorizationPropsFileName;
	private String bindaasAuditPropsFileName;
	
	private ManagementServiceImpl managementService;
	private InformationServiceImpl informationService;
	private ExecutionServiceImpl executionService;
	
	private ServiceRegistration managementServiceReg;
	private ServiceRegistration informationServiceReg;
	private ServiceRegistration executionServiceReg;
	
	
	private SecurityHandler securityModule;
	private AuditInLogger auditModule;
	
	private Log log = LogFactory.getLog(getClass());
	
	
	
	
	public BindaasInitializer()
	{
		BundleContext context = Activator.getContext();
		context.registerService(CommandProvider.class.getName(), new BindaasOSGIConsole(this), null);
		context.registerService(IBindaasAdminService.class.getName(), this , null);
	}
	
	public Properties getBindaasProperties() {
		return bindaasProperties;
	}


	public void setBindaasProperties(Properties bindaasProperties) {
		this.bindaasProperties = bindaasProperties;
	}


	public String getBindaasPropertyFileName() {
		return bindaasPropertyFileName;
	}


	public void setBindaasPropertyFileName(String bindaasPropertyFileName) {
		this.bindaasPropertyFileName = bindaasPropertyFileName;
	}


	public String getBindaasAuthenticationPropsFileName() {
		return bindaasAuthenticationPropsFileName;
	}


	public void setBindaasAuthenticationPropsFileName(
			String bindaasAuthenticationPropsFileName) {
		this.bindaasAuthenticationPropsFileName = bindaasAuthenticationPropsFileName;
	}


	public String getBindaasAuthorizationPropsFileName() {
		return bindaasAuthorizationPropsFileName;
	}


	public void setBindaasAuthorizationPropsFileName(
			String bindaasAuthorizationPropsFileName) {
		this.bindaasAuthorizationPropsFileName = bindaasAuthorizationPropsFileName;
	}


	public String getBindaasAuditPropsFileName() {
		return bindaasAuditPropsFileName;
	}


	public void setBindaasAuditPropsFileName(String bindaasAuditPropsFileName) {
		this.bindaasAuditPropsFileName = bindaasAuditPropsFileName;
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
		
		Properties newBindaasProperties = new PrettyPrintProperties("BINDAAS SERVER");
		// Try to load from local property files
		try{
			log.debug("Loading Bindaas Properties from [" + bindaasPropertyFileName + "]" );
			newBindaasProperties.load(new FileInputStream(bindaasPropertyFileName));
			log.debug("Bindaas Properties loaded . Source = [" + bindaasPropertyFileName  +"]");
			
			//  overwrite current props with loaded props
			bindaasProperties = newBindaasProperties;
			
		}
		catch(IOException e)
		{			
			log.error("Bindaas Properties loaded . Source = [DEFAULT]",e);
			bindaasProperties.store(new FileOutputStream(bindaasPropertyFileName), "Bindaas Service Properties. Last Modified at " + (new Date()).toString()); // use the defualt settings and save the files
			log.debug("Bindaas Properties saved locally");
		}
		
		// load authentication & authorization props

		if(bindaasProperties.get(BindaasConstants.AUTHENTICATION_STATUS).equals(Boolean.TRUE.toString()))
		{
			securityModule.setEnableAuthentication(true);
			securityModule.setAuthenticationProviderClass(bindaasProperties.getProperty(BindaasConstants.AUTHENTICATION_PROVIDER));
			
			try{
				Properties newBindaasAuthenticationProps = new PrettyPrintProperties("BINDAAS AUTHENTICATION");
				newBindaasAuthenticationProps.load(new FileInputStream(bindaasAuthenticationPropsFileName));
				securityModule.setAuthenticationProps(newBindaasAuthenticationProps);
			}
			catch(Exception er)
			{
				log.error("Authentication Provider property file [" + bindaasAuthenticationPropsFileName + "] could not be loaded.Using default" ,er);
				securityModule.getAuthenticationProps().store(new FileOutputStream(bindaasAuthenticationPropsFileName), "Bindaas Authentication Provider Properties. Last Modified at " + (new Date()).toString());
			}
			
			
			// authorization 
			
			
			
			if(bindaasProperties.get(BindaasConstants.AUTHORIZATION_STATUS).equals(Boolean.TRUE.toString()))
			{
				securityModule.setEnableAuthorization(true);
				securityModule.setAuthorizationProviderClass(bindaasProperties.getProperty(BindaasConstants.AUTHORIZATION_PROVIDER));
				
				try{
					Properties newBindaasAuthorizationProps = new PrettyPrintProperties("BINDAAS AUTHORIZATION");
					newBindaasAuthorizationProps.load(new FileInputStream(bindaasAuthorizationPropsFileName));
					securityModule.setAuthorizationProps(newBindaasAuthorizationProps);
				}
				catch(Exception er)
				{
					log.error("Authorization Provider property file [" + bindaasAuthorizationPropsFileName + "] could not be loaded.Using default" ,er);
					securityModule.getAuthorizationProps().store(new FileOutputStream(bindaasAuthorizationPropsFileName), "Bindaas Authorization Provider Properties. Last Modified at " + (new Date()).toString());
				}
				
			}
			
		}
		
		// configure audit
		if(bindaasProperties.get(BindaasConstants.AUDIT_STATUS).equals(Boolean.TRUE.toString()))
		{
			auditModule.setEnableAudit(true);
			auditModule.setAuditProviderClass(bindaasProperties.getProperty(BindaasConstants.AUDIT_PROVIDER));
			
			try{
				Properties newBindaasAuditProps = new PrettyPrintProperties("BINDAAS AUDIT PROPERTIES");
				newBindaasAuditProps.load(new FileInputStream(bindaasAuditPropsFileName));
				auditModule.setAuditProviderProps(newBindaasAuditProps);
			}
			catch(Exception er)
			{
				log.error("Audit Provider property file [" + bindaasAuditPropsFileName + "] could not be loaded.Using default" ,er);
				auditModule.getAuditProviderProps().store(new FileOutputStream(bindaasAuditPropsFileName), "Bindaas Audit Provider Properties. Last Modified at " + (new Date()).toString());
			}
			
		}
		
		// start server
		start();
		
		
		
	}
	
	
	public void start() throws Exception
	{
		String protocol = bindaasProperties.getProperty(BindaasConstants.PROTOCOL);
		String host = bindaasProperties.getProperty(BindaasConstants.HOST);
		String port = bindaasProperties.getProperty(BindaasConstants.PORT);
		String publishUrl = protocol + "://" + host + ":" + port + "/"  ; // construct it !
		
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
		cxfServiceProps.put("org.apache.cxf.rs.address", publishUrl + "administration"); // TODO use String constants here
		managementServiceReg =  context.registerService(IManagementService.class.getName(), managementService, cxfServiceProps);
		
		cxfServiceProps.put("org.apache.cxf.rs.address", publishUrl + "info");
		informationServiceReg = context.registerService(IInformationService.class.getName(), informationService, cxfServiceProps);
		
		cxfServiceProps.put("org.apache.cxf.rs.address", publishUrl + "services");
		executionServiceReg = context.registerService(IExecutionService.class.getName(), executionService, cxfServiceProps);
		
		// configure auditModule : read props enable audit
		
		context.registerService(ISecurityHandler.class.getName(), securityModule, null);
		
		
		/** Temp Code 
		 * 
		 */
		String[] topics = new String[] {
	            BindaasEventConstants.CREATE_PROFILE_TOPIC
	        };
	        
	        Dictionary props = new Hashtable();
	        props.put(EventConstants.EVENT_TOPIC, topics);
	        context.registerService(EventHandler.class.getName(), new TestEventSubscriber() , props); // TODO : Test code to remove later
	        
	}
	
	public void stop() throws Exception
	{
		if(managementServiceReg!=null)
		{
			managementServiceReg.unregister();
		}
		
		if(informationServiceReg!=null)
		{
			informationServiceReg.unregister();
		}
		
		if(executionServiceReg!=null)
		{
			executionServiceReg.unregister();
		}
		
	}
	
	@Override
	public void enableAuthentication() throws Exception {
	
		this.bindaasProperties.put(BindaasConstants.AUTHENTICATION_STATUS, Boolean.TRUE.toString());
		saveBindaasProperties();
		restart();
	}

	private void saveBindaasProperties() throws IOException {
		bindaasProperties.store(new FileOutputStream(bindaasPropertyFileName), "Bindaas Service Properties. Last Modified at " + (new Date()).toString());
		
	}


	@Override
	public void disableAuthentication() throws Exception {
		
		this.bindaasProperties.put(BindaasConstants.AUTHENTICATION_STATUS, Boolean.FALSE.toString());
		saveBindaasProperties();
		restart();
	}

	@Override
	public void enableMethodLevelAuthorization() throws Exception {
		this.bindaasProperties.put(BindaasConstants.AUTHORIZATION_STATUS, Boolean.TRUE.toString());
		saveBindaasProperties();
		restart();
		
	}

	@Override
	public void disableMethodLevelAuthorization() throws Exception {
		this.bindaasProperties.put(BindaasConstants.AUTHORIZATION_STATUS, Boolean.FALSE.toString());
		saveBindaasProperties();
		restart();
		
	}

	@Override
	public void setPort(int port) throws Exception {
		this.bindaasProperties.put(BindaasConstants.PORT, port);
		saveBindaasProperties();
		restart();
		
	}

	@Override
	public void setHost(String host) throws Exception {
		this.bindaasProperties.put(BindaasConstants.HOST, host);
		saveBindaasProperties();
		restart();
	}

	@Override
	public void restart() throws Exception {
		stop();
		init();
	}

	@Override
	public void addProperty(String key, String value) throws IOException {
		this.bindaasProperties.put(key, value);
		saveBindaasProperties();
		
	}

	@Override
	public String displayProperties() {
		StringBuffer str = new StringBuffer();
		str.append("Bindaas Service Properties\n");
		for(Object key : this.bindaasProperties.keySet())
		{
			str.append(key + "\t=\t" + this.bindaasProperties.getProperty(key.toString()));
		}
		return str.toString();
	}


	


	@Override
	public void enableHttps() throws Exception {
		this.bindaasProperties.put(BindaasConstants.PROTOCOL, "https");
		saveBindaasProperties();
		restart();
	}


	@Override
	public void disableHttps() throws Exception {
		this.bindaasProperties.put(BindaasConstants.PROTOCOL, "http");
		saveBindaasProperties();
		restart();
		
	}


	@Override
	public void enableAudit() throws Exception {
		
		this.bindaasProperties.put(BindaasConstants.AUDIT_STATUS, Boolean.TRUE.toString());
		saveBindaasProperties();
		restart();
	}


	@Override
	public void disableAudit() throws Exception {
		this.bindaasProperties.put(BindaasConstants.AUDIT_STATUS, Boolean.FALSE.toString());
		saveBindaasProperties();
		restart();
		
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
		buffer.append(securityModule.getAuthenticationProps()).append("\n");
		buffer.append(securityModule.getAuthorizationProps()).append("\n");
		buffer.append(auditModule.getAuditProviderProps()).append("\n");
		buffer.append("Registered Data Providers " + informationService.listProviders().getEntity()).append("\n");
		buffer.append("Registered Query Modifiers " + informationService.listQueryModifiers().getEntity()).append("\n");
		buffer.append("Registered QueryResult Modifiers " + informationService.listQueryResultModifiers().getEntity());
		buffer.append("Registered Submit Payload Modifiers " + informationService.listSubmitPayloadModifiers().getEntity()).append("\n");
		return buffer.toString();
	}

	@Override
	public String getProperty(String key) throws Exception {
		return bindaasProperties.getProperty(key);
		
	}
	
 // set properties
	
	

}
