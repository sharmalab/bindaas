package edu.emory.cci.bindaas.hearbeat.api;

import java.io.IOException;

import edu.emory.cci.bindaas.hearbeat.impl.model.Heartbeat;

public interface IHeartbeatLogger {

	public void logHeartbeat(Heartbeat heartbeat) throws IOException;
}
