package edu.emory.cci.bindaas.security.api;

import java.util.List;
import java.util.Map;

public interface IAuditProvider {

	public void audit(Map<String,String> auditMessage) throws Exception;
	public List<Map<String,String>> getAuditLogs() throws Exception;
}
