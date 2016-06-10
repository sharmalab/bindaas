package edu.emory.cci.bindaas.native_app_runner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.io.Files;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.annotations.Expose;

import edu.emory.cci.bindaas.framework.api.ISubmitPayloadModifier;
import edu.emory.cci.bindaas.framework.model.ModifierException;
import edu.emory.cci.bindaas.framework.model.RequestContext;
import edu.emory.cci.bindaas.framework.model.SubmitEndpoint;
import edu.emory.cci.bindaas.framework.provider.exception.AbstractHttpCodeException;
import edu.emory.cci.bindaas.framework.provider.exception.MethodNotImplementedException;
import edu.emory.cci.bindaas.framework.provider.exception.ModifierExecutionFailedException;
import edu.emory.cci.bindaas.framework.util.GSONUtil;
import edu.emory.cci.bindaas.framework.util.IOUtils;

public class NativeAppRunnerSPM implements ISubmitPayloadModifier{

	private Log log = LogFactory.getLog(getClass());
	private ExecutorService executorService ;
	
	
	public void init()
	{
		executorService = Executors.newCachedThreadPool();
	}
	
	@Override
	public JsonObject getDocumentation() {

		return new JsonObject();
	}

	@Override
	public void validate() throws ModifierException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getDescriptiveName() {

		return "NativeAppRunnerSPM";
	}

	@Override
	public InputStream transformPayload(InputStream data,
			SubmitEndpoint submitEndpoint, JsonObject modifierProperties,
			RequestContext requestContext) throws AbstractHttpCodeException {
		try {
				final NativeProcessDescriptor descriptor = GSONUtil.getGSONInstance().fromJson(modifierProperties, NativeProcessDescriptor.class);
				File tempDir = Files.createTempDir();
				File inputFile = new File(tempDir,"input-file");
				FileOutputStream fos = new FileOutputStream(inputFile);
				IOUtils.copyAndCloseInput(data, fos);
				fos.close();
				
				if(requestContext!=null)
				{
					Map<String,Object> attr = requestContext.getAttributes();
					if(attr == null)
					{
						attr = new HashMap<String, Object>();
						requestContext.setAttributes(attr);
					}
					attr.put("source", inputFile.getAbsolutePath());
				}
				
				File outputFile = new File(tempDir, "output-file");
				final String command = descriptor.commandPattern.replace("$input", inputFile.getAbsolutePath()).replace("$output", outputFile.getAbsolutePath());
				
				final String[] commandList = command.split(" ");
				
				Callable<Integer> callable = new Callable<Integer>() {
					
					@Override
					public Integer call() throws Exception {
						ProcessBuilder processBuilder = new ProcessBuilder(commandList);
						if(descriptor.environmentVariables!=null)
						{
							Map<String,String> env = processBuilder.environment();
							env.putAll(descriptor.environmentVariables);
						}
						if(descriptor.workingDirectory!=null)
						{
							processBuilder.directory(new File(descriptor.workingDirectory));
						}
						log.debug(String.format("Executing command:\n%s $ %s", descriptor.workingDirectory == null ? "" : descriptor.workingDirectory ,command));
						Process process = processBuilder.start();
						int exitCode = process.waitFor();
						return exitCode;
					}
				};
				
				 
				Future<Integer> future = executorService.submit(callable);
				Integer exitCode = future.get(100, TimeUnit.SECONDS);
				if(exitCode == 0)
				{
					if(outputFile.exists())
					{
						File zipFile = new File(tempDir , "output-file.zip");
						ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile));
						ZipEntry zipEntry = new ZipEntry(outputFile.getName());
						zos.putNextEntry(zipEntry);
						IOUtils.copy(new FileInputStream(outputFile), zos, 2048);
						zos.closeEntry();
						zos.close();
						
						submitEndpoint.getProperties().add("inputType", new JsonPrimitive("ZIP"));
						
						return new FileInputStream(zipFile);
					}
				}
				else throw new Exception("Native Process returned with exit code [" + exitCode + "]");
				
		}
		catch(Exception e)
		{
			log.error(e);
			throw new ModifierExecutionFailedException(getClass().getName() , 1 , e);
		}
		
				
		return null;
	}

	@Override
	public String transformPayload(String data, SubmitEndpoint submitEndpoint,
			JsonObject modifierProperties, RequestContext requestContext)
			throws AbstractHttpCodeException {
		throw new MethodNotImplementedException(getClass().getName(), 1);
	}
	
	private static class NativeProcessDescriptor {
		@Expose private String commandPattern; // use $input to represent input filename $output for output
		@Expose private Map<String,String> environmentVariables;
		@Expose private String workingDirectory;
	}

}
