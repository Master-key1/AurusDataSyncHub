package com.auruspay.fiserv.comparator;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.auruspay.AurusDataSyncHubApplication;
import com.auruspay.fiserv.comparator.model.ComparisionXmlResult;
import com.auruspay.fiserv.comparator.model.EMVComparisonResult;
import com.auruspay.fiserv.comparator.model.EmvValidationSummary;
import com.auruspay.fiserv.comparator.model.ValidationResults;
import com.auruspay.fiserv.comparator.service.FieldValidators;
import com.auruspay.fiserv.comparator.validation.DTOMapper;
import com.auruspay.fiserv.comparator.validation.TransactionRuleEngine;
import com.auruspay.fiserv.dto.TransactionContext;
import com.auruspay.fiserv.service.ServiceProvider;

@Service
public class XmlComparators {

	private final AurusDataSyncHubApplication aurusDataSyncHubApplication;

	@Autowired
	ComparisionXmlResult comparisionXmlResult;
	@Autowired
	TransactionRuleEngine engine;
	@Autowired
	ServiceProvider serviceProvider;

	private final Set<String> mandateField = Set.of("TPPID", "TermID", "MerchID", "GroupID", "Auth", "DID", "TxnCrncy",
			"MerchCatCode", "POSEntryMode", "POSCondCode", "PartAuthrztnApprvlCapablt", "PymtType", "TknType",
			"TermCatCode");

	private static final Logger log = LoggerFactory.getLogger(XmlComparators.class);

	// Comprehensive list of sensitive fields requiring masking
	private static final Set<String> SENSITIVE_FIELDS = Set.of("Track2Data1", "PAN", "CVV", "CVV2", "Cvv2", "PIN",
			"PINBlock", "EncryptedPAN");

	@Autowired
	private FieldValidators fieldValidators;

	private final DocumentBuilderFactory documentBuilderFactory;

	public XmlComparators(AurusDataSyncHubApplication aurusDataSyncHubApplication) {
		this.documentBuilderFactory = createSecureFactory();
		this.aurusDataSyncHubApplication = aurusDataSyncHubApplication;
	}

