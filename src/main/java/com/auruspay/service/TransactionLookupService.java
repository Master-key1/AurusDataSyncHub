package com.auruspay.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.auruspay.dto.ProcessRequest;
import com.auruspay.dto.TransactionLookupResponse;
import com.auruspay.fiserv.logservice.exception.NoDataFoundException;
import com.auruspay.fiserv.util.TxnUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class TransactionLookupService {

	private static final Logger log = LoggerFactory.getLogger(TransactionLookupService.class);

	private final ObjectMapper objectMapper;

	// FD
	static final List<String> FD_PROCESSOR_LIST = List.of("30", "33", "45", "48");

	// VANTIV
	static final List<String> VANTIV_PROCESSOR_LIST = List.of("23", "32");

	// CHASE
	static final List<String> CHASE_PROCESSOR_LIST = List.of("47", "61");


    // Single JSON file for all supported processors
    @Value("${app.data.file-path:data/Aurus.json}")
    private String filePath;
    
    @Value("${app.data.file-path:data/Fiserv.json}")
    private String FiservFilePath;
    
    @Value("${app.data.file-path:data/Vantiv.json}")
    private String VantivFilePath;
    
    @Value("${app.data.file-path:data/Chase.json}")
    private String ChaseFilePath;

//	@Value("${app.data.file-path:data/data.json}")
//	private String dataFilePath;

	public TransactionLookupService(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	public TransactionLookupResponse lookupTransaction(ProcessRequest request, String processorId) throws Exception {

		String lookupKey = null;
		TransactionLookupResponse response = new TransactionLookupResponse();

		try {
			String cctRequest = sanitizeRequest(request.getCctRequest());
			String cctResponse = sanitizeRequest(request.getCctResponse());
			
			log.info("Transaction lookup started for processorId={}", processorId);

			lookupKey = TxnUtil.generateTxnId(objectMapper, cctRequest,cctResponse, processorId);

			log.info("Generated lookup key={}", lookupKey);

			JsonNode transactionNode = getTransactionNode(lookupKey,processorId);

			if (transactionNode == null || transactionNode.isNull()) {
				log.warn("Transaction node not found for lookupKey = {}", lookupKey);
				throw new NoDataFoundException("FAILED", "NO_DATA_FOUND",
						"Transaction data was not found against the generated key.", lookupKey);
			}

			log.info("transactionNode ={}", transactionNode);
			ProcessRequest approvedProcessorRequest = buildSuccessResponse(transactionNode);

			response.setLookupKey(lookupKey);
			response.setProcessRequest(approvedProcessorRequest);
			response.setStatus("success");

			log.info("Transaction lookup successful. lookupKey={}", lookupKey);

			return response;

		} catch (NoDataFoundException e) {

			log.warn("No transaction found. lookupKey={}, message={}", lookupKey, e.getMessage());
			response.setLookupKey(lookupKey);
			response.setStatus(e.getStatus());

			return response;

		} catch (Exception e) {

			log.error("Transaction lookup failed. lookupKey={}", lookupKey, e);
			response.setLookupKey(lookupKey);
			response.setStatus("FAILED");

			return response;
		}
	}

	private JsonNode getTransactionNode(String lookupKey, String processorId) throws IOException {

	    Path path;

	    if (FD_PROCESSOR_LIST.contains(processorId)) {
	        path = Path.of(FiservFilePath);
	    } else if (VANTIV_PROCESSOR_LIST.contains(processorId)) {
	        path = Path.of(VantivFilePath);
	    } else if (CHASE_PROCESSOR_LIST.contains(processorId)) {
	        path = Path.of(ChaseFilePath);
	    } else {
	        throw new IllegalArgumentException("Unsupported processor ID: " + processorId);
	    }

	    if (!Files.isRegularFile(path)) {
	        log.error("Transaction data file missing: {}", path.toAbsolutePath());
	        throw new IOException("Transaction data file not found: " + path);
	    }

	    JsonNode root = objectMapper.readTree(path.toFile());

	    JsonNode transactionNode = root.get(lookupKey);

	    if (transactionNode == null) {
	        log.warn("Transaction data not found for lookupKey: {}", lookupKey);
	    }

	    return transactionNode;
	}
	private ProcessRequest buildSuccessResponse(JsonNode node) {

		if (node == null || node.isNull()) {
			throw new IllegalArgumentException("Transaction node is empty");
		}

		ProcessRequest response = new ProcessRequest();

		response.setCctRequest(extract(node, "cct_request"));
		response.setProcessorRequest(extract(node, "processor_request"));
		response.setProcessorResponse(extract(node, "processor_response"));
		response.setCctResponse(extract(node, "cct_response"));

		return response;
	}

	private String extract(JsonNode node, String field) {

		if (node == null || node.isNull()) {
			return null;
		}

		JsonNode valueNode = node.get(field);

		if (valueNode == null || valueNode.isNull()) {
			return null;
		}

		try {
			if (valueNode.isObject() || valueNode.isArray()) {
				return objectMapper.writeValueAsString(valueNode);
			}

			return valueNode.asText();

		} catch (Exception e) {
			log.warn("Unable to extract field={}", field, e);
			return null;
		}
	}

	private String sanitizeRequest(String request) {

		if (request == null || request.isBlank()) {
			throw new IllegalArgumentException("CCT request cannot be empty");
		}

		return request.replaceAll(",\\s*}", "}");
	}
}