package com.auruspay.comparator.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class ComparisonJsonResult {

    private List<IssueDetail> matchIssue = new ArrayList<>();
    private List<IssueDetail> missMatchIssue = new ArrayList<>();
    private List<IssueDetail> skippedIssue = new ArrayList<>();
    private List<IssueDetail> validationIssue = new ArrayList<>();

   

    public ComparisonJsonResult() {
    }

	public List<IssueDetail> getMatchIssue() {
		return matchIssue;
	}

	public void setMatchIssue(List<IssueDetail> matchIssue) {
		this.matchIssue = matchIssue;
	}

	public List<IssueDetail> getMissMatchIssue() {
		return missMatchIssue;
	}

	public void setMissMatchIssue(List<IssueDetail> missMatchIssue) {
		this.missMatchIssue = missMatchIssue;
	}

	public List<IssueDetail> getSkippedIssue() {
		return skippedIssue;
	}

	public void setSkippedIssue(List<IssueDetail> skippedIssue) {
		this.skippedIssue = skippedIssue;
	}

	public List<IssueDetail> getValidationIssue() {
		return validationIssue;
	}

	public void setValidationIssue(List<IssueDetail> validationIssue) {
		this.validationIssue = validationIssue;
	}

	@Override
	public String toString() {
		return "ComparisonJsonResult [matchIssue=" + matchIssue + ", missMatchIssue=" + missMatchIssue
				+ ", skippedIssue=" + skippedIssue + ", validationIssue=" + validationIssue + "]";
	}

	
  

	
}