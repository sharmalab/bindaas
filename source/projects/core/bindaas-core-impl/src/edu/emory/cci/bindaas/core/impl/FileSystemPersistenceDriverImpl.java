package edu.emory.cci.bindaas.core.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.helpers.IOUtils;

import edu.emory.cci.bindaas.core.api.IPersistenceDriver;

public class FileSystemPersistenceDriverImpl implements IPersistenceDriver {

	private String metadataStore;
	private File metadataStoreDirectory;
	private Log log = LogFactory.getLog(getClass());
	
	public void init() throws Exception
	{
		if(metadataStore!=null)
		{
			metadataStoreDirectory = new File(metadataStore);
			if(!metadataStoreDirectory.isDirectory())
			{
				log.info("Metadata Store does not exist. Creating a new one");
				metadataStoreDirectory.mkdirs();
			}
		}
		else
		{
			throw new Exception("Metadata Store Directory not set");
		}
		
	}
	@Override
	public List<String> loadAllWorkspaces() throws IOException {
		List<String> workspaceContents = new ArrayList<String>();
		
		File[] listOfWorkspaces = metadataStoreDirectory.listFiles(
				
				new FilenameFilter() {
					
					@Override
					public boolean accept(File arg0, String filename) {
						if(filename.endsWith(".workspace"))
							return true;
						else
							return false;
					}
				}
				);
		
		for(File workspaceFile : listOfWorkspaces)
		{
			String content = readContent(workspaceFile);
			workspaceContents.add(content);
		}
		
		return workspaceContents;
	}

	@Override
	public String loadWorkspaceByName(String name) throws IOException {
		File workspaceFile = new File(metadataStoreDirectory.getAbsolutePath() +"/"+ name + ".workspace");
		if(workspaceFile.isFile() && workspaceFile.canRead())
		{
			return readContent(workspaceFile);
		}
		else throw new IOException("Workspace metadata not found. name=[" + name + "]");
		
	}

	@Override
	public void createOrUpdateWorkspace(String name, String content)
			throws IOException {
		File workspaceFile = new File(metadataStoreDirectory.getAbsolutePath() +"/"+ name + ".workspace");
		writeToFile(workspaceFile, content);
		
	}

	@Override
	public synchronized void removeWorkspace(String name) throws IOException {
		File workspaceFile = new File(metadataStoreDirectory.getAbsolutePath() +"/"+ name + ".workspace");
		workspaceFile.delete();
		
	}
	
	public String getMetadataStore() {
		return metadataStore;
	}
	public void setMetadataStore(String metadataStore) {
		this.metadataStore = metadataStore;
	}
	private String readContent(File file) throws IOException
	{
		StringWriter sw = new StringWriter();
		IOUtils.copy(new FileReader(file), sw , 2048);
		return sw.toString();
		
	}
	private synchronized void writeToFile(File file,String content) throws IOException
	{
		FileOutputStream fos = new FileOutputStream(file);
		ByteArrayInputStream bis = new ByteArrayInputStream(content.getBytes());
		IOUtils.copyAndCloseInput(bis, fos);
	}

}
