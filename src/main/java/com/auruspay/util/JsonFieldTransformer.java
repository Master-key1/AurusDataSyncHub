package com.auruspay.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class JsonFieldTransformer {

    private final ObjectMapper mapper = new ObjectMapper();
    private JsonNode configRoot;

    @PostConstruct
    public void init() {
        try (InputStream is =
                     new ClassPathResource("static/AurusIsoField.json").getInputStream()) {

            configRoot = mapper.readTree(is);
            System.out.println("✅ Mapping JSON loaded");

        } catch (Exception e) {
            throw new RuntimeException("❌ Failed to load mapping JSON", e);
        }
    }

    public Map<String, String> transform(String requestJson) {

        Map<String, String> result = new LinkedHashMap<>();

        try {
            JsonNode requestNode = mapper.readTree(requestJson);

            Iterator<Map.Entry<String, JsonNode>> fields = requestNode.fields();

            while (fields.hasNext()) {

                Map.Entry<String, JsonNode> entry = fields.next();

                String reqKey = entry.getKey();
                String value = entry.getValue().asText();

                // default fallback
                String mappedKey = reqKey;

                // if mapping exists in config JSON
                if (configRoot != null && configRoot.has(reqKey)) {
                    mappedKey = configRoot.get(reqKey).asText();
                }

                // FINAL FORMAT:
                String formattedValue = mappedKey + " : " + reqKey + " : " + value;

                result.put(mappedKey, formattedValue);
            }

        } catch (Exception e) {
            throw new RuntimeException("Transform failed", e);
        }

        return result;
    }

    public static void main(String[] args) {

        try {
            JsonFieldTransformer transformer = new JsonFieldTransformer();
            transformer.init();

            String requestJson =
                    "\r\n"
                    + "\r\n"
                    + "{\"6.9\":\"12345\",\"4.11\":\"150.00\",\"4.13\":\"134001\",\"4.15\":\"1\",\"14.13\":\"1\",\"3.43\":\"6\",\"4.20\":\"840\",\"4.21\":\"840\",\"4.26\":\"1\",\"4.113\":\"1\",\"12.1\":\"0000002\",\"1.1\":\"100000139336\",\"1.2\":\"79794\",\"1.3\":\"59806551\",\"3.1\":\"17\",\"3.2\":\"AESDK\",\"3.3\":\"Reg01\",\"5.1\":\"0000002\",\"3.4\":\"00\",\"4.18\":\"05142026\",\"3.5\":\"26.03.065.001\",\"4.19\":\"202022\",\"5.3\":\"0000002\",\"7.1\":\"0.00\",\"3.6\":\"1.8\",\"7.2\":\"0.00\",\"3.7\":\"5.3.18-57-default\",\"7.3\":\"0.00\",\"3.8\":\"1\",\"7.5\":\"%1F%1F%1D\",\"5.8\":\"5.0874\",\"5.9\":\"01\",\"4.32\":\"3\",\"7.9\":\"%1F%1F%1D\",\"12.55\":\"10001\",\"4.79\":\"1\",\"4.38\":\"26.03.065.001\",\"3.21\":\"1.91\",\"4.42\":\"P2PE NOT SUPPORTED\",\"5.13\":\"01\",\"5.14\":\"01\",\"4.40\":\"00\",\"2.1\":\"C0N43N4U45RLN8R1\",\"2.2\":\"60:33:4b:10:42:98\",\"2.3\":\"192.168.255.243\",\"4.1\":\"3\",\"4.2\":\"000002\",\"4.3\":\"4\",\"4.4\":\"4761730111160043%5EUAT+USA%2FTEST+CARD+02++++++%5E3112201114380440000000000000000%7E4761730111160043%3D31122011303130600000\",\"4.5\":\"150.00\",\"4.6\":\"0.00\",\"4.7\":\"0.00\"}";

            Map<String, String> result = transformer.transform(requestJson);

            System.out.println("===== OUTPUT =====");

            result.forEach((k, v) -> System.out.println(v));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}