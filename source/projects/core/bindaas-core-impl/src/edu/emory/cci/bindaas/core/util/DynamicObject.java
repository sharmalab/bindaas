package edu.emory.cci.bindaas.core.util;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;

import edu.emory.cci.bindaas.framework.util.GSONUtil;


public class DynamicObject<T extends ThreadSafe> {

	private T defaultObject;
	private T currentObject;
	private final static String suffix = ".config.json";
	private String name;
	private String filename;
	private Boolean usingDefault;
	
	public Boolean getUsingDefault() {
		return usingDefault;
	}

	public void setUsingDefault(Boolean usingDefault) {
		this.usingDefault = usingDefault;
	}


	private Log log = LogFactory.getLog(getClass());
	private List<DynamicObjectChangeListener<T>> changeListeners;

	public DynamicObject(String name,T defaultObject,  BundleContext context) throws Exception {
		this.name = name;
		this.filename = name + suffix;
		this.defaultObject = defaultObject;
		changeListeners = new ArrayList<DynamicObject.DynamicObjectChangeListener<T>>();
		init();

		Dictionary<String, String> props = new Hashtable<String, String>();
		props.put("name", this.name);
		props.put("filename", filename);
		props.put("type", defaultObject.getClass().getName());
		context.registerService(DynamicObject.class.getName(), this, props);
		
	}

	@SuppressWarnings("unchecked")
	public void init() throws Exception {
		if(defaultObject!=null)
		{
			usingDefault = true;
			File file = new File(filename);
			if(file.isFile() && file.canRead())
			{
				// read from the file
				try {
						currentObject = (T) GSONUtil.getGSONInstance().fromJson(new FileReader(file),defaultObject.getClass());
						usingDefault = false;
						currentObject.init();
				}
				catch(Exception e)
				{
					log.warn("Reading default properties for [" + this.name + "]",e);
					currentObject = (T) defaultObject.clone();
					currentObject.init();
				}
			}
			else
			{
				log.debug("Reading default properties for [" + this.name + "]");
				currentObject = (T) defaultObject.clone();
				currentObject.init();
				saveObject();
			}
		}
		else throw new Exception("Default values not set");
	}

	public T getObject() {
		return currentObject;
	}

	public void saveObject() throws Exception{
		synchronized (currentObject) {
			FileWriter fw = null;
			try {
				// delete existing file
				File file = new File(filename);
				file.delete();
				// create  a new writer
				fw = new FileWriter(filename);
				// write to the file
				GSONUtil.getGSONInstance().toJson(currentObject, fw);
				sendNotification(currentObject);
			} catch (Exception e) {
				log.error(e);
				throw e;
			} finally {
				if(fw!=null)
					fw.close();
			}

		}
	}
	/**
	 * Send notification asynchronously
	 * @param updateObject
	 */
	private void sendNotification(T updateObject)
	{
		@SuppressWarnings("unchecked")
		final T clonedCopy = (T) updateObject.clone();
		Runnable runnable = new Runnable() {
			
			@Override
			public void run() {
				for(DynamicObjectChangeListener<T> changeListener : changeListeners)
				{
					changeListener.update(clonedCopy);
				}
				
			}
		};
		Thread t = new Thread(runnable);
		t.start();
		
		
	}
	
	@SuppressWarnings("unchecked")
	public synchronized T addChangeListener(DynamicObjectChangeListener<T> changeListener)
	{
		changeListeners.add(changeListener);
		return (T)currentObject.clone();
	}
	public synchronized void removeChangeListener(DynamicObjectChangeListener<T> changeListener)
	{
		changeListeners.remove(changeListener);
	}
	
 
 public static interface DynamicObjectChangeListener<T>  
 {
	 public void update(T object);
 }

}
