package edu.emory.cci.bindaas.datasource.provider.aime4.model;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import edu.emory.cci.bindaas.datasource.provider.aime4.jaxb.AnnotationOfAnnotationCollection;

public class SimplyfiedAnnotationOfAnnotationCollection {

	private static Gson gson = new Gson();
	public  JsonObject toJSON (){
		 return gson.toJsonTree(this).getAsJsonObject();
	};
	
	public static SimplyfiedAnnotationOfAnnotationCollection convert(AnnotationOfAnnotationCollection annotationOfAnnotationCollection)
	{
		return null; // TODO : implement this
	}
}
