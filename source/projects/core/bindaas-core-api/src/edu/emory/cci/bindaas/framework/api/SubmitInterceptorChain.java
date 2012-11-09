package edu.emory.cci.bindaas.framework.api;

public class SubmitInterceptorChain {

	private ISubmitHandler handler;
	public SubmitInterceptor next(){ return null ;}
	
	public ISubmitHandler getSubmitHandler()
	{
		return handler;
	}
	
	public void setSubmitHandler(ISubmitHandler handler)
	{
		this.handler = handler;
	}
}
