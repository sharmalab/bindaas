package edu.emory.cci.bindaas.hearbeat.client.api;

import edu.emory.cci.bindaas.hearbeat.impl.model.Heartbeat;

public interface IHearbeatMonitorClient {

	
	public void sendHeartBeat(Heartbeat heartbeat ) throws Exception ;
}
