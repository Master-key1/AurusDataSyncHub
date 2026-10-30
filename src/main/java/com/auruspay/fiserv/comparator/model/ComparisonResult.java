package com.auruspay.fiserv.comparator.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ComparisonResult {
	
	@Autowired
	ComparisionXmlResult comparisionXmlResult ;
	
	private String lookupKey ;
	@Autowired
	CompareCctResult CompareCctResult ;

	public ComparisionXmlResult getComparisionXmlResult() {
		return comparisionXmlResult;
	}

	public void setComparisionXmlResult(ComparisionXmlResult comparisionXmlResult) {
		this.comparisionXmlResult = comparisionXmlResult;
	}

	public CompareCctResult getCompareCctResult() {
		return CompareCctResult;
	}

	public void setCompareCctResult(CompareCctResult CompareCctResult) {
		this.CompareCctResult = CompareCctResult;
	}

	public String getLookupKey() {
		return lookupKey;
	}

	public void setLookupKey(String lookupKey) {
		this.lookupKey = lookupKey;
	}
	
	

}
