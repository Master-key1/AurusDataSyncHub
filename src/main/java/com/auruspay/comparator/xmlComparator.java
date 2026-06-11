package com.auruspay.comparator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.*;

import java.io.ByteArrayInputStream;
import java.util.LinkedHashMap;
import java.util.Map;

public class xmlComparator {

	public static void main(String[] args) throws Exception {
		String approvedXml = "\r\n"
				+ "<GMF xmlns=\"com/fiserv/Merchant/gmfV10.02\"><CreditRequest><CommonGrp><PymtType>Credit</PymtType><TxnType>Authorization</TxnType><LocalDateTime>20260507145941</LocalDateTime><TrnmsnDateTime>20260507185941</TrnmsnDateTime><STAN>195710</STAN><RefNum>507145941</RefNum><OrderNum>0000006</OrderNum><TPPID>RAU053</TPPID><TermID>00000001</TermID><MerchID>RD13317808</MerchID><MerchCatCode>5294</MerchCatCode><POSEntryMode>901</POSEntryMode><POSCondCode>00</POSCondCode><TermCatCode>05</TermCatCode><TermEntryCapablt>12</TermEntryCapablt><TxnAmt>000000050000</TxnAmt><TxnCrncy>840</TxnCrncy><TermLocInd>0</TermLocInd><CardCaptCap>1</CardCaptCap><GroupID>20001</GroupID><POSID>0001</POSID><MerchEcho>f2cb60ec-ba11-4edf-a2eb-9555f7c14bcb</MerchEcho></CommonGrp><CardGrp><Track2Data>424242XXXXXX4242=17111011234567440</Track2Data><CardType>Visa</CardType></CardGrp><AddtlAmtGrp><PartAuthrztnApprvlCapablt>1</PartAuthrztnApprvlCapablt></AddtlAmtGrp><VisaGrp><ACI>Y</ACI></VisaGrp></CreditRequest></GMF>\r\n"
				+ "";
		String declinedXml = "\r\n"
				+ "<GMF xmlns=\"com/fiserv/Merchant/gmfV10.02\"><CreditRequest><CommonGrp><PymtType>Credit</PymtType><TxnType>Authorization</TxnType><LocalDateTime>20260514202026</LocalDateTime><TrnmsnDateTime>20260515002026</TrnmsnDateTime><STAN>088789</STAN><RefNum>514202026</RefNum><OrderNum>0000002</OrderNum><TPPID>RAU053</TPPID><TermID>00000001</TermID><MerchID>RD13317808</MerchID><MerchCatCode>5294</MerchCatCode><POSEntryMode>901</POSEntryMode><POSCondCode>00</POSCondCode><TermCatCode>05</TermCatCode><TermEntryCapablt>12</TermEntryCapablt><TxnAmt>000000015000</TxnAmt><TxnCrncy>840</TxnCrncy><TermLocInd>0</TermLocInd><CardCaptCap>1</CardCaptCap><GroupID>20001</GroupID><POSID>0001</POSID><MerchEcho>4936cd0c-f0cb-4c19-8622-b3a248028445</MerchEcho></CommonGrp><CardGrp><Track2Data>4761730111160043=31122011303130600000</Track2Data><CardType>Visa</CardType></CardGrp><AddtlAmtGrp><PartAuthrztnApprvlCapablt>1</PartAuthrztnApprvlCapablt></AddtlAmtGrp><VisaGrp><ACI>Y</ACI></VisaGrp><CustInfoGrp><AVSBillingPostalCode>12345</AVSBillingPostalCode></CustInfoGrp></CreditRequest></GMF>\r\n"
				+ "";

		Map<String, String> approvedMap = extractAll(approvedXml);
		Map<String, String> declinedMap = extractAll(declinedXml);

		smartCompare(approvedMap, declinedMap);
	}

	// =========================
	// XML PARSER (DYNAMIC)
	// =========================
	private static Map<String, String> extractAll(String xml) throws Exception {

		Map<String, String> map = new LinkedHashMap<>();

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();

		Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes()));
		doc.getDocumentElement().normalize();

		traverse(doc.getDocumentElement(), map);

		return map;
	}

	private static void traverse(Node node, Map<String, String> map) {

		NodeList children = node.getChildNodes();
		boolean hasChildElement = false;

		for (int i = 0; i < children.getLength(); i++) {

			Node child = children.item(i);

			if (child.getNodeType() == Node.ELEMENT_NODE) {
				hasChildElement = true;
				traverse(child, map);
			}
		}

		if (!hasChildElement && node.getNodeType() == Node.ELEMENT_NODE) {

			String tag = node.getNodeName();
			String value = node.getTextContent().trim();

			if (!value.isEmpty()) {
				map.put(tag, value);
			}
		}
	}

	// =========================
	// INTELLIGENT COMPARATOR
	// =========================
	private static void smartCompare(Map<String, String> approved, Map<String, String> declined) {

		System.out.println("=== INTELLIGENT GMF ANALYSIS ===\n");

		Map<String, String> all = new LinkedHashMap<>();
		all.putAll(approved);
		declined.forEach(all::putIfAbsent);

		for (String tag : all.keySet()) {

			String a = approved.get(tag);
			String d = declined.get(tag);

			if (a != null && d != null) {

				if (a.equals(d)) {
					System.out.println(tag + " : " + a + " : " + d + " [MATCH]");
				} else {

				//	String reason = getReason(tag, a, d);
					System.out.println(tag + " : " + a + " : " + d + " [DIFF] ");
				}

			} else if (a != null) {

				System.out.println(tag + " : " + a + " : NULL [ONLY APPROVED]");

			} else {

				System.out.println(tag + " : NULL : " + d + " [ONLY DECLINED]");
			}
		}
	}

	// =========================
	// RULE ENGINE (INTELLIGENCE)
	// =========================
	private static String getReason(String tag, String a, String d) {

		switch (tag) {

		case "TxnAmt":
			return "[RISK] Amount mismatch → possible issuer/fraud rule trigger";

		case "Track2Data":
			return "[CRITICAL] Card/BIN mismatch → different card or issuer decline";

		case "PymtType":
			return "[CRITICAL] Payment type mismatch → request routing issue";

		case "AVSBillingPostalCode":
			return "[RISK] AVS mismatch → address verification failed";

		case "LocalDateTime":
		case "TrnmsnDateTime":
			return "[INFO] Timing difference → not a failure reason";

		case "STAN":
		case "RefNum":
		case "OrderNum":
			return "[INFO] System identifier change → ignore for decline analysis";

		default:
			return "[INFO] Field changed but no rule mapped";
		}
	}
}