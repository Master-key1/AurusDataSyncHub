package com.auruspay.fiserv.comparator;

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

import com.auruspay.fiserv.comparator.config.IsoFieldDefinitionLoader;
import com.auruspay.fiserv.comparator.model.CompareCctResult;
import com.auruspay.fiserv.comparator.model.IsoFieldDefinition;
import com.auruspay.fiserv.comparator.model.IssueDetail;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Compares declined and approved transaction JSON payloads.
 *
 * Result categories:
 * 1. MATCHED
 * 2. MISMATCH
 * 3. SKIPPED
 * 4. VALIDATION ISSUE
 *
 * Thread-safe:
 * - ObjectMapper is initialized once and not modified after initialization.
 * - No mutable comparison state is maintained at instance level.
 * - Each comparison creates its own result collections.
 */
@Service
public class JsonComparator {

    private static final Logger log =
            LoggerFactory.getLogger(JsonComparator.class);

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final String MATCHED = "MATCHED";
    private static final String MISMATCH = "MISMATCH";
    private static final String SKIPPED = "SKIPPED";
    private static final String MISSING = "MISSING";

    // ---------------------------------------------------------
    // FIELDS TO SKIP
    // ---------------------------------------------------------

    private static final Set<String> SKIPPED_VERSION_FIELDS = Set.of(
            "6.1", "6.3", "6.4", "12.54", "6.11", "6.42",
            "7.4", "4.67", "7.3", "2.4.58", "12.1",
            "6.7", "6.5", "6.6", "6.30", "6.32",
            "6.99.11", "20.15", "2.4.3", "7.5",
            "11.7", "12.61", "12.94", "12.86",
            "11.8", "1.141"
    );

    // ---------------------------------------------------------
    // DYNAMIC / PATTERN VALIDATION FIELDS
    // ---------------------------------------------------------

    private static final Set<String> VALID_PATTERN_FIELDS = Set.of(
            "1.1", "1.2", "1.3"
    );

    private static final Set<String> VALIDATE_PATTERN_FIELDS = Set.of(
            "11.1", "11.2", "11.4", "11.8",
            "4.4", "4.18", "5.5", "4.19",
            "11.5", "7.5", "4.5", "4.6",
            "4.7", "4.8", "4.9", "4.10",
            "4.11", "4.44", "4.2", "4.70",
            "5.1", "5.3", "6.10", "6.40",
            "12.96", "2.4.1", "6.9",

            "0",
            "1.3",
            "2.2",
            "2.3",
            "6.8",
            "11.3",
            "11.6",
            "11.9",
            "12.72",
            "12.214"
    );

    // ---------------------------------------------------------
    // FIELDS WHERE LENGTH DIFFERENCE IS ACCEPTABLE
    // ---------------------------------------------------------

    private static final Set<String> LENGTH_IGNORING_FIELDS = Set.of(
            "4.5", "4.6", "4.7", "4.8",
            "4.9", "4.10", "4.11", "4.64"
    );

    // ---------------------------------------------------------
    // REGEX PATTERNS
    // ---------------------------------------------------------

    private static final Pattern UUID_PATTERN =
            Pattern.compile(
                    "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"
            );

    private static final Pattern FIELD_121_PATTERN =
            Pattern.compile(
                    "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}-\\d{10}$"
            );

    private static final Pattern IPV4_PATTERN =
            Pattern.compile(
                    "^((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)\\.){3}(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)$"
            );

    private static final Pattern SIX_DIGIT_PATTERN =
            Pattern.compile("^\\d{6}$");

    private static final Pattern THIRTEEN_DIGIT_PATTERN =
            Pattern.compile("^\\d{13}$");

    private static final Pattern AMOUNT_PATTERN =
            Pattern.compile("^\\d+\\.\\d{2}$");

    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^\\+\\d{10,15}$");

    private static final Pattern CARD_EXPIRY_PATTERN =
            Pattern.compile("^\\d{4}$");

    private static final Pattern FIELD_444_PATTERN =
            Pattern.compile("^3\\d{31}$");

    private static final Pattern INVOICE_REFERENCE_PATTERN =
            Pattern.compile("^[A-Z]\\d{9}$");

