package edu.emory.cci.bindaas.datasource.provider.aime;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.google.gson.JsonObject;

import edu.emory.cci.bindaas.framework.api.ISubmitHandler;
import edu.emory.cci.bindaas.framework.model.ProviderException;
import edu.emory.cci.bindaas.framework.model.QueryResult;
import edu.emory.cci.bindaas.framework.model.SubmitEndpoint;
import edu.emory.cci.bindaas.framework.model.SubmitEndpoint.Type;
import edu.emory.cci.bindaas.framework.util.GSONUtil;
import edu.emory.cci.bindaas.datasource.provider.aime.model.AIMBean;
import edu.emory.cci.bindaas.datasource.provider.aime.model.DataSourceConfiguration;
import edu.emory.cci.bindaas.datasource.provider.aime.model.SubmitEndpointProperties;
import edu.emory.cci.bindaas.datasource.provider.aime.model.SubmitEndpointProperties.InputType;

public class AIMESubmitHandler implements ISubmitHandler {

	private Log log = LogFactory.getLog(getClass());
	private String aimDocNamespace = "gme://caCORE.caCORE/3.2/edu.northwestern.radiology.AIM";
	private String aimDocPrefix = "ns1";
	private Map<String,String> xpathExpressions;
	private DateFormat dateFormat;
	private String dateFormatString = "yyyy-MM-dd'T'hh:mm:ss";
	private String insertQuery;
	
	
	public String getDateFormatString() {
		return dateFormatString;
	}

	public void setDateFormatString(String dateFormatString) {
		this.dateFormatString = dateFormatString;
	}

	public String getAimDocNamespace() {
		return aimDocNamespace;
	}

	public void setAimDocNamespace(String aimDocNamespace) {
		this.aimDocNamespace = aimDocNamespace;
	}

	public String getAimDocPrefix() { 
		return aimDocPrefix;
	}

	public String getInsertQuery() {
		return insertQuery;
	}

	public void setInsertQuery(String insertQuery) {
		this.insertQuery = insertQuery;
	}

	public void setAimDocPrefix(String aimDocPrefix) {
		this.aimDocPrefix = aimDocPrefix;
	}

	public Map<String, String> getXpathExpressions() {
		return xpathExpressions;
	}

	public void setXpathExpressions(Map<String, String> xpathExpressions) {
		this.xpathExpressions = xpathExpressions;
	}

	public void init() throws Exception
	{
		dateFormat = new SimpleDateFormat(dateFormatString);
	}
	
	@Override
	public QueryResult submit(JsonObject dataSource,
			JsonObject endpointProperties, InputStream is)
			throws ProviderException {
		throw new ProviderException("Not Yet Implemented"); // TODO : handle zip stream
	}

	@Override
	public QueryResult submit(JsonObject dataSource,
			JsonObject endpointProperties, String data)
			throws ProviderException {
		SubmitEndpointProperties seProps = GSONUtil.getGSONInstance().fromJson(endpointProperties, SubmitEndpointProperties.class);
		if(seProps.getInputType().equals(InputType.XML))
		{
			Connection connection = null;
			try {
				DataSourceConfiguration configuration = GSONUtil.getGSONInstance().fromJson(dataSource, DataSourceConfiguration.class);
				connection = AIMEProvider.getConnection(configuration);
				AIMBean aimBean = createAIMBeanFromDocument(data);
				
				Statement st = connection.createStatement();
				String insertStatement = String.format(insertQuery, seProps.getTableName() ,aimBean.getUniqueIdentifier() , aimBean.getReviewer() , (new java.sql.Timestamp(aimBean.getDateCreated().getTime())).toString(),
						aimBean.getPatientId(),aimBean.getXmlContent(),AIMEProvider.class.getName() + "#" + AIMEProvider.VERSION,aimBean.getImageSopInstanceUID() , aimBean.getStudyInstanceUID(),aimBean.getSeriesInstanceUID());
				
				log.debug(insertStatement);
				st.executeUpdate(insertStatement);
				connection.commit();
				
			} catch (Exception e) {
				log.error(e);
				throw new ProviderException(e);
			}
			finally{
				if(connection!=null){
					try {
						connection.close();
					} catch (SQLException e) {
						log.error(e);
					}
				}
					
			}
		}
		else
		{
			throw new ProviderException("Wrong type of data submitted. Expected XML");
		}
		
		QueryResult result = new QueryResult();
		result.setCallback(false);
		result.setError(false);
		result.setData("{ 'result' : 'success'}".getBytes());
		return result;
	}

	@Override
	public SubmitEndpoint validateAndInitializeSubmitEndpoint(
			SubmitEndpoint submitEndpoint) throws ProviderException {
		JsonObject props = submitEndpoint.getProperties();
		SubmitEndpointProperties seProps = GSONUtil.getGSONInstance().fromJson(props, SubmitEndpointProperties.class);
		
		if(seProps.getInputType().equals(InputType.XML))
		{
			submitEndpoint.setType(Type.FORM_DATA); 
		}
		else if(seProps.getInputType().equals(InputType.ZIP))
		{
			submitEndpoint.setType(Type.MULTIPART); 
		}
		
		
		
		return submitEndpoint;
	}

	@Override
	public JsonObject getSubmitPropertiesSchema() {

		return new JsonObject(); // TODO : return json schema
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
				log.debug("Assigning [" + field + "] = [" + value + "]");
			} else
				log.warn("No value found for attrubute [" + field + "]");
			
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
}
