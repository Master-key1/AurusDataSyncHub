package com.auruspay.chase.validation;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

/**
 * Field-level validators for ISO 8583 data elements, run against a pair of
 * parsed messages (typically an "approved" and a "declined" golden-template
 * test case) to check each bit's format independently.
 *
 * IMPORTANT: this checks FORMAT/RANGE per bit, not approved==declined
 * equality. Transaction-specific fields (PAN, amount, STAN, date/time,
 * terminal/acquirer IDs, auth codes) are expected to legitimately differ
 * between two different test transactions - only a handful of fields
 * (MCC validity, date/time being well-formed) get extra semantic checks.
 * If your test suite actually needs "these two must match" assertions for
 * specific bits (e.g. same terminal ID used in both cases), add that check
 * in compareTransactions where noted - it's a business rule this class
 * can't infer from the data alone.
 */
@Component
public class Iso8583FieldValidator {

    private static final Pattern DIGITS = Pattern.compile("^[0-9]+$");
    private static final Pattern HEX = Pattern.compile("^[0-9A-Fa-f]+$");
    private static final Pattern ALNUM = Pattern.compile("^[0-9A-Za-z]+$");

    // ---------------------------------------------------------------
    // Orchestrator
    // ---------------------------------------------------------------

    /**
     * Runs every applicable bit validator against both field maps and
     * returns one result per bit present in either message. 
     */
    public List<Iso8583FieldValidationResult> compareTransactions(
            Map<Integer, String> approvedRequest, Map<Integer, String> declinedRequest) {

        List<Iso8583FieldValidationResult> results = new ArrayList<>();
        TreeSet<Integer> allBits = new TreeSet<>();
        allBits.addAll(approvedRequest.keySet());
        allBits.addAll(declinedRequest.keySet());

        for (int bit : allBits) {
            String approvedValue = approvedRequest.get(bit);
            String declinedValue = declinedRequest.get(bit);

            Iso8583FieldValidationResult result = switch (bit) {
                case 2 -> validateBit2(bit, approvedValue, declinedValue);
                case 3 -> validateBit3(bit, approvedValue, declinedValue);
                case 4 -> validateBit4(bit, approvedValue, declinedValue);
                case 7 -> validateBit7(bit, approvedValue, declinedValue);
                case 8 -> validateBit8(bit, approvedValue, declinedValue);
                case 11 -> validateBit11(bit, approvedValue, declinedValue);
                case 12 -> validateBit12(bit, approvedValue, declinedValue);
                case 13 -> validateBit13(bit, approvedValue, declinedValue);
                case 14 -> validateBit14(bit, approvedValue, declinedValue);
                case 15 -> validateBit15(bit, approvedValue, declinedValue);
                case 18 -> validateBit18(bit, approvedValue, declinedValue);
                case 22 -> validateBit22(bit, approvedValue, declinedValue);
                case 23 -> validateBit23(bit, approvedValue, declinedValue);
                case 25 -> validateBit25(bit, approvedValue, declinedValue);
                case 35 -> validateBit35(bit, approvedValue, declinedValue);
                case 37 -> validateBit37(bit, approvedValue, declinedValue);
                case 38 -> validateBit38(bit, approvedValue, declinedValue);
                case 39 -> validateBit39(bit, approvedValue, declinedValue);
                case 41 -> validateBit41(bit, approvedValue, declinedValue);
                case 42 -> validateBit42(bit, approvedValue, declinedValue);
                case 44 -> validateBit44(bit, approvedValue, declinedValue);
                case 45 -> validateBit45(bit, approvedValue, declinedValue);
                case 48 -> validateBit48(bit, approvedValue, declinedValue);
                case 49 -> validateBit49(bit, approvedValue, declinedValue);
                case 52 -> validateBit52(bit, approvedValue, declinedValue);
                case 53 -> validateBit53(bit, approvedValue, declinedValue);
                case 54 -> validateBit54(bit, approvedValue, declinedValue);
                case 55 -> validateBit55(bit, approvedValue, declinedValue);
                case 60 -> validateBit60(bit, approvedValue, declinedValue);
                case 62 -> validateBit62(bit, approvedValue, declinedValue);
                case 63 -> validateBit63(bit, approvedValue, declinedValue);
                case 70 -> validateBit70(bit, approvedValue, declinedValue);
                case 71 -> validateBit71(bit, approvedValue, declinedValue);
                case 72 -> validateBit72(bit, approvedValue, declinedValue);
                case 90 -> validateBit90(bit, approvedValue, declinedValue);
                case 121 -> validateBit121(bit, approvedValue, declinedValue);
                case 122 -> validateBit122(bit, approvedValue, declinedValue);
                case 123 -> validateBit123(bit, approvedValue, declinedValue);
                default -> validateGeneric(bit, "Bit " + bit, approvedValue, declinedValue);
            };
            results.add(result);
        }
        return results;
    }

