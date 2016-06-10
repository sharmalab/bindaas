package edu.emory.cci.bindaas.aime.dataprovider.model;

import edu.emory.cci.bindaas.aime.dataprovider.shape_handler.EllipticalShapeHandler;
import edu.emory.cci.bindaas.aime.dataprovider.shape_handler.IShapeHandler;
import edu.emory.cci.bindaas.aime.dataprovider.shape_handler.MultiPointShapeHandler;
import edu.emory.cci.bindaas.aime.dataprovider.shape_handler.PolyLineShapeHandler;
import edu.emory.cci.bindaas.aime.dataprovider.shape_handler.TextCalloutShapeHandler;

public enum Shape {

	ELLIPTICAL(new EllipticalShapeHandler()),POLY_LINE(new PolyLineShapeHandler()),MULTI_POINT(new MultiPointShapeHandler()),TEXT_CALLOUT(new TextCalloutShapeHandler()) ; 
	
	private Shape(IShapeHandler shapeHandler)
	{
		this.shapeHandler = shapeHandler;
	}
	private IShapeHandler shapeHandler;
	
	public IShapeHandler getShapeHandler()
	{
		return shapeHandler;
	}
	
}
