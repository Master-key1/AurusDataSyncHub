package com.auruspay.comparator;

import com.auruspay.comparator.config.IsoFieldDefinitionLoader;
import com.auruspay.comparator.model.ComparisonJsonResult;
import com.auruspay.comparator.model.IsoFieldDefinition;
import com.auruspay.comparator.model.IssueDetail;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Compares the ISO-8583-style fields of a declined transaction's JSON payload
 * against the approved transaction's JSON payload and buckets every field
 * into MATCHED / MISMATCH / SKIPPED / VALIDATION-ISSUE.
 *
 * Stateless and thread-safe: every {@link #compare(String, String)} call
 * builds its own {@link ComparisonJsonResult}; no mutable instance state is
 * shared across requests.
 */
@Service
public class JsonComparator {

    private static final Logger log = LoggerFactory.getLogger(JsonComparator.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final String MATCHED = "MATCHED";
    private static final String MISMATCH = "MISMATCH";
    private static final String SKIPPED = "SKIPPED";
    private static final String MISSING = "MISSING";

    private static final Set<String> SKIPPED_VERSION_FIELDS = Set.of(
            "6.1", "6.3", "6.4", "12.54", "6.11", "6.42", "7.4", "4.67", "7.3", "2.4.58",
            "12.1", "6.7", "6.5", "6.6", "6.30", "6.32", "6.99.11","20.15",
            "2.4.3", "7.5", "11.7","12.61","12.94","12.86","11.8","1.141");

    // Fields that must satisfy a dedicated format rule (see isValueValidForField).
    // If both sides differ in value but both individually conform to the rule,
    // the field is treated as an acceptable dynamic mismatch (MATCHED-ish) rather
    // than a hard validation issue.
    private static final Set<String> VALID_PATTERN_FIELDS = Set.of(
    		"1.1","1.2","1.3"
    		);
    private static final Set<String> VALIDATE_PATTERN_FIELDS = Set.of(
            "11.1", "11.2", "11.4", "11.8", "4.4", "4.18", "5.5", "4.19", "11.5", "7.5",
            "4.5", "4.6", "4.7", "4.8", "4.9", "4.10", "4.11", "4.44", "4.2", "4.70",
            "5.1", "5.3", "6.10", "6.40", "12.96", "2.4.1", "6.9",
            // --- newly added field rules ---
            "0",      // Message Type Indicator, e.g. 1800
            "1.3",    // Trace / audit number
            "2.2",    // MAC address
            "2.3",    // Terminal/host IP (allows 00.00.00.00 placeholder)
            "6.8",    // Country code, e.g. US
            "11.3",   // Numeric flag
            "11.6",   // Session flag, e.g. 0_0
            "11.9",   // Numeric flag
            "12.72",  // Numeric flag
            "12.214"  // Numeric flag
    );

    // Fields whose length is allowed to legitimately differ between the two
    // sides (e.g. amount formatting) — a length difference alone should not
    // be reported as a mismatch, but the *value* is still validated normally.
    private static final Set<String> LENGTH_IGNORING_FIELDS = Set.of(
            "4.5", "4.6", "4.7", "4.8", "4.9", "4.10", "4.11", "4.64");

    // ---- Precompiled patterns (regex compilation is expensive; String.matches()
    // recompiles on every call, which matters at per-field, per-transaction volume) ----
    private static final Pattern UUID_PATTERN =
            Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");
    private static final Pattern FIELD_121_PATTERN = Pattern.compile(
            "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}-\\d{10}$");
    // NOTE: kept for the "IP" ClassType hint in getPattern(); actual IP validation
    // now goes through isValidIPv4(), which correctly accepts values with leading
    // zero octets like "00.00.00.00" that this strict regex rejects.
    private static final Pattern IPV4_PATTERN =
            Pattern.compile("^((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)\\.){3}(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)$");
    private static final Pattern SIX_DIGIT_PATTERN = Pattern.compile("^\\d{6}$");
    private static final Pattern THIRTEEN_DIGIT_PATTERN = Pattern.compile("^\\d{13}$");
    private static final Pattern AMOUNT_PATTERN = Pattern.compile("^\\d+\\.\\d{2}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+\\d{10,15}$");
    private static final Pattern CARD_EXPIRY_PATTERN = Pattern.compile("^\\d{4}$");
    private static final Pattern FIELD_444_PATTERN = Pattern.compile("^3\\d{31}$");
    private static final Pattern INVOICE_REFERENCE_PATTERN = Pattern.compile("^[A-Z]\\d{9}$");
    private static final Pattern REFERENCE_TOKEN_PATTERN = Pattern.compile("^Token-\\d+$");
    private static final Pattern FIELD_1296_PATTERN = Pattern.compile("^W\\d{9}$");
    // NOTE: matches the literal text "%1F"/"%1D", not the ASCII unit/group
    // separator control chars (0x1F / 0x1D). Left unchanged from the original —
    // confirm against a real payload before "fixing" this.
    private static final Pattern ITEM_DETAIL_LIST_PATTERN = Pattern.compile("^\\d+%1F\\d+%1F.*%1D$");
    private static final Pattern ZIP_PATTERN = Pattern.compile("^[0-9A-Za-z]+(?:-[0-9A-Za-z]+)?$");
    private static final Pattern NUMERIC_PATTERN = Pattern.compile("^[0-9]+$");
    private static final Pattern ALPHA_PATTERN = Pattern.compile("^[a-zA-Z]+$");
    private static final Pattern ALPHANUMERIC_PATTERN = Pattern.compile("^[a-zA-Z0-9]+$");
    private static final DateTimeFormatter DATE_TIME_11_4_FORMAT = DateTimeFormatter.ofPattern("MMddyyyyHHmmss");

    // --- newly added patterns ---
    private static final Pattern MTI_PATTERN = Pattern.compile("^\\d{4}$");
    private static final Pattern MAC_ADDRESS_PATTERN = Pattern.compile("^([0-9A-Fa-f]{2}:){5}[0-9A-Fa-f]{2}$");
    private static final Pattern COUNTRY_CODE_PATTERN = Pattern.compile("^[A-Z]{2}$");
    private static final Pattern SESSION_FLAG_PATTERN = Pattern.compile("^\\d+_\\d+$");

    private final IsoFieldDefinitionLoader definitionLoader;

    public JsonComparator(IsoFieldDefinitionLoader definitionLoader) {
        this.definitionLoader = definitionLoader;
    }

    /** Why a field did or didn't match, independent of its display message. */
    private enum ReasonCode {
        MATCHED, MISSING_FIELD, TYPE_MISMATCH, LENGTH_MISMATCH, VALUE_MISMATCH
    }

    private static final class ReasonResult {
        final ReasonCode code;
        final String message;

        ReasonResult(ReasonCode code, String message) {
            this.code = code;
            this.message = message;
        }
    }

    public ComparisonJsonResult compare(String declinedJson, String approvedJson) throws Exception {
        if (declinedJson == null || approvedJson == null) {
            throw new IllegalArgumentException("declinedJson and approvedJson must not be null");
        }

        long startTime = System.currentTimeMillis();

        JsonNode declined;
        JsonNode approved;
        try {
            declined = MAPPER.readTree(sanitizeJson(declinedJson));
            approved = MAPPER.readTree(sanitizeJson(approvedJson));
        } catch (Exception e) {
            log.error("Invalid JSON payload: {}", e.getMessage());
            throw new IllegalArgumentException("Unable to parse JSON payload", e);
        }

        Set<String> fields = new TreeSet<>();
        declined.fieldNames().forEachRemaining(fields::add);
        approved.fieldNames().forEachRemaining(fields::add);

        List<IssueDetail> matchIssue = new ArrayList<>();
        List<IssueDetail> missMatchIssue = new ArrayList<>();
        List<IssueDetail> skippedIssue = new ArrayList<>();
        List<IssueDetail> validationIssue = new ArrayList<>();

        for (String field : fields) {
            if (SKIPPED_VERSION_FIELDS.contains(field)) {
                skippedIssue.add(buildSkippedIssue(field, declined, approved));
                continue;
            }

            String declinedValue = declined.has(field) ? declined.get(field).asText() : MISSING;
            String approvedValue = approved.has(field) ? approved.get(field).asText() : MISSING;

            IsoFieldDefinition definition = definitionLoader.getField(field);
            boolean valuesMatch = Objects.equals(declinedValue, approvedValue);
            boolean lengthIgnored = LENGTH_IGNORING_FIELDS.contains(field);

            ReasonResult reason = determineReason(declinedValue, approvedValue, valuesMatch, lengthIgnored);
            IssueDetail issue = buildIssue(field, declinedValue, approvedValue, definition, reason);

            classifyIssue(field, declinedValue, approvedValue, reason, issue,
                    matchIssue, missMatchIssue, validationIssue);
        }

        ComparisonJsonResult result = new ComparisonJsonResult();
        result.setValidationIssue(validationIssue);
     //   result.setMatchIssue(matchIssue);
        result.setMissMatchIssue(missMatchIssue);
     ///   result.setSkippedIssue(skippedIssue);

        log.info("JSON comparison completed in {} ms. Matched={}, Mismatch={}, Skipped={}, Validation={}",
                System.currentTimeMillis() - startTime,
                matchIssue.size(), missMatchIssue.size(), skippedIssue.size(), validationIssue.size());

        return result;
    }

    private void classifyIssue(String field, String declinedValue, String approvedValue, ReasonResult reason,
                                IssueDetail issue, List<IssueDetail> matchIssue, List<IssueDetail> missMatchIssue,
                                List<IssueDetail> validationIssue) {
        switch (reason.code) {
            case MATCHED:
                matchIssue.add(issue);
                break;

            case VALUE_MISMATCH:
                if (VALIDATE_PATTERN_FIELDS.contains(field)) {
                    boolean patternMatch = isPatternMatched(field, declinedValue, approvedValue);
                    issue.setPatternMatch(patternMatch ? MATCHED : MISMATCH );
                    // Overwrite the generic "Value mismatch" reason with a specific,
                    // per-side explanation of what actually failed validation.
                    issue.setReason(describePatternValidity(field, declinedValue, approvedValue, patternMatch));
                    (patternMatch ? missMatchIssue : validationIssue).add(issue);
                } else {
                    validationIssue.add(issue);
                }
                break;

            case MISSING_FIELD:
            case TYPE_MISMATCH:
            case LENGTH_MISMATCH:
            default:
                issue.setPatternMatch(MISMATCH);
                validationIssue.add(issue);
                log.info("Field={} | Result=VALIDATION ISSUE | Reason={}", field, reason.message);
                break;
        }
    }

    private ReasonResult determineReason(String declinedValue, String approvedValue, boolean valuesMatch,
                                          boolean lengthIgnored) {
        if (valuesMatch) {
            return new ReasonResult(ReasonCode.MATCHED, "Expected Value");
        }
        if (MISSING.equals(declinedValue) || MISSING.equals(approvedValue)) {
            return new ReasonResult(ReasonCode.MISSING_FIELD, "Field missing in one transaction");
        }

        String declinedType = getType(declinedValue);
        String approvedType = getType(approvedValue);
        if (!declinedType.equals(approvedType)) {
            return new ReasonResult(ReasonCode.TYPE_MISMATCH,
                    "Data type mismatch (" + declinedType + " vs " + approvedType + ")");
        }

        if (!lengthIgnored && declinedValue.length() != approvedValue.length()) {
            return new ReasonResult(ReasonCode.LENGTH_MISMATCH,
                    "Length mismatch (" + declinedValue.length() + " vs " + approvedValue.length() + ")");
        }

        return new ReasonResult(ReasonCode.VALUE_MISMATCH, "Value mismatch");
    }

    private IssueDetail buildSkippedIssue(String field, JsonNode declined, JsonNode approved) {
        IssueDetail issue = new IssueDetail();
        issue.setField(field);
        issue.setFieldName("VERSION FIELD (validation skipped)");
        issue.setDeclinedValue(declined.has(field) ? declined.get(field).asText() : MISSING);
        issue.setApprovedValue(approved.has(field) ? approved.get(field).asText() : MISSING);
        issue.setComparisonResult(SKIPPED);
        return issue;
    }

    private IssueDetail buildIssue(String field, String declinedValue, String approvedValue,
                                    IsoFieldDefinition definition, ReasonResult reason) {
        IssueDetail issue = new IssueDetail();
        issue.setField(field);
        issue.setFieldName(definition != null ? definition.getName() : "UNKNOWN FIELD");
        issue.setDeclinedValue(declinedValue);
        issue.setApprovedValue(approvedValue);
        issue.setComparisonResult(reason.code == ReasonCode.MATCHED ? MATCHED : MISMATCH);
        issue.setExpectedMinLength(definition != null ? String.valueOf(definition.getMinLength()) : "N/A");
        issue.setExpectedMaxLength(definition != null ? String.valueOf(definition.getMaxLength()) : "N/A");
        issue.setExpectedPattern(definition != null ? getPattern(definition.getClassType()) : "N/A");
        issue.setDeclinedLength(MISSING.equals(declinedValue) ? "0" : String.valueOf(declinedValue.length()));
        issue.setApprovedLength(MISSING.equals(approvedValue) ? "0" : String.valueOf(approvedValue.length()));
        issue.setReason(reason.message);
        return issue;
    }

    /**
     * Per-field, per-value validity check.
     *
     * @return {@code Boolean.TRUE}/{@code FALSE} if the field has a dedicated rule,
     *         or {@code null} if no dedicated rule exists (caller should fall back
     *         to the generic structural comparison).
     */
    private Boolean isValueValidForField(String field, String value) {
        if (value == null) {
            return Boolean.FALSE;
        }
        switch (field) {
            case "0":
                return matches(MTI_PATTERN, value);
            case "1.1":
            case "1.2":
            case "1.3":
            case "11.3":
            case "11.9":
            case "12.72":
            case "12.214":
                return matches(NUMERIC_PATTERN, value);
            case "2.2":
                return matches(MAC_ADDRESS_PATTERN, value);
            case "2.3":
            case "11.2":
            case "5.5":
            case "2.4.1":
                return isValidIPv4(value);
            case "6.8":
                return matches(COUNTRY_CODE_PATTERN, value);
            case "11.6":
                return matches(SESSION_FLAG_PATTERN, value);
            case "11.1":
                return matches(UUID_PATTERN, value);
            case "4.18":
                return isValidYYYYMMDD(value);
            case "4.19":
            case "4.2":
                return matches(SIX_DIGIT_PATTERN, value);
            case "12.1":
                return matches(FIELD_121_PATTERN, value);
            case "11.5":
                return matches(THIRTEEN_DIGIT_PATTERN, value);
            case "7.5":
                return matches(ITEM_DETAIL_LIST_PATTERN, value);
            case "4.5":
            case "4.6":
            case "4.7":
            case "4.8":
            case "4.9":
            case "4.10":
            case "4.11":
            case "4.64":
                return isValidAmount(value);
            case "6.10":
            case "6.40":
                return matches(PHONE_PATTERN, value);
            case "4.70":
                return isValidCardExpiry(value);
            case "4.44":
                return matches(FIELD_444_PATTERN, value);
            case "5.1":
                return matches(INVOICE_REFERENCE_PATTERN, value);
            case "5.3":
                return isValidReferenceNumber(value);
            case "11.4":
                return isValidDateTime(value);
            case "12.96":
                return matches(FIELD_1296_PATTERN, value);
            case "6.9":
                    return isValidZipCode(value);
            default:
                return null; // no dedicated rule for this field
        }
    }
    
    private boolean isValidZipCode(String value) {
        return value != null && ZIP_PATTERN.matcher(value).matches();
    }

    private boolean isPatternMatched(String field, String declinedValue, String approvedValue) {
        if (declinedValue == null || approvedValue == null) {
            return false;
        }

        Boolean declinedValid = isValueValidForField(field, declinedValue);
        Boolean approvedValid = isValueValidForField(field, approvedValue);

        if (declinedValid == null || approvedValid == null) {
            // No dedicated rule (e.g. field "11.8") — fall back to structural comparison.
            String approvedType = getType(approvedValue);
            String declinedType = getType(declinedValue);
            return approvedType.equals(declinedType) && approvedValue.length() == declinedValue.length();
        }

        return declinedValid && approvedValid;
    }

    /**
     * Builds a specific, human-readable explanation of a VALUE_MISMATCH for a
     * pattern-validated field, calling out which side (declined/approved/both)
     * actually failed the expected format — instead of a generic "Value mismatch".
     */
    private String describePatternValidity(String field, String declinedValue, String approvedValue,
                                            boolean patternMatch) {
        Boolean declinedValid = isValueValidForField(field, declinedValue);
        Boolean approvedValid = isValueValidForField(field, approvedValue);

        if (declinedValid == null || approvedValid == null) {
            return patternMatch
                    ? "Values differ but share the same data type and length for field " + field
                    : "Values differ in data type or length for field " + field;
        }
        if (declinedValid && approvedValid) {
            return "Values differ but both conform to the expected format for field " + field
                    + " (treated as an acceptable dynamic mismatch)";
        }
        if (!declinedValid && !approvedValid) {
            return "Both declined and approved values fail the expected format for field " + field;
        }
        return !declinedValid
                ? "Declined value does not match the expected format for field " + field
                : "Approved value does not match the expected format for field " + field;
    }

    private boolean isValidReferenceNumber(String value) {
        return value != null && (matches(INVOICE_REFERENCE_PATTERN, value) || matches(REFERENCE_TOKEN_PATTERN, value));
    }

    private boolean isValidCardExpiry(String value) {
        if (value == null || !matches(CARD_EXPIRY_PATTERN, value)) {
            return false;
        }
        int month = Integer.parseInt(value.substring(0, 2));
        return month >= 1 && month <= 12;
    }

    private boolean isValidDateTime(String value) {
        try {
            LocalDateTime.parse(value, DATE_TIME_11_4_FORMAT);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isValidAmount(String value) {
        return value != null && !value.trim().isEmpty() && matches(AMOUNT_PATTERN, value);
    }

    private boolean isValidYYYYMMDD(String value) {
        try {
            LocalDate.parse(value, DateTimeFormatter.BASIC_ISO_DATE);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Numeric-range IPv4 validation. Deliberately NOT regex-only: the previous
     * IPV4_PATTERN regex (kept above for getPattern()'s "IP" hint) rejects
     * legitimate values with a leading-zero octet such as "00.00.00.00", which
     * shows up as a real placeholder value in fields like 2.3/11.2/5.5.
     */
    private boolean isValidIPv4(String value) {
        if (value == null) {
            return false;
        }
        String[] octets = value.split("\\.", -1);
        if (octets.length != 4) {
            return false;
        }
        for (String octet : octets) {
            if (octet.isEmpty() || octet.length() > 3 || !matches(NUMERIC_PATTERN, octet)) {
                return false;
            }
            int num = Integer.parseInt(octet);
            if (num < 0 || num > 255) {
                return false;
            }
        }
        return true;
    }

    private static boolean matches(Pattern pattern, String value) {
        return value != null && pattern.matcher(value).matches();
    }

    private static String sanitizeJson(String json) {
        // Strips trailing commas before a closing brace/bracket (lenient JSON).
        return json.replaceAll(",\\s*([}\\]])", "$1");
    }

    private String getPattern(String type) {
        if (type == null) {
            return "N/A";
        }
        switch (type) {
            case "NUMERIC":
                return "\\d+";
            case "ALPHA":
                return "[A-Za-z ]+";
            case "ALPHA_NUMERIC":
                return "[A-Za-z0-9._\\- ]+";
            case "IP":
                return IPV4_PATTERN.pattern();
            default:
                return "N/A";
        }
    }

    private static String getType(String value) {
        if (value == null || MISSING.equals(value)) {
            return "UNKNOWN";
        }
        if (matches(NUMERIC_PATTERN, value)) {
            return "NUMERIC";
        }
        if (matches(ALPHA_PATTERN, value)) {
            return "ALPHA";
        }
        if (matches(ALPHANUMERIC_PATTERN, value)) {
            return "ALPHANUMERIC";
        }
        return "SPECIAL";
    }
}