    // ---------------------------------------------------------------
    // Per-bit validators
    // ---------------------------------------------------------------

    public Iso8583FieldValidationResult validateBit2(int field, String approvedValue, String declinedValue) {
        Iso8583FieldValidationResult r = new Iso8583FieldValidationResult(field, "PAN", approvedValue, declinedValue);
        checkPattern(r, true, approvedValue, DIGITS, 12, 19, "PAN must be 12-19 numeric digits");
        checkPattern(r, false, declinedValue, DIGITS, 12, 19, "PAN must be 12-19 numeric digits");
        return r;
    }

    public Iso8583FieldValidationResult validateBit3(int field, String approvedValue, String declinedValue) {
        Iso8583FieldValidationResult r = new Iso8583FieldValidationResult(field, "Processing Code", approvedValue, declinedValue);
        checkPattern(r, true, approvedValue, DIGITS, 6, 6, "Processing code must be exactly 6 numeric digits");
        checkPattern(r, false, declinedValue, DIGITS, 6, 6, "Processing code must be exactly 6 numeric digits");
        return r;
    }

    public Iso8583FieldValidationResult validateBit4(int field, String approvedValue, String declinedValue) {
        Iso8583FieldValidationResult r = new Iso8583FieldValidationResult(field, "Transaction Amount", approvedValue, declinedValue);
        checkPattern(r, true, approvedValue, DIGITS, 12, 12, "Amount must be exactly 12 numeric digits");
        checkPattern(r, false, declinedValue, DIGITS, 12, 12, "Amount must be exactly 12 numeric digits");
        return r;
    }

    public Iso8583FieldValidationResult validateBit7(int field, String approvedValue, String declinedValue) {
        Iso8583FieldValidationResult r = new Iso8583FieldValidationResult(field, "Transmission Date/Time", approvedValue, declinedValue);
        checkPattern(r, true, approvedValue, DIGITS, 10, 10, "Must be 10 numeric digits (MMDDhhmmss)");
        checkPattern(r, false, declinedValue, DIGITS, 10, 10, "Must be 10 numeric digits (MMDDhhmmss)");
        return r;
    }

    public Iso8583FieldValidationResult validateBit8(int field, String approvedValue, String declinedValue) {
        Iso8583FieldValidationResult r = new Iso8583FieldValidationResult(field, "Amount, Cardholder Billing Fee", approvedValue, declinedValue);
        checkPattern(r, true, approvedValue, DIGITS, 12, 12, "Must be exactly 12 numeric digits");
        checkPattern(r, false, declinedValue, DIGITS, 12, 12, "Must be exactly 12 numeric digits");
        return r;
    }

    public Iso8583FieldValidationResult validateBit11(int field, String approvedValue, String declinedValue) {
        Iso8583FieldValidationResult r = new Iso8583FieldValidationResult(field, "STAN", approvedValue, declinedValue);
        checkPattern(r, true, approvedValue, DIGITS, 6, 6, "STAN must be exactly 6 numeric digits");
        checkPattern(r, false, declinedValue, DIGITS, 6, 6, "STAN must be exactly 6 numeric digits");
        if (approvedValue != null && approvedValue.equals(declinedValue)) {
            r.note("Approved and declined share the same STAN - verify these are genuinely distinct transactions");
        }
        return r;
    }

    public Iso8583FieldValidationResult validateBit12(int field, String approvedValue, String declinedValue) {
        Iso8583FieldValidationResult r = new Iso8583FieldValidationResult(field, "Local Transaction Time", approvedValue, declinedValue);
        checkTime(r, true, approvedValue);
        checkTime(r, false, declinedValue);
        return r;
    }

    public Iso8583FieldValidationResult validateBit13(int field, String approvedValue, String declinedValue) {
        Iso8583FieldValidationResult r = new Iso8583FieldValidationResult(field, "Local Transaction Date", approvedValue, declinedValue);
        checkLocalDate(r, true, approvedValue);
        checkLocalDate(r, false, declinedValue);
        return r;
    }

