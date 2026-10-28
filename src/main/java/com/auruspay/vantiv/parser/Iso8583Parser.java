package com.worldpay.iso8583.parser;

import com.worldpay.iso8583.model.FieldDefinition;
import com.worldpay.iso8583.model.Iso8583Message;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import com.worldpay.iso8583.parser.Field62Parser;

/**
 * Decodes a raw hex dump of a Worldpay ISO 8583 message.
 * <p>
 * Wire layout: 4 hex chars MTI (2-byte packed BCD) + 16 hex chars primary bitmap
 * (+ 16 hex chars secondary bitmap if bit 1 is set) + fields in ascending bit order,
 * each encoded per {@link FieldDictionary}.
 * <p>
 * This never throws on malformed input. Anything it can't safely decode is recorded
 * in {@link Iso8583Message#getParseIssues()} and parsing stops at that point, so the
 * caller always gets back everything that *could* be decoded plus a clear reason for
 * whatever couldn't.
 * <p>
 * WP-formatted (Worldpay/Vantiv) captures commonly mask sensitive digits - PAN and Track II
 * data - with a placeholder character (typically {@code X}, case-insensitive) instead of the
 * real digit. That placeholder is not a valid hex nibble, so it's handled as a special case
 * rather than treated as corruption: {@link #MASK_CHARS} defines which characters are accepted
 * in place of a digit, both for the message-wide "is this even hex" check and for the
 * packed-numeric digit check in {@link #requireDigits}. Everywhere else in the message
 * (MTI, bitmaps, lengths, alpha/binary fields) still must be strict hex - masking only ever
 * appears inside numeric cardholder-data fields, so loosening the check everywhere else would
 * hide real corruption instead of catching it.
 */
@Component
public class Iso8583Parser {

    /** Placeholder characters WP-formatted captures use to mask sensitive digits (e.g. PAN). */
    private static final String MASK_CHARS = "Xx";
    private final Field62Parser field62Parser;
    public Iso8583Parser(Field62Parser field62Parser) {
        this.field62Parser = field62Parser;
    }

    public Iso8583Message parse(String rawHex) {
        Iso8583Message message = new Iso8583Message();

        if (rawHex == null || rawHex.isBlank()) {
            message.addIssue("Empty message");
            return message;
        }

        String hex = rawHex.trim();

        if (hex.length() % 2 != 0) {
            message.addIssue("Message has an odd number of hex characters (" + hex.length()
                    + ") - not byte-aligned. This means at least one hex digit was dropped or "
                    + "added during capture/transcription; the message cannot be reliably parsed "
                    + "past the point of corruption.");
        }
        if (!hex.matches("(?i)[0-9A-F" + MASK_CHARS + "]*")) {
            message.addIssue("Message contains characters that are neither hex digits nor the "
                    + "'" + MASK_CHARS.charAt(0) + "' mask placeholder used for masked PAN/Track II data");
            return message;
        }

        Cursor c = new Cursor(hex);

        if (c.remainingHexChars() < 4) {
            message.addIssue("Message too short to contain an MTI");
            return message;
        }
        message.setMti(c.takeHex(4)); // 2-byte packed BCD MTI, digits == hex chars

        if (c.remainingHexChars() < 16) {
            message.addIssue("Message too short to contain a primary bitmap");
            return message;
        }
        String primaryBitmapHex = c.takeHex(16);
        boolean[] primaryBits = toBits(primaryBitmapHex, message, "primary bitmap");

        boolean[] secondaryBits = new boolean[64];
        if (primaryBits[0]) { // bit 1 => secondary bitmap follows
            if (c.remainingHexChars() < 16) {
                message.addIssue("Secondary bitmap indicated but message is too short to contain it");
                return message;
            }
            String secondaryBitmapHex = c.takeHex(16);
            secondaryBits = toBits(secondaryBitmapHex, message, "secondary bitmap");
        }

        // bit 1 itself is the secondary-bitmap indicator, not a data field - skip it.
        for (int bit = 2; bit <= 64; bit++) {
            if (primaryBits[bit - 1]) {
                if (!decodeField(bit, c, message)) return message;
            }
        }
        for (int bit = 65; bit <= 128; bit++) {
            if (secondaryBits[bit - 65]) {
                if (!decodeField(bit, c, message)) return message;
            }
        }

        if (c.remainingHexChars() > 0) {
            message.addIssue("Message has " + c.remainingHexChars()
                    + " trailing hex characters left over after decoding all fields in the bitmap "
                    + "- likely an unmapped field or a length miscalculation upstream.");
        }

        return message;
    }

