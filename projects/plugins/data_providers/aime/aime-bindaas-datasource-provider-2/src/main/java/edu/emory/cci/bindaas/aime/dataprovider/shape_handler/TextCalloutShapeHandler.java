package edu.emory.cci.bindaas.aime.dataprovider.shape_handler;

import java.util.ArrayList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import edu.emory.cci.bindaas.aime.dataprovider.model.CoordinateGroup;
import edu.emory.cci.bindaas.aime.dataprovider.model.MarkupInfo;

/**
 *  applies to text callouts
 * @author nadir
 *
 */

public class TextCalloutShapeHandler implements IShapeHandler{

	private String xpathExpression = "/ns1:ImageAnnotation/ns1:textAnnotationCollection/ns1:TextAnnotation";
	
	@Override
	public MarkupInfo extractMarkupInformation(Document doc, XPath xpath)
			throws Exception {
		MarkupInfo markupInfo = new MarkupInfo();
		markupInfo.setCoordinateGroup(new ArrayList<CoordinateGroup>());
		
		
		XPathExpression expression = xpath.compile(xpathExpression);
		Object result = expression.evaluate(doc, XPathConstants.NODESET);
		NodeList nodes = (NodeList) result;

		if (nodes.getLength() > 0) {
			
			for(int i = 0  ; i < nodes.getLength() ; i ++ )
			{
				Element element = (Element) nodes.item(i);
				String textCallout = element.getAttribute("text");
				CoordinateGroup coordinateGroup = new CoordinateGroup();
				coordinateGroup.setTextCallout(textCallout);
				markupInfo.getCoordinateGroup().add(coordinateGroup);
			}
			
			markupInfo.setCount(markupInfo.getCoordinateGroup().size());
			
		} 
		else
			{
				markupInfo.setCount(0);
			}
		return markupInfo;
	}

}
