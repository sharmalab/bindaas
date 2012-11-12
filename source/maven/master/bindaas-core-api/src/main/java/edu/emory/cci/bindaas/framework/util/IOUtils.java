package edu.emory.cci.bindaas.framework.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

public class IOUtils {

	public static String toString(InputStream in) throws IOException
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buffer = new byte[2048];
		int bytesRead = -1;
		
		while((bytesRead = in.read(buffer)) > 0)
		{
			baos.write(buffer, 0, bytesRead);
		}
		
		baos.close();
		return (new String(baos.toByteArray()));
	}
	
	public static String join(Collection<?> s, String delimiter) {
	     StringBuilder builder = new StringBuilder();
	     Iterator iter = s.iterator();
	     while (iter.hasNext()) {
	         builder.append(iter.next());
	         if (!iter.hasNext()) {
	           break;                  
	         }
	         builder.append(delimiter);
	     }
	     return builder.toString();
	 }
	
	public static String[] joinMap(Map<String,String> s, String delimiter) {
	     StringBuilder keyBuilder = new StringBuilder();
	     StringBuilder valueBuilder = new StringBuilder();
	     Iterator iter = s.keySet().iterator();
	     while (iter.hasNext()) {
	    	 String key = iter.next().toString();
	    	 keyBuilder.append(key);
	    	 valueBuilder.append(s.get(key));
	         if (!iter.hasNext()) {
	           break;                  
	         }
	         keyBuilder.append(delimiter);
	         valueBuilder.append(delimiter);
	     }
	     return new String[]{ keyBuilder.toString() , valueBuilder.toString()};
	 }
}
