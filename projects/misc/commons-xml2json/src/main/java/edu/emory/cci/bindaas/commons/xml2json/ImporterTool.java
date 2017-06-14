package edu.emory.cci.bindaas.commons.xml2json;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.impl.client.DefaultHttpClient;

import com.google.gson.JsonObject;

import edu.emory.cci.bindaas.commons.xml2json.model.DirectoryProperties;
import edu.emory.cci.bindaas.commons.xml2json.model.HttpProperties;
import edu.emory.cci.bindaas.commons.xml2json.model.ImportDescriptor;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;

import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;


public class ImporterTool {

	private SSLContext ctx ;
	
	public ImporterTool() throws Exception
	{
		ctx = SSLContext.getInstance("TLS");
		ctx.init(new KeyManager[0],
				new TrustManager[] { new DefaultTrustManager() },
				new SecureRandom());
	}
	
	public List<JsonObject> importFromXMLSource(ImportDescriptor importDescriptor) throws Exception
	{
		List<JsonObject> retVal = new ArrayList<JsonObject>();
		XML2JSON xml2json = new XML2JSON();
		xml2json.setNamespaceAware(importDescriptor.isNamespaceAware());
		if(importDescriptor.getPrefixMapping()!=null)
			xml2json.setPrefixes(importDescriptor.getPrefixMapping());
		xml2json.setMappings(importDescriptor.getMappings());
		xml2json.setRootElementSelector(importDescriptor.getRootDocumentSelector());
		xml2json.init();
		
		if(importDescriptor.getUrl()!=null)
		{
			String url = importDescriptor.getUrl();
			if(url.startsWith("http://") && importDescriptor.getUrlProperties()!=null)
			{
				
			}
			else if(url.startsWith("file://"))
			{
				
				List<JsonObject> list = xml2json.parseXML(new FileInputStream(url.replace("file://", "")));
				retVal.addAll(list);
			}
			else if(url.startsWith("directory://") && importDescriptor.getUrlProperties()!=null)
			{
				DirectoryProperties directoryProps = GSONUtil.getGSONInstance().fromJson(importDescriptor.getUrlProperties(), DirectoryProperties.class);
				String path = url.replace("directory://", "");
				File rootDirectory = new File(path);
				if(rootDirectory.isDirectory())
				{
					List<File> files = fetchFilesFromDirectory(directoryProps.getPatternContains(), rootDirectory , directoryProps.isRecursiveScan());
					for(File file : files)
					{
						List<JsonObject> list = xml2json.parseXML(new FileInputStream(file));
						retVal.addAll(list);
					}
					
				}
				else 
					throw new Exception(path + " is not a valid directory");
				
			}
			else
			{
				throw new Exception("Unsupported protocol [" + url + "] or missing properties");
			}
		}
		else
			throw new Exception("Missing url field");
		
		
		return retVal;
	}
	
	
	private List<File> fetchFilesFromDirectory(final String pattern , File parent , boolean scanRecursively)
	{
		 List<File> files = new ArrayList<File>();
		 
		 for(File file : parent.listFiles())
		 {
			 if(file.isDirectory() && scanRecursively)
			 {
				 files.addAll(fetchFilesFromDirectory(pattern, file, scanRecursively));
			 }
			 else if(file.isFile())
			 {
				 if(pattern!=null)
				 {
					 if(file.getName().contains(pattern))
						 files.add(file);
				 }
				 else
				 {
					 files.add(file);
				 }
			 }
		 }
		 
		 return files;
	}
	
