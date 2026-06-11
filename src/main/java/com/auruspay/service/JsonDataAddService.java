package com.auruspay.service;

import com.auruspay.dto.ProcessRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class JsonDataAddService {

    private final ObjectMapper mapper = new ObjectMapper();

    // ⚠️ Better than resources/static (production safe for dev use)
    private static final String FILE_PATH = "static/data.json";

    // ================= SAVE DATA =================
    public String saveData(ProcessRequest request) throws Exception {

        String txnId = generateTxnId(request.getCctRequest());

        File file = new File(FILE_PATH);

        Map<String, Object> finalJson = loadExisting(file);

        if (finalJson.containsKey(txnId)) {
            return "❌ Already exists: " + txnId;
        }

        Map<String, Object> inner = new LinkedHashMap<>();

        inner.put("cct_request", clean(request.getCctRequest()));
        inner.put("processor_request", clean(request.getProcessorRequest()));
        inner.put("processor_response", clean(request.getProcessorResponse()));
        inner.put("cct_response", clean(request.getCctResponse()));

        finalJson.put(txnId, inner);

        mapper.writerWithDefaultPrettyPrinter()
                .writeValue(file, finalJson);

        return "✅ Added successfully: " + txnId;
    }

    
    private String clean(String value) {
        if (value == null) {
            return null;
        }

        return value
                .replace("\\r\\n", "")   // literal \r\n from UI/string
                .replace("\r\n", "")     // real Windows newline
                .replace("\n", "")       // Linux newline
                .replace("\r", "")       // old Mac newline
                .trim();
    }
    // ================= LOAD OR CREATE FILE =================
    @SuppressWarnings("unchecked")
    private Map<String, Object> loadExisting(File file) {

        try {
            // ✅ CREATE FILE + FOLDER IF NOT EXISTS
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();

                mapper.writerWithDefaultPrettyPrinter()
                        .writeValue(file, new LinkedHashMap<>());

                return new LinkedHashMap<>();
            }

            // ✅ READ EXISTING DATA
            if (file.length() > 0) {
                return mapper.readValue(file, LinkedHashMap.class);
            }

        } catch (Exception e) {
            System.out.println("⚠️ Error handling file: " + e.getMessage());
        }

        return new LinkedHashMap<>();
    }

    // ================= TXN ID GENERATION =================
    private String generateTxnId(String cctRequest) {

        try {
            Map<String, Object> cctReqObj =
                    mapper.readValue(cctRequest, LinkedHashMap.class);

            return "FD_" +
                    getValue(cctReqObj, "4.1") + "_" +
                    getValue(cctReqObj, "4.3") + "_" +
                    getValue(cctReqObj, "3.21") + "_" +
                    getValue(cctReqObj, "4.15") + "_" +
                    getValue(cctReqObj, "4.20") + "_" +
                    getValue(cctReqObj, "4.21") + "_" +
                    getValue(cctReqObj, "4.40");

        } catch (Exception e) {
            return "FD_UNKNOWN_TXN";
        }
    }

    private String getValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value == null ? "NA" : String.valueOf(value);
    }
}