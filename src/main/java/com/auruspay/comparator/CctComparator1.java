
package com.auruspay.comparator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.*;

public class CctComparator1 {

	private static final ObjectMapper mapper = new ObjectMapper();

	// ================= ISO FIELD MODEL =================
	static class IsoField {
		String id;
		int minLength;
		int maxLength;
		String classType;
		String failedMsg;

		public IsoField(String id, int minLength, int maxLength, String classType, String failedMsg) {
			this.id = id;
			this.minLength = minLength;
			this.maxLength = maxLength;
			this.classType = classType;
			this.failedMsg = failedMsg;
		}
	}

	// ================= LOAD XML RULES =================
	public static Map<String, IsoField> loadRules(String xmlPath) throws Exception {

		Map<String, IsoField> rules = new HashMap<>();

		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File(xmlPath));

		NodeList list = doc.getElementsByTagName("isofield");

		for (int i = 0; i < list.getLength(); i++) {

			Element e = (Element) list.item(i);

			String id = e.getAttribute("id");
			int min = Integer.parseInt(e.getAttribute("minlength"));
			int max = Integer.parseInt(e.getAttribute("maxlength"));
			String type = e.getAttribute("classType");
			String msg = e.getAttribute("failedMsg");

			rules.put(id, new IsoField(id, min, max, type, msg));
		}

