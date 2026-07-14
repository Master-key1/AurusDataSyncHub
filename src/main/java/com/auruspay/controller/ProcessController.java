package com.auruspay.controller;

import com.auruspay.comparator.JsonComparator;
import com.auruspay.comparator.XmlComparator;
import com.auruspay.comparator.model.JsonRequest;
import com.auruspay.comparator.model.ComparisionXmlResult;
import com.auruspay.comparator.model.ComparisonJsonResult;
import com.auruspay.comparator.model.ComparisonResult;
import com.auruspay.comparator.model.ValidationIssue;
import com.auruspay.decryptor.AurusDecryptor;
import com.auruspay.dto.ExceptionResponse;
import com.auruspay.dto.ProcessRequest;
import com.auruspay.dto.UserInput;
import com.auruspay.service.JsonDataAddService;
import com.auruspay.service.TransactionLookupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Transaction APIs", description = "Process, Lookup and Compare transactions")
@RestController
public class ProcessController {

    private static final Logger logger =
            LoggerFactory.getLogger(ProcessController.class);

    private final JsonDataAddService jsonDataAddService;
    private final TransactionLookupService lookupService;
    private final AurusDecryptor aurusDecryptor;
    private final XmlComparator xmlComparator;
    private final JsonComparator jsonComparator;

    public ProcessController(
            JsonDataAddService jsonDataAddService,
            TransactionLookupService lookupService,
            AurusDecryptor aurusDecryptor,
            XmlComparator xmlComparator,
            JsonComparator jsonComparator) {

        this.jsonDataAddService = jsonDataAddService;
        this.lookupService = lookupService;
        this.aurusDecryptor = aurusDecryptor;
        this.xmlComparator = xmlComparator;
        this.jsonComparator = jsonComparator;
    }

    @PostMapping(value = "/process")
    public ResponseEntity<String> processAndSave(@RequestBody ProcessRequest request) {
        try {
            logger.info("Received transaction save request");

            if (request == null) {
                logger.warn("processAndSave called with null request");
                return ResponseEntity.badRequest().body("Request body is required");
            }

            String txnId = jsonDataAddService.saveData(request);
            return ResponseEntity.ok(txnId);

        } catch (Exception e) {
            logger.error("Error while saving transaction", e);
            return ResponseEntity.internalServerError().body("Failed to save transaction");
        }
    }

    // ================= DECRYPT =================
    @PostMapping("/decryptor")
    public ResponseEntity<String> decrypt(@RequestBody String encryptedData) {

        logger.info("Decrypt request received");

        if (encryptedData == null || encryptedData.trim().isEmpty()) {
            logger.warn("Empty encrypted data received");
            return ResponseEntity.badRequest().body("Input data is required");
        }

        try {
            String decryptedData = aurusDecryptor.decryptor(encryptedData);
            logger.info("Decryption completed successfully");
            return ResponseEntity.ok(decryptedData);

        } catch (Exception e) {
            logger.error("Decryption failed", e);
            return ResponseEntity.internalServerError().body("Decryption failed: " + e.getMessage());
        }
    }

    @GetMapping("/test")
    public String openForms() {
        logger.info("Test API called");
        return "Sucessfully tested...!";
    }

    // ================= SMART COMPARE (currently disabled - see @PostMapping above) =================
    // @PostMapping(value = "/json/compare", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> smartCompare(@RequestBody JsonRequest request) {

        if (request == null || request.getApprovedJson() == null || request.getDeclinedJson() == null) {
            logger.warn("smartCompare called with missing fields | request={}", request);
            return ResponseEntity.badRequest()
                    .body(buildExceptionResponse("INVALID_REQUEST", "approvedJson and declinedJson are required"));
        }