    /** @return false if decoding had to stop early (issue already recorded on the message) */
    private boolean decodeField(int bit, Cursor c, Iso8583Message message) {
        FieldDefinition def = FieldDictionary.get(bit);
        if (def == null) {
            message.addIssue("Bit " + bit + " is set but has no field definition in FieldDictionary - "
                    + "cannot determine its length, so remaining fields cannot be reliably decoded.");
            return false;
        }
        try {
            switch (def.format()) {
                case FIXED_NUMERIC_PACKED -> {
                    String raw = c.takeHex(def.byteLength() * 2);
                    String digits = def.digitLength() > 0 ? raw.substring(0, def.digitLength()) : raw;
                    if (!requireDigits(bit, def, digits, message)) return false;
                    message.putField(bit, digits);
                }
                case FIXED_ALPHA -> {
                    String raw = c.takeHex(def.byteLength() * 2);
                    message.putField(bit, hexToAscii(raw));
                }
                case BINARY_FIXED -> {
                    String raw = c.takeHex(def.byteLength() * 2);
                    message.putField(bit, raw); // display as hex, not human-readable
                }
                case LLVAR_NUMERIC_PACKED -> {
                    int digitCount = Integer.parseInt(c.takeHex(2), 16); // 1 binary length byte
                    int byteCount = (digitCount + 1) / 2;
                    String raw = c.takeHex(byteCount * 2);
                    String digits = raw.substring(0, digitCount);
                    if (!requireDigits(bit, def, digits, message)) return false;
                    message.putField(bit, digits);
                }
                case TRACK2_DATA -> {
                    int charCount = Integer.parseInt(c.takeHex(2), 16); // 1 binary length byte
                    int byteCount = (charCount + 1) / 2;
                    String raw = c.takeHex(byteCount * 2);
                    String track2 = raw.substring(0, charCount);
                    if (!requireTrack2Chars(bit, def, track2, message)) return false;
                    message.putField(bit, track2);
                }
                case LLVAR_RAW -> {
                    int charCount = Integer.parseInt(c.takeHex(2), 16); // 1 binary length byte
                    int byteCount = (charCount + 1) / 2;
                    String raw = c.takeHex(byteCount * 2);
                    message.putField(bit, raw.substring(0, charCount)); // printed as-is, no content validation
                }
                case LLLVAR_BINARY_LENGTH -> {

                    int byteCount = Integer.parseInt(c.takeHex(4), 16);

                    String raw = c.takeHex(byteCount * 2);

                    message.putField(bit, raw);

                    switch (bit) {

                        case 60 ->
                            message.putSubFields(60, parseField60(raw));

                        case 62 ->
                            message.putSubFields(62, field62Parser.parse(raw));
                            
                        //case 63 -> message.putSubFields(63, field63Parser.parse(raw));
                       // case 120 -> message.putSubFields(120, field120Parser.parse(raw));

                    }
                }
                
                case LLLVAR_ASCII_BINARY_LENGTH -> {

                    int byteCount = Integer.parseInt(c.takeHex(4), 16);

                    String raw = c.takeHex(byteCount * 2);

                    message.putField(bit, hexToAscii(raw));
                }
            }
            return true;
        } catch (Cursor.TruncatedException e) {
            message.addIssue("Bit " + bit + " (" + def.name() + ") expected more data than the message "
                    + "contains - message is truncated or an earlier field's length was decoded wrong.");
            return false;
        } catch (NumberFormatException e) {
            message.addIssue("Bit " + bit + " (" + def.name() + ") length prefix is not valid hex/binary - "
                    + "message is likely misaligned from an earlier field.");
            return false;
        }
    }

