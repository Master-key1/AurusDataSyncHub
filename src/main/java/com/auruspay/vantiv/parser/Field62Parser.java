package com.auruspay.vantiv.parser;


import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.auruspay.vantiv.model.Field62Definition;

/**
 * Parser for Worldpay DE62 (Vantiv Transaction Request)
 */
@Component
public class Field62Parser {

    public static Map<String, String> parse(String hex) {

        Map<String, String> fields = new LinkedHashMap<>();

        if (hex == null || hex.isBlank()) {
            return fields;
        }

        Cursor c = new Cursor(hex);

        try {

            /*************************************************
             * 62.0 Primary Bitmap
             *************************************************/
        	String primaryBitmap = c.take(16);

        	fields.put("62.0 Primary Bitmap", primaryBitmap);
        	fields.put("62.0 Primary Bitmap(Binary)", hexToBinary(primaryBitmap));

        	boolean[] primaryBits = bitmapToBits(primaryBitmap);

        	boolean[] secondaryBits = new boolean[64];

        	if (primaryBits[0]) {

        	    String secondaryBitmap = c.take(16);

        	    fields.put("62.1 Secondary Bitmap", secondaryBitmap);
        	    fields.put("62.1 Secondary Bitmap(Binary)", hexToBinary(secondaryBitmap));

        	    secondaryBits = bitmapToBits(secondaryBitmap);
        	}

            /*************************************************
             * 62.2 Terminal Sequence Number
             * 6 Hex = 3 Bytes
             *************************************************/
        	if (primaryBits[1]) {
        	    fields.put(
        	            "62.2 Terminal Sequence Number",
        	            c.take(6));
        	}

            /*************************************************
             * 62.38 Sales Tax Amount
             * 10 Hex
             *************************************************/
        	if (primaryBits[37]) {
        	    fields.put(
        	            "62.38 Sales Tax Amount",
        	            hexToAscii(c.take(20)));
        	}

            /*************************************************
             * 62.44 DCC 2-Pass Flag
             *************************************************/
        	if (primaryBits[43]) {
        	    fields.put(
        	            "62.44 DCC 2-Pass Flag",
        	            hexToAscii(c.take(2)));
        	}

            /*************************************************
             * 62.46 Draft Locator ID
             * 11 Bytes
             *************************************************/
        	if (primaryBits[45]) {
        	    fields.put(
        	            "62.46 Draft Locator ID",
        	            hexToAscii(c.take(22)));
        	}

            /*************************************************
             * 62.50 Sales Tax Indicator
             *************************************************/
        	if (primaryBits[49]) {
        	    fields.put(
        	            "62.50 Sales Tax Indicator",
        	            hexToAscii(c.take(2)));
        	}
        	
        	if (primaryBits[62]) {
        	    fields.put(
        	            "62.63 Tip Amount",
        	            c.take(12));
        	}
            /*************************************************
             * 62.68 POS Environment Indicator
             *************************************************/
        	if (secondaryBits[3]) {
            fields.put(
                    "62.68 POS Environment Indicator",
                    hexToAscii(c.take(2)));
        	}
            /*************************************************
             * 62.69 Customer Bill-To Address
             *
             * Fixed Length : 122 Bytes
             *************************************************/
        	if (secondaryBits[4]) {
            fields.put(
                    "62.69 Customer Bill-To Address",
                    hexToAscii(c.take(224)));
        	}
            /*************************************************
             * 62.70 Customer Shipping Data
             *
             * Fixed Length : 114 Bytes
             *************************************************/
        	if (secondaryBits[5]) {
        	fields.put(
                    "62.70 Customer Shipping Data",
                    hexToAscii(c.take(224)));
        	}
            /*************************************************
             * 62.72 Customer Order ID
             *
             * Fixed Length : 13 Bytes
             *************************************************/
        	if (secondaryBits[7]) {
        		int len = Integer.parseInt(c.take(2),16);
        		String orderId =
        				hexToAscii(c.take(len * 2));

        		fields.put("62.72 Customer Order ID", orderId);}

        	if (secondaryBits[8]) {

        		int len = Integer.parseInt(c.take(2),16);

        		String email =
        		        hexToAscii(c.take(len * 2));

        		fields.put("62.73 Customer Email", email);
        	}

        	if (secondaryBits[9]) {
        		String phone =
        		        hexToAscii(c.take(20));

        		fields.put("62.74 Customer Phone Number", phone);
        	}
            /*************************************************
             * 62.75 Customer IP Address
             *
             * Fixed Length : 15 Bytes
             *************************************************/
        	if (secondaryBits[10]) {
        	fields.put(
                    "62.75 Customer IP Address",
                    hexToAscii(c.take(30)));
        	}
            /*************************************************
             * 62.76 Customer Web Session ID
             *
             * Current captures show 15 bytes.
             *************************************************/
        	if (secondaryBits[11]) {
        	fields.put(
                    "62.76 Customer Web Session ID",
                    hexToAscii(c.take(30)));
        	}
            /*************************************************
             * 62.88 User Account Data
             *
             * Current captures show 150 bytes.
             *************************************************/
        	if (secondaryBits[23]) {
        	fields.put(
                    "62.88 User Account Data",
                    hexToAscii(c.take(298)));
        	}
            /*************************************************
             * 62.89 Purchase Information
             *
             * Current captures show 200 Bytes
             *************************************************/
        	if (secondaryBits[24]) {
        	fields.put(
                    "62.89 Purchase Information",
                    hexToAscii(c.take(500)));
        	}
            /*************************************************
             * 62.90 Line Item
             *
             * Variable Length
             *
             * First 2 bytes = binary length
             * Example:
             * 0190 = 400 bytes
             *************************************************/
        	if (secondaryBits[25]) {
            int lineItemLength =
                    Integer.parseInt(c.take(4), 16);

            String lineItem =
                    c.take(lineItemLength * 2);

            fields.put(
                    "62.90 Line Item",
                    hexToAscii(lineItem));
        	}
            /*************************************************
             * 62.91 Payments General Data
             *
             * Remaining Data
             *************************************************/

        	if (secondaryBits[26]) {

        	    fields.put(
        	        "62.91 Payments General Data",
        	        hexToAscii(c.take(40)));
        	}

        	if (secondaryBits[27]) {

        	    fields.put(
        	        "62.92 Membership Data",
        	        hexToAscii(c.remainingHex()));
        	}
        }
        catch (Exception ex) {

            fields.put("ERROR",
                    "Unable to parse DE62 : "
                            + ex.getMessage());
        }

        return fields;
    }

