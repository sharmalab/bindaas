package edu.emory.cci.bindaas.aime.dataprovider.utils;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;

import edu.emory.cci.bindaas.framework.util.GSONUtil;

public class MigrationSummary {

@Expose private List<MigrationEntry> migrationEntries;	

public MigrationSummary()
{
	migrationEntries = new ArrayList<MigrationSummary.MigrationEntry>();
}
	
public List<MigrationEntry> getMigrationEntries() {
	return migrationEntries;
}

public void setMigrationEntries(List<MigrationEntry> migrationEntries) {
	this.migrationEntries = migrationEntries;
}

	public static class MigrationEntry
	{
		@Expose private String projectName;
		@Expose private String dataProviderName;
		@Expose private JsonObject oldDataSourceConfiguration;
		@Expose private JsonObject newDataSourceConfiguration;
		@Expose private boolean success;
		@Expose private String errorDescription;
		@Expose private Integer rowsUpdated;
		
		
		
		public String getProjectName() {
			return projectName;
		}
		public void setProjectName(String projectName) {
			this.projectName = projectName;
		}
		public String getDataProviderName() {
			return dataProviderName;
		}
		public void setDataProviderName(String dataProviderName) {
			this.dataProviderName = dataProviderName;
		}
		public JsonObject getOldDataSourceConfiguration() {
			return oldDataSourceConfiguration;
		}
		public void setOldDataSourceConfiguration(JsonObject oldDataSourceConfiguration) {
			this.oldDataSourceConfiguration = oldDataSourceConfiguration;
		}
		public JsonObject getNewDataSourceConfiguration() {
			return newDataSourceConfiguration;
		}
		public void setNewDataSourceConfiguration(JsonObject newDataSourceConfiguration) {
			this.newDataSourceConfiguration = newDataSourceConfiguration;
		}
		public boolean isSuccess() {
			return success;
		}
		public void setSuccess(boolean success) {
			this.success = success;
		}
		public String getErrorDescription() {
			return errorDescription;
		}
		public void setErrorDescription(String errorDescription) {
			this.errorDescription = errorDescription;
		}
		public Integer getRowsUpdated() {
			return rowsUpdated;
		}
		public void setRowsUpdated(Integer rowsUpdated) {
			this.rowsUpdated = rowsUpdated;
		}
		
	}

	public String toString(){
		return GSONUtil.getGSONInstance().toJson(this);
	}
}

