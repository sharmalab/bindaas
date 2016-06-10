package edu.emory.cci.bindaas.core.rest.security;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageContentsList;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.security.SecurityContext;
import org.apache.cxf.transport.http.AbstractHTTPDestination;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import edu.emory.cci.bindaas.core.bundle.Activator;
import edu.emory.cci.bindaas.core.config.BindaasConfiguration;
import edu.emory.cci.bindaas.core.util.DynamicObject;
import edu.emory.cci.bindaas.security.api.IAuditProvider;
import edu.emory.cci.bindaas.security.model.hibernate.AuditMessage;

public class AuditInLogger extends AbstractPhaseInterceptor<Message> {

	private Log log = LogFactory.getLog(getClass());
	private ServiceTracker<DynamicObject<BindaasConfiguration>,DynamicObject<BindaasConfiguration>> bindaasConfigServiceTracker;
	
	private String auditProviderClass;

	public boolean isEnableAudit() {
		
		DynamicObject<BindaasConfiguration> bindaasConfig = (DynamicObject<BindaasConfiguration>) bindaasConfigServiceTracker.getService();
		if(bindaasConfig!=null)
		{
			return bindaasConfig.getObject().getEnableAudit();
		}
		else
		{
			log.fatal("BindaasConfiguration not available");
			return false;
		}
	}
	
	
public void init() throws InvalidSyntaxException
{
	String filterExpression = "(&(objectclass=edu.emory.cci.bindaas.core.util.DynamicObject)(name=bindaas))";
	Filter filter = FrameworkUtil.createFilter(filterExpression);
	bindaasConfigServiceTracker = new ServiceTracker <DynamicObject<BindaasConfiguration>,DynamicObject<BindaasConfiguration>>(Activator.getContext(),filter, new ServiceTrackerCustomizer<DynamicObject<BindaasConfiguration>, DynamicObject<BindaasConfiguration>>() {

		@Override
		public DynamicObject<BindaasConfiguration> addingService(
				ServiceReference<DynamicObject<BindaasConfiguration>> arg0) {
			return	Activator.getContext().getService(arg0);
			
		}

		@Override
		public void modifiedService(
				ServiceReference<DynamicObject<BindaasConfiguration>> arg0,
				DynamicObject<BindaasConfiguration> arg1) {
			
		}

		@Override
		public void removedService(
				ServiceReference<DynamicObject<BindaasConfiguration>> arg0,
				DynamicObject<BindaasConfiguration> arg1) {

		}
	});
	bindaasConfigServiceTracker.open();
}
	public String getAuditProviderClass() {
		return auditProviderClass;
	}

	public void setAuditProviderClass(String auditProviderClass) {
		this.auditProviderClass = auditProviderClass;
	}

	public AuditInLogger() {
		super(Phase.POST_INVOKE);
	}

	public void handleMessage(Message message) {
		if (isEnableAudit()) {
			try {
				AuditMessage auditMessage = new AuditMessage();
				String pathInfo = (String) message
						.get("org.apache.cxf.request.url");
				auditMessage.setRequestUri(pathInfo);

				if (message.get(Message.QUERY_STRING) != null)
					auditMessage.setQueryString(message.get(Message.QUERY_STRING).toString());
					


				SecurityContext user = message.get(SecurityContext.class);
				if (user != null && user.getUserPrincipal() != null) {
					auditMessage.setSubject(user
							.getUserPrincipal().toString());
					
				} else {
					auditMessage.setSubject("anonymous");
				}
				
				HttpServletRequest request = (HttpServletRequest) message
						.get(AbstractHTTPDestination.HTTP_REQUEST);

				auditMessage.setTimestamp(new Date());
				auditMessage.setEvent("invoke");
				auditMessage.setSource(request.getRemoteAddr());
				
				Message outMessage = message.getExchange().getOutMessage();
				if(outMessage!=null)
				{
					MessageContentsList objs = MessageContentsList
							.getContentsList(outMessage);

					if (objs != null && objs.size() == 1) {
						Object content = objs.get(0);

						if (content instanceof Response) {
							Response resp = (Response) content;
							auditMessage.setOutcome( resp.getStatus());

						}
					}

				}
				
				IAuditProvider auditProvider = locateAuditProvider();
				if (auditProvider != null) {
					auditProvider.audit(auditMessage);
				} else
					throw new Exception("IAuditProvider not available");
			} catch (Exception e) {

				log.error("Exception in AuditModule", e);
			}
		}
	}

	public void handleFault(Message messageParam) {

	}

	private IAuditProvider locateAuditProvider() {
		final BundleContext context = Activator.getContext();
		@SuppressWarnings("rawtypes")
		ServiceReference[] serviceReferences;
		try {
			serviceReferences = context.getAllServiceReferences(
					IAuditProvider.class.getName(), "(class="
							+ auditProviderClass + ")");
			if (serviceReferences!=null && serviceReferences.length > 0) {
				@SuppressWarnings("unchecked")
				Object service = context.getService(serviceReferences[0]);
				if (service != null) {
					return (IAuditProvider) service;
				}
			}
		} catch (InvalidSyntaxException e) {
			log.error(e);

		}

		return null;

	}
}
