package edu.emory.cci.bindaas.aime.dataprovider.shape_handler;

import java.util.ArrayList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import edu.emory.cci.bindaas.aime.dataprovider.model.CoordinateGroup;
import edu.emory.cci.bindaas.aime.dataprovider.model.MarkupInfo;
import edu.emory.cci.bindaas.aime.dataprovider.model.SpatialCoordinate;

/**
 * 
 * @author nadir
 *
 */

public class EllipticalShapeHandler implements IShapeHandler{

	private String xpathExpression = "/ns1:ImageAnnotation/ns1:geometricShapeCollection/ns1:GeometricShape[@xsi:type='Ellipse']";
	private Log log= LogFactory.getLog(getClass());
	
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
				NodeList spatialCoordinateNodeList = element.getElementsByTagName("SpatialCoordinate");
				CoordinateGroup coordinateGroup = new CoordinateGroup();
				coordinateGroup.setCoordinates(new ArrayList<SpatialCoordinate>());
				
				for(int j = 0; j < spatialCoordinateNodeList.getLength();j++)
					{
						Element spatialCoordinateElement = (Element) spatialCoordinateNodeList.item(j);
						String index = spatialCoordinateElement.getAttribute("coordinateIndex");
						String x = spatialCoordinateElement.getAttribute("x");
						String y = spatialCoordinateElement.getAttribute("y");
						
						try{
							 SpatialCoordinate spatialCoordinate = new SpatialCoordinate();
							 spatialCoordinate.setIndex(Integer.parseInt(index));
							 spatialCoordinate.setX(Double.parseDouble(x));
							 spatialCoordinate.setY(Double.parseDouble(y));
							 
							 coordinateGroup.getCoordinates().add(spatialCoordinate);
						}catch(NumberFormatException numE)
						{
							log.warn("SpatialCoordinate could not be parsed" , numE);
							break;
						}
					}
				
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
