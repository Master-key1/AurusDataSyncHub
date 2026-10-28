package com.auruspay.comparator;

import com.auruspay.comparator.config.IsoFieldDefinitionLoader;
import com.auruspay.comparator.model.IsoFieldDefinition;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CctComparator {

	// FIX: was referencing CctComparator.class (wrong class) instead of this class
	private static final Logger LOGGER = Logger.getLogger(CctComparator.class.getName());

	private static final ObjectMapper mapper = new ObjectMapper();

	// FIX: this was used in compare() but never declared/injected -> compile error.
	// Replace "IsoFieldDefinitionLoader" with whatever your actual loader class is
	// named.
	@Autowired
	private IsoFieldDefinitionLoader definitionLoader;

	private static final Set<String> VERSION_FIELDS = Set.of("6.1", "4.4", "6.3", "6.4", "6.5", "6.6", "6.30", "6.32");

	public List<Map<String, String>> compare(String declinedJson, String approvedJson) throws Exception {

		if (declinedJson == null || approvedJson == null) {
			throw new IllegalArgumentException("declinedJson and approvedJson must not be null");
		}

		// FIX: sanitize BOTH inputs (only declinedJson was sanitized before),
		// and also strip trailing commas before "]" not just "}"
		declinedJson = sanitizeJson(declinedJson);
		approvedJson = sanitizeJson(approvedJson);

		LOGGER.info("Starting CCT comparison");

		JsonNode declined = mapper.readTree(declinedJson);
		JsonNode approved = mapper.readTree(approvedJson);

		Set<String> fields = new TreeSet<>();
		declined.fieldNames().forEachRemaining(fields::add);
		approved.fieldNames().forEachRemaining(fields::add);

		

		List<Map<String, String>> matchIssue = new ArrayList<>();
		List<Map<String, String>> issues = new ArrayList<>();

		List<Map<String, String>> missMatchIssue = new ArrayList<>();
		List<Map<String, String>> skippedIssue = new ArrayList<>();
		for (String field : fields) {

			if (VERSION_FIELDS.contains(field)) {
				// Still record that we saw it, just skip detailed validation,
				// so callers get a consistent, auditable set of rows.
				Map<String, String> versionRow = new LinkedHashMap<>();
				versionRow.put("field", field);
				versionRow.put("fieldName", "VERSION FIELD (validation skipped)");
				versionRow.put("declinedValue", declined.has(field) ? declined.get(field).asText() : "MISSING");
				versionRow.put("approvedValue", approved.has(field) ? approved.get(field).asText() : "MISSING");
				versionRow.put("comparisonResult", "SKIPPED");
				skippedIssue.add(versionRow);
				continue;
			}

			String dVal = declined.has(field) ? declined.get(field).asText() : "MISSING";

			String aVal = approved.has(field) ? approved.get(field).asText() : "MISSING";

			IsoFieldDefinition definition = definitionLoader.getField(field);
			boolean matched = Objects.equals(dVal, aVal);

			Map<String, String> issue = new LinkedHashMap<>();
			issue.put("field", field);
			issue.put("fieldName", definition != null ? definition.getName() : "UNKNOWN FIELD");
			issue.put("declinedValue", dVal);
			issue.put("approvedValue", aVal);
			issue.put("comparisonResult", matched ? "MATCHED" : "MISMATCH");

			// FIX: always populate these keys (even when definition is null),
			// so every row has the same schema for downstream consumers (CSV/table/etc).
			// issue.put("expectedType", definition != null ? definition.getClassType() :
			// "N/A");
			issue.put("expectedMinLength", definition != null ? String.valueOf(definition.getMinLength()) : "N/A");
			issue.put("expectedMaxLength", definition != null ? String.valueOf(definition.getMaxLength()) : "N/A");
			issue.put("expectedPattern", definition != null ? getPattern(definition.getClassType()) : "N/A");
			issue.put("declinedLength", "MISSING".equals(dVal) ? "0" : String.valueOf(dVal.length()));
			issue.put("approvedLength", "MISSING".equals(aVal) ? "0" : String.valueOf(aVal.length()));

			// FIX: reason was computed by getReason() but never actually attached before.
			issue.put("reason", matched ? "Expected Value" : getReason(dVal, aVal));

			Set<String> patternFields = Set.of("11.1", "4.4", "4.18");
			String reason = issue.get("reason");

			if ( "Value mismatch".equals(reason)) {

			
				if (patternFields.contains(field)) {

					boolean patternMatch = isPatternMatched(field, dVal, aVal);

					System.out.println("Field: " + field + " Pattern Match: " + patternMatch);

					String result = patternMatch ? "MATCHED" : "MISMATCH";
					issue.put("patternMatch",result);
					if (patternMatch) {
						
						missMatchIssue.add(issue);
					}
				}
				//issues.add(issue);

			}else if ("Expected Value".equals(reason)) {
				matchIssue.add(issue);
			}else {
				issues.add(issue);
			}

		}

		// LOGGER.info("Comparison completed. Total fields checked: " + issues.size());
		
		Map<String, List<Map<String, String>>> issuer = new LinkedHashMap<>();

		issuer.put("skippedIssue", skippedIssue);
		issuer.put("matchIssue", matchIssue);
		issuer.put("missMatchIssue", missMatchIssue);
		
		for (Map.Entry<String, List<Map<String, String>>> entry : issuer.entrySet()) {

		    System.out.println("\n===== " + entry.getKey() + " =====");

		    for (Map<String, String> issue : entry.getValue()) {
		        System.out.println(issue);
		    }
		}
		

		return issues;
	}

	private boolean isPatternMatched(String field, String declinedValue, String approvedValue) {

		

		if (declinedValue == null || approvedValue == null) {
			return false;
		}

		switch (field) {

// REQUEST ID
		case "11.1":

			String uuidPattern = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";

			return declinedValue.matches(uuidPattern) && approvedValue.matches(uuidPattern);

// SERVER IP ADDRESS
		case "11.2":

		case "5.5":

			String ipPattern = "^((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)\\.){3}"
					+ "(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)$";

			return declinedValue.matches(ipPattern) && approvedValue.matches(ipPattern);

// TRANSACTION DATE (yyyyMMdd)
		case "4.18":

			return declinedValue.matches("^\\d{8}$") && approvedValue.matches("^\\d{8}$");

// TRANSACTION TIME (HHmmss)
		case "4.19":

			return declinedValue.matches("^\\d{6}$") && approvedValue.matches("^\\d{6}$");

// TOKEN FIELDS
		case "12.1":
		case "5.3":

			return declinedValue.matches("^Token-\\d+$") && approvedValue.matches("^Token-\\d+$");

// TIMESTAMP
		case "11.5":

			return declinedValue.matches("^\\d{13}$") && approvedValue.matches("^\\d{13}$");

// OTT
		case "4.44":

			return declinedValue.matches("^\\d{32}$") && approvedValue.matches("^\\d{32}$");

		default:

// Generic fallback:
// same type and same length

			String approvedType = getType(approvedValue);
			String declinedType = getType(declinedValue);

			return approvedType.equals(declinedType) && approvedValue.length() == declinedValue.length();
		}
	}

	/**
	 * Strips trailing commas before a closing "}" or "]" so mildly malformed JSON
	 * (e.g. {"a":"1","b":"2",}) doesn't fail to parse.
	 */
	private static String sanitizeJson(String json) {
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
			return "^((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)\\.){3}(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)$";

		default:
			return "N/A";
		}
	}

	private static String getType(String value) {

		if (value == null || value.equals("MISSING")) {
			return "UNKNOWN";
		}

		if (value.matches("^[0-9]+$")) {
			return "NUMERIC";
		}

		if (value.matches("^[a-zA-Z]+$")) {
			return "ALPHA";
		}

		if (value.matches("^[a-zA-Z0-9]+$")) {
			return "ALPHANUMERIC";
		}

		return "SPECIAL";
	}

	private static String getReason(String dVal, String aVal) {

		if ("MISSING".equals(dVal) || "MISSING".equals(aVal)) {
			return "Field missing in one transaction";
		}
		if (!getType(dVal).equals(getType(aVal))) {
			return "Data type mismatch (" + getType(dVal) + " vs " + getType(aVal) + ")";
		}

		if (dVal.length() != aVal.length()) {
			return "Length mismatch (" + dVal.length() + " vs " + aVal.length() + ")";
		}

		return "Value mismatch";
	}
}