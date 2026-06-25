
package com.auruspay.comparator;

import com.auruspay.comparator.model.ValidationResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class XmlComparator {

    private static final String TAG_MISSING = "TAG MISSING";

    @Autowired
    private FieldValidator fieldValidator;

    public List<Map<String, String>> getXmlComparator(
            String approvedXml,
            String declinedXml) {

        Map<String, String> approvedMap = extractAll(approvedXml);
        Map<String, String> declinedMap = extractAll(declinedXml);

        return smartCompare(approvedMap, declinedMap);
    }

    private Map<String, String> extractAll(String xml) {

        Map<String, String> values = new LinkedHashMap<>();

        if (xml == null || xml.trim().isEmpty()) {
            return values;
        }

        try {

            String fixedXml = xml.replaceAll(
                    "([a-zA-Z0-9]+)=([^\"'\\s>]+)",
                    "$1=\"$2\"");

            Document document =
                    DocumentBuilderFactory
                            .newInstance()
                            .newDocumentBuilder()
                            .parse(
                                    new ByteArrayInputStream(
                                            fixedXml.getBytes(
                                                    StandardCharsets.UTF_8)));

            document.getDocumentElement().normalize();

            traverse(
                    document.getDocumentElement(),
                    values);

        } catch (Exception e) {

            values.put(
                    "PARSE_ERROR",
                    e.getMessage());
        }

        return values;
    }

    private void traverse(
            Node node,
            Map<String, String> values) {

        NodeList children =
                node.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {

            Node child =
                    children.item(i);

            if (child.getNodeType()
                    == Node.ELEMENT_NODE) {

                if (!hasElementChildren(child)) {

                    values.put(
                            child.getNodeName(),
                            child.getTextContent()
                                    .trim());

                } else {

                    traverse(
                            child,
                            values);
                }
            }
        }
    }

    private boolean hasElementChildren(
            Node node) {

        NodeList children =
                node.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {

            if (children.item(i).getNodeType()
                    == Node.ELEMENT_NODE) {

                return true;
            }
        }

        return false;
    }

    public List<Map<String, String>> smartCompare(
            Map<String, String> approved,
            Map<String, String> declined) {

        List<Map<String, String>> result =
                new ArrayList<>();

        Set<String> fields =
                new TreeSet<>();

        fields.addAll(approved.keySet());
        fields.addAll(declined.keySet());

        for (String field : fields) {

            String approvedValue =
                    approved.getOrDefault(
                            field,
                            TAG_MISSING);

            String declinedValue =
                    declined.getOrDefault(
                            field,
                            TAG_MISSING);

            ValidationResult approvedValidation =
                    fieldValidator.validate(
                            field,
                            approvedValue);

            ValidationResult declinedValidation =
                    fieldValidator.validate(
                            field,
                            declinedValue);

            boolean valueMatched =
                    Objects.equals(
                            approvedValue,
                            declinedValue);

            String pattern;

            if ("VALID".equals(approvedValidation.status())
                    && "VALID".equals(declinedValidation.status())) {

                pattern = "MATCHED";

            } else {

                pattern = "MISMATCH";
            }

            String reason;

            if (TAG_MISSING.equals(approvedValue)
                    || TAG_MISSING.equals(declinedValue)) {

                reason =
                        "Field missing in one transaction";

            } else if (valueMatched) {

                reason =
                        "Expected Value";

            } else if (!"No validation rule configured"
                    .equals(approvedValidation.reason())) {

                reason =
                        approvedValidation.reason();

            } else if (!"No validation rule configured"
                    .equals(declinedValidation.reason())) {

                reason =
                        declinedValidation.reason();

            } else if (approvedValue.length()
                    != declinedValue.length()) {

                reason =
                        "Length mismatch ("
                                + approvedValue.length()
                                + " vs "
                                + declinedValue.length()
                                + ")";

            } else {

                reason =
                        "Value mismatch";
            }

            Map<String, String> row =
                    new LinkedHashMap<>();

            row.put(
                    "field",
                    field);

            row.put(
                    "approved",
                    approvedValue);

            row.put(
                    "declined",
                    declinedValue);

            row.put(
                    "pattern",
                    pattern);

            row.put(
                    "reason",
                    reason);
           
            row.put("field", field);
            row.put("approved", approvedValue);
            row.put("declined", declinedValue);
            row.put("pattern", pattern);
            row.put("reason", reason);

            System.out.println("Returned Row : " + row);

            result.add(row);

            result.add(row);
        }

        return result;
    }
}