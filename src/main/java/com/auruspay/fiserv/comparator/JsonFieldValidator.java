
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
import com.auruspay.fiserv.comparator.model.ValidateResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class JsonFieldValidator {

    private static final Logger log =
            LoggerFactory.getLogger(JsonFieldValidator.class);

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final IsoFieldDefinitionLoader definitionLoader;

    private static final String MISSING = "NULL";
    private static final String MATCH = "MATCH";
    private static final String MISMATCH = "MISMATCH";
    private static final String VALID = "VALID";
    private static final String INVALID = "INVALID";
    private static final String LENGTH_VALID = "VALID";
    private static final String LENGTH_INVALID = "INVALID";

    private static final String SKIPPED = "SKIPPED";

    private static final String ALL_PASSED_SUMMARY =
            "Validation successful. All checks passed.";

    private static final String ENVIRONMENT_VARIATION_SUMMARY =
            "This value might vary depending on the environment.";

    /*
     * Fields that should not be validated.
     */
    private static final Set<String> SKIPPED_VERSION_FIELDS = Set.of(
            "0", "1.1", "1.2", "1.3", "1.4", "1.141",
            "2.1", "2.2", "2.3", "2.4.3", "2.4.58",
            "4.13", "4.18", "4.19", "4.24", "4.38",
            "4.41", "4.46", "4.47", "4.49", "4.32",
            "4.52", "4.67", "4.68", "5.5", "5.8",
            "5.9", "5.13", "5.14", "5.15", "5.23",
            "6.1", "6.2", "6.3", "6.4", "6.5",
            "6.6", "6.7", "6.8", "6.11", "6.30",
            "6.32", "6.42", "6.99.11",
            "7.2", "7.3", "7.4", "7.5", "7.6",
            "7.7", "11.1", "11.2", "11.3", "11.4",
            "11.5", "11.6", "11.7", "11.8", "11.9",
            "12.1", "12.54", "12.61", "12.86",
            "20.15", "91.1.9", "92.6.3", "12.112",
            "12.214", "12.23", "4.149", "4.146",
            "4.141", "4.142", "3.23", "4.2",
            "3.66", "4.100", "4.101", "4.103",
            "12.128", "12.129", "12.132", "12.210",
            "12.63", "12.70", "12..74", "15.1", "4.126",
            "4.127",
            "4.128",
            "4.134",
            "4.135",
            "5.1",
            "4.64"
    );

    /*
     * Important fields requiring validation.
     */
    private static final Set<String> IMP_VERSION_FIELDS = Set.of(
            "3.1", "3.2", "3.5", "3.21",
            "4.1", "4.3", "4.15", "4.16",
            "4.17", "4.20", "4.21", "4.30",
            "4.36", "4.38", "4.39", "4.40",
            "4.63", "4.70", "4.78", "4.104",
            "4.112", "4.113", "4.141",
            "5.1", "6.9", "7.8",
            "11.3", "12.72", "12.73",
            "12.89", "14.13", "3.3",
            "3.7", "8.1", "4.32", "4.22"
    );

    /*
     * Fields where length differences are acceptable.
     */
    private static final Set<String> LENGTH_IGNORING_FIELDS = Set.of(
            "4.5", "4.6", "4.7", "4.8",
            "4.9", "4.10", "4.11",
            "4.64", "4.161", "4.163",
            "4.135", "4.134", "4.128",
            "4.127", "4.126"
    );

    /*
     * Expected value differences.
     * These fields should not be reported as value mismatches.
     * Pattern and length checks must still be performed.
     */
    private static final Set<String> MISMATCH_VALUE_FIELDS = Set.of(
            "4.5", "4.6", "4.7", "4.8",
            "4.9", "4.10", "4.11",
            "8.1", "4.38", "6.9",
            "4.64", "7.5"
    );

    /*
     * Precompiled validation patterns.
     */
    private static final Pattern UUID_PATTERN =
            Pattern.compile(
                    "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");

    private static final Pattern FIELD_121_PATTERN =
            Pattern.compile(
                    "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}-\\d{10}$");

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
            Pattern.compile("^(?:[A-Z]\\d{9}|[A-Z0-9]+-\\d{8}|\\d+)$");

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

    private static final Pattern MTI_PATTERN =
            Pattern.compile("^\\d{4}$");

    private static final Pattern MAC_ADDRESS_PATTERN =
            Pattern.compile("^([0-9A-Fa-f]{2}:){5}[0-9A-Fa-f]{2}$");

    private static final Pattern COUNTRY_CODE_PATTERN =
            Pattern.compile("^[A-Z]{2}$");

    private static final Pattern SESSION_FLAG_PATTERN =
            Pattern.compile("^\\d+_\\d+$");

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private static final Pattern PIN_BLOCK_PATTERN =
            Pattern.compile("^[0-9A-Fa-f]{16}$");

    private static final Pattern KSN_PATTERN =
            Pattern.compile("^[0-9A-Fa-f]{20}$");

    private static final DateTimeFormatter DATE_TIME_11_4_FORMAT =
            DateTimeFormatter.ofPattern("MMddyyyyHHmmss");

    /*
     * Constructor injection.
     *
     * IsoFieldDefinitionLoader must be configured as a Spring bean.
     */
    public JsonFieldValidator(
            IsoFieldDefinitionLoader definitionLoader) {

        this.definitionLoader =
                Objects.requireNonNull(
                        definitionLoader,
                        "IsoFieldDefinitionLoader must not be null");

        log.info("JsonFieldValidator initialized successfully");
    }

    // ============================================================
    // MAIN VALIDATION METHOD
    // ============================================================

    /**
     * Validates declined and approved transaction JSON.
     *
     * Returns categorized results:
     * 1. Matched fields
     * 2. Mismatched fields
     * 3. Validation issues
     */
    public CompareCctResult validation(
            String declinedJson,
            String approvedJson) throws Exception {

        long startTime = System.currentTimeMillis();

        log.info("=================================================");
        log.info("STARTING JSON FIELD VALIDATION");
        log.info("=================================================");

        List<ValidateResult> list =
                validateAll(declinedJson, approvedJson);

        List<ValidateResult> matchVal = new ArrayList<>();
        List<ValidateResult> misMatchVal = new ArrayList<>();
        List<ValidateResult> issueVal = new ArrayList<>();

        for (ValidateResult val : list) {

            if (val == null) {
                continue;
            }

            String field = val.getField();

            String valueStatus = val.getValue();
            String patternStatus = val.getPattern();
            String lengthStatus = val.getLength();

            boolean expectedMismatch =
                    MISMATCH_VALUE_FIELDS.contains(field);

            boolean patternInvalid =
                    INVALID.equals(patternStatus);

            boolean lengthInvalid =
                    LENGTH_INVALID.equals(lengthStatus);

            boolean valueMismatch =
                    MISMATCH.equals(valueStatus)
                            && !expectedMismatch;

            log.info(
                    "CLASSIFY FIELD={} | VALUE={} | PATTERN={} | LENGTH={} | ExpectedMismatch={}",
                    field,
                    valueStatus,
                    patternStatus,
                    lengthStatus,
                    expectedMismatch
            );

            /*
             * A completely valid field.
             */
            if (MATCH.equals(valueStatus)
                    && VALID.equals(patternStatus)
                    && LENGTH_VALID.equals(lengthStatus)) {

                matchVal.add(val);

                log.info(
                        "FIELD CLASSIFICATION: MATCHED | Field={}",
                        field
                );

                continue;
            }

            /*
             * Actual value mismatch.
             *
             * Expected mismatches are excluded.
             */
            if (valueMismatch) {

                misMatchVal.add(val);

                log.info(
                        "FIELD CLASSIFICATION: MISMATCH | Field={}",
                        field
                );
            }

            /*
             * Pattern and length failures.
             *
             * A field may appear in both mismatch and validation
             * issue lists if both conditions apply.
             */
            if (patternInvalid || lengthInvalid) {

                issueVal.add(val);

                log.info(
                        "FIELD CLASSIFICATION: VALIDATION ISSUE | Field={} | PatternInvalid={} | LengthInvalid={}",
                        field,
                        patternInvalid,
                        lengthInvalid
                );
            }

            if (expectedMismatch
                    && !patternInvalid
                    && !lengthInvalid) {

                log.info(
                        "EXPECTED VALUE DIFFERENCE IGNORED | Field={}",
                        field
                );
            }
        }

        /*
         * Create a new response object for every invocation.
         * Never share mutable response state between requests.
         */
        CompareCctResult response = new CompareCctResult();

     //   response.setMatchIssue(matchVal);
        response.setMissMatchIssue(misMatchVal);
        response.setValidationIssue(issueVal);

        log.info("=================================================");
        log.info("FINAL JSON VALIDATION SUMMARY");
        log.info("Matched Fields       : {}", matchVal.size());
        log.info("Mismatched Fields    : {}", misMatchVal.size());
        log.info("Validation Issues    : {}", issueVal.size());
        log.info("Total Processed      : {}", list.size());
        log.info(
                "Execution Time      : {} ms",
                System.currentTimeMillis() - startTime
        );
        log.info("=================================================");

        return response;
    }

    // ============================================================
    // VALIDATE ALL FIELDS
    // ============================================================

    /**
     * Validates every applicable field from both JSON payloads.
     *
     * Fields are collected using the union of approved and declined
     * JSON keys.
     */
    public List<ValidateResult> validateAll(
            String declinedJson,
            String approvedJson) throws Exception {

        if (declinedJson == null || approvedJson == null) {

            throw new IllegalArgumentException(
                    "declinedJson and approvedJson must not be null");
        }

        long startTime = System.currentTimeMillis();

        log.info("Starting validateAll()");

        JsonNode declined;
        JsonNode approved;

        try {

            declined = MAPPER.readTree(
                    sanitizeJson(declinedJson));

            approved = MAPPER.readTree(
                    sanitizeJson(approvedJson));

        } catch (Exception e) {

            log.error(
                    "JSON parsing failed: {}",
                    e.getMessage(),
                    e
            );

            throw new IllegalArgumentException(
                    "Unable to parse JSON payload",
                    e
            );
        }

        if (declined == null
                || approved == null
                || !declined.isObject()
                || !approved.isObject()) {

            throw new IllegalArgumentException(
                    "Both transaction payloads must be valid JSON objects");
        }

        Set<String> fields = new TreeSet<>();

        declined.fieldNames().forEachRemaining(fields::add);
        approved.fieldNames().forEachRemaining(fields::add);

        log.info(
                "Total unique fields identified: {}",
                fields.size()
        );

        List<ValidateResult> results = new ArrayList<>();

        int skippedCount = 0;
        int emptyCount = 0;

        for (String field : fields) {

            JsonNode declinedNode = declined.get(field);
            JsonNode approvedNode = approved.get(field);

            String declinedValue =
                    getFieldValue(declinedNode);

            String approvedValue =
                    getFieldValue(approvedNode);

            log.info(
                    "-------------------------------------------------");

            log.info(
                    "FIELD COMPARISON | Field={} | Approved={} | Declined={}",
                    field,
                    safeLogValue(approvedValue),
                    safeLogValue(declinedValue)
            );

            /*
             * Skip configured version fields.
             */
            if (SKIPPED_VERSION_FIELDS.contains(field)) {

                skippedCount++;

                log.info(
                        "FIELD SKIPPED | Field={} | Reason=Version/Metadata field",
                        field
                );

                continue;
            }

            /*
             * Skip fields where both values are missing.
             */
            if (isEmpty(approvedValue)
                    && isEmpty(declinedValue)) {

                emptyCount++;

                log.info(
                        "FIELD SKIPPED | Field={} | Reason=Both values are empty",
                        field
                );

                continue;
            }

            /*
             * Do not skip equal values.
             *
             * Equal values must still pass pattern and length
             * validation.
             */
            ValidateResult result = validateField(
                    field,
                    approvedValue,
                    declinedValue
            );

            if (result != null) {

                results.add(result);

                log.info(
                        "FIELD VALIDATION RESULT | Field={} | Value={} | Pattern={} | Length={} | Summary={}",
                        field,
                        result.getValue(),
                        result.getPattern(),
                        result.getLength(),
                        result.getSummary()
                );
            }
        }

        long failedCount = results.stream()
                .filter(r ->
                        !ALL_PASSED_SUMMARY.equals(r.getSummary()))
                .count();

        long passedCount =
                results.size() - failedCount;

        log.info("=================================================");
        log.info("VALIDATE ALL COMPLETED");
        log.info("Total Unique Fields : {}", fields.size());
        log.info("Validated Fields    : {}", results.size());
        log.info("Skipped Fields      : {}", skippedCount);
        log.info("Empty Fields        : {}", emptyCount);
        log.info("Failed Fields       : {}", failedCount);
        log.info("Passed Fields       : {}", passedCount);
        log.info(
                "Execution Time      : {} ms",
                System.currentTimeMillis() - startTime
        );
        log.info("=================================================");

        return results;
    }

    // ============================================================
    // SINGLE FIELD VALIDATION
    // ============================================================

    /**
     * Performs three independent validations:
     *
     * 1. Value comparison
     * 2. Pattern validation
     * 3. Length validation
     */
    public ValidateResult validateField(
            String field,
            String approvedValue,
            String declinedValue) {

        log.info(
                "VALIDATING FIELD | Field={} | Approved={} | Declined={}",
                field,
                safeLogValue(approvedValue),
                safeLogValue(declinedValue)
        );

        /*
         * Handle empty product placeholder.
         */
        String product = "%1F%1F%1D";

        if (product.equals(approvedValue)) {
            approvedValue = null;
        }

        if (product.equals(declinedValue)) {
            declinedValue = null;
        }

        boolean approvedMissing =
                isEmpty(approvedValue);

        boolean declinedMissing =
                isEmpty(declinedValue);

        if (approvedMissing && declinedMissing) {

            log.info(
                    "BOTH VALUES MISSING | Field={}",
                    field
            );

            return null;
        }

        String approved =
                approvedMissing ? MISSING : approvedValue;

        String declined =
                declinedMissing ? MISSING : declinedValue;

        IsoFieldDefinition definition =
                definitionLoader.getField(field);

        String fieldName =
                definition != null
                        ? definition.getName()
                        : "UNKNOWN FIELD";

        log.info(
                "FIELD DEFINITION | Field={} | Name={} | ClassType={} | MinLength={} | MaxLength={}",
                field,
                fieldName,
                definition != null ? definition.getClassType() : null,
                definition != null ? definition.getMinLength() : null,
                definition != null ? definition.getMaxLength() : null
        );

        // ========================================================
        // 1. VALUE VALIDATION
        // ========================================================

        boolean valuesMatch =
                !approvedMissing
                        && !declinedMissing
                        && approved.equalsIgnoreCase(declined);

        String valueStatus =
                valuesMatch ? MATCH : MISMATCH;

        log.info(
                "VALUE COMPARISON | Field={} | Approved={} | Declined={} | Result={}",
                field,
                safeLogValue(approvedValue),
                safeLogValue(declinedValue),
                valueStatus
        );

        // ========================================================
        // 2. PATTERN VALIDATION
        // ========================================================

        Boolean approvedPatternValid =
                approvedMissing
                        ? Boolean.FALSE
                        : isValueValidForField(
                                field,
                                approved,
                                definition
                        );

        Boolean declinedPatternValid =
                declinedMissing
                        ? Boolean.FALSE
                        : isValueValidForField(
                                field,
                                declined,
                                definition
                        );

        String patternStatus;

        if (approvedPatternValid == null
                && declinedPatternValid == null) {

            patternStatus = VALID;

        } else {

            boolean approvedPatternOk =
                    approvedPatternValid == null
                            || approvedPatternValid;

            boolean declinedPatternOk =
                    declinedPatternValid == null
                            || declinedPatternValid;

            patternStatus =
                    approvedPatternOk && declinedPatternOk
                            ? VALID
                            : INVALID;
        }

        log.info(
                "PATTERN VALIDATION | Field={} | ApprovedValid={} | DeclinedValid={} | Result={}",
                field,
                approvedPatternValid,
                declinedPatternValid,
                patternStatus
        );

        // ========================================================
        // 3. LENGTH VALIDATION
        // ========================================================

        String lengthStatus;

        if (LENGTH_IGNORING_FIELDS.contains(field)) {

            lengthStatus = LENGTH_VALID;

            log.info(
                    "LENGTH VALIDATION SKIPPED | Field={} | Reason=Length ignoring field",
                    field
            );

        } else if (approvedMissing || declinedMissing) {

            lengthStatus = LENGTH_INVALID;

            log.warn(
                    "LENGTH VALIDATION FAILED | Field={} | Reason=One side is missing",
                    field
            );

        } else if (definition != null
                && definition.getMaxLength() > 0) {

            int minLength =
                    Math.max(definition.getMinLength(), 0);

            int maxLength =
                    definition.getMaxLength();

            boolean approvedLengthValid =
                    approved.length() >= minLength
                            && approved.length() <= maxLength;

            boolean declinedLengthValid =
                    declined.length() >= minLength
                            && declined.length() <= maxLength;

            lengthStatus =
                    approvedLengthValid && declinedLengthValid
                            ? LENGTH_VALID
                            : LENGTH_INVALID;

            log.info(
                    "LENGTH VALIDATION | Field={} | ApprovedLength={} | DeclinedLength={} | MinLength={} | MaxLength={} | ApprovedValid={} | DeclinedValid={} | Result={}",
                    field,
                    approved.length(),
                    declined.length(),
                    minLength,
                    maxLength,
                    approvedLengthValid,
                    declinedLengthValid,
                    lengthStatus
            );

        } else {

            lengthStatus = LENGTH_VALID;

            log.info(
                    "LENGTH VALIDATION | Field={} | Result=VALID | Reason=No XML length constraint",
                    field
            );
        }

        // ========================================================
        // 4. BUILD SUMMARY
        // ========================================================

        String summary;

        if ("1.1".equals(field)
                || "1.2".equals(field)
                || "1.3".equals(field)) {

            summary = ENVIRONMENT_VARIATION_SUMMARY;

        } else {

            summary = buildValidationSummary(
                    valueStatus,
                    patternStatus,
                    lengthStatus
            );
        }

        ValidateResult result = new ValidateResult(
                field,
                fieldName,
                declined,
                approved,
                valueStatus,
                patternStatus,
                lengthStatus,
                summary
        );

        log.info(
                "FIELD VALIDATION COMPLETED | Field={} | Value={} | Pattern={} | Length={} | Summary={}",
                field,
                valueStatus,
                patternStatus,
                lengthStatus,
                summary
        );

        return result;
    }

    // ============================================================
    // VALIDATION SUMMARY
    // ============================================================

    private String buildValidationSummary(
            String valueStatus,
            String patternStatus,
            String lengthStatus) {

        StringBuilder sb = new StringBuilder();

        if (MISMATCH.equals(valueStatus)) {

            sb.append(
                    "Approved and declined values are different. "
            );
        }

        if (INVALID.equals(patternStatus)) {

            sb.append(
                    "Approved or declined value does not match the expected pattern. "
            );
        }

        if (LENGTH_INVALID.equals(lengthStatus)) {

            sb.append(
                    "Approved or declined value length is invalid. "
            );
        }

        if (sb.length() == 0) {
            return ALL_PASSED_SUMMARY;
        }

        return sb.toString().trim();
    }

    // ============================================================
    // PATTERN VALIDATION ROUTER
    // ============================================================

    private Boolean isValueValidForField(
            String field,
            String value,
            IsoFieldDefinition definition) {

        if (value == null) {
            return Boolean.FALSE;
        }

        Boolean dedicatedResult =
                dedicatedRuleForField(field, value);

        if (dedicatedResult != null) {

            return dedicatedResult;
        }

        return genericPatternForClassType(
                value,
                definition
        );
    }

    // ============================================================
    // FIELD-SPECIFIC VALIDATION
    // ============================================================

    private Boolean dedicatedRuleForField(
            String field,
            String value) {

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

            case "4.16":
                return isValidPinBlock(value);

            case "4.17":
                return isValidKsn(value);

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

    // ============================================================
    // GENERIC CLASS TYPE VALIDATION
    // ============================================================

    private Boolean genericPatternForClassType(
            String value,
            IsoFieldDefinition definition) {

        if (definition == null
                || definition.getClassType() == null) {

            return null;
        }

        switch (definition.getClassType()) {

            case "NUMERIC":
                return matches(NUMERIC_PATTERN, value);

            case "AMOUNT":
                return isValidAmount(value);

            case "IP":
                return isValidIPv4(value);

            case "EMAIL":
                return matches(EMAIL_PATTERN, value);

            case "ENUM":
                return isValidEnum(value, definition);

            case "DATE":
            case "TIME":
            case "DOB":
                return matches(NUMERIC_PATTERN, value);

            default:
                return null;
        }
    }

    // ============================================================
    // ENUM VALIDATION
    // ============================================================

    private boolean isValidEnum(
            String value,
            IsoFieldDefinition definition) {

        if (value == null) {
            return false;
        }

        String allowedValues = definition.getValue();

        if (allowedValues == null
                || allowedValues.trim().isEmpty()) {

            log.debug(
                    "ENUM validation skipped because no allowed values are defined"
            );

            return true;
        }

        for (String token : allowedValues.split(",")) {

            if (token.trim().equals(value.trim())) {

                log.debug(
                        "ENUM validation successful | Value={}",
                        value
                );

                return true;
            }
        }

        log.warn(
                "ENUM validation failed | Value={} | AllowedValues={}",
                safeLogValue(value),
                allowedValues
        );

        return false;
    }

    // ============================================================
    // INDIVIDUAL VALIDATION METHODS
    // ============================================================

    private boolean isValidZipCode(String value) {

        return value != null
                && ZIP_PATTERN.matcher(value.trim()).matches();
    }

    private boolean isValidReferenceNumber(String value) {

        return value != null
                && (
                        matches(INVOICE_REFERENCE_PATTERN, value)
                        || matches(REFERENCE_TOKEN_PATTERN, value)
                );
    }

    private boolean isValidCardExpiry(String value) {

        if (value == null
                || !matches(CARD_EXPIRY_PATTERN, value)) {

            return false;
        }

        int month = Integer.parseInt(
                value.substring(0, 2)
        );

        return month >= 1 && month <= 12;
    }

    private boolean isValidDateTime(String value) {

        if (value == null) {
            return false;
        }

        try {

            LocalDateTime.parse(
                    value,
                    DATE_TIME_11_4_FORMAT
            );

            return true;

        } catch (Exception e) {

            log.debug(
                    "DateTime validation failed | Value={}",
                    safeLogValue(value)
            );

            return false;
        }
    }

    private boolean isValidAmount(String value) {

        return value != null
                && !value.trim().isEmpty()
                && matches(AMOUNT_PATTERN, value);
    }

    private boolean isValidYYYYMMDD(String value) {

        if (value == null) {
            return false;
        }

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

    private boolean isValidKsn(String value) {

        return value != null
                && KSN_PATTERN.matcher(value).matches();
    }

    private boolean isValidPinBlock(String value) {

        return value != null
                && PIN_BLOCK_PATTERN.matcher(value).matches();
    }

    /**
     * Numeric-range IPv4 validation.
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

    // ============================================================
    // COMMON HELPER METHODS
    // ============================================================

    private static boolean matches(
            Pattern pattern,
            String value) {

        return value != null
                && pattern.matcher(value).matches();
    }

    private static boolean isEmpty(String value) {

        return value == null
                || value.trim().isEmpty()
                || "null".equalsIgnoreCase(value.trim());
    }

    /**
     * Safely extracts JSON values.
     */
    private static String getFieldValue(JsonNode node) {

        if (node == null || node.isNull()) {
            return null;
        }

        if (node.isContainerNode()) {
            return node.toString();
        }

        return node.asText();
    }

    /**
     * Redacts transaction values from logs.
     *
     * Payment transaction data should not be exposed in application logs.
     */
    private static String safeLogValue(String value) {

        if (value == null) {
            return "<NULL>";
        }

        if (value.trim().isEmpty()) {
            return "<EMPTY>";
        }

        return "[REDACTED, length=" + value.length() + "]";
    }

    /**
     * Removes trailing commas from lenient JSON.
     */
    private static String sanitizeJson(String json) {

        return json.replaceAll(
                ",\\s*([}\\]])",
                "$1"
        );
    }

    // ============================================================
    // OPTIONAL SKIPPED FIELD RESULT
    // ============================================================

    /**
     * Creates an explicit skipped result when a caller needs
     * a complete field report.
     */
    private ValidateResult buildSkippedResult(
            String field,
            String declinedValue,
            String approvedValue) {

        IsoFieldDefinition definition =
                definitionLoader.getField(field);

        String fieldName =
                definition != null
                        ? definition.getName()
                        : "VERSION FIELD (validation skipped)";

        String declined =
                isEmpty(declinedValue)
                        ? MISSING
                        : declinedValue;

        String approved =
                isEmpty(approvedValue)
                        ? MISSING
                        : approvedValue;

        log.info(
                "BUILDING SKIPPED RESULT | Field={}",
                field
        );

        return new ValidateResult(
                field,
                fieldName,
                declined,
                approved,
                SKIPPED,
                SKIPPED,
                SKIPPED,
                "Validation skipped for version field " + field + "."
        );
    }
}
