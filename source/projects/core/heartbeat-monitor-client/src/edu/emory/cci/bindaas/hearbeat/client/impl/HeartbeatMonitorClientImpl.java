package edu.emory.cci.bindaas.hearbeat.client.impl;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.net.ssl.SSLContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.BasicClientConnectionManager;

import edu.emory.cci.bindaas.core.util.DynamicObject;
import edu.emory.cci.bindaas.core.util.DynamicObject.DynamicObjectChangeListener;
import edu.emory.cci.bindaas.hearbeat.client.api.IHearbeatMonitorClient;
import edu.emory.cci.bindaas.hearbeat.client.bundle.Activator;
import edu.emory.cci.bindaas.hearbeat.client.conf.HeartbeatClientConfiguration;
import edu.emory.cci.bindaas.hearbeat.impl.model.Heartbeat;



public class HeartbeatMonitorClientImpl implements IHearbeatMonitorClient,Runnable {

	private Log log = LogFactory.getLog(getClass());
	private DynamicObject<HeartbeatClientConfiguration> configuration;
	private HeartbeatClientConfiguration defaultConfiguration;
	private String uniqueIdentifier;
	private String lastPingStatus;
	private String url;
	private DefaultHttpClient httpClient;
	
	public HeartbeatClientConfiguration getDefaultConfiguration() {
		return defaultConfiguration;
	}
	public void setDefaultConfiguration(
			HeartbeatClientConfiguration defaultConfiguration) {
		this.defaultConfiguration = defaultConfiguration;
	}


	private  void initHttpClient() throws Exception
	{
		Scheme http = new Scheme("http", 80, PlainSocketFactory.getSocketFactory());

		SSLContext sslcontext = SSLContext.getInstance("TLS");
		sslcontext.init(null, null, null);
		SSLSocketFactory sf = new SSLSocketFactory(
		        sslcontext,
		        SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
		Scheme https443 = new Scheme("https", 443, sf);
		Scheme https8443 = new Scheme("https", 443, sf);

		SchemeRegistry sr = new SchemeRegistry();
		sr.register(http);
		sr.register(https443);
		sr.register(https8443);
		
		ClientConnectionManager connMrg = new BasicClientConnectionManager(sr);
		
		httpClient = new DefaultHttpClient(connMrg);
		
		
	}
	public void init() throws Exception
	{
		if(defaultConfiguration!=null)
		{
			configuration = new DynamicObject<HeartbeatClientConfiguration>("heartbeat-client",defaultConfiguration , Activator.getContext());
			if(configuration.getObject().getUniqueIdentifier() == null && configuration.getUsingDefault())
			{
				// generate a client id
				configuration.getObject().setUniqueIdentifier(UUID.randomUUID().toString());
				configuration.saveObject();
				this.lastPingStatus = "FIRST_TIME";
			}
			if(this.lastPingStatus == null) this.lastPingStatus = "JUST_STARTED";
			this.uniqueIdentifier = configuration.getObject().getUniqueIdentifier();
			this.url = configuration.getObject().getServerUrl();
			initHttpClient();
			
			
			Thread t = new Thread(this, "heartbeat-client");
			t.start();
		}
		else throw new Exception("Default Configuration not specified");
	}

	
	@Override
	public void sendHeartBeat(Heartbeat heartbeat) throws Exception {
		HttpPost postRequest = new HttpPost(this.url);
		try{
			postRequest.setEntity(new org.apache.http.entity.StringEntity(heartbeat.toString()));
			HttpResponse response = httpClient.execute(postRequest);
			if(response.getStatusLine().getStatusCode() == 200 )
			{
				this.lastPingStatus = "SUCCEDED";
			}
			else
			throw new Exception("Could not connect to remote server [" + url + "]");
			
			
		}
		catch(Exception e)
		{
			this.lastPingStatus = "FAILED";
			log.trace("Failed to send heartbeat" , e);
			throw e;
		}
		finally{
			postRequest.releaseConnection();
		}
		
	}
	
	
	private Heartbeat createHeartbeat() throws UnknownHostException
	{
		Heartbeat heartbeat = new Heartbeat();
		heartbeat.setHostname(Inet4Address.getLocalHost().getHostAddress());
		heartbeat.setUniqueId(uniqueIdentifier);
		long totalMemory = Runtime.getRuntime().totalMemory(); 
		long usedMemory = totalMemory - Runtime.getRuntime().freeMemory();
		Double utilization = ((double)usedMemory/(double)totalMemory) * 100;
		heartbeat.setMemoryUtilization(utilization);
		heartbeat.setTimestamp((new Date()).toString());
		heartbeat.setLastPingStatus(new String(this.lastPingStatus));
		heartbeat.setJavaVersion(System.getProperty("java.version"));
		heartbeat.setJavaVendor(System.getProperty("java.vendor"));
		heartbeat.setOsName(System.getProperty("os.name"));
		heartbeat.setOsArch(System.getProperty("os.arch"));
		heartbeat.setOsVersion(System.getProperty("os.version"));
		heartbeat.setUserAccount(System.getProperty("user.name"));
		
		return heartbeat;
	}

	@Override
	public void run() {
		try {
			
			 final AtomicBoolean enable = new AtomicBoolean(configuration.getObject().isEnable());
			 final AtomicInteger frequency = new AtomicInteger(configuration.getObject().getFrequency());
			 
			 DynamicObjectChangeListener<HeartbeatClientConfiguration> listener = new DynamicObjectChangeListener<HeartbeatClientConfiguration>() {

					@Override
					public void update(HeartbeatClientConfiguration object) {
						enable.set(object.isEnable());
						frequency.set(object.getFrequency());
					}
				
			};
			
			configuration.addChangeListener(listener);

			while(enable.get())
			{
				try{
					Heartbeat heartbeat = createHeartbeat();
					sendHeartBeat(heartbeat);
				}catch(Exception e)
				{
					log.trace(e);
				}
				
				Thread.sleep(frequency.get() * 1000);
			}
		}
		catch(Exception e)
		{
			log.trace("Failed to send heartbeaat",e);
		}
		
	}
	
}
