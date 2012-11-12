package edu.emory.cci.bindaas.framework.api;

import java.io.InputStream;

import edu.emory.cci.bindaas.framework.model.SubmitEndpoint;

public abstract class SubmitInterceptor {

	
	public abstract InputStream transformMessage( InputStream data , SubmitEndpoint submitEndpoint) throws Exception;
	
	public void interceptMessage(InputStream data , SubmitInterceptorChain chain ,  SubmitEndpoint submitEndpoint) throws Exception
	{
		InputStream is = transformMessage(data, submitEndpoint);
		SubmitInterceptor nextInterceptor = chain.next();
		if(nextInterceptor == null )
		{
			ISubmitHandler handler  = chain.getSubmitHandler();
//			handler.submit(submitEndpoint., endpointProperties, is)
		}
		
	}
	
}
