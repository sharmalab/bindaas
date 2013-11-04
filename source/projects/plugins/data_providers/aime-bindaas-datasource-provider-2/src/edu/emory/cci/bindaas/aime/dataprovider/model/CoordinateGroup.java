package edu.emory.cci.bindaas.aime.dataprovider.model;

import java.util.List;

import com.google.gson.annotations.Expose;

public class CoordinateGroup {
	@Expose private List<SpatialCoordinate> coordinates;
	@Expose private String textCallout;
	
	
	public String getTextCallout() {
		return textCallout;
	}

	public void setTextCallout(String textCallout) {
		this.textCallout = textCallout;
	}

	public List<SpatialCoordinate> getCoordinates() {
		return coordinates;
	}

	public void setCoordinates(List<SpatialCoordinate> coordinates) {
		this.coordinates = coordinates;
	}
	
	
}
