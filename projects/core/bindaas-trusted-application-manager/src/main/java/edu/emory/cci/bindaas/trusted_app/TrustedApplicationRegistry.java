package edu.emory.cci.bindaas.trusted_app;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.gson.annotations.Expose;

import edu.emory.cci.bindaas.core.util.ThreadSafe;
import edu.emory.cci.bindaas.framework.util.GSONUtil;

public class TrustedApplicationRegistry implements ThreadSafe {

	@Expose private List<TrustedApplicationEntry> trustedApplications;
	private Map<String,TrustedApplicationEntry> lookupTable;
	
	
	public void init() 
	{
		if(trustedApplications!=null)
		{
			lookupTable = new HashMap<String, TrustedApplicationRegistry.TrustedApplicationEntry>();
			for(TrustedApplicationEntry entry : trustedApplications)
			{
				lookupTable.put(entry.getApplicationId(), entry);
			}
		}
	}
	
	public List<TrustedApplicationEntry> getTrustedApplications() {
		return trustedApplications;
	}


	public TrustedApplicationEntry lookup(String trustedApplicationId)
	{
		return lookupTable.get(trustedApplicationId);
	}
	public void setTrustedApplications(
			List<TrustedApplicationEntry> trustedApplications) {
		this.trustedApplications = trustedApplications;
	}

	public TrustedApplicationEntry registerApplication(String applicationName)
	{
		TrustedApplicationEntry entry = new TrustedApplicationEntry();
		entry.setName(applicationName);
		entry.setApplicationId(UUID.randomUUID().toString());
		entry.setApplicationKey(UUID.randomUUID().toString());
		trustedApplications.add(entry);
		lookupTable.put(entry.getApplicationId(), entry);
		return entry;
	}
	

	public Object clone()
	{
		String content = GSONUtil.getGSONInstance().toJson(this);
		return GSONUtil.getGSONInstance().fromJson(content, TrustedApplicationRegistry.class);
	}
	
	
	public static class TrustedApplicationEntry {
		@Expose private String applicationId;
		@Expose private String applicationKey;
		@Expose private String name;
		
		
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getApplicationId() {
			return applicationId;
		}
		public void setApplicationId(String applicationId) {
			this.applicationId = applicationId;
		}
		public String getApplicationKey() {
			return applicationKey;
		}
		public void setApplicationKey(String applicationKey) {
			this.applicationKey = applicationKey;
		}
		
	}
}
