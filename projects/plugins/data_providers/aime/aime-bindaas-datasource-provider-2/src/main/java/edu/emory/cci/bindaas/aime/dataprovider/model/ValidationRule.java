package edu.emory.cci.bindaas.aime.dataprovider.model;

import javax.xml.xpath.XPath;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;

import com.google.gson.annotations.Expose;

import edu.emory.cci.bindaas.aime.dataprovider.exception.MarkupValidationException;
import edu.emory.cci.bindaas.aime.dataprovider.shape_handler.IShapeHandler;

public class ValidationRule {

	@Expose private Shape shape;
	@Expose private Integer max;
	@Expose private Integer min;
	@Expose private Boolean enforce;

	private Log log = LogFactory.getLog(getClass());
	public Shape getShape() {
		return shape;
	}
	public void setShape(Shape shape) {
		this.shape = shape;
	}
	public Integer getMax() {
		return max;
	}
	public void setMax(Integer max) {
		this.max = max;
	}
	public Integer getMin() {
		return min;
	}
	public void setMin(Integer min) {
		this.min = min;
	}
	
	
	public MarkupInfo validateAndExtractMarkupInfo(Document doc, XPath xpath) throws MarkupValidationException
	{
		IShapeHandler shapeHandler = shape.getShapeHandler();
		MarkupInfo markupInfo;
		try {
		
			markupInfo = shapeHandler.extractMarkupInformation(doc, xpath);
			Integer min = this.min == null ? 0 : this.min;
			Integer max = this.max == null ? Integer.MAX_VALUE : this.max;
			
			if(this.enforce && (markupInfo.getCount() < min || markupInfo.getCount() > max  ))
			{
				throw new MarkupValidationException("Markup Validation Failed. Shape [" + shape + "] Expected Min/Max [" + min + "/" + max + "] Found [" + markupInfo.getCount() + "]" );
			}
			
			return markupInfo;
			
		} 
		catch(MarkupValidationException e)
		{
			throw e;
		}
		catch (Exception e) {
			log.error(e);
			throw new MarkupValidationException("Markup Validation Failed due to processing error",e);
		}
		
	}
	
}
