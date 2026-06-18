package com.auruspay.comparator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.*;

@Component
public class JsonFieldTransformerComparator {

    private static final Logger log =
            LoggerFactory.getLogger(JsonFieldTransformerComparator.class);

    private final ObjectMapper mapper = new ObjectMapper();

    private JsonNode configRoot;

    @PostConstruct
    public void init() {

        try (InputStream is =
                     new ClassPathResource("static/AurusIsoField.json").getInputStream()) {

            configRoot = mapper.readTree(is);

            log.info("AurusIsoField.json loaded successfully");

        } catch (Exception e) {

            log.error("Failed to load AurusIsoField.json", e);

            throw new RuntimeException("Failed to load mapping file", e);
        }
    }

    public List<Map<String, String>> compareAndTransform(
            String declinedJson,
            String approvedJson) {

        List<Map<String, String>> result = new ArrayList<>();

        try {

            JsonNode declined = mapper.readTree(declinedJson);
            JsonNode approved = mapper.readTree(approvedJson);

            Set<String> allFields = new TreeSet<>();

            declined.fieldNames().forEachRemaining(allFields::add);
            approved.fieldNames().forEachRemaining(allFields::add);

            for (String field : allFields) {

                String dVal =
                        declined.has(field)
                                ? declined.get(field).asText()
                                : "MISSING";

                String aVal =
                        approved.has(field)
                                ? approved.get(field).asText()
                                : "MISSING";

                if (!Objects.equals(dVal, aVal)) {

                    String mappedName =
                            configRoot != null && configRoot.has(field)
                                    ? configRoot.get(field).asText()
                                    : field;

                    Map<String, String> row = new LinkedHashMap<>();

                    row.put("field", field);
                    row.put("fieldName", mappedName);
                    row.put("declinedValue", dVal);
                    row.put("approvedValue", aVal);
                    row.put("reason", getReason(dVal, aVal));

                    result.add(row);
                }
            }

        } catch (Exception e) {

            log.error("Comparison failed", e);

            throw new RuntimeException("Comparison failed", e);
        }

        return result;
    }

    private String getReason(String dVal, String aVal) {

        if ("MISSING".equals(dVal) || "MISSING".equals(aVal)) {
            return "Field Missing";
        }

        if (dVal.length() != aVal.length()) {
            return "Length Mismatch";
        }

        if (!getType(dVal).equals(getType(aVal))) {
            return "Data Type Mismatch";
        }

        return "Value Mismatch";
    }

    private String getType(String value) {

        if (value == null || value.equals("MISSING")) {
            return "UNKNOWN";
        }

        if (value.matches("^[0-9]+$")) {
            return "NUMERIC";
        }

        if (value.matches("^[a-zA-Z]+$")) {
            return "ALPHA";
        }

        if (value.matches("^[a-zA-Z0-9]+$")) {
            return "ALPHANUMERIC";
        }

        return "SPECIAL";
    }
}