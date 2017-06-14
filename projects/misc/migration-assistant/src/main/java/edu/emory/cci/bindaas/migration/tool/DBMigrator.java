package edu.emory.cci.bindaas.migration.tool;

import java.io.ByteArrayInputStream;
import java.sql.Connection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.ibm.db2.jcc.DB2Driver;

import edu.emory.cci.bindaas.migrationasst.datasource.provider.aime.model.AIMBean;

public class DBMigrator {

	private String sourceTable;
	private String stagingTable;
	private String username;
	private String password;
	private String url;
	private DB2Driver dbDriver;
	private String createTableQuery;
	private String readAnnotationQuery;
	private String aimDocNamespace = "gme://caCORE.caCORE/3.2/edu.northwestern.radiology.AIM";
	private String aimDocPrefix = "ns1";
	private Map<String,String> xpathExpressions;
	private DateFormat dateFormat;
	private String dateFormatString = "yyyy-MM-dd'T'hh:mm:ss";
	
	public DBMigrator()
	{
		dbDriver = new DB2Driver();
	}
	
	
	public void init()
	{
		dateFormat = new SimpleDateFormat(dateFormatString);
	}
	
	public AIMBean createAIMBeanFromDocument(String annotationContent)
			throws Exception {
		AIMBean aimBean = new AIMBean();

		DocumentBuilderFactory domFactory = DocumentBuilderFactory
				.newInstance();
		domFactory.setNamespaceAware(true); // never forget this!
		DocumentBuilder builder = domFactory.newDocumentBuilder();
		Document doc = builder.parse( new ByteArrayInputStream(annotationContent.getBytes())); 

		
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
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
				else if (aimDocPrefix.equals(prefix))
					return aimDocNamespace;
				else if ("xml".equals(prefix))
					return XMLConstants.XML_NS_URI;
				return XMLConstants.NULL_NS_URI;
			}
		});
		
		Map<String,String> parsedValues = new HashMap<String, String>();
		for(String field : xpathExpressions.keySet())
		{
			XPathExpression expression = xpath.compile(xpathExpressions.get(field));
			Object result = expression.evaluate(doc, XPathConstants.NODESET);
			NodeList nodes = (NodeList) result;

			if (nodes.item(0) != null && nodes.item(0).getNodeValue() != null) {
				String value = nodes.item(0).getNodeValue();
				parsedValues.put(field, value);
				System.out.println("Assigning [" + field + "] = [" + value + "]");
			} else
				System.out.println("No value found for attrubute [" + field + "]");
			
		}
		
		aimBean.setPatientId(parsedValues.get("patientId"));
		aimBean.setReviewer(parsedValues.get("reviewer"));
		aimBean.setUniqueIdentifier(parsedValues.get("uniqueIdentifier"));
		aimBean.setXmlContent(annotationContent);
		aimBean.setDateCreated(dateFormat.parse(parsedValues.get("dateCreated")));
		aimBean.setImageSopInstanceUID(parsedValues.get("imageSopInstanceUID"));
		aimBean.setStudyInstanceUID(parsedValues.get("studyInstanceUID"));
		aimBean.setSeriesInstanceUID(parsedValues.get("seriesInstanceUID"));
		return aimBean;
	}
	
	public void start() throws Exception
	{
		Connection conn = null;
		try{
			
			Properties connectionProps = new Properties();
			connectionProps.put("user", username);
			connectionProps.put("password", password);
			conn = dbDriver.connect(url, connectionProps);
			conn.setAutoCommit(false);
			
			dropStagingTable(conn);
			
			List<String> annotations = readAnnotations(conn);
			for(String annotation : annotations)
			{
				AIMBean aimBean = createAIMBeanFromDocument(annotation);
				insertIntoStagingTable(conn, aimBean);
			}
			
			conn.commit();
			
			dropSourceTable(conn);
			createSourceTable(conn);
			copyStagingToSourceTable(conn);
			conn.commit();
			
		}catch(Exception e)
		{
			throw e;
		}
		finally{
			if(conn!=null) conn.close();
		}
	}
	
	public void copyStagingToSourceTable(Connection conn) throws Exception
	{
		
	}
	
	public void createSourceTable(Connection conn) throws Exception
	{
		
	}
	public void dropSourceTable(Connection conn) throws Exception
	{
		
	}
	public void createStagingTable(Connection conn)
	{
		
	}
	public void dropStagingTable(Connection conn) throws Exception
	{
		
	}
	
	public void insertIntoStagingTable(Connection conn , AIMBean aimBean ) throws Exception
	{
		
	}
	
	public List<String> readAnnotations(Connection conn) throws Exception
	{
		return null;
	}
	
	
	
}
