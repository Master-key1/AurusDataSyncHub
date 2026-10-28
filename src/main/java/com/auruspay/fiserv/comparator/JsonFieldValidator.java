package com.auruspay.comparator;

import com.auruspay.comparator.config.IsoFieldDefinitionLoader;
import com.auruspay.comparator.model.ComparisonJsonResult;
import com.auruspay.comparator.model.IsoFieldDefinition;
import com.auruspay.comparator.model.IssueDetail;
import com.auruspay.comparator.model.ValidateResult;
import com.auruspay.util.ExtractMultipleKeywords;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Validates the ISO-8583-style fields of a declined transaction's JSON payload
 * against the approved transaction's JSON payload, and returns a flat,
 * per-field report — one {@link ValidateResult} per field, each carrying
 * independent Value / Pattern / Length verdicts plus a human-readable summary.
 *
 * This is a companion to {@link JsonComparator}: where {@code JsonComparator}
 * buckets fields into MATCHED/MISMATCH/SKIPPED/VALIDATION-ISSUE lists, this
 * class always emits at most one result per field that actually needs
 * attention, which is easier for downstream consumers (UI tables, per-field
 * dashboards, etc.) to iterate over uniformly.
 *
 * Stateless and thread-safe: every {@link #validateAll(String, String)} call
 * builds its own result list; no mutable instance state is shared across
 * requests.
 */
@Service
public class JsonFieldValidator {

	private final ExtractMultipleKeywords extractMultipleKeywords;

	private static final Logger log = LoggerFactory.getLogger(JsonFieldValidator.class);
	private static final ObjectMapper MAPPER = new ObjectMapper();
	@Autowired
	private ComparisonJsonResult comparisonJsonResult;
	private static final String MISSING = "NULL";
	private static final String MATCH = "MATCH";
	private static final String MISMATCH = "MISMATCH";
	private static final String VALID = "VALID";
	private static final String INVALID = "INVALID";
	private static final String LENGTH_VALID = "VALID";
	private static final String LENGTH_INVALID = "INVALID";
	private static final String SKIPPED = "SKIPPED";
	private static final String ALL_PASSED_SUMMARY = "Validation successful. All checks passed.";

	// Fields that are version/metadata fields and are exempt from validation
	// entirely — still reported (so every field the payload contains shows up
	// in the output), but flagged as SKIPPED rather than checked.
	private static final Set<String> SKIPPED_VERSION_FIELDS = Set.of("0", "1.1", "1.2", "1.3", "1.4", "1.141", "2.1",
			"2.2", "2.3", "2.4.3", "2.4.58", "4.13", "4.18", "4.19", "4.24", "4.38", "4.41", "4.46", "4.47", "4.49",
			"4.32", "4.52", "4.67", "4.68", "5.5", "5.8", "5.9", "5.13", "5.14", "5.15", "5.23", "6.1", "6.2", "6.3",
			"6.4", "6.5", "6.6", "6.7", "6.8", "6.11", "6.30", "6.32", "6.42", "6.99.11", "7.2", "7.3", "7.4", "7.5",
			"7.6", "7.7", "11.1", "11.2", "11.3", "11.4", "11.5", "11.6", "11.7", "11.8", "11.9", "12.1", "12.54",
			"12.61", "12.86", "12.94", "20.15", "91.1.9", "92.6.3", "12.112", "12.214", "12.23", "4.149", "4.146",
			"4.141", "4.142", "3.23", "4.2", "3.66", "4.100", "4.101", "4.103", "12.128", "12.129", "12.132", "12.210",
			"12.63", "12.70", "12..74", "15.1","4.64"

	);
	private static final Set<String> IMP_VERSION_FIELDS = Set.of("3.1", "3.2", "3.5", "3.21", "4.1", "4.3", "4.15",
			"4.16", "4.17", "4.20", "4.21", "4.30", "4.36", "4.38", "4.39", "4.40", "4.63", "4.70", "4.78", "4.104",
			"4.112", "4.113", "4.141", "5.1", "6.9",  "7.8", "11.3", "12.72", "12.73", "12.89", "14.13", "3.3",
			"3.7", "8.1", "4.32", "4.22");
	// Fields whose length is allowed to legitimately differ between the two
	// sides (e.g. amount formatting) — length is not checked for these at all.
	private static final Set<String> LENGTH_IGNORING_FIELDS = Set.of("4.5", "4.6", "4.7", "4.8", "4.9", "4.10", "4.11",
			"4.64", "4.161", "4.163", "4.135", "4.134", "4.128", "4.127", "4.126");
	private static final Set<String> MISMATCH_VALUE_FIELDS = Set.of("4.5", "4.6", "4.7", "4.8", "4.9", "4.10", "4.11",
			"8.1", "4.38","6.9","4.64","7.5");
	// ---- Precompiled patterns (regex compilation is expensive; String.matches()
	// recompiles on every call, which matters at per-field, per-transaction volume)
	// ----
	private static final Pattern UUID_PATTERN = Pattern
			.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");
	private static final Pattern FIELD_121_PATTERN = Pattern
			.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}-\\d{10}$");
	private static final Pattern SIX_DIGIT_PATTERN = Pattern.compile("^\\d{6}$");
	private static final Pattern THIRTEEN_DIGIT_PATTERN = Pattern.compile("^\\d{13}$");
	private static final Pattern AMOUNT_PATTERN = Pattern.compile("^\\d+\\.\\d{2}$");
	private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+\\d{10,15}$");
	private static final Pattern CARD_EXPIRY_PATTERN = Pattern.compile("^\\d{4}$");
	private static final Pattern FIELD_444_PATTERN = Pattern.compile("^3\\d{31}$");
	private static final Pattern INVOICE_REFERENCE_PATTERN =
		    Pattern.compile("^(?:[A-Z]\\d{9}|[A-Z0-9]+-\\d{8}|\\d+)$");
	private static final Pattern REFERENCE_TOKEN_PATTERN = Pattern.compile("^Token-\\d+$");
	private static final Pattern FIELD_1296_PATTERN = Pattern.compile("^W\\d{9}$");
	// NOTE: matches the literal text "%1F"/"%1D", not the ASCII unit/group
	// separator control chars (0x1F / 0x1D). Kept identical to JsonComparator —
	// confirm against a real payload before "fixing" this.
	private static final Pattern ITEM_DETAIL_LIST_PATTERN = Pattern.compile("^\\d+%1F\\d+%1F.*%1D$");
	private static final Pattern ZIP_PATTERN = Pattern.compile("^[0-9A-Za-z]+(?:-[0-9A-Za-z]+)?$");
	private static final Pattern NUMERIC_PATTERN = Pattern.compile("^[0-9]+$");
	private static final Pattern MTI_PATTERN = Pattern.compile("^\\d{4}$");
	private static final Pattern MAC_ADDRESS_PATTERN = Pattern.compile("^([0-9A-Fa-f]{2}:){5}[0-9A-Fa-f]{2}$");
	private static final Pattern COUNTRY_CODE_PATTERN = Pattern.compile("^[A-Z]{2}$");
	private static final Pattern SESSION_FLAG_PATTERN = Pattern.compile("^\\d+_\\d+$");
	// Generic fallback for classType="EMAIL" fields that don't have a dedicated
	// field-specific rule (e.g. 19.25 MERCHANTEMAIL, 72.93.7 CUSTOMEREMAILADDR).
	private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
	private static final DateTimeFormatter DATE_TIME_11_4_FORMAT = DateTimeFormatter.ofPattern("MMddyyyyHHmmss");
	private static final Pattern PIN_BLOCK_PATTERN = Pattern.compile("^[0-9A-Fa-f]{16}$");
	private static final Pattern KSN_PATTERN = Pattern.compile("^[0-9A-Fa-f]{20}$");

	private final IsoFieldDefinitionLoader definitionLoader;
	static IsoFieldDefinitionLoader loader;

	public JsonFieldValidator(IsoFieldDefinitionLoader definitionLoader,
			ExtractMultipleKeywords extractMultipleKeywords) {
		this.definitionLoader = definitionLoader;
		this.extractMultipleKeywords = extractMultipleKeywords;
	}

	static {
		loader = new IsoFieldDefinitionLoader();
		loader.loadDefinitions(); // normally called by Spring's @PostConstruct; called manually here

	}

	/**
	 * Runs {@link #validateField(String, String, String)} across every field in the
	 * union of the declined and approved payloads.
	 *
	 * @param declinedJson raw JSON payload from the declined transaction
	 * @param approvedJson raw JSON payload from the approved transaction
	 * @return one {@link ValidateResult} per field that actually needs attention
	 * 
	 */

	public ComparisonJsonResult validation(String declinedJson, String approvedJson) throws Exception {

		List<ValidateResult> list = validateAll(declinedJson, approvedJson);
		List<ValidateResult> matchVal = new ArrayList<>();
		List<ValidateResult> misMatchVal = new ArrayList<>();
		List<ValidateResult> issueVal = new ArrayList<>();
		if (list != null) {
			for (ValidateResult val : list) {

				if (val != null && SKIPPED_VERSION_FIELDS.contains(val.getField())) {
					if (val.getPattern().equals("VALID") && val.getValue().equals("MATCH")) {
						matchVal.add(val);

						// log.info("matchVal:{} ",matchVal);
					} else if (val.getPattern().equals("VALID") && val.getValue().equals("MISMATCH")
							&& !MISMATCH_VALUE_FIELDS.contains(val.getField())) {
						misMatchVal.add(val);

						// log.info("misMatchVal:{} ",misMatchVal);
					} else if (!LENGTH_IGNORING_FIELDS.contains(val.getField())) {
						if (!List.of("8.1","7.5").contains(val.getField()))
							issueVal.add(val);
						// log.info("list:{} ",issueVal);
						//
					}
				}
			}
		}
	
			// comparisonJsonResult.setMatchIssue(matchVal);
			if (misMatchVal != null)
				comparisonJsonResult.setMissMatchIssue(misMatchVal);
		if (issueVal != null)
			comparisonJsonResult.setValidationIssue(issueVal);

		return comparisonJsonResult;
	}

	public List<ValidateResult> validateAll(String declinedJson, String approvedJson) throws Exception {
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

		List<ValidateResult> results = new ArrayList<>();
		List<ValidateResult> skipped = new ArrayList<>();

		for (String field : fields) {
			String declinedValue = declined.has(field) ? declined.get(field).asText() : null;
			String approvedValue = approved.has(field) ? approved.get(field).asText() : null;

			if (Objects.equals(approvedValue, declinedValue)) {
				continue;
			}
			boolean approvedEmpty = approvedValue == null || approvedValue.trim().isEmpty();
			boolean declinedEmpty = declinedValue == null || declinedValue.trim().isEmpty();

			if (approvedEmpty && declinedEmpty) {
				// System.out.printf("Skipping Field: %s (both values are null/blank)%n",
				// field);
				continue;
			}
			/*
			 * 
			 * if (declinedEmpty && !approvedEmpty) { System.out.
			 * printf("Declined is empty - Field: %s, Approved: %s, Declined: %s%n", field,
			 * approvedValue, declinedValue); continue; }
			 * 
			 * if (approvedEmpty && !declinedEmpty) { System.out.
			 * printf("Approved is empty - Field: %s, Approved: %s, Declined: %s%n", field,
			 * approvedValue, declinedValue); continue; }
			 */
			ValidateResult val = validateField(field, approvedValue, declinedValue);
			if (val != null && !SKIPPED_VERSION_FIELDS.contains(fields)
					&& (IMP_VERSION_FIELDS.contains(field) || LENGTH_IGNORING_FIELDS.contains(field)))
				results.add(val);
			else
				skipped.add(val);

		}

		long failedCount = results.stream().filter(r -> !ALL_PASSED_SUMMARY.equals(r.getSummary())).count();

		log.info("Field-level validation completed in {} ms. Total={}, Failed={}, Passed={}",
				System.currentTimeMillis() - startTime, results.size(), failedCount, results.size() - failedCount);

		return results;
	}

	/**
	 * Validates a single field's declined vs approved values across three
	 * independent checks — Value equality, Pattern conformance, Length validity —
	 * and returns a {@link ValidateResult} with a summary that only mentions the
	 * checks that failed.
	 *
	 * @param field         ISO field key, e.g. "1.1"
	 * @param approvedValue value from the approved transaction (may be null)
	 * @param declinedValue value from the declined transaction (may be null)
	 */
	public ValidateResult validateField(String field, String approvedValue, String declinedValue) {

		String product = "%1F%1F%1D";
		if (product.equals(approvedValue)) {
			approvedValue = null;
		}
		if (product.equals(declinedValue)) {
			declinedValue = null;
		}
		boolean approvedMissing = approvedValue == null || approvedValue.isEmpty()
				|| "null".equalsIgnoreCase(approvedValue) || "".equalsIgnoreCase(approvedValue);

		boolean declinedMissing = declinedValue == null || declinedValue.isEmpty()
				|| "null".equalsIgnoreCase(declinedValue) || "".equalsIgnoreCase(declinedValue);

		// Log when BOTH are missing
		if (approvedMissing && declinedMissing) {
			log.info("Field {}: both approved and declined values are missing. approved='{}', declined='{}'", field,
					approvedValue, declinedValue);
			return null;
		}

		if (declinedMissing && approvedValue.length() == 0) {
			log.info("field :{}", field);
		}

		if (approvedMissing && declinedValue.length() == 0) {
			log.info("field* :{}", field);
		}
		log.info("CHECK field={}, approved=[{}], declined=[{}], approvedMissing={}, declinedMissing={}", field,
				approvedValue, declinedValue, approvedMissing, declinedMissing);

		String declined = declinedValue == null ? MISSING : declinedValue;
		String approved = approvedValue == null ? MISSING : approvedValue;
		if (approvedValue == null && declinedValue == null) {
			return null;
		}

		IsoFieldDefinition definition = loader.getField(field);

		String fieldName = definition != null ? definition.getName() : "UNKNOWN FIELD";

		declinedMissing = MISSING.equals(declined);
		approvedMissing = MISSING.equals(approved);

		// ---- 1. Value check ------------------------------------------------
		boolean valuesMatch = !declinedMissing && !approvedMissing
				&& (Objects.equals(declined, approved) || approved.equalsIgnoreCase(declined));
		String valueStatus = valuesMatch ? MATCH : MISMATCH;

		// ---- 2. Pattern check (per-side, via the field's dedicated rule, or the
		// XML's classType-driven generic rule when no dedicated rule exists) ----
		// A missing value can never satisfy a pattern.
		Boolean declinedPatternValid = declinedMissing ? Boolean.FALSE
				: isValueValidForField(field, declined, definition);
		Boolean approvedPatternValid = approvedMissing ? Boolean.FALSE
				: isValueValidForField(field, approved, definition);

		String patternStatus;
		if (declinedPatternValid == null && approvedPatternValid == null) {
			// No dedicated rule AND no usable classType rule for this field ->
			// nothing to validate against, so pattern is considered valid.
			patternStatus = VALID;
		} else {
			boolean declinedOk = declinedPatternValid == null || declinedPatternValid;
			boolean approvedOk = approvedPatternValid == null || approvedPatternValid;
			patternStatus = (declinedOk && approvedOk) ? VALID : INVALID;
		}

		// ---- 3. Length check -------------------------------------------------
		// Driven by the ISO field XML's minlength/maxlength. A field is only
		// flagged INVALID when its value actually falls outside [minLength,
		// maxLength] as defined in the XML. minLength may legitimately be 0
		// (e.g. "4.5" TRANSACTION AMOUNT is min=0 max=15) — only maxLength > 0
		// signals that the field has a real, enforced length in the spec.
		// Fields with no XML definition, or maxLength == 0 (reserved / not
		// currently used), have no defined constraint, so they are not flagged.
		String lengthStatus;
		if (LENGTH_IGNORING_FIELDS.contains(field)) {
			lengthStatus = LENGTH_VALID;
		} else if ((declinedMissing || approvedMissing)) {
			lengthStatus = LENGTH_INVALID;
		} else if (definition != null && definition.getMaxLength() > 0) {
			int minLen = Math.max(definition.getMinLength(), 0);
			int maxLen = definition.getMaxLength();
			boolean declinedLenOk = declined.length() >= minLen && declined.length() <= maxLen;
			boolean approvedLenOk = approved.length() >= minLen && approved.length() <= maxLen;
			lengthStatus = (declinedLenOk && approvedLenOk) ? LENGTH_VALID : LENGTH_INVALID;
		} else {
			// No usable length definition in the ISO field XML -> don't flag length.
			lengthStatus = LENGTH_VALID;
		}

		String summary = null;
		if (field.equals("1.1") || field.equals("1.2") || field.equals("1.3"))
			summary = "This value might vary depending on the environment.";
		else
			summary = buildValidationSummary(valueStatus, patternStatus, lengthStatus);

		ValidateResult result = new ValidateResult(field, fieldName, declined, approved, valueStatus, patternStatus,
				lengthStatus, summary);

		log.debug("Field={} | Value={} | Pattern={} | Length={} | Summary={}", field, valueStatus, patternStatus,
				lengthStatus, summary);

		return result;
	}

	/**
	 * Builds a {@link ValidateResult} entry for a field that's exempt from
	 * validation (see {@link #SKIPPED_VERSION_FIELDS}), for callers that want an
	 * explicit placeholder entry instead of omitting the field entirely. Not called
	 * from {@link #validateAll(String, String)} (which omits skipped fields from
	 * the report), but kept available for callers that want a complete, gap-free
	 * field list.
	 */
	private ValidateResult buildSkippedResult(String field, String declinedValue, String approvedValue) {
		IsoFieldDefinition definition = definitionLoader.getField(field);
		String fieldName = definition != null ? definition.getName() : "VERSION FIELD (validation skipped)";
		String declined = declinedValue == null ? MISSING : declinedValue;
		String approved = approvedValue == null ? MISSING : approvedValue;

		return new ValidateResult(field, fieldName, declined, approved, SKIPPED, SKIPPED, "Skipped",
				"Validation skipped for version field " + field + ".");
	}

	/**
	 * Builds the human-readable summary. Only failed checks are mentioned, in Value
	 * -> Pattern -> Length order; if everything passed, returns the fixed "all
	 * checks passed" message.
	 */
	private String buildValidationSummary(String valueStatus, String patternStatus, String lengthStatus) {
		StringBuilder sb = new StringBuilder();

		if (MISMATCH.equals(valueStatus)) {
			sb.append("Approved and declined values are different. ");
		}
		if (MISMATCH.equals(patternStatus)) {
			sb.append("Approved or declined value does not match the expected pattern. ");
		}
		if (LENGTH_INVALID.equalsIgnoreCase(lengthStatus)) {
			sb.append("Approved or declined value length is invalid. ");
		}

		if (sb.length() == 0) {
			return ALL_PASSED_SUMMARY;
		}
		return sb.toString().trim();
	}

	/**
	 * Per-field, per-value validity check. Checks a dedicated, hand-written rule
	 * first (see {@link #dedicatedRuleForField(String, String)}); if the field has
	 * no dedicated rule, falls back to a generic rule driven by the field's
	 * {@code classType} in the ISO field XML (see
	 * {@link #genericPatternForClassType(String, IsoFieldDefinition)}).
	 *
	 * @return {@code Boolean.TRUE}/{@code FALSE} if either a dedicated or a
	 *         classType-driven rule applies, or {@code null} if neither exists
	 *         (caller should treat the field as pattern-valid, since there is
	 *         nothing to validate against).
	 */
	private Boolean isValueValidForField(String field, String value, IsoFieldDefinition definition) {
		if (value == null) {
			return Boolean.FALSE;
		}

		Boolean dedicated = dedicatedRuleForField(field, value);
		if (dedicated != null) {
			return dedicated;
		}

		return genericPatternForClassType(value, definition);
	}

	/**
	 * Hand-written, field-specific validation rules. These take priority over the
	 * generic classType-driven fallback because they encode knowledge more precise
	 * than the XML's classType alone can express (e.g. field "11.1" isn't just
	 * ALPHA_NUMERIC — it's specifically a UUID).
	 *
	 * @return {@code Boolean.TRUE}/{@code FALSE} if this field has a dedicated
	 *         rule, or {@code null} if it doesn't (caller should fall back to
	 *         {@link #genericPatternForClassType}).
	 */
	private Boolean dedicatedRuleForField(String field, String value) {
		switch (field) {
		case "0":
			return matches(MTI_PATTERN, value);
		case "1.1":
		case "1.2":
		case "1.3":
			return matches(NUMERIC_PATTERN, value);
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
			return null; // no dedicated rule for this field
		}
	}

	/**
	 * Generic fallback rule derived from the field's {@code classType} in the ISO
	 * field XML (0/1.1/... {@code classType="NUMERIC"}, 4.5/4.6/...
	 * {@code classType="AMOUNT"}, etc.) — used only when
	 * {@link #dedicatedRuleForField} returns {@code null}, i.e. the field has no
	 * field-specific override.
	 *
	 * @return {@code Boolean.TRUE}/{@code FALSE} if the classType maps to a
	 *         checkable rule, or {@code null} if the classType is ALPHA_NUMERIC,
	 *         unrecognized, or the definition itself is missing (nothing to
	 *         validate against in either case).
	 */
	private Boolean genericPatternForClassType(String value, IsoFieldDefinition definition) {
		if (definition == null || definition.getClassType() == null) {
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
			// The XML doesn't give a fixed calendar/time format per field
			// beyond "this is a date/time/DOB component" -- a strict
			// parse would false-positive across differently-formatted
			// fields, so this catches non-numeric junk without
			// over-constraining the accepted format.
			return matches(NUMERIC_PATTERN, value);
		default:
			// ALPHA_NUMERIC or any other/unknown classType -> no generic
			// rule; almost anything is a legitimate ALPHA_NUMERIC value.
			return null;
		}
	}

	/**
	 * Validates a value against an ENUM field's allowed token list, as defined by
	 * the ISO field XML's {@code value="..."} attribute (e.g.
	 * {@code <isofield id="73.3" ... value="0,1,2"/>}).
	 *
	 * IMPORTANT: this reads {@code definition.getValue()} — the ENUM token list —
	 * NOT {@code definition.getName()}, which is just the field's display label
	 * (e.g. "NOT SO GOOD FLAG") and has nothing to do with which values are legal.
	 * {@code IsoFieldDefinition} must expose a {@code value} property populated
	 * from the XML's {@code value="..."} attribute for this to work correctly.
	 *
	 * @return {@code true} if {@code definition.getValue()} is unavailable (nothing
	 *         to validate against), or if {@code value} matches one of the
	 *         comma-separated tokens; {@code false} otherwise.
	 */
	private boolean isValidEnum(String value, IsoFieldDefinition definition) {
		if (value == null) {
			return false;
		}
		String allowedValues = definition.getValue();
		if (allowedValues == null || allowedValues.trim().isEmpty()) {
			return true; // no enum list to validate against
		}
		for (String token : allowedValues.split(",")) {
			if (token.trim().equals(value.trim())) {
				return true;
			}
		}
		return false;
	}

	private boolean isValidZipCode(String value) {
		return value != null && ZIP_PATTERN.matcher(value.trim()).matches();
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

	private boolean isValidKsn(String value) {
		return value != null && KSN_PATTERN.matcher(value).matches();
	}

	private boolean isValidPinBlock(String value) {
		return value != null && PIN_BLOCK_PATTERN.matcher(value).matches();
	}

	/**
	 * Numeric-range IPv4 validation. Deliberately NOT regex-only: a strict regex
	 * rejects legitimate values with a leading-zero octet such as "00.00.00.00",
	 * which shows up as a real placeholder value in fields like 2.3/11.2/5.5.
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
}