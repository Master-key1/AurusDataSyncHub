package com.auruspay.comparator;

import com.auruspay.AurusDataSyncHubApplication;
import com.auruspay.comparator.model.ComparisionXmlResult;
import com.auruspay.comparator.model.EMVComparisonResult;
import com.auruspay.comparator.model.ValidationResult;
import com.auruspay.comparator.model.ValidationResults;
import com.auruspay.comparator.service.FieldValidators;
import com.auruspay.comparator.validation.DTOMapper;
import com.auruspay.comparator.validation.FieldValidator;
import com.auruspay.comparator.validation.TransactionRuleEngine;
import com.auruspay.dto.TransactionContext;
import com.auruspay.service.ServiceProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class XmlComparator {

    private final AurusDataSyncHubApplication aurusDataSyncHubApplication;
	
	@Autowired
	ComparisionXmlResult comparisionXmlResult ;
	@Autowired
	TransactionRuleEngine engine;
	@Autowired
	ServiceProvider serviceProvider;
	private final Set<String> mandateField = Set.of("TPPID","TermID","MerchID","GroupID","Auth","DID","POSEntryMode","POSCondCode","PartAuthrztnApprvlCapablt",
			"PymtType","TknType"
			);

	private static final Logger log = LoggerFactory.getLogger(XmlComparator.class);

	// Comprehensive list of sensitive fields requiring masking
	private static final Set<String> SENSITIVE_FIELDS = Set.of("Track2Data1", "PAN", "CVV", "CVV2", "Cvv2", "PIN",
			"PINBlock", "EncryptedPAN");

	@Autowired
	private FieldValidator fieldValidator;
	@Autowired
	private FieldValidators fieldValidators;

	private final DocumentBuilderFactory documentBuilderFactory;

	public XmlComparator(AurusDataSyncHubApplication aurusDataSyncHubApplication) {
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
		
		if (declinedXml == null || declinedXml.isBlank()||declinedXml.isEmpty()) {
            return new ComparisionXmlResult();
        }
		if (approvedXml == null || approvedXml.isBlank()||approvedXml.isEmpty()) {
            return new ComparisionXmlResult();
        }
		
		  log.info("approvedXml : {}",approvedXml);
	        log.info("declinedXml : {}",declinedXml);
		
		Map<String, String> approved = extractAll(approvedXml);
		Map<String, String> declined = extractAll(declinedXml);
		
		
		// Now receiving the categorized map
	 ComparisionXmlResult results = smartCompare(approved, declined);
	// declined.forEach((k, v) -> System.out.println(k + " = " + v));

	  TransactionContext approvedContext = DTOMapper.mapToDTO(approved, TransactionContext.class);
	  TransactionContext declineContext = DTOMapper.mapToDTO(declined, TransactionContext.class);
	// TransactionContext
	 serviceProvider.setTransactionContext(approvedContext);
		 log.info("approvedContext EMVDATA :  {} ", approvedContext.getEmvData());
		 serviceProvider.setTransactionContext(declineContext);
		 log.info("declineContext EMVDATA :  {} ", declineContext.getEmvData());
		//String signature = engine.validateAllRules(context);

		Map<String, String> approvedEmv = serviceProvider.getEmvParser().parseToMap(approvedContext.getEmvData());
		Map<String, String> declinedEmv = serviceProvider.getEmvParser().parseToMap( declineContext.getEmvData());
		System.out.println("--------------------");
		approvedEmv.forEach((k, v) -> System.out.println(k + " = " + v));
		 log.info("----------------------------------------------------------------------");
		declinedEmv.forEach((k, v) -> System.out.println(k + " = " + v));
		 log.info("----------------------------------------------------------------------");
		 Map<String, EMVComparisonResult> comparison =
			        serviceProvider.getEmvComparator().compare(approvedEmv, declinedEmv);

			System.out.println("========== MATCHED EMV TAGS ==========");

			comparison.values().stream()
			        .filter(r -> "MATCH".equals(r.status()))
			        .forEach(r -> System.out.println(
			                "Tag: " + r.tag()
			                + " | Approved: " + r.approvedValue()
			                + " | Declined: " + r.declinedValue()));

			System.out.println("\n========== VALIDATION ISSUES ==========");

			comparison.values().stream()
			        .filter(r -> !"MATCH".equals(r.status()))
			        .forEach(r -> System.out.println(
			                "Tag: " + r.tag()
			                + " | Status: " + r.status()
			                + " | Approved: " + r.approvedValue()
			                + " | Declined: " + r.declinedValue()));
		 

		 //System.out.println( context.toString());
		//results.setSummary(signature);
	//	System.out.println("Summary: "+signature);
		return results;
	}

	private Map<String, String> extractAll(String xml) {
		Map<String, String> map = new LinkedHashMap<>();
		if (xml == null || xml.isBlank())
			return map;

		String sanitized = sanitizeXml(xml);
		try {
			DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
			Document doc = builder.parse(new ByteArrayInputStream(sanitized.getBytes(StandardCharsets.UTF_8)));
			doc.getDocumentElement().normalize();
			traverse(doc.getDocumentElement(), map);
		} catch (ParserConfigurationException | SAXException | IOException e) {
			log.warn("Failed to parse XML payload: {}", e.getMessage());
			map.put("PARSE_ERROR", "Unable to parse XML: " + e.getMessage());
		}
		return map;
	}

	private String sanitizeXml(String xml) {
		String sanitized = xml.startsWith("\uFEFF") ? xml.substring(1) : xml;
		sanitized = sanitized.stripLeading();
		int firstTag = sanitized.indexOf('<');
		return (firstTag > 0) ? sanitized.substring(firstTag) : sanitized;
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

	public ComparisionXmlResult smartCompare(Map<String, String> approved,Map<String, String> declined) {
		// Separate lists to hold the rows
		List<Map<String, String>> matchedList = new ArrayList<>();
		List<Map<String, String>> mismatchList = new ArrayList<>();
		List<Map<String, String>> validationIssueList = new ArrayList<>();
		

		Set<String> fields = new TreeSet<>(approved.keySet());
		fields.addAll(declined.keySet());

		for (String field : fields) {

		    String valA = approved.getOrDefault(field, "TAG MISSING");
		    String valD = declined.getOrDefault(field, "TAG MISSING");

		    ValidationResult resA = fieldValidator.validate(field, valA);
		    ValidationResult resD = fieldValidator.validate(field, valD);
		    
		  ValidationResults result = fieldValidators.validate(field, valA,field, valD);

		  //  System.out.println("ValidationResult : " + result);
		    Map<String, String> row = new LinkedHashMap<>();
		    row.put("field", field);
		    row.put("aVal", maskIfSensitive(field, valA));
		    row.put("dVal", maskIfSensitive(field, valD));

		    boolean isMandate = mandateField.contains(field.trim());

		    if (isMandate) {
		     //   System.out.println("Mandatory Field: " + field);

		        // optional: check if both exist
		        if (!valD.equals(valA) ) {
		            row.put("value status", "ValueMisMatch");
		        } else {
		            row.put("value status", "MISSING");
		        }

		    } else {
		        row.put("value status",
		                valA.equals(valD) ? "MATCHED" : "MISMATCH");
		    }
		
			// Inside your for-loop in smartCompare:
			// The logic you already have for 'Reason' is sufficient:
			if (resA.reason().startsWith("Valid") && resD.reason().startsWith("Valid")) {
			    row.put("Reason", resA.reason());
			} else if (resA.reason().startsWith("Valid") && !resD.reason().startsWith("Valid")) {
			    row.put("Reason", resD.reason());
			} else if (!resA.reason().startsWith("Valid") && resD.reason().startsWith("Valid")) {
			    row.put("Reason", resA.reason());
			} else {
			    // This handles the case where both are invalid, concatenating the reasons
				if(resA.reason().equals(resD.reason()))
			    row.put("Reason", resA.reason() );
				else
				 row.put("Reason", resA.reason() + " | " + resD.reason());
			}
			
			 if (isMandate) {
				 row.put("Reason","Invalid "+field);
			 }

			if (resA.status().equals(resD.status()))
				row.put("matchPattern", "MATCHED");
			else
				row.put("matchPattern", "MISMATCH");

			// Categorize
			if (resA.status().equals(resD.status()) && valA.equals(valD)) {
				matchedList.add(row);
			} else if (resA.status().equals(resD.status()) && resA.status().equals("VALID") && !isMandate) {
				mismatchList.add(row);
			} else if(isMandate) {
				validationIssueList.add(row);
			}else {
				validationIssueList.add(row);
			}
		}
		// Return both lists in a map for easy access
		Map<String, List<Map<String, String>>> result = new LinkedHashMap<>();
		result.put("ValueMATCHED", matchedList);
		result.put("ValueMISMATCH", mismatchList);
		result.put("MandateField", validationIssueList);
		
		
		
		comparisionXmlResult.setXmlValidationIssue(validationIssueList);
		comparisionXmlResult.setXmlMatchIssue(matchedList);
		comparisionXmlResult.setXmlMissMatchIssue(mismatchList);
		

		System.out.println("-------------matchedList-----------------");

		matchedList.stream().forEach(System.out::println);
		System.out.println("--------------mismatchList----------------");
		mismatchList.stream().forEach(System.out::println);
		System.out.println("-------------validationIssueList-----------------");
		validationIssueList.stream().forEach(System.out::println);

		 log.info("comparisionXmlResult : {}",comparisionXmlResult);
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
