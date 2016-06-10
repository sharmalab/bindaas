package edu.emory.cci.bindaas.security.api;

import java.io.IOException;
import java.util.List;

import edu.emory.cci.bindaas.security.model.hibernate.AuditMessage;

public interface IAuditProvider {

	public void audit(AuditMessage auditMessage) throws Exception;
	public List<AuditMessage> getAuditLogs() throws Exception;
	public int clean() throws Exception;
	public void dump(Writer writer) throws Exception;
	
	public static interface Writer 
	{
		public void write(AuditMessage auditMessage) throws IOException;
		public void flush() throws IOException;
		
	}
	
	
}
