package com.auruspay.controller;

import com.auruspay.comparator.JsonComparator;
import com.auruspay.comparator.JsonFieldValidator;
import com.auruspay.comparator.XmlComparator;
import com.auruspay.comparator.XmlComparators;
import com.auruspay.comparator.model.ComparisionXmlResult;
import com.auruspay.comparator.model.ComparisonJsonResult;
import com.auruspay.comparator.model.ComparisonResult;
import com.auruspay.comparator.model.ValidateResult;
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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
	private final XmlComparators xmlComparator;
	private final JsonComparator jsonComparator;
	private final FileReadData fileReadData;
	private final ExtractMultipleKeywords extractMultipleKeywords;
	private final JsonFieldValidator fieldValidator;

	@Value("${aurus.file.import-directory:C:/data/combine}")
	private String fileImportDirectory;

	public ProcessController(JsonDataAddService jsonDataAddService, TransactionLookupService transactionLookupService,
			AurusDecryptor aurusDecryptor, XmlComparators xmlComparator, JsonComparator jsonComparator,
			FileReadData fileReadData, ExtractMultipleKeywords extractMultipleKeywords,
			JsonFieldValidator fieldValidator) {
		this.jsonDataAddService = jsonDataAddService;
		this.transactionLookupService = transactionLookupService;
		this.aurusDecryptor = aurusDecryptor;
		this.xmlComparator = xmlComparator;
		this.jsonComparator = jsonComparator;
		this.fileReadData = fileReadData;
		this.extractMultipleKeywords = extractMultipleKeywords;
		this.fieldValidator = fieldValidator;
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

	@GetMapping("/submit")
	public ResponseEntity<?> submitForms() {
		log.info("Received transaction save request");

		List<String> directoryPaths = List.of("C:\\Users\\nkharose\\Pictures\\LogData\\AT Home\\AT Home",
				"C:\\Users\\nkharose\\Pictures\\LogData\\AVOLTA\\AVOLTA",
				"C:\\Users\\nkharose\\Pictures\\LogData\\Brink\\Brink",
				"C:\\Users\\nkharose\\Pictures\\LogData\\Chedraui\\Chedraui",
				"C:\\Users\\nkharose\\Pictures\\LogData\\Express_Corp\\Express_Corp",
				"C:\\Users\\nkharose\\Pictures\\LogData\\LOFT US Corp\\LOFT US Corp",
				"C:\\Users\\nkharose\\Pictures\\LogData\\Penske\\Penske",
				"C:\\Users\\nkharose\\Pictures\\LogData\\Rohit\\Rohit",
				"C:\\Users\\nkharose\\Pictures\\LogData\\Tailored Brands Corp\\Tailored Brands Corp",
				"C:\\Users\\nkharose\\Pictures\\LogData\\Tillys\\Tillys",
				"C:\\Users\\nkharose\\Pictures\\LogData\\Under Armour\\Under Armour",
				"C:\\Users\\nkharose\\Pictures\\LogData\\Zumiez_INC Corp\\Zumiez_INC Corp");

		FileReadData extractor = new FileReadData();

		Map<String, String> uploadedFiles = new HashMap<>();
		Map<String, String> failedFiles = new HashMap<>();

		for (String directoryPath : directoryPaths) {

			log.info("Processing directory: {}", directoryPath);

			List<String> filePaths = extractor.listFilesInDirectory(directoryPath, ".txt");

			if (filePaths.isEmpty()) {
				log.info("No .txt files found in {}", directoryPath);
				continue;
			}

			Map<String, Map<String, List<String>>> allResults = extractor.extractDataForFiles(filePaths);

			if (allResults.isEmpty()) {
				log.info("No matching data found in {}", directoryPath);
				continue;
			}

			allResults.forEach((filePath, data) -> {

				String fileName = new File(filePath).getName();

				if (data == null || data.isEmpty()) {
					String issue = "No matching data found in file";

					log.error("File: {} | Issue: {}", fileName, issue);

					failedFiles.put(filePath, issue);
					return;
				}

				ProcessRequest request = new ProcessRequest();

				// Track which required data is found
				boolean cctRequestFound = false;
				boolean cctResponseFound = true;
				boolean processorRequestFound = true;
				boolean processorResponseFound = true;
				boolean processorIdFound = false;

				for (Map.Entry<String, List<String>> entry : data.entrySet()) {

					String key = entry.getKey();

					String value = (entry.getValue() != null && !entry.getValue().isEmpty()) ? entry.getValue().get(0)
							: null;

					if (value == null || value.isBlank()) {
						continue;
					}

					if (key.contains("[STPL-GRAY-STREAM]-AURUSPAY ENCRYPTED REQUEST :")) {

						request.setCctRequest(value);
						cctRequestFound = true;
					}

					if (key.contains("[STPL-GRAY-STREAM]-AURUSPAY ENCRYPTED RESPONSE :")) {

						request.setCctResponse(value);
						cctResponseFound = true;
					}

					if (key.contains("[STPL-GRAY-STREAM]- PROCESSOR REQUEST :")
							|| key.contains("[STPL-GRAY-STREAM]- REQUEST :")
							|| key.contains("[STPL-GRAY-STREAM]- PLCC PROCESSOR REQUEST ::")) {

						request.setProcessorRequest(value);
						processorRequestFound = true;
					}

					if (key.contains("[STPL-GRAY-STREAM]-FINAL RESPONSE :")
							|| key.contains("PROCESSOR RESPONSE FOR FIRST DATA PERSISTENT :")) {

						request.setProcessorResponse(value);
						processorResponseFound = true;
					}

					if (key.contains("PROCESSOR TERMINAL DETAILS")) {

						request.setProcessorId(value);
						processorIdFound = true;
					}
				}

				/*
				 * Validate required data before saving
				 */
				List<String> missingKeys = new ArrayList<String>();

				if (!cctRequestFound) {
					missingKeys.add("AURUSPAY ENCRYPTED REQUEST");
				}

				if (!cctResponseFound) {
					missingKeys.add("AURUSPAY ENCRYPTED RESPONSE");
				}

				if (!processorRequestFound) {
					missingKeys.add("PROCESSOR REQUEST");
				}

				if (!processorResponseFound) {
					missingKeys.add("PROCESSOR RESPONSE");
				}

				if (!processorIdFound) {
					missingKeys.add("PROCESSOR TERMINAL DETAILS");
				}

				/*
				 * If any required data is missing, do not upload the file.
				 */
				if (!missingKeys.isEmpty()) {

					String issue = "Required key/data not found: " + String.join(", ", missingKeys);

					log.error("File: {} | Upload skipped | Issue: {}", fileName, issue);

					failedFiles.put(filePath, issue);

					return;
				}

				/*
				 * Save data
				 */
				try {

					String txnId = jsonDataAddService.saveData(request);

					if (txnId != null && !txnId.isBlank()) {

						uploadedFiles.put(txnId, filePath);

						log.info("File uploaded successfully: {} | Transaction ID: {}", fileName, txnId);

					} else {

						String issue = "saveData() returned null or empty transaction ID";

						log.error("File: {} | Upload failed | Issue: {}", fileName, issue);

						failedFiles.put(filePath, issue);
					}

				} catch (Exception e) {

					String issue = "Exception while saving data: " + e.getMessage();

					log.error("File: {} | Upload failed | Issue: {}", fileName, issue, e);

					failedFiles.put(filePath, issue);
				}
			});
		}

		/*
		 * Final response
		 */
		Map<String, Object> finalResponse = new LinkedHashMap();

		finalResponse.put("uploadedFiles", uploadedFiles);
		finalResponse.put("failedFiles", failedFiles);

		log.info("Uploaded Files: {}", uploadedFiles);
		log.info("Failed Files: {}", failedFiles);

		return ResponseEntity.ok(finalResponse);
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

		log.info("Declined transaction | cctRequest={}, processorRequest={}", declinedRequest.getCctRequest(),
				declinedRequest.getProcessorRequest());

		// ---------- LOOKUP (direct checks, no exception thrown) ----------
		TransactionLookupResponse approvedTransaction;
		try {
			approvedTransaction = transactionLookupService.lookupTransaction(declinedRequest, request.getProcessorId());
		} catch (Exception e) {
			log.error("Unexpected error during transaction lookup: {}", e.getMessage(), e);
			return ResponseEntity.ok(new ExceptionResponse("DECLINED", "LOOKUP_FAILED", e.getMessage(), null));
		}

		if (approvedTransaction == null) {
			log.warn("Transaction not found. lookupKey=null");
			return ResponseEntity
					.ok(new ExceptionResponse("DECLINED", "NO_DATA_FOUND", "Transaction data key not found", null));
		}

		ProcessRequest approvedProcessRequest = approvedTransaction.getProcessRequest();

		if (approvedProcessRequest == null || (approvedProcessRequest.getCctRequest() == null
				&& approvedProcessRequest.getProcessorRequest() == null)) {
			log.warn("Transaction not found. lookupKey={}", approvedTransaction.getLookupKey());
			return ResponseEntity.ok(new ExceptionResponse("DECLINED", "NO_DATA_FOUND",
					"Transaction data was not found in the Golden Template for the generated key.",
					approvedTransaction.getLookupKey()));
		}

		log.info("Approved transaction | lookupKey={}, cctRequest={}, processorRequest={}",
				approvedTransaction.getLookupKey(), approvedProcessRequest.getCctRequest(),
				approvedProcessRequest.getProcessorRequest());
		// ---------- END LOOKUP ----------

		ComparisonJsonResult jsonComparisonResult = null;
		try {

			String app = """
					{"4.10":"0.00","4.11":"0.01","4.13":"188001","4.15":"3","4.163":"0.00","4.16":"","4.20":"840","4.21":"840","4.22":"0","12.87":"","4.26":"1","3.1":"20","3.2":"P630 Plus-A","3.3":"REG01","4.17":"","3.4":"00","4.18":"07072026","3.5":"26.04.142.003","4.19":"143048","7.1":"0.00","3.6":"1.8","7.2":"0.00","3.7":"VOS3_3.4.0-432","7.3":"0.00","92.6.3":"999","3.8":"1","7.4":"001","7.5":"null","4.32":"1","7.8":"1","7.9":"%1F1%1F1%7E%7E%7E001%7EGASOLINE%7EG%7E1%2E000%7E10%2E000%7E10%2E00%7E%1D","4.36":"","4.142":"%7B%22BTSignal%22%3A%22%22%2C%22Battery%22%3A%22NA%22%2C%22GPRSSignal%22%3A%22NotConnected%22%2C%22PingState%22%3A%220%22%2C%22WifiSignal%22%3A%22100%22%7D","4.38":"26.04.142.003","4.29":"","12.63":"1001","5.13":"03101001","4.46":"01","5.14":"04","4.47":"01","5.15":"01","4.48":"","4.49":"01","4.134":"0.00","4.135":"0.00","4.40":"01","4.41":"00","2.1":"552276417","2.2":"552276417","2.3":"10.192.12.250","6.1":"","6.2":"","6.3":"","6.4":"","6.5":"","6.6":"","6.7":"","6.8":"","6.9":"12345","4.57":"00","4.126":"0.00","4.127":"0.00","4.128":"0.00","4.52":"0","4.64":"0.00","4.69":"679999XXXXXXX2010","4.112":"0","4.63":"0","1.1":"101231310749","1.2":"03101","1.3":"18604837","5.1":"12345667","5.3":"1324566","5.4":"111","5.5":"","5.6":"","5.8":"6.000","5.9":"03101001","6.11":"","12.98":"","6.18":"","6.17":"","3.23":"10.192.15.65","4.70":"1249","3.21":"2.51","6.10":"","6.23":"","6.22":"","6.25":"","6.24":"","6.27":"","6.26":"","91.1.9":"6.000","6.21":"","6.20":"","4.1":"21","4.2":"000014","4.3":"6","6.19":"","4.4":"6CC8B6BD4B040AD65B1DB36C4ADBAB1237AA54736E6E42D2832DC0CA0F6CD1EDFB96CE99F10119FA%7EFFFF901347C7E0600096%7E0000000000000000","4.5":"0.01","8.1":"4F%7EA0000000043060D0561111%1E50%7E4D41455354524F%1E5F20%7E5553412044656269742F54657374204361726420303620202020%1E5F24%7E491231%1E5F25%7E250201%1E5F28%7E0840%1E5F2A%7E0840%1E5F2D%7E656e%1E5F30%7E0201%1E5F34%7E11%1E5F56%7E%1E82%7E1800%1E84%7EA0000000043060D0561111%1E8E%7E00000000000000000205410342035E031F0000000000000000000000%1E8F%7E%1E95%7E8000008000%1E9A%7E260707%1E9B%7E6800%1E9C%7E00%1E9F02%7E000000000100%1E9F03%7E000000000000%1E9F06%7EA0000000043060%1E9F07%7EFFC0%1E9F08%7E0002%1E9F09%7E0002%1E9F0D%7EB0509C8800%1E9F0E%7E0000000000%1E9F0F%7EB0709C9800%1E9F10%7E0110A50009020000000000000000000000FF%1E9F11%7E01%1E9F12%7E4D41455354524F4465626974%1E9F15%7E5999%1E9F1A%7E0840%1E9F1C%7E3138363034383337%1E9F1E%7E353532323736343137%1E9F21%7E050116%1E9F26%7E311B5006DBC083E8%1E9F27%7E80%1E9F33%7EE0F8C8%1E9F34%7E410302%1E9F35%7E22%1E9F36%7E0042%1E9F37%7E2FD9BC07%1E9F39%7E05%1E9F40%7E6000F0A001%1E9F41%7E00000102%1E9F42%7E840%1E9F4C%7E%1E9F53%7E52%1E9F6E%7E08400000303000%1E9F7C%7E%1EDF79%7E342e302e30%1E","4.6":"0.00","4.7":"0.00","4.8":"0.00","4.9":"0.00"}
					     		 		""";
			String dec = """
					{"4.11":"175.00","4.13":"198001","4.15":"8","4.16":"D25B79B57DED5D84","14.13":"1","3.43":"6","4.20":"840","4.21":"840","4.26":"1","4.113":"1","12.1":"0025015","1.1":"101231309688","1.2":"3100400","1.3":"35890831","3.1":"17","3.2":"AESDK","3.3":"Reg01","5.1":"0025015","4.17":"FFFF05700240AF60405F","3.4":"00","4.18":"07172026","3.5":"25.09.115.001","4.19":"105912","5.3":"0025015","7.1":"0.00","3.6":"1.8","7.2":"0.00","3.7":"10.0","7.3":"0.00","3.8":"1","7.5":"%1F%1F%1D","5.8":"5.0874","5.9":"01","4.32":"3","7.8":"1","4.79":"1","4.38":"25.09.115.001","3.21":"2.39","4.42":"P2PE NOT SUPPORTED","5.13":"01","5.14":"01","4.40":"00","2.1":"C0N43N4U45RLN8R1","2.2":"60:33:4b:10:42:98","2.3":"10.4.0.99","4.1":"246","4.2":"018297","4.3":"17","4.4":"5569269367557629D29012010901544100015","4.5":"175.00","8.1":"50%7E4D415354455243415244%1E57%7E5569269367557629D29012010901544100015%1E5A%7E5569269367557629%1E82%7E1980%1E84%7EA0000000041010%1E8E%7E000000000000000042031E031F03%1E95%7E0000048001%1E9A%7E260717%1E9B%7EE800%1E9C%7E00%1E5F24%7E290131%1E5F2A%7E0840%1E5F34%7E01%1E9F02%7E000000015000%1E9F03%7E000000000000%1E9F06%7EA0000000041010%1E9F07%7EFF00%1E9F09%7E0002%1E9F10%7E0110A04003220000000000000000000000FF%1E9F1A%7E0840%1E9F1E%7E33383337333333303330333833303332%1E9F26%7E37C0425368FE7A73%1E9F27%7E80%1E9F33%7E604808%1E9F34%7E420300%1E9F35%7E25%1E9F36%7E0019%1E9F37%7E85BD249A%1E9F39%7E07%1E9F41%7E00000643%1E9F53%7E52%1E9F6E%7E0840000330300001010080%1EBF0C%7E9F5D030000009F4D020B0A9F6E0B0840000330300001010080%1E","4.6":"0.00","4.7":"0.00"}

					     		 			""";
			List<ValidateResult> result = null;
			// 08069379999

		//	 jsonComparisonResult = compareJson(declinedRequest, approvedTransaction);

		jsonComparisonResult = fieldValidator.validation(declinedRequest.getCctRequest(),
				approvedTransaction.getProcessRequest().getCctRequest());

		//	jsonComparisonResult = fieldValidator.validation(dec, app);
			// log.info("jsonComparisonResult :",jsonComparisonResult);

		} catch (NoDataFoundException e) {
			log.warn("Transaction data not found. lookupKey={}", e.getLookupKey());
			return ResponseEntity
					.ok(new ExceptionResponse(e.getStatus(), e.getCode(), e.getMessage(), e.getLookupKey()));
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
				jsonComparisonResult.getMissMatchIssue(), jsonComparisonResult.getValidationIssue());

		log.info("XML comparison result: \n mismatch={},\n validation={}", xmlComparisonResult.getXmlMissMatchIssue(),
				xmlComparisonResult.getXmlValidationIssue());

		ComparisonResult comparisonResult = new ComparisonResult();
		comparisonResult.setComparisonJsonResult(jsonComparisonResult);
		comparisonResult.setComparisionXmlResult(xmlComparisonResult);

		ValidationIssue validationIssue = new ValidationIssue();
		validationIssue.setRequestKey(approvedTransaction.getLookupKey());

		if (jsonComparisonResult.getValidationIssue() != null || xmlComparisonResult.getXmlValidationIssue() != null) {
			validationIssue.setAurusRequestValidationIssue(jsonComparisonResult.getValidationIssue());
			validationIssue.setProcessorRequestValidationIssue(xmlComparisonResult.getXmlValidationIssue());
		}
		if (jsonComparisonResult.getMissMatchIssue() != null || xmlComparisonResult.getXmlMissMatchIssue() != null) {
			validationIssue.setAurusRequestValidationIssue(jsonComparisonResult.getMissMatchIssue());
			validationIssue.setProcessorRequestValidationIssue(xmlComparisonResult.getXmlMissMatchIssue());
		} else {
			String msg = "The transaction request has been compared with both approved and declined transaction requests, and the request format is valid.\r\n"
					+ "\r\n"
					+ "The transaction was successfully sent to the processor and received by them. However, it was declined at the processor's end.\r\n"
					+ "\r\n"
					+ "Please raise this issue with the processor for further investigation and identify the reason for the decline.\r\n"
					+ "";
			return ResponseEntity.ok(buildCustomResponse(approvedTransaction.getLookupKey(), msg, "success"));
		}

		log.info("Validation completed successfully. lookupKey={}", approvedTransaction.getLookupKey());
		return ResponseEntity.ok(comparisonResult);

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

		log.info("Declined transaction | cctRequest={}, processorRequest={}", declinedRequest.getCctRequest(),
				declinedRequest.getProcessorRequest());

		// ---------- LOOKUP (direct checks, no exception thrown) ----------
		TransactionLookupResponse approvedTransaction;
		try {
			approvedTransaction = transactionLookupService.lookupTransaction(declinedRequest, request.getProcessorId());
		} catch (Exception e) {
			log.error("Unexpected error during transaction lookup: {}", e.getMessage(), e);
			return ResponseEntity.ok(new ExceptionResponse("DECLINED", "LOOKUP_FAILED", e.getMessage(), null));
		}

		if (approvedTransaction == null) {
			log.warn("Transaction not found. lookupKey=null");
			return ResponseEntity
					.ok(new ExceptionResponse("DECLINED", "NO_DATA_FOUND", "Transaction data key not found", null));
		}

		ProcessRequest approvedProcessRequest = approvedTransaction.getProcessRequest();

		if (approvedProcessRequest == null || (approvedProcessRequest.getCctRequest() == null
				&& approvedProcessRequest.getProcessorRequest() == null)) {
			log.warn("Transaction not found. lookupKey={}", approvedTransaction.getLookupKey());
			return ResponseEntity.ok(new ExceptionResponse("DECLINED", "NO_DATA_FOUND",
					"Transaction data was not found in the Golden Template for the generated key.",
					approvedTransaction.getLookupKey()));
		}

		log.info("Approved transaction | lookupKey={}, cctRequest={}, processorRequest={}",
				approvedTransaction.getLookupKey(), approvedProcessRequest.getCctRequest(),
				approvedProcessRequest.getProcessorRequest());
		// ---------- END LOOKUP ----------

		ComparisonJsonResult jsonComparisonResult;
		try {
			jsonComparisonResult = compareJson(declinedRequest, approvedTransaction);
		} catch (NoDataFoundException e) {
			log.warn("Transaction data not found. lookupKey={}", e.getLookupKey());
			return ResponseEntity
					.ok(new ExceptionResponse(e.getStatus(), e.getCode(), e.getMessage(), e.getLookupKey()));
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
				jsonComparisonResult.getMissMatchIssue(), jsonComparisonResult.getValidationIssue());

		log.info("XML comparison result: match={},\n mismatch={},\n validation={},\n skipped={}",
				xmlComparisonResult.getXmlMissMatchIssue(), xmlComparisonResult.getXmlValidationIssue());

		ComparisonResult comparisonResult = new ComparisonResult();
		comparisonResult.setComparisonJsonResult(jsonComparisonResult);
		comparisonResult.setComparisionXmlResult(xmlComparisonResult);

		log.info("Validation completed successfully. lookupKey={}", approvedTransaction.getLookupKey());
		return ResponseEntity.ok(comparisonResult);

	}

	/**
	 * Shared decrypt -> lookup -> compareJson -> compareXmlIfPresent ->
	 * ValidationIssue flow used by both /validationissue and /xcompare.
	 *
	 * NOTE: All "failure" branches intentionally return HTTP 200 with status/code
	 * fields in the body (instead of 4xx/5xx) so that n8n's HTTP Request node does
	 * not treat them as node failures and terminate the workflow. Branch on
	 * body.status / body.code in n8n (e.g. an IF node on {{$json.code ===
	 * "NO_DATA_FOUND"}}) instead of on HTTP status.
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

		log.info("Declined transaction | cctRequest={}, processorRequest={}", declinedRequest.getCctRequest(),
				declinedRequest.getProcessorRequest());

		// ---------- LOOKUP (direct checks, no exception thrown) ----------
		TransactionLookupResponse approvedTransaction;
		try {
			approvedTransaction = transactionLookupService.lookupTransaction(declinedRequest, request.getProcessorId());
		} catch (Exception e) {
			log.error("Unexpected error during transaction lookup: {}", e.getMessage(), e);
			return ResponseEntity.ok(new ExceptionResponse("DECLINED", "LOOKUP_FAILED", e.getMessage(), null));
		}

		if (approvedTransaction == null) {
			log.warn("Transaction not found. lookupKey=null");
			return ResponseEntity
					.ok(new ExceptionResponse("DECLINED", "NO_DATA_FOUND", "Transaction data key not found", null));
		}

		ProcessRequest approvedProcessRequest = approvedTransaction.getProcessRequest();

		if (approvedProcessRequest == null || (approvedProcessRequest.getCctRequest() == null
				&& approvedProcessRequest.getProcessorRequest() == null)) {
			log.warn("Transaction not found. lookupKey={}", approvedTransaction.getLookupKey());
			return ResponseEntity.ok(new ExceptionResponse("DECLINED", "NO_DATA_FOUND",
					"Transaction data was not found in the Golden Template for the generated key.",
					approvedTransaction.getLookupKey()));
		}

		log.info("Approved transaction | lookupKey={}, cctRequest={}, processorRequest={}",
				approvedTransaction.getLookupKey(), approvedProcessRequest.getCctRequest(),
				approvedProcessRequest.getProcessorRequest());
		// ---------- END LOOKUP ----------

		ComparisonJsonResult jsonComparisonResult;
		try {
			jsonComparisonResult = compareJson(declinedRequest, approvedTransaction);
		} catch (NoDataFoundException e) {
			log.warn("Transaction data not found. lookupKey={}", e.getLookupKey());
			return ResponseEntity
					.ok(new ExceptionResponse(e.getStatus(), e.getCode(), e.getMessage(), e.getLookupKey()));
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
				jsonComparisonResult.getMissMatchIssue(), jsonComparisonResult.getValidationIssue());

		log.info("XML comparison result: match={},\n mismatch={},\n validation={},\n skipped={}",
				xmlComparisonResult.getXmlMissMatchIssue(), xmlComparisonResult.getXmlValidationIssue());

		ComparisonResult comparisonResult = new ComparisonResult();
		comparisonResult.setComparisonJsonResult(jsonComparisonResult);
		comparisonResult.setComparisionXmlResult(xmlComparisonResult);

		ValidationIssue validationIssue = new ValidationIssue();
		validationIssue.setRequestKey(approvedTransaction.getLookupKey());
		validationIssue.setAurusRequestValidationIssue(jsonComparisonResult.getValidationIssue());
		validationIssue.setProcessorRequestValidationIssue(xmlComparisonResult.getXmlValidationIssue());

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
				.map(ProcessRequest::getCctRequest).filter(json -> !json.isBlank())
				.orElseThrow(() -> new NoDataFoundException("DECLINED", "NO_DATA_FOUND", "Transaction data not found",
						lookupKey));

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
				.orElseThrow(() -> new NoDataFoundException("DECLINED", "NO_DATA_FOUND",
						"Approved processor request not found", approvedTransaction.getLookupKey()));

		String approvedXml = approvedProcessRequest.getProcessorRequest();
/*
		approvedXml = """
				<?xml version="1.0" encoding="UTF-8"?><Request Version="3" ClientTimeout="30" xmlns="https://stg.dw.us.fdcnet.biz/rc"><ReqClientID><DID>00070043299621013039</DID><App>RAPIDCONNECTSRS</App><Auth>20001318192|00000001</Auth><ClientRef>2723FCDVRAU070</ClientRef></ReqClientID><Transaction><ServiceID>160</ServiceID><Payload Encoding="xml_escape"><GMF xmlns="com/fiserv/Merchant/gmfV12.04"><DebitRequest><CommonGrp><PymtType>Debit</PymtType><TxnType>Sale</TxnType><LocalDateTime>20260707050122</LocalDateTime><TrnmsnDateTime>20260707090122</TrnmsnDateTime><STAN>010507</STAN><RefNum>707050122</RefNum><TPPID>RAU070</TPPID><TermID>00000001</TermID><MerchID>318192</MerchID><MerchCatCode>5541</MerchCatCode><POSEntryMode>051</POSEntryMode><POSCondCode>00</POSCondCode><TermCatCode>12</TermCatCode><TermEntryCapablt>12</TermEntryCapablt><TxnAmt>000000000001</TxnAmt><TxnCrncy>840</TxnCrncy><TermLocInd>0</TermLocInd><CardCaptCap>1</CardCaptCap><GroupID>20001</GroupID><MerchEcho>5f31e78f-8247-486b-9b4e-5641fabb830a</MerchEcho></CommonGrp><CardGrp><Track2Data>67999989000002010=4912201148359490000</Track2Data></CardGrp><PINGrp><PINData>FFFFFFFFFFFFFFFF</PINData><KeySerialNumData>00000000000000000000</KeySerialNumData></PINGrp><AddtlAmtGrp><PartAuthrztnApprvlCapablt>1</PartAuthrztnApprvlCapablt></AddtlAmtGrp><EMVGrp><EMVData>82021800950580000080009A032607079C01005F24034912315F2A0208405F3401119F02060000000001009F03060000000000009F090200029F1A0208409F1E0835323237363431379F2608311B5006DBC083E89F2701809F3303E0F8C89F34034103029F3501229F360200429F37042FD9BC079F3901059F4104000001029F530152840BA0000000043060D05611119F10120110A50009020000000000000000000000FF9F6E07084000003030009F0607A00000000430609F0702FFC05F300202014F0BA0000000043060D0561111</EMVData><CardSeqNum>011</CardSeqNum></EMVGrp></DebitRequest></GMF></Payload></Transaction></Request>

				       			""";

		declinedXml = """
				<?xml version="1.0" encoding="UTF-8"?><Request Version="3" ClientTimeout="30" xmlns="https://prod.dw.us.fdcnet.biz/rc"><ReqClientID><DID>00179018495193182915</DID><App>RAPIDCONNECTSRS</App><Auth>20001440541|00000004</Auth><ClientRef>E365738VRAU070</ClientRef></ReqClientID><Transaction><ServiceID>160</ServiceID><Payload Encoding="xml_escape"><GMF xmlns="com/fiserv/Merchant/gmfV12.04"><DebitRequest><CommonGrp><PymtType>Debit</PymtType><TxnType>Sale</TxnType><LocalDateTime>20260712211702</LocalDateTime><TrnmsnDateTime>20260713011702</TrnmsnDateTime><STAN>072752</STAN><RefNum>712211702</RefNum><TPPID>RAU070</TPPID><TermID>00000004</TermID><MerchID>440541</MerchID><MerchCatCode>5621</MerchCatCode><POSEntryMode>071</POSEntryMode><POSCondCode>00</POSCondCode><TermCatCode>12</TermCatCode><TermEntryCapablt>12</TermEntryCapablt><TxnAmt>000000000776</TxnAmt><TxnCrncy>840</TxnCrncy><TermLocInd>0</TermLocInd><CardCaptCap>1</CardCaptCap><GroupID>20001</GroupID><MerchEcho>95a72701-7e06-4d33-9468-a5a8563fcabd</MerchEcho></CommonGrp><CardGrp><Track2Data>4124880303887507=31122010000018099995</Track2Data></CardGrp><PINGrp><PINData>D19D6BFC67B33F81</PINData><MSKeyID>CEXU211826</MSKeyID></PINGrp><AddtlAmtGrp><PartAuthrztnApprvlCapablt>1</PartAuthrztnApprvlCapablt></AddtlAmtGrp><EMVGrp><EMVData>82020060950500000000009A032607129C01005F24033112315F2A0208405F3401009F02060000000007769F03060000000000009F090200969F1A0208409F1E0832363432353138379F2608697BEDA8BA7EC8889F2701809F3303E068C89F34034203009F3501229F3602003B9F3704539B801C9F3901079F4104000000488407A00000009808409F10201F42016EA00000000010030273000000004000000000000000000000000000009F6E04238800009F0607A00000009808409F0702C0809F660436C040004F07A0000000980840</EMVData><CardSeqNum>000</CardSeqNum></EMVGrp></DebitRequest></GMF></Payload></Transaction></Request>

				""";
				*/
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

	private CustomResponse buildCustomResponse(String lookupKey, Object message, String status) {
		CustomResponse customResponse = new CustomResponse();
		customResponse.setLookupKey(lookupKey);
		customResponse.setMessage(message);
		customResponse.setStatus(status);
		return customResponse;
	}
}