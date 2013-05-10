package edu.emory.cci.bindaas.datasource.provider.aime4.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;

import edu.emory.cci.bindaas.datasource.provider.aime4.jaxb.DicomImageReferenceEntity;
import edu.emory.cci.bindaas.datasource.provider.aime4.jaxb.ImageAnnotationCollection;
import edu.emory.cci.bindaas.datasource.provider.aime4.jaxb.ImagingObservationEntity;
import edu.emory.cci.bindaas.datasource.provider.aime4.jaxb.ImagingPhysicalEntity;

public class SimplyfiedImageAnnotationCollection {
	
	@Expose private String annotationContainerUID;
	@Expose private String patientId;
	@Expose private String patientName;
	@Expose private String sex;
	@Expose private String dob;
	@Expose private String annotationDateTime;
	@Expose private String annotationType;
	@Expose private String user;
	@Expose private List<ImageAnnotation> annotations;
	
	private static Gson gson = new Gson();
	
	
	public  JsonObject toJSON (){
		 return gson.toJsonTree(this).getAsJsonObject();
	};
	
	public static SimplyfiedImageAnnotationCollection convert(ImageAnnotationCollection imageAnnotationCollection)
	{
		SimplyfiedImageAnnotationCollection simplified = new SimplyfiedImageAnnotationCollection();
		simplified.annotationContainerUID = imageAnnotationCollection.getUniqueIdentifier().getRoot();
		simplified.patientId = imageAnnotationCollection.getPerson().getId().getValue();
		simplified.patientName = imageAnnotationCollection.getPerson().getName().getValue();
		simplified.sex = imageAnnotationCollection.getPerson().getSex().getValue();
		simplified.dob = imageAnnotationCollection.getPerson().getBirthDate().getValue();
		simplified.annotationDateTime = imageAnnotationCollection.getDateTime().getValue();
		simplified.annotationType = "ImageAnnotation";
		simplified.user = imageAnnotationCollection.getUser().getLoginName().getValue();
		
		List<ImageAnnotation> listOfAnnotations = new ArrayList<SimplyfiedImageAnnotationCollection.ImageAnnotation>();
		
		if(imageAnnotationCollection.getImageAnnotations()!=null && imageAnnotationCollection.getImageAnnotations().getImageAnnotation()!=null)
		{
			ImageAnnotation simpleImageAnnotation = new ImageAnnotation();
			for(edu.emory.cci.bindaas.datasource.provider.aime4.jaxb.ImageAnnotation fullImageAnnotation : imageAnnotationCollection.getImageAnnotations().getImageAnnotation())
			{
				simpleImageAnnotation.annotationName = fullImageAnnotation.getName().getValue();
				simpleImageAnnotation.annotationUID = fullImageAnnotation.getUniqueIdentifier().getRoot();
				DicomImageReferenceEntity dicomImageRef = (DicomImageReferenceEntity) fullImageAnnotation.getImageReferenceEntityCollection().getImageReferenceEntity().get(0); 
				simpleImageAnnotation.studyInstanceUid = dicomImageRef.getImageStudy().getInstanceUid().getRoot();
				simpleImageAnnotation.imagingObservationEntities = fullImageAnnotation.getImagingObservationEntityCollection().getImagingObservationEntity();
				simpleImageAnnotation.imagingPhysicalEntities = fullImageAnnotation.getImagingPhysicalEntityCollection().getImagingPhysicalEntity();
			}
			listOfAnnotations.add(simpleImageAnnotation);
		}
		
		simplified.annotations = listOfAnnotations;
		return simplified; 
	}
	
	public static class ImageAnnotation 
	{
		@Expose private String annotationUID;
		@Expose private String annotationName;
		@Expose private String studyInstanceUid;
		@Expose private List<ImagingPhysicalEntity> imagingPhysicalEntities;
		@Expose private List<ImagingObservationEntity> imagingObservationEntities;
	}
	
	
}
