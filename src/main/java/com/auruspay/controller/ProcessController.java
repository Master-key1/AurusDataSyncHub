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
import com.auruspay.dto.TransactionLookupResponse;
import com.auruspay.dto.UserInput;
import com.auruspay.logservice.exception.NoDataFoundException;
import com.auruspay.service.JsonDataAddService;
import com.auruspay.service.TransactionLookupService;
import com.auruspay.util.ExtractMultipleKeywords;
import com.auruspay.util.FileReadData;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

	private static final Logger logger = LoggerFactory.getLogger(ProcessController.class);

	private final JsonDataAddService jsonDataAddService;
	private final TransactionLookupService lookupService;
	private final AurusDecryptor aurusDecryptor;
	private final XmlComparator xmlComparator;
	private final JsonComparator jsonComparator;

	public ProcessController(JsonDataAddService jsonDataAddService, TransactionLookupService lookupService,
			AurusDecryptor aurusDecryptor, XmlComparator xmlComparator, JsonComparator jsonComparator) {

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

	@GetMapping("/submit")
	public ResponseEntity<?> submitForms() {

		try {
			logger.info("Received transaction save request");

			String directoryPath = "C:\\Users\\nkharose\\Pictures\\Data\\FD\\digitaldetails";

			FileReadData extractor = new FileReadData();

			List<String> filePaths = extractor.listFilesInDirectory(directoryPath, ".txt");

			if (filePaths.isEmpty()) {
				logger.info("No .txt files found to process");
				return ResponseEntity.ok(directoryPath);
			}

			Map<String, Map<String, List<String>>> allResults = extractor.extractDataForFiles(filePaths);

			if (allResults.isEmpty()) {
				logger.info("No matching data found");
				return ResponseEntity.ok("No matching data found");
			}

			Map<String, String> mapper = new HashMap<>();

			allResults.forEach((filePath, data) -> {

				logger.info("========== FILE : {} ==========", filePath);

				if (data == null || data.isEmpty()) {
					logger.info("No matching data found for this file");
					return;
				}

				// Build request object for this file
				ProcessRequest request = new ProcessRequest();

				data.forEach((key, values) -> {

					logger.info("Key : {}", key);

					if (values != null) {
						values.forEach(value -> {
							logger.info("{} - {}", key, value);
						});
					}

					logger.info("Keys found count: {}", data.keySet().size());

					// Get first extracted value
					String value = null;

					if (values != null && !values.isEmpty()) {
						value = values.get(0);
					}

					if (key.contains("[STPL-GRAY-STREAM]-AURUSPAY ENCRYPTED REQUEST :")) {
						request.setCctRequest(value);
					}

					if (key.contains("[STPL-GRAY-STREAM]-AURUSPAY ENCRYPTED RESPONSE :")) {
						request.setCctResponse(value);
					}

					if (key.contains("[STPL-GRAY-STREAM]- PROCESSOR REQUEST :")
							|| key.contains("[STPL-GRAY-STREAM]- REQUEST :")) {

						request.setProcessorRequest(value);
					}

					if (key.contains("[STPL-GRAY-STREAM]-FINAL RESPONSE :")
							|| key.contains("PROCESSOR RESPONSE FOR FIRST DATA PERSISTENT :")) {

						request.setProcessorResponse(value);
					}

					if (key.contains("PROCESSOR TERMINAL DETAILS")) {
						request.setProcessorId(value);
					}

				});

				logger.info("Request Object : {}", request);

				String txnId = null;

				try {

					txnId = jsonDataAddService.saveData(request);

					logger.info("Transaction saved successfully. txnId : {}", txnId);

					// Prevent null key in response map
					if (txnId != null && !txnId.trim().isEmpty()) {

						mapper.put(txnId, filePath);

					} else {

						logger.warn("Transaction ID is null/empty. File skipped from response map : {}", filePath);
					}

				} catch (Exception e) {

					logger.error("Error while saving data for file : {}", filePath, e);

				}

			});

			logger.info("Final Response Map : {}", mapper);

			return ResponseEntity.ok(mapper);

		} catch (Exception e) {

			logger.error("Error while saving transaction", e);

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to save transaction");
		}
	}

	@GetMapping("/test")
	public String openForms() {

		try {
			logger.info("Received transaction save request");

			/*
			 * if (request == null) {
			 * logger.warn("processAndSave called with null request"); return
			 * ResponseEntity.badRequest().body("Request body is required"); }
			 */
			ExtractMultipleKeywords extractor = new ExtractMultipleKeywords();

			// String uuid = "3bd5954e-1996-4f82-8110-d81ac10df45b";

			Map<String, List<String>> data = extractor.extractData();
			System.out.println("Data size : " + data.size());

			if (data.isEmpty()) {
				System.out.println("No data found for UUID : " + data.size());
			} else {
				data.forEach((key, value) -> {
					System.out.println(key + ":");
					value.forEach(System.out::println);
				});
			}
			ProcessRequest request = new ProcessRequest();

			if (data.get("[STPL-GRAY-STREAM]-AURUSPAY ENCRYPTED REQUEST :") != null) {
				request.setCctRequest(data.get("[STPL-GRAY-STREAM]-AURUSPAY ENCRYPTED REQUEST :").toString());
			}
			if (data.get("[STPL-GRAY-STREAM]-AURUSPAY ENCRYPTED RESPONSE :") != null) {
				request.setCctResponse(data.get("[STPL-GRAY-STREAM]-AURUSPAY ENCRYPTED RESPONSE :").toString());
			}
			if (data.get("[STPL-GRAY-STREAM]- PROCESSOR REQUEST :") != null) {
				request.setProcessorRequest(data.get("[STPL-GRAY-STREAM]- PROCESSOR REQUEST :").toString());
			}

			if (data.get("[STPL-GRAY-STREAM]-FINAL RESPONSE :") != null) {
				request.setProcessorResponse(data.get("[STPL-GRAY-STREAM]-FINAL RESPONSE :").toString());
			}

			if (data.get("PROCESSOR TERMINAL DETAILS") != null) {
				request.setProcessorId((data.get("PROCESSOR TERMINAL DETAILS").toString()));

			}

			System.out.println(request.toString());

			String txnId = jsonDataAddService.saveData(request);
			return (txnId);

		} catch (Exception e) {
			logger.error("Error while saving transaction", e);
			return ("Failed to save transaction");
		}

	}

	// ================= SMART COMPARE (currently disabled - see @PostMapping above)
	// =================
	// @PostMapping(value = "/json/compare", consumes =
	// MediaType.APPLICATION_JSON_VALUE)
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
			return ResponseEntity.badRequest().body(buildExceptionResponse("DECRYPTION_FAILED", e.getMessage()));
		}

		try {
			ComparisonJsonResult result = jsonComparator.compare(declined, approved);
			return ResponseEntity.ok(result);
		} catch (Exception e) {
			logger.error("smartCompare comparison failed | Reason: {}", e.getMessage(), e);
			return ResponseEntity.badRequest().body(buildExceptionResponse("COMPARISON_FAILED", e.getMessage()));
		}
	}

	@PostMapping(value = "/validationissue")
	public ResponseEntity<?> validationIssue(@RequestBody UserInput request) {

		logger.info("Compare request received: {}", request);

		if (request == null) {

			logger.error("Compare request is null");

			return ResponseEntity.ok(buildExceptionResponse("INVALID_REQUEST", "Request body is null"));
		}

		ProcessRequest declinedRequest = new ProcessRequest();

		String cctRequest = null;
		String procRequest = null;

		// ================= DECRYPT REQUEST =================

		try {

			if (request.getCctRequest() == null || request.getCctRequest().isBlank()) {

				logger.warn("cctRequest is empty");

				return ResponseEntity.ok(buildExceptionResponse("INVALID_REQUEST", "cctRequest cannot be empty"));

			}

			cctRequest = aurusDecryptor.decryptor(request.getCctRequest());

			if (request.getProcessorRequest() != null && !request.getProcessorRequest().isBlank()) {

				procRequest = aurusDecryptor.decryptor(request.getProcessorRequest());

			} else {

				logger.info("processorRequest is empty, skipping decryption");
			}

		} catch (Exception e) {

			logger.error("Decryption failed. Error={}", e.getMessage(), e);

			return ResponseEntity.ok(buildExceptionResponse("DECRYPTION_FAILED", e.getMessage()));
		}

		declinedRequest.setCctRequest(cctRequest);
		declinedRequest.setProcessorRequest(procRequest);

		logger.info("CCT Request : {}", cctRequest);
		logger.info("Processor Request : {}", procRequest);

		// ================= FETCH APPROVED DATA =================

		TransactionLookupResponse data = null;

		try {

			data = lookupService.lookupTransaction(declinedRequest, request.getProcessorId());

			logger.info("Approved transaction found. lookupKey={}", data.getLookupKey());

		} catch (NoDataFoundException e) {

			logger.warn("Transaction not found. lookupKey={}, message={}", e.getLookupKey(), e.getMessage());

			logger.error("NoDataFoundException details", e);

			return ResponseEntity
					.ok(new ExceptionResponse("DECLINED", "NO_DATA_FOUND", e.getMessage(), e.getLookupKey()));

		} catch (NullPointerException e) {

			logger.warn("Transaction not found. lookupKey={}, message={}", data.getLookupKey(), e.getMessage());

			logger.error("NoDataFoundException details", e);

			return ResponseEntity
					.ok(new ExceptionResponse("DECLINED", "NO_DATA_FOUND", e.getMessage(), data.getLookupKey()));

		} catch (Exception e) {

			logger.error("Unexpected error during transaction lookup. Error={}", e.getMessage(), e);

			return ResponseEntity.ok(new ExceptionResponse("DECLINED", "LOOKUP_FAILED", e.getMessage(), null));
		}

		ComparisonResult comparisonResult = new ComparisonResult();

		ValidationIssue validationIssue = new ValidationIssue();

		ComparisionXmlResult xmlComparedData;

		ComparisonJsonResult jsonComparedData;

//		if (declinedRequest.getProcessorRequest() == null ||  declinedRequest.getProcessorRequest().isBlank() ||  declinedRequest.getProcessorRequest().isEmpty()) {

		// ================= JSON/CCT COMPARISON =================

		try {

			String key = data.getLookupKey();

			String approvedJson = Optional.ofNullable(data.getProcessRequest()).map(ProcessRequest::getCctRequest)
					.filter(json -> !json.isBlank()).orElseThrow(() -> new NoDataFoundException("DECLINED",
							"NO_DATA_FOUND", "Transaction data not found", key));

			String declinedJson = declinedRequest.getCctRequest();

			jsonComparedData = jsonComparator.compare(declinedJson, approvedJson);

		} catch (NoDataFoundException e) {

			logger.warn("Transaction data not found. lookupKey={}", e.getLookupKey());

			return ResponseEntity.badRequest()
					.body(new ExceptionResponse(e.getStatus(), e.getCode(), e.getMessage(), e.getLookupKey()));

		} catch (Exception e) {

			logger.error("JSON comparison failed. Error={}", e.getMessage(), e);

			return ResponseEntity.ok(buildExceptionResponse("JSON_COMPARISON_FAILED", e.getMessage()));
		}
		// ================= XML COMPARISON =================

		if (!(declinedRequest.getProcessorRequest().isBlank() || declinedRequest.getProcessorRequest().isEmpty())) {

			try {

				String approvedXml = data.getProcessRequest().getProcessorRequest();

				String declinedXml = declinedRequest.getProcessorRequest();

				xmlComparedData = approvedXml != null ? xmlComparator.getXmlComparator(approvedXml, declinedXml)
						: new ComparisionXmlResult();

			} catch (Exception e) {

				logger.error("XML comparison failed. Error={}", e.getMessage(), e);

				return ResponseEntity.ok(buildExceptionResponse("XML_COMPARISON_FAILED", e.getMessage()));
			}

		} else {

			xmlComparedData = new ComparisionXmlResult();
		}

		if (jsonComparedData != null)
			comparisonResult.setComparisonJsonResult(jsonComparedData);
		if (xmlComparedData != null)
			comparisonResult.setComparisionXmlResult(xmlComparedData);

		validationIssue
				.setProcessorRequestValidationIssue(comparisonResult.getComparisionXmlResult().getXmlValidationIssue());

		validationIssue.setAurusRequestValidationIssue(comparisonResult.getComparisonJsonResult().getValidationIssue());

		logger.info("Validation issue comparison completed successfully");

		return ResponseEntity.ok(validationIssue);
	}

	private NoDataFoundException noDataFound(String lookupKey) {
		return new NoDataFoundException("FAILED", "NO_DATA_FOUND", "Transaction data key not found", lookupKey);
	}

	@PostMapping(value = "/xcompare")
	public ResponseEntity<?> compare(@Valid @RequestBody UserInput request) {

		logger.info("Compare request received: {}", request);

		if (request == null) {
			logger.error("Compare request is null");
			return ResponseEntity.badRequest().body(buildExceptionResponse("INVALID_REQUEST", "Request body is null"));
		}

		ProcessRequest declinedRequest = new ProcessRequest();

		String cctRequest = null;
		String procRequest = null;

		try {

			// CCT Request - mandatory
			if (request.getCctRequest() != null && !request.getCctRequest().isBlank()) {

				cctRequest = aurusDecryptor.decryptor(request.getCctRequest());

			} else {
				return ResponseEntity.badRequest()
						.body(buildExceptionResponse("INVALID_REQUEST", "cctRequest cannot be empty"));
			}

			// Processor Request - optional
			// Allows: null, "", " "
			if (request.getProcessorRequest() != null && !request.getProcessorRequest().isBlank()) {

				procRequest = aurusDecryptor.decryptor(request.getProcessorRequest());

			} else {
				logger.info("processorRequest is empty, skipping decryption");
				procRequest = null;
			}

		} catch (Exception e) {
			logger.error("Decryption failed for request | Reason: {}", e.getMessage(), e);

			return ResponseEntity.badRequest().body(buildExceptionResponse("DECRYPTION_FAILED", e.getMessage()));
		}

		declinedRequest.setCctRequest(cctRequest);
		declinedRequest.setProcessorRequest(procRequest);

		logger.info("CCT Request : {}", cctRequest);
		logger.info("Processor Request : {}", procRequest);

		if (cctRequest == null && procRequest == null) {
			logger.warn("Both cctRequest and processorRequest are null after decryption");

			return ResponseEntity.badRequest()
					.body(buildExceptionResponse("EMPTY_REQUEST", "Both cctRequest and processorRequest are null"));
		}

		// ================= FETCH APPROVED DATA =================
		TransactionLookupResponse data = null;
		try {

			data = lookupService.lookupTransaction(declinedRequest, request.getProcessorId());

			logger.info("Approved transaction found : {}", data.getLookupKey());

		} catch (NoDataFoundException ex) {

			logger.warn("Transaction not found. lookupKey={}, message={}", data.getLookupKey(), ex.getMessage());

			ExceptionResponse response = new ExceptionResponse("DECLINED", "NO_DATA_FOUND",
					"Transaction data not found", data.getLookupKey());

			return ResponseEntity.ok(response);

		} catch (Exception ex) {

			logger.error("Transaction lookup failed. reason={}", ex.getMessage(), ex);

			ExceptionResponse response = new ExceptionResponse("DECLINED", "LOOKUP_FAILED",
					"Failed to retrieve the approved transaction");

			return ResponseEntity.ok(response);
		}

		ComparisonResult comparisonResult = new ComparisonResult();
		ValidationIssue validationIssue = new ValidationIssue();

		ComparisionXmlResult xmlComparedData;
		ComparisonJsonResult jsonComparedData;
		if (declinedRequest.getProcessorRequest() == null || declinedRequest.getProcessorRequest().isBlank()
				|| declinedRequest.getProcessorRequest().isEmpty()) {
			ExceptionResponse response = new ExceptionResponse("DECLINED", "NO_DATA_FOUND",
					"Transaction data not found", data.getLookupKey());

			return ResponseEntity.ok(response);
		}

		// ================= XML COMPARISON =================
		if (!(declinedRequest.getProcessorRequest() == null || declinedRequest.getProcessorRequest().isBlank()
				|| declinedRequest.getProcessorRequest().isEmpty())) {

			String approvedXml = data.getProcessRequest().getProcessorRequest();
			String declinedXml = declinedRequest.getProcessorRequest();

			try {
				xmlComparedData = (approvedXml != null) ? xmlComparator.getXmlComparator(approvedXml, declinedXml)
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

			String approvedJson = data.getProcessRequest().getCctRequest();
			String declinedJson = declinedRequest.getCctRequest();

			try {
				jsonComparedData = (approvedJson != null) ? jsonComparator.compare(declinedJson, approvedJson)
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

		validationIssue
				.setProcessorRequestValidationIssue(comparisonResult.getComparisionXmlResult().getXmlValidationIssue());

		validationIssue.setAurusRequestValidationIssue(comparisonResult.getComparisonJsonResult().getValidationIssue());

		logger.info("Compare API completed successfully");

		return ResponseEntity.ok(validationIssue);
	}

	private ExceptionResponse buildExceptionResponse(String code, String message) {
		ExceptionResponse exceptionResponse = new ExceptionResponse();
		exceptionResponse.setCode(code);
		exceptionResponse.setMessage(message);
		return exceptionResponse;
	}
}