    private static final Pattern REFERENCE_TOKEN_PATTERN =
            Pattern.compile("^Token-\\d+$");

    private static final Pattern FIELD_1296_PATTERN =
            Pattern.compile("^W\\d{9}$");

    private static final Pattern ITEM_DETAIL_LIST_PATTERN =
            Pattern.compile("^\\d+%1F\\d+%1F.*%1D$");

    private static final Pattern ZIP_PATTERN =
            Pattern.compile("^[0-9A-Za-z]+(?:-[0-9A-Za-z]+)?$");

    private static final Pattern NUMERIC_PATTERN =
            Pattern.compile("^[0-9]+$");

    private static final Pattern ALPHA_PATTERN =
            Pattern.compile("^[a-zA-Z]+$");

    private static final Pattern ALPHANUMERIC_PATTERN =
            Pattern.compile("^[a-zA-Z0-9]+$");

    private static final DateTimeFormatter DATE_TIME_11_4_FORMAT =
            DateTimeFormatter.ofPattern("MMddyyyyHHmmss");

    // Newly added validation patterns

    private static final Pattern MTI_PATTERN =
            Pattern.compile("^\\d{4}$");

    private static final Pattern MAC_ADDRESS_PATTERN =
            Pattern.compile("^([0-9A-Fa-f]{2}:){5}[0-9A-Fa-f]{2}$");

    private static final Pattern COUNTRY_CODE_PATTERN =
            Pattern.compile("^[A-Z]{2}$");

    private static final Pattern SESSION_FLAG_PATTERN =
            Pattern.compile("^\\d+_\\d+$");

    // ---------------------------------------------------------
    // DEPENDENCIES
    // ---------------------------------------------------------

    private final IsoFieldDefinitionLoader definitionLoader;

    public JsonComparator(IsoFieldDefinitionLoader definitionLoader) {
        this.definitionLoader = definitionLoader;
    }

    // ---------------------------------------------------------
    // REASON CODES
    // ---------------------------------------------------------

    private enum ReasonCode {

        MATCHED,
        MISSING_FIELD,
        TYPE_MISMATCH,
        LENGTH_MISMATCH,
        VALUE_MISMATCH
    }

    private enum ResultCategory {

        MATCHED,
        MISMATCH,
        SKIPPED,
        VALIDATION
    }

    private static final class ReasonResult {

        private final ReasonCode code;
        private final String message;

        private ReasonResult(ReasonCode code, String message) {
            this.code = code;
            this.message = message;
        }
    }

    // ---------------------------------------------------------
    // MAIN COMPARISON METHOD
    // ---------------------------------------------------------

