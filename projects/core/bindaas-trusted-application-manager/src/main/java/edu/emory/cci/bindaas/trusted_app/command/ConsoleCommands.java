package edu.emory.cci.bindaas.trusted_app.command;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import edu.emory.cci.bindaas.framework.util.GSONUtil;
import edu.emory.cci.bindaas.trusted_app.TrustedApplicationRegistry;
import edu.emory.cci.bindaas.trusted_app.TrustedApplicationRegistry.TrustedApplicationEntry;
import edu.emory.cci.bindaas.trusted_app.bundle.Activator;
import edu.emory.cci.bindaas.trusted_app.impl.TrustedApplicationManagerImpl;


public class ConsoleCommands {
	
	private TrustedApplicationManagerImpl trustedAppManager;
	
	public TrustedApplicationManagerImpl getTrustedAppManager() {
		return trustedAppManager;
	}

	public void setTrustedAppManager(TrustedApplicationManagerImpl trustedAppManager) {
		this.trustedAppManager = trustedAppManager;
	}

	
	public void init()
	{
		Dictionary<String, Object> dict = new Hashtable<String, Object>();
		dict.put("osgi.command.scope", "bindaas-trusted-app");
		dict.put("osgi.command.function", new String[] {"register" , "unregister" , "show"});
		Activator.getContext().registerService(ConsoleCommands.class, this, dict);
	}
	
	public void registerTrustedApplication(String applicationName) throws Exception{
		TrustedApplicationRegistry registry = trustedAppManager.getTrustedApplicationRegistry().getObject();
		TrustedApplicationEntry entry = registry.registerApplication(applicationName);
		trustedAppManager.getTrustedApplicationRegistry().saveObject();
		System.out.println(GSONUtil.getGSONInstance().toJson(entry));
	}
	
	public void deleteTrustedApplication(String applicationID) throws Exception{
		throw new Exception("Not Implemented"); // TODO implement this
	};
	
	
	public void show()
	{
		TrustedApplicationRegistry registry = trustedAppManager.getTrustedApplicationRegistry().getObject();
		List<TrustedApplicationEntry> entries = registry.getTrustedApplications();
		for(TrustedApplicationEntry entry : entries)
		{
			System.out.println("++++++++++++");
			System.out.println(GSONUtil.getGSONInstance().toJson(entry));
			
		}
		
	}
	
	
	public void unregister() throws Exception{
		System.out.println("Enter Trusted Application ID");
		BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
		String applicationID = bufferRead.readLine();
		deleteTrustedApplication(applicationID);
	}
	public void register() throws Exception{
		System.out.println("Enter Name of the Trusted Application");
		BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
		String applicationName = bufferRead.readLine();
		registerTrustedApplication(applicationName);
	}
}
