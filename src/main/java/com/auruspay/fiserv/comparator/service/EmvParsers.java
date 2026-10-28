package com.auruspay.comparator.service;

import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Parses a BER-TLV encoded EMV data hex string (as sent in GMF's EMVData
 * field) into a tag -> value map. Handles both single- and multi-byte tags
 * (tags whose first byte has its low 5 bits set to 1 continue into
 * subsequent bytes) and both short- and long-form BER lengths.
 */
@Service
public class EmvParsers {

    public Map<String, String> parseToMap(String emvDataHex) {
        Map<String, String> result = new LinkedHashMap<>();
        if (emvDataHex == null || emvDataHex.isBlank()) {
            return result;
        }

        byte[] data;
        try {
            data = hexToBytes(emvDataHex.trim());
        } catch (IllegalArgumentException e) {
            return result;
        }

        int i = 0;
        int len = data.length;

        while (i < len) {
            int tagStart = i;
            int first = data[i] & 0xFF;
            i++;
            if ((first & 0x1F) == 0x1F) {
                // multi-byte tag: continue while high bit of each subsequent byte is set
                int next;
                do {
                    if (i >= len) break;
                    next = data[i] & 0xFF;
                    i++;
                } while ((next & 0x80) != 0);
            }
            if (i > len) break;
            String tag = bytesToHex(data, tagStart, i - tagStart);

            if (i >= len) break;
            int lenByte = data[i] & 0xFF;
            i++;
            int valueLength;
            if ((lenByte & 0x80) != 0) {
                int numLenBytes = lenByte & 0x7F;
                if (i + numLenBytes > len) break;
                valueLength = 0;
                for (int k = 0; k < numLenBytes; k++) {
                    valueLength = (valueLength << 8) | (data[i] & 0xFF);
                    i++;
                }
            } else {
                valueLength = lenByte;
            }

            if (i + valueLength > len) {
                // truncated/malformed data - stop parsing rather than throw
                break;
            }
            String value = bytesToHex(data, i, valueLength);
            i += valueLength;

            result.put(tag, value);
        }

        return result;
    }

    private byte[] hexToBytes(String hex) {
        if (hex.length() % 2 != 0) {
            throw new IllegalArgumentException("Odd-length hex string");
        }
        byte[] out = new byte[hex.length() / 2];
        for (int i = 0; i < out.length; i++) {
            out[i] = (byte) Integer.parseInt(hex.substring(i * 2, i * 2 + 2), 16);
        }
        return out;
    }

    private String bytesToHex(byte[] data, int offset, int length) {
        StringBuilder sb = new StringBuilder(length * 2);
        for (int i = offset; i < offset + length; i++) {
            sb.append(String.format("%02X", data[i]));
        }
        return sb.toString();
    }
}