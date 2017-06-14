package edu.emory.cci.bindaas.datasource.provider.aime4.outputformat;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.sql.ResultSet;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import edu.emory.cci.bindaas.datasource.provider.aime4.bundle.Activator;
import edu.emory.cci.bindaas.datasource.provider.aime4.jaxb.AnnotationOfAnnotationCollection;
import edu.emory.cci.bindaas.datasource.provider.aime4.model.OutputFormatProps;
import edu.emory.cci.bindaas.datasource.provider.aime4.model.OutputFormatProps.OutputFormat;
import edu.emory.cci.bindaas.datasource.provider.aime4.model.OutputFormatProps.QueryType;
import edu.emory.cci.bindaas.datasource.provider.aime4.model.SimplyfiedAnnotationOfAnnotationCollection;
import edu.emory.cci.bindaas.framework.model.QueryResult;
import edu.emory.cci.bindaas.framework.util.StandardMimeType;

public class XqueryAnnotationOfAnnotationJSONHandler implements IFormatHandler {

	
	private  Unmarshaller annotationOfAnnotationCollectionUnmarshaller ;
	
	public XqueryAnnotationOfAnnotationJSONHandler() throws JAXBException
	{
		annotationOfAnnotationCollectionUnmarshaller = JAXBContext.newInstance(AnnotationOfAnnotationCollection.class).createUnmarshaller();
		Activator.getContext().registerService(IFormatHandler.class.getName(), this, null);
	}
	
	@Override
	public QueryResult format(OutputFormatProps outputFormatProps,
			ResultSet queryResult) throws Exception {
		QueryResult qr = new QueryResult();
		JsonArray jsonArray = new JsonArray();
		 while (queryResult.next())
	      {
			 String annotationContent = queryResult.getString(1);
			 if(annotationContent!=null)
			 {
				 JAXBElement<AnnotationOfAnnotationCollection> wrapper = annotationOfAnnotationCollectionUnmarshaller.unmarshal(new StreamSource(new StringReader(annotationContent)), AnnotationOfAnnotationCollection.class);
				 SimplyfiedAnnotationOfAnnotationCollection simplified = SimplyfiedAnnotationOfAnnotationCollection.convert(wrapper.getValue());
				 JsonObject json = simplified.toJSON();
				 jsonArray.add(json);
			 }
	      }
		 
	     
		 
		 qr.setData(new ByteArrayInputStream(jsonArray.toString().getBytes()));
		 qr.setMimeType(StandardMimeType.JSON.toString());
		return qr;
		
	}

	@Override
	public QueryType getQueryType() {
		
		return QueryType.XQUERY;
	}

	@Override
	public OutputFormat getOutputFormat() {

		return OutputFormat.SIMPLE_JSON_ANNO_OF_ANNOTATION;
	}

	@Override
	public void validate(OutputFormatProps outputFormatProps) throws Exception {
		if(outputFormatProps.getOutputFormat()!=OutputFormat.SIMPLE_JSON_ANNO_OF_ANNOTATION || outputFormatProps.getQueryType() != QueryType.XQUERY)
			throw new Exception("Incompatible OutputFormat and/or QueryType specified. Expected QueryType =[" + QueryType.XQUERY + "] and OutputFormat=["+ OutputFormat.SIMPLE_JSON_ANNO_OF_ANNOTATION + "]");
		if(outputFormatProps.getRootElement() == null) throw new Exception("Root XML Element not provided");
	}

}
