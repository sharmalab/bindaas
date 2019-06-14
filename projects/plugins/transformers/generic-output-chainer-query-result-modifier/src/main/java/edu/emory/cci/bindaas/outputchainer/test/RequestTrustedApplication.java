package edu.emory.cci.bindaas.outputchainer.test;

import java.io.ByteArrayOutputStream;
import java.security.MessageDigest;

import javax.xml.bind.DatatypeConverter;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

public class RequestTrustedApplication {
	private final static long ROUNDOFF_FACTOR = 100000;
	public static void main(String[] args) throws Exception {
		DefaultHttpClient httpClient = new DefaultHttpClient();
		String appID = "demo-id";
		String appKey = "demo-secret-key";
		String salt = "asfdagertaewrtae";
		String username = "admin";
		String protocol = "api_key";

		String digest = calculateDigestValue(appID, appKey, salt, username);
		HttpGet get = new HttpGet("http://localhost:9099/trustedApplication/issueShortLivedAuthenticationToken");
		get.addHeader("_protocol", protocol);
		get.addHeader("_username", username);
		get.addHeader("_applicationID", appID);
		get.addHeader("_salt", salt);
		get.addHeader("_digest", digest);
		HttpResponse response = httpClient.execute(get);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		response.getEntity().writeTo(bos);
		System.out.println(new String(bos.toByteArray()));
		
	}
	
	public static String calculateDigestValue(String applicationID , String applicationKey , String salt , String username) throws Exception
	{
		long roundoff = System.currentTimeMillis()/ROUNDOFF_FACTOR;
		String prenonce = roundoff + "|" + applicationKey;
		byte[] nonceBytes = MessageDigest.getInstance("SHA-1").digest(prenonce.getBytes("UTF-8"));
		String nonce = DatatypeConverter.printBase64Binary(nonceBytes);
		
		String predigest = String.format("%s|%s|%s|%s", username , nonce , applicationKey , salt);
		String digest = DatatypeConverter.printBase64Binary(MessageDigest.getInstance("SHA-1").digest(predigest.getBytes("UTF-8")));
		
		System.out.println(String.format("digest[%s]=sha1(%s)\t prenonce=[%s]" , digest , predigest , prenonce));
		return digest;
	}
}
