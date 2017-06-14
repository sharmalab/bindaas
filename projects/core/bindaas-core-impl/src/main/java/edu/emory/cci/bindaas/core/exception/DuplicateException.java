package edu.emory.cci.bindaas.core.exception;


public class DuplicateException extends FrameworkEntityException {

	
	private static final long serialVersionUID = 1L;
	private Type type;
	private String name;
	
	public DuplicateException(String name , Type type)
	{
		this.name = name;
		this.type = type;
	}
	@Override
	public String getMessage() {
		
		return   type + "[" + name + "] already exist";
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}
	

}
