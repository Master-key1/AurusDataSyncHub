package com.auruspay.comparator.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ComparisonResult {
	
	@Autowired
	ComparisionXmlResult comparisionXmlResult ;
	
	@Autowired
	ComparisonJsonResult comparisonJsonResult ;

	public ComparisionXmlResult getComparisionXmlResult() {
		return comparisionXmlResult;
	}

	public void setComparisionXmlResult(ComparisionXmlResult comparisionXmlResult) {
		this.comparisionXmlResult = comparisionXmlResult;
	}

	public ComparisonJsonResult getComparisonJsonResult() {
		return comparisonJsonResult;
	}

	public void setComparisonJsonResult(ComparisonJsonResult comparisonJsonResult) {
		this.comparisonJsonResult = comparisonJsonResult;
	}
	
	

}