	private DocumentBuilderFactory createSecureFactory() {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
			factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
			factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
			factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		} catch (ParserConfigurationException e) {
			log.warn("Underlying XML parser does not support an expected XXE-hardening feature", e);
		}
		factory.setXIncludeAware(false);
		factory.setExpandEntityReferences(false);
		factory.setNamespaceAware(true);
		return factory;
	}

	public ComparisionXmlResult getXmlComparator(String approvedXml, String declinedXml) {

		if (declinedXml == null || declinedXml.isBlank() || declinedXml.isEmpty()) {
			return new ComparisionXmlResult();
		}
		if (approvedXml == null || approvedXml.isBlank() || approvedXml.isEmpty()) {
			return new ComparisionXmlResult();
		}

		// log.info("approvedXml : {}", approvedXml);
		// log.info("declinedXml : {}", declinedXml);

		Map<String, String> approved = extractAll(approvedXml);
		Map<String, String> declined = extractAll(declinedXml);

		// Now receiving the categorized map
		ComparisionXmlResult results = smartXmlCompare(approved, declined);

		TransactionContext approvedContext = DTOMapper.mapToDTO(approved, TransactionContext.class);
		TransactionContext declineContext = DTOMapper.mapToDTO(declined, TransactionContext.class);

		serviceProvider.setTransactionContext(approvedContext);
		log.info("approvedContext EMVDATA :  {} ", approvedContext.getEmvData());
		serviceProvider.setTransactionContext(declineContext);
		log.info("declineContext EMVDATA :  {} ", declineContext.getEmvData());
		
		if(approvedContext.getEmvData() !=null &&  declineContext.getEmvData() !=null) {

		Map<String, String> approvedEmv = serviceProvider.getEmvParser().parseToMap(approvedContext.getEmvData());
		Map<String, String> declinedEmv = serviceProvider.getEmvParser().parseToMap(declineContext.getEmvData());

		// System.out.println("--------------------");
		// approvedEmv.forEach((k, v) -> System.out.println(k + " = " + v));
		// log.info("----------------------------------------------------------------------");
		// declinedEmv.forEach((k, v) -> System.out.println(k + " = " + v));
		log.info("----------------------------------------------------------------------");

		// GMF PymtType/TxnType drive which EMV tags are mandatory and how the
		// comparator's Reason text describes them (Credit / Refund / etc.)
		String pymtType = approved.getOrDefault("PymtType", declined.get("PymtType"));
		String txnType = approved.getOrDefault("TxnType", declined.get("TxnType"));

		if (!(approvedEmv.isEmpty() && declinedEmv.isEmpty())) {
			Map<String, EMVComparisonResult> comparison = serviceProvider.getAdvanceEmvcomparator().compare(approvedEmv,
					declinedEmv, pymtType, txnType);

			// System.out.println("========== MATCHED EMV TAGS ==========");
			// comparison.values().stream().filter(r ->
			// "MATCH".equals(r.status())).forEach(r -> System.out.println("Tag: " + r.tag()
			// + " | Approved: " + r.approvedValue() + " | Declined: " +
			// r.declinedValue()));

			// .out.println("========== MATCHED EMV TAGS ==========");
			// comparison.values().stream().filter(r ->
			// "MATCH".equals(r.status())).forEach(r -> System.out.println("Tag: " + r.tag()
			// + " | Approved: " + r.approvedValue() + " | Declined: " +
			// r.declinedValue()));

			// System.out.println("\n========== VALIDATION ISSUES ==========");
			// comparison.values().stream().filter(r ->
			// !"MATCH".equals(r.status())).forEach(r -> System.out.println("Tag: " +
			// r.tag() + " | Status: " + r.status() + " | Approved: "+ r.approvedValue() + "
			// | Declined: " + r.declinedValue()));

			// Root-cause analysis: groups mismatches into "Potential Root Cause"
			// (transaction-defining + verification tags) and "Configuration /
			// Application Differences" (card/terminal identity tags), and prints
			// exactly where the issue is.
			String rootCauseReport = serviceProvider.getEmvRootCauseReport().generate(approvedEmv, declinedEmv);
			// System.out.println("\n" + rootCauseReport);
			log.info("EMV root cause report:\n{}", rootCauseReport);

			// Roll-up verdict (issue found or not, plus counts per category) -
			// also prints its own structured summary.
			EmvValidationSummary emvValidationSummary = serviceProvider.getAdvanceEmvcomparator().summarize(approvedEmv,
					declinedEmv, pymtType, txnType);

			// Attach all EMV results to the response so callers get them via the API too
			results.setEmvTagComparison(comparison);
			// results.setEmvValidationSummary(emvValidationSummary);
			// results.setEmvRootCauseReport(rootCauseReport);
		}

		}
		return results;
	}

	private Map<String, String> extractAll(String xml) {

		Map<String, String> map = new LinkedHashMap<>();

		if (xml == null || xml.isBlank()) {
			return map;
		}

		String sanitized = sanitizeXml(xml);

		try {

			DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();

			Document doc = builder.parse(new ByteArrayInputStream(sanitized.getBytes(StandardCharsets.UTF_8)));

			doc.getDocumentElement().normalize();

			traverse(doc.getDocumentElement(), map);

		} catch (ParserConfigurationException | SAXException | IOException e) {

			log.error("XML parsing failed: {}", e.getMessage(), e);

			map.put("PARSE_ERROR", "Unable to parse XML: " + e.getMessage());
		}

		return map;
	}

	private String sanitizeXml(String xml) {

		if (xml == null || xml.isBlank()) {
			return "";	
		}

		// Remove BOM and leading/trailing whitespace
		String sanitized = xml.replace("\uFEFF", "").strip();

		// Find all XML declarations
		Pattern pattern = Pattern.compile("<\\?xml\\s+[^?]*\\?>", Pattern.CASE_INSENSITIVE);

		Matcher matcher = pattern.matcher(sanitized);

		StringBuilder result = new StringBuilder();

		int lastPosition = 0;
		boolean firstDeclaration = true;

		while (matcher.find()) {

			// Append content before the declaration
			result.append(sanitized, lastPosition, matcher.start());

			// Retain only the first XML declaration
			if (firstDeclaration) {
				result.append(matcher.group());
				firstDeclaration = false;
			}

			lastPosition = matcher.end();
		}

		// Append remaining XML content
		result.append(sanitized.substring(lastPosition));

		return result.toString().strip();
	}

	private void traverse(Node node, Map<String, String> map) {
		NodeList children = node.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			if (child.getNodeType() != Node.ELEMENT_NODE)
				continue;
			if (hasElementChild(child)) {
				traverse(child, map);
			} else {
				String fieldName = child.getLocalName() != null ? child.getLocalName() : child.getNodeName();
				map.put(fieldName, child.getTextContent().trim());
			}
		}
	}

	private boolean hasElementChild(Node node) {
		NodeList children = node.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			if (children.item(i).getNodeType() == Node.ELEMENT_NODE)
				return true;
		}
		return false;
	}

	public ComparisionXmlResult smartXmlCompare(Map<String, String> approved, Map<String, String> declined) {
		List<Map<String, String>> matchedList = new ArrayList<>();
		List<Map<String, String>> mismatchList = new ArrayList<>();
		List<Map<String, String>> validationIssueList = new ArrayList<>();

		log.info("smartXmlCompare: *****************");

		Set<String> skippedVersionFields = Set.of(
				"App", "ClientRef", "EMVData", "LocalDateTime", "MerchEcho", "Auth", "TxnLinkID",
				"TrnmsnDateTime", "STAN", "Track2Data", "TxnAmt", "ServiceID", "RefNum",
				"CardSeqNum", "PINData", "AcctNum", "CCVData", "CardExpiryDate", "OrderNum");

		Set<String> mustVersionFields = Set.of(
				 "Track2Data", "DID", "MSKeyID", "PymtType", "TxnType", "TPPID",
				"POSEntryMode", "POSCondCode", "GroupID", "SettleInd", "CardType",
				"PartAuthrztnApprvlCapablt", "EcommTxnInd", "FinAuthInd");

		if (approved == null) {
			approved = Map.of();
		}
		if (declined == null) {
			declined = Map.of();
		}

		// Only fields found in at least one XML are considered.
		// Therefore, a field missing from both XMLs is automatically skipped.
		Set<String> fields = new TreeSet<>(approved.keySet());
		fields.addAll(declined.keySet());

		String approvedPosEntryMode = approved.get("POSEntryMode");
		String declinedPosEntryMode = declined.get("POSEntryMode");
		boolean approvedEmvTransaction = "071".equals(approvedPosEntryMode)
				|| "051".equals(approvedPosEntryMode);
		boolean declinedEmvTransaction = "071".equals(declinedPosEntryMode)
				|| "051".equals(declinedPosEntryMode);

		for (String field : fields) {

			String approvedValue = approved.get(field);
			String declinedValue = declined.get(field);

			boolean approvedPresent = approvedValue != null && !approvedValue.isBlank();
			boolean declinedPresent = declinedValue != null && !declinedValue.isBlank();

			// Both missing/blank: do not validate or report this field.
			if (!approvedPresent && !declinedPresent) {
				log.debug("Skipping field missing in both XMLs: {}", field);
				continue;
			}

			String valA = approvedPresent ? approvedValue : "Tag Missing";
			String valD = declinedPresent ? declinedValue : "Tag Missing";

			// If only one XML contains the field, always highlight it as a validation issue.
			// This check is deliberately before the skipped-field check so one-sided
			// missing fields are not hidden by the version-specific skip list.
			if (!approvedPresent || !declinedPresent) {

				Map<String, String> row = new LinkedHashMap<>();
				row.put("FIELD", field);
				row.put("ApprovedValue", approvedPresent ? maskIfSensitive(field, valA) : "Tag Missing");
				row.put("DeclinedValue", declinedPresent ? maskIfSensitive(field, valD) : "Tag Missing");
				row.put("VALUE", "MISMATCH");
				row.put("PATTERN", "NOT_MATCHED");

				if ("EMVData".equals(field) && (approvedEmvTransaction || declinedEmvTransaction)) {
					if (!approvedPresent && !declinedPresent) {
						// Defensive only; the both-missing check above normally handles this.
						row.put("Reason", "EMVData is missing from both XML requests");
					} else if (!approvedPresent) {
						row.put("Reason", "EMVData must be present in Approved XML for EMV transactions (POSEntryMode = 071/051)");
					} else {
						row.put("Reason", "EMVData must be present in Declined XML for EMV transactions (POSEntryMode = 071/051)");
					}
				} else if (!approvedPresent) {
					row.put("Reason", "Field missing in Approved XML");
				} else {
					row.put("Reason", "Field missing in Declined XML");
				}

				if (mandateField.contains(field.trim()) || mustVersionFields.contains(field)) {
					row.put("REASON", "INVALID");
				}

				validationIssueList.add(row);
				log.warn("Field missing on one side. Field: {}, Approved present: {}, Declined present: {}",
						field, approvedPresent, declinedPresent);
				continue;
			}

			// Both sides contain a value. Skip version-specific fields unless mandatory.
			if (skippedVersionFields.contains(field) && !mustVersionFields.contains(field)) {
				log.debug("Skipping version-specific field: {}", field);
				continue;
			}

			ValidationResults result = fieldValidators.validate(
					field, valA, field, valD, approved, declined);

			if (result == null) {
				log.warn("Validation result is null for field: {}", field);
				continue;
			}

			log.info("Field: {}, Approved Value: {}, Declined Value: {}, Result: {}",
					field, maskIfSensitive(field, valA), maskIfSensitive(field, valD), result);

			Map<String, String> row = new LinkedHashMap<>();
			row.put("FIELD", field);
			row.put("ApprovedValue", maskIfSensitive(field, valA));
			row.put("DeclinedValue", maskIfSensitive(field, valD));
			row.put("VALUE", result.getValue());
			row.put("Reason", result.getReason());
			row.put("PATTERN", result.getPattern());

			boolean isMandate = mandateField.contains(field.trim()) || mustVersionFields.contains(field);
			if (isMandate) {
				row.put("REASON", "MATCH".equals(result.getValue()) ? "VALID" : "INVALID");
			}

			boolean valueMatch = "MATCH".equalsIgnoreCase(result.getValue());
			boolean patternMatched = "MATCHED".equalsIgnoreCase(result.getPattern());

			/*
			 * Categorize by pattern validation first:
			 * 1. Pattern MATCHED + values MATCH     -> matchedList
			 * 2. Pattern MATCHED + values differ   -> mismatchList
			 * 3. Pattern NOT MATCHED / invalid     -> validationIssueList
			 *
			 * A mandatory field with a normal value difference is still a
			 * comparison mismatch. It must not be moved to validationIssueList
			 * merely because it is mandatory.
			 */
			if (patternMatched) {
				if (valueMatch) {
					matchedList.add(row);
				} else {
					mismatchList.add(row);
				}
			} else {
				validationIssueList.add(row);
			}
		}

		comparisionXmlResult.setXmlValidationIssue(validationIssueList);
		// Uncomment if the response model exposes a matched-fields property.
		// comparisionXmlResult.setXmlMatchIssue(matchedList);
		comparisionXmlResult.setXmlMissMatchIssue(mismatchList);

		log.info("XML comparison completed. Matched: {}, Mismatched: {}, Validation issues: {}",
				matchedList.size(), mismatchList.size(), validationIssueList.size());
		log.debug("Mismatch list: {}", mismatchList);
		log.debug("Validation issue list: {}", validationIssueList);

		return comparisionXmlResult;
	}

	/**
	 * Masks sensitive cardholder data while preserving format for Track2Data
	 */
	private String maskIfSensitive(String field, String value) {
		if (!SENSITIVE_FIELDS.contains(field) || value == null || value.length() < 6)
			return value;
		if ("TAG MISSING".equals(value) || "EMPTY VALUE".equals(value))
			return value;

		// Specialized handling for Track2Data:
		// PAN=ExpiryYYMM[ServiceCode][DiscretionaryData]
		if (field.contains("Track2")) {
			int sep = value.indexOf('=');
			if (sep == -1)
				return "******";
			String pan = value.substring(0, sep);
			String rest = value.substring(sep);
			return pan.substring(0, 6) + "******" + pan.substring(pan.length() - 4) + rest.replaceAll("\\d", "*");
		}

		// Standard PAN masking
		return value.substring(0, 6) + "*".repeat(Math.max(0, value.length() - 10))
				+ value.substring(value.length() - 4);
	}
}