		return rules;
	}

	// ================= COMPARE =================
	public static List<Map<String, Object>> compare(String declinedJson, String approvedJson,
			Map<String, IsoField> rules) throws Exception {

		JsonNode declined = mapper.readTree(declinedJson);
		JsonNode approved = mapper.readTree(approvedJson);

		Set<String> fields = new TreeSet<>();
		declined.fieldNames().forEachRemaining(fields::add);
		approved.fieldNames().forEachRemaining(fields::add);

		List<Map<String, Object>> result = new ArrayList<>();

		for (String field : fields) {

			String dVal = declined.has(field) ? declined.get(field).asText() : "MISSING";
			String aVal = approved.has(field) ? approved.get(field).asText() : "MISSING";

			Map<String, Object> issue = validate(field, dVal, aVal, rules.get(field));

			if (issue != null) {
				result.add(issue);
			}
		}

		return result;
	}

	// ================= VALIDATION =================
	private static Map<String, Object> validate(String field, String declined, String approved, IsoField rule) {

		Map<String, Object> res = new LinkedHashMap<>();

		// 🔥 VALUE IN HEADER (FIELD KEY)
		res.put(field, declined + " | " + approved);

		// missing check
		if ("MISSING".equals(declined) || "MISSING".equals(approved)) {
			res.put("status", "FAIL");
			res.put("reason", "Missing field in one request");
			return res;
		}

		// XML RULE CHECK
		if (rule != null) {

			if (approved.length() < rule.minLength || approved.length() > rule.maxLength) {
				res.put("status", "FAIL");
				res.put("reason", "Length violation (" + rule.minLength + "-" + rule.maxLength + ")");
				return res;
			}

			if (!checkType(approved, rule.classType)) {
				res.put("status", "FAIL");
				res.put("reason", "Type mismatch (" + rule.classType + ")");
				return res;
			}
		}

		// VALUE CHECK
		if (!declined.equals(approved)) {
			res.put("status", "FAIL");
			res.put("reason", "Value mismatch");
			return res;
		}

		res.put("status", "PASS");
		res.put("reason", "Matched");

		return res;
	}

	// ================= TYPE CHECK =================
	private static boolean checkType(String value, String type) {

		if (value == null)
			return false;

		switch (type) {
		case "NUMERIC":
			return value.matches("^[0-9.]+$");

		case "ALPHA":
			return value.matches("^[a-zA-Z]+$");

		case "ALPHANUMERIC":
			return value.matches("^[a-zA-Z0-9%+=/._:-]+$");

		default:
			return true;
		}
	}

	// ================= MAIN =================
	public static void main(String[] args) throws Exception {

		String declinedJson = "{\"6.9\":\"12345\",\"4.11\":\"150.00\",\"4.13\":\"134001\",\"4.15\":\"1\",\"14.13\":\"1\",\"3.43\":\"6\",\"4.20\":\"840\",\"4.21\":\"840\",\"4.26\":\"1\",\"4.113\":\"1\",\"12.1\":\"0000002\",\"1.1\":\"100000139336\",\"1.2\":\"79794\",\"1.3\":\"59806551\",\"3.1\":\"17\",\"3.2\":\"AESDK\",\"3.3\":\"Reg01\",\"5.1\":\"0000002\",\"3.4\":\"00\",\"4.18\":\"05142026\",\"3.5\":\"26.03.065.001\",\"4.19\":\"202022\",\"5.3\":\"0000002\",\"7.1\":\"0.00\",\"3.6\":\"1.8\",\"7.2\":\"0.00\",\"3.7\":\"5.3.18-57-default\",\"7.3\":\"0.00\",\"3.8\":\"1\",\"7.5\":\"%1F%1F%1D\",\"5.8\":\"5.0874\",\"5.9\":\"01\",\"4.32\":\"3\",\"7.9\":\"%1F%1F%1D\",\"12.55\":\"10001\",\"4.79\":\"1\",\"4.38\":\"26.03.065.001\",\"3.21\":\"1.91\",\"4.42\":\"P2PE NOT SUPPORTED\",\"5.13\":\"01\",\"5.14\":\"01\",\"4.40\":\"00\",\"2.1\":\"C0N43N4U45RLN8R1\",\"2.2\":\"60:33:4b:10:42:98\",\"2.3\":\"192.168.255.243\",\"4.1\":\"3\",\"4.2\":\"000002\",\"4.3\":\"4\",\"4.4\":\"4761730111160043%5EUAT+USA%2FTEST+CARD+02++++++%5E3112201114380440000000000000000%7E4761730111160043%3D31122011303130600000\",\"4.5\":\"150.00\",\"4.6\":\"0.00\",\"4.7\":\"0.00\"}";

		String approvedJson = "\r\n"
				+ "{\"4.11\":\"500.00\",\"4.13\":\"127001\",\"4.15\":\"1\",\"14.13\":\"1\",\"3.43\":\"6\",\"4.20\":\"840\",\"4.21\":\"840\",\"4.26\":\"1\",\"4.113\":\"1\",\"12.1\":\"0000006\",\"1.1\":\"100000139336\",\"1.2\":\"79794\",\"1.3\":\"59806551\",\"3.1\":\"17\",\"3.2\":\"AESDK\",\"3.3\":\"Reg01\",\"5.1\":\"0000006\",\"3.4\":\"00\",\"4.18\":\"05072026\",\"3.5\":\"26.03.065.001\",\"4.19\":\"145940\",\"5.3\":\"0000006\",\"7.1\":\"0.00\",\"3.6\":\"1.8\",\"7.2\":\"0.00\",\"3.7\":\"5.3.18-57-default\",\"7.3\":\"0.00\",\"3.8\":\"1\",\"7.5\":\"%1F%1F%1D\",\"5.8\":\"5.0874\",\"5.9\":\"01\",\"4.32\":\"3\",\"7.9\":\"%1F%1F%1D\",\"12.55\":\"10001\",\"4.79\":\"1\",\"4.38\":\"26.03.065.001\",\"3.21\":\"1.91\",\"4.42\":\"P2PE NOT SUPPORTED\",\"5.13\":\"01\",\"5.14\":\"01\",\"4.40\":\"00\",\"2.1\":\"C0N43N4U45RLN8R1\",\"2.2\":\"60:33:4b:10:42:98\",\"2.3\":\"192.168.255.243\",\"4.1\":\"3\",\"4.2\":\"000017\",\"4.3\":\"4\",\"4.4\":\"424242XXXXXX4242%5EAAFES+TEST+VISA%5E17111011234567+++440%7E424242XXXXXX4242%3D17111011234567440\",\"4.5\":\"500.00\",\"4.6\":\"0.00\",\"4.7\":\"0.00\"}\r\n"
				+ "";

		Map<String, IsoField> rules = loadRules("src/main/resources/static/iso-fields-definition.xml");

		List<Map<String, Object>> result = compare(declinedJson, approvedJson, rules);

		System.out.println("===== VALIDATION RESULT =====");

		for (Map<String, Object> r : result) {
			System.out.println(r);
		}
	}
}

