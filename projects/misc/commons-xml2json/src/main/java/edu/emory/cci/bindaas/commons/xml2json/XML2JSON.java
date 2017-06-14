package edu.emory.cci.bindaas.commons.xml2json;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import edu.emory.cci.bindaas.commons.xml2json.model.Mapping;
import edu.emory.cci.bindaas.commons.xml2json.model.Type;

public class XML2JSON {
	
	private  XPath xpath;
	private  Map<String,String> prefixes = new HashMap<String, String>();
	private  boolean namespaceAware = false;
	private  String rootElementSelector;
	private List<Mapping> mappings ;
	
	public List<Mapping> getMappings() {
		return mappings;
	}

	public void setMappings(List<Mapping> mappings) {
		this.mappings = mappings;
	}

	public String getRootElementSelector() {
		return rootElementSelector;
	}

	public void setRootElementSelector(String rootElementSelector) {
		this.rootElementSelector = rootElementSelector;
	}

	public void init() throws Exception
	{
		XPathFactory factory = XPathFactory.newInstance();
		xpath = factory.newXPath();
		xpath.setNamespaceContext(new NamespaceContext() {

			public Iterator getPrefixes(String namespaceURI) {
				return null;
			}

			public String getPrefix(String namespaceURI) {
				return null;
			}

			public String getNamespaceURI(String prefix) {
				
				if (prefix == null)
					throw new NullPointerException("Null prefix");
				else if ("xml".equals(prefix))
					return XMLConstants.XML_NS_URI;
				else
				{
					String namespaceURI = prefixes.get(prefix);
					if(namespaceURI == null)
						return XMLConstants.NULL_NS_URI;
					return namespaceURI;
				}
				
			}
		});

	}
	
	public List<JsonObject> parseXML(InputStream is) throws Exception
	{
		DocumentBuilderFactory domFactory = DocumentBuilderFactory
				.newInstance();
		domFactory.setNamespaceAware(namespaceAware); // never forget this!
		DocumentBuilder builder = domFactory.newDocumentBuilder();
		Document doc = builder.parse( is );
		
		if(rootElementSelector!=null)
		{
			XPathExpression expression = xpath.compile(rootElementSelector);
			Object result = expression.evaluate(doc, XPathConstants.NODESET);
			NodeList nodes = (NodeList) result;
			List<JsonObject> listOfJsonObjects = new ArrayList<JsonObject>();
			for(int i=0; i<nodes.getLength(); i++)
			{
				Node node = nodes.item(i);
				Document newDoc = builder.newDocument();
				newDoc.appendChild(newDoc.adoptNode(node.cloneNode(true)));
//				toString(newDoc);
//				toString(node);
				JsonObject jsonObj = parseXML(newDoc, mappings);
				listOfJsonObjects.add(jsonObj);
			}
			
			return listOfJsonObjects;
		}
		else
		{
			return Arrays.asList( new JsonObject[]{parseXML(doc, mappings)});
		}
		
		
		
		
		
	}
	public  JsonObject parseXML(Document document , List<Mapping> mappings) throws Exception
	{
		JsonObject parent = new JsonObject();
		
		for(Mapping mapping : mappings)
		{
			assertNotNull(mapping.getName(), "[name] field");
			
			if(mapping.getType().equals(Type.SIMPLE))
			{
				assertNotNull(mapping.getXpath(), "[xpath] field");
				String value = parseExpression(mapping.getXpath(), document);
				if(value!=null)
				parent.add(mapping.getName(), new JsonPrimitive(value));
			}
			else if (mapping.getType().equals(Type.ARRAY))
			{
				assertNotNull(mapping.getXpath(), "[xpath] field");
				List<String> setOfValues = parseExpressionArray(mapping.getXpath() , document);
				JsonArray array = new JsonArray();
				for(String val : setOfValues)
				{
					array.add(new JsonPrimitive(val));
				}
				
				parent.add(mapping.getName(), array);
			}
			else
			{
				assertNotNull(mapping.getNestedFields(), "[nestedFields] field");
				JsonObject jsonObject = parseXML(document, mapping.getNestedFields());
				parent.add(mapping.getName(), jsonObject);
			}
		}
		return parent;
	}
	
	
	public  String parseExpression(String xpathExpr , Document document) throws Exception
	{
		XPathExpression expression = xpath.compile(xpathExpr);
		Object result = expression.evaluate(document, XPathConstants.NODESET);
		NodeList nodes = (NodeList) result;

		if (nodes.item(0) != null && nodes.item(0).getNodeValue() != null) {
			String value = nodes.item(0).getNodeValue();
			return value;	
		}
		return null;
	}
	
