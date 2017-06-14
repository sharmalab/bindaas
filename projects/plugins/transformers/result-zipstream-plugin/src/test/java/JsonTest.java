import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;




public class JsonTest {

	public static void main(String[] args) {
		String json = "[{\"id\":1,\"filename\": \"data.txt\",\"location\":\"/Desktop/data.txt\"},{\"id\":2,\"filename\":\"data copy.txt\",\"location\":\"data copy.txt\"}]";
		 JsonElement jelement = new JsonParser().parse(json);
		 JsonArray  jarray = jelement.getAsJsonArray();
		 for (int i = 0; i < jarray.size(); i++) {
			 JsonObject object = jarray.get(i).getAsJsonObject();
			 System.out.println(object.get("location"));
		 }
		 
		 
	}
}
