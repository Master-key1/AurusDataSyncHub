
package com.auruspay.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.auruspay.decryptor.AurusDecryptor;
import com.auruspay.dto.ProcessRequest;
import com.auruspay.fiserv.util.TxnUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class JsonDataAddService {

    private static final Logger log =
            LoggerFactory.getLogger(JsonDataAddService.class);

    @Autowired
    private AurusDecryptor aurusDecryptor;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Single JSON file for all supported processors
    @Value("${app.data.file-path:data/Aurus.json}")
    private String filePath;
    
    @Value("${app.data.file-path:data/Fiserv.json}")
    private String FiservFilePath;
    
    @Value("${app.data.file-path:data/Vantiv.json}")
    private String VantivFilePath;
    
    @Value("${app.data.file-path:data/Chase.json}")
    private String ChaseFilePath;

	// FD
	static final List<String> FD_PROCESSOR_LIST = List.of("30", "33", "45", "48");

	// VANTIV
	static final List<String> VANTIV_PROCESSOR_LIST = List.of("23", "32");

	// CHASE
	static final List<String> CHASE_PROCESSOR_LIST = List.of("47", "61");
    // ================= SAVE DATA =================
    public synchronized String saveData(ProcessRequest request)
            throws Exception {

        log.info("========== Save transaction started ==========");

        if (request == null) {
            throw new IllegalArgumentException(
                    "ProcessRequest cannot be null");
        }

        if (request.getProcessorId() == null
                || request.getProcessorId().isBlank()) {
            throw new IllegalArgumentException(
                    "Processor ID cannot be null or blank");
        }

        log.info(
                "Request received. cctRequest present={}, " +
                "processorRequest present={}, processorResponse present={}, " +
                "cctResponse present={}, processorId present={}",
                request.getCctRequest() != null,
                request.getProcessorRequest() != null,
                request.getProcessorResponse() != null,
                request.getCctResponse() != null,
                true
        );

        String cctRequest = decrypt(request.getCctRequest());
        String processorRequest = decrypt(request.getProcessorRequest());
        String processorResponse = decrypt(request.getProcessorResponse());
        String cctResponse = decrypt(request.getCctResponse());

        String processorId = request.getProcessorId()
                .replace("[", "")
                .replace("]", "")
                .trim();

        // Do not log decrypted payment payloads.
        log.info("Decryption completed. processorId={}", processorId);

        String txnId = TxnUtil.generateTxnId(
                objectMapper,
                cctRequest,
                cctResponse,
                processorId
        );

        log.info("Generated Transaction ID: {}", txnId);

        
        Path dataPath= null;
        if(FD_PROCESSOR_LIST.contains(processorId)) {
        	if (FiservFilePath == null || FiservFilePath.isBlank()) {
                throw new IllegalStateException(
                        "Property app.data.file-path is blank");
            }
        	dataPath = Paths.get(FiservFilePath)
                    .toAbsolutePath()
                    .normalize();
        }else if(VANTIV_PROCESSOR_LIST.contains(processorId)) {
        	if (VantivFilePath == null || VantivFilePath.isBlank()) {
                throw new IllegalStateException(
                        "Property app.data.file-path is blank");
            }
        	dataPath = Paths.get(VantivFilePath)
                    .toAbsolutePath()
                    .normalize();
        }else if(CHASE_PROCESSOR_LIST.contains(processorId)) {
        	if (ChaseFilePath == null || ChaseFilePath.isBlank()) {
                throw new IllegalStateException(
                        "Property app.data.file-path is blank");
            }
        	dataPath = Paths.get(ChaseFilePath)
                    .toAbsolutePath()
                    .normalize();
        }else {
        	if (filePath == null || filePath.isBlank()) {
                throw new IllegalStateException(
                        "Property app.data.file-path is blank");
            }
        	dataPath = Paths.get(filePath)
                    .toAbsolutePath()
                    .normalize();
        	}
        	

        File file = dataPath.toFile();

        // Create missing parent directories automatically.
        ensureParentDirectory(dataPath);

        log.info("Data file location: {}", file.getAbsolutePath());

        // Load existing transactions or initialize a new JSON file.
        Map<String, Object> finalJson = loadExisting(file);

        log.debug("Existing transaction count: {}", finalJson.size());

        // Check for duplicate transactions.
        if (finalJson.containsKey(txnId)) {
            log.warn("Duplicate transaction found: {}", txnId);
            return "Already exists: " + txnId;
        }

        // Prepare transaction data.
        Map<String, Object> txnData = new LinkedHashMap<>();

        txnData.put("cct_request", safeParse(cctRequest));
        txnData.put("processor_request", processorRequest);
        txnData.put("processor_response", processorResponse);
        txnData.put("cct_response", safeParse(cctResponse));

        finalJson.put(txnId, txnData);

        // Write the updated transaction map to the JSON file.
        try {
            objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValue(file, finalJson);

            log.info(
                    "Transaction written successfully. txnId={}, totalRecords={}",
                    txnId,
                    finalJson.size()
            );

        } catch (Exception e) {
            log.error(
                    "Failed writing transaction data. txnId={}, file={}",
                    txnId,
                    file.getAbsolutePath(),
                    e
            );
            throw e;
        }

        log.info("========== Save transaction completed ==========");

        return txnId;
    }

    // ================= DECRYPT =================
    private String decrypt(String value) {

        if (value == null) {
            log.warn("Decrypt skipped because input value is null");
            return null;
        }

        try {
            log.debug("Decrypt started. Input length={}", value.length());

            String result = aurusDecryptor.decryptor(value.trim());

            log.debug(
                    "Decrypt successful. Output length={}",
                    result == null ? 0 : result.length()
            );

            return result;

        } catch (Exception e) {
            log.error(
                    "Decryption failed. Input length={}",
                    value.length(),
                    e
            );

            throw new IllegalStateException(
                    "Unable to decrypt request field", e);
        }
    }

    // ================= SAFE JSON PARSER =================
    private JsonNode safeParse(String value) {

        if (value == null || value.isBlank()) {
            log.warn("JSON parsing skipped because payload is empty");
            return null;
        }

        try {
            String cleaned = TxnUtil.clean(value);

            JsonNode node = objectMapper.readTree(cleaned);

            log.debug(
                    "JSON parsing successful. Node type={}",
                    node == null ? "null" : node.getNodeType()
            );

            return node;

        } catch (Exception e) {
            log.error("Invalid JSON payload; unable to parse transaction field", e);
            return null;
        }
    }

    // ================= ENSURE DIRECTORY =================
    private void ensureParentDirectory(Path path) throws IOException {

        Path parent = path.getParent();

        if (parent != null) {
            Files.createDirectories(parent);

            if (!Files.isDirectory(parent)) {
                throw new IOException(
                        "Parent path is not a directory: " + parent);
            }

            if (!Files.isWritable(parent)) {
                throw new IOException(
                        "Parent directory is not writable: " + parent);
            }

            log.debug("Data directory is ready: {}", parent);
        }
    }

    // ================= LOAD EXISTING FILE =================
    private Map<String, Object> loadExisting(File file) throws IOException {

        if (!file.exists()) {
            log.info(
                    "Data file does not exist. Initializing: {}",
                    file.getAbsolutePath()
            );

            Map<String, Object> empty = new LinkedHashMap<>();

            objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValue(file, empty);

            return empty;
        }

        if (!file.isFile()) {
            throw new IOException(
                    "Configured data path is not a regular file: "
                            + file.getAbsolutePath());
        }

        if (file.length() == 0) {
            log.info(
                    "Data file is empty. Starting with an empty transaction map: {}",
                    file.getAbsolutePath()
            );

            return new LinkedHashMap<>();
        }

        Map<String, Object> data = objectMapper.readValue(
                file,
                new TypeReference<LinkedHashMap<String, Object>>() {}
        );

        if (data == null) {
            throw new IOException(
                    "JSON data file contains null instead of a transaction object: "
                            + file.getAbsolutePath());
        }

        log.info(
                "Loaded {} transactions from {}",
                data.size(),
                file.getAbsolutePath()
        );

        return data;
    }
}