	public  List<String> parseExpressionArray(String xpathExpr , Document document) throws Exception
	{
		XPathExpression expression = xpath.compile(xpathExpr);
		Object result = expression.evaluate(document, XPathConstants.NODESET);
		NodeList nodes = (NodeList) result;
		List<String> setOfValues = new ArrayList<String>();
		for(int i=0 ; i<nodes.getLength() ; i++)
		{
			if (nodes.item(i) != null && nodes.item(i).getNodeValue() != null) {
				String value = nodes.item(i).getNodeValue();
				setOfValues.add(value);	
			}	
		}
		
		return setOfValues;
	}
	
	
	
	public Map<String, String> getPrefixes() {
		return prefixes;
	}

	public void setPrefixes(Map<String, String> prefixes) {
		this.prefixes = prefixes;
	}

	public boolean isNamespaceAware() {
		return namespaceAware;
	}

	public void setNamespaceAware(boolean namespaceAware) {
		this.namespaceAware = namespaceAware;
	}

	public static void assertNotNull (Object object , String variableName ) throws Exception
	{
			assertCondition(object != null , variableName + " must not be null ");	
	}
	
	public static void assertCondition (boolean condition , String errorMessage ) throws Exception
	{
		if(condition == false)
			throw new Exception(errorMessage);
	}
	
	
	
	
	public static void main(String[] args) throws Exception {
		Mapping one = new Mapping();
		one.setName("patientId");
		one.setType(Type.SIMPLE);
		one.setXpath("/gbm:tcga_bcr/gbm:patient/shared:bcr_patient_barcode/text()");
		
//		Mapping two = new Mapping();
//		two.setName("two");
//		two.setType(Type.SIMPLE);
//		two.setXpath("");
//		
//		Mapping three = new Mapping();
//		three.setName("three");
//		three.setType(Type.ARRAY);
//		three.setXpath("");
//		
//		Mapping four = new Mapping();
//		four.setName("four");
//		four.setType(Type.OBJECT);
//		four.setNestedFields( Arrays.asList(new Mapping[]{one, three}));
		
		XML2JSON xml2json = new XML2JSON();
		Map<String,String> prefixes = xml2json.prefixes;
		prefixes.put("gbm", "http://tcga.nci/bcr/xml/clinical/gbm/2.5");
		prefixes.put("shared", "http://tcga.nci/bcr/xml/clinical/shared/2.5");
		xml2json.setNamespaceAware(true);
		xml2json.setMappings(Arrays.asList(new Mapping[]{one}));
		xml2json.init();
		FileInputStream fis = new FileInputStream("/Users/Nadir/dev/TCGA-GBM Clinical Data/nationwidechildrens.org_GBM.bio.Level_1.7.43.0/nationwidechildrens.org_clinical.TCGA-02-0084.xml");
		List<JsonObject> retVal = xml2json.parseXML(fis);
		
		System.out.println(retVal);
		
	}
	
	public static String  toString(Document doc) throws Exception 
	{
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer transformer = tf.newTransformer();
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		StringWriter writer = new StringWriter();
		transformer.transform(new DOMSource(doc), new StreamResult(writer));
		String output = writer.getBuffer().toString().replaceAll("\n|\r", "");
		return output;
	}
	
	public static String  toString(Node node) throws Exception 
	{
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer transformer = tf.newTransformer();
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		StringWriter writer = new StringWriter();
		transformer.transform(new DOMSource(node), new StreamResult(writer));
		String output = writer.getBuffer().toString().replaceAll("\n|\r", "");
		return output;
	}
}
