package edu.emory.cci.bindaas.framework.util;

import java.util.Properties;

public class PrettyPrintProperties extends Properties{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String propName;
	
	public PrettyPrintProperties(String name)
	{
		this.propName = name;
	}
	
	public String toString(){
		StringBuffer buff= new StringBuffer();
		buff.append("\n");
		buff.append( String.format("++++++++++++ Begin [%s] Properties +++++++++", propName)) .append("\n");
		for(Object key : this.keySet())
		{
			buff.append(String.format("Key=[ %s ] Value=[ %s ]", key,this.get(key))).append("\n"); 
		}
		buff.append( String.format("++++++++++++ End [%s] Properties +++++++++", propName)) .append("\n");
		buff.append("\n");
		return buff.toString();
	}
}
