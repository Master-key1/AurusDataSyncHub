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

@Service
public class JsonDataAddService {

    private static final Logger log =
            LoggerFactory.getLogger(JsonDataAddService.class);

    @Autowired
    private AurusDecryptor aurusDecryptor;

    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${app.data.file-path:data/data.json}")
    private String filePath;

    // ================= SAVE DATA =================
    public synchronized String saveData(ProcessRequest request) throws Exception {

    	

    	String cctRequest = request.getCctRequest() != null
    	        ? aurusDecryptor.decryptor(request.getCctRequest())
    	        : null;
    	log.info("Decrypted CCT Request: {}", cctRequest);

    	String processorRequest = request.getProcessorRequest() != null
    	        ? aurusDecryptor.decryptor(request.getProcessorRequest())
    	        : null;
    	log.info("Decrypted Processor Request*: {}", processorRequest);

    	String processorResponse = request.getProcessorResponse() != null
    	        ? aurusDecryptor.decryptor(request.getProcessorResponse())
    	        : null;
    	log.info("Decrypted Processor Response*: {}", processorResponse);

    	String cctResponse = request.getCctResponse() != null
    	        ? aurusDecryptor.decryptor(request.getCctResponse())
    	        : null;
    	log.info("Decrypted CCT Response: {}", cctResponse);
    	String txnId = generateTxnId(cctRequest);

    	File file = new File(filePath);

    	log.info("Using data file: {}", file.getAbsolutePath());

    	Map<String, Object> finalJson = loadExisting(file);

    	if (finalJson.containsKey(txnId)) {
    	    log.warn("Transaction already exists: {}", txnId);
    	    return "Already exists: " + txnId;
    	}

    	Map<String, Object> txnData = new LinkedHashMap<>();

    	txnData.put("cct_request",
    	        safeReadTree(cctRequest));

    	txnData.put("processor_request",
    	        (processorRequest));

    	txnData.put("processor_response",
    	        (processorResponse));

    	txnData.put("cct_response",
    	        safeReadTree(cctResponse));

    	finalJson.put(txnId, txnData);

    	mapper.writerWithDefaultPrettyPrinter()
    	        .writeValue(file, finalJson);

    	log.info("Transaction saved successfully: {}", txnId);

    	return txnId;
    	}

    // ================= CLEAN INPUT =================
    private String clean(String value) {

        if (value == null) {
            return null;
        }

        return value
                .replace("\\r\\n", "")
                .replace("\r\n", "")
                .replace("\n", "")
                .replace("\r", "")
                .replaceAll(",\\s*}", "}")   // remove trailing commas
                .trim();
    }

    // ================= SAFE JSON PARSER =================
    private JsonNode safeReadTree(String value) {

        try {

            if (value == null || value.trim().isEmpty()) {
                return null;
            }

            return mapper.readTree(clean(value));

        } catch (Exception e) {

            log.warn("Invalid JSON skipped");
            return null;
        }
    }

    // ================= LOAD EXISTING FILE =================
    @SuppressWarnings("unchecked")
    private Map<String, Object> loadExisting(File file) {

        try {

            if (!file.exists()) {

                File parent = file.getParentFile();

                if (parent != null && !parent.exists()) {
                    parent.mkdirs();
                }

                file.createNewFile();

                Map<String, Object> empty = new LinkedHashMap<>();

                mapper.writerWithDefaultPrettyPrinter()
                        .writeValue(file, empty);

                log.info("Created new data file: {}", file.getAbsolutePath());

                return empty;
            }

            if (file.length() > 0) {
                return mapper.readValue(file, LinkedHashMap.class);
            }

        } catch (Exception e) {
            log.error("Error loading file: {}", file.getAbsolutePath(), e);
        }

        return new LinkedHashMap<>();
    }

    // ================= TXN ID GENERATION =================
    private String generateTxnId(String cctRequest) {

        try {

            String cleanedRequest = clean(cctRequest);

            Map<String, Object> map =
                    mapper.readValue(cleanedRequest, LinkedHashMap.class);

            return String.join("_",
                    "FD",
                    getValue(map, "3.1"),
                    getValue(map, "3.5"),
                    getValue(map, "3.21"),
                    getValue(map, "4.1"),
                    getValue(map, "4.3"),
                    getValue(map, "4.20"),
                    getValue(map, "4.21"),
                    getValue(map, "4.30"),
                    getValue(map, "4.40"));

        } catch (Exception e) {

            log.error("TxnId generation failed", e);
            return "FD_UNKNOWN_TXN";
        }
    }

    private String getValue(Map<String, Object> map, String key) {

        Object value = map.get(key);

        return value == null
                ? "NA"
                : String.valueOf(value);
    }
}