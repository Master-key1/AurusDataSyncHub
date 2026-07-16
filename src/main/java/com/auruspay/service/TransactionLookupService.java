package com.auruspay.service;

import com.auruspay.dto.ProcessRequest;
import com.auruspay.exception.NoDataFoundException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class TransactionLookupService {

	@Value("${app.data.file-path:data/data.json}")
	private String dataFilePath;

	private static final Logger log = LoggerFactory.getLogger(TransactionLookupService.class);

	private final ObjectMapper mapper = new ObjectMapper();

	// ================= PUBLIC API =================
	public ProcessRequest lookupTransaction(ProcessRequest request, String ProcessorId) throws Exception {
		String lookupKey = null;
		try {
			String req = request.getCctRequest().replaceAll(",\\s*}", "}");
			log.info("CCT Request received : {} ",request.getCctRequest());

			 lookupKey = generateTxnIdFromUserInput(req , ProcessorId);
			log.info("Lookup Key: {}", lookupKey);

			File file = new File(dataFilePath);

			if (!file.exists()) {
				log.error("Data file not found: {}", file.getAbsolutePath());
				return errorResponse("DATA_FILE_NOT_FOUND", lookupKey);
			}

			JsonNode root = mapper.readTree(file);
			JsonNode transactionNode = root.get(lookupKey);

			if (transactionNode == null) {
				log.warn("No transaction found for lookup key: {}", lookupKey);
				throw new NoDataFoundException("NO_DATA_FOUND  "+ lookupKey);
			}

			log.info("Transaction found for lookup key: {}", lookupKey);

			return buildResponse(transactionNode, lookupKey);

		} catch (NoDataFoundException e) {
			log.error("Error while processing lookup request { NO_DATA_FOUND }: ", e);
			throw new NoDataFoundException("NO_DATA_FOUND  "+ lookupKey);

		}catch (Exception e) {
			log.error("Error while processing lookup request", e);
			return errorResponse("INTERNAL_SERVER_ERROR", null);

		}
	}

	// ================= SUCCESS RESPONSE =================
	private ProcessRequest buildResponse(JsonNode node, String key) {

		ProcessRequest response = new ProcessRequest();

		response.setCctRequest(extract(node, "cct_request"));
		response.setProcessorRequest(extract(node, "processor_request"));
		response.setProcessorResponse(extract(node, "processor_response"));
		response.setCctResponse(extract(node, "cct_response"));

		return response;
	}

	// ================= ERROR RESPONSE =================
	private ProcessRequest errorResponse(String status, String key) {

		ProcessRequest response = new ProcessRequest();

		String msg = status + " | " + key;

		response.setCctRequest(msg);
		response.setProcessorRequest(msg);
		response.setProcessorResponse(msg);
		response.setCctResponse(msg);

		return response;
	}

	// ================= SAFE JSON EXTRACT =================
	private String extract(JsonNode node, String field) {

		if (node == null || node.get(field) == null || node.get(field).isNull()) {
			return null;
		}

		try {
			JsonNode valueNode = node.get(field);

			// If already JSON object
			if (valueNode.isObject() || valueNode.isArray()) {
				return mapper.writeValueAsString(valueNode);
			}

			return valueNode.asText();

		} catch (Exception e) {
			log.warn("Failed to extract field: {}", field);
			return null;
		}
	}

	// ================= TXN ID GENERATOR =================
	private String generateTxnIdFromUserInput(String cctRequestJson,String ProcessorId) throws Exception {

		Map<String, Object> cctReqObj = mapper.readValue(cctRequestJson, LinkedHashMap.class);

		return String.join("_", "FD_"+ProcessorId,
				getValue(cctReqObj, "3.1"),
				getValue(cctReqObj, "3.5"),
				getValue(cctReqObj, "3.21"),
				getValue(cctReqObj, "4.1"),
				getValue(cctReqObj, "4.3"),
				getValue(cctReqObj, "4.20"),
				getValue(cctReqObj, "4.21"),
				getValue(cctReqObj, "4.30"),
				getValue(cctReqObj, "4.40"));
	}

	// ================= SAFE GET =================
	private String getValue(Map<String, Object> map, String key) {
		Object value = map.get(key);
		return value == null ? "NA" : String.valueOf(value);
	}
}