	public void importFromXMLSource(ImportDescriptor importDescriptor , IJsonWriter writer) throws Exception
	{
		XML2JSON xml2json = new XML2JSON();
		xml2json.setNamespaceAware(importDescriptor.isNamespaceAware());
		if(importDescriptor.getPrefixMapping()!=null)
			xml2json.setPrefixes(importDescriptor.getPrefixMapping());
		xml2json.setMappings(importDescriptor.getMappings());
		xml2json.setRootElementSelector(importDescriptor.getRootDocumentSelector());
		xml2json.init();
		
		if(importDescriptor.getUrl()!=null)
		{
			String url = importDescriptor.getUrl();
			if(url.startsWith("http://") && importDescriptor.getUrlProperties()!=null)
			{
				HttpProperties httpProps = GSONUtil.getGSONInstance().fromJson(importDescriptor.getUrlProperties(), HttpProperties.class);
				
				DefaultHttpClient httpclient = new DefaultHttpClient();
				
				HttpGet getRequest = new HttpGet(url);
				HttpParams param = new BasicHttpParams();
				
				if(httpProps.getQueryParameters()!=null)
				for(String queryParam : httpProps.getQueryParameters().keySet())
				{
					param.setParameter(queryParam, httpProps.getQueryParameters().get(queryParam));
				}
				getRequest.setParams(param);
				
				if(httpProps.getHttpHeaders()!=null)
				for(String httpHeader : httpProps.getHttpHeaders().keySet())
				{
					getRequest.addHeader(httpHeader , httpProps.getHttpHeaders().get(httpHeader));
				}
				
				HttpResponse response = httpclient.execute(getRequest);
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					
					List<JsonObject> list = xml2json.parseXML(entity.getContent());
					for(JsonObject obj : list)
					{
						writer.writeObject(obj ) ;
					}
					
				}
				else throw new Exception("Remote Server returned no response");

			}
			else if(url.startsWith("https://") && importDescriptor.getUrlProperties()!=null)
			{
				HttpProperties httpProps = GSONUtil.getGSONInstance().fromJson(importDescriptor.getUrlProperties(), HttpProperties.class);
				
				DefaultHttpClient httpclient = new DefaultHttpClient();
				SSLContext.setDefault(ctx);

				SSLSocketFactory sf = new SSLSocketFactory(ctx);
				sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
				Scheme scheme443 = new Scheme("https", sf, 443);
//				Scheme scheme8443 = new Scheme("https", sf, 8443);
				httpclient.getConnectionManager().getSchemeRegistry().register(scheme443);
//				httpclient.getConnectionManager().getSchemeRegistry().register(scheme8443);

				
				HttpGet getRequest = new HttpGet(url);
				HttpParams param = new BasicHttpParams();
				if(httpProps.getQueryParameters()!=null)
				for(String queryParam : httpProps.getQueryParameters().keySet())
				{
					param.setParameter(queryParam, httpProps.getQueryParameters().get(queryParam));
				}
				getRequest.setParams(param);
				
				if(httpProps.getHttpHeaders()!=null)
				for(String httpHeader : httpProps.getHttpHeaders().keySet())
				{
					getRequest.addHeader(httpHeader , httpProps.getHttpHeaders().get(httpHeader));
				}
				
				HttpResponse response = httpclient.execute(getRequest);
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					
					List<JsonObject> list = xml2json.parseXML(entity.getContent());
					for(JsonObject obj : list)
					{
						writer.writeObject(obj ) ;
					}
					
				}
				else throw new Exception("Remote Server returned no response");
			}
			else if(url.startsWith("file://"))
			{
				
				List<JsonObject> list = xml2json.parseXML(new FileInputStream(url.replace("file://", "")));
				for(JsonObject obj : list)
				{
					writer.writeObject(obj ) ;
				}
			}
			else if(url.startsWith("directory://") && importDescriptor.getUrlProperties()!=null)
			{
				DirectoryProperties directoryProps = GSONUtil.getGSONInstance().fromJson(importDescriptor.getUrlProperties(), DirectoryProperties.class);
				String path = url.replace("directory://", "");
				File rootDirectory = new File(path);
				if(rootDirectory.isDirectory())
				{
					List<File> files = fetchFilesFromDirectory(directoryProps.getPatternContains(), rootDirectory , directoryProps.isRecursiveScan());
					for(File file : files)
					{
						List<JsonObject> list = xml2json.parseXML(new FileInputStream(file));
						
						for(JsonObject obj : list)
						{
							writer.writeObject(obj ) ;
						}
					}
					
				}
				else 
					throw new Exception(path + " is not a valid directory");
				
			}
			else
			{
				throw new Exception("Unsupported protocol [" + url + "] or missing properties");
			}
		}
		else
			throw new Exception("Missing url field");
		
	}
	
	
	private static class DefaultTrustManager implements X509TrustManager {

		public void checkClientTrusted(X509Certificate[] arg0, String arg1)
				throws CertificateException {
		}

		public void checkServerTrusted(X509Certificate[] arg0, String arg1)
				throws CertificateException {
		}

		public X509Certificate[] getAcceptedIssuers() {
			return null;
		}
	}

}
