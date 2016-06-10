package edu.emory.cci.bindaas.aime.dataprovider.model;

import java.util.Date;

public class AIMBean {

	private String uniqueIdentifier;
	private String reviewer;
	private Date dateCreated;
	private String patientId;
	private String imageSopInstanceUID;
	private String seriesInstanceUID;
	private String studyInstanceUID;
	private String markups;
	private String xmlContent;
		
	
	public String getMarkups() {
		return markups;
	}
	public void setMarkups(String markups) {
		this.markups = markups;
	}
	public String getImageSopInstanceUID() {
		return imageSopInstanceUID;
	}
	public void setImageSopInstanceUID(String imageSopInstanceUID) {
		this.imageSopInstanceUID = imageSopInstanceUID;
	}
	public String getSeriesInstanceUID() {
		return seriesInstanceUID;
	}
	public void setSeriesInstanceUID(String seriesInstanceUID) {
		this.seriesInstanceUID = seriesInstanceUID;
	}
	public String getStudyInstanceUID() {
		return studyInstanceUID;
	}
	public void setStudyInstanceUID(String studyInstanceUID) {
		this.studyInstanceUID = studyInstanceUID;
	}
	public String getUniqueIdentifier() {
		return uniqueIdentifier;
	}
	public void setUniqueIdentifier(String uniqueIdentifier) {
		this.uniqueIdentifier = uniqueIdentifier;
	}
	public String getReviewer() {
		return reviewer;
	}
	public void setReviewer(String reviewer) {
		this.reviewer = reviewer;
	}
	public Date getDateCreated() {
		return dateCreated;
	}
	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}
	public String getPatientId() {
		return patientId;
	}
	public void setPatientId(String patientId) {
		this.patientId = patientId;
	}
	public String getXmlContent() {
		return xmlContent;
	}
	public void setXmlContent(String xmlContent) {
		this.xmlContent = xmlContent;
	}
	
}
