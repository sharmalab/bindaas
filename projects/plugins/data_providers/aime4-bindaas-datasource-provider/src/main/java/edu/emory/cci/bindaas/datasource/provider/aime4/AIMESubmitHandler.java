package edu.emory.cci.bindaas.datasource.provider.aime4;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import edu.emory.cci.bindaas.datasource.provider.aime4.jaxb.AnnotationOfAnnotationCollection;
import edu.emory.cci.bindaas.datasource.provider.aime4.jaxb.ImageAnnotationCollection;
import edu.emory.cci.bindaas.datasource.provider.aime4.model.AnnotationContainerBean;
import edu.emory.cci.bindaas.datasource.provider.aime4.model.DataSourceConfiguration;
import edu.emory.cci.bindaas.datasource.provider.aime4.model.SubmitEndpointProperties;
import edu.emory.cci.bindaas.datasource.provider.aime4.model.SubmitEndpointProperties.InputType;
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
	private  Unmarshaller imageAnnotationCollectionUnmarshaller ;
	private  Unmarshaller annotationOfAnnotationCollectionUnmarshaller ;
	
	
	private  DateFormat dateFormat;
	private  String dateFormatString = "yyyyMMddhhmmss";
	private String insertQuery;
	
	public String getInsertQuery() {
		return insertQuery;
	}

	public void setInsertQuery(String insertQuery) {
		this.insertQuery = insertQuery;
	}
	
	public void init() throws Exception
	{
		dateFormat = new SimpleDateFormat(dateFormatString);
		imageAnnotationCollectionUnmarshaller = JAXBContext.newInstance(ImageAnnotationCollection.class).createUnmarshaller();
		annotationOfAnnotationCollectionUnmarshaller = JAXBContext.newInstance(AnnotationOfAnnotationCollection.class).createUnmarshaller(); 
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
					JsonObject retVal = saveAnnotationToDatabase(requestContext ,annotations, connection, seProps.getTableName());
					
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
			JsonObject endpointProperties, String data , RequestContext requestContext)
			throws AbstractHttpCodeException {
		SubmitEndpointProperties seProps = GSONUtil.getGSONInstance().fromJson(endpointProperties, SubmitEndpointProperties.class);
		if(seProps.getInputType().equals(InputType.XML))
		{
			Connection connection = null;
			try {
				DataSourceConfiguration configuration = GSONUtil.getGSONInstance().fromJson(dataSource, DataSourceConfiguration.class);
				connection = AIMEProvider.getConnection(configuration);
				JsonObject retVal = saveAnnotationToDatabase(requestContext ,data, connection, seProps.getTableName());
				
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

	public  AnnotationContainerBean createAnnotationContainerBeanFromDocument(String annotationContent)
			throws Exception {
		
		// parse using JAXB
		try{
				JAXBElement<ImageAnnotationCollection> annotationCollectionWrapper  = imageAnnotationCollectionUnmarshaller.unmarshal( new StreamSource(new StringReader(annotationContent)), ImageAnnotationCollection.class);
				ImageAnnotationCollection imageAnnotationCollection = annotationCollectionWrapper.getValue();
				AnnotationContainerBean annotationContainerBean = new AnnotationContainerBean();
				annotationContainerBean.setDateCreated(dateFormat.parse(imageAnnotationCollection.getDateTime().getValue())); // TODO : confirm date format from Vlad
				annotationContainerBean.setUniqueIdentifier(imageAnnotationCollection.getUniqueIdentifier().getRoot());
				annotationContainerBean.setXmlContent(annotationContent);
				annotationContainerBean.setReviewer(imageAnnotationCollection.getUser().getLoginName().getValue());
				annotationContainerBean.setPatientId(imageAnnotationCollection.getPerson().getId().getValue());
				annotationContainerBean.setCount(imageAnnotationCollection.getImageAnnotations().getImageAnnotation().size());
				annotationContainerBean.setType("ImageAnnotationCollection");
				return annotationContainerBean;
		} 
		catch(Exception e)
		{
			// try with AnnotationOfAnnotationCollection
			JAXBElement<AnnotationOfAnnotationCollection> annotationCollectionWrapper  = annotationOfAnnotationCollectionUnmarshaller.unmarshal( new StreamSource(new StringReader(annotationContent)), AnnotationOfAnnotationCollection.class);
			AnnotationOfAnnotationCollection annotationOfAnnotationCollection = annotationCollectionWrapper.getValue();
			AnnotationContainerBean annotationContainerBean = new AnnotationContainerBean();
			annotationContainerBean.setDateCreated(dateFormat.parse(annotationOfAnnotationCollection.getDateTime().getValue())); // TODO : confirm date format from Vlad
			annotationContainerBean.setUniqueIdentifier(annotationOfAnnotationCollection.getUniqueIdentifier().getRoot());
			annotationContainerBean.setXmlContent(annotationContent);
			annotationContainerBean.setReviewer(annotationOfAnnotationCollection.getUser().getLoginName().getValue());
			annotationContainerBean.setCount(annotationOfAnnotationCollection.getAnnotationOfAnnotations().getAnnotationOfAnnotation().size());
			annotationContainerBean.setType("AnnotationOfAnnotationCollection");
			return annotationContainerBean;
		}
		
		
		
		
		
		
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

	private JsonObject saveAnnotationToDatabase(RequestContext requestContext, String content , Connection connection , String tableName) throws Exception 
	{
		try {
			AnnotationContainerBean aimBean = createAnnotationContainerBeanFromDocument(content);
			String insertStatement = String.format(insertQuery, tableName);
			PreparedStatement ps = connection.prepareStatement(insertStatement);
			ps.setString( 1, aimBean.getUniqueIdentifier());
			ps.setString( 2, aimBean.getReviewer());
			ps.setTimestamp(3 ,  (new java.sql.Timestamp(aimBean.getDateCreated().getTime()))); 
			ps.setString( 4, aimBean.getPatientId());
			ps.setObject( 5, aimBean.getXmlContent()); 
			ps.setString( 6, requestContext.getUser());
			ps.setString( 7, aimBean.getType());
			ps.setInt( 8, aimBean.getCount() );
			
			ps.executeUpdate();
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
	
	private JsonObject saveAnnotationToDatabase(RequestContext requestContext,Collection<String> contents , Connection connection , String tableName) throws Exception 
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
					AnnotationContainerBean aimBean = createAnnotationContainerBeanFromDocument(content);
					submissionResult = new JsonObject();
					submissionResult.add("uid", new JsonPrimitive(aimBean.getUniqueIdentifier()));
					String insertStatement = String.format(insertQuery, tableName ,
							aimBean.getUniqueIdentifier() , aimBean.getReviewer() , (new java.sql.Timestamp(aimBean.getDateCreated().getTime())).toString(),
							aimBean.getPatientId(),aimBean.getXmlContent(), requestContext.getUser() , aimBean.getType() , aimBean.getCount() );
					
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
	
//	public static void main(String[] args) throws Exception{
//		
//		FileInputStream fis = new FileInputStream("/Users/Nadir/dev/ClearCanvas AIM Integration/outdoc_image_annotation.xml");
//		String annotationContent = IOUtils.toString(fis);
//		AIMESubmitHandler handler = new AIMESubmitHandler();
//		handler.init();
//		AnnotationContainerBean containerBean = handler.createAnnotationContainerBeanFromDocument(annotationContent);
//		System.out.println(containerBean);
//	}

}
