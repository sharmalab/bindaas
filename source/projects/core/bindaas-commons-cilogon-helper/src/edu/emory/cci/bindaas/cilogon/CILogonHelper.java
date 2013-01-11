package edu.emory.cci.bindaas.cilogon;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;

import javax.xml.bind.DatatypeConverter;

import net.oauth.signature.pem.PEMReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bouncycastle.util.encoders.Base64;

import edu.uiuc.ncsa.security.util.pkcs.KeyUtil;

public class CILogonHelper {

	
	private Log log = LogFactory.getLog(getClass());
	private String clientId;
	private String privateKeyFile;
	private String publicKeyFile;
	private PublicKey publicKey;
	private PrivateKey privateKey;
	private String ciLogonUrl;
	private URI ciLogonURL;
	
	
	public CILogonSession createSession(String redirectUrl) throws URISyntaxException{
		URI redirectURL = new URI(redirectUrl);
		return new CILogonSession(ciLogonURL, redirectURL, 100 , clientId, privateKey, publicKey);
	}
	
	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getPrivateKeyFile() {
		return privateKeyFile;
	}

	public void setPrivateKeyFile(String privateKeyFile) {
		this.privateKeyFile = privateKeyFile;
	}

	public String getPublicKeyFile() {
		return publicKeyFile;
	}

	public void setPublicKeyFile(String publicKeyFile) {
		this.publicKeyFile = publicKeyFile;
	}

	public String getCiLogonUrl() {
		return ciLogonUrl;
	}

	public void setCiLogonUrl(String ciLogonUrl) {
		this.ciLogonUrl = ciLogonUrl;
	}
	
	
	
	
	public void init() throws Exception
	{
		try {
//			publicKey = KeyUtil.fromX509PEM(publicKeyFile);
//			privateKey = KeyUtil.fromPKCS8PEM(privateKeyFile);
			PEMReader pemReaderPublicKey =  new PEMReader(publicKeyFile);
			
			
			publicKey = KeyUtil.fromX509DER(pemReaderPublicKey.getDerBytes()); 
			privateKey = KeyUtil.fromPKCS8DER(  (new PEMReader(privateKeyFile)).getDerBytes()  );
		
			ciLogonURL = new URI(ciLogonUrl);
		}
		catch(Exception e)
		{
			log.error(e);
			throw e;
		}
		
		Activator.getContext().registerService(this.getClass().getName(), this, null);
		
	}
}