    public CompareCctResult compare(
            String declinedJson,
            String approvedJson) throws Exception {

        if (declinedJson == null || approvedJson == null) {
            throw new IllegalArgumentException(
                    "declinedJson and approvedJson must not be null"
            );
        }

        long startTime = System.currentTimeMillis();

        log.info("==================================================");
        log.info("JSON COMPARISON STARTED");
        log.info("==================================================");

        JsonNode declined;
        JsonNode approved;

        // -----------------------------------------------------
        // JSON PARSING
        // -----------------------------------------------------

        try {

            declined = MAPPER.readTree(declinedJson);
            approved = MAPPER.readTree(approvedJson);

            if (declined == null || approved == null
                    || !declined.isObject() || !approved.isObject()) {

                throw new IllegalArgumentException(
                        "Both JSON payloads must contain JSON objects"
                );
            }

            log.info("Declined JSON parsed successfully");
            log.info("Approved JSON parsed successfully");

        } catch (Exception e) {

            log.error(
                    "JSON parsing failed. Error={}",
                    e.getMessage(),
                    e
            );

            throw new IllegalArgumentException(
                    "Unable to parse JSON payload",
                    e
            );
        }

        // -----------------------------------------------------
        // COLLECT ALL FIELDS
        // -----------------------------------------------------

        Set<String> fields = new TreeSet<>();

        declined.fieldNames().forEachRemaining(fields::add);
        approved.fieldNames().forEachRemaining(fields::add);

        log.info("Total unique fields identified: {}", fields.size());

        // -----------------------------------------------------
        // RESULT COLLECTIONS
        // -----------------------------------------------------

        List<IssueDetail> matchIssue = new ArrayList<>();

        List<IssueDetail> missMatchIssue = new ArrayList<>();

        List<IssueDetail> skippedIssue = new ArrayList<>();

        List<IssueDetail> validationIssue = new ArrayList<>();

        // -----------------------------------------------------
        // FIELD-BY-FIELD COMPARISON
        // -----------------------------------------------------

        for (String field : fields) {

            log.info("--------------------------------------------------");
            log.info("Processing ISO Field: {}", field);

            // -------------------------------------------------
            // SKIPPED FIELD CHECK
            // -------------------------------------------------

            if (SKIPPED_VERSION_FIELDS.contains(field)) {

                IssueDetail issue =
                        buildSkippedIssue(field, declined, approved);

                skippedIssue.add(issue);

                log.info(
                        "Field={} | Category={} | Reason=Version field excluded from validation",
                        field,
                        SKIPPED
                );

                continue;
            }

            // -------------------------------------------------
            // EXTRACT VALUES
            // -------------------------------------------------

            boolean declinedExists = declined.has(field);
            boolean approvedExists = approved.has(field);

            String declinedValue = declinedExists
                    ? extractValue(declined.get(field))
                    : MISSING;

            String approvedValue = approvedExists
                    ? extractValue(approved.get(field))
                    : MISSING;

            // -------------------------------------------------
            // FIELD DEFINITION
            // -------------------------------------------------

            IsoFieldDefinition definition =
                    definitionLoader.getField(field);

            String fieldName = definition != null
                    ? definition.getName()
                    : "UNKNOWN FIELD";

            // -------------------------------------------------
            // COMPARISON DETAILS
            // -------------------------------------------------

            boolean valuesMatch =
                    Objects.equals(declinedValue, approvedValue);

            boolean lengthIgnored =
                    LENGTH_IGNORING_FIELDS.contains(field);

            String declinedType = getType(declinedValue);
            String approvedType = getType(approvedValue);

            log.info(
                    "FIELD DETAILS | Field={} | Name={} | DeclinedValue={} | ApprovedValue={}",
                    field,
                    fieldName,
                    safeLogValue(declinedValue),
                    safeLogValue(approvedValue)
            );

            log.info(
                    "FIELD TYPES | Field={} | DeclinedType={} | ApprovedType={}",
                    field,
                    declinedType,
                    approvedType
            );

            log.info(
                    "FIELD LENGTHS | Field={} | DeclinedLength={} | ApprovedLength={} | LengthIgnored={}",
                    field,
                    getLength(declinedValue),
                    getLength(approvedValue),
                    lengthIgnored
            );

            // -------------------------------------------------
            // DETERMINE REASON
            // -------------------------------------------------

            ReasonResult reason = determineReason(
                    declinedValue,
                    approvedValue,
                    valuesMatch,
                    lengthIgnored
            );

            log.info(
                    "INITIAL COMPARISON | Field={} | Result={} | Reason={}",
                    field,
                    reason.code,
                    reason.message
            );

            // -------------------------------------------------
            // BUILD ISSUE
            // -------------------------------------------------

            IssueDetail issue = buildIssue(
                    field,
                    declinedValue,
                    approvedValue,
                    definition,
                    reason
            );

            // -------------------------------------------------
            // CLASSIFY RESULT
            // -------------------------------------------------

            ResultCategory category = classifyIssue(
                    field,
                    declinedValue,
                    approvedValue,
                    reason,
                    issue,
                    matchIssue,
                    missMatchIssue,
                    validationIssue
            );

            log.info(
                    "FINAL FIELD RESULT | Field={} | Name={} | Category={} | ComparisonResult={} | Reason={}",
                    field,
                    fieldName,
                    category,
                    issue.getComparisonResult(),
                    issue.getReason()
            );
        }

        // -----------------------------------------------------
        // BUILD FINAL RESPONSE
        // -----------------------------------------------------

        CompareCctResult result = new CompareCctResult();

       // result.setMatchIssue(matchIssue);

        result.setMissMatchIssue(missMatchIssue);

      //  result.setSkippedIssue(skippedIssue);

        result.setValidationIssue(validationIssue);

        // -----------------------------------------------------
        // SUMMARY
        // -----------------------------------------------------

        long totalTime = System.currentTimeMillis() - startTime;

        int totalProcessed =
                matchIssue.size()
                + missMatchIssue.size()
                + skippedIssue.size()
                + validationIssue.size();

        log.info("==================================================");
        log.info("JSON COMPARISON COMPLETED");
        log.info("==================================================");

        log.info("Total Fields       : {}", fields.size());
        log.info("Total Processed    : {}", totalProcessed);
        log.info("MATCHED            : {}", matchIssue.size());
        log.info("MISMATCH           : {}", missMatchIssue.size());
        log.info("SKIPPED            : {}", skippedIssue.size());
        log.info("VALIDATION ISSUE   : {}", validationIssue.size());
        log.info("Processing Time    : {} ms", totalTime);

        log.info("==================================================");

        return result;
    }

