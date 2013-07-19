package edu.emory.cci.bindaas.security.api;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

public class BindaasUser implements Principal {

	public final static String EMAIL_ADDRESS = "email";
	public final static String FIRST_NAME = "firstName";
	public final static String LAST_NAME = "lastName";
	
	private String name;
	private String domain;
	private Map<String,Object> properties;
	
	public BindaasUser(String name)
	{
		
		properties = new HashMap<String, Object>();
		if(name.contains("@"))
		{
			String[] parts = name.split("@");
			domain = parts[1];
			this.name = parts[0];
		}
		else
		{
			domain = "localhost";
			this.name = name;
		}
	}
	
	public BindaasUser()
	{
			domain = "localhost";
			properties = new HashMap<String, Object>();
	}
	

	
	public void setDomain(String domain) {
		this.domain = domain;
	}

	public String getName() {
		return name;
	}

	public String getDomain() {
		return domain;
	}

	public void addProperty(String name , Object propVal) {
		properties.put(name, propVal);
	}
	
	public Object getProperty(String name)
	{
		return properties.get(name);
	}
	
	public String toString()
	{
		return String.format("Name = [%s] Domain = [%s]", name , domain);
	}
	
}
