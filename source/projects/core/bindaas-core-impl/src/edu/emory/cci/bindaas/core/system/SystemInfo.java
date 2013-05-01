package edu.emory.cci.bindaas.core.system;

import edu.emory.cci.bindaas.core.bundle.Activator;

public class SystemInfo {
	private Integer runtimeVersion;

	public Integer getRuntimeVersion() {
		return runtimeVersion;
	}

	public void setRuntimeVersion(Integer runtimeVersion) {
		this.runtimeVersion = runtimeVersion;
	}
	
	public void init(){
		Activator.getContext().registerService(SystemInfo.class, this, null);
	}
}
