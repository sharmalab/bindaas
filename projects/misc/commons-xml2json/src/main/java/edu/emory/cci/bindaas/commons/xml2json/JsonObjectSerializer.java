package edu.emory.cci.bindaas.commons.xml2json;

import java.lang.reflect.Type;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class JsonObjectSerializer implements JsonSerializer<JsonObject> {

	
	public JsonElement serialize(JsonObject src, Type typeOfSrc,
			JsonSerializationContext context) {
		
		return src;
	}
	


}
