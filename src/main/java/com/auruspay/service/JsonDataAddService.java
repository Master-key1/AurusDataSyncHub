package com.auruspay.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.auruspay.decryptor.AurusDecryptor;
import com.auruspay.dto.ProcessRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class JsonDataAddService {

	private static final Logger log = LoggerFactory.getLogger(JsonDataAddService.class);

	@Autowired
	private AurusDecryptor aurusDecryptor;

	private final ObjectMapper mapper = new ObjectMapper();

	@Value("${app.data.file-path:data/data.json}")
	private String filePath;

	// 🚨 Remove ALL control characters safely
	private static final Pattern CTRL_CHARS = Pattern.compile("[\\x00-\\x1F&&[^\\n\\r\\t]]");

	// ================= SAVE DATA =================
	public synchronized String saveData(ProcessRequest request) throws Exception {

		String cctRequest = decrypt(request.getCctRequest());
		String processorRequest = decrypt(request.getProcessorRequest());
		String processorResponse = decrypt(request.getProcessorResponse());
		String cctResponse = decrypt(request.getCctResponse());

		String txnId = generateTxnId(cctRequest);

		File file = new File(filePath);
		log.info("Using data file: {}", file.getAbsolutePath());

		Map<String, Object> finalJson = loadExisting(file);

		if (finalJson.containsKey(txnId)) {
			log.warn("Transaction already exists: {}", txnId);
			return "Already exists: " + txnId;
		}

		Map<String, Object> txnData = new LinkedHashMap<>();

		txnData.put("cct_request", safeParse(cctRequest));
		txnData.put("processor_request", processorRequest);
		txnData.put("processor_response", processorResponse);
		txnData.put("cct_response", safeParse(cctResponse));

		finalJson.put(txnId, txnData);

		mapper.writerWithDefaultPrettyPrinter().writeValue(file, finalJson);

		log.info("Transaction saved successfully: {}", txnId);

		return txnId;
	}

	// ================= DECRYPT =================
	private String decrypt(String value) {
		try {
			if (value == null)
				return null;
			return aurusDecryptor.decryptor(value);
		} catch (Exception e) {
			log.error("Decryption failed", e);
			return null;
		}
	}

	// ================= SAFE JSON PARSER =================
	private JsonNode safeParse(String value) {
		try {
			if (value == null || value.isBlank())
				return null;

			String cleaned = clean(value);

			// validate JSON before parsing
			return mapper.readTree(cleaned);

		} catch (Exception e) {
			log.error("Invalid JSON payload skipped: {}", truncate(value));
			return null;
		}
	}

	// ================= CLEAN INPUT (PRODUCTION SAFE) =================
	private String clean(String value) {
		if (value == null)
			return null;

		String cleaned = value.replaceAll("\\\\r\\\\n", "").replace("\r", "").replace("\n", "");

		// remove illegal control chars
		cleaned = CTRL_CHARS.matcher(cleaned).replaceAll("");

		return cleaned.trim();
	}

	// ================= LOAD EXISTING FILE =================
	@SuppressWarnings("unchecked")
	private Map<String, Object> loadExisting(File file) {

		try {
			if (!file.exists()) {
				file.getParentFile().mkdirs();
				file.createNewFile();

				Map<String, Object> empty = new LinkedHashMap<>();
				mapper.writeValue(file, empty);

				return empty;
			}

			if (file.length() > 0) {
				return mapper.readValue(file, LinkedHashMap.class);
			}

		} catch (Exception e) {
			log.error("Error loading file", e);
		}

		return new LinkedHashMap<>();
	}

	// ================= TXN ID =================
	private String generateTxnId(String cctRequest) {

		try {
			String cleaned = clean(cctRequest);

			Map<String, Object> map = mapper.readValue(cleaned, LinkedHashMap.class);

			return String.join("_", "FD", get(map, "3.1"), get(map, "3.5"), get(map, "3.21"), get(map, "4.1"),
					get(map, "4.3"), get(map, "4.20"), get(map, "4.21"), get(map, "4.30"), get(map, "4.40"));

		} catch (Exception e) {
			log.error("TxnId generation failed", e);
			return "FD_UNKNOWN_TXN";
		}
	}

	private String get(Map<String, Object> map, String key) {
		Object val = map.get(key);
		return val == null ? "NA" : String.valueOf(val);
	}

	private String truncate(String value) {
		if (value == null)
			return "null";
		return value.length() > 200 ? value.substring(0, 200) + "..." : value;
	}
}