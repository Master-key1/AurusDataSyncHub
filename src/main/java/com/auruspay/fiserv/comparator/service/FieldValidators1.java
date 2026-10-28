package com.auruspay.comparator.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.auruspay.comparator.model.ValidationResults;
import com.auruspay.service.ServiceProvider;

@Component
public class FieldValidators1 {
	
	@Autowired
	 private ServiceProvider serviceProvider ;

	public ValidationResults validate(String field1, String valA, String field2, String valD) {

	    if (valA == null || valA.isBlank() || "EMPTY VALUE".equals(valA) || "TAG MISSING".equals(valA)
	            || valD == null || valD.isBlank() || "EMPTY VALUE".equals(valD) || "TAG MISSING".equals(valD)) {

	        String reason;

	        if ((valA == null || valA.isBlank() || "EMPTY VALUE".equals(valA) || "TAG MISSING".equals(valA))
	                && (valD == null || valD.isBlank() || "EMPTY VALUE".equals(valD) || "TAG MISSING".equals(valD))) {
	            reason = "Both valueA and valueD are missing/empty";
	        } else if (valA == null || valA.isBlank() || "EMPTY VALUE".equals(valA) || "TAG MISSING".equals(valA)) {
	            reason = "valueA is missing/empty";
	        } else {
	            reason = "valueD is missing/empty";
	        }

	        return new ValidationResults(
	                field1,
	                valA,
	                valD,
	                "INVALID",
	                "REQUIRED",
	                reason
	        );
	    }

	    valA = valA.trim();
	    valD = valD.trim();

	    return switch (field1) {

		case "DID" -> serviceProvider.getTagValidationService().validateDID(field1, valA, valD);
		case "App" -> serviceProvider.getTagValidationService().validateApp(field1, valA, valD);
		case "Auth" -> serviceProvider.getTagValidationService().validateAuth(field1, valA, valD, serviceProvider);
		case "ClientRef" -> serviceProvider.getTagValidationService().validateClientRef(field1, valA, valD);
		case "PymtType" -> serviceProvider.getTagValidationService().validatePymtType(field1, valA, valD);
		case "TxnType" -> serviceProvider.getTagValidationService().validateTxnType(field1, valA, valD);
		case "LocalDateTime", "TrnmsnDateTime" -> serviceProvider.getTagValidationService().validateDateTime(field1, valA, valD);
		case "STAN" -> serviceProvider.getTagValidationService().validateStan(field1, valA, valD);
		case "POSEntryMode" -> serviceProvider.getTagValidationService().validatePOSEntryMode(field1, valA, valD);
		case "TxnAmt" -> serviceProvider.getTagValidationService().validateTxnAmt(field1, valA, valD);
		case "MerchEcho" -> serviceProvider.getTagValidationService().validateMerchEcho(field1, valA, valD);
		
	        default ->
	                new ValidationResults(
	                        field1,
	                        valA,
	                        valD,
	                        "VALID",
	                        "N/A",
	                        "No validation rule configured"
	                );
	    };
	}
	
	

}