    // ---------------------------------------------------------
    // CLASSIFICATION LOGIC
    // ---------------------------------------------------------

    private ResultCategory classifyIssue(
            String field,
            String declinedValue,
            String approvedValue,
            ReasonResult reason,
            IssueDetail issue,
            List<IssueDetail> matchIssue,
            List<IssueDetail> missMatchIssue,
            List<IssueDetail> validationIssue) {

        // Missing fields cannot be accepted as dynamic differences.
        if (reason.code == ReasonCode.MISSING_FIELD) {

            issue.setComparisonResult(MISMATCH);
            issue.setPatternMatch(MISMATCH);

            validationIssue.add(issue);

            log.warn(
                    "VALIDATION ISSUE | Field={} | Reason=Field missing in one transaction",
                    field
            );

            return ResultCategory.VALIDATION;
        }

        // -----------------------------------------------------
        // PATTERN VALIDATION
        // -----------------------------------------------------

        if (isPatternValidationField(field)) {

            log.info(
                    "Pattern validation started | Field={}",
                    field
            );

            Boolean declinedValid =
                    isValueValidForField(field, declinedValue);

            Boolean approvedValid =
                    isValueValidForField(field, approvedValue);

            boolean patternMatch = isPatternMatched(
                    field,
                    declinedValue,
                    approvedValue
            );

            String patternResult =
                    patternMatch ? MATCHED : MISMATCH;

            issue.setPatternMatch(patternResult);

            issue.setReason(
                    describePatternValidity(
                            field,
                            declinedValue,
                            approvedValue,
                            patternMatch
                    )
            );

            log.info(
                    "PATTERN VALIDATION | Field={} | DeclinedValid={} | ApprovedValid={} | Result={}",
                    field,
                    declinedValid,
                    approvedValid,
                    patternResult
            );

            if (patternMatch) {

                issue.setComparisonResult(MATCHED);

                matchIssue.add(issue);

                log.info(
                        "ACCEPTED DYNAMIC FIELD | Field={} | Both values satisfy the expected validation rule",
                        field
                );

                return ResultCategory.MATCHED;

            } else {

                issue.setComparisonResult(MISMATCH);

                validationIssue.add(issue);

                log.warn(
                        "PATTERN VALIDATION FAILED | Field={} | Reason={}",
                        field,
                        issue.getReason()
                );

                return ResultCategory.VALIDATION;
            }
        }

        // -----------------------------------------------------
        // EXACT MATCH
        // -----------------------------------------------------

        if (reason.code == ReasonCode.MATCHED) {

            issue.setComparisonResult(MATCHED);
            issue.setPatternMatch(MATCHED);

            matchIssue.add(issue);

            log.debug(
                    "FIELD MATCHED | Field={}",
                    field
            );

            return ResultCategory.MATCHED;
        }

        // -----------------------------------------------------
        // STRUCTURAL VALIDATION
        // -----------------------------------------------------

        if (reason.code == ReasonCode.TYPE_MISMATCH
                || reason.code == ReasonCode.LENGTH_MISMATCH) {

            issue.setComparisonResult(MISMATCH);
            issue.setPatternMatch(MISMATCH);

            validationIssue.add(issue);

            log.warn(
                    "STRUCTURAL VALIDATION ISSUE | Field={} | Reason={}",
                    field,
                    reason.message
            );

            return ResultCategory.VALIDATION;
        }

        // -----------------------------------------------------
        // VALUE MISMATCH
        // -----------------------------------------------------

        issue.setComparisonResult(MISMATCH);
        issue.setPatternMatch(MISMATCH);

        missMatchIssue.add(issue);

        log.warn(
                "VALUE MISMATCH | Field={} | Declined={} | Approved={} | Reason={}",
                field,
                safeLogValue(declinedValue),
                safeLogValue(approvedValue),
                reason.message
        );

        return ResultCategory.MISMATCH;
    }