    public Iso8583FieldValidationResult validateBit14(int field, String approvedValue, String declinedValue) {
        Iso8583FieldValidationResult r = new Iso8583FieldValidationResult(field, "Expiration Date (YYMM)", approvedValue, declinedValue);
        checkExpiry(r, true, approvedValue);
        checkExpiry(r, false, declinedValue);
        return r;
    }

    public Iso8583FieldValidationResult validateBit15(int field, String approvedValue, String declinedValue) {
        Iso8583FieldValidationResult r = new Iso8583FieldValidationResult(field, "Settlement Date", approvedValue, declinedValue);
        checkPattern(r, true, approvedValue, DIGITS, 4, 4, "Must be exactly 4 numeric digits (MMDD)");
        checkPattern(r, false, declinedValue, DIGITS, 4, 4, "Must be exactly 4 numeric digits (MMDD)");
        return r;
    }

    public Iso8583FieldValidationResult validateBit18(int field, String approvedValue, String declinedValue) {
        Iso8583FieldValidationResult r = new Iso8583FieldValidationResult(field, "Merchant Category Code", approvedValue, declinedValue);
        checkMcc(r, true, approvedValue);
        checkMcc(r, false, declinedValue);
        return r;
    }

    public Iso8583FieldValidationResult validateBit22(int field, String approvedValue, String declinedValue) {
        Iso8583FieldValidationResult r = new Iso8583FieldValidationResult(field, "POS Entry Mode", approvedValue, declinedValue);
        checkPattern(r, true, approvedValue, DIGITS, 3, 3, "Must be exactly 3 numeric digits");
        checkPattern(r, false, declinedValue, DIGITS, 3, 3, "Must be exactly 3 numeric digits");
        return r;
    }

    public Iso8583FieldValidationResult validateBit23(int field, String approvedValue, String declinedValue) {
        Iso8583FieldValidationResult r = new Iso8583FieldValidationResult(field, "PAN Sequence Number", approvedValue, declinedValue);
        checkPattern(r, true, approvedValue, DIGITS, 2, 2, "Must be exactly 2 numeric digits");
        checkPattern(r, false, declinedValue, DIGITS, 2, 2, "Must be exactly 2 numeric digits");
        return r;
    }

    public Iso8583FieldValidationResult validateBit25(int field, String approvedValue, String declinedValue) {
        Iso8583FieldValidationResult r = new Iso8583FieldValidationResult(field, "POS Condition Code", approvedValue, declinedValue);
        checkPattern(r, true, approvedValue, DIGITS, 2, 2, "Must be exactly 2 numeric digits");
        checkPattern(r, false, declinedValue, DIGITS, 2, 2, "Must be exactly 2 numeric digits");
        return r;
    }

    public Iso8583FieldValidationResult validateBit35(int field, String approvedValue, String declinedValue) {
        Iso8583FieldValidationResult r = new Iso8583FieldValidationResult(field, "Track 2 Data", approvedValue, declinedValue);
        checkTrack2(r, true, approvedValue);
        checkTrack2(r, false, declinedValue);
        return r;
    }

    public Iso8583FieldValidationResult validateBit37(int field, String approvedValue, String declinedValue) {
        Iso8583FieldValidationResult r = new Iso8583FieldValidationResult(field, "Retrieval Reference Number", approvedValue, declinedValue);
        checkPattern(r, true, approvedValue, ALNUM, 12, 12, "RRN must be exactly 12 alphanumeric characters");
        checkPattern(r, false, declinedValue, ALNUM, 12, 12, "RRN must be exactly 12 alphanumeric characters");
        if (approvedValue != null && approvedValue.equals(declinedValue)) {
            r.note("Approved and declined share the same RRN - verify these are genuinely distinct transactions");
        }
        return r;
    }

    public Iso8583FieldValidationResult validateBit38(int field, String approvedValue, String declinedValue) {
        Iso8583FieldValidationResult r = new Iso8583FieldValidationResult(field, "Authorization ID Response", approvedValue, declinedValue);
        checkPattern(r, true, approvedValue, ALNUM, 1, 6, "Auth ID must be 1-6 alphanumeric characters");
        checkPattern(r, false, declinedValue, ALNUM, 1, 6, "Auth ID must be 1-6 alphanumeric characters");
        return r;
    }

