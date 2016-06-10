package edu.emory.cci.bindaas.version_manager.command;

import java.util.Dictionary;
import java.util.Hashtable;

import edu.emory.cci.bindaas.version_manager.api.IVersionManager;
import edu.emory.cci.bindaas.version_manager.bundle.Activator;

public class VersionCommand {

	private IVersionManager versionManager;
	
	
	public IVersionManager getVersionManager() {
		return versionManager;
	}

	public void setVersionManager(IVersionManager versionManager) {
		this.versionManager = versionManager;
	}

	public void init() {
		Dictionary<String, Object> dict = new Hashtable<String, Object>();
		dict.put("osgi.command.scope", "bindaas");
		dict.put("osgi.command.function", new String[] { "version" });
		Activator.getContext()
				.registerService(VersionCommand.class, this, dict);

	}

	public void version() {
		System.out.println(String.format("System Build : %s\nBuild Date : %s", versionManager.getSystemBuild() , versionManager.getSystemBuildDate()));
	}

}
