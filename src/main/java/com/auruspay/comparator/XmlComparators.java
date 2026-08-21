package com.auruspay.comparator;

import com.auruspay.AurusDataSyncHubApplication;
import com.auruspay.comparator.model.ComparisionXmlResult;
import com.auruspay.comparator.model.EMVComparisonResult;
import com.auruspay.comparator.model.EmvValidationSummary;
import com.auruspay.comparator.model.ValidationResults;
import com.auruspay.comparator.service.FieldValidators;
import com.auruspay.comparator.validation.DTOMapper;
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
public class XmlComparators {

    private final AurusDataSyncHubApplication aurusDataSyncHubApplication;

    @Autowired
    ComparisionXmlResult comparisionXmlResult;
    @Autowired
    TransactionRuleEngine engine;
    @Autowired
    ServiceProvider serviceProvider;

    private final Set<String> mandateField = Set.of("TPPID", "TermID", "MerchID", "GroupID", "Auth", "DID","TxnCrncy","MerchCatCode",
            "POSEntryMode", "POSCondCode", "PartAuthrztnApprvlCapablt", "PymtType", "TknType","TermCatCode");

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

      //  log.info("approvedXml : {}", approvedXml);
     //   log.info("declinedXml : {}", declinedXml);

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

        Map<String, String> approvedEmv = serviceProvider.getEmvParser().parseToMap(approvedContext.getEmvData());
        Map<String, String> declinedEmv = serviceProvider.getEmvParser().parseToMap(declineContext.getEmvData());

    //    System.out.println("--------------------");
      //  approvedEmv.forEach((k, v) -> System.out.println(k + " = " + v));
      //  log.info("----------------------------------------------------------------------");
      //  declinedEmv.forEach((k, v) -> System.out.println(k + " = " + v));
        log.info("----------------------------------------------------------------------");

        // GMF PymtType/TxnType drive which EMV tags are mandatory and how the
        // comparator's Reason text describes them (Credit / Refund / etc.)
        String pymtType = approved.getOrDefault("PymtType", declined.get("PymtType"));
        String txnType = approved.getOrDefault("TxnType", declined.get("TxnType"));

        if(!(approvedEmv.isEmpty()  && declinedEmv.isEmpty())) {
        Map<String, EMVComparisonResult> comparison = serviceProvider.getAdvanceEmvcomparator().compare(approvedEmv, declinedEmv, pymtType, txnType);

	//	System.out.println("========== MATCHED EMV TAGS ==========");
	//	comparison.values().stream().filter(r -> "MATCH".equals(r.status())).forEach(r -> System.out.println("Tag: " + r.tag() + " | Approved: " + r.approvedValue() + " | Declined: " + r.declinedValue()));

	//.out.println("========== MATCHED EMV TAGS ==========");
		//comparison.values().stream().filter(r -> "MATCH".equals(r.status())).forEach(r -> System.out.println("Tag: " + r.tag() + " | Approved: " + r.approvedValue() + " | Declined: " + r.declinedValue()));

		//System.out.println("\n========== VALIDATION ISSUES ==========");
	//	comparison.values().stream().filter(r -> !"MATCH".equals(r.status())).forEach(r -> System.out.println("Tag: " + r.tag() + " | Status: " + r.status() + " | Approved: "+ r.approvedValue() + " | Declined: " + r.declinedValue()));

        // Root-cause analysis: groups mismatches into "Potential Root Cause"
        // (transaction-defining + verification tags) and "Configuration /
        // Application Differences" (card/terminal identity tags), and prints
        // exactly where the issue is.
        String rootCauseReport = serviceProvider.getEmvRootCauseReport().generate(approvedEmv, declinedEmv);
      //  System.out.println("\n" + rootCauseReport);
        log.info("EMV root cause report:\n{}", rootCauseReport);

        // Roll-up verdict (issue found or not, plus counts per category) -
        // also prints its own structured summary.
        EmvValidationSummary emvValidationSummary = serviceProvider.getAdvanceEmvcomparator()
                .summarize(approvedEmv, declinedEmv, pymtType, txnType);

        // Attach all EMV results to the response so callers get them via the API too
     results.setEmvTagComparison(comparison);
     // results.setEmvValidationSummary(emvValidationSummary);
    //  results.setEmvRootCauseReport(rootCauseReport);

        }
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

