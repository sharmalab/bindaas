package edu.emory.cci.bindaas.commons.xml2json.test;

import java.io.FileNotFoundException;
import java.io.FileReader;

import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;

import edu.emory.cci.bindaas.commons.xml2json.GSONUtil;
import edu.emory.cci.bindaas.commons.xml2json.IJsonWriter;
import edu.emory.cci.bindaas.commons.xml2json.ImporterTool;
import edu.emory.cci.bindaas.commons.xml2json.model.ImportDescriptor;

public class ImportToolTest {

	public static void main(String[] args) throws Exception, JsonIOException, FileNotFoundException {
//		ImportDescriptor importDescriptor = GSONUtil.getGSONInstance().fromJson(new FileReader("src/test/resources/importDescriptor.json"), ImportDescriptor.class);
		ImportDescriptor importDescriptor = GSONUtil.getGSONInstance().fromJson(new FileReader("src/test/resources/importAIMEDescriptor.json"), ImportDescriptor.class);
		
		ImporterTool importTool = new ImporterTool();
		importTool.importFromXMLSource(importDescriptor , new IJsonWriter() {
			
			public void writeObject(JsonObject jsonObject) {
				
				System.out.println(jsonObject);
				
			}
		});
	}
}
