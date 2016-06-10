package edu.emory.cci.bindaas.webconsole.util;

import java.util.HashMap;
import java.util.Map;

public class UriTemplate {

	private String[] template;
	private String[] path;
	private boolean match;
	private Map<String,String> parameters;
	
	public UriTemplate(String[] template , String[] path)
	{
		this.template = template;
		this.path = path;
	}
	public boolean isMatch()
	{
		match = true;
		Map<String,String> localParameters = new HashMap<String, String>();
		if(path.length == template.length)
		{	
			for(int index = 0; index < template.length ; index++)
			{
				if(template[index].startsWith("{") && template[index].endsWith("}"))
				{
					localParameters.put(template[index].substring(1,template[index].length()-1), path[index]);
					continue;
				}
				else if(template[index].equals(path[index]))
				{
					continue;
				}
				else
				{
					match = false;
					break;
				}
			}
		}
		else{
			match = false;
		}
		
		
		if(match)
		{
			parameters = localParameters;
		}
		
		return match;
	}
	
	public String[] getTemplate() {
		return template;
	}

	public void setTemplate(String[] template) {
		this.template = template;
	}

	public String[] getPath() {
		return path;
	}

	public void setPath(String[] path) {
		this.path = path;
	}

	public Map<String,String> getParameters()
	{
		 return parameters;
	}
	
}