    public ComparisionXmlResult smartXmlCompare(Map<String, String> approved, Map<String, String> declined) {
        List<Map<String, String>> matchedList = new ArrayList<>();
        List<Map<String, String>> mismatchList = new ArrayList<>();
        List<Map<String, String>> validationIssueList = new ArrayList<>();
        
        Set<String> Skipped_VERSION_FIELDS = Set.of(
        		"App","ClientRef","EMVData","LocalDateTime","MerchEcho","TrnmsnDateTime","STAN","Track2Data","TxnAmt", "ServiceID","RefNum","CardSeqNum","PINData"
        		
        		
        		);
        Set<String> MUST_VERSION_FIELDS = Set.of(
        		"RefNum","Track2Data","DID","MSKeyID"
        		
        		);

        Set<String> fields = new TreeSet<>(approved.keySet());
        fields.addAll(declined.keySet());

        for (String field : fields) {

            String valA = approved.getOrDefault(field, "Tag Missing");
            String valD = declined.getOrDefault(field, "Tag Missing");
            ValidationResults result =null;
            if(!List.of("AVSBillingAddr").contains(field)) {
            	continue;
            	// Single call replaces the old dual FieldValidator.validate(field, valA) / validate(field, valD)
            }
             result = fieldValidators.validate(field, valA, field, valD,approved,declined);
             

            Map<String, String> row = new LinkedHashMap<>();
            row.put("FIELD", field);
            row.put("ApprovedValue", maskIfSensitive(field, valA));
            row.put("DeclinedValue", maskIfSensitive(field, valD));

            boolean isMandate = mandateField.contains(field.trim());

            row.put("VALUE", result.getValue());

            // EMV-specific override: for EMV transactions (POSEntryMode = 071/051),
            // EMVData must be present regardless of what the generic field rule says.
            String declinedPosEntryMode = declined.get("POSEntryMode");
            String declinedEmvData = declined.get("EMVData");
            String approvedPosEntryMode = approved.get("POSEntryMode");
            String approvedEmvData = approved.get("EMVData");

            boolean approvedEmvInvalid =
                    ("071".equals(approvedPosEntryMode) || "051".equals(approvedPosEntryMode))
                            && (approvedEmvData == null || approvedEmvData.isBlank());

            boolean declinedEmvInvalid =
                    ("071".equals(declinedPosEntryMode) || "051".equals(declinedPosEntryMode))
                            && (declinedEmvData == null || declinedEmvData.isBlank());

            if (approvedEmvInvalid || declinedEmvInvalid) {

                if (approvedEmvInvalid && declinedEmvInvalid) {
                    row.put("Reason",
                            "EMVData must be present in both Approved and Declined requests for EMV transactions (POSEntryMode = 071/051)");
                } else if (approvedEmvInvalid) {
                    row.put("Reason",
                            "EMVData must be present in Approved request for EMV transactions (POSEntryMode = 071/051)");
                } else {
                    row.put("Reason",
                            "EMVData must be present in Declined request for EMV transactions (POSEntryMode = 071/051)");
                }

            } else {
                row.put("Reason", result.getReason());

                if (isMandate) {
                    row.put("REASON", "MATCH".equals(result.getValue()) ? "VALID" : "INVALID");
                }
            }

            row.put("PATTERN", result.getPattern());

            boolean valueMatch = "MATCH".equals(result.getValue());
            boolean patternMatched = "MATCHED".equals(result.getPattern());

            // Categorize
            if(!(Skipped_VERSION_FIELDS.contains(field) && result.getPattern().equals("MATCHED"))) {
            if (patternMatched && valueMatch) {
                matchedList.add(row);
            } else if (patternMatched && !isMandate) {
                mismatchList.add(row);
            } else {
                validationIssueList.add(row);
            }
        }
        }
        
        comparisionXmlResult.setXmlValidationIssue(validationIssueList);
     //  comparisionXmlResult.setXmlMatchIssue(matchedList);
        comparisionXmlResult.setXmlMissMatchIssue(mismatchList);

    //    System.out.println("-------------matchedList-----------------");
     //   matchedList.forEach(System.out::println);
      //  System.out.println("--------------mismatchList----------------");
     //   mismatchList.forEach(System.out::println);
     //   System.out.println("-------------validationIssueList-----------------");
       // validationIssueList.forEach(System.out::println);

        log.info("comparisionXmlResult : {}", comparisionXmlResult);
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