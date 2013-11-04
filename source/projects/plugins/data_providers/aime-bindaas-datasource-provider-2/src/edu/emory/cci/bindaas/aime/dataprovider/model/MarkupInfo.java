package edu.emory.cci.bindaas.aime.dataprovider.model;

import java.util.List;

import com.google.gson.annotations.Expose;
// corresponds to a particular Shape
public class MarkupInfo {

	@Expose private Integer count;
	@Expose private List<CoordinateGroup> coordinateGroup;
	
	public Integer getCount() {
		return count;
	}
	public void setCount(Integer count) {
		this.count = count;
	}
	public List<CoordinateGroup> getCoordinateGroup() {
		return coordinateGroup;
	}
	public void setCoordinateGroup(List<CoordinateGroup> coordinateGroup) {
		this.coordinateGroup = coordinateGroup;
	}
	
	
	
}
