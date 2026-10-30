
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
import com.auruspay.util.Constants;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class TransactionLookupService {

    private static final Logger log =
            LoggerFactory.getLogger(TransactionLookupService.class);

    private final ObjectMapper objectMapper;
    private  Constants constants;

    // VANTIV
    private static final List<String> VANTIV_PROCESSOR_LIST =
            List.of("23", "32");

    // CHASE
    private static final List<String> CHASE_PROCESSOR_LIST =
            List.of("47", "61");

    @Value("${app.data.file-path:data/Aurus.json}")
    private String filePath;

    @Value("${app.data.fiserv-file-path:data/Fiserv.json}")
    private String fiservFilePath;

    @Value("${app.data.vantiv-file-path:data/Vantiv.json}")
    private String vantivFilePath;

    @Value("${app.data.chase-file-path:data/Chase.json}")
    private String chaseFilePath;

	public TransactionLookupService(ObjectMapper objectMapper) {

		this.objectMapper = objectMapper;

	}

    public TransactionLookupResponse lookupTransaction(
            ProcessRequest request, String processorId) {

        String lookupKey = null;

        TransactionLookupResponse response =
                new TransactionLookupResponse();

        try {

            log.info("Transaction lookup started. ProcessorId={}", processorId);

            String cctRequest = sanitizeRequest(request.getCctRequest());
            String cctResponse = sanitizeRequest(request.getCctResponse());

            lookupKey = TxnUtil.generateTxnId(
                    objectMapper,
                    cctRequest,
                    cctResponse,
                    processorId
            );

            log.info("Generated lookup key={}", lookupKey);

            JsonNode transactionNode =
                    getTransactionNode(lookupKey, processorId);

            if (transactionNode == null || transactionNode.isNull()) {

                throw new NoDataFoundException(
                        "FAILED",
                        "NO_DATA_FOUND",
                        "Transaction data was not found against the generated key.",
                        lookupKey
                );
            }

            ProcessRequest approvedProcessorRequest =
                    buildSuccessResponse(transactionNode);

            response.setLookupKey(lookupKey);
            response.setProcessRequest(approvedProcessorRequest);
            response.setStatus("success");

            log.info("Transaction lookup successful. LookupKey={}", lookupKey);
            log.info("response {}", response.getProcessRequest().toString().subSequence(0, 20));
            log.info("response {}", response.getLookupKey());
            

        } catch (NoDataFoundException e) {

            log.warn("No transaction found. LookupKey={}, Message={}",
                    lookupKey, e.getMessage());

            response.setLookupKey(lookupKey);
            response.setStatus(e.getStatus());

        } catch (Exception e) {

            log.error("Transaction lookup failed. LookupKey={}",
                    lookupKey, e);

            response.setLookupKey(lookupKey);
            response.setStatus("FAILED");
        }
    

        return response;
    }

    private JsonNode getTransactionNode(
            String lookupKey, String processorId) throws IOException {

        Path path = getProcessorFilePath(processorId);

        if (!Files.isRegularFile(path)) {

            log.error("Transaction data file missing: {}",
                    path.toAbsolutePath());

            throw new IOException(
                    "Transaction data file not found: " + path);
        }

        JsonNode root = objectMapper.readTree(path.toFile());

        JsonNode transactionNode = root.get(lookupKey);

        if (transactionNode == null) {
            log.warn("Transaction not found. LookupKey={}", lookupKey);
        }

        return transactionNode;
    }

    private Path getProcessorFilePath(String processorId) {

        if (constants.RAPID_PROCESSOR_LIST.contains(processorId)
                || constants.TRANSARMOR_PROCESSOR_LIST.contains(processorId)) {

            return Path.of(fiservFilePath);
        }

        if (VANTIV_PROCESSOR_LIST.contains(processorId)) {
            return Path.of(vantivFilePath);
        }

        if (CHASE_PROCESSOR_LIST.contains(processorId)) {
            return Path.of(chaseFilePath);
        }

        if (List.of("30", "33", "45", "48").contains(processorId)) {
            return Path.of(filePath);
        }

        throw new IllegalArgumentException(
                "Unsupported processor ID: " + processorId);
    }

    private ProcessRequest buildSuccessResponse(JsonNode node) {

        if (node == null || node.isNull()) {
            throw new IllegalArgumentException(
                    "Transaction node is empty");
        }

        ProcessRequest response = new ProcessRequest();

        response.setCctRequest(extract(node, "cct_request"));
        response.setProcessorRequest(extract(node, "processor_request"));
        response.setProcessorResponse(extract(node, "processor_response"));
        response.setCctResponse(extract(node, "cct_response"));

        return response;
    }

    private String extract(JsonNode node, String field) {

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
            throw new IllegalArgumentException(
                    "CCT request cannot be empty");
        }

        return request.replaceAll(",\\s*}", "}");
    }
}