/*
package com.auruspay.comparator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.*;

public class CctComparator1 {

    private static final ObjectMapper mapper = new ObjectMapper();

    // ================= MODEL =================
    static class IsoField {
        String id;
        int minLength;
        int maxLength;
        String classType;
        String failedMsg;

        public IsoField(String id, int minLength, int maxLength, String classType, String failedMsg) {
            this.id = id;
            this.minLength = minLength;
            this.maxLength = maxLength;
            this.classType = classType;
            this.failedMsg = failedMsg;
        }
    }

    // ================= LOAD XML =================
    public static Map<String, IsoField> loadRules(String xmlPath) throws Exception {

        Map<String, IsoField> rules = new HashMap<>();

        Document doc = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .parse(new File(xmlPath));

        NodeList list = doc.getElementsByTagName("isofield");

        for (int i = 0; i < list.getLength(); i++) {

            Element e = (Element) list.item(i);

            String id = e.getAttribute("id");
            int min = Integer.parseInt(e.getAttribute("minlength"));
            int max = Integer.parseInt(e.getAttribute("maxlength"));
            String type = e.getAttribute("classType");
            String msg = e.getAttribute("failedMsg");

            rules.put(id, new IsoField(id, min, max, type, msg));
        }

        return rules;
    }

    // ================= MAIN COMPARE =================
    public static List<Map<String, Object>> compare(
            String declinedJson,
            String approvedJson,
            Map<String, IsoField> rules
    ) throws Exception {

        JsonNode declined = mapper.readTree(declinedJson);
        JsonNode approved = mapper.readTree(approvedJson);

        Set<String> allFields = new TreeSet<>();
        declined.fieldNames().forEachRemaining(allFields::add);
        approved.fieldNames().forEachRemaining(allFields::add);

        List<Map<String, Object>> result = new ArrayList<>();

        for (String field : allFields) {

            String dVal = declined.has(field) ? declined.get(field).asText() : "MISSING";
            String aVal = approved.has(field) ? approved.get(field).asText() : "MISSING";

            Map<String, Object> issue = validate(field, dVal, aVal, rules.get(field));

            if (issue != null) {
                result.add(issue);
            }
        }

        return result;
    }

    // ================= VALIDATION =================
    private static Map<String, Object> validate(
            String field,
            String declined,
            String approved,
            IsoField rule
    ) {

        Map<String, Object> res = new LinkedHashMap<>();

        res.put("field", field);
        res.put("declinedValue", declined);
        res.put("approvedValue", approved);

        // 1. missing check
        if ("MISSING".equals(declined) || "MISSING".equals(approved)) {
            res.put("status", "FAIL");
            res.put("reason", "Missing field in one request");
            return res;
        }

        // 2. XML rule validation
        if (rule != null) {

            if (approved.length() < rule.minLength || approved.length() > rule.maxLength) {
                res.put("status", "FAIL");
                res.put("reason", "Length violation (" + rule.minLength + "-" + rule.maxLength + ")");
                return res;
            }

            if (!checkType(approved, rule.classType)) {
                res.put("status", "FAIL");
                res.put("reason", "Type mismatch expected " + rule.classType);
                return res;
            }
        }

        // 3. value mismatch
        if (!declined.equals(approved)) {
            res.put("status", "FAIL");
            res.put("reason", "Value mismatch");
            return res;
        }

        return null; // means OK
    }

    // ================= TYPE CHECK =================
    private static boolean checkType(String value, String type) {

        switch (type) {
            case "NUMERIC":
                return value.matches("^[0-9.]+$");

            case "ALPHA":
                return value.matches("^[a-zA-Z]+$");

            case "ALPHANUMERIC":
                return value.matches("^[a-zA-Z0-9%+=/._:-]+$");

            default:
                return true;
        }
    }

    // ================= MAIN METHOD =================
    public static void main(String[] args) throws Exception {

        String declinedJson ="{\"6.9\":\"12345\",\"4.11\":\"150.00\",\"4.13\":\"134001\",\"4.15\":\"1\",\"14.13\":\"1\",\"3.43\":\"6\",\"4.20\":\"840\",\"4.21\":\"840\",\"4.26\":\"1\",\"4.113\":\"1\",\"12.1\":\"0000002\",\"1.1\":\"100000139336\",\"1.2\":\"79794\",\"1.3\":\"59806551\",\"3.1\":\"17\",\"3.2\":\"AESDK\",\"3.3\":\"Reg01\",\"5.1\":\"0000002\",\"3.4\":\"00\",\"4.18\":\"05142026\",\"3.5\":\"26.03.065.001\",\"4.19\":\"202022\",\"5.3\":\"0000002\",\"7.1\":\"0.00\",\"3.6\":\"1.8\",\"7.2\":\"0.00\",\"3.7\":\"5.3.18-57-default\",\"7.3\":\"0.00\",\"3.8\":\"1\",\"7.5\":\"%1F%1F%1D\",\"5.8\":\"5.0874\",\"5.9\":\"01\",\"4.32\":\"3\",\"7.9\":\"%1F%1F%1D\",\"12.55\":\"10001\",\"4.79\":\"1\",\"4.38\":\"26.03.065.001\",\"3.21\":\"1.91\",\"4.42\":\"P2PE NOT SUPPORTED\",\"5.13\":\"01\",\"5.14\":\"01\",\"4.40\":\"00\",\"2.1\":\"C0N43N4U45RLN8R1\",\"2.2\":\"60:33:4b:10:42:98\",\"2.3\":\"192.168.255.243\",\"4.1\":\"3\",\"4.2\":\"000002\",\"4.3\":\"4\",\"4.4\":\"4761730111160043%5EUAT+USA%2FTEST+CARD+02++++++%5E3112201114380440000000000000000%7E4761730111160043%3D31122011303130600000\",\"4.5\":\"150.00\",\"4.6\":\"0.00\",\"4.7\":\"0.00\"}";

        String approvedJson ="\r\n"
        		+ "{\"4.11\":\"500.00\",\"4.13\":\"127001\",\"4.15\":\"1\",\"14.13\":\"1\",\"3.43\":\"6\",\"4.20\":\"840\",\"4.21\":\"840\",\"4.26\":\"1\",\"4.113\":\"1\",\"12.1\":\"0000006\",\"1.1\":\"100000139336\",\"1.2\":\"79794\",\"1.3\":\"59806551\",\"3.1\":\"17\",\"3.2\":\"AESDK\",\"3.3\":\"Reg01\",\"5.1\":\"0000006\",\"3.4\":\"00\",\"4.18\":\"05072026\",\"3.5\":\"26.03.065.001\",\"4.19\":\"145940\",\"5.3\":\"0000006\",\"7.1\":\"0.00\",\"3.6\":\"1.8\",\"7.2\":\"0.00\",\"3.7\":\"5.3.18-57-default\",\"7.3\":\"0.00\",\"3.8\":\"1\",\"7.5\":\"%1F%1F%1D\",\"5.8\":\"5.0874\",\"5.9\":\"01\",\"4.32\":\"3\",\"7.9\":\"%1F%1F%1D\",\"12.55\":\"10001\",\"4.79\":\"1\",\"4.38\":\"26.03.065.001\",\"3.21\":\"1.91\",\"4.42\":\"P2PE NOT SUPPORTED\",\"5.13\":\"01\",\"5.14\":\"01\",\"4.40\":\"00\",\"2.1\":\"C0N43N4U45RLN8R1\",\"2.2\":\"60:33:4b:10:42:98\",\"2.3\":\"192.168.255.243\",\"4.1\":\"3\",\"4.2\":\"000017\",\"4.3\":\"4\",\"4.4\":\"424242XXXXXX4242%5EAAFES+TEST+VISA%5E17111011234567+++440%7E424242XXXXXX4242%3D17111011234567440\",\"4.5\":\"500.00\",\"4.6\":\"0.00\",\"4.7\":\"0.00\"}\r\n"
        		+ "";
      
        Map<String, IsoField> rules =
                loadRules("src/main/resources/static/iso-fields-definition.xml");

        List<Map<String, Object>> result =
                compare(declinedJson, approvedJson, rules);

        System.out.println("===== VALIDATION RESULT =====");

        result.forEach(System.out::println);
    }
}

*/