    public Iso8583FieldValidationResult validateBit39(int field, String approvedValue, String declinedValue) {
        Iso8583FieldValidationResult r = new Iso8583FieldValidationResult(field, "Response Code", approvedValue, declinedValue);
        checkPattern(r, true, approvedValue, ALNUM, 2, 2, "Response code must be exactly 2 characters");
        checkPattern(r, false, declinedValue, ALNUM, 2, 2, "Response code must be exactly 2 characters");
        if ("00".equals(approvedValue)) {
            r.note("Approved response code confirmed as 00");
        } else if (approvedValue != null) {
            r.note("Expected approved response code 00, got " + approvedValue);
        }
        if (declinedValue != null && "00".equals(declinedValue)) {
            r.note("Declined message carries an approval response code (00) - check test data");
        }
        return r;
    }

    public Iso8583FieldValidationResult validateBit41(int field, String approvedValue, String declinedValue) {
        Iso8583FieldValidationResult r = new Iso8583FieldValidationResult(field, "Card Acceptor Terminal ID", approvedValue, declinedValue);
        checkPattern(r, true, approvedValue, ALNUM, 3, 3, "Terminal ID must be exactly 3 alphanumeric characters");
        checkPattern(r, false, declinedValue, ALNUM, 3, 3, "Terminal ID must be exactly 3 alphanumeric characters");
        return r;
    }

    public Iso8583FieldValidationResult validateBit42(int field, String approvedValue, String declinedValue) {
        Iso8583FieldValidationResult r = new Iso8583FieldValidationResult(field, "Card Acceptor ID / Acquirer ID", approvedValue, declinedValue);
        checkPattern(r, true, approvedValue, DIGITS, 12, 12, "Must be exactly 12 numeric digits");
        checkPattern(r, false, declinedValue, DIGITS, 12, 12, "Must be exactly 12 numeric digits");
        return r;
    }

    public Iso8583FieldValidationResult validateBit44(int field, String approvedValue, String declinedValue) {
        return validateGeneric(field, "Additional Response Data", approvedValue, declinedValue);
    }

    public Iso8583FieldValidationResult validateBit45(int field, String approvedValue, String declinedValue) {
        return validateGeneric(field, "Track 1 Data", approvedValue, declinedValue);
    }

    public Iso8583FieldValidationResult validateBit48(int field, String approvedValue, String declinedValue) {
        Iso8583FieldValidationResult r = new Iso8583FieldValidationResult(field, "Additional Data - Private Use", approvedValue, declinedValue);
        checkSubfields(r, true, approvedValue);
        checkSubfields(r, false, declinedValue);
        return r;
    }

    public Iso8583FieldValidationResult validateBit49(int field, String approvedValue, String declinedValue) {
        Iso8583FieldValidationResult r = new Iso8583FieldValidationResult(field, "Currency Code, Transaction", approvedValue, declinedValue);
        checkPattern(r, true, approvedValue, DIGITS, 3, 3, "Must be exactly 3 numeric digits");
        checkPattern(r, false, declinedValue, DIGITS, 3, 3, "Must be exactly 3 numeric digits");
        return r;
    }

    public Iso8583FieldValidationResult validateBit52(int field, String approvedValue, String declinedValue) {
        Iso8583FieldValidationResult r = new Iso8583FieldValidationResult(field, "PIN Block", approvedValue, declinedValue);
        checkPattern(r, true, approvedValue, HEX, 16, 16, "PIN block must be exactly 16 hex characters");
        checkPattern(r, false, declinedValue, HEX, 16, 16, "PIN block must be exactly 16 hex characters");
        return r;
    }

    public Iso8583FieldValidationResult validateBit53(int field, String approvedValue, String declinedValue) {
        Iso8583FieldValidationResult r = new Iso8583FieldValidationResult(field, "Security Related Control Information", approvedValue, declinedValue);
        checkPattern(r, true, approvedValue, HEX, 16, 16, "Must be exactly 16 hex characters");
        checkPattern(r, false, declinedValue, HEX, 16, 16, "Must be exactly 16 hex characters");
        return r;
    }

    public Iso8583FieldValidationResult validateBit54(int field, String approvedValue, String declinedValue) {
        return validateGeneric(field, "Additional Amount", approvedValue, declinedValue);
    }

