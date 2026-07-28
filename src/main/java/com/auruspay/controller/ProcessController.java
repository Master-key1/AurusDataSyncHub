package com.auruspay.controller;

import com.auruspay.comparator.JsonComparator;
import com.auruspay.comparator.XmlComparator;
import com.auruspay.comparator.model.ComparisionXmlResult;
import com.auruspay.comparator.model.ComparisonJsonResult;
import com.auruspay.comparator.model.ComparisonResult;
import com.auruspay.comparator.model.ValidationIssue;
import com.auruspay.decryptor.AurusDecryptor;
import com.auruspay.dto.CustomResponse;
import com.auruspay.dto.ExceptionResponse;
import com.auruspay.dto.ProcessRequest;
import com.auruspay.dto.TransactionLookupResponse;
import com.auruspay.dto.UserInput;
import com.auruspay.logservice.exception.BlankRequestException;
import com.auruspay.logservice.exception.NoDataFoundException;
import com.auruspay.service.JsonDataAddService;
import com.auruspay.service.TransactionLookupService;
import com.auruspay.util.ExtractMultipleKeywords;
import com.auruspay.util.FileReadData;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST endpoints for saving, decrypting, and comparing Aurus transactions.
 */
@Tag(name = "Transaction APIs", description = "Process, Lookup and Compare transactions")
@RestController
public class ProcessController {

    private static final Logger log = LoggerFactory.getLogger(ProcessController.class);

    // Keys used by the flat-file extractor to tag interesting lines. Centralised
    // here instead of being repeated as inline string literals across methods.
    private static final String KEY_CCT_REQUEST = "[STPL-GRAY-STREAM]-AURUSPAY ENCRYPTED REQUEST :";
    private static final String KEY_CCT_RESPONSE = "[STPL-GRAY-STREAM]-AURUSPAY ENCRYPTED RESPONSE :";
    private static final String KEY_PROCESSOR_REQUEST_1 = "[STPL-GRAY-STREAM]- PROCESSOR REQUEST :";
    private static final String KEY_PROCESSOR_REQUEST_2 = "[STPL-GRAY-STREAM]- REQUEST :";
    private static final String KEY_PROCESSOR_RESPONSE_1 = "[STPL-GRAY-STREAM]-FINAL RESPONSE :";
    private static final String KEY_PROCESSOR_RESPONSE_2 = "PROCESSOR RESPONSE FOR FIRST DATA PERSISTENT :";
    private static final String KEY_PROCESSOR_ID = "PROCESSOR TERMINAL DETAILS";

    private final JsonDataAddService jsonDataAddService;
    private final TransactionLookupService transactionLookupService;
    private final AurusDecryptor aurusDecryptor;
    private final XmlComparator xmlComparator;
    private final JsonComparator jsonComparator;
    private final FileReadData fileReadData;
    private final ExtractMultipleKeywords extractMultipleKeywords;

    @Value("${aurus.file.import-directory:C:/data/combine}")
    private String fileImportDirectory;

    public ProcessController(JsonDataAddService jsonDataAddService,
                              TransactionLookupService transactionLookupService,
                              AurusDecryptor aurusDecryptor,
                              XmlComparator xmlComparator,
                              JsonComparator jsonComparator,
                              FileReadData fileReadData,
                              ExtractMultipleKeywords extractMultipleKeywords) {
        this.jsonDataAddService = jsonDataAddService;
        this.transactionLookupService = transactionLookupService;
        this.aurusDecryptor = aurusDecryptor;
        this.xmlComparator = xmlComparator;
        this.jsonComparator = jsonComparator;
        this.fileReadData = fileReadData;
        this.extractMultipleKeywords = extractMultipleKeywords;
    }

    // ================= SAVE =================

    @PostMapping("/process")
    public ResponseEntity<String> saveTransaction(@RequestBody ProcessRequest request) {
        if (request == null) {
            log.warn("saveTransaction called with null request body");
            return ResponseEntity.badRequest().body("Request body is required");
        }

        try {
            String transactionId = jsonDataAddService.saveData(request);
            log.info("Transaction saved successfully. txnId={}", transactionId);
            return ResponseEntity.ok(transactionId);
        } catch (Exception e) {
            log.error("Error while saving transaction", e);
            return ResponseEntity.internalServerError().body("Failed to save transaction");
        }
    }

