package com.auruspay.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.auruspay.decryptor.AurusDecryptor;
import com.auruspay.dto.ProcessRequest;
import com.auruspay.service.JsonDataAddService;
import com.auruspay.service.TransactionLookupService;
import com.auruspay.util.FileReadData;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Transaction APIs", description = "Process, Lookup and Compare transactions")
@RestController
public class DataSyncController {
	private static final Logger log = LoggerFactory.getLogger(DataSyncController.class);
	
	private final JsonDataAddService jsonDataAddService;
	private final TransactionLookupService transactionLookupService;
	private final AurusDecryptor aurusDecryptor;
	
	public DataSyncController(JsonDataAddService jsonDataAddService, TransactionLookupService transactionLookupService,
			AurusDecryptor aurusDecryptor) {
		super();
		this.jsonDataAddService = jsonDataAddService;
		this.transactionLookupService = transactionLookupService;
		this.aurusDecryptor = aurusDecryptor;
	}

	@GetMapping("/save")
	public ResponseEntity<?> submitForms() {
		log.info("Received transaction save request");

		List<String> directoryPaths = List.of("/home/adatal@offaurusinc.com/Videos/Logs_Data/LOG_6_0_1/DOWNLOAD");

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
}
