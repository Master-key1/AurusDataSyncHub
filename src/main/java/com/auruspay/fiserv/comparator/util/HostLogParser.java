package com.auruspay.fiserv.comparator.util;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.auruspay.decryptor.AurusDecryptor;
import com.fasterxml.jackson.databind.JsonNode;

@Component
public class HostLogParser {

	private static final Logger log = LoggerFactory.getLogger(HostLogParser.class);

	/**
	 * Existing pattern for fields like:
	 *
	 * processorId=123 imfProcessorId=456
	 */
	private static final Pattern FIELD_PATTERN = Pattern.compile("(\\w+)\\s*=\\s*([^,\\]]+)");

	/**
	 * Dynamic pattern for log fields like:
	 *
	 * [CARD TYPE : VI] [SUB CARD TYPE : ] [CARD HOLDER ID CODE : 1]
	 *
	 * Group 1 = Field Name Group 2 = Field Value
	 */
	private static final Pattern BRACKET_FIELD_PATTERN = Pattern.compile("\\[\\s*([^:\\]]+?)\\s*:\\s*([^\\]]*)\\]");

	/**
	 * Extract processorId from:
	 *
	 * PROCESSOR TERMINAL DETAILS
	 */
	private String getProcessorId3(String line) {

		if (line == null || !line.contains("PROCESSOR TERMINAL DETAILS")) {

			return null;
		}

		Matcher matcher = FIELD_PATTERN.matcher(line);

		while (matcher.find()) {

			String fieldName = matcher.group(1);
			String fieldValue = matcher.group(2).trim();

			if ("processorId".equals(fieldName)) {
				return fieldValue;
			}
		}

		return null;
	}

	/**
	 * Extract processorId or imfProcessorId.
	 */
	private String getProcessorId(String line) {

		if (line == null || !line.contains("PROCESSOR TERMINAL DETAILS")) {

			return null;
		}

		Matcher matcher = FIELD_PATTERN.matcher(line);

		String processorId = null;
		String imfProcessorId = null;

		while (matcher.find()) {

			String fieldName = matcher.group(1);

			String fieldValue = matcher.group(2);

			if (fieldValue != null) {
				fieldValue = fieldValue.trim();
			}

			if ("processorId".equals(fieldName)) {

				processorId = fieldValue;

			} else if ("imfProcessorId".equals(fieldName)) {

				imfProcessorId = fieldValue;
			}
		}

		/*
		 * If both processor IDs are null/empty, throw exception.
		 */
		if ((processorId == null || processorId.isEmpty()) && (imfProcessorId == null || imfProcessorId.isEmpty())) {

			throw new IllegalStateException("Both processorId and imfProcessorId are null or empty");
		}

		/*
		 * Return processorId if available. Otherwise return IMF processor ID.
		 */
		return processorId != null && !processorId.isEmpty()

				? processorId

				: imfProcessorId;
	}

	/**
	 * Extract dynamic fields from log lines.
	 *
	 * Example input:
	 *
	 * [CARD TYPE : AX] [SUB CARD TYPE : ] [CARD HOLDER ID CODE : 1]
	 *
	 * Result:
	 *
	 * CARD TYPE -> AX SUB CARD TYPE -> "" CARD HOLDER ID CODE -> 1
	 */
	private void extractBracketFields(String line, Map<String, String> dynamicFields) {

		if (line == null || line.isEmpty()) {

			return;
		}

		Matcher matcher = BRACKET_FIELD_PATTERN.matcher(line);

		while (matcher.find()) {

			String fieldName = matcher.group(1).trim();

			String fieldValue = matcher.group(2).trim();

			/*
			 * Ignore empty field names.
			 */
			if (fieldName.isEmpty()) {
				continue;
			}

			/*
			 * Convert literal "null" from log into Java null.
			 *
			 * Example:
			 *
			 * [SUB CARD TYPE : null]
			 *
			 * becomes:
			 *
			 * "SUB CARD TYPE" -> null
			 */
			if ("null".equalsIgnoreCase(fieldValue)) {

				fieldValue = null;
			}

			/*
			 * Store dynamically discovered field.
			 */
			dynamicFields.put(fieldName, fieldValue);

			log.debug("Dynamic Field Found - [{}] = [{}]", fieldName, fieldValue);
		}
	}

