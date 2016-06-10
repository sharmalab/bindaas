package edu.emory.cci.bindaas.json2csv;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import au.com.bytecode.opencsv.CSVWriter;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;

public class Json2CSV {

	private String mongoHost = "127.0.0.1";
	private int mongoPort = 27017;
	private String mongoDB;
	private String mongoCollection;
	private String outputFile = "output.csv";

	public void convert() {
		Mongo mongo = null;
		try {
			mongo = new Mongo(mongoHost,mongoPort);
			DB db = mongo.getDB(mongoDB);
			DBCollection collection = db.getCollection(mongoCollection);
			System.out.println("Querying collection");
			DBCursor cursor = collection.find();
			System.out.println("Found [" + cursor.count() + "] objects");
			List<Map<String, String>> listOfValues = new ArrayList<Map<String, String>>();
			Set<String> uniqueColumns = new HashSet<String>();

			while (cursor.hasNext()) {
				DBObject dbObject = cursor.next();
				Map<String, String> map = new HashMap<String, String>();
				for (String key : dbObject.keySet()) {
					map.put(key, dbObject.get(key).toString());
					uniqueColumns.add(key);
				}
				listOfValues.add(map);
			}

			System.out.println("Writing to file [" + outputFile + "]");
			// construct CSV
			FileWriter sw = new FileWriter(outputFile);
			CSVWriter writer = new CSVWriter(sw, ',',
					CSVWriter.NO_QUOTE_CHARACTER);

			String[] columns = uniqueColumns.toArray(new String[uniqueColumns
					.size()]);

			writer.writeNext(columns);
			for (Map<String, String> row : listOfValues) {
				String[] csvVals = new String[columns.length];
				for (int index = 0; index < columns.length; index++) {
					csvVals[index] = row.get(columns[index]);
				}
				writer.writeNext(csvVals);
			}

			writer.close();
			System.out.println("Export Completed");

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (mongo != null) {
				mongo.close();
			}
		}
	}

	public static void usage()
	{
		System.err.println("java [ -Dhost=<host> -Dport<port> -Doutput=<output_file> ] -Ddb=<mongodb> -Dcollection=<mongoCollection>  -jar [jar]");
		System.exit(-1);
	}
	
	public static void main(String[] args) throws Exception{
		Json2CSV json2csv = new Json2CSV();
		json2csv.mongoHost = System.getProperty("host") == null ? json2csv.mongoHost : System.getProperty("host");
		json2csv.mongoPort = System.getProperty("port") == null ? json2csv.mongoPort : Integer.parseInt(System.getProperty("port"));
		json2csv.outputFile = System.getProperty("output") == null ? json2csv.outputFile : System.getProperty("output");
		if(System.getProperty("db") == null ) usage();
		if(System.getProperty("collection") == null ) usage();
		
		json2csv.mongoDB = System.getProperty("db");
		json2csv.mongoCollection = System.getProperty("collection");
		
		json2csv.convert();
	}
}
