package com.auruspay.comparator;

import org.springframework.stereotype.Service;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class XmlComparator2 {

    public List<Map<String, String>> getXmlComparator(
            String approvedXml,
            String declinedXml) {

        Map<String, String> approvedMap = extractAllSafe(approvedXml);
        Map<String, String> declinedMap = extractAllSafe(declinedXml);

        return smartCompare(approvedMap, declinedMap);
    }

    // =========================
    // SAFE XML PARSER
    // =========================
    private Map<String, String> extractAllSafe(String xml) {

        Map<String, String> map = new LinkedHashMap<>();

        if (xml == null || xml.trim().isEmpty()) {
            return map;
        }

        try {
            String cleanedXml = cleanXml(xml);

            if (!cleanedXml.startsWith("<")) {
                return map;
            }

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(false);

            DocumentBuilder builder = factory.newDocumentBuilder();

            Document doc = builder.parse(
                    new ByteArrayInputStream(cleanedXml.getBytes(StandardCharsets.UTF_8))
            );

            doc.getDocumentElement().normalize();

            traverse(doc.getDocumentElement(), map);

        } catch (Exception e) {
            System.err.println("XML parse failed: " + e.getMessage());
        }

        return map;
    }

    // =========================
    // CLEAN INPUT (FDk / FD REMOVAL)
    // =========================
    private String cleanXml(String input) {

        if (input == null) return "";

        String cleaned = input;

        // remove known wrappers
        cleaned = cleaned.replace("FDk", "")
                         .replace("FD", "");

        // remove BOM / invisible chars
        cleaned = cleaned.replace("\uFEFF", "")
                         .replace("\u00A0", "");

        // remove new lines
        cleaned = cleaned.replace("\r", "")
                         .replace("\n", "")
                         .trim();

        // remove anything before first XML tag
        cleaned = cleaned.replaceAll("^[^<]*", "");

        return cleaned;
    }

    // =========================
    // XML TRAVERSAL
    // =========================
    private void traverse(Node node, Map<String, String> map) {

        NodeList children = node.getChildNodes();
        boolean hasChildElement = false;

        for (int i = 0; i < children.getLength(); i++) {

            Node child = children.item(i);

            if (child.getNodeType() == Node.ELEMENT_NODE) {
                hasChildElement = true;
                traverse(child, map);
            }
        }

        if (!hasChildElement && node.getNodeType() == Node.ELEMENT_NODE) {

            String tag = node.getNodeName();
            String value = node.getTextContent().trim();

            if (!value.isEmpty()) {
                map.put(tag, value);
            }
        }
    }

    // =========================
    // COMPARISON ENGINE
    // =========================
    public List<Map<String, String>> smartCompare(
            Map<String, String> approved,
            Map<String, String> declined) {

        List<Map<String, String>> result = new ArrayList<>();

        Set<String> allFields = new TreeSet<>();
        allFields.addAll(approved.keySet());
        allFields.addAll(declined.keySet());

        for (String field : allFields) {

            String a = approved.get(field);
            String d = declined.get(field);

            if (Objects.equals(a, d)) {
                continue;
            }

            Map<String, String> row = new LinkedHashMap<>();
            row.put("field", field);

            row.put("approvedValue", a == null ? "MISSING" : a);
            row.put("declinedValue", d == null ? "MISSING" : d);

            if (a == null) {
                row.put("status", "ONLY_DECLINED");
                row.put("reason", "Field missing in approved request");

            } else if (d == null) {
                row.put("status", "ONLY_APPROVED");
                row.put("reason", "Field missing in declined request");

            } else {
                row.put("status", "DIFF");
                row.put("reason", "Value mismatch");
            }

            result.add(row);
        }

        return result;
    }
}