    // ---------------------------------------------------------
    // DETERMINE COMPARISON REASON
    // ---------------------------------------------------------

    private ReasonResult determineReason(
            String declinedValue,
            String approvedValue,
            boolean valuesMatch,
            boolean lengthIgnored) {

        if (valuesMatch) {

            return new ReasonResult(
                    ReasonCode.MATCHED,
                    "Expected Value"
            );
        }

        if (MISSING.equals(declinedValue)
                || MISSING.equals(approvedValue)) {

            return new ReasonResult(
                    ReasonCode.MISSING_FIELD,
                    "Field missing in one transaction"
            );
        }

        String declinedType = getType(declinedValue);
        String approvedType = getType(approvedValue);

        if (!declinedType.equals(approvedType)) {

            return new ReasonResult(
                    ReasonCode.TYPE_MISMATCH,
                    "Data type mismatch ("
                            + declinedType
                            + " vs "
                            + approvedType
                            + ")"
            );
        }

        if (!lengthIgnored
                && declinedValue.length() != approvedValue.length()) {

            return new ReasonResult(
                    ReasonCode.LENGTH_MISMATCH,
                    "Length mismatch ("
                            + declinedValue.length()
                            + " vs "
                            + approvedValue.length()
                            + ")"
            );
        }

        return new ReasonResult(
                ReasonCode.VALUE_MISMATCH,
                "Value mismatch"
        );
    }

    // ---------------------------------------------------------
    // BUILD SKIPPED ISSUE
    // ---------------------------------------------------------

    private IssueDetail buildSkippedIssue(
            String field,
            JsonNode declined,
            JsonNode approved) {

        IssueDetail issue = new IssueDetail();

        issue.setField(field);

        issue.setFieldName(
                "VERSION FIELD (validation skipped)"
        );

        issue.setDeclinedValue(
                declined.has(field)
                        ? extractValue(declined.get(field))
                        : MISSING
        );

        issue.setApprovedValue(
                approved.has(field)
                        ? extractValue(approved.get(field))
                        : MISSING
        );

        issue.setComparisonResult(SKIPPED);

        return issue;
    }

    // ---------------------------------------------------------
    // BUILD ISSUE DETAILS
    // ---------------------------------------------------------

    private IssueDetail buildIssue(
            String field,
            String declinedValue,
            String approvedValue,
            IsoFieldDefinition definition,
            ReasonResult reason) {

        IssueDetail issue = new IssueDetail();

        issue.setField(field);

        issue.setFieldName(
                definition != null
                        ? definition.getName()
                        : "UNKNOWN FIELD"
        );

        issue.setDeclinedValue(declinedValue);

        issue.setApprovedValue(approvedValue);

        issue.setComparisonResult(
                reason.code == ReasonCode.MATCHED
                        ? MATCHED
                        : MISMATCH
        );

        issue.setExpectedMinLength(
                definition != null
                        ? String.valueOf(definition.getMinLength())
                        : "N/A"
        );

        issue.setExpectedMaxLength(
                definition != null
                        ? String.valueOf(definition.getMaxLength())
                        : "N/A"
        );

        issue.setExpectedPattern(
                definition != null
                        ? getPattern(definition.getClassType())
                        : "N/A"
        );

        issue.setDeclinedLength(
                MISSING.equals(declinedValue)
                        ? "0"
                        : String.valueOf(declinedValue.length())
        );

        issue.setApprovedLength(
                MISSING.equals(approvedValue)
                        ? "0"
                        : String.valueOf(approvedValue.length())
        );

        issue.setReason(reason.message);

        return issue;
    }

