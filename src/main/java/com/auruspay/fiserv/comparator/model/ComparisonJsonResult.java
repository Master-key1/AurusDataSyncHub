package com.auruspay.comparator.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class ComparisonJsonResult {

 //   private List<?> matchIssue = new ArrayList<>();
    private List<?> missMatchIssue = new ArrayList<>();
//    private List<?> skippedIssue = new ArrayList<>();
    private List<?> validationIssue = new ArrayList<>();

   

    public ComparisonJsonResult() {
    }


	public List<?> getMissMatchIssue() {
		return missMatchIssue;
	}

	public void setMissMatchIssue(List<?> missMatchIssue) {
		this.missMatchIssue = missMatchIssue;
	}

	
	public List<?> getValidationIssue() {
		return validationIssue;
	}

	public void setValidationIssue(List<?> validationIssue) {
		this.validationIssue = validationIssue;
	}

	
  

	
}