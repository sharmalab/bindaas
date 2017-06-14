package edu.emory.cci.bindaas.framework.model;

public class ModifierException extends Exception{

	
	private static final long serialVersionUID = 7533907432696194881L;
	private String modifierId;
	
	public ModifierException(String modifierId) {

		this.modifierId = modifierId;
	}

	public ModifierException(String modifierId,String arg0, Throwable arg1) {
		super(arg0, arg1);
		this.modifierId = modifierId;
	}

	public ModifierException(String modifierId,String arg0) {
		super(arg0);
		this.modifierId = modifierId;
	}

	public ModifierException(String modifierId,Throwable arg0) {
		super(arg0);
		this.modifierId = modifierId;
	}

	public String getMessage()
	{
		return "modifierId=[" + modifierId + "] : " + super.getMessage();
	}
}
