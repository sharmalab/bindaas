package edu.emory.cci.bindaas.security_dashboard.model;

import java.util.Date;
import java.util.Map;
import java.util.Set;

import com.google.gson.annotations.Expose;

import edu.emory.cci.bindaas.framework.util.GSONUtil;

public class User {
	@Expose private String name; // username
	@Expose private Map<String,String> attributes; // other attributes
	@Expose private String email; 
	@Expose private String firstName;
	@Expose private String lastName;
	@Expose private Date dob; //  YYYY-MM-DD
	@Expose private String nickname;
	@Expose private String postcode;
	@Expose private Gender gender;
	@Expose private Set<String> groups;	
	
	public Map<String,String> getSregAttributes()
	{
		return null; // TODO: implemented
	}
	
	public Map<String,String> getAxAttributes(Map<String,String> namespaceMapping)
	{
		return null; // TODO: implemented
	}
	
	public Map<String, String> getAttributes() {
		return attributes;
	}
	public void setAttributes(Map<String, String> attributes) {
		this.attributes = attributes;
	}
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
	public Date getDob() {
		return dob;
	}
	public void setDob(Date dob) {
		this.dob = dob;
	}
	
	public String getNickname() {
		return nickname;
	}
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
	public String getPostcode() {
		return postcode;
	}
	public void setPostcode(String postcode) {
		this.postcode = postcode;
	}
	public Gender getGender() {
		return gender;
	}
	public void setGender(Gender gender) {
		this.gender = gender;
	}

	public static enum Gender { M ,F  , UNKNOWN }
	

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
}
