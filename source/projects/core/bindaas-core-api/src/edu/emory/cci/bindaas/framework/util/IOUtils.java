package edu.emory.cci.bindaas.framework.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

public class IOUtils {

	public static String toString(InputStream in) throws IOException
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024*100];
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
	     Iterator<?> iter = s.iterator();
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
	     Iterator<?> iter = s.keySet().iterator();
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

	public static void copyAndCloseInput(InputStream is,
			OutputStream outputStream) throws IOException {
		copy(is,outputStream , 2048);
		outputStream.close();
		is.close();
	}
	
	 public static int copy(final InputStream input, final OutputStream output,
	            int bufferSize) throws IOException {
	        int avail = input.available();
	        if (avail > 262144) {
	            avail = 262144;
	        }
	        if (avail > bufferSize) {
	            bufferSize = avail;
	        }
	        final byte[] buffer = new byte[bufferSize];
	        int n = 0;
	        n = input.read(buffer);
	        int total = 0;
	        while (-1 != n) {
	            if (n == 0) {
	                throw new IOException("0 bytes read in violation of InputStream.read(byte[])");
	            }
	            output.write(buffer, 0, n);
	            total += n;
	            n = input.read(buffer);
	        }
	        return total;
	    }
}
