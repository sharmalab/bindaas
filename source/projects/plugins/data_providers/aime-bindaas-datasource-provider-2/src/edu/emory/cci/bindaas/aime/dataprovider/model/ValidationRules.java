package edu.emory.cci.bindaas.aime.dataprovider.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPath;

import org.w3c.dom.Document;

import com.google.gson.annotations.Expose;

import edu.emory.cci.bindaas.aime.dataprovider.exception.MarkupValidationException;

public class ValidationRules {
	
	public ValidationRules()
	{
		validationRules = new ArrayList<ValidationRule>();
	}

	@Expose private List<ValidationRule> validationRules;

	public List<ValidationRule> getValidationRules() {
		return validationRules;
	}

	public void setValidationRules(List<ValidationRule> validationRules) {
		this.validationRules = validationRules;
	}
	
	

	public Markups extractMarkups(Document doc, XPath xpath) throws MarkupValidationException {
		Markups markups = new Markups();
		for(ValidationRule rule : this.validationRules)
		{
			MarkupInfo markupInfo = rule.validateAndExtractMarkupInfo(doc, xpath);
			markups.getMarkups().put(rule.getShape(), markupInfo);
		}
		return markups;
	}
	
}
