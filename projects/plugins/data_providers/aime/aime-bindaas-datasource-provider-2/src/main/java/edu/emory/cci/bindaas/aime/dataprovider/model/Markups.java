package edu.emory.cci.bindaas.aime.dataprovider.model;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.annotations.Expose;

import edu.emory.cci.bindaas.framework.util.GSONUtil;

public class Markups {

	@Expose private Map<Shape,MarkupInfo> markups;

	public Markups()
	{
		markups = new HashMap<Shape, MarkupInfo>();
	}
	
	public Map<Shape, MarkupInfo> getMarkups() {
		return markups;
	}

	public void setMarkups(Map<Shape, MarkupInfo> markups) {
		this.markups = markups;
	}
	
	
	public String toString()
	{
		return GSONUtil.getGSONInstance().toJson(this);
	}
}
