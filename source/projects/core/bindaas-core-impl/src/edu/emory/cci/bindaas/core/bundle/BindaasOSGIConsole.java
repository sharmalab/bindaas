package edu.emory.cci.bindaas.core.bundle;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;

/**
 * bindaas -audit true/false
 * bindaas -authentication true/false
 * bindaas -authorization true/false
 * bindaas -version
 * bindaas -status
 * bindaas -help
 * bindaas -host <hostname>
 * bindaas -port <port>
 * bindaas -restart 
 * @author nadir
 *
 */
public class BindaasOSGIConsole implements CommandProvider {

	private BindaasInitializer bindaasInitialzier;
	private Log log = LogFactory.getLog(getClass());
	private CommandLineParser parser;
	private Options bindaasOptions ;
	
	private String usageHelp;
	
	private void initCLI()
	{
		parser = new PosixParser();
		HelpFormatter formatter = new HelpFormatter();
		bindaasOptions = new Options();
		bindaasOptions.addOption("audit", true, "Enable/Disable Audit. Specify [true/false]");
		bindaasOptions.addOption("authentication", true, "Enable/Disable Audit. Specify [true/false]");
		bindaasOptions.addOption("authorization", true, "Enable/Disable Audit. Specify [true/false]");
		bindaasOptions.addOption("help", false, "Help on Command Line options");
		bindaasOptions.addOption("status", false, "Status of Bindaas Server");
		bindaasOptions.addOption("host", true, "Bindaas Server Hostname");
		bindaasOptions.addOption("port", true, "Bindaas Server Port");
		bindaasOptions.addOption("restart", false, "restart Bindaas Server");
		
		StringWriter sw = new StringWriter();
		formatter.printHelp(new PrintWriter(sw),80,"bindaas [options]" , "\n" ,  bindaasOptions , 5, 5, "\n");
		usageHelp = "\n"  + sw.toString() + "\n";
	}
	public BindaasOSGIConsole(BindaasInitializer bindaasInitialzier) {
		this.bindaasInitialzier = bindaasInitialzier;
		initCLI();
		log.info("Bindaas OSGI Console Started");
	}

	@Override
	public String getHelp() {
		return usageHelp;
	}
	
	
	public void _bindaas(CommandInterpreter ci)
	{
		
	}

	public void _version(CommandInterpreter ci) {

		ci.println(bindaasInitialzier.getVersion());
	}

	public void _authentication(CommandInterpreter ci) {
		try {
			if (Boolean.parseBoolean(ci.nextArgument()) == true) {
				bindaasInitialzier.enableAuthentication();
				ci.println("Authentication enabled");
			} else {
				bindaasInitialzier.disableAuthentication();
				ci.println("Authentication disabled");
			}
		} catch (Exception e) {
			ci.printStackTrace(e);
		}
	}

	public void _authorization(CommandInterpreter ci) {
		try {
			if (Boolean.parseBoolean(ci.nextArgument()) == true) {
				bindaasInitialzier.enableMethodLevelAuthorization();
				ci.println("Authorization enabled");
			} else {
				bindaasInitialzier.disableMethodLevelAuthorization();
				ci.println("Authorization disabled");
			}
		} catch (Exception e) {
			ci.printStackTrace(e);
		}
	}

	public void _audit(CommandInterpreter ci) {
		try {
			if (Boolean.parseBoolean(ci.nextArgument()) == true) {
				bindaasInitialzier.enableAudit();
				ci.println("Audit enabled");
			} else {
				bindaasInitialzier.disableAudit();
				ci.println("Audit disabled");
			}
		} catch (Exception e) {
			ci.printStackTrace(e);
		}
	}

	public void _port(CommandInterpreter ci) {
		try {
			int port = Integer.parseInt(ci.nextArgument());
			bindaasInitialzier.setPort(port);
			ci.println("Port set to [" + port + "]");
		} catch (Exception e) {
			ci.printStackTrace(e);
		}
	}

	public void _host(CommandInterpreter ci) {
		try {
			String host = ci.nextArgument();
			bindaasInitialzier.setHost(host);
			ci.println("Host set to [" + host + "]");
		} catch (Exception e) {
			ci.printStackTrace(e);
		}
	}
	
	public void _bindaasStatus(CommandInterpreter ci) {
		try {
			ci.println(bindaasInitialzier.showStatus());
		} catch (Exception e) {
			ci.printStackTrace(e);
		}
	}
	
	public void _restart(CommandInterpreter ci){
		try {
			
			bindaasInitialzier.restart();
			ci.println("Bindaas Server restarted");
		} catch (Exception e) {
			ci.printStackTrace(e);
		}
	}
	
	public void _loggerLevel(CommandInterpreter ci)
	{
		String level = ci.nextArgument();
		if(level!=null)
		{
			if(level.equalsIgnoreCase("debug"))
			{
				Logger.getRootLogger().setPriority(Priority.DEBUG);
				System.out.println("Logger level changed to [" + level + "]");
			}
			else if (level.equalsIgnoreCase("info"))
			{
				Logger.getRootLogger().setPriority(Priority.INFO);
				System.out.println("Logger level changed to [" + level + "]");
			}
			else if(level.equalsIgnoreCase("warn"))
			{
				Logger.getRootLogger().setPriority(Priority.WARN);
				System.out.println("Logger level changed to [" + level + "]");
			}
			else if(level.equalsIgnoreCase("error"))
			{
				Logger.getRootLogger().setPriority(Priority.ERROR);
				System.out.println("Logger level changed to [" + level + "]");
			}
			else 
			{
				System.err.println("Please specify a correct log level");
			}
			
			
		}
		else 
		{
			System.err.println("Please specify log level");
		}
	}

}
