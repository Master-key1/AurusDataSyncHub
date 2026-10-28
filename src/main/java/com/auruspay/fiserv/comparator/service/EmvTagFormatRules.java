package com.auruspay.comparator.service;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * Expected hex length/pattern per EMV TLV tag, independent of what the
 * value actually is. Used for tags whose VALUE is supposed to differ every
 * transaction (cryptogram, counters, dates, etc.) - those still need to be
 * the right length and character set, even though comparing them for
 * equality against another transaction is meaningless.
 *
 * Lengths are expressed as hex-character patterns (2 hex chars = 1 byte).
 */
final class EmvTagFormatRules {

    private static final Map<String, Pattern> PATTERNS = Map.ofEntries(
            // Card/terminal identifiers - variable length hex
            Map.entry("4F", Pattern.compile("^[0-9A-Fa-f]{10,32}$")),   // AID: 5-16 bytes
            Map.entry("84", Pattern.compile("^[0-9A-Fa-f]{10,32}$")),   // Dedicated File Name
            Map.entry("9F06", Pattern.compile("^[0-9A-Fa-f]{10,32}$")), // AID (terminal)
            Map.entry("50", Pattern.compile("^[0-9A-Fa-f]{2,32}$")),    // Application Label
            Map.entry("5A", Pattern.compile("^[0-9A-Fa-f]{2,20}$")),    // Application PAN

            // Dates - 3-byte BCD, numeric only
            Map.entry("5F24", Pattern.compile("^\\d{6}$")), // Application Expiration Date
            Map.entry("5F25", Pattern.compile("^\\d{6}$")), // Application Effective Date
            Map.entry("9A", Pattern.compile("^\\d{6}$")),   // Transaction Date
            Map.entry("9F21", Pattern.compile("^\\d{6}$")), // Transaction Time (HHMMSS)

            // Numeric codes - BCD, numeric only
            Map.entry("5F2A", Pattern.compile("^\\d{4}$")), // Transaction Currency Code
            Map.entry("5F30", Pattern.compile("^\\d{4}$")), // Service Code
            Map.entry("5F34", Pattern.compile("^\\d{2}$")), // PAN Sequence Number
            Map.entry("5F36", Pattern.compile("^\\d{1,2}$")), // Transaction Currency Exponent
            Map.entry("9F1A", Pattern.compile("^\\d{4}$")), // Terminal Country Code
            Map.entry("9F15", Pattern.compile("^\\d{4}$")), // Merchant Category Code

            // Amounts - 6-byte BCD, numeric only
            Map.entry("9F02", Pattern.compile("^\\d{12}$")), // Amount Authorised
            Map.entry("9F03", Pattern.compile("^\\d{12}$")), // Amount Other
            Map.entry("9F04", Pattern.compile("^\\d{12}$")), // Amount Other Binary

            // Fixed-length hex (binary/bitmask) fields
            Map.entry("82", Pattern.compile("^[0-9A-Fa-f]{4}$")),   // AIP - 2 bytes
            Map.entry("95", Pattern.compile("^[0-9A-Fa-f]{10}$")),  // TVR - 5 bytes
            Map.entry("9C", Pattern.compile("^[0-9A-Fa-f]{2}$")),   // Transaction Type - 1 byte
            Map.entry("9B", Pattern.compile("^[0-9A-Fa-f]{4}$")),   // Transaction Status Info - 2 bytes
            Map.entry("9F07", Pattern.compile("^[0-9A-Fa-f]{4}$")), // Application Usage Control - 2 bytes
            Map.entry("9F08", Pattern.compile("^[0-9A-Fa-f]{4}$")), // Application Version Number - 2 bytes
            Map.entry("9F09", Pattern.compile("^[0-9A-Fa-f]{4}$")), // Application Version Number Terminal - 2 bytes
            Map.entry("9F10", Pattern.compile("^[0-9A-Fa-f]{2,64}$")), // Issuer Application Data - variable, up to 32 bytes
            Map.entry("9F1B", Pattern.compile("^[0-9A-Fa-f]{8}$")),   // Terminal Floor Limit - 4 bytes
            Map.entry("9F1E", Pattern.compile("^[0-9A-Fa-f]{16}$")),  // IFD Serial Number - 8 bytes
            Map.entry("9F26", Pattern.compile("^[0-9A-Fa-f]{16}$")),  // Application Cryptogram - 8 bytes
            Map.entry("9F27", Pattern.compile("^[0-9A-Fa-f]{2}$")),   // Cryptogram Information Data - 1 byte
            Map.entry("9F33", Pattern.compile("^[0-9A-Fa-f]{6}$")),   // Terminal Capabilities - 3 bytes
            Map.entry("9F34", Pattern.compile("^[0-9A-Fa-f]{6}$")),   // CVM Results - 3 bytes
            Map.entry("9F35", Pattern.compile("^[0-9A-Fa-f]{2}$")),   // Terminal Type - 1 byte
            Map.entry("9F36", Pattern.compile("^[0-9A-Fa-f]{4}$")),   // ATC - 2 bytes
            Map.entry("9F37", Pattern.compile("^[0-9A-Fa-f]{8}$")),   // Unpredictable Number - 4 bytes
            Map.entry("9F39", Pattern.compile("^[0-9A-Fa-f]{2}$")),   // POS Entry Mode - 1 byte
            Map.entry("9F41", Pattern.compile("^[0-9A-Fa-f]{4,8}$")), // Transaction Sequence Counter - 2-4 bytes
            Map.entry("9F53", Pattern.compile("^[0-9A-Fa-f]{2}$")),   // Transaction Category Code - 1 byte
            Map.entry("9F6E", Pattern.compile("^[0-9A-Fa-f]{4,64}$")) // Third Party Data - variable
    );

