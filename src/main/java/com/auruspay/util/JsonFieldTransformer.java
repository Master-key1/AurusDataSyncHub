package com.auruspay.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class JsonFieldTransformer {

    private static final Logger log =
            LoggerFactory.getLogger(JsonFieldTransformer.class);

    private final ObjectMapper mapper = new ObjectMapper();
    private JsonNode configRoot;

    @PostConstruct
    public void init() {

        try (InputStream is =
                     new ClassPathResource("static/AurusIsoField.json").getInputStream()) {

            configRoot = mapper.readTree(is);
            log.info("Mapping JSON loaded successfully");

        } catch (Exception e) {
            throw new RuntimeException("Failed to load mapping JSON", e);
        }
    }

    // ================= TRANSFORM =================
    public Map<String, String> transform(String requestJson) {

        Map<String, String> result = new LinkedHashMap<>();

        try {
            JsonNode requestNode = mapper.readTree(requestJson);

            Iterator<Map.Entry<String, JsonNode>> fields = requestNode.fields();

            while (fields.hasNext()) {

                Map.Entry<String, JsonNode> entry = fields.next();

                String reqKey = entry.getKey();
                String value = entry.getValue() != null ? entry.getValue().asText() : "NA";

                String mappedKey = resolveMapping(reqKey);

                // FINAL FORMAT
                String formatted = mappedKey + " : " + reqKey + " : " + value;

                result.put(reqKey, formatted);
            }

        } catch (Exception e) {
            log.error("Transform failed", e);
            throw new RuntimeException("Transform failed", e);
        }

        return result;
    }

    // ================= SAFE MAPPING =================
    private String resolveMapping(String reqKey) {

        if (configRoot != null && configRoot.has(reqKey)) {
            return configRoot.get(reqKey).asText();
        }

        return reqKey; // fallback
    }

    // ================= MAIN TEST =================
    public static void main() {

        try {
            JsonFieldTransformer transformer = new JsonFieldTransformer();
            transformer.init();

            String requestJson =
                    "{\"6.9\":\"12345\",\"4.11\":\"150.00\",\"4.13\":\"134001\",\"4.15\":\"1\"}";

            Map<String, String> result = transformer.transform(requestJson);

            System.out.println("===== OUTPUT =====");

            result.forEach((k, v) -> System.out.println(v));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}