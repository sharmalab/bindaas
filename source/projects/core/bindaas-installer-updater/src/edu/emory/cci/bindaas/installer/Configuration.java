package edu.emory.cci.bindaas.installer;

import java.util.List;

import com.google.gson.annotations.Expose;

/**
 * POJO describing repositories,configurations and bundles to install
 * @author nadir
 *
 */
public class Configuration {

	@Expose private List<Repository> repositories;

	public List<Repository> getRepositories() {
		return repositories;
	}

	public void setRepositories(List<Repository> repositories) {
		this.repositories = repositories;
	}
	
}
