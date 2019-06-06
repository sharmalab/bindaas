package edu.emory.cci.bindaas.trusted_app_client.util;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

import com.google.common.base.CharMatcher;
import com.google.common.base.Joiner;
import com.google.gson.Gson;

import com.google.gson.JsonObject;
import edu.emory.cci.bindaas.trusted_app_client.core.APIKey;
import edu.emory.cci.bindaas.trusted_app_client.core.ITrustedAppClient;
import edu.emory.cci.bindaas.trusted_app_client.core.TrustedAppClientImpl;

/**
 * Read accounts information from a CSV
 * 
 * CSV should have following format :
 * 
 * username|email|expires|role
 * 
 * @author nadir
 * 
 */
public class SpreadsheetImporter {

	private String filename;
	private File file;
	private ITrustedAppClient trustedAppClient;
	private String applicationID;
	private String applicationSecretKey;
	private String baseUrl;
	private SimpleDateFormat dateFormat;
	private String reportFile;
	private Gson gson;

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getApplicationID() {
		return applicationID;
	}

	public void setApplicationID(String applicationID) {
		this.applicationID = applicationID;
	}

	public String getApplicationSecretKey() {
		return applicationSecretKey;
	}

	public void setApplicationSecretKey(String applicationSecretKey) {
		this.applicationSecretKey = applicationSecretKey;
	}

	public String getBaseUrl() {
		return baseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public String getReportFile() {
		return reportFile;
	}

	public void setReportFile(String reportFile) {
		this.reportFile = reportFile;
	}

	public void init() throws Exception {
		file = new File(filename);
		if (file.isFile() && file.canRead()) {
			this.trustedAppClient = new TrustedAppClientImpl(baseUrl,
					applicationID, applicationSecretKey);
			dateFormat = new SimpleDateFormat("MM/dd/yyyy");
			gson = new Gson();
		} else
			throw new Exception("Cannot open file for reading [" + filename
					+ "]");

	}

	
	public void start() throws Exception {
		CSVWriter reportWriter = new CSVWriter(new FileWriter(reportFile));
		reportWriter.writeNext(new String[] { "username", "email", "expires",
				"role", "server-response" });
		CSVReader reader = new CSVReader(new FileReader(file));
		String[] nextLine;

		while ((nextLine = reader.readNext()) != null) {
			try {
				if (nextLine.length >= 4) {
					String username = CharMatcher.ASCII.retainFrom(nextLine[0]
							.trim());
					String email = CharMatcher.ASCII.retainFrom(nextLine[1]
							.trim());
					String expires = CharMatcher.ASCII.retainFrom(nextLine[2]
							.trim());
					String role = CharMatcher.ASCII.retainFrom(nextLine[3]
							.trim());
					String comments = String.format(
							"{ 'username' : '%s' , 'role' : '%s' }", username,
							role);
					Date dateExpires = dateFormat.parse(expires);
					// FIXME fix protocol
					try {
						JsonObject serverResponse = trustedAppClient.authorizeNewUser("api_key",
								email, dateExpires.getTime(), comments);

						System.out.println(username + "|\t" + email + "|\t"
								+ serverResponse);
						reportWriter.writeNext(new String[] { username, email,
								expires, role, serverResponse.toString() });
					} catch (Exception e) {
						e.printStackTrace();
						reportWriter.writeNext(new String[] { username, email,
								expires, role, String.format("{ 'error' : 'duplicate entry' , 'details' : '%s' }", e.getMessage()) });
					} 

				} else {
					System.out.println("Cannot write entry "
							+ Joiner.on(",").join(nextLine));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		reader.close();
		reportWriter.close();
	}

	public static void main(String[] args) throws Exception {
		SpreadsheetImporter importer = new SpreadsheetImporter();
		importer.setApplicationID("");
		importer.setApplicationSecretKey("");
		importer.setBaseUrl("");
		importer.setFilename("");
		importer.setReportFile("");
		importer.init();
		importer.start();
	}

}
