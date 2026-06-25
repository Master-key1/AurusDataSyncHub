package com.auruspay.comparator;

import com.auruspay.comparator.config.IsoFieldDefinitionLoader;
import com.auruspay.comparator.model.ComparisonResult;
import com.auruspay.comparator.model.IsoFieldDefinition;
import com.auruspay.comparator.model.IssueDetail;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class JsonComparator {

	private static final Logger LOGGER = Logger.getLogger(JsonComparator.class.getName());
	private static final ObjectMapper MAPPER = new ObjectMapper();

	private static final String MATCHED = "MATCHED";
	private static final String MISMATCH = "MISMATCH";
	private static final String SKIPPED = "SKIPPED";
	private static final String MISSING = "MISSING";

	private static final Set<String> SKIPPED_VERSION_FIELDS = Set.of("6.1", "6.3", "6.4", "12.54", "6.11", "6.42","7.4",
			"4.67", "7.3", "2.4.58", "12.1", "6.7", "6.5", "6.6", "6.30", "6.32", "6.99.11", "2.4.3", "7.5", "11.7");

	private static final Set<String> VALIDATE_PATTERN_FIELDS = Set.of("11.1", "11.2", "11.4", "11.8", "4.4", "4.18",
			"5.5", "4.19", "11.5", "7.5", "4.5", "4.6", "4.7", "4.8", "4.9", "4.10", "4.11", "4.44", "4.2", "4.70",
			"5.1", "5.3", "6.10", "6.40", "12.96", "2.4.1", "6.9");

	private static final Set<String> LEEGTH_IGNORING_FILED = Set.of("4.5", "4.6", "4.7", "4.8", "4.9", "4.10", "4.11",
			"4.64");

	@Autowired
	private IsoFieldDefinitionLoader definitionLoader;

	public ComparisonResult compare(String declinedJson, String approvedJson) throws Exception {

		if (declinedJson == null || approvedJson == null) {
			throw new IllegalArgumentException("declinedJson and approvedJson must not be null");
		}

		long startTime = System.currentTimeMillis();

		declinedJson = sanitizeJson(declinedJson);
		approvedJson = sanitizeJson(approvedJson);

		JsonNode declined;
		JsonNode approved;

		try {
			declined = MAPPER.readTree(declinedJson);
			approved = MAPPER.readTree(approvedJson);
		} catch (Exception e) {
			LOGGER.severe("Invalid JSON payload: " + e.getMessage());
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
			LOGGER.info("Processing field: " + field);

			if (SKIPPED_VERSION_FIELDS.contains(field)) {
				IssueDetail issue = new IssueDetail();
				issue.setField(field);
				issue.setFieldName("VERSION FIELD (validation skipped)");
				issue.setDeclinedValue(declined.has(field) ? declined.get(field).asText() : MISSING);
				issue.setApprovedValue(approved.has(field) ? approved.get(field).asText() : MISSING);
				issue.setComparisonResult(SKIPPED);

				LOGGER.info("Field " + field + " is SKIPPED.");
				skippedIssue.add(issue);
				continue;
			}

			String dVal = declined.has(field) ? declined.get(field).asText() : MISSING;
			String aVal = approved.has(field) ? approved.get(field).asText() : MISSING;

			LOGGER.info("Field=" + field + " | DeclinedValue=" + dVal + " | ApprovedValue=" + aVal);

			IsoFieldDefinition definition = definitionLoader.getField(field);
			boolean matched = Objects.equals(dVal, aVal);

			IssueDetail issue = buildIssue(field, dVal, aVal, definition, matched);
			String reason = issue.getReason();
			boolean patternMatch = false;

			if ("Expected Value".equals(reason)) {
				LOGGER.info("Field=" + field + " | Result=MATCHED");
				matchIssue.add(issue);
			} else if ("Value mismatch".equals(reason)) {
				if (VALIDATE_PATTERN_FIELDS.contains(field)) {
					patternMatch = isPatternMatched(field, dVal, aVal);
					issue.setPatternMatch(patternMatch ? MATCHED : MISMATCH+"#");
					LOGGER.info("Field=" + field + ", PatternMatch=" + (patternMatch ? MATCHED : MISMATCH));
				}

				if (patternMatch) {
					missMatchIssue.add(issue);
				} else {

					validationIssue.add(issue);
				}
			} else {
				if (!("Expected Value".equals(reason) && "Value mismatch".equals(reason))) {
					issue.setPatternMatch(MISMATCH + "*");
				}
				if (LEEGTH_IGNORING_FILED.contains(field)) {
					issue.setReason("Expected Value");
					// issue.setPatternMatch(MATCHED);
					matchIssue.add(issue);
				} else
					validationIssue.add(issue);
				LOGGER.info("Field=" + field + " | Result=VALIDATION ISSUE | Reason=" + reason);
			}
		}

		ComparisonResult result = new ComparisonResult();
		result.setValidationIssue(validationIssue);
		result.setMatchIssue(matchIssue);
		result.setMissMatchIssue(missMatchIssue);
		result.setSkippedIssue(skippedIssue);

		LOGGER.info("Comparison completed in " + (System.currentTimeMillis() - startTime) + " ms. Matched="
				+ matchIssue.size() + ", Mismatch=" + missMatchIssue.size() + ", Skipped=" + skippedIssue.size()
				+ ", Validation=" + validationIssue.size());

		return result;
	}

	private IssueDetail buildIssue(String field, String dVal, String aVal, IsoFieldDefinition definition,
			boolean matched) {
		IssueDetail issue = new IssueDetail();
		issue.setField(field);
		issue.setFieldName(definition != null ? definition.getName() : "UNKNOWN FIELD");
		issue.setDeclinedValue(dVal);
		issue.setApprovedValue(aVal);
		issue.setComparisonResult(matched ? MATCHED : MISMATCH);
		issue.setExpectedMinLength(definition != null ? String.valueOf(definition.getMinLength()) : "N/A");
		issue.setExpectedMaxLength(definition != null ? String.valueOf(definition.getMaxLength()) : "N/A");
		issue.setExpectedPattern(definition != null ? getPattern(definition.getClassType()) : "N/A");
		issue.setDeclinedLength(MISSING.equals(dVal) ? "0" : String.valueOf(dVal.length()));
		issue.setApprovedLength(MISSING.equals(aVal) ? "0" : String.valueOf(aVal.length()));
		// Ignoring length in that file

		if (!LEEGTH_IGNORING_FILED.contains(field)) {
			issue.setReason(matched ? "Expected Value" : getReason(dVal, aVal));
		}
		return issue;
	}

	private boolean isPatternMatched(String field, String declinedValue, String approvedValue) {
		if (declinedValue == null || approvedValue == null)
			return false;
		switch (field) {
		case "11.1":
			String uuidPattern = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";
			return declinedValue.matches(uuidPattern) && approvedValue.matches(uuidPattern);
		case "11.2":
		case "5.5":
			String ipPattern = "^((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)\\.){3}"
					+ "(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)$";
			return declinedValue.matches(ipPattern) && approvedValue.matches(ipPattern);
		case "4.18":
			return isValidYYYYMMDD(declinedValue) && isValidYYYYMMDD(approvedValue);
		case "4.19":
			return declinedValue.matches("^\\d{6}$") && approvedValue.matches("^\\d{6}$");
		case "12.1":
			return isValidField121(declinedValue) && isValidField121(approvedValue);

		case "11.5":
			return declinedValue.matches("^\\d{13}$") && approvedValue.matches("^\\d{13}$");
		case "7.5":
			return isValidItemDetailList(declinedValue) && isValidItemDetailList(approvedValue);
		case "4.5":
		case "4.6":
		case "4.7":
		case "4.8":
		case "4.9":
		case "4.10":
		case "4.11":
		case "4.64":
			return isValidAmount(declinedValue) && isValidAmount(approvedValue);

		case "6.10": // BILLING CELL NUMBER
		case "6.40": // CUSTOMER PHONE NUMBER

			return isValidPhoneNumber(declinedValue) && isValidPhoneNumber(approvedValue);
		case "4.70":

			return isValidCardExpiry(declinedValue) && isValidCardExpiry(approvedValue);
		case "4.44":
			return isValidField444(declinedValue) && isValidField444(approvedValue);
		case "5.1": // INVOICE NUMBER

			return isValidInvoiceOrReference(declinedValue) && isValidInvoiceOrReference(approvedValue);

		case "5.3": // REFERENCE NUMBER

			return isValidReferenceNumber(declinedValue) && isValidReferenceNumber(approvedValue);
		case "11.4":
			return isValidDateTime(declinedValue) && isValidDateTime(approvedValue);
		case "12.96":
			return isValidField1296(declinedValue) && isValidField1296(approvedValue);
		case "2.4.1":
			return isValidIPv4(declinedValue) && isValidIPv4(approvedValue);
		case "4.2":
			return declinedValue.matches("^\\d{6}$") && approvedValue.matches("^\\d{6}$");
		case "6.9":
			Pattern zipPattern = Pattern.compile("^[0-9A-Za-z ]{1,10}$");
			return zipPattern.matcher(declinedValue).matches() && zipPattern.matcher(approvedValue).matches();

		default:
			String approvedType = getType(approvedValue);
			String declinedType = getType(declinedValue);
			return approvedType.equals(declinedType) && approvedValue.length() == declinedValue.length();
		}
	}

	private boolean isValidPhoneNumber(String value) {

		if (value == null || value.trim().isEmpty()) {
			return false;
		}

		return value.matches("^\\+\\d{10,15}$");
	}

	private boolean isValidReferenceNumber(String value) {

		return value != null && (value.matches("^[A-Z]\\d{9}$") || value.matches("^Token-\\d+$"));
	}

	private boolean isValidInvoiceOrReference(String value) {

		return value != null && value.matches("^[A-Z]\\d{9}$");
	}

	private boolean isValidCardExpiry(String value) {

		if (value == null || !value.matches("^\\d{4}$")) {
			return false;
		}

		int month = Integer.parseInt(value.substring(0, 2));

		return month >= 1 && month <= 12;
	}

	private boolean isValidField1296(String value) {
		return value != null && value.matches("^W\\d{9}$");
	}

	private boolean isValidIPv4(String value) {
		String ipPattern = "^((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)\\.){3}" + "(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)$";
		return value != null && value.matches(ipPattern);
	}

	private boolean isValidDateTime(String value) {
		try {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMddyyyyHHmmss");
			LocalDateTime.parse(value, formatter);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private boolean isValidField121(String value) {
		return value != null && value
				.matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}-\\d{10}$");
	}

	private boolean isValidAmount(String value) {

		if (value == null || value.trim().isEmpty()) {
			return false;
		}

		return value.matches("^\\d+\\.\\d{2}$");
	}

	private boolean isValidField444(String value) {
		return value != null && value.matches("^3\\d{31}$");
	}

	private boolean isValidItemDetailList(String value) {
		return value != null && value.matches("^\\d+%1F\\d+%1F.*%1D$");
	}

	private static String sanitizeJson(String json) {
		return json.replaceAll(",\\s*([}\\]])", "$1");
	}

	private boolean isValidYYYYMMDD(String value) {
		try {
			java.time.LocalDate.parse(value, java.time.format.DateTimeFormatter.BASIC_ISO_DATE);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private String getPattern(String type) {
		if (type == null)
			return "N/A";
		switch (type) {
		case "NUMERIC":
			return "\\d+";
		case "ALPHA":
			return "[A-Za-z ]+";
		case "ALPHA_NUMERIC":
			return "[A-Za-z0-9._\\- ]+";
		case "IP":
			return "^((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)\\.){3}" + "(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)$";
		default:
			return "N/A";
		}
	}

	private static String getType(String value) {
		if (value == null || MISSING.equals(value))
			return "UNKNOWN";
		if (value.matches("^[0-9]+$"))
			return "NUMERIC";
		if (value.matches("^[a-zA-Z]+$"))
			return "ALPHA";
		if (value.matches("^[a-zA-Z0-9]+$"))
			return "ALPHANUMERIC";
		return "SPECIAL";
	}

	private static String getReason(String dVal, String aVal) {
		if (MISSING.equals(dVal) || MISSING.equals(aVal))
			return "Field missing in one transaction";
		if (!getType(dVal).equals(getType(aVal)))
			return "Data type mismatch (" + getType(dVal) + " vs " + getType(aVal) + ")";
		if (dVal.length() != aVal.length())
			return "Length mismatch (" + dVal.length() + " vs " + aVal.length() + ")";
		return "Value mismatch";
	}
}