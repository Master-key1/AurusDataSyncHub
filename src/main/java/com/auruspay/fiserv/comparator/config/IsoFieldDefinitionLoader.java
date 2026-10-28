package com.auruspay.comparator.config;

import com.auruspay.comparator.model.IsoFieldDefinition;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Component
public class IsoFieldDefinitionLoader {

    private static final Logger log = LoggerFactory.getLogger(IsoFieldDefinitionLoader.class);
    private static final String RESOURCE_PATH = "static/iso-fields-definition.xml";

    private final Map<String, IsoFieldDefinition> fieldMap = new HashMap<>();

    @PostConstruct
    public void loadDefinitions() {
        try (InputStream inputStream = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream(RESOURCE_PATH)) {

            if (inputStream == null) {
                throw new RuntimeException(RESOURCE_PATH + " not found on classpath. "
                        + "Check that the file actually lives at src/main/resources/" + RESOURCE_PATH
                        + " and that the build actually copied it (target/classes/" + RESOURCE_PATH + ").");
            }

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(inputStream);

            NodeList nodeList = document.getElementsByTagName("isofield");
            int skipped = 0;

            for (int i = 0; i < nodeList.getLength(); i++) {
                Element element = (Element) nodeList.item(i);
                try {
                    IsoFieldDefinition field = parseField(element);

                    if (field.getId() == null || field.getId().isEmpty()) {
                        log.warn("Skipping <isofield> at index {} — missing/blank 'id' attribute", i);
                        skipped++;
                        continue;
                    }

                    if (fieldMap.containsKey(field.getId())) {
                        log.warn("Duplicate isofield id='{}' at index {} — overwriting previous definition (was: {})",
                                field.getId(), i, fieldMap.get(field.getId()).getName());
                    }

                    fieldMap.put(field.getId(), field);
                } catch (Exception elementEx) {
                    // A single malformed <isofield> (e.g. missing/blank minlength or
                    // maxlength causing Integer.parseInt to throw) must NOT abort
                    // parsing of every subsequent element — that would silently wipe
                    // out fields that are otherwise perfectly valid, which is exactly
                    // what produces a "field X is in the XML but shows UNKNOWN FIELD"
                    // symptom with no clue why.
                    skipped++;
                    log.error("Failed to parse <isofield> at index {} (id='{}') — skipping. Cause: {}",
                            i, element.getAttribute("id"), elementEx.getMessage());
                }
            }

            log.info("ISO Field Definitions Loaded: {} (skipped {} malformed entries)",
                    fieldMap.size(), skipped);

            if (fieldMap.isEmpty()) {
                log.error("ISO field map is EMPTY after loading — every field lookup will return null "
                        + "and every ValidateResult will show fieldName='UNKNOWN FIELD'. Check the log lines above "
                        + "for parse failures, and confirm the resource on the classpath is the file you expect.");
            }

            if (log.isDebugEnabled()) {
                log.debug("Loaded field ids: {}", fieldMap.keySet());
            }

        } catch (Exception e) {
            throw new RuntimeException("Error loading " + RESOURCE_PATH, e);
        }
    }

    private IsoFieldDefinition parseField(Element element) {
        IsoFieldDefinition field = new IsoFieldDefinition();

        field.setId(trimOrNull(element.getAttribute("id")));
        field.setName(trimOrNull(element.getAttribute("name")));
        field.setClassType(trimOrNull(element.getAttribute("classType")));
        field.setFailedMsg(trimOrNull(element.getAttribute("failedMsg")));

        field.setMinLength(parseIntOrZero(element.getAttribute("minlength"), field.getId(), "minlength"));
        field.setMaxLength(parseIntOrZero(element.getAttribute("maxlength"), field.getId(), "maxlength"));

        // Optional attribute used by ENUM classType fields, e.g. value="0,1,2".
        // getAttribute() returns "" (not null) when the attribute is absent,
        // which is fine — absence just means "no enum list to check".
        field.setValue(trimOrNull(element.getAttribute("value")));

        return field;
    }

    /**
     * Parses a length attribute defensively: blank/missing -> 0 (instead of
     * throwing and aborting the whole element), with a warning so the gap is
     * visible instead of silent.
     */
    private int parseIntOrZero(String rawValue, String fieldId, String attributeName) {
        if (rawValue == null || rawValue.trim().isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(rawValue.trim());
        } catch (NumberFormatException e) {
            log.warn("Field id='{}' has non-numeric {}='{}' — defaulting to 0", fieldId, attributeName, rawValue);
            return 0;
        }
    }

    private String trimOrNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public IsoFieldDefinition getField(String fieldId) {
        if (fieldId == null) {
            return null;
        }
        IsoFieldDefinition definition = fieldMap.get(fieldId.trim());
        if (definition == null && log.isDebugEnabled()) {
            log.debug("No ISO field definition found for id='{}'", fieldId);
        }
        return definition;
    }
}