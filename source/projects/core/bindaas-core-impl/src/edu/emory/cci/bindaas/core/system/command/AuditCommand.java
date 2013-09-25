package edu.emory.cci.bindaas.core.system.command;

import java.util.Hashtable;

import edu.emory.cci.bindaas.core.bundle.Activator;
import edu.emory.cci.bindaas.security.api.IAuditProvider;

public class AuditCommand {

	private IAuditProvider auditProvider;
	
	public IAuditProvider getAuditProvider() {
		return auditProvider;
	}


	public void setAuditProvider(IAuditProvider auditProvider) {
		this.auditProvider = auditProvider;
	}

	
	public void init()
	{
		Hashtable<String, Object> props = new Hashtable<String, Object>();
		props.put("osgi.command.scope", "bindaas:audit");
		props.put("osgi.command.function", new String[] {"clean"});
		Activator.getContext().registerService(AuditCommand.class, this, props);
	}
	
	
	public void clean() throws Exception
	{
		Integer rowsDeleted = auditProvider.clean();
		System.out.println(String.format("Audit Logs purged. Total %s rows deleted", rowsDeleted ));
		
	}
}
