package com.auruspay.comparator.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public final class EMVParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(EMVParser.class);

    private static final Map<String, String> TAG_MAP = new HashMap<>();

    public EMVParser() {
    }

    static {
        try {
            loadTagsFromXml();
        } catch (Exception e) {
            LOGGER.error("Failed to load EMV tag definitions", e);
        }
    }

    private static void loadTagsFromXml() throws Exception {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setExpandEntityReferences(false);

        try (InputStream is = EMVParser.class.getClassLoader()
                .getResourceAsStream("emv-tags.xml")) {

            if (is == null) {
                throw new RuntimeException(
                        "emv-tags.xml not found in src/main/resources");
            }

            Document doc = factory.newDocumentBuilder().parse(is);

            NodeList nodeList = doc.getElementsByTagName("tag");

            for (int i = 0; i < nodeList.getLength(); i++) {

                Element element = (Element) nodeList.item(i);

                String id = element.getAttribute("id").trim().toUpperCase();
                String name = element.getAttribute("name").trim();

                TAG_MAP.put(id, name);
            }

            LOGGER.info("Loaded {} EMV tags", TAG_MAP.size());
        }
    }

    /**
     * Returns Tag -> Value
     */
    public  Map<String, String> parseToMap(String tlvData) {

        if (tlvData == null || tlvData.trim().isEmpty()) {
            return Collections.emptyMap();
        }

        tlvData = tlvData.replaceAll("\\s+", "").toUpperCase();

        Map<String, String> result = new LinkedHashMap<>();

        int index = 0;

        while (index < tlvData.length()) {

            try {

                String tag = readTag(tlvData, index);
                index += tag.length();

                int[] lengthInfo = readLength(tlvData, index);

                int valueLength = lengthInfo[0];
                int consumedLengthBytes = lengthInfo[1];

                index += consumedLengthBytes;

                int valueEndIndex = index + (valueLength * 2);

                if (valueEndIndex > tlvData.length()) {
                    throw new IllegalArgumentException(
                            "Invalid TLV data. Value length exceeds remaining data.");
                }

                String value = tlvData.substring(index, valueEndIndex);

                index = valueEndIndex;

                result.put(tag, value);

            } catch (Exception e) {

                LOGGER.error(
                        "Error parsing EMV data at index {}. Remaining data: {}",
                        index,
                        tlvData.substring(index),
                        e);

                break;
            }
        }

        return result;
    }

    /**
     * Returns Tag Name -> Value
     */
    public  Map<String, String> parseToNameMap(String tlvData) {

        Map<String, String> tagMap = parseToMap(tlvData);

        Map<String, String> result = new LinkedHashMap<>();

        tagMap.forEach((tag, value) ->
                result.put(TAG_MAP.getOrDefault(tag, tag), value));

        return result;
    }

    private  String readTag(String data, int index) {

        String firstByteHex = data.substring(index, index + 2);

        int firstByte = Integer.parseInt(firstByteHex, 16);

        StringBuilder tag = new StringBuilder(firstByteHex);

        index += 2;

        if ((firstByte & 0x1F) == 0x1F) {

            while (index < data.length()) {

                String nextByteHex = data.substring(index, index + 2);

                tag.append(nextByteHex);

                int nextByte = Integer.parseInt(nextByteHex, 16);

                index += 2;

                if ((nextByte & 0x80) == 0) {
                    break;
                }
            }
        }

        return tag.toString();
    }

    private  int[] readLength(String data, int index) {

        int firstLengthByte =
                Integer.parseInt(data.substring(index, index + 2), 16);

        if ((firstLengthByte & 0x80) == 0) {

            return new int[]{
                    firstLengthByte,
                    2
            };
        }

        int numberOfLengthBytes = firstLengthByte & 0x7F;

        StringBuilder lengthHex = new StringBuilder();

        int currentPos = index + 2;

        for (int i = 0; i < numberOfLengthBytes; i++) {

            lengthHex.append(data, currentPos, currentPos + 2);

            currentPos += 2;
        }

        int actualLength = Integer.parseInt(lengthHex.toString(), 16);

        return new int[]{
                actualLength,
                2 + (numberOfLengthBytes * 2)
        };
    }

    public  String getTagName(String tag) {
        return TAG_MAP.getOrDefault(tag, "Unknown");
    }

    public  void main(String[] args) {

        String emvData = "82020000950500000000009A032606249C01005F24033112315F2A0208405F3401019F02060000000005009F03060000000000009F090200969F1A0208409F1E0835323838343732329F2608F099469A9B1401EB9F2701809F3303E068C89F34034203009F3501229F360200729F37047990BA479F3901079F4104000002049F5301528407A00000009808409F100706011103A000009F6E04207000009F0607A00000009808405F300200004F07A0000000980840";
        EMVParser emvParser = new EMVParser();
        Map<String, String> emvTags = emvParser.parseToMap(emvData);

        emvTags.forEach((tag, value) ->
                System.out.println(
                        tag + " (" + getTagName(tag) + ") = " + value));
    }
}