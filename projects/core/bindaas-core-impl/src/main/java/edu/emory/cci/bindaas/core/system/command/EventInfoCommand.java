package edu.emory.cci.bindaas.core.system.command;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Hashtable;

import au.com.bytecode.opencsv.CSVWriter;
import edu.emory.cci.bindaas.core.bundle.Activator;
import edu.emory.cci.bindaas.core.model.hibernate.BindaasEventInfo;
import edu.emory.cci.bindaas.core.system.event.handler.EventLoggerService;



public class EventInfoCommand {

	private EventLoggerService eventLoggerService;
	
	
	
	public EventLoggerService getEventLoggerService() {
		return eventLoggerService;
	}

	public void setEventLoggerService(EventLoggerService eventLoggerService) {
		this.eventLoggerService = eventLoggerService;
	}

	public void init()
	{
		Hashtable<String, Object> props = new Hashtable<String, Object>();
		props.put("osgi.command.scope", "bindaas:event");
		props.put("osgi.command.function", new String[] {"clean" , "dump" ,"help"});
		Activator.getContext().registerService(EventInfoCommand.class, this, props);
	}
	
	public void help()
	{
		System.out.println("clean\t clear all audit logs");
		System.out.println("dump [filename]\t dumps audit logs to file. Default filename = [event.log.csv]");
		
	}
	
	public void dump(String filename) throws Exception
	{
		File file = new File(filename);
		final CSVWriter csvWriter = new CSVWriter(new FileWriter(file));
		csvWriter.writeNext(new String[]{"Timestamp" , "Event Type" , "Topic" , "Data" , "ThreadId" , "ReferenceId" });
		EventLoggerService.Writer writer  = new EventLoggerService.Writer() {
			
			@Override
			public void write(BindaasEventInfo bindaasEventInfo) throws IOException {
				
				csvWriter.writeNext(new String[]{bindaasEventInfo.getTimestamp().toString() , bindaasEventInfo.getEventType() , bindaasEventInfo.getTopic() , bindaasEventInfo.getEventData() , bindaasEventInfo.getThreadId(), bindaasEventInfo.getReferenceId()});
				
			}

			@Override
			public void flush() throws IOException {
				csvWriter.flush();
				
			}
			
		};
		eventLoggerService.dump(writer);
		csvWriter.close();
		System.out.println("Dump written to [" + file.getAbsolutePath() + "]");
	}
	
	public void dump() throws Exception
	{
		String defaultFile = "event.log.csv";
		dump(defaultFile);
	}
	
	public void clean() throws Exception
	{
		Integer rowsDeleted = eventLoggerService.clean();
		System.out.println(String.format("Event Log purged. Total %s rows deleted", rowsDeleted ));
		
	}
}
