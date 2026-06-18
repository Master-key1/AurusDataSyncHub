package com.auruspay.comparator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.stereotype.Service;

@Service
public class CctComparator2 {

    private static final Logger LOGGER =
            Logger.getLogger(CctComparator.class.getName());

    private static final ObjectMapper mapper = new ObjectMapper();

    public List<Map<String, String>> compare(String declinedJson, String approvedJson) throws Exception {

        LOGGER.info("Starting CCT comparison");

        JsonNode declined = mapper.readTree(declinedJson);
        JsonNode approved = mapper.readTree(approvedJson);

        Set<String> fields = new TreeSet<>();
        declined.fieldNames().forEachRemaining(fields::add);
        approved.fieldNames().forEachRemaining(fields::add);

        List<Map<String, String>> issues = new ArrayList<>();

        for (String field : fields) {

            String dVal = declined.has(field)
                    ? declined.get(field).asText()
                    : "MISSING";

            String aVal = approved.has(field)
                    ? approved.get(field).asText()
                    : "MISSING";

            if (!Objects.equals(dVal, aVal)) {

                Map<String, String> issue = new LinkedHashMap<>();

                issue.put("field", field);
                issue.put("declinedValue", dVal);
                issue.put("approvedValue", aVal);
                issue.put("reason", getReason(dVal, aVal));

                issues.add(issue);

                LOGGER.info(() ->
                        "Difference Found -> Field: " + field +
                        ", Declined: " + dVal +
                        ", Approved: " + aVal +
                        ", Reason: " + issue.get("reason"));
            }
        }

        LOGGER.info("Comparison completed. Total differences: " + issues.size());

        return issues;
    }

    private static String getReason(String dVal, String aVal) {

        if ("MISSING".equals(dVal) || "MISSING".equals(aVal)) {
            return "Field missing in one transaction";
        }

        if (dVal.length() != aVal.length()) {
            return "Length mismatch (" + dVal.length() + " vs " + aVal.length() + ")";
        }

        if (!getType(dVal).equals(getType(aVal))) {
            return "Data type mismatch (" + getType(dVal) + " vs " + getType(aVal) + ")";
        }

        return "Expected Value";
    }

    private static String getType(String value) {

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