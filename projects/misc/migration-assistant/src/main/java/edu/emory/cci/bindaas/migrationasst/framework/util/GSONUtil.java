package edu.emory.cci.bindaas.migrationasst.framework.util;

import java.lang.reflect.Modifier;
import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class GSONUtil {

	private static Gson gson;
	
	
	private GSONUtil()
	{
		// do all the initialization here
		

	}
	public static Gson getGSONInstance()
	{
		if(gson == null)
		{
			GsonBuilder builder = new GsonBuilder();
			builder.excludeFieldsWithModifiers(Modifier.TRANSIENT, Modifier.VOLATILE);
			builder.registerTypeAdapter(Class.class, new ClassSerializer());
			builder.registerTypeAdapter(Class.class, new ClassDeserializer());
			builder.registerTypeAdapter(JsonObject.class, new JsonObjectSerializer());
			builder.registerTypeAdapter(JsonObject.class, new JsonObjectDeserializer());
			builder.registerTypeAdapter(JsonPropertyWrapper.class, new JsonPropertyWrapper.Deserializer());
			gson = builder.excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create();
			
		}
		return gson;
	}
	
	private static class ClassSerializer implements JsonSerializer<Class> {

		public JsonElement serialize(Class clazz, Type arg1,
				JsonSerializationContext context) {
			
			return new JsonPrimitive(clazz.getName());
		}
		
	}
	
	private static class ClassDeserializer implements JsonDeserializer<Class> {

		public Class deserialize(JsonElement json, Type arg1,
				JsonDeserializationContext arg2) throws JsonParseException {
			String val = json.getAsString();
			try {
				return Class.forName(val);
			} catch (ClassNotFoundException e) {
				
				e.printStackTrace();
				return null;
			}
			
		}
		
	}
}
