package edu.emory.cci.bindaas.aime.dataprovider;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import edu.emory.cci.bindaas.aime.dataprovider.exception.MarkupValidationException;
import edu.emory.cci.bindaas.aime.dataprovider.model.AIMBean;
import edu.emory.cci.bindaas.aime.dataprovider.model.DataSourceConfiguration;
import edu.emory.cci.bindaas.aime.dataprovider.model.MarkupInfo;
import edu.emory.cci.bindaas.aime.dataprovider.model.Markups;
import edu.emory.cci.bindaas.aime.dataprovider.model.SubmitEndpointProperties;
import edu.emory.cci.bindaas.aime.dataprovider.model.ValidationRule;
import edu.emory.cci.bindaas.aime.dataprovider.model.SubmitEndpointProperties.InputType;
import edu.emory.cci.bindaas.framework.api.ISubmitHandler;
import edu.emory.cci.bindaas.framework.model.ProviderException;
import edu.emory.cci.bindaas.framework.model.QueryResult;
import edu.emory.cci.bindaas.framework.model.RequestContext;
import edu.emory.cci.bindaas.framework.model.SubmitEndpoint;
import edu.emory.cci.bindaas.framework.model.SubmitEndpoint.Type;
import edu.emory.cci.bindaas.framework.provider.exception.AbstractHttpCodeException;
import edu.emory.cci.bindaas.framework.provider.exception.BadContentException;
import edu.emory.cci.bindaas.framework.provider.exception.SubmitExecutionFailedException;
import edu.emory.cci.bindaas.framework.util.GSONUtil;
import edu.emory.cci.bindaas.framework.util.IOUtils;
import edu.emory.cci.bindaas.framework.util.StandardMimeType;

public class AIMESubmitHandler implements ISubmitHandler {

	private static Log log = LogFactory.getLog(AIMESubmitHandler.class);
	private String aimDocNamespace = "gme://caCORE.caCORE/3.2/edu.northwestern.radiology.AIM";
	private String xsiNamespace = "http://www.w3.org/2001/XMLSchema-instance";
	private String aimDocPrefix = "ns1";
	private Map<String,String> xpathExpressions;
	private DateFormat dateFormat;
	private String dateFormatString = "yyyy-MM-dd'T'hh:mm:ss";
	private String insertQuery;
	private DocumentBuilderFactory domFactory;
	private XPath xpath;
	
	
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
		domFactory = DocumentBuilderFactory
				.newInstance();
		domFactory.setNamespaceAware(true); // never forget this!
		
