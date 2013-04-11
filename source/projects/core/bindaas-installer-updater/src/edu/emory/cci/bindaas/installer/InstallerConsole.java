package edu.emory.cci.bindaas.installer;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import edu.emory.cci.bindaas.framework.util.GSONUtil;
import edu.emory.cci.bindaas.installer.bundle.Activator;

public class InstallerConsole implements CommandProvider {
	private String bundleDirectory = "dependencies";

	public String getBundleDirectory() {
		return bundleDirectory;
	}

	public void setBundleDirectory(String bundleDirectory) {
		this.bundleDirectory = bundleDirectory;
	}
	
	
	public void _bindaas(CommandInterpreter ci)
	{
		String argument = ci.nextArgument();
		if(argument!=null && argument.equals("install"))
		{
			String configFile = ci.nextArgument();
			if(configFile!=null)
			{
				try {
					URL url = new URL(configFile) ;
					String jsonContent = url.getContent().toString();
					if(jsonContent!=null)
					{
						Configuration configuration = GSONUtil.getGSONInstance().fromJson(jsonContent, Configuration.class);
						if(configuration!=null && configuration.getRepositories()!=null)
						{
							BundleContext context = Activator.getContext();
							for(Repository repo : configuration.getRepositories())
							{
								String baseUrl = repo.getBaseUrl();
								for(String bundlePath : repo.getBundles())
								{
									String bundleUrl = baseUrl + "/" + bundlePath;
									context.installBundle(bundleUrl);
									ci.println("Bundle [" + bundleUrl + "] installed from repository[" + repo.getName() + "]");
								}
							}
						}
					}
				} catch (Exception e) {
						ci.printStackTrace(e);
				}
			}
			else
			{
				
				// discover and start bundles in bundleDirectory
				BundleContext bundleContext = Activator.getContext();
				File bundleDir = new File(bundleDirectory);
				ci.println("Installating bundles from [" + bundleDir.getAbsolutePath() + "] ");
				
				File[] discoveredBundles = bundleDir.listFiles(new FileFilter() {
					
					@Override
					public boolean accept(File file) {
						if(file.isFile() && file.getName().endsWith(".jar"))
							return true;
						else
						return false;
					}
				});
				
				if(discoveredBundles!=null)
				{
					List<Bundle> bundleList = new ArrayList<Bundle>();
					for(File bundleToInstall : discoveredBundles)
					{
						try{
						Bundle bundle = bundleContext.installBundle(bundleToInstall.getAbsolutePath(), new FileInputStream(bundleToInstall));
						bundleList.add(bundle);
						ci.println("Bundle installed [" + bundle + "]");
						
						}
						catch(Exception e)
						{
							ci.printStackTrace(e);
							ci.println("Bundle not installed " + bundleToInstall);
						}
					}
					
					
					// starting bundles
					
					for(Bundle bundle : bundleList)
					{
						try{
							bundle.start();
							ci.println("Bundle started [" + bundle + "]");
						}catch(Exception e)
						{
							ci.println("Bundle not started [" + bundle+  "] reason=[" + e + "]");
						}
					}
				}
				else
				{
					ci.println("No bundles discovered - nothing to install");
				}
				

			}
		}
	
	}
	@Override
	public String getHelp() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("\nbindaas install [configFile Url] : Install Bindaas\n");
		buffer.append("\nbindaas update : Update Bindaas\n");
		return buffer.toString();
	}

}