    public Iso8583FieldValidationResult validateBit55(int field, String approvedValue, String declinedValue) {
        Iso8583FieldValidationResult r = new Iso8583FieldValidationResult(field, "EMV/Chip Data", approvedValue, declinedValue);
        checkPattern(r, true, approvedValue, HEX, 6, 1026, "EMV data must be hex characters");
        checkPattern(r, false, declinedValue, HEX, 6, 1026, "EMV data must be hex characters");
        if (approvedValue != null && approvedValue.length() % 2 != 0) {
            r.note("Approved EMV data has an odd character count - unusual for byte-paired TLV hex data");
        }
        if (declinedValue != null && declinedValue.length() % 2 != 0) {
            r.note("Declined EMV data has an odd character count - unusual for byte-paired TLV hex data");
        }
        return r;
    }

    public Iso8583FieldValidationResult validateBit60(int field, String approvedValue, String declinedValue) {
        Iso8583FieldValidationResult r = new Iso8583FieldValidationResult(field, "Reserved National 1", approvedValue, declinedValue);
        checkNotBlank(r, true, approvedValue);
        checkNotBlank(r, false, declinedValue);
        return r;
    }

    public Iso8583FieldValidationResult validateBit62(int field, String approvedValue, String declinedValue) {
        Iso8583FieldValidationResult r = new Iso8583FieldValidationResult(field, "Reserved Private 2", approvedValue, declinedValue);
        checkSubfields(r, true, approvedValue);
        checkSubfields(r, false, declinedValue);
        return r;
    }

    public Iso8583FieldValidationResult validateBit63(int field, String approvedValue, String declinedValue) {
        Iso8583FieldValidationResult r = new Iso8583FieldValidationResult(field, "Reserved Private 3", approvedValue, declinedValue);
        checkSubfields(r, true, approvedValue);
        checkSubfields(r, false, declinedValue);
        return r;
    }

    public Iso8583FieldValidationResult validateBit70(int field, String approvedValue, String declinedValue) {
        Iso8583FieldValidationResult r = new Iso8583FieldValidationResult(field, "Network Management Information Code", approvedValue, declinedValue);
        checkPattern(r, true, approvedValue, DIGITS, 3, 3, "Must be exactly 3 numeric digits");
        checkPattern(r, false, declinedValue, DIGITS, 3, 3, "Must be exactly 3 numeric digits");
        return r;
    }

    public Iso8583FieldValidationResult validateBit71(int field, String approvedValue, String declinedValue) {
        return validateGeneric(field, "Message Number", approvedValue, declinedValue);
    }

    public Iso8583FieldValidationResult validateBit72(int field, String approvedValue, String declinedValue) {
        return validateGeneric(field, "Message Number Last", approvedValue, declinedValue);
    }

    public Iso8583FieldValidationResult validateBit90(int field, String approvedValue, String declinedValue) {
        Iso8583FieldValidationResult r = new Iso8583FieldValidationResult(field, "Original Data Elements", approvedValue, declinedValue);
        checkPattern(r, true, approvedValue, ALNUM, 42, 42, "Original data elements must be 42 alphanumeric characters (per field length)");
        checkPattern(r, false, declinedValue, ALNUM, 42, 42, "Original data elements must be 42 alphanumeric characters (per field length)");
        return r;
    }

    public Iso8583FieldValidationResult validateBit121(int field, String approvedValue, String declinedValue) {
        return validateGeneric(field, "eWIC Product Data 3", approvedValue, declinedValue);
    }

    public Iso8583FieldValidationResult validateBit122(int field, String approvedValue, String declinedValue) {
        return validateGeneric(field, "eWIC Product Data 2", approvedValue, declinedValue);
    }

    public Iso8583FieldValidationResult validateBit123(int field, String approvedValue, String declinedValue) {
        return validateGeneric(field, "eWIC Product Data 1", approvedValue, declinedValue);
    }

    public Iso8583FieldValidationResult validateGeneric(int field, String fieldName, String approvedValue, String declinedValue) {
        Iso8583FieldValidationResult r = new Iso8583FieldValidationResult(field, fieldName, approvedValue, declinedValue);
        checkNotBlank(r, true, approvedValue);
        checkNotBlank(r, false, declinedValue);
        return r;
    }

    // ---------------------------------------------------------------
    // Shared checks
    // ---------------------------------------------------------------

    private void checkPattern(Iso8583FieldValidationResult r, boolean isApproved, String value,
                               Pattern pattern, int minLen, int maxLen, String message) {
        if (value == null) {
            return; // absent/optional field - not itself a failure
        }
        boolean ok = pattern.matcher(value).matches() && value.length() >= minLen && value.length() <= maxLen;
        if (!ok) {
            fail(r, isApproved, message + " (got '" + value + "', length " + value.length() + ")");
        }
    }

