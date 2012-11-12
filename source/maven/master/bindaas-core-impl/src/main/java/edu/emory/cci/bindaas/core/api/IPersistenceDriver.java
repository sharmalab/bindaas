package edu.emory.cci.bindaas.core.api;

import java.io.IOException;
import java.util.List;

public interface IPersistenceDriver {
	
	public List<String> loadAllWorkspaces() throws IOException;
	public String loadWorkspaceByName(String name) throws IOException;
	public void createOrUpdateWorkspace(String name , String content) throws IOException;
	public void removeWorkspace(String name) throws IOException;

}
