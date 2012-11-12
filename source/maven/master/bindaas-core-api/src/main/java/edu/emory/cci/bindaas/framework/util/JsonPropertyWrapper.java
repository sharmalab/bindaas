package edu.emory.cci.bindaas.framework.util;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;



public class JsonPropertyWrapper {

	 private Map<String,String> props;

	 public JsonPropertyWrapper()
	 {
		 props = new HashMap<String, String>();
	 }
	public Map<String, String> getProps() {
		return props;
	}

	public void setProps(Map<String, String> props) {
		this.props = props;
	}
	
	
	public static class Deserializer  implements JsonDeserializer<JsonPropertyWrapper> {

		public JsonPropertyWrapper deserialize(JsonElement jsonElement, Type arg1,
				JsonDeserializationContext arg2) throws JsonParseException {
			JsonPropertyWrapper json = new JsonPropertyWrapper();
			JsonObject obj = jsonElement.getAsJsonObject();
			for(Entry<String,JsonElement> entry : obj.entrySet())
			{
				if(entry.getValue().isJsonPrimitive())
				json.getProps().put(entry.getKey(), entry.getValue().getAsString());
				else if(entry.getValue().isJsonArray())
					json.getProps().put(entry.getKey(), entry.getValue().getAsJsonArray().toString());
				else if(entry.getValue().isJsonObject())
					json.getProps().put(entry.getKey(), entry.getValue().getAsJsonObject().toString());
			}
			return json;
		}
		
	}
}
