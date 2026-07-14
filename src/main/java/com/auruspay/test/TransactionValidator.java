package com.auruspay.test;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import com.auruspay.decryptor.AurusDecryptor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TransactionValidator {

    
    public static  AurusDecryptor aurusDecryptor =  new AurusDecryptor();

    public static void main1(String[] args) throws JsonMappingException, JsonProcessingException {
    	
    	


    	        try {

    	            String encryptedData = "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 ";

    	            // Decrypt Request
    	            String decryptedJson =
    	                    String.valueOf(aurusDecryptor.decryptor(encryptedData));

    	            System.out.println("========== DECRYPTED JSON ==========");
    	            System.out.println(decryptedJson);

    	            ObjectMapper mapper = new ObjectMapper();

    	            Map<String, String> requestMap =
    	                    mapper.readValue(
    	                            decryptedJson,
    	                            new TypeReference<Map<String, String>>() {
    	                            });
/*
    	            System.out.println("\n========== REQUEST DATA ==========");

    	            requestMap.forEach((key, value) ->
    	                    System.out.println(key + " = " + value));
*/
    	            System.out.println("\n========== VALIDATION ==========");

    	            System.out.println(" \"4.3\": "
    	                    + requestMap.get("4.3"));

    	            String result = validate(requestMap);

    	            System.out.println("\nValidation Result : " + result);

    	        } catch (Exception e) {

    	            System.err.println("Error while processing request");

    	            e.printStackTrace();
    	        }
    	    }

    	    public static String validate(Map<String, String> request) {

    	        String txnType = request.get("4.3");

    	        if (txnType == null || txnType.isBlank()) {
    	            return "4.3 FIELD MISSING";
    	        }

    	        switch (txnType) {

    	            case "6":
    	                return validateTxnType6(request);

    	            case "15":
    	                return validateTxnType15(request);

    	            default:
    	                return "NO VALIDATION CONFIGURED FOR 4.3 = " + txnType;
    	        }
    	    }

    	    /**
    	     * Validation for Transaction Type 6
    	     */
    	    private static String validateTxnType6(Map<String, String> request) {

    	    	String field44 = request.get("4.4");

    	    	if (field44 == null || field44.isBlank()) {
    	    	    return "4.4 FIELD MISSING";
    	    	}

    	    	System.out.println("\n4.4 : " + field44);

    	    	if (!field44.matches("^[0-9A-Fa-f]+~[0-9A-Fa-f]+$")) {
    	    	    return "4.4 INVALID FORMAT";
    	    	}

    	        String field81 = request.get("8.1");

    	        if (field81 == null || field81.isBlank()) {
    	            return "8.1 FIELD MISSING";
    	        }

    	        String decoded81 =
    	                URLDecoder.decode(field81, StandardCharsets.UTF_8);

    	        Map<String, String> emvTags = new HashMap<>();

    	        String[] tags = decoded81.split("\u001E");

    	        for (String tag : tags) {

    	            if (tag == null || tag.trim().isEmpty()) {
    	                continue;
    	            }

    	            String[] pair = tag.split("~", 2);

    	            if (pair.length != 2) {
    	                return "8.1 INVALID TAG FORMAT : " + tag;
    	            }

    	            emvTags.put(pair[0].toLowerCase(), pair[1]);
    	        }

    	        String[] mandatoryTags = {
    	                "9f41",
    	                "9f40",
    	                "9f06",
    	                "9f27",
    	                "9f1a",
    	                "9a",
    	                "9f26",
    	                "82",
    	                "9f36",
    	                "9f37",
    	                "95",
    	                "9c",
    	                "5f2a",
    	                "9f02",
    	                "9f10"
    	        };

    	        for (String tag : mandatoryTags) {

    	            if (!emvTags.containsKey(tag)) {
    	                return "8.1 MISSING TAG : "
    	                        + tag.toUpperCase();
    	            }
    	        }

    	        return "VALID";
    	    }

    	    /**
    	     * Validation for Transaction Type 15
    	     */
    	    private static String validateTxnType15(Map<String, String> request) {

    	        String field444 = request.get("4.44");

    	        if (field444 == null || field444.isBlank()) {
    	            return "4.44 FIELD MISSING";
    	        }

    	        System.out.println("4.44 : " + field444);

    	        if (!field444.matches("^\\d{32}$")) {
    	            return "4.44 INVALID FORMAT FOR 4.3=15";
    	        }

    	        return "VALID";
    	    }
    	}