	/**
	 * Main log parser.
	 */
	public Map<String, String> parseLog(String  txnId, JsonNode jsonNode) {

		long startTime = System.currentTimeMillis();

		Map<String, String> map = new LinkedHashMap<>();

		/*
		 * ============================================================ TRANSACTION
		 * DETAILS ============================================================
		 */

//		String txnId = request.getTxnId();

		//String uuid = request.getUuid();
        String uuid = jsonNode.path("uuid").asText("");

		map.put("TXNID", txnId);
		map.put("UUID", uuid);

		/*
		 * ============================================================ GET LOG DATA
		 * ============================================================
		 */

		String logDetails = jsonNode.path("data").asText("");
		/*
		 * Empty log handling.
		 */
		if (logDetails == null || logDetails.isEmpty()) {

			log.warn("Empty logDetails received for UUID={}", uuid);

			map.put("LogDetails", "");

			map.put("DynamicFields", "{}");

			return map;
		}

		/*
		 * ============================================================ SPLIT LOG INTO
		 * LINES ============================================================
		 */

		String[] logLines = logDetails.split("\\r?\\n");

		/*
		 * ============================================================ ISSUE MAP
		 * ============================================================
		 */

		Map<String, String> issues = new LinkedHashMap<>();

		/*
		 * ============================================================ DYNAMIC FIELD
		 * MAP ============================================================
		 *
		 * Example:
		 *
		 * CARD TYPE=AX SUB CARD TYPE= CARD HOLDER ID CODE=1
		 */
		Map<String, String> dynamicFields = new LinkedHashMap<>();

		String processorId = null;

		/*
		 * ============================================================ PROCESS EACH LOG
		 * LINE ============================================================
		 */

		for (String line : logLines) {

			/*
			 * Ignore null/empty lines.
			 */
			if (line == null || line.isEmpty()) {

				continue;
			}

			/*
			 * ======================================================== UUID FILTER
			 * ========================================================
			 *
			 * Only process lines belonging to current transaction.
			 */
			if (uuid != null && !uuid.isEmpty() && !line.contains(uuid)) {

				continue;
			}

			/*
			 * ======================================================== DYNAMIC BRACKET
			 * FIELD EXTRACTION ========================================================
			 *
			 * Example:
			 *
			 * [CARD TYPE : AX] [SUB CARD TYPE : ] [CARD HOLDER ID CODE : 1]
			 *
			 * Automatically extracts all fields.
			 */
			extractBracketFields(line, dynamicFields);

			/*
			 * ======================================================== PROCESSOR TERMINAL
			 * DETAILS ========================================================
			 */

			if (line.contains("PROCESSOR TERMINAL DETAILS")) {

				try {

					String pid = getProcessorId(line);

					if (pid != null) {

						processorId = pid;
					}

				} catch (IllegalStateException e) {

					log.warn("Unable to extract processorId from line: {}", line);
				}
			}

			log.debug("Processing line: {}", line);

			/*
			 * ======================================================== AURUSPAY ENCRYPTED
			 * REQUEST ========================================================
			 */

			processLine(line, "AURUSPAY ENCRYPTED REQUEST :", "AurusReq", map);

			/*
			 * ======================================================== STPL GRAY STREAM
			 * REQUEST ========================================================
			 */

			if (line.contains("[STPL-GRAY-STREAM]- REQUEST :")) {

				processLine(line, "[STPL-GRAY-STREAM]- REQUEST :", "ProcReq", map);
			}

			/*
			 * ======================================================== STPL GRAY STREAM
			 * TRANSACTION RESPONSE ========================================================
			 */

			if (line.contains("[STPL-GRAY-STREAM]-TRANSACTION RESPONSE :")) {

				processLine(line, "[STPL-GRAY-STREAM]-TRANSACTION RESPONSE :", "ProcRes", map);
			}

			/*
			 * ======================================================== PROCESSOR REQUEST
			 * ========================================================
			 */

			if (line.contains("[STPL-GRAY-STREAM]-PROCESSOR REQUEST :")) {

				processLine(line, "[STPL-GRAY-STREAM]-PROCESSOR REQUEST :", "ProcReq", map);
			}

			/*
			 * ======================================================== PROCESSOR RESPONSE
			 * ========================================================
			 */

			if (line.contains("[STPL-GRAY-STREAM]-PROCESSOR RESPONSE :")) {

				processLine(line, "[STPL-GRAY-STREAM]-PROCESSOR RESPONSE :", "ProcRes", map);
			}

			/*
			 * ======================================================== AURUSPAY ENCRYPTED
			 * RESPONSE ========================================================
			 */

			processLine(line, "AURUSPAY ENCRYPTED RESPONSE :", "AurusRes", map);

			/*
			 * ======================================================== TEMP PROCESSOR
			 * RESPONSE ========================================================
			 */

			if (line.contains("[STPL-GRAY-STREAM]-PROCESSOR RESPONSE")) {

				processLine(line, "[STPL-GRAY-STREAM]-PROCESSOR RESPONSE", "ProcRes", map);
			}

			/*
			 * ======================================================== ISSUE DETECTION
			 * ========================================================
			 */

			if (line.matches(".*(ERROR|Exception|Timeout|Declined|Failed).*")) {

				/*
				 * Uncomment if issue extraction is required.
				 *
				 * issues.put( "Issue " + (issues.size() + 1), line.trim() );
				 */
			}
		}

		/*
		 * ============================================================ FINAL PROCESSOR
		 * ID ============================================================
		 */

		map.put("processorId", processorId);

		/*
		 * ============================================================ DYNAMIC FIELDS
		 * ============================================================
		 *
		 * Example:
		 *
		 * DynamicFields={ CARD TYPE=AX, SUB CARD TYPE=, CARD HOLDER ID CODE=1 }
		 */
		map.put("DynamicFields", String.valueOf(dynamicFields));

		/*
		 * ============================================================ ISSUES
		 * ============================================================
		 */

		map.put("IssuesCount", String.valueOf(issues.size()));

		map.put("Issues", String.valueOf(issues));

		/*
		 * ============================================================ LOG DETAILS
		 * CLEANUP ============================================================
		 */

		String patterns = "/opt/auruspay_switch/log/auruspay/auruspay\\.log(?:-.*)?\\.zip:";

		map.put("LogDetails", jsonNode.path("data").asText("")); //.replaceAll(patterns, "")

		/*
		 * ============================================================ PERFORMANCE LOG
		 * ============================================================
		 */

        //DIRPOLL Logs
        map.put("DirPoll", jsonNode.path("dirpoll").asText(""));

		long duration = System.currentTimeMillis() - startTime;

		log.info("Log parsing completed for UUID={} with {} issues in {} ms", uuid, issues.size(), duration);

		return map;
	}

