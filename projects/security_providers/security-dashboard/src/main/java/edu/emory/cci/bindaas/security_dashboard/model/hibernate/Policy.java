package edu.emory.cci.bindaas.security_dashboard.model.hibernate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.hibernate.annotations.GenericGenerator;

import com.google.gson.annotations.Expose;

@Entity
public class Policy {

	@Id
	@GeneratedValue(generator="increment")
	@GenericGenerator(name="increment", strategy = "increment")
	private Long id;
	
	@Expose
	@Column(name = "resource" , unique = true)
	private String resource;
	
	@Expose
	private String authorizedGroups;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getResource() {
		return resource;
	}

	public void setResource(String resource) {
		this.resource = resource;
	}

	public String getAuthorizedGroups() {
		return authorizedGroups;
	}

	public void setAuthorizedGroups(String authorizedGroups) {
		this.authorizedGroups = authorizedGroups;
	}
	
	
	
}