    // ---------------------------------------------------------
    // CHECK WHETHER FIELD NEEDS PATTERN VALIDATION
    // ---------------------------------------------------------

    private boolean isPatternValidationField(String field) {

        return VALID_PATTERN_FIELDS.contains(field)
                || VALIDATE_PATTERN_FIELDS.contains(field);
    }

    // ---------------------------------------------------------
    // FIELD VALUE VALIDATION
    // ---------------------------------------------------------

    /**
     * Returns:
     * TRUE  = Value satisfies the dedicated format.
     * FALSE = Value violates the dedicated format.
     * NULL  = No dedicated format is configured.
     */
    private Boolean isValueValidForField(
            String field,
            String value) {

        if (value == null || MISSING.equals(value)) {
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
                return null;
        }
    }

    // ---------------------------------------------------------
    // PATTERN MATCHING
    // ---------------------------------------------------------

    private boolean isPatternMatched(
            String field,
            String declinedValue,
            String approvedValue) {

        if (declinedValue == null || approvedValue == null
                || MISSING.equals(declinedValue)
                || MISSING.equals(approvedValue)) {

            return false;
        }

        Boolean declinedValid =
                isValueValidForField(field, declinedValue);

        Boolean approvedValid =
                isValueValidForField(field, approvedValue);

        // No dedicated rule: use structural comparison.
        if (declinedValid == null || approvedValid == null) {

            String declinedType = getType(declinedValue);
            String approvedType = getType(approvedValue);

            boolean sameType =
                    declinedType.equals(approvedType);

            boolean sameLength =
                    declinedValue.length() == approvedValue.length();

            return sameType && sameLength;
        }

        return declinedValid && approvedValid;
    }

    // ---------------------------------------------------------
    // PATTERN VALIDATION REASON
    // ---------------------------------------------------------

    private String describePatternValidity(
            String field,
            String declinedValue,
            String approvedValue,
            boolean patternMatch) {

        Boolean declinedValid =
                isValueValidForField(field, declinedValue);

        Boolean approvedValid =
                isValueValidForField(field, approvedValue);

        // Structural fallback for fields without dedicated rules.
        if (declinedValid == null || approvedValid == null) {

            return patternMatch
                    ? "Values differ but share the same data type and length for field "
                            + field
                    : "Values differ in data type or length for field "
                            + field;
        }

        if (declinedValid && approvedValid) {

            return "Values differ but both conform to the expected format for field "
                    + field
                    + " (accepted dynamic difference)";
        }

        if (!declinedValid && !approvedValid) {

            return "Both declined and approved values fail the expected format for field "
                    + field;
        }

        return !declinedValid
                ? "Declined value does not match the expected format for field "
                        + field
                : "Approved value does not match the expected format for field "
                        + field;
    }

    // ---------------------------------------------------------
    // ZIP CODE VALIDATION
    // ---------------------------------------------------------

    private boolean isValidZipCode(String value) {

        return value != null
                && matches(ZIP_PATTERN, value);
    }

    // ---------------------------------------------------------
    // REFERENCE NUMBER VALIDATION
    // ---------------------------------------------------------

    private boolean isValidReferenceNumber(String value) {

        return value != null
                && (
                        matches(INVOICE_REFERENCE_PATTERN, value)
                        || matches(REFERENCE_TOKEN_PATTERN, value)
                );
    }

    // ---------------------------------------------------------
    // CARD EXPIRY VALIDATION
    // ---------------------------------------------------------

    private boolean isValidCardExpiry(String value) {

        if (value == null
                || !matches(CARD_EXPIRY_PATTERN, value)) {

            return false;
        }

        int month = Integer.parseInt(value.substring(0, 2));

        return month >= 1 && month <= 12;
    }