    /** Generic fallback for tags without an explicit rule: must be valid, even-length hex. */
    private static final Pattern GENERIC_HEX = Pattern.compile("^([0-9A-Fa-f]{2})+$");

    /**
     * @return null if the value satisfies the tag's expected format, otherwise
     *         a human-readable description of what was expected.
     */
    static String validate(String tag, String value) {
        if (value == null || value.isBlank()) {
            return null; // presence is checked elsewhere (missing-tag logic)
        }
        Pattern pattern = PATTERNS.get(tag);
        if (pattern != null) {
            return pattern.matcher(value).matches() ? null : describeExpected(tag, pattern);
        }
        return GENERIC_HEX.matcher(value).matches() ? null : "valid hexadecimal (even number of hex characters)";
    }

    private static String describeExpected(String tag, Pattern pattern) {
        // Human-friendly description without dumping the raw regex
        return switch (tag) {
            case "5F24", "5F25", "9A", "9F21" -> "6 numeric digits (YYMMDD)";
            case "5F2A", "5F30", "9F1A", "9F15" -> "4 numeric digits";
            case "5F34" -> "2 numeric digits";
            case "9F02", "9F03", "9F04" -> "12 numeric digits";
            case "82", "9F07", "9F08", "9F09", "9B" -> "4 hex characters (2 bytes)";
            case "95" -> "10 hex characters (5 bytes)";
            case "9C", "9F27", "9F35", "9F39", "9F53" -> "2 hex characters (1 byte)";
            case "9F33", "9F34" -> "6 hex characters (3 bytes)";
            case "9F36" -> "4 hex characters (2 bytes)";
            case "9F37" -> "8 hex characters (4 bytes)";
            case "9F1E" -> "16 hex characters (8 bytes)";
            case "9F26" -> "16 hex characters (8 bytes)";
            case "4F", "84", "9F06" -> "10-32 hex characters (5-16 bytes)";
            default -> "value matching pattern " + pattern.pattern();
        };
    }

    private EmvTagFormatRules() {
    }
}