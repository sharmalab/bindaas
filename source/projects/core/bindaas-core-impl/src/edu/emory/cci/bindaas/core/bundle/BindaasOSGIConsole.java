package edu.emory.cci.bindaas.core.bundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;

public class BindaasOSGIConsole implements CommandProvider {

	private BindaasInitializer bindaasInitialzier;
	private Log log = LogFactory.getLog(getClass());
	
	public BindaasOSGIConsole(BindaasInitializer bindaasInitialzier) {
		this.bindaasInitialzier = bindaasInitialzier;
		log.info("Bindaas OSGI Console Started");
	}

	@Override
	public String getHelp() {
		StringBuffer message = new StringBuffer();
		message.append("version - Bindaas version").append("\n");
		message.append("host - Hostname of Bindaas Server").append("\n");
		message.append("port - Bindaas Server Port.").append("\n");
		message.append("authentication - true|false").append("\n");
		message.append("authorization - true|false").append("\n");
		message.append("audit - true|false").append("\n");
		message.append("bindaasStatus").append("\n");
		message.append("restart").append("\n");

		return message.toString();
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
