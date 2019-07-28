package edu.emory.cci.bindaas.core.jwt;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UserInfo {

	@SerializedName("sub")
	@Expose
	private String sub;

	@SerializedName("given_name")
	@Expose
	private String givenName;

	@SerializedName("family_name")
	@Expose
	private String familyName;

	@SerializedName("nickname")
	@Expose
	private String nickname;

	@SerializedName("name")
	@Expose
	private String name;

	@SerializedName("picture")
	@Expose
	private String picture;

	@SerializedName("locale")
	@Expose
	private String locale;

	@SerializedName("updated_at")
	@Expose
	private String updatedAt;

	@SerializedName("email")
	@Expose
	private String email;

	@SerializedName("email_verified")
	@Expose
	private Boolean emailVerified;

	@SerializedName("https://bindaas.com/role")
	@Expose
	private String role;

	public String getSub() { return sub; }

	public void setSub(String sub) { this.sub = sub; }

	public String getGivenName() { return givenName; }

	public void setGivenName(String givenName) { this.givenName = givenName; }

	public String getFamilyName() { return familyName; }

	public void setFamilyName(String familyName) { this.familyName = familyName; }

	public String getNickname() { return nickname; }

	public void setNickname(String nickname) { this.nickname = nickname; }

	public String getName() { return name; }

	public void setName(String name) { this.name = name; }

	public String getPicture() { return picture; }

	public void setPicture(String picture) { this.picture = picture; }

	public String getLocale() { return locale; }

	public void setLocale(String locale) { this.locale = locale; }

	public String getUpdatedAt() { return updatedAt; }

	public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

	public String getEmail() { return email; }

	public void setEmail(String email) { this.email = email; }

	public Boolean getEmailVerified() { return emailVerified; }

	public void setEmailVerified(Boolean emailVerified) { this.emailVerified = emailVerified; }

	public String getRole() { return role; }

	public void setRole(String role) { this.role = role; }

}