    private void checkNotBlank(Iso8583FieldValidationResult r, boolean isApproved, String value) {
        if (value != null && value.trim().isEmpty()) {
            fail(r, isApproved, "Field is present but blank");
        }
    }

    private void checkTime(Iso8583FieldValidationResult r, boolean isApproved, String value) {
        if (value == null) {
            return;
        }
        if (!DIGITS.matcher(value).matches() || value.length() != 6) {
            fail(r, isApproved, "Local time must be 6 numeric digits (HHMMSS)");
            return;
        }
        int hh = Integer.parseInt(value.substring(0, 2));
        int mm = Integer.parseInt(value.substring(2, 4));
        int ss = Integer.parseInt(value.substring(4, 6));
        if (hh > 23 || mm > 59 || ss > 59) {
            fail(r, isApproved, "Local time out of range: " + value);
        }
    }

    private void checkLocalDate(Iso8583FieldValidationResult r, boolean isApproved, String value) {
        if (value == null) {
            return;
        }
        if (!DIGITS.matcher(value).matches() || value.length() != 8) {
            fail(r, isApproved, "Local date must be 8 numeric digits (MMDDYYYY)");
            return;
        }
        int mm = Integer.parseInt(value.substring(0, 2));
        int dd = Integer.parseInt(value.substring(2, 4));
        int yyyy = Integer.parseInt(value.substring(4, 8));
        if (mm < 1 || mm > 12 || dd < 1 || dd > 31 || yyyy < 2000 || yyyy > 2099) {
            fail(r, isApproved, "Local date out of range: " + value);
        }
    }

    private void checkExpiry(Iso8583FieldValidationResult r, boolean isApproved, String value) {
        if (value == null) {
            return;
        }
        if (!DIGITS.matcher(value).matches() || value.length() != 4) {
            fail(r, isApproved, "Expiration date must be 4 numeric digits (YYMM)");
            return;
        }
        int mm = Integer.parseInt(value.substring(2, 4));
        if (mm < 1 || mm > 12) {
            fail(r, isApproved, "Expiration month out of range: " + value);
        }
    }

    private void checkMcc(Iso8583FieldValidationResult r, boolean isApproved, String value) {
        if (value == null) {
            return;
        }
        if (!DIGITS.matcher(value).matches() || value.length() != 4) {
            fail(r, isApproved, "MCC must be exactly 4 numeric digits");
            return;
        }
        int mcc = Integer.parseInt(value);
        if (mcc == 0 || mcc == 9999) {
            fail(r, isApproved, "MCC cannot be 0000 or 9999: " + value);
        }
    }

    private void checkTrack2(Iso8583FieldValidationResult r, boolean isApproved, String value) {
        if (value == null) {
            return;
        }
        if (!value.matches("^[0-9]{1,19}=[0-9]{3,}$")) {
            fail(r, isApproved, "Track 2 data must match PAN=<expiry+service+discretionary> numeric format");
        }
    }

    /** Parses "TAG:VALUE;TAG:VALUE;..." (as produced by Iso8583MessageParser bit48/62/63) and sanity-checks each entry. */
    private void checkSubfields(Iso8583FieldValidationResult r, boolean isApproved, String raw) {
        if (raw == null) {
            return;
        }
        Map<String, String> subfields = parseSubfields(raw);
        if (subfields.isEmpty()) {
            fail(r, isApproved, "Sub-field data present but could not be parsed: '" + raw + "'");
            return;
        }
        for (Map.Entry<String, String> e : subfields.entrySet()) {
            if (e.getValue() == null || e.getValue().trim().isEmpty()) {
                fail(r, isApproved, "Sub-tag " + e.getKey() + " has an empty value");
            }
        }
    }

    private Map<String, String> parseSubfields(String raw) {
        Map<String, String> map = new LinkedHashMap<>();
        for (String entry : raw.split(";")) {
            if (entry.isBlank()) {
                continue;
            }
            int idx = entry.indexOf(':');
            if (idx < 0) {
                continue;
            }
            map.put(entry.substring(0, idx), entry.substring(idx + 1));
        }
        return map;
    }

    private void fail(Iso8583FieldValidationResult r, boolean isApproved, String message) {
        if (isApproved) {
            r.failApproved(message);
        } else {
            r.failDeclined(message);
        }
    }
}