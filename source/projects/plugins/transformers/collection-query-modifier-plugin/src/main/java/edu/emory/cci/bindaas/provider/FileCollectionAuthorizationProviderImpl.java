package edu.emory.cci.bindaas.provider;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.osgi.framework.BundleContext;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;

import edu.emory.cci.bindaas.framework.util.GSONUtil;
import edu.emory.cci.bindaas.framework.util.IOUtils;

public class FileCollectionAuthorizationProviderImpl implements ICollectionAuthorizationProvider {

	private String fileResourcePath;
	private JsonParser parser = GSONUtil.getJsonParser();
	
	public static JsonObject getProviderDocumentation(BundleContext context , String filePath)
	{
		Enumeration<String> enumerationOfPaths = context.getBundle().getEntryPaths(filePath);
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

	
	public Map<String, Map<String, List<String>>> load2() {
		
		/*
		if(fileName!=null)
		{
			File file = new File(fileName); 
			
			if(file.exists() && file.canRead())
			{
				
				Map<String, Map<String, List<String>>> userQueryAuthMap = new HashMap<String, Map<String, List<String>>>();
				JsonObject jsonObject = null;
				try {
					jsonObject = parser.parse(new FileReader(fileName)).getAsJsonObject();
				} catch (JsonIOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JsonSyntaxException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				Set<Entry<String, JsonElement>> jsonEntrySet = jsonObject.entrySet();

				// iterator for all users
				Iterator<Entry<String, JsonElement>> userIterator = jsonEntrySet.iterator();
				
				while (userIterator.hasNext()) {
					Entry<String, JsonElement> userEntry = userIterator.next();
					String userId = userEntry.getKey();
					JsonObject userJsonObject = userEntry.getValue().getAsJsonObject();
					Set<Entry<String, JsonElement>> attributeEntrySet = userJsonObject.entrySet();
					Iterator<Entry<String, JsonElement>> attrubuteIterator = attributeEntrySet.iterator();
					Map<String, List<String>> attributeListMap = new HashMap<String, List<String>>();
					while(attrubuteIterator.hasNext())
					{
						Entry<String, JsonElement> entry = attrubuteIterator.next();
						String attributeName = entry.getKey();
						JsonElement jsonElement = entry.getValue();
						JsonArray jsonArray = jsonElement.getAsJsonArray();
						System.out.println(jsonElement.toString());
						List<String> jsonObjList = new Gson().fromJson(jsonArray, ArrayList.class);						
						attributeListMap.put(attributeName, jsonObjList);
						
					}
					userQueryAuthMap.put(userId, attributeListMap);
				}
				
				
				return userQueryAuthMap;
			}
			else
			{

				throw new IllegalArgumentException("Cannot load registry data source file [" + file.getAbsolutePath() + "]");
				
			}
			
		
		}
		else
		{

			throw new IllegalArgumentException("null registry data source file");
			
		}
		*/
		
		return null;
		
	}

	
	@Override
	public Map<String,Map<String, String>> load(BundleContext context) {
		
		URL url = context.getBundle().getEntry(fileResourcePath);
		
		Map<String,Map<String, String>> userCollectionGroupMap = new HashMap<String, Map<String, String>>();
		
		// store the group and collection mapping from the file
		Map<String, String> groupCollectionMap = new HashMap<String, String>();
		
		try {
			String content = toString(url.openStream());
			JsonObject jsonObject = null;
			try {
				jsonObject = parser.parse(content).getAsJsonObject();
			} catch (JsonIOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JsonSyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			
			JsonArray groupCollectionArray = jsonObject.get("groupCollection").getAsJsonArray();
			Iterator<JsonElement> groupCollectionIterator = groupCollectionArray.iterator();
			while (groupCollectionIterator.hasNext()) {
				JsonObject groupObject = groupCollectionIterator.next().getAsJsonObject();
				Set<Entry<String, JsonElement>> entrySet = groupObject.entrySet();
				Iterator<Entry<String, JsonElement>> groupIterator = entrySet.iterator();
				
				while (groupIterator.hasNext()) {
					Entry<String, JsonElement> groupEntry = groupIterator.next();
					String groupName = groupEntry.getKey();
					String collection = groupEntry.getValue().getAsString();
					groupCollectionMap.put(groupName, collection);
					
				}
				
			}

			JsonArray userGroups = jsonObject.get("userGroups").getAsJsonArray();
			Iterator<JsonElement> userGroupIterator = userGroups.iterator();
			while (userGroupIterator.hasNext()) {
				JsonObject userGroupObject = userGroupIterator.next().getAsJsonObject();
				Set<Entry<String, JsonElement>> entrySet = userGroupObject.entrySet();
				Iterator<Entry<String, JsonElement>> userIterator = entrySet.iterator();
				
				Map<String, String> userGroupMap = new HashMap<String, String>();
				while (userIterator.hasNext()) {
					Entry<String, JsonElement> userEntry = userIterator.next();
					String userName = userEntry.getKey();
					JsonArray jsonGroupArray = userEntry.getValue().getAsJsonArray();
					List<String> jsonObjList = new Gson().fromJson(jsonGroupArray, ArrayList.class);	
					for (String groupName : jsonObjList) {
						
						String collectionName = groupCollectionMap.get(groupName);
						userGroupMap.put(collectionName, groupName);
						
					}
					
					// add the user and the map of collection to group name since the query will
					// use the collection name
					userCollectionGroupMap.put(userName, userGroupMap);
				}
				
			}
			
//			Set<Entry<String, JsonElement>> jsonEntrySet = jsonObject.entrySet();
//			
//			// iterator for all users
//			Iterator<Entry<String, JsonElement>> userIterator = jsonEntrySet.iterator();
//			
//			while (userIterator.hasNext()) {
//				Entry<String, JsonElement> userEntry = userIterator.next();
//				String userId = userEntry.getKey();
//				JsonArray jsonArray = userEntry.getValue().getAsJsonArray();					
//				List<String> jsonObjList = new Gson().fromJson(jsonArray, ArrayList.class);						
//				attributeListMap.put(userId, jsonObjList);
//			}
						
			return userCollectionGroupMap;
			
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
				

		return userCollectionGroupMap;
		
	}


	public String getFileResourcePath() {
		return fileResourcePath;
	}


	public void setFileResourcePath(String fileResourcePath) {
		this.fileResourcePath = fileResourcePath;
	}
	
	private String toString(InputStream in) throws IOException
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
	
	
}
