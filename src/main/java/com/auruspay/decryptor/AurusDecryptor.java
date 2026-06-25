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

	private static final Logger log = LoggerFactory.getLogger(AurusDecryptor.class);

	private static final ObjectMapper mapper = new ObjectMapper();

	// ==========================================
	// AES KEY
	// ==========================================

	private static final String HEX_KEY = "A309BB49B764D95BD17666F0709C2881";

	// ==========================================
	// MAIN DECRYPT METHOD
	// ==========================================

	public static String decryptor(String encryptedInput) {

		// ======================================
		// EMPTY CHECK
		// ======================================

		if (encryptedInput == null || encryptedInput.isBlank()) {

			log.warn("Empty encrypted input received");

			return "";
		}

		try {

			// ======================================
			// CLEAN INPUT
			// ======================================

			String cleanInput =

					encryptedInput

							.replace("\\", "")

							.replace("\"", "")

							.replace("\n", "")

							.replace("\r", "")

							.replaceAll("\\s", "")

							.trim();

			log.info("====================================");

			log.info("Starting Decryption Data: {} ",encryptedInput);

			log.info("Encrypted Length : {}", encryptedInput.length());

			log.info("Cleaned Length : {}", cleanInput.length());

			log.info("====================================");

			// ======================================
			// BASE64 VALIDATION
			// ======================================

			if (!cleanInput.matches("^[A-Za-z0-9+/=]+$")) {

				log.error("Invalid Base64 Input");

				return encryptedInput;
			}

			// ======================================
			// DECRYPT
			// ======================================

			String decrypted = decrypt(cleanInput, HEX_KEY);

			// ======================================
			// HTML DECODE
			// ======================================

			String decoded = decodeHtml(decrypted);

			// ======================================
			// CLEAN JSON FORMAT
			// ======================================

			String finalOutput = cleanJsonString(decoded);

			log.info("Decryption Successful");

			return finalOutput;

		} catch (Exception e) {

			log.error("====================================");

			log.error("Decryption failed : {}", e.getMessage(), e);

			log.error("====================================");

			return encryptedInput;
		}
	}

	// ==========================================
	// AES DECRYPT
	// ==========================================

	private static String decrypt(

			String base64Content,

			String hexKey

	) throws Exception {

		byte[] keyBytes = new byte[16];

		for (int i = 0; i < 32; i += 2) {

			keyBytes[i / 2] =

					(byte) (

					(Character.digit(hexKey.charAt(i), 16) << 4)

							+

							Character.digit(hexKey.charAt(i + 1), 16));
		}

		SecretKeySpec secretKey =

				new SecretKeySpec(keyBytes, "AES");

		Cipher cipher =

				Cipher.getInstance("AES/ECB/PKCS5Padding");

		cipher.init(Cipher.DECRYPT_MODE, secretKey);

		byte[] decodedBuffer =

				Base64.getDecoder().decode(base64Content);

		byte[] decryptedBuffer =

				cipher.doFinal(decodedBuffer);

		return new String(

				decryptedBuffer,

				StandardCharsets.UTF_8);
	}

	// ==========================================
	// HTML ENTITY DECODE
	// ==========================================

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

	// ==========================================
	// CLEAN JSON STRING
	// ==========================================

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

			mapper.readTree(cleaned);

			return cleaned;

		} catch (Exception e) {

			return input;
		}
	}

	// ==========================================
	// MAIN METHOD
	// ==========================================

	
}