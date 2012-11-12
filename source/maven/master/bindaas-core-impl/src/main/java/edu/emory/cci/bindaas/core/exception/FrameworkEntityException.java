package edu.emory.cci.bindaas.core.exception;

public abstract class FrameworkEntityException extends Exception{

	private static final long serialVersionUID = -4509057815809375314L;

	public static enum Type {
		Profile,Workspace,QueryEndpoint,DeleteEndpoint,SubmitEndpoint
	}

	public abstract String getMessage() ;
}