    /*************************************************
     * Convert Hex -> ASCII
     *************************************************/
    private static String hexToAscii(String hex) {

        byte[] bytes = new byte[hex.length() / 2];

        for (int i = 0; i < bytes.length; i++) {

            bytes[i] = (byte) Integer.parseInt(
                    hex.substring(i * 2, i * 2 + 2),
                    16);
        }

        return new String(bytes, StandardCharsets.US_ASCII)
                .trim();
    }

    /*************************************************
     * Hex -> Binary
     *************************************************/
    private static boolean[] bitmapToBits(String bitmapHex) {

        boolean[] bits = new boolean[64];

        for (int i = 0; i < 16; i++) {

            int value = Integer.parseInt(
                    String.valueOf(bitmapHex.charAt(i)), 16);

            for (int j = 0; j < 4; j++) {

                bits[i * 4 + j] = (value & (1 << (3 - j))) != 0;
            }
        }

        return bits;
    }
    
    private static String hexToBinary(String hex) {

        StringBuilder sb = new StringBuilder();

        for (char ch : hex.toCharArray()) {

            sb.append(
                    String.format("%4s",
                            Integer.toBinaryString(
                                    Integer.parseInt(
                                            String.valueOf(ch), 16)))
                            .replace(' ', '0'));
        }

        return sb.toString();
    }

    /*************************************************
     * Sequential Reader
     *************************************************/
    
    private static void parseField(
            int fieldNumber,
            Cursor c,
            Map<String,String> fields) {

        Field62Definition definition =
                Field62Dictionary.get(fieldNumber);

        if (definition == null) {
            return;
        }

        switch (definition.format()) {

            case FIXED_HEX ->

                fields.put(
                        "62." + fieldNumber + " " + definition.name(),
                        c.take(definition.length()));

            case FIXED_ASCII ->

                fields.put(
                        "62." + fieldNumber + " " + definition.name(),
                        hexToAscii(c.take(definition.length())));

            case LLVAR_ASCII -> {

                int length =
                        Integer.parseInt(c.take(4),16);

                fields.put(
                        "62." + fieldNumber + " " + definition.name(),
                        hexToAscii(c.take(length * 2)));
            }

            case REMAINING ->

                fields.put(
                        "62." + fieldNumber + " " + definition.name(),
                        hexToAscii(c.remainingHex()));

            default -> {
                // Ignore unsupported formats
            }
        }
    }
    private static class Cursor {

        private final String hex;

        private int position;

        Cursor(String hex) {

            this.hex = hex;
        }

        String take(int hexChars) {

            if (position + hexChars > hex.length()) {

                throw new RuntimeException(
                        "Unexpected end of DE62."
                                + " Position="
                                + position);
            }

            String value =
                    hex.substring(position,
                            position + hexChars);

            position += hexChars;

            return value;
        }

        int remaining() {

            return hex.length() - position;
        }

        String remainingHex() {

            return hex.substring(position);
        }

    }

}