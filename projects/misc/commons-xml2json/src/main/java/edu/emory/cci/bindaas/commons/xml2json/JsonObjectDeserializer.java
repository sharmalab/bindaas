package edu.emory.cci.bindaas.commons.xml2json;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class JsonObjectDeserializer implements JsonDeserializer<JsonObject> {

	
	public JsonObject deserialize(JsonElement json, Type typeOfT,
			JsonDeserializationContext context) throws JsonParseException {
		
		return json.getAsJsonObject();
	}

}
