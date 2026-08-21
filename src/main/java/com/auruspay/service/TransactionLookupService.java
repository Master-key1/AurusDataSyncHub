package com.auruspay.service;

import com.auruspay.dto.ProcessRequest;
import com.auruspay.dto.TransactionLookupResponse;
import com.auruspay.logservice.exception.NoDataFoundException;
import com.auruspay.util.TxnUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class TransactionLookupService {

	private static final Logger log = LoggerFactory.getLogger(TransactionLookupService.class);

	private final ObjectMapper objectMapper;

	@Value("${app.data.file-path:data/data.json}")
	private String dataFilePath;

	public TransactionLookupService(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	public TransactionLookupResponse lookupTransaction(ProcessRequest request, String processorId) throws Exception {

		String lookupKey = null;
		TransactionLookupResponse response = new TransactionLookupResponse();

		try {
			String cctRequest = sanitizeRequest(request.getCctRequest());

			log.info("Transaction lookup started for processorId={}", processorId);

			lookupKey = TxnUtil.generateTxnId(objectMapper, cctRequest, processorId);

			log.info("Generated lookup key={}", lookupKey);

			JsonNode transactionNode = getTransactionNode(lookupKey);

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

	private JsonNode getTransactionNode(String lookupKey) throws IOException {

		Path path = Path.of(dataFilePath);

		if (!Files.exists(path)) {
			log.error("Transaction data file missing: {}", path.toAbsolutePath());
			throw new IOException("Transaction data file not found");
		}

		JsonNode root = objectMapper.readTree(path.toFile());

		return root.get(lookupKey);
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