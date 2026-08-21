package com.auruspay.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Shared helpers for transaction-id generation and payload cleaning.
 * Extracted from JsonDataAddService / TransactionLookupService, which
 * previously duplicated this logic.
 */
public final class TxnUtil {

	private static final Logger log = LoggerFactory.getLogger(TxnUtil.class);

	public static final String PREFIX = "FD";
	public static final String NOT_AVAILABLE = "NA";
	public static final String UNKNOWN_TXN = "FD_UNKNOWN_TXN";

	private static final Pattern CTRL_CHARS = Pattern.compile("[\\x00-\\x1F&&[^\\n\\r\\t]]");

	private TxnUtil() {
		// utility class, no instances
	}

	// ================= CLEAN INPUT =================
	public static String clean(String value) {

		if (value == null) {
			return null;
		}

		int originalLength = value.length();

		String cleaned = value
				.replaceAll("\\\\r\\\\n", "")
				.replace("\r", "")
				.replace("\n", "");

		cleaned = CTRL_CHARS.matcher(cleaned).replaceAll("");

		log.trace("Clean completed. Before={}, After={}", originalLength, cleaned.length());

		return cleaned.trim();
	}

	// ================= TXN ID =================
	public static String generateTxnId(ObjectMapper objectMapper, String cctRequestJson, String processorId) {

		try {
			if (cctRequestJson == null || cctRequestJson.isBlank()) {
				log.error("Cannot generate txnId. cctRequest is empty");
				return UNKNOWN_TXN;
			}
			if (processorId == null || processorId.isBlank()) {
				log.error("Cannot generate txnId. processorId is empty");
				return UNKNOWN_TXN;
			}

			log.debug("CCT : {}", cctRequestJson);

			String cleaned = clean(cctRequestJson);

			Map<String, Object> requestMap = objectMapper.readValue(
					cleaned,
					new TypeReference<LinkedHashMap<String, Object>>() {}
			);
			//FD_30_ 8_ 1.0 _2.40 _ 3_ 15 _ 840 _NA_NA_NA_NA_NA_NA
			
			String txnId = String.join("_",
					PREFIX + "_" + safeValue(processorId),
					safeValue(getValue(requestMap, "3.1")),
					safeValue(getValue(requestMap, "3.5")),
					safeValue(getValue(requestMap, "3.21")),
					safeValue(getValue(requestMap, "4.1")),
					safeValue(getValue(requestMap, "4.3")),
					safeValue(getValue(requestMap, "4.20")),
					safeValue(getValue(requestMap, "4.21")),
					safeValue(getValue(requestMap, "4.30")),
					safeValue(getValue(requestMap, "4.32")),
					safeValue(getValue(requestMap, "4.40")),
					safeValue(getValue(requestMap, "4.36")),
					safeValue(getValue(requestMap, "4.67"))
			);

			log.info("TxnId generated successfully: {}", txnId);

			return txnId;

		} catch (Exception e) {
			log.error("TxnId generation failed. cctRequest preview={}", truncate(cctRequestJson), e);
			return UNKNOWN_TXN;
		}
	}

	public static String safeValue(Object value) {

		if (value == null || value.toString().isBlank() || value.toString().isEmpty()) {
			return NOT_AVAILABLE;
		}

		return String.valueOf(value).trim();
	}

	public static String getValue(Map<String, Object> map, String key) {

		Object value = map.get(key);

		if (value == null || value.toString().isBlank() || value.toString().isEmpty()) {
			log.warn("Missing txnId field: {}", key);
			return NOT_AVAILABLE;
		}

		return String.valueOf(value);
	}

	public static String truncate(String value) {

		if (value == null) {
			return "null";
		}

		return value.length() > 200
				? value.substring(0, 200) + "..."
				: value;
	}
}