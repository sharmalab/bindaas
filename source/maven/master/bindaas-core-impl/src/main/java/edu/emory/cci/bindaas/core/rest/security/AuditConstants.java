package edu.emory.cci.bindaas.core.rest.security;

public interface AuditConstants {

	// Timestamp of the message
	public final static String TIMESTAMP = "timestamp";
	// Subject requesting 
	public final static String SUBJECT = "subject";
	// Request URI
	public final static String REQUEST_URI = "requestUri";
	// Type of Event
	public final static String EVENT = "event";
	// Outcome [success/failed]
	public final static String OUTCOME = "outcome";
	// Request payload
	public final static String REQUEST = "request";
	// Response payload
	public final static String RESPONSE = "response";
	// Source info
	public final static String SOURCE = "source";
	
	public final static String QUERY_STRING = "queryString";
}
