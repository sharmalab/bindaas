package edu.emory.cci.bindaas.commons.cxf;

import java.util.Date;

import org.apache.cxf.endpoint.Server;

public class ResourceContext {
	
	private Server server;
	private String name;
	private Long bundleId;
	private Long serviceId;
	private Date serverStarted;
	private String endpointUrl;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Server getServer() {
		return server;
	}
	public void setServer(Server server) {
		this.server = server;
	}
	public long getBundleId() {
		return bundleId;
	}
	public void setBundleId(long bundleId) {
		this.bundleId = bundleId;
	}
	
	public Long getServiceId() {
		return serviceId;
	}
	public void setServiceId(Long serviceId) {
		this.serviceId = serviceId;
	}
	public void setBundleId(Long bundleId) {
		this.bundleId = bundleId;
	}
	public Date getServerStarted() {
		return serverStarted;
	}
	public void setServerStarted(Date serverStarted) {
		this.serverStarted = serverStarted;
	}
	public String getEndpointUrl() {
		return endpointUrl;
	}
	public void setEndpointUrl(String endpointUrl) {
		this.endpointUrl = endpointUrl;
	}
	
	
	
}
