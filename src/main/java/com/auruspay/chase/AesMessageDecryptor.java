package com.auruspay.chase;

import java.security.GeneralSecurityException;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Decrypts base64-encoded, AES-encrypted ISO 8583 payloads.
 *
 * SECURITY NOTE: the original class had the raw AES key bytes hardcoded in
 * source. That means the key ships inside every jar/class file and shows up
 * in decompiled bytecode - anyone with read access to the artifact has the
 * key. For production this key MUST come from a secrets manager (Vault,
 * AWS/Azure/GCP secrets manager, K8s secret mounted as env var, etc.), not
 * application.properties committed to source control.
 *
 * Configure via:
 *   iso8583.aes.key-base64=<base64 of your 16/24/32 raw key bytes>
 *
 * This class also drops the KeyStore round-trip from the original
 * (SecretKeySpec -> KeyStore.setEntry -> KeyStore.getEntry back out), which
 * added no security value over using the SecretKeySpec directly - it wasn't
 * backed by a password-protected keystore file, so it was just extra
 * indirection around the same in-memory key. If you need real
 * keystore-backed key management (PKCS12 file, HSM, etc.), that's a
 * different integration and worth doing properly rather than emulating it.
 *
 * ALGORITHM NOTE: Cipher.getInstance("AES") resolves to AES/ECB/PKCS5Padding,
 * same as the original code. ECB is not semantically secure (identical
 * plaintext blocks produce identical ciphertext blocks), but changing it
 * would break compatibility with whatever system encrypts these messages
 * upstream. Left unchanged intentionally - flag to your security team if
 * you control both sides and can move to AES/GCM/NoPadding with a random IV.
 */
@Component
public class AesMessageDecryptor {

    private static final Logger log = LoggerFactory.getLogger(AesMessageDecryptor.class);

    // Fallback matches the original hardcoded key so behavior doesn't change
    // silently if the property isn't set yet - but this fallback should be
    // removed once the property/secret is wired up in every environment.
    private static final byte[] DEFAULT_KEY_BYTES = {
            -93, 9, -69, 73, -73, 100, -39, 91, -47, 118, 102, -16, 112, -100, 40, -127
    };

    private final SecretKeySpec keySpec;

    public AesMessageDecryptor(@Value("${iso8583.aes.key-base64:}") String keyBase64) {
        byte[] keyBytes = (keyBase64 == null || keyBase64.isBlank())
                ? DEFAULT_KEY_BYTES
                : Base64.getDecoder().decode(keyBase64);
        if (keyBytes == DEFAULT_KEY_BYTES) {
            log.warn("iso8583.aes.key-base64 not set - falling back to built-in default key. "
                    + "Do not run production traffic on the default key.");
        }
        this.keySpec = new SecretKeySpec(keyBytes, "AES");
    }

    /**
     * Decrypts a base64-encoded AES ciphertext. Returns the input unchanged
     * if it is null/blank (matches original behavior).
     *
     * @throws Iso8583ParseException if decryption fails (bad key, corrupt
     *         input, wrong padding, etc.) - callers get an explicit failure
     *         instead of the original's swallow-and-log approach.
     */
    public String decrypt(String request) throws Iso8583ParseException {
        if (request == null || request.trim().isEmpty()) {
            return request;
        }
        try {
            // Base64.getDecoder() is the strict/basic decoder: it rejects any
            // character outside the base64 alphabet, including a trailing
            // newline or padding space that callers (curl -d @file, Postman
            // raw bodies, etc.) commonly leave in. Strip whitespace defensively
            // rather than trusting every caller to send a clean string.
            String cleaned = request.replaceAll("\\s+", "");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            byte[] outputBytes = cipher.doFinal(Base64.getDecoder().decode(cleaned));
            return new String(outputBytes);
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            log.error("Failed to decrypt inbound ISO8583 payload", e);
            throw new Iso8583ParseException("Unable to decrypt message payload", e);
        }
    }
}