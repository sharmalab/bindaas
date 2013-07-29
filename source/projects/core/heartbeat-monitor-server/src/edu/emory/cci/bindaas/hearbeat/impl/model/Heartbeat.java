package edu.emory.cci.bindaas.hearbeat.impl.model;

import com.google.gson.annotations.Expose;

import edu.emory.cci.bindaas.framework.util.GSONUtil;

public class Heartbeat {

	@Expose private String uniqueId; // uniquely identifies this instance
	@Expose private String hostname;
	@Expose private String timestamp;
	@Expose private Double memoryUtilization; // specified in % of usedMemory/totalMemory
	@Expose private String lastPingStatus; // FIRST_TIME,SUCCESS,FAILED
	@Expose private String osName;
	@Expose private String osArch;
	@Expose private String osVersion;
	@Expose private String userAccount;
	@Expose private String javaVersion;
	@Expose private String javaVendor;
	
	
	public String getOsName() {
		return osName;
	}


	public void setOsName(String osName) {
		this.osName = osName;
	}


	public String getOsArch() {
		return osArch;
	}


	public void setOsArch(String osArch) {
		this.osArch = osArch;
	}


	public String getOsVersion() {
		return osVersion;
	}


	public void setOsVersion(String osVersion) {
		this.osVersion = osVersion;
	}


	public String getUserAccount() {
		return userAccount;
	}


	public void setUserAccount(String userAccount) {
		this.userAccount = userAccount;
	}


	public String getJavaVersion() {
		return javaVersion;
	}


	public void setJavaVersion(String javaVersion) {
		this.javaVersion = javaVersion;
	}


	public String getJavaVendor() {
		return javaVendor;
	}


	public void setJavaVendor(String javaVendor) {
		this.javaVendor = javaVendor;
	}


	public String getUniqueId() {
		return uniqueId;
	}


	public void setUniqueId(String uniqueId) {
		this.uniqueId = uniqueId;
	}


	public String getHostname() {
		return hostname;
	}


	public void setHostname(String hostname) {
		this.hostname = hostname;
	}


	public String getTimestamp() {
		return timestamp;
	}


	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}


	public Double getMemoryUtilization() {
		return memoryUtilization;
	}


	public void setMemoryUtilization(Double memoryUtilization) {
		this.memoryUtilization = memoryUtilization;
	}


	public String getLastPingStatus() {
		return lastPingStatus;
	}


	public void setLastPingStatus(String lastPingStatus) {
		this.lastPingStatus = lastPingStatus;
	}


	public String toString()
	{
		return GSONUtil.getGSONInstance().toJson(this);
	}
}
