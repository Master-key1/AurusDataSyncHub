package com.auruspay.comparator.model;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class ValidationIssue {
	
	private String requestKey ;
	private List<Map<String, String>> ProcessorRequestValidationIssue ;
	private List<?> AurusRequestValidationIssue ;
	
	 
	 public String getRequestKey() {
		return requestKey;
	}
	public void setRequestKey(String requestKey) {
		this.requestKey = requestKey;
	}
	 public List<Map<String, String>> getProcessorRequestValidationIssue() {
		 return ProcessorRequestValidationIssue;
	 }
	 public void setProcessorRequestValidationIssue(List<Map<String, String>> processorRequestValidationIssue) {
		 ProcessorRequestValidationIssue = processorRequestValidationIssue;
	 }
	 public List<?> getAurusRequestValidationIssue() {
		 return AurusRequestValidationIssue;
	 }
	 public void setAurusRequestValidationIssue(List<?> aurusRequestValidationIssue) {
		 AurusRequestValidationIssue = aurusRequestValidationIssue;
	 }
}
