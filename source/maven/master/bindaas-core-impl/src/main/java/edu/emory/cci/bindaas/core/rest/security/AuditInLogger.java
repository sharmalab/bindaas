package edu.emory.cci.bindaas.core.rest.security;

import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

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
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import edu.emory.cci.bindaas.core.bundle.Activator;
import edu.emory.cci.bindaas.security.api.IAuditProvider;

public class AuditInLogger extends AbstractPhaseInterceptor<Message> {

	private Log log = LogFactory.getLog(getClass());
	private boolean enableAudit;
	private Properties auditProviderProps;
	private String auditProviderClass;

	public boolean isEnableAudit() {
		return enableAudit;
	}

	public void setEnableAudit(boolean enableAudit) {
		this.enableAudit = enableAudit;
	}

	public Properties getAuditProviderProps() {
		return auditProviderProps;
	}

	public void setAuditProviderProps(Properties auditProviderProps) {
		this.auditProviderProps = auditProviderProps;
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
		if (enableAudit) {
			try {
				Map<String, String> auditMessage = new HashMap<String, String>();
				String pathInfo = (String) message
						.get("org.apache.cxf.request.url");
				auditMessage.put(AuditConstants.REQUEST_URI, pathInfo);

				if (message.get(Message.QUERY_STRING) != null)
					auditMessage.put(AuditConstants.QUERY_STRING,
							message.get(Message.QUERY_STRING).toString());

				if (message.get("org.apache.cxf.form_data") != null) {
					Map formParam = (Map) message
							.get("org.apache.cxf.form_data");
					auditMessage.put(AuditConstants.REQUEST,
							formParam.toString());
				}

				SecurityContext user = message.get(SecurityContext.class);
				if (user != null && user.getUserPrincipal() != null) {
					auditMessage.put(AuditConstants.SUBJECT, user
							.getUserPrincipal().toString());
				} else {
					auditMessage.put(AuditConstants.SUBJECT, "anonymous");
				}
				HttpServletRequest request = (HttpServletRequest) message
						.get(AbstractHTTPDestination.HTTP_REQUEST);

				auditMessage.put(AuditConstants.TIMESTAMP, GregorianCalendar
						.getInstance().getTime().toString());
				auditMessage.put(AuditConstants.EVENT, "invoke");
				auditMessage
						.put(AuditConstants.SOURCE, request.getRemoteAddr());
				Message outMessage = message.getExchange().getOutMessage();
				MessageContentsList objs = MessageContentsList
						.getContentsList(outMessage);

				if (objs != null && objs.size() == 1) {
					Object content = objs.get(0);

					if (content instanceof Response) {
						Response resp = (Response) content;
						auditMessage.put(AuditConstants.OUTCOME,
								resp.getStatus() + "");

					}
				}

				IAuditProvider auditProvider = locateAuditProvider();
				if (auditProvider != null) {
					auditProvider.audit(auditMessage, auditProviderProps);
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
		ServiceReference[] serviceReferences;
		try {
			serviceReferences = context.getAllServiceReferences(
					IAuditProvider.class.getName(), "(class="
							+ auditProviderClass + ")");
			if (serviceReferences!=null && serviceReferences.length > 0) {
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
