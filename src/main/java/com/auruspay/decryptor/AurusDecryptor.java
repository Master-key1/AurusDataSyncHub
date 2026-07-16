package com.auruspay.decryptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class AurusDecryptor {

    private static final Logger log =
            LoggerFactory.getLogger(AurusDecryptor.class);

    private static final ObjectMapper mapper = new ObjectMapper();

    // AES HEX KEY (16 bytes)
    private static final String HEX_KEY =
            "A309BB49B764D95BD17666F0709C2881";


    public static String decryptor(String encryptedInput) {

        if (encryptedInput == null || encryptedInput.isBlank()) {

            log.warn("Empty encrypted input received");

            return "";
        }


        try {

            /*
             * Clean encrypted input
             */
            String cleanInput = encryptedInput
                    .replace("\\", "")
                    .replace("\"", "")
                    .replace("[", "")
                    .replace("]", "")
                    .replace("\n", "")
                    .replace("\r", "")
                    .replaceAll("\\s", "")
                    .trim();


            log.info("====================================");
            log.info("Encrypted Length : {}", encryptedInput.length());
            log.info("Cleaned Length   : {}", cleanInput.length());

            if (cleanInput.length() > 20) {
                log.info("Start : {}", cleanInput.substring(0, 20));
                log.info("End   : {}",
                        cleanInput.substring(cleanInput.length() - 20));
            }

            log.info("====================================");


            /*
             * Base64 validation
             */
            try {

                Base64.getDecoder().decode(cleanInput);

            } catch (IllegalArgumentException e) {

                log.error("Invalid Base64 encrypted input");

                return "";
            }


            /*
             * AES decrypt
             */
            String decrypted =
                    decrypt(cleanInput, HEX_KEY);


            /*
             * Decode HTML entities
             */
            String decoded =
                    decodeHtml(decrypted);


            /*
             * Clean JSON
             */
            String finalOutput =
                    cleanJsonString(decoded);


            log.info("Decryption Successful");

            return finalOutput;


        } catch (Exception e) {

            log.error("====================================");
            log.error("Decryption failed", e);
            log.error("====================================");

            return "";
        }
    }



    private static String decrypt(
            String base64Content,
            String hexKey
    ) throws Exception {


        byte[] keyBytes =
                hexStringToByteArray(hexKey);


        SecretKeySpec secretKey =
                new SecretKeySpec(keyBytes, "AES");


        Cipher cipher =
                Cipher.getInstance("AES/ECB/PKCS5Padding");


        cipher.init(
                Cipher.DECRYPT_MODE,
                secretKey
        );


        byte[] encryptedBytes =
                Base64.getDecoder()
                        .decode(base64Content);


        byte[] decryptedBytes =
                cipher.doFinal(encryptedBytes);


        return new String(
                decryptedBytes,
                StandardCharsets.UTF_8
        );
    }



    private static byte[] hexStringToByteArray(String hex) {

        byte[] bytes =
                new byte[hex.length() / 2];


        for (int i = 0; i < hex.length(); i += 2) {

            bytes[i / 2] =
                    (byte)
                            ((Character.digit(hex.charAt(i), 16) << 4)
                                    +
                                    Character.digit(hex.charAt(i + 1), 16));
        }

        return bytes;
    }



    private static String decodeHtml(String input) {

        if (input == null) {
            return "";
        }


        return input
                .replace("&gt;", ">")
                .replace("&lt;", "<")
                .replace("&amp;", "&")
                .replace("&#37;", "%")
                .replace("&apos;", "'")
                .replace("&quot;", "\"");
    }



    public static String cleanJsonString(String input) {

        if (input == null || input.isBlank()) {

            return "";
        }


        try {

            String cleaned =
                    input
                            .replaceAll("^\"", "")
                            .replaceAll("\"$", "")
                            .replace("\\\"", "\"")
                            .replace("\\n", "")
                            .replace("\\r", "")
                            .replace("\\\\", "\\")
                            .trim();


            // Validate JSON only if it is JSON
            mapper.readTree(cleaned);


            return cleaned;


        } catch (Exception e) {

            return input;
        }
    }
}