package edu.emory.cci.bindaas.datasource.provider.mongodb.outputformat;

import java.util.Dictionary;
import java.util.Hashtable;

import edu.emory.cci.bindaas.datasource.provider.mongodb.bundle.Activator;



public abstract class AbstractFormatHandler implements IFormatHandler{

	public AbstractFormatHandler()
	{
		Dictionary<String, String> prop = new Hashtable<String, String>();
		prop.put("class", getClass().getName());
		Activator.getContext().registerService(IFormatHandler.class.getName(), this, prop);
	}
}
