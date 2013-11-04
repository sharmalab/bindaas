package edu.emory.cci.bindaas.aime.dataprovider.shape_handler;

import javax.xml.xpath.XPath;

import org.w3c.dom.Document;

import edu.emory.cci.bindaas.aime.dataprovider.model.MarkupInfo;

public interface IShapeHandler {

	public MarkupInfo extractMarkupInformation(Document doc, XPath xpath) throws Exception;
}