    // ================= DECRYPT =================

    @PostMapping("/decryptor")
    public ResponseEntity<String> decrypt(@RequestBody String encryptedData) {
        if (encryptedData == null || encryptedData.trim().isEmpty()) {
            log.warn("Empty encrypted data received");
            return ResponseEntity.badRequest().body("Input data is required");
        }

        try {
            String decryptedData = aurusDecryptor.decryptor(encryptedData);
            log.info("Decryption completed successfully");
            return ResponseEntity.ok(decryptedData);
        } catch (Exception e) {
            log.error("Decryption failed", e);
            return ResponseEntity.internalServerError().body("Decryption failed: " + e.getMessage());
        }
    }

    // ================= BULK IMPORT FROM DIRECTORY =================

    /**
     * Reads every .txt file in the configured import directory, extracts the
     * Aurus/processor request-response fields from each, and persists one
     * transaction per file. Renamed from the misleading "submitForms"/"GET
     * /submit" (a state-changing action must not be a GET).
     */
    @PostMapping("/transactions/import")
    public ResponseEntity<?> importTransactionsFromDirectory() {
        try {
            log.info("Starting bulk transaction import from directory: {}", fileImportDirectory);

            List<String> filePaths = fileReadData.listFilesInDirectory(fileImportDirectory, ".txt");
            if (filePaths.isEmpty()) {
                log.info("No .txt files found to process in {}", fileImportDirectory);
                return ResponseEntity.ok(Map.of("directory", fileImportDirectory, "filesProcessed", 0));
            }

            Map<String, Map<String, List<String>>> extractedByFile = fileReadData.extractDataForFiles(filePaths);
            if (extractedByFile.isEmpty()) {
                log.info("No matching data found in any file");
                return ResponseEntity.ok("No matching data found");
            }

            Map<String, String> txnIdToFilePath = new HashMap<>();
            extractedByFile.forEach((filePath, extractedFields) ->
                    importSingleFile(filePath, extractedFields, txnIdToFilePath));

            log.info("Bulk import complete. Transactions saved: {}", txnIdToFilePath.size());
            return ResponseEntity.ok(txnIdToFilePath);

        } catch (Exception e) {
            log.error("Error during bulk transaction import", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to save transaction");
        }
    }

    private void importSingleFile(String filePath,
                                   Map<String, List<String>> extractedFields,
                                   Map<String, String> txnIdToFilePath) {
        log.info("========== FILE : {} ==========", filePath);

        if (extractedFields == null || extractedFields.isEmpty()) {
            log.info("No matching data found for file: {}", filePath);
            return;
        }

        ProcessRequest request = buildProcessRequest(extractedFields);
        log.info("Built request from file {}: {}", filePath, request);

        try {
            String txnId = jsonDataAddService.saveData(request);
            if (txnId != null && !txnId.trim().isEmpty()) {
                log.info("Transaction saved successfully. txnId={}, file={}", txnId, filePath);
                txnIdToFilePath.put(txnId, filePath);
            } else {
                log.warn("Transaction ID is null/empty. File skipped from response map: {}", filePath);
            }
        } catch (Exception e) {
            log.error("Error while saving data for file: {}", filePath, e);
        }
    }

    /**
     * Maps the raw extracted key/value-list data (as produced by the flat-file
     * extractors) onto a ProcessRequest, taking the first extracted value for
     * each recognised key.
     */
    private ProcessRequest buildProcessRequest(Map<String, List<String>> extractedFields) {
        ProcessRequest request = new ProcessRequest();

        extractedFields.forEach((key, values) -> {
            String value = firstValue(values);

            if (key.contains(KEY_CCT_REQUEST)) {
                request.setCctRequest(value);
            }
            if (key.contains(KEY_CCT_RESPONSE)) {
                request.setCctResponse(value);
            }
            if (key.contains(KEY_PROCESSOR_REQUEST_1) || key.contains(KEY_PROCESSOR_REQUEST_2)) {
                request.setProcessorRequest(value);
            }
            if (key.contains(KEY_PROCESSOR_RESPONSE_1) || key.contains(KEY_PROCESSOR_RESPONSE_2)) {
                request.setProcessorResponse(value);
            }
            if (key.contains(KEY_PROCESSOR_ID)) {
                request.setProcessorId(value);
            }
        });

        return request;
    }

    // ================= SINGLE EXTRACTED TRANSACTION =================

    /**
     * Extracts a single transaction's fields (via ExtractMultipleKeywords) and
     * persists it. Renamed from "openForms"/"GET /test" — a test-sounding GET
     * endpoint that mutates state should not exist in a production controller.
     */
    @PostMapping("/transactions/import-single")
    public ResponseEntity<String> importSingleExtractedTransaction() {
        try {
            Map<String, List<String>> extractedFields = extractMultipleKeywords.extractData();
            log.info("Extracted {} keys from source", extractedFields.size());

            if (extractedFields.isEmpty()) {
                log.info("No data found to extract");
            } else if (log.isDebugEnabled()) {
                extractedFields.forEach((key, values) -> log.debug("{} -> {}", key, values));
            }

            ProcessRequest request = buildProcessRequest(extractedFields);
            log.info("Built request: {}", request);

            String txnId = jsonDataAddService.saveData(request);
            log.info("Transaction saved successfully. txnId={}", txnId);
            return ResponseEntity.ok(txnId);

        } catch (Exception e) {
            log.error("Error while saving transaction", e);
            return ResponseEntity.internalServerError().body("Failed to save transaction");
        }
    }

    private String firstValue(List<String> values) {
        return (values == null || values.isEmpty()) ? null : values.get(0);
    }

    // ================= VALIDATION / COMPARISON =================

    @PostMapping(value = "/validationissue")
    public ResponseEntity<?> validateTransaction(@RequestBody UserInput request) {

        log.info("Validation request received: {}", request);

        if (request == null) {
            log.error("Validation request is null");
            return ResponseEntity.ok(buildExceptionResponse("INVALID_REQUEST", "Request body is null"));
        }

        ProcessRequest declinedRequest;
        try {
            declinedRequest = decryptRequest(request);
        } catch (BlankRequestException e) {
            log.warn("cctRequest is empty");
            return ResponseEntity.ok(buildExceptionResponse("INVALID_REQUEST", "cctRequest cannot be empty"));
        } catch (Exception e) {
            log.error("Decryption failed: {}", e.getMessage(), e);
            return ResponseEntity.ok(buildExceptionResponse("DECRYPTION_FAILED", e.getMessage()));
        }

        log.info("Declined transaction | cctRequest={}, processorRequest={}",
                declinedRequest.getCctRequest(), declinedRequest.getProcessorRequest());

        // ---------- LOOKUP (direct checks, no exception thrown) ----------
        TransactionLookupResponse approvedTransaction;
        try {
            approvedTransaction = transactionLookupService.lookupTransaction(declinedRequest, request.getProcessorId());
        } catch (Exception e) {
            log.error("Unexpected error during transaction lookup: {}", e.getMessage(), e);
            return ResponseEntity.ok(
                    new ExceptionResponse("DECLINED", "LOOKUP_FAILED", e.getMessage(), null));
        }

        if (approvedTransaction == null) {
            log.warn("Transaction not found. lookupKey=null");
            return ResponseEntity.ok(
                    new ExceptionResponse("DECLINED", "NO_DATA_FOUND", "Transaction data key not found", null));
        }

        ProcessRequest approvedProcessRequest = approvedTransaction.getProcessRequest();

        if (approvedProcessRequest == null
                || (approvedProcessRequest.getCctRequest() == null && approvedProcessRequest.getProcessorRequest() == null)) {
            log.warn("Transaction not found. lookupKey={}", approvedTransaction.getLookupKey());
            return ResponseEntity.ok(
                    new ExceptionResponse("DECLINED", "NO_DATA_FOUND",
                            "Transaction data was not found against the generated key.",
                            approvedTransaction.getLookupKey()));
        }

        log.info("Approved transaction | lookupKey={}, cctRequest={}, processorRequest={}",
                approvedTransaction.getLookupKey(),
                approvedProcessRequest.getCctRequest(),
                approvedProcessRequest.getProcessorRequest());
        // ---------- END LOOKUP ----------

        ComparisonJsonResult jsonComparisonResult;
        try {
            jsonComparisonResult = compareJson(declinedRequest, approvedTransaction);
        } catch (NoDataFoundException e) {
            log.warn("Transaction data not found. lookupKey={}", e.getLookupKey());
            return ResponseEntity.ok(
                    new ExceptionResponse(e.getStatus(), e.getCode(), e.getMessage(), e.getLookupKey()));
        } catch (Exception e) {
            log.error("JSON comparison failed: {}", e.getMessage(), e);
            return ResponseEntity.ok(buildExceptionResponse("JSON_COMPARISON_FAILED", e.getMessage()));
        }

        ComparisionXmlResult xmlComparisonResult;
        try {
            xmlComparisonResult = compareXmlIfPresent(declinedRequest, approvedTransaction);
        } catch (Exception e) {
            log.error("XML comparison failed: {}", e.getMessage(), e);
            return ResponseEntity.ok(buildExceptionResponse("XML_COMPARISON_FAILED", e.getMessage()));
        }

        log.info("JSON comparison result: match={},\n mismatch={},\n validation={},\n skipped={}",
                jsonComparisonResult.getMatchIssue(), jsonComparisonResult.getMissMatchIssue(),
                jsonComparisonResult.getValidationIssue(), jsonComparisonResult.getSkippedIssue());

        log.info("XML comparison result: match={},\n mismatch={},\n validation={},\n skipped={}",
                xmlComparisonResult.getXmlMatchIssue(), xmlComparisonResult.getXmlMissMatchIssue(),
                xmlComparisonResult.getXmlValidationIssue(), xmlComparisonResult.getXmlSkippedIssue());

        ComparisonResult comparisonResult = new ComparisonResult();
        comparisonResult.setComparisonJsonResult(jsonComparisonResult);
        comparisonResult.setComparisionXmlResult(xmlComparisonResult);

        ValidationIssue validationIssue = new ValidationIssue();
        validationIssue.setRequestKey(approvedTransaction.getLookupKey());
        
        if(jsonComparisonResult.getValidationIssue() != null || xmlComparisonResult.getXmlValidationIssue() !=null) {
        validationIssue.setAurusRequestValidationIssue(jsonComparisonResult.getValidationIssue());
        validationIssue.setProcessorRequestValidationIssue(xmlComparisonResult.getXmlValidationIssue());
        }else {
        	String msg = "The transaction request has been compared with both approved and declined transaction requests, and the request format is valid.\r\n"
        			+ "\r\n"
        			+ "The transaction was successfully sent to the processor and received by them. However, it was declined at the processor's end.\r\n"
        			+ "\r\n"
        			+ "Please raise this issue with the processor for further investigation and identify the reason for the decline.\r\n"
        			+ "";
        	 return ResponseEntity.ok(buildCustomResponse( approvedTransaction.getLookupKey(), msg , "success"));
        }
        

        log.info("Validation completed successfully. lookupKey={}", approvedTransaction.getLookupKey());
        return ResponseEntity.ok(validationIssue);
    
    }

    @PostMapping(value = "/xcompare")
    public ResponseEntity<?> validateTransactions(@RequestBody UserInput request) {

        log.info("Validation request received: {}", request);

        if (request == null) {
            log.error("Validation request is null");
            return ResponseEntity.ok(buildExceptionResponse("INVALID_REQUEST", "Request body is null"));
        }

        ProcessRequest declinedRequest;
        try {
            declinedRequest = decryptRequest(request);
        } catch (BlankRequestException e) {
            log.warn("cctRequest is empty");
            return ResponseEntity.ok(buildExceptionResponse("INVALID_REQUEST", "cctRequest cannot be empty"));
        } catch (Exception e) {
            log.error("Decryption failed: {}", e.getMessage(), e);
            return ResponseEntity.ok(buildExceptionResponse("DECRYPTION_FAILED", e.getMessage()));
        }

        log.info("Declined transaction | cctRequest={}, processorRequest={}",
                declinedRequest.getCctRequest(), declinedRequest.getProcessorRequest());

        // ---------- LOOKUP (direct checks, no exception thrown) ----------
        TransactionLookupResponse approvedTransaction;
        try {
            approvedTransaction = transactionLookupService.lookupTransaction(declinedRequest, request.getProcessorId());
        } catch (Exception e) {
            log.error("Unexpected error during transaction lookup: {}", e.getMessage(), e);
            return ResponseEntity.ok(
                    new ExceptionResponse("DECLINED", "LOOKUP_FAILED", e.getMessage(), null));
        }

        if (approvedTransaction == null) {
            log.warn("Transaction not found. lookupKey=null");
            return ResponseEntity.ok(
                    new ExceptionResponse("DECLINED", "NO_DATA_FOUND", "Transaction data key not found", null));
        }

        ProcessRequest approvedProcessRequest = approvedTransaction.getProcessRequest();

        if (approvedProcessRequest == null
                || (approvedProcessRequest.getCctRequest() == null && approvedProcessRequest.getProcessorRequest() == null)) {
            log.warn("Transaction not found. lookupKey={}", approvedTransaction.getLookupKey());
            return ResponseEntity.ok(
                    new ExceptionResponse("DECLINED", "NO_DATA_FOUND",
                            "Transaction data was not found against the generated key.",
                            approvedTransaction.getLookupKey()));
        }

        log.info("Approved transaction | lookupKey={}, cctRequest={}, processorRequest={}",
                approvedTransaction.getLookupKey(),
                approvedProcessRequest.getCctRequest(),
                approvedProcessRequest.getProcessorRequest());
        // ---------- END LOOKUP ----------

        ComparisonJsonResult jsonComparisonResult;
        try {
            jsonComparisonResult = compareJson(declinedRequest, approvedTransaction);
        } catch (NoDataFoundException e) {
            log.warn("Transaction data not found. lookupKey={}", e.getLookupKey());
            return ResponseEntity.ok(
                    new ExceptionResponse(e.getStatus(), e.getCode(), e.getMessage(), e.getLookupKey()));
        } catch (Exception e) {
            log.error("JSON comparison failed: {}", e.getMessage(), e);
            return ResponseEntity.ok(buildExceptionResponse("JSON_COMPARISON_FAILED", e.getMessage()));
        }

        ComparisionXmlResult xmlComparisonResult;
        try {
            xmlComparisonResult = compareXmlIfPresent(declinedRequest, approvedTransaction);
        } catch (Exception e) {
            log.error("XML comparison failed: {}", e.getMessage(), e);
            return ResponseEntity.ok(buildExceptionResponse("XML_COMPARISON_FAILED", e.getMessage()));
        }

        log.info("JSON comparison result: match={},\n mismatch={},\n validation={},\n skipped={}",
                jsonComparisonResult.getMatchIssue(), jsonComparisonResult.getMissMatchIssue(),
                jsonComparisonResult.getValidationIssue(), jsonComparisonResult.getSkippedIssue());

        log.info("XML comparison result: match={},\n mismatch={},\n validation={},\n skipped={}",
                xmlComparisonResult.getXmlMatchIssue(), xmlComparisonResult.getXmlMissMatchIssue(),
                xmlComparisonResult.getXmlValidationIssue(), xmlComparisonResult.getXmlSkippedIssue());

        ComparisonResult comparisonResult = new ComparisonResult();
        comparisonResult.setComparisonJsonResult(jsonComparisonResult);
        comparisonResult.setComparisionXmlResult(xmlComparisonResult);

        ValidationIssue validationIssue = new ValidationIssue();
        validationIssue.setRequestKey(approvedTransaction.getLookupKey());
        validationIssue.setAurusRequestValidationIssue(jsonComparisonResult.getValidationIssue());
        validationIssue.setProcessorRequestValidationIssue(xmlComparisonResult.getXmlValidationIssue());
        
        validationIssue.setAurusRequestValidationIssue(jsonComparisonResult.getMissMatchIssue());
        validationIssue.setProcessorRequestValidationIssue(xmlComparisonResult.getXmlMissMatchIssue());
        
        validationIssue.setAurusRequestValidationIssue(jsonComparisonResult.getMatchIssue());
        validationIssue.setProcessorRequestValidationIssue(xmlComparisonResult.getXmlMatchIssue());
        
        validationIssue.setAurusRequestValidationIssue(jsonComparisonResult.getSkippedIssue());
        validationIssue.setProcessorRequestValidationIssue(xmlComparisonResult.getXmlSkippedIssue());


        log.info("Validation completed successfully. lookupKey={}", approvedTransaction.getLookupKey());
        return ResponseEntity.ok(validationIssue);
    
    }

    /**
     * Shared decrypt -> lookup -> compareJson -> compareXmlIfPresent -> ValidationIssue flow
     * used by both /validationissue and /xcompare.
     *
     * NOTE: All "failure" branches intentionally return HTTP 200 with status/code fields
     * in the body (instead of 4xx/5xx) so that n8n's HTTP Request node does not treat them
     * as node failures and terminate the workflow. Branch on body.status / body.code in n8n
     * (e.g. an IF node on {{$json.code === "NO_DATA_FOUND"}}) instead of on HTTP status.
     */
    private ResponseEntity<?> processValidation(UserInput request) {
        log.info("Validation request received: {}", request);

        if (request == null) {
            log.error("Validation request is null");
            return ResponseEntity.ok(buildExceptionResponse("INVALID_REQUEST", "Request body is null"));
        }

        ProcessRequest declinedRequest;
        try {
            declinedRequest = decryptRequest(request);
        } catch (BlankRequestException e) {
            log.warn("cctRequest is empty");
            return ResponseEntity.ok(buildExceptionResponse("INVALID_REQUEST", "cctRequest cannot be empty"));
        } catch (Exception e) {
            log.error("Decryption failed: {}", e.getMessage(), e);
            return ResponseEntity.ok(buildExceptionResponse("DECRYPTION_FAILED", e.getMessage()));
        }

        log.info("Declined transaction | cctRequest={}, processorRequest={}",
                declinedRequest.getCctRequest(), declinedRequest.getProcessorRequest());

        // ---------- LOOKUP (direct checks, no exception thrown) ----------
        TransactionLookupResponse approvedTransaction;
        try {
            approvedTransaction = transactionLookupService.lookupTransaction(declinedRequest, request.getProcessorId());
        } catch (Exception e) {
            log.error("Unexpected error during transaction lookup: {}", e.getMessage(), e);
            return ResponseEntity.ok(
                    new ExceptionResponse("DECLINED", "LOOKUP_FAILED", e.getMessage(), null));
        }

        if (approvedTransaction == null) {
            log.warn("Transaction not found. lookupKey=null");
            return ResponseEntity.ok(
                    new ExceptionResponse("DECLINED", "NO_DATA_FOUND", "Transaction data key not found", null));
        }

        ProcessRequest approvedProcessRequest = approvedTransaction.getProcessRequest();

        if (approvedProcessRequest == null
                || (approvedProcessRequest.getCctRequest() == null && approvedProcessRequest.getProcessorRequest() == null)) {
            log.warn("Transaction not found. lookupKey={}", approvedTransaction.getLookupKey());
            return ResponseEntity.ok(
                    new ExceptionResponse("DECLINED", "NO_DATA_FOUND",
                            "Transaction data was not found against the generated key.",
                            approvedTransaction.getLookupKey()));
        }

        log.info("Approved transaction | lookupKey={}, cctRequest={}, processorRequest={}",
                approvedTransaction.getLookupKey(),
                approvedProcessRequest.getCctRequest(),
                approvedProcessRequest.getProcessorRequest());
        // ---------- END LOOKUP ----------

        ComparisonJsonResult jsonComparisonResult;
        try {
            jsonComparisonResult = compareJson(declinedRequest, approvedTransaction);
        } catch (NoDataFoundException e) {
            log.warn("Transaction data not found. lookupKey={}", e.getLookupKey());
            return ResponseEntity.ok(
                    new ExceptionResponse(e.getStatus(), e.getCode(), e.getMessage(), e.getLookupKey()));
        } catch (Exception e) {
            log.error("JSON comparison failed: {}", e.getMessage(), e);
            return ResponseEntity.ok(buildExceptionResponse("JSON_COMPARISON_FAILED", e.getMessage()));
        }

        ComparisionXmlResult xmlComparisonResult;
        try {
            xmlComparisonResult = compareXmlIfPresent(declinedRequest, approvedTransaction);
        } catch (Exception e) {
            log.error("XML comparison failed: {}", e.getMessage(), e);
            return ResponseEntity.ok(buildExceptionResponse("XML_COMPARISON_FAILED", e.getMessage()));
        }

        log.info("JSON comparison result: match={},\n mismatch={},\n validation={},\n skipped={}",
                jsonComparisonResult.getMatchIssue(), jsonComparisonResult.getMissMatchIssue(),
                jsonComparisonResult.getValidationIssue(), jsonComparisonResult.getSkippedIssue());

        log.info("XML comparison result: match={},\n mismatch={},\n validation={},\n skipped={}",
                xmlComparisonResult.getXmlMatchIssue(), xmlComparisonResult.getXmlMissMatchIssue(),
                xmlComparisonResult.getXmlValidationIssue(), xmlComparisonResult.getXmlSkippedIssue());

        ComparisonResult comparisonResult = new ComparisonResult();
        comparisonResult.setComparisonJsonResult(jsonComparisonResult);
        comparisonResult.setComparisionXmlResult(xmlComparisonResult);

        ValidationIssue validationIssue = new ValidationIssue();
        validationIssue.setRequestKey(approvedTransaction.getLookupKey());
        validationIssue.setAurusRequestValidationIssue(jsonComparisonResult.getMatchIssue());
        validationIssue.setProcessorRequestValidationIssue(xmlComparisonResult.getXmlMatchIssue());

        log.info("Validation completed successfully. lookupKey={}", approvedTransaction.getLookupKey());
        return ResponseEntity.ok(validationIssue);
    }

    // ---------------------------------------------------------------------
    // Helper methods
    // ---------------------------------------------------------------------

    private ProcessRequest decryptRequest(UserInput request) throws Exception {
        if (request.getCctRequest() == null || request.getCctRequest().isBlank()) {
            throw new BlankRequestException("cctRequest cannot be empty");
        }

        String decryptedCctRequest = aurusDecryptor.decryptor(request.getCctRequest());

        String decryptedProcessorRequest = null;
        if (request.getProcessorRequest() != null && !request.getProcessorRequest().isBlank()) {
            decryptedProcessorRequest = aurusDecryptor.decryptor(request.getProcessorRequest());
        } else {
            log.info("processorRequest is empty, skipping decryption");
        }

        ProcessRequest declinedRequest = new ProcessRequest();
        declinedRequest.setCctRequest(decryptedCctRequest);
        declinedRequest.setProcessorRequest(decryptedProcessorRequest);
        return declinedRequest;
    }

    private ComparisonJsonResult compareJson(ProcessRequest declinedRequest,
                                              TransactionLookupResponse approvedTransaction) throws Exception {
        String lookupKey = approvedTransaction.getLookupKey();

        String approvedJson = Optional.ofNullable(approvedTransaction.getProcessRequest())
                .map(ProcessRequest::getCctRequest)
                .filter(json -> !json.isBlank())
                .orElseThrow(() -> new NoDataFoundException(
                        "DECLINED", "NO_DATA_FOUND", "Transaction data not found", lookupKey));

        String declinedJson = declinedRequest.getCctRequest();

        log.debug("declinedJson={}", declinedJson);
        log.debug("approvedJson={}", approvedJson);

        ComparisonJsonResult result = jsonComparator.compare(declinedJson, approvedJson);
        log.info("JSON comparison result: {}", result);
        return result;
    }

    private ComparisionXmlResult compareXmlIfPresent(ProcessRequest declinedRequest,
                                                       TransactionLookupResponse approvedTransaction) throws Exception {

        log.info("Declined-Processor-Request : {}", declinedRequest.getProcessorRequest());
        String declinedXml = declinedRequest.getProcessorRequest();
        if (declinedXml == null || declinedXml.isBlank()) {
            return new ComparisionXmlResult();
        }

        ProcessRequest approvedProcessRequest = Optional.ofNullable(approvedTransaction.getProcessRequest())
                .orElseThrow(() -> new NoDataFoundException(
                        "DECLINED", "NO_DATA_FOUND", "Approved processor request not found",
                        approvedTransaction.getLookupKey()));

        String approvedXml = approvedProcessRequest.getProcessorRequest();

        log.info("processorRequest present, running XML comparison");
        log.info("approvedXml : {}", approvedXml);
        log.info("declinedXml : {}", declinedXml);

        return xmlComparator.getXmlComparator(approvedXml, declinedXml);
    }

    private ExceptionResponse buildExceptionResponse(String code, String message) {
        ExceptionResponse exceptionResponse = new ExceptionResponse();
        exceptionResponse.setCode(code);
        exceptionResponse.setMessage(message);
        return exceptionResponse;
    }
    private CustomResponse buildCustomResponse(String lookupKey, Object message,String status) {
    	CustomResponse customResponse = new CustomResponse();
    	customResponse.setLookupKey(lookupKey);
    	customResponse.setMessage(message);
    	customResponse.setStatus(status);
        return customResponse;
    }
}