		XPathFactory factory = XPathFactory.newInstance();
		this.xpath = factory.newXPath();
		xpath.setNamespaceContext(new NamespaceContext() {

			@SuppressWarnings("rawtypes")
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
				else if ("xsi".equals(prefix))
					return xsiNamespace;
				return XMLConstants.NULL_NS_URI;
			}
		});
		

	}
	
	private String getSource(RequestContext requestContext)
	{
		if(requestContext!=null && requestContext.getUser()!=null)
		{
			return requestContext.getUser();
		}
		else
			return  AIMEProvider.class.getName() + "#" + AIMEProvider.VERSION;
	}
	@Override
	public QueryResult submit(JsonObject dataSource,
			JsonObject endpointProperties, InputStream is , RequestContext requestContext)
			throws AbstractHttpCodeException {
		
		SubmitEndpointProperties seProps = GSONUtil.getGSONInstance().fromJson(endpointProperties, SubmitEndpointProperties.class);
		if(seProps.getInputType().equals(InputType.ZIP))
		{
			try{
				
				Collection<String> annotations = extractFromZipAsString(is);
				Connection connection = null;
				try {
					DataSourceConfiguration configuration = GSONUtil.getGSONInstance().fromJson(dataSource, DataSourceConfiguration.class);
					connection = AIMEProvider.getConnection(configuration);
					
					
					String source = getSource(requestContext);
					JsonObject retVal = saveAnnotationToDatabase(annotations, connection, seProps.getTableName() , source , configuration.getValidationRules());
					
					QueryResult result = new QueryResult();
					result.setData( new ByteArrayInputStream(retVal.toString().getBytes()));
					result.setMimeType(StandardMimeType.JSON.toString());
					return result;
					
				} catch (Exception e) {
					log.error(e);
					try {
						connection.rollback();
					} catch (SQLException e1) {
						log.error(e1);
						throw new ProviderException(AIMEProvider.class.getName(),AIMEProvider.VERSION,e1);
					}
					throw new ProviderException(AIMEProvider.class.getName(),AIMEProvider.VERSION,e);
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
			catch(Exception e)
			{
				log.error(e);
				throw new SubmitExecutionFailedException(AIMEProvider.class.getName(), AIMEProvider.VERSION, e);
			}
		}
		else
		{
			throw new BadContentException(AIMEProvider.class.getName(),AIMEProvider.VERSION,"Wrong type of data submitted. Expected ZIP");
		}
		
		
	}

	@Override
	public QueryResult submit(JsonObject dataSource,
			JsonObject endpointProperties, String data, RequestContext requestContext)
			throws AbstractHttpCodeException {
		SubmitEndpointProperties seProps = GSONUtil.getGSONInstance().fromJson(endpointProperties, SubmitEndpointProperties.class);
		if(seProps.getInputType().equals(InputType.XML))
		{
			Connection connection = null;
			try {
				DataSourceConfiguration configuration = GSONUtil.getGSONInstance().fromJson(dataSource, DataSourceConfiguration.class);
				connection = AIMEProvider.getConnection(configuration);
				
				String source = getSource(requestContext);
				JsonObject retVal = saveAnnotationToDatabase(data, connection, seProps.getTableName() ,source, configuration.getValidationRules());
				
				QueryResult result = new QueryResult();
				
				result.setData(new ByteArrayInputStream(retVal.toString().getBytes()));
				result.setMimeType(StandardMimeType.JSON.toString());
				return result;
			} catch (Exception e) {
				log.error(e);
				try {
					connection.rollback();
				} catch (SQLException e1) {
					log.error(e1);
					throw new SubmitExecutionFailedException(AIMEProvider.class.getName(),AIMEProvider.VERSION,e1);
				}
				throw new SubmitExecutionFailedException(AIMEProvider.class.getName(),AIMEProvider.VERSION,e);
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
			throw new BadContentException(AIMEProvider.class.getName(),AIMEProvider.VERSION,"Wrong type of data submitted. Expected XML");
		}
		
		
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

	public AIMBean createAIMBeanFromDocument(String annotationContent, List<ValidationRule> validationRules)
			throws Exception {
		AIMBean aimBean = new AIMBean();
		DocumentBuilder builder = this.domFactory.newDocumentBuilder();
		Document doc = builder.parse( new ByteArrayInputStream(annotationContent.getBytes())); 
		
		Map<String,String> parsedValues = new HashMap<String, String>();
		for(String field : this.xpathExpressions.keySet())
		{
			XPathExpression expression = this.xpath.compile(xpathExpressions.get(field));
			Object result = expression.evaluate(doc, XPathConstants.NODESET);
			NodeList nodes = (NodeList) result;

			if (nodes.item(0) != null && nodes.item(0).getNodeValue() != null) {
				String value = nodes.item(0).getNodeValue();
				parsedValues.put(field, value);
				log.debug("Assigning [" + field + "] = [" + value + "]");
			} else
				log.warn("No value found for attrubute [" + field + "]");
			
		}
		
		Markups markups = extractMarkups( validationRules , doc , this.xpath );
		aimBean.setMarkups(markups.toString());
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
	
	public static Markups extractMarkups(List<ValidationRule> validationRules , Document doc , XPath xpath) throws MarkupValidationException
	{
		Markups markups = new Markups();
		for(ValidationRule rule : validationRules)
		{
			MarkupInfo markupInfo = rule.validateAndExtractMarkupInfo(doc, xpath);
			markups.getMarkups().put(rule.getShape(), markupInfo);
		}
		return markups;
	}
	
	public static Collection<String> extractFromZipAsString(
			InputStream is) throws Exception {

			File tempFile = new File(System.getProperty("java.io.tmpdir") + File.separator + UUID.randomUUID().toString() );
			Collection<String> list = new ArrayList<String>();
			log.debug("Saving zip file to [" + tempFile + "]");
			IOUtils.copyAndCloseInput(is, new FileOutputStream(tempFile));
			
			ZipFile zipFile = new ZipFile(tempFile);
			Enumeration<? extends ZipEntry> zipFileEntries = zipFile.entries();
			while(zipFileEntries.hasMoreElements())
			{
				ZipEntry entry = zipFileEntries.nextElement();
				
				if(!entry.isDirectory())
				{
					log.debug("Adding file [" + entry.getName() + "]");
					InputStream zis = zipFile.getInputStream(entry);
					String contents = IOUtils.toString(zis);
					list.add(contents);
				}
				
			}
			
		return list;
	}

	private JsonObject saveAnnotationToDatabase(String content , Connection connection , String tableName, String source , List<ValidationRule> validationRules) throws Exception 
	{
		try {
			AIMBean aimBean = createAIMBeanFromDocument(content,validationRules);
			Statement st = connection.createStatement();
			String insertStatement = String.format(insertQuery, tableName ,aimBean.getUniqueIdentifier() , aimBean.getReviewer() , (new java.sql.Timestamp(aimBean.getDateCreated().getTime())).toString(),
					aimBean.getPatientId(),aimBean.getXmlContent(), source ,aimBean.getImageSopInstanceUID() , aimBean.getStudyInstanceUID(),aimBean.getSeriesInstanceUID()
					, aimBean.getMarkups());
			
			log.debug(insertStatement);
			st.executeUpdate(insertStatement);
			connection.commit();
			
			JsonObject retVal = new JsonObject();
			retVal.add("count", new JsonPrimitive(1));
			JsonArray arr = new JsonArray();
			JsonObject submissionResult = new JsonObject();
			submissionResult.add("uid", new JsonPrimitive(aimBean.getUniqueIdentifier()));
			submissionResult.add("result", new JsonPrimitive("success"));
			arr.add(submissionResult);
			retVal.add("submissions", arr);
			return retVal;
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}
	
	private JsonObject saveAnnotationToDatabase(Collection<String> contents , Connection connection , String tableName , String source, List<ValidationRule> validationRules) throws Exception 
	{
		try {
			Statement st = connection.createStatement();
			int count = 0;
			JsonObject retVal = new JsonObject();
			JsonArray arr = new JsonArray();
			retVal.add("submissions", arr);
			for(String content : contents)
			{
				JsonObject submissionResult = null;
				try{
					AIMBean aimBean = createAIMBeanFromDocument(content, validationRules);
					submissionResult = new JsonObject();
					submissionResult.add("uid", new JsonPrimitive(aimBean.getUniqueIdentifier()));
					String insertStatement = String.format(insertQuery, tableName ,aimBean.getUniqueIdentifier() , aimBean.getReviewer() , (new java.sql.Timestamp(aimBean.getDateCreated().getTime())).toString(),
							aimBean.getPatientId(),aimBean.getXmlContent(), source , aimBean.getImageSopInstanceUID() , aimBean.getStudyInstanceUID(),aimBean.getSeriesInstanceUID() , aimBean.getMarkups());
					
					log.debug(insertStatement);
					st.execute(insertStatement);
					++count;
					submissionResult.add("result", new JsonPrimitive("success"));
				}
				catch(Exception e2)
				{
					log.error("Annotation could not be added\n" + content ,e2);
					if(submissionResult!=null)
					{
						submissionResult.add("result", new JsonPrimitive("failed"));
						submissionResult.add("errorMessage", new JsonPrimitive(e2.getMessage()));
						
					}
				}
				
				if(submissionResult!=null)
					arr.add(submissionResult);
				
				
			}
			
			
			connection.commit();
			retVal.add("count", new JsonPrimitive(count));
			return retVal;
			
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}

}