    // ---------------------------------------------------------
    // DATE TIME VALIDATION
    // ---------------------------------------------------------

    private boolean isValidDateTime(String value) {

        try {

            LocalDateTime.parse(
                    value,
                    DATE_TIME_11_4_FORMAT
            );

            return true;

        } catch (Exception e) {

            return false;
        }
    }

    // ---------------------------------------------------------
    // AMOUNT VALIDATION
    // ---------------------------------------------------------

    private boolean isValidAmount(String value) {

        return value != null
                && !value.trim().isEmpty()
                && matches(AMOUNT_PATTERN, value);
    }

    // ---------------------------------------------------------
    // YYYYMMDD VALIDATION
    // ---------------------------------------------------------

    private boolean isValidYYYYMMDD(String value) {

        try {

            LocalDate.parse(
                    value,
                    DateTimeFormatter.BASIC_ISO_DATE
            );

            return true;

        } catch (Exception e) {

            return false;
        }
    }

    // ---------------------------------------------------------
    // IPV4 VALIDATION
    // ---------------------------------------------------------

    private boolean isValidIPv4(String value) {

        if (value == null) {
            return false;
        }

        String[] octets = value.split("\\.", -1);

        if (octets.length != 4) {
            return false;
        }

        for (String octet : octets) {

            if (octet.isEmpty()
                    || octet.length() > 3
                    || !matches(NUMERIC_PATTERN, octet)) {

                return false;
            }

            int num = Integer.parseInt(octet);

            if (num < 0 || num > 255) {
                return false;
            }
        }

        return true;
    }

    // ---------------------------------------------------------
    // GENERIC REGEX MATCHING
    // ---------------------------------------------------------

    private static boolean matches(
            Pattern pattern,
            String value) {

        return value != null
                && pattern.matcher(value).matches();
    }

    // ---------------------------------------------------------
    // JSON VALUE EXTRACTION
    // ---------------------------------------------------------

    /**
     * Preserves the actual JSON representation.
     *
     * Strings  -> text
     * Numbers  -> numeric text
     * Boolean  -> true / false
     * Objects  -> JSON representation
     * Arrays   -> JSON representation
     * Null     -> null
     */
    private static String extractValue(JsonNode node) {

        if (node == null) {
            return MISSING;
        }

        if (node.isNull()) {
            return "null";
        }

        if (node.isTextual()) {
            return node.textValue();
        }

        return node.toString();
    }

    // ---------------------------------------------------------
    // VALUE TYPE IDENTIFICATION
    // ---------------------------------------------------------

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

    // ---------------------------------------------------------
    // FIELD PATTERN DESCRIPTION
    // ---------------------------------------------------------

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

    // ---------------------------------------------------------
    // LENGTH HELPER
    // ---------------------------------------------------------

    private static int getLength(String value) {

        if (value == null || MISSING.equals(value)) {
            return 0;
        }

        return value.length();
    }

    // ---------------------------------------------------------
    // SAFE VALUE LOGGING
    // ---------------------------------------------------------

    /**
     * Avoids writing complete PAN-like numeric values and long
     * hexadecimal values into application logs.
     *
     * The actual values remain unchanged in the comparison result.
     */
    private static String safeLogValue(String value) {

        if (value == null) {
            return "null";
        }

        if (MISSING.equals(value)) {
            return MISSING;
        }

        if (value.isEmpty()) {
            return "[EMPTY]";
        }

        if (matches(NUMERIC_PATTERN, value)) {

            if (value.length() <= 6) {
                return "[REDACTED:" + value.length() + "]";
            }

            return value.substring(0, 2)
                    + "****"
                    + value.substring(value.length() - 2);
        }

        // Mask long hexadecimal strings, which may contain
        // sensitive cryptographic or transaction data.
        if (value.length() >= 16
                && value.matches("(?i)^[0-9a-f]+$")) {

            return value.substring(0, 4)
                    + "****"
                    + value.substring(value.length() - 4);
        }

        if (value.length() > 80) {

            return value.substring(0, 40)
                    + "...[TRUNCATED]";
        }

        return value;
    }
}