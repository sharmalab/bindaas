package edu.emory.cci.bindaas.security.api;

import java.util.Map;
import java.util.Properties;

public interface IAuditProvider {

	public void audit(Map<String,String> auditMessage) throws Exception;
}
