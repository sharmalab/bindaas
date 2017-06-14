package edu.emory.cci.bindaas.framework.model;

import java.util.GregorianCalendar;

import com.google.gson.annotations.Expose;

import edu.emory.cci.bindaas.framework.util.GSONUtil;

/**
 * contains basic attributes all entities must have
 * @author nadir
 *
 */
public class Entity implements Cloneable{

	@Expose private String name;
	@Expose private String timeCreated; // TODO : change to DATE later
	@Expose private String createdBy;
	@Expose private String description;
	
	private static final String VALID_NAME_REGEX = "^[\\w\\-\\.]+$";
	
	
	public void validate() throws Exception
	{
		if(name == null || "".equals(name))
			throw new Exception("name attribute cannot be null");
		
		if( name.matches(VALID_NAME_REGEX) == false)
			throw new Exception("name contains illegal characters");
		if(description == null)
			description = "";
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public Entity()
	{
		timeCreated = GregorianCalendar.getInstance().getTime().toString();
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getTimeCreated() {
		return timeCreated;
	}
	public void setTimeCreated(String timeCreated) {
		this.timeCreated = timeCreated;
	}
	public String getCreatedBy() {
		return createdBy;
	}
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}
	
	public String toString()
	{
		return GSONUtil.getGSONInstance().toJson(this) ;
	}
	
	public boolean equals(Object obj)
	{
		if(obj instanceof Entity)
		{
			Entity objEntity = (Entity) obj;
			if(objEntity.getName().equals(name) && objEntity.getTimeCreated().equals(timeCreated) && objEntity.getCreatedBy().equals(createdBy))
				return true;
		}
		return false;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {

		return GSONUtil.getGSONInstance().fromJson(toString(), getClass());
	}
	
//	public void finalize()
//	{
//		System.out.println("-------------- GC [" + getClass() + "]--------------------");
//	}
	
}
