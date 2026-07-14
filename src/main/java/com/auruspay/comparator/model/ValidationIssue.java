package com.auruspay.comparator.model;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class ValidationIssue {
	
	 List<Map<String, String>> ProcessorRequestValidationIssue ;
	 List<IssueDetail> AurusRequestValidationIssue ;
	 
	 public List<Map<String, String>> getProcessorRequestValidationIssue() {
		 return ProcessorRequestValidationIssue;
	 }
	 public void setProcessorRequestValidationIssue(List<Map<String, String>> processorRequestValidationIssue) {
		 ProcessorRequestValidationIssue = processorRequestValidationIssue;
	 }
	 public List<IssueDetail> getAurusRequestValidationIssue() {
		 return AurusRequestValidationIssue;
	 }
	 public void setAurusRequestValidationIssue(List<IssueDetail> aurusRequestValidationIssue) {
		 AurusRequestValidationIssue = aurusRequestValidationIssue;
	 }
	
	 
	 

	 
}
