package com.auruspay.service;

import com.auruspay.dto.ProcessRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class TransactionLookupService {

    private final ObjectMapper mapper = new ObjectMapper();

    // ================= PUBLIC API =================
    public ProcessRequest lookupTransaction(String requestJson) throws Exception {

        String lookupKey = generateTxnIdFromUserInput(requestJson);

        System.out.println("🔑 Lookup Key: " + lookupKey);

        File file = new File("static/data.json");

        if (!file.exists()) {
            return errorResponse("DATA_FILE_NOT_FOUND", lookupKey);
        }

        JsonNode root = mapper.readTree(file);

        JsonNode transactionNode = root.get(lookupKey);

        if (transactionNode == null) {
            return errorResponse("NO_DATA_FOUND", lookupKey);
        }

        return buildResponse(transactionNode, lookupKey);
    }

    // ================= SUCCESS RESPONSE =================
    private ProcessRequest buildResponse(JsonNode node, String key) {

        ProcessRequest response = new ProcessRequest();

        response.setCctRequest(cleanJson(node.get("cct_request")));
        response.setProcessorRequest(cleanJson(node.get("processor_request")));
        response.setProcessorResponse(cleanJson(node.get("processor_response")));
        response.setCctResponse(cleanJson(node.get("cct_response")));

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

    // ================= CLEAN JSON FIX =================
    private String cleanJson(JsonNode node) {

        if (node == null || node.isNull()) {
            return null;
        }

        try {
            String text = node.toString();

            // normalize escaped quotes if any
            text = text.replace("\\\"", "\"");

            // if JSON object/array, keep it structured
            if ((text.trim().startsWith("{") && text.trim().endsWith("}")) ||
                (text.trim().startsWith("[") && text.trim().endsWith("]"))) {
                return mapper.readTree(text).toString();
            }

            return text;

        } catch (Exception e) {
            return node.toString();
        }
    }

    // ================= TXN ID GENERATOR =================
    private String generateTxnIdFromUserInput(String cctRequestJson) throws Exception {

        Map<String, Object> cctReqObj =
                mapper.readValue(cctRequestJson, LinkedHashMap.class);

        return "FD_" +
                getValue(cctReqObj, "4.1") + "_" +
                getValue(cctReqObj, "4.3") + "_" +
                getValue(cctReqObj, "3.21") + "_" +
                getValue(cctReqObj, "4.15") + "_" +
                getValue(cctReqObj, "4.20") + "_" +
                getValue(cctReqObj, "4.21") + "_" +
                getValue(cctReqObj, "4.40");
    }

    // ================= SAFE GET =================
    private String getValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value == null ? "NA" : String.valueOf(value);
    }
}