	/**
	 * Existing processor ID parser.
	 */
	public static String getProcessorId2(String logLine) {

		if (logLine == null || !logLine.contains("ProcessorInfinispan")) {

			return null;
		}

		Pattern pattern = Pattern.compile("processorId=(\\d+)");

		Matcher matcher = pattern.matcher(logLine);

		if (matcher.find()) {

			return matcher.group(1);
		}

		return null;
	}

	/**
	 * Process encrypted request/response.
	 */
	private void processLine(String line, String marker, String mapKey, Map<String, String> map) {

		if (!line.contains(marker)) {
			return;
		}

		try {

			/*
			 * Get encrypted data after marker.
			 */
			String encrypted = line.substring(line.indexOf(marker) + marker.length()).trim();

			/*
			 * Remove whitespace.
			 */
			String sanitized = encrypted.replaceAll("\\s", "");

			log.debug("Decrypting {} data", mapKey);

			/*
			 * Decrypt.
			 */
			String decrypted = AurusDecryptor.decryptor(sanitized);

			/*
			 * Format decrypted data.
			 */
			decrypted = formatData(decrypted);

			/*
			 * Encrypted output.
			 */
			String encryptedText = """
					%s
					""".formatted(sanitized);

			/*
			 * Decrypted output.
			 */
			String decryptedText = """
					%s
					""".formatted(decrypted);

			/*
			 * Put encrypted data.
			 */
			map.put(mapKey + "Encrypted", encryptedText);

			/*
			 * Put decrypted data.
			 */
			map.put(mapKey + "Decrypt", decryptedText);

			log.info("Encrypted: {}", encryptedText);

			log.info("Decrypted: {}", decryptedText);

		} catch (Exception e) {

			log.error("Decryption failed for {} : {}", mapKey, e.getMessage());

			map.put(mapKey + "Decrypt", "DECRYPTION_ERROR: " + e.getMessage());
		}
	}

	/**
	 * Format decrypted data.
	 */
	private String formatData(String data) {

		if (data == null) {
			return "";
		}

		return data.replace("\\n", "").replace("\\r", "");
	}
}