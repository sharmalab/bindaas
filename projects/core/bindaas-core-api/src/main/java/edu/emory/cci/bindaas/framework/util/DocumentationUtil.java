package edu.emory.cci.bindaas.framework.util;

import java.io.File;
import java.net.URL;
import java.util.Enumeration;

import org.osgi.framework.BundleContext;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class DocumentationUtil {
	
	/**
	 * Initializes and returns Documentation for a Provider by searching the documentationResourcePath for artificats.
	 * @param context
	 * @param documentationResourcePath
	 * @return
	 */
	
	public static JsonObject getProviderDocumentation(BundleContext context , String documentationResourcePath)
	{
		Enumeration<String> enumerationOfPaths = context.getBundle().getEntryPaths(documentationResourcePath);
		JsonObject retVal = new JsonObject();
		
		while( enumerationOfPaths.hasMoreElements())
		{
			String path = enumerationOfPaths.nextElement();
			try {
					URL url = context.getBundle().getEntry(path);
					File file = new File(url.getFile());
					if( file.getName().endsWith(".html"))
					{
						String name = file.getName().replace(".html", "");
						String content = IOUtils.toString(url.openStream());
						retVal.add(name, new JsonPrimitive(content));
					}
			}
			catch(Exception e)
			{
				// do nothing
			}
			
		}
		return retVal;
	}

}
