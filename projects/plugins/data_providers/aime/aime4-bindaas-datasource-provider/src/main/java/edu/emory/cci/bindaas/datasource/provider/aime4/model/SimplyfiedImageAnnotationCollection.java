package edu.emory.cci.bindaas.datasource.provider.aime4.model;

import java.util.ArrayList;
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
		simplified.annotationContainerUID = imageAnnotationCollection.getUniqueIdentifier().getRoot(); // mandatory
		
		try{
		simplified.patientId = imageAnnotationCollection.getPerson().getId().getValue(); 
		}catch(NullPointerException ne){}
		
		try{
		simplified.patientName = imageAnnotationCollection.getPerson().getName().getValue();
		}catch(NullPointerException ne){}
		
		try{
		simplified.sex = imageAnnotationCollection.getPerson().getSex().getValue();
		}catch(NullPointerException ne){}
		
		try{
		simplified.dob = imageAnnotationCollection.getPerson().getBirthDate().getValue();
		}catch(NullPointerException ne){}
		
		try{
		simplified.annotationDateTime = imageAnnotationCollection.getDateTime().getValue();// mandatory
		}catch(NullPointerException ne){}
		
		simplified.annotationType = "ImageAnnotation";
		
		try{
		simplified.user = imageAnnotationCollection.getUser().getLoginName().getValue();
		}catch(NullPointerException ne){}
		
		List<ImageAnnotation> listOfAnnotations = new ArrayList<SimplyfiedImageAnnotationCollection.ImageAnnotation>();
		
		if(imageAnnotationCollection.getImageAnnotations()!=null && imageAnnotationCollection.getImageAnnotations().getImageAnnotation()!=null)
		{
			ImageAnnotation simpleImageAnnotation = new ImageAnnotation();
			for(edu.emory.cci.bindaas.datasource.provider.aime4.jaxb.ImageAnnotation fullImageAnnotation : imageAnnotationCollection.getImageAnnotations().getImageAnnotation())
			{
				try{
				simpleImageAnnotation.annotationName = fullImageAnnotation.getName().getValue();
				}catch(NullPointerException ne){}
				
				try{
				simpleImageAnnotation.annotationUID = fullImageAnnotation.getUniqueIdentifier().getRoot();
				}catch(NullPointerException ne){}
				
				try{
				DicomImageReferenceEntity dicomImageRef = (DicomImageReferenceEntity) fullImageAnnotation.getImageReferenceEntityCollection().getImageReferenceEntity().get(0); 
				simpleImageAnnotation.studyInstanceUid = dicomImageRef.getImageStudy().getInstanceUid().getRoot();
				
				try{
				simpleImageAnnotation.imagingObservationEntities = fullImageAnnotation.getImagingObservationEntityCollection().getImagingObservationEntity();
				}catch(NullPointerException ne){}
				
				try{
				simpleImageAnnotation.imagingPhysicalEntities = fullImageAnnotation.getImagingPhysicalEntityCollection().getImagingPhysicalEntity();
				}catch(NullPointerException ne){}
				
				}catch(NullPointerException ne){}
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
