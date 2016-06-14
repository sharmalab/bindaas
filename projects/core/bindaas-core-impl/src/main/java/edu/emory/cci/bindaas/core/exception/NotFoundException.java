package edu.emory.cci.bindaas.core.exception;

public class NotFoundException extends FrameworkEntityException{
	
	private static final long serialVersionUID = 1L;
	private String name;
	private Type type;
	
	public NotFoundException(String name , Type type)
	{
		this.name = name;
		this.type = type;
	}
	
	@Override
	public String getMessage() {
		
		return "Cannot find " + type + "[" + name + "]";
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
