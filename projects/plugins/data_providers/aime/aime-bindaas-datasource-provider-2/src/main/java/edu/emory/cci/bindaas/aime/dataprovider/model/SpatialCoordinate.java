package edu.emory.cci.bindaas.aime.dataprovider.model;

import com.google.gson.annotations.Expose;

public class SpatialCoordinate {

	@Expose private Double x;
	@Expose private Double y;
	@Expose private Integer index;
	
	public Double getX() {
		return x;
	}
	public void setX(Double x) {
		this.x = x;
	}
	public Double getY() {
		return y;
	}
	public void setY(Double y) {
		this.y = y;
	}
	public Integer getIndex() {
		return index;
	}
	public void setIndex(Integer index) {
		this.index = index;
	}
	
	
}
