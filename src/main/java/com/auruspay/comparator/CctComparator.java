package com.auruspay.comparator;

import com.auruspay.comparator.config.IsoFieldDefinitionLoader;
import com.auruspay.comparator.model.IsoFieldDefinition;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CctComparator {

    private static final Logger LOGGER =
            Logger.getLogger(CctComparator.class.getName());

    private static final ObjectMapper mapper =
            new ObjectMapper();

    @Autowired
    private IsoFieldDefinitionLoader definitionLoader;

    public List<Map<String, String>> compare(
            String declinedJson,
            String approvedJson) throws Exception {
    	
    	
         	declinedJson=	declinedJson.replaceAll(",\\s*}", "}");

        LOGGER.info("Starting CCT comparison");

        JsonNode declined =
                mapper.readTree(declinedJson);

        System.out.println("approvedJson : "+approvedJson);
        JsonNode approved =
                mapper.readTree(approvedJson);

        Set<String> fields = new TreeSet<>();

        declined.fieldNames()
                .forEachRemaining(fields::add);

        approved.fieldNames()
                .forEachRemaining(fields::add);

        List<Map<String, String>> issues =
                new ArrayList<>();

        for (String field : fields) {

            String dVal =
                    declined.has(field)
                            ? declined.get(field).asText()
                            : "MISSING";

            String aVal =
                    approved.has(field)
                            ? approved.get(field).asText()
                            : "MISSING";

            IsoFieldDefinition definition =
                    definitionLoader.getField(field);

            if (!Objects.equals(dVal, aVal)) {

                Map<String, String> issue =
                        new LinkedHashMap<>();

                issue.put("field", field);

                issue.put(
                        "fieldName",
                        definition != null
                                ? definition.getName()
                                : "UNKNOWN FIELD");

                issue.put(
                        "declinedValue",
                        dVal);

                issue.put(
                        "approvedValue",
                        aVal);

                issue.put(
                        "reason",
                        getReason(dVal, aVal));

                if (definition != null) {

                    issue.put(
                            "expectedType",
                            definition.getClassType());

                    issue.put(
                            "minLength",
                            String.valueOf(
                                    definition.getMinLength()));

                    issue.put(
                            "maxLength",
                            String.valueOf(
                                    definition.getMaxLength()));

                    issue.put(
                            "declinedValidation",
                            validateValue(
                                    dVal,
                                    definition));

                    issue.put(
                            "approvedValidation",
                            validateValue(
                                    aVal,
                                    definition));
                }

                issues.add(issue);

                LOGGER.info(
                        "Difference Found -> "
                                + issue);
            }
        }

        LOGGER.info(
                "Comparison completed. Total differences : "
                        + issues.size());

        return issues;
    }

    private String validateValue(
            String value,
            IsoFieldDefinition definition) {

        if ("MISSING".equals(value)) {
            return "FIELD MISSING";
        }

        int length = value.length();

        if (length < definition.getMinLength()) {
            return "MIN LENGTH FAILED";
        }

        if (definition.getMaxLength() > 0 &&
                length > definition.getMaxLength()) {

            return "MAX LENGTH FAILED";
        }

        String type =
                definition.getClassType();

        switch (type) {

            case "NUMERIC":

                if (!value.matches("\\d+")) {
                    return "INVALID NUMERIC";
                }

                break;

            case "ALPHA":

                if (!value.matches("[A-Za-z ]+")) {
                    return "INVALID ALPHA";
                }

                break;

            case "ALPHA_NUMERIC":

                if (!value.matches("[A-Za-z0-9._\\- ]+")) {
                    return "INVALID ALPHANUMERIC";
                }

                break;

            case "IP":

                if (!value.matches(
                        "^((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)\\.){3}" +
                        "(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)$")) {

                    return "INVALID IP";
                }

                break;
        }

        return "VALID";
    }

    private String getReason(
            String dVal,
            String aVal) {

        if ("MISSING".equals(dVal)
                || "MISSING".equals(aVal)) {

            return "Field missing in one transaction";
        }

        if (dVal.length()
                != aVal.length()) {

            return "Length mismatch ("
                    + dVal.length()
                    + " vs "
                    + aVal.length()
                    + ")";
        }

        return "Value mismatch";
    }
}