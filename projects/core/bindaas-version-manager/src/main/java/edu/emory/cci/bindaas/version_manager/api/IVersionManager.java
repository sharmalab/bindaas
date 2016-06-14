package edu.emory.cci.bindaas.version_manager.api;

public interface IVersionManager {
	public String getSystemBuild() ;
	public String getSystemMajor() ;
	public String getSystemMinor() ;
	public String getSystemRevision() ;
	public String getSystemBuildDate() ;
}
