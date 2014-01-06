package edu.emory.cci.bindaas.security_dashboard.model;

import java.util.Set;

import com.google.gson.annotations.Expose;

import edu.emory.cci.bindaas.framework.util.GSONUtil;

public class User implements Comparable<User>{
	@Expose private String name; // username
	
	@Expose private String email; 
	@Expose private String firstName;
	@Expose private String lastName;
	@Expose private String apiKey;
	@Expose private String expirationDate;
	
	@Expose private Set<String> groups;	
	
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public Set<String> getGroups() {
		return groups;
	}
	public void setGroups(Set<String> groups) {
		this.groups = groups;
	}
	
	public String toString()
	{
		return GSONUtil.getGSONInstance().toJson(this);
	}
	
	public int hashCode()
	{
		return name.hashCode();
	}
	
	public boolean equals(Object group)
	{
		if(group instanceof User)
		{
			return User.class.cast(group).name.equals(this.name);
		}
		
		return false;
	}
	public String getApiKey() {
		return apiKey;
	}
	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}
	
	public boolean hasApiKey()
	{
		if(apiKey == null) return false ; else return true;
	}
	public String getExpirationDate() {
		return expirationDate;
	}
	public void setExpirationDate(String expirationDate) {
		this.expirationDate = expirationDate;
	}
	
	@Override
	public int compareTo(User o) {
	
		return o.name.compareTo(this.name);
	}
	
	
}
