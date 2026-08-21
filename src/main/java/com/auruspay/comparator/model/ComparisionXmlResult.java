package com.auruspay.comparator.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class ComparisionXmlResult {

//	private List<Map<String, String>> xmlMatchIssue = new ArrayList<>();
	private List<Map<String, String>> xmlMissMatchIssue = new ArrayList<>();
//	private List<Map<String, String>> xmlSkippedIssue = new ArrayList<>();
	private List<Map<String, String>> xmlValidationIssue = new ArrayList<>();
	//String summary;

	// EMV tag-by-tag comparison, keyed by tag id (e.g. "9C", "9F02")
	private Map<String, EMVComparisonResult> emvTagComparison = new LinkedHashMap<>();

	// Roll-up verdict: issue found or not, plus counts per category
//	private EmvValidationSummary emvValidationSummary;

	// Markdown-formatted "Potential Root Cause" / "Configuration Differences" report
//	private String emvRootCauseReport;

	public List<Map<String, String>> getXmlMissMatchIssue() {
		return xmlMissMatchIssue;
	}

	public void setXmlMissMatchIssue(List<Map<String, String>> xmlMissMatchIssue) {
		this.xmlMissMatchIssue = xmlMissMatchIssue;
	}

	
	public List<Map<String, String>> getXmlValidationIssue() {
		return xmlValidationIssue;
	}

	public void setXmlValidationIssue(List<Map<String, String>> xmlValidationIssue) {
		this.xmlValidationIssue = xmlValidationIssue;
	}

	public Map<String, EMVComparisonResult> getEmvTagComparison() {
		return emvTagComparison;
	}

	public void setEmvTagComparison(Map<String, EMVComparisonResult> emvTagComparison) {
		this.emvTagComparison = emvTagComparison;
	}


	
}