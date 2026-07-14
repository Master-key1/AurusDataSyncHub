package com.auruspay.comparator.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;


@Component
public class ComparisionXmlResult {
	
	 private List<Map<String, String>> xmlMatchIssue = new ArrayList<>();
	    private List<Map<String, String>> xmlMissMatchIssue = new ArrayList<>();
	    private List<Map<String, String>> xmlSkippedIssue = new ArrayList<>();
	    private List<Map<String, String>> xmlValidationIssue = new ArrayList<>();
	    String summary ;
	    
	    
		public String getSummary() {
			return summary;
		}
		public void setSummary(String summary) {
			this.summary = summary;
		}
		public List<Map<String, String>> getXmlMatchIssue() {
			return xmlMatchIssue;
		}
		public void setXmlMatchIssue(List<Map<String, String>> xmlMatchIssue) {
			this.xmlMatchIssue = xmlMatchIssue;
		}
		public List<Map<String, String>> getXmlMissMatchIssue() {
			return xmlMissMatchIssue;
		}
		public void setXmlMissMatchIssue(List<Map<String, String>> xmlMissMatchIssue) {
			this.xmlMissMatchIssue = xmlMissMatchIssue;
		}
		public List<Map<String, String>> getXmlSkippedIssue() {
			return xmlSkippedIssue;
		}
		public void setXmlSkippedIssue(List<Map<String, String>> xmlSkippedIssue) {
			this.xmlSkippedIssue = xmlSkippedIssue;
		}
		public List<Map<String, String>> getXmlValidationIssue() {
			return xmlValidationIssue;
		}
		public void setXmlValidationIssue(List<Map<String, String>> xmlValidationIssue) {
			this.xmlValidationIssue = xmlValidationIssue;
		}
		@Override
		public String toString() {
			return "ComparisionXmlResult [xmlMatchIssue=" + xmlMatchIssue + ", xmlMissMatchIssue=" + xmlMissMatchIssue
					+ ", xmlSkippedIssue=" + xmlSkippedIssue + ", xmlValidationIssue=" + xmlValidationIssue + "]";
		}
	    
	    

}
