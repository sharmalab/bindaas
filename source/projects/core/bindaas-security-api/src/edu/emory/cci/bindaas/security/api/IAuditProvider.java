package edu.emory.cci.bindaas.security.api;

import java.util.List;

import edu.emory.cci.bindaas.security.model.hibernate.AuditMessage;

public interface IAuditProvider {

	public void audit(AuditMessage auditMessage) throws Exception;
	public List<AuditMessage> getAuditLogs() throws Exception;
	
	
	
}