        String approved;
        String declined;
        try {
            approved = aurusDecryptor.decryptor(request.getApprovedJson());
            declined = aurusDecryptor.decryptor(request.getDeclinedJson());
        } catch (Exception e) {
            logger.error("Decryption failed in smartCompare | Reason: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(buildExceptionResponse("DECRYPTION_FAILED", e.getMessage()));
        }

        try {
            ComparisonJsonResult result = jsonComparator.compare(declined, approved);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("smartCompare comparison failed | Reason: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(buildExceptionResponse("COMPARISON_FAILED", e.getMessage()));
        }
    }
/*
    // ================= MAIN COMPARE API (returns ValidationIssue) =================
    @PostMapping(value = "/validationissue")
    public ResponseEntity<?> validationIssue(@Valid @RequestBody UserInput request) {

        logger.info("Compare request received: {}", request);

        if (request == null) {
            logger.error("Compare request is null");
            return ResponseEntity.badRequest()
                    .body(buildExceptionResponse("INVALID_REQUEST", "Request body is null"));
        }
        
        

        ProcessRequest declinedRequest = new ProcessRequest();
        String cctRequest;
        String procRequest;

        try {
            cctRequest = request.getCctRequest() != null
                    ? aurusDecryptor.decryptor(request.getCctRequest())
                    : null;

            procRequest = request.getProcessorRequest() != null
                    ? aurusDecryptor.decryptor(request.getProcessorRequest())
                    : null;
        } catch (Exception e) {
            logger.error("Decryption failed for request | Reason: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(buildExceptionResponse("DECRYPTION_FAILED", e.getMessage()));
        }

        declinedRequest.setCctRequest(cctRequest);
        declinedRequest.setProcessorRequest(procRequest);

        logger.info("CCT Request : {}", cctRequest);
        logger.info("Processor Request : {}", procRequest);

        if (cctRequest == null && procRequest == null) {
            logger.warn("Both cctRequest and processorRequest are null after decryption | Original request: {}", request);
            return ResponseEntity.badRequest()
                    .body(buildExceptionResponse("EMPTY_REQUEST", "Both cctRequest and processorRequest are null"));
        }

        // ================= FETCH APPROVED DATA =================
        ProcessRequest approvedRequest;
        try {
            approvedRequest = lookupService.lookupTransaction(declinedRequest);
        } catch (Exception e) {
            logger.error("lookupTransaction threw an exception | Reason: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(buildExceptionResponse("LOOKUP_FAILED", e.getMessage()));
        }

        if (approvedRequest == null) {
            logger.warn("Approved transaction not found | DeclinedCctRequest: {}", cctRequest);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(buildExceptionResponse("APPROVED_TXN_NOT_FOUND",
                            "No approved transaction found for the given declined request"));
        }

        logger.info("Approved transaction found : {}", approvedRequest.getProcessorRequest());

        // NOTE: these are request-local now, not shared singleton beans.
        ComparisonResult comparisonResult = new ComparisonResult();
        ValidationIssue validationIssue = new ValidationIssue();

        ComparisionXmlResult xmlComparedData = null;
        ComparisonJsonResult jsonComparedData = null;

        // ================= XML COMPARISON =================
        if (declinedRequest.getProcessorRequest() != null) {
            String approvedXml = approvedRequest.getProcessorRequest();
            String declinedXml = declinedRequest.getProcessorRequest();

            if (approvedXml == null) {
                logger.warn("Skipping XML comparison — approvedXml is null");
            }

            try {
                xmlComparedData = (approvedXml != null)
                        ? xmlComparator.getXmlComparator(approvedXml, declinedXml)
                        : new ComparisionXmlResult();
            } catch (Exception e) {
                logger.error("XML comparison failed | Reason: {}", e.getMessage(), e);
                return ResponseEntity.internalServerError()
                        .body(buildExceptionResponse("XML_COMPARISON_FAILED", e.getMessage()));
            }
        } else {
            xmlComparedData = new ComparisionXmlResult();
        }

        // ================= JSON/CCT COMPARISON =================
        if (declinedRequest.getCctRequest() != null) {
            String approvedJson = approvedRequest.getCctRequest();
            String declinedJson = declinedRequest.getCctRequest();

            if (approvedJson == null) {
                logger.warn("Skipping JSON comparison — approvedJson is null");
            }

            try {
                jsonComparedData = (approvedJson != null)
                        ? jsonComparator.compare(declinedJson, approvedJson)
                        : new ComparisonJsonResult();
            } catch (Exception e) {
                logger.error("JSON comparison failed | Reason: {}", e.getMessage(), e);
                return ResponseEntity.internalServerError()
                        .body(buildExceptionResponse("JSON_COMPARISON_FAILED", e.getMessage()));
            }
        } else {
            jsonComparedData = new ComparisonJsonResult();
        }

        comparisonResult.setComparisionXmlResult(xmlComparedData);
        comparisonResult.setComparisonJsonResult(jsonComparedData);

        validationIssue.setProcessorRequestValidationIssue(
                comparisonResult.getComparisionXmlResult().getXmlValidationIssue());
        validationIssue.setAurusRequestValidationIssue(
                comparisonResult.getComparisonJsonResult().getValidationIssue());

        logger.info("Compare API completed successfully");
        return ResponseEntity.ok(validationIssue);
    }
    */
    
    @PostMapping(value = "/validationissue")
    public ResponseEntity<?> validationIssue(@Valid @RequestBody UserInput request) {

        logger.info("Compare request received: {}", request);

        if (request == null) {
            logger.error("Compare request is null");
            return ResponseEntity.badRequest()
                    .body(buildExceptionResponse("INVALID_REQUEST", "Request body is null"));
        }

        ProcessRequest declinedRequest = new ProcessRequest();

        String cctRequest = null;
        String procRequest = null;

        try {

            // CCT Request - mandatory
            if (request.getCctRequest() != null 
                    && !request.getCctRequest().isBlank()) {

                cctRequest = aurusDecryptor.decryptor(request.getCctRequest());

            } else {
                return ResponseEntity.badRequest()
                        .body(buildExceptionResponse(
                                "INVALID_REQUEST",
                                "cctRequest cannot be empty"
                        ));
            }


            // Processor Request - optional
            // Allows: null, "", "   "
            if (request.getProcessorRequest() != null
                    && !request.getProcessorRequest().isBlank()) {

                procRequest = aurusDecryptor.decryptor(request.getProcessorRequest());

            } else {
                logger.info("processorRequest is empty, skipping decryption");
                procRequest = null;
            }


        } catch (Exception e) {
            logger.error("Decryption failed for request | Reason: {}", e.getMessage(), e);

            return ResponseEntity.badRequest()
                    .body(buildExceptionResponse(
                            "DECRYPTION_FAILED",
                            e.getMessage()
                    ));
        }


        declinedRequest.setCctRequest(cctRequest);
        declinedRequest.setProcessorRequest(procRequest);

        logger.info("CCT Request : {}", cctRequest);
        logger.info("Processor Request : {}", procRequest);


        if (cctRequest == null && procRequest == null) {
            logger.warn("Both cctRequest and processorRequest are null after decryption");

            return ResponseEntity.badRequest()
                    .body(buildExceptionResponse(
                            "EMPTY_REQUEST",
                            "Both cctRequest and processorRequest are null"
                    ));
        }


        // ================= FETCH APPROVED DATA =================
        ProcessRequest approvedRequest;

        try {
            approvedRequest = lookupService.lookupTransaction(declinedRequest);

        } catch (Exception e) {
            logger.error("lookupTransaction threw an exception | Reason: {}", e.getMessage(), e);

            return ResponseEntity.internalServerError()
                    .body(buildExceptionResponse(
                            "LOOKUP_FAILED",
                            e.getMessage()
                    ));
        }


        if (approvedRequest == null) {
            logger.warn("Approved transaction not found");

            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(buildExceptionResponse(
                            "APPROVED_TXN_NOT_FOUND",
                            "No approved transaction found for the given declined request"
                    ));
        }


        ComparisonResult comparisonResult = new ComparisonResult();
        ValidationIssue validationIssue = new ValidationIssue();

        ComparisionXmlResult xmlComparedData;
        ComparisonJsonResult jsonComparedData;


        // ================= XML COMPARISON =================
        if (declinedRequest.getProcessorRequest() != null) {

            String approvedXml = approvedRequest.getProcessorRequest();
            String declinedXml = declinedRequest.getProcessorRequest();

            try {
                xmlComparedData = (approvedXml != null)
                        ? xmlComparator.getXmlComparator(approvedXml, declinedXml)
                        : new ComparisionXmlResult();

            } catch (Exception e) {
                logger.error("XML comparison failed | Reason: {}", e.getMessage(), e);

                return ResponseEntity.internalServerError()
                        .body(buildExceptionResponse(
                                "XML_COMPARISON_FAILED",
                                e.getMessage()
                        ));
            }

        } else {
            xmlComparedData = new ComparisionXmlResult();
        }


        // ================= JSON/CCT COMPARISON =================
        if (declinedRequest.getCctRequest() != null) {

            String approvedJson = approvedRequest.getCctRequest();
            String declinedJson = declinedRequest.getCctRequest();

            try {
                jsonComparedData = (approvedJson != null)
                        ? jsonComparator.compare(declinedJson, approvedJson)
                        : new ComparisonJsonResult();

            } catch (Exception e) {
                logger.error("JSON comparison failed | Reason: {}", e.getMessage(), e);

                return ResponseEntity.internalServerError()
                        .body(buildExceptionResponse(
                                "JSON_COMPARISON_FAILED",
                                e.getMessage()
                        ));
            }

        } else {
            jsonComparedData = new ComparisonJsonResult();
        }


        comparisonResult.setComparisionXmlResult(xmlComparedData);
        comparisonResult.setComparisonJsonResult(jsonComparedData);


        validationIssue.setProcessorRequestValidationIssue(
                comparisonResult.getComparisionXmlResult().getXmlValidationIssue()
        );

        validationIssue.setAurusRequestValidationIssue(
                comparisonResult.getComparisonJsonResult().getValidationIssue()
        );


        logger.info("Compare API completed successfully");

        return ResponseEntity.ok(validationIssue);
    }
    
    @PostMapping(value = "/xcompare")
    public ResponseEntity<?> compare(@Valid @RequestBody UserInput request) {

        logger.info("Compare request received: {}", request);

        if (request == null) {
            logger.error("Compare request is null");
            return ResponseEntity.badRequest()
                    .body(buildExceptionResponse("INVALID_REQUEST", "Request body is null"));
        }

        ProcessRequest declinedRequest = new ProcessRequest();

        String cctRequest = null;
        String procRequest = null;

        try {

            // CCT Request - mandatory
            if (request.getCctRequest() != null 
                    && !request.getCctRequest().isBlank()) {

                cctRequest = aurusDecryptor.decryptor(request.getCctRequest());

            } else {
                return ResponseEntity.badRequest()
                        .body(buildExceptionResponse(
                                "INVALID_REQUEST",
                                "cctRequest cannot be empty"
                        ));
            }


            // Processor Request - optional
            // Allows: null, "", "   "
            if (request.getProcessorRequest() != null
                    && !request.getProcessorRequest().isBlank()) {

                procRequest = aurusDecryptor.decryptor(request.getProcessorRequest());

            } else {
                logger.info("processorRequest is empty, skipping decryption");
                procRequest = null;
            }


        } catch (Exception e) {
            logger.error("Decryption failed for request | Reason: {}", e.getMessage(), e);

            return ResponseEntity.badRequest()
                    .body(buildExceptionResponse(
                            "DECRYPTION_FAILED",
                            e.getMessage()
                    ));
        }


        declinedRequest.setCctRequest(cctRequest);
        declinedRequest.setProcessorRequest(procRequest);

        logger.info("CCT Request : {}", cctRequest);
        logger.info("Processor Request : {}", procRequest);


        if (cctRequest == null && procRequest == null) {
            logger.warn("Both cctRequest and processorRequest are null after decryption");

            return ResponseEntity.badRequest()
                    .body(buildExceptionResponse(
                            "EMPTY_REQUEST",
                            "Both cctRequest and processorRequest are null"
                    ));
        }


        // ================= FETCH APPROVED DATA =================
        ProcessRequest approvedRequest;

        try {
            approvedRequest = lookupService.lookupTransaction(declinedRequest);

        } catch (Exception e) {
            logger.error("lookupTransaction threw an exception | Reason: {}", e.getMessage(), e);

            return ResponseEntity.internalServerError()
                    .body(buildExceptionResponse(
                            "LOOKUP_FAILED",
                            e.getMessage()
                    ));
        }


        if (approvedRequest == null) {
            logger.warn("Approved transaction not found");

            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(buildExceptionResponse(
                            "APPROVED_TXN_NOT_FOUND",
                            "No approved transaction found for the given declined request"
                    ));
        }


        ComparisonResult comparisonResult = new ComparisonResult();
        ValidationIssue validationIssue = new ValidationIssue();

        ComparisionXmlResult xmlComparedData;
        ComparisonJsonResult jsonComparedData;


        // ================= XML COMPARISON =================
        if (declinedRequest.getProcessorRequest() != null) {

            String approvedXml = approvedRequest.getProcessorRequest();
            String declinedXml = declinedRequest.getProcessorRequest();

            try {
                xmlComparedData = (approvedXml != null)
                        ? xmlComparator.getXmlComparator(approvedXml, declinedXml)
                        : new ComparisionXmlResult();

            } catch (Exception e) {
                logger.error("XML comparison failed | Reason: {}", e.getMessage(), e);

                return ResponseEntity.internalServerError()
                        .body(buildExceptionResponse(
                                "XML_COMPARISON_FAILED",
                                e.getMessage()
                        ));
            }

        } else {
            xmlComparedData = new ComparisionXmlResult();
        }


        // ================= JSON/CCT COMPARISON =================
        if (declinedRequest.getCctRequest() != null) {

            String approvedJson = approvedRequest.getCctRequest();
            String declinedJson = declinedRequest.getCctRequest();

            try {
                jsonComparedData = (approvedJson != null)
                        ? jsonComparator.compare(declinedJson, approvedJson)
                        : new ComparisonJsonResult();

            } catch (Exception e) {
                logger.error("JSON comparison failed | Reason: {}", e.getMessage(), e);

                return ResponseEntity.internalServerError()
                        .body(buildExceptionResponse(
                                "JSON_COMPARISON_FAILED",
                                e.getMessage()
                        ));
            }

        } else {
            jsonComparedData = new ComparisonJsonResult();
        }


        comparisonResult.setComparisionXmlResult(xmlComparedData);
        comparisonResult.setComparisonJsonResult(jsonComparedData);


        validationIssue.setProcessorRequestValidationIssue(
                comparisonResult.getComparisionXmlResult().getXmlValidationIssue()
        );

        validationIssue.setAurusRequestValidationIssue(
                comparisonResult.getComparisonJsonResult().getValidationIssue()
        );


        logger.info("Compare API completed successfully");

        return ResponseEntity.ok(validationIssue);
    }

    private ExceptionResponse buildExceptionResponse(String code, String message) {
        ExceptionResponse exceptionResponse = new ExceptionResponse();
        exceptionResponse.setErrorCode(code);
        exceptionResponse.setErrorMessage(message);
        return exceptionResponse;
    }
}