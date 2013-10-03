package edu.emory.cci.bindaas.core.system.command;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Hashtable;

import au.com.bytecode.opencsv.CSVWriter;
import edu.emory.cci.bindaas.core.bundle.Activator;
import edu.emory.cci.bindaas.security.api.IAuditProvider;
import edu.emory.cci.bindaas.security.model.hibernate.AuditMessage;

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
		props.put("osgi.command.function", new String[] {"clean" , "dump" ,"help"});
		Activator.getContext().registerService(AuditCommand.class, this, props);
	}
	
	public void help()
	{
		System.out.println("clean\t clear all audit logs");
		System.out.println("dump [filename]\t dumps audit logs to file. Default filename = [audit.log.csv]");
		
	}
	
	public void dump(String filename) throws Exception
	{
		File file = new File(filename);
		final CSVWriter csvWriter = new CSVWriter(new FileWriter(file));
		csvWriter.writeNext(new String[]{"Timestamp" , "Request URL" , "Query Parameters" , "Requester" , "Source IP" , "Event" , "HTTP Response Code"});
		IAuditProvider.Writer writer  = new IAuditProvider.Writer() {
			
			@Override
			public void write(AuditMessage auditMessage) throws IOException {
				
				csvWriter.writeNext(new String[]{auditMessage.getTimestamp().toString() , auditMessage.getRequestUri() , auditMessage.getQueryString() , auditMessage.getSubject() , auditMessage.getSource() , auditMessage.getEvent(),auditMessage.getOutcome() + "" });
				
			}

			@Override
			public void flush() throws IOException {
				csvWriter.flush();
				
			}
			
		};
		auditProvider.dump(writer);
		csvWriter.close();
		System.out.println("Dump written to [" + file.getAbsolutePath() + "]");
	}
	
	public void dump() throws Exception
	{
		String defaultFile = "audit.log.csv";
		dump(defaultFile);
	}
	
	public void clean() throws Exception
	{
		Integer rowsDeleted = auditProvider.clean();
		System.out.println(String.format("Audit Logs purged. Total %s rows deleted", rowsDeleted ));
		
	}
}
