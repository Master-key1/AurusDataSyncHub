package com.auruspay.chase.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.auruspay.chase.Iso8583ParseResult;
import com.auruspay.chase.validation.Iso8583FieldValidationResult;
import com.auruspay.chase.validation.Iso8583FieldValidator;

@Service
public class ChaseValidationService {
	
	@Autowired
	private Iso8583FieldValidator fieldValidator ;
	
	
	public List<Iso8583FieldValidationResult> validation(Iso8583ParseResult approvedRequestParse , Iso8583ParseResult declinedRequestParse) {

		Map<Integer, String> approvedRequest = approvedRequestParse.getFields() ;
		Map<Integer, String> declinedRequest = declinedRequestParse.getFields() ;
		
		 return fieldValidator.compareTransactions(approvedRequest, declinedRequest);
		
		
	}

}