    private java.util.Map<String, String> parseField60(String hex) {

        java.util.Map<String, String> map = new java.util.LinkedHashMap<>();

        String value = hexToAscii(hex);

        int pos = 0;

        try {
        	 	
            map.put("60.1 Terminal Type",
                    value.substring(pos, pos + 1));
            pos += 1;

            map.put("60.2 Physical Terminal Location",
                    value.substring(pos, pos + 1));
            pos += 1;

            map.put("60.3 Terminal Entry Capability",
                    value.substring(pos, pos + 1));
            pos += 1;

            map.put("60.4 Merchant Type Indicator",
                    value.substring(pos, pos + 1));
            pos += 1;

            map.put("60.5 POS Card Retention Indicator",
                    value.substring(pos, pos + 1));
            pos += 1;

            map.put("60.6 POS Transaction Status Indicator",
                    value.substring(pos, pos + 1));
            pos += 1;

            map.put("60.7 POS Transaction Routing Indicator",
                    value.substring(pos, pos + 1));
            pos += 1;

            map.put("60.8 Chain Code",
                    value.substring(pos, pos + 6));
            pos += 6;

            map.put("60.9 Division Number",
                    value.substring(pos, pos + 3));
            pos += 3;
            
            
            map.put("60.10 Store Number",
                    value.substring(pos, pos + 8));
            pos += 8;
            
            map.put("60.11 Register/Lane Number	",
                    value.substring(pos, pos + 3));
            pos += 3;

            map.put("60.12 Employee Number",
                    value.substring(pos));

        }
        catch (Exception ex) {
            map.put("ERROR",
                    "Unable to parse DE60 : " + ex.getMessage());
        }

        return map;
    }
    /**
     * Packed-BCD fields must decode to decimal digits - or, in a WP-formatted capture, the mask
     * placeholder ({@link #MASK_CHARS}) standing in for a digit that was redacted before it was
     * logged (PAN, Track II). A hex letter that is neither a digit nor the mask placeholder
     * (A-F, aside from the placeholder itself) almost always means the message is misaligned -
     * either a dropped/extra hex character earlier in the stream, or a wrong length assumption
     * for a preceding field.
     */
    private boolean requireDigits(int bit, FieldDefinition def, String digits, Iso8583Message message) {
        if (!digits.chars().allMatch(ch -> Character.isDigit(ch) || MASK_CHARS.indexOf(ch) >= 0)) {
            message.addIssue("Bit " + bit + " (" + def.name() + ") decoded to '" + digits + "', which is not "
                    + "all digits (or masked-digit placeholders) despite being a packed-numeric field. This "
                    + "means the message is misaligned from an earlier point (dropped/extra hex character, or "
                    + "a wrong preceding field length) - decoding stopped here rather than return garbage.");
            return false;
        }
        return true;
    }

    /**
     * Track II data is packed BCD like other numeric fields, but it legitimately contains one
     * non-digit separator nibble (conventionally {@code D}) between the PAN and the expiry -
     * plus, in a WP-formatted capture, mask placeholders over the PAN digits. Anything else
     * (another hex letter, a second separator) still means the message is misaligned.
     */
    private boolean requireTrack2Chars(int bit, FieldDefinition def, String track2, Iso8583Message message) {
        if (!track2.chars().allMatch(ch -> Character.isDigit(ch) || MASK_CHARS.indexOf(ch) >= 0 || ch == 'D' || ch == 'd')) {
            message.addIssue("Bit " + bit + " (" + def.name() + ") decoded to '" + track2 + "', which contains "
                    + "characters other than digits, masked-digit placeholders, and the Track II field "
                    + "separator ('D'). This means the message is misaligned from an earlier point "
                    + "(dropped/extra hex character, or a wrong preceding field length) - decoding stopped "
                    + "here rather than return garbage.");
            return false;
        }
        return true;
    }

    private boolean[] toBits(String bitmapHex, Iso8583Message message, String label) {
        BigInteger v = new BigInteger(bitmapHex, 16);
        boolean[] bits = new boolean[64];
        for (int i = 0; i < 64; i++) {
            bits[i] = v.testBit(63 - i);
        }
        return bits;
    }

    private String hexToAscii(String hex) {
        byte[] bytes = new byte[hex.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(hex.substring(i * 2, i * 2 + 2), 16);
        }
        return new String(bytes, StandardCharsets.US_ASCII);
    }

    private final Map<Integer, Map<String, String>> subFields = new LinkedHashMap<>();

    public Map<Integer, Map<String, String>> getSubFields() {
        return subFields;
    }

    public void putSubFields(int bit, Map<String, String> value) {
        subFields.put(bit, value);
    }
    
    /** Small forward-only reader over a hex string, tracking position in hex-char units. */
    private static final class Cursor {
        private final String hex;
        private int pos;

        Cursor(String hex) {
            this.hex = hex;
        }

        int remainingHexChars() {
            return hex.length() - pos;
        }

        String takeHex(int nHexChars) {
            if (pos + nHexChars > hex.length()) {
                throw new TruncatedException();
            }
            String v = hex.substring(pos, pos + nHexChars);
            pos += nHexChars;
            return v;
        }

        static final class TruncatedException extends RuntimeException